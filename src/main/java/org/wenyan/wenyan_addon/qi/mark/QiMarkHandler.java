package org.wenyan.wenyan_addon.qi.mark;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiManager;
import org.wenyan.wenyan_addon.qi.damage.QiDamageHelper;
import org.wenyan.wenyan_addon.qi.damage.QiDamageTypes;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementType;

/**
 * 生物灵气属性标记：生物生成 15% 概率附加无限时标记（等级 1-5，属性由生成区块决定）。
 * 标记生物造成伤害时附加属性伤害；受伤时对该属性伤害获得抗性。
 */
@EventBusSubscriber(modid = WenyanAddon.MODID)
public final class QiMarkHandler {
    private static final double MARK_CHANCE = 0.15;

    private QiMarkHandler() {
    }

    @SubscribeEvent
    public static void onMobSpawn(MobSpawnEvent.PositionCheck event) {
        if (!(event.getEntity() instanceof Mob mob) || mob instanceof Animal) {
            return;
        }
        if (event.getLevel().getRandom().nextDouble() >= MARK_CHANCE) {
            return;
        }
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        ElementAttribute attribute = markAttribute(serverLevel, new BlockPos(
                (int) event.getX(), (int) event.getY(), (int) event.getZ()));
        Holder<MobEffect> effect = QiMarkEffects.holderOf(attribute);
        if (effect == null || mob.hasEffect(effect)) {
            return;
        }
        int amplifier = serverLevel.getRandom().nextInt(5); // 0-4 = 等级 1-5
        mob.addEffect(new ElementMarkInstance(effect, amplifier, attribute));
    }

    /**
     * 生成时所在区块决定标记属性；无趋向（末地/无主群系）→ 无属性。
     */
    private static ElementAttribute markAttribute(ServerLevel level, BlockPos pos) {
        ChunkQiManager manager = ChunkQiManager.of(level);
        return manager.preferredElement(level, ChunkPos.containing(pos));
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        // 测试：打印玩家受到的所有伤害（来源类型 + 数值）
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            var key = event.getSource().typeHolder().unwrapKey().orElse(null);
            String typeName = key != null ? key.identifier().toString() : "generic";
            org.wenyan.wenyan_addon.WenyanAddon.LOGGER.info(
                    "[受伤] {} 受到 {} 伤害: {}", player.getName().getString(), typeName, event.getAmount());
        }
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity attackerLiving) {
            // 攻击者携带标记 → 附加标记属性伤害（原伤害 × 等级比例）
            ElementMarkInstance mark = markOf(attackerLiving);
            if (mark != null && mark.attribute() != null && mark.attribute() != ElementType.NEUTRAL
                    && attackerLiving.level() instanceof ServerLevel serverLevel) {
                double bonus = event.getAmount() * mark.bonusRatio();
                if (bonus > 0 && attackerLiving instanceof net.minecraft.server.level.ServerPlayer caster) {
                    QiDamageHelper.applyDamage(serverLevel, caster,
                            event.getEntity(), mark.attribute(), bonus);
                } else if (bonus > 0) {
                    // 非玩家攻击者：无视无敌帧直接附加属性伤害（含属性击退）
                    LivingEntity victim = event.getEntity();
                    victim.invulnerableTime = 0;
                    victim.hurt(serverLevel.damageSources().generic(), (float) bonus);
                    double knockback = mark.attribute().defaultCoefficients().knockback();
                    if (knockback > 0) {
                        double dx =attackerLiving.getX() - victim.getX();
                        double dz =attackerLiving.getZ() - victim.getZ();
                        victim.knockback((float) knockback, dx, dz);
                    }
                }
            }
        }
        // 受害者携带标记 → 对该属性伤害获得抗性
        ElementMarkInstance victimMark = markOf(event.getEntity());
        if (victimMark != null && victimMark.attribute() != null) {
            var key = QiDamageTypes.keyOf(victimMark.attribute());
            if (key != null && source.is(key)) {
                float reduction = (float) victimMark.resistanceRatio();
                event.setAmount(event.getAmount() * (1.0f - reduction));
            }
        }
    }

    /**
     * 查找实体的灵气标记（遍历效果，按 effect 反查属性）。
     */
    private static ElementMarkInstance markOf(LivingEntity entity) {
        if (entity == null) {
            return null;
        }
        for (var instance : entity.getActiveEffects()) {
            ElementAttribute attribute = QiMarkEffects.attributeOf(instance.getEffect());
            if (attribute != null) {
                return instance instanceof ElementMarkInstance mark
                        ? mark
                        : new ElementMarkInstance(instance.getEffect(), instance.getAmplifier(), attribute);
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onLivingDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        // 强化生物（带标记）死亡 → 掉灵石（纯度随机：杂质 5-30% / 纯质 50-70% / 精纯 90-100%）
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ElementMarkInstance mark = markOf(event.getEntity());
        if (mark == null) {
            return;
        }
        net.minecraft.world.item.ItemStack stone = org.wenyan.wenyan_addon.WenyanAddon.SPIRIT_STONE_ITEM.get()
                .getDefaultInstance();
        double roll = serverLevel.getRandom().nextDouble();
        double purity;
        if (roll < 0.5) {
            purity = 0.05 + serverLevel.getRandom().nextDouble() * 0.25;      // 杂质 5-30%
        } else if (roll < 0.9) {
            purity = 0.50 + serverLevel.getRandom().nextDouble() * 0.20;      // 纯质 50-70%
        } else {
            purity = 0.90 + serverLevel.getRandom().nextDouble() * 0.10;      // 精纯 90-100%
        }
        org.wenyan.wenyan_addon.qi.storage.SpiritStoneContainer.setPurity(stone, purity);
        event.getEntity().spawnAtLocation(serverLevel, stone);
    }
}
