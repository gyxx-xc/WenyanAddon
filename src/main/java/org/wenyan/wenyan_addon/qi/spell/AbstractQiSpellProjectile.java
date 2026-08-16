package org.wenyan.wenyan_addon.qi.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.wenyan.wenyan_addon.qi.damage.QiDamageHelper;

/**
 * 法术投射物基类：实现 {@link QiSpellSource}（单属性法术）。
 * 飞行碰撞实体/方块时自动触发五行伤害（来源=施法者）。
 * 复数属性法术可覆写 {@link #spellElements()}。
 */
public abstract class AbstractQiSpellProjectile extends ThrowableProjectile implements QiSpellSource {
    private boolean hitDone = false;

    protected AbstractQiSpellProjectile(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    protected AbstractQiSpellProjectile(EntityType<? extends ThrowableProjectile> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level);
    }

    /**
     * 基础伤害：默认取 {@link #baseDamage()}，可覆写组合文言函数传入量。
     */
    protected double currentDamage() {
        return baseDamage();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (hitDone || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        hitDone = true;
        if (hitResult.getType() == HitResult.Type.ENTITY && hitResult instanceof EntityHitResult entityHit) {
            Entity hitEntity = entityHit.getEntity();
            if (hitEntity instanceof LivingEntity target && !target.is(getOwner())) {
                applySpellDamage(serverLevel, target);
            }
        } else if (hitResult instanceof BlockHitResult) {
            onHitBlock(serverLevel);
        }
        discard();
    }

    /**
     * 应用法术伤害：单属性直接伤害，复数属性多段伤害。
     */
    protected void applySpellDamage(ServerLevel serverLevel, LivingEntity target) {
        Entity owner = getOwner();
        if (!(owner instanceof ServerPlayer caster)) {
            return;
        }
        var elements = spellElements();
        if (elements.size() > 1) {
            QiDamageHelper.applyCompoundDamage(serverLevel, caster, target, elements, currentDamage());
        } else if (!elements.isEmpty()) {
            QiDamageHelper.applyDamage(serverLevel, caster, target, elements.get(0), currentDamage());
        }
    }

    /**
     * 命中方块（默认无效果，可覆写如生成粒子/方块效果）。
     */
    protected void onHitBlock(ServerLevel serverLevel) {
    }
}
