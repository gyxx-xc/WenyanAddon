package org.wenyan.wenyan_addon.qi.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.wenyan.wenyan_addon.qi.damage.QiDamageHelper;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.List;

/**
 * 法术触发统一入口：实体碰撞/方块接触/物品攻击/药水效果 tick 均通过此触发伤害应用。
 * 施法者为 null（非玩家来源）时不造成伤害。
 */
public final class QiSpellTriggers {
    private QiSpellTriggers() {
    }

    /**
     * 触发：法术实体命中目标（含复数属性多段伤害）。
     */
    public static void onEntityHit(ServerLevel level, ServerPlayer caster,
                                   LivingEntity target, QiSpellSource source) {
        if (caster == null || target == null || !target.isAlive()) {
            return;
        }
        List<ElementAttribute> elements = source.spellElements();
        if (elements.size() > 1) {
            QiDamageHelper.applyCompoundDamage(level, caster, target, elements, source.baseDamage());
        } else if (!elements.isEmpty()) {
            QiDamageHelper.applyDamage(level, caster, target, elements.get(0), source.baseDamage());
        }
    }

    /**
     * 触发：实体踩上/接触法术方块。
     */
    public static void onBlockContact(ServerLevel level, ServerPlayer caster,
                                      LivingEntity target, QiSpellBlock block) {
        onEntityHit(level, caster, target, block);
    }

    /**
     * 触发：法术物品攻击目标（手持物品命中/投掷命中）。
     */
    public static void onItemAttack(ServerLevel level, ServerPlayer caster,
                                    LivingEntity target, QiSpellItem item) {
        onEntityHit(level, caster, target, item);
    }

    /**
     * 触发：法术药水效果 tick 伤害（施法者需通过 {@link QiSpellEffectContext} 存储）。
     */
    public static void onEffectTick(ServerLevel level, ServerPlayer caster,
                                    LivingEntity target, QiSpellMobEffect effect,
                                    MobEffectInstance instance) {
        if (caster == null || target == null || !target.isAlive() || instance.getDuration() <= 0) {
            return;
        }
        double base = effect.baseDamage() * instance.getAmplifier() / 20.0;
        onEntityHit(level, caster, target, new ScaledSource(effect, base));
    }

    /**
     * 缩放基础伤害的包装来源（药水效果按 tick 分摊）。
     */
    private record ScaledSource(QiSpellSource delegate, double scaledDamage) implements QiSpellSource {
        @Override
        public List<ElementAttribute> spellElements() {
            return delegate.spellElements();
        }

        @Override
        public double baseDamage() {
            return scaledDamage;
        }
    }
}
