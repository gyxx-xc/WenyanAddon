package org.wenyan.wenyan_addon.qi.mark;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
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
    /**
     * 灵石基础掉落概率（1 级标记）；每级 ×等级（5 级 = 5 倍）。
     */
    private static final double BASE_DROP_CHANCE = 0.10;

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
        // 攻击者携带标记 → 取消原伤害，重建为对应属性的灵气伤害（非灵气伤害才转换，避免递归）
        if (attacker instanceof LivingEntity attackerLiving && !QiDamageTypes.isQiDamage(source)) {
            ElementMarkInstance mark = markOf(attackerLiving);
            if (mark != null && mark.attribute() != null && mark.attribute() != ElementType.NEUTRAL
                    && attackerLiving.level() instanceof ServerLevel serverLevel) {

                float amount = event.getAmount();
                if (amount <= 0 || event.isCanceled()) {
                    return;
                }

                // 对应属性伤害类型未加载（datapack JSON 未生效）→ 跳过转换
                Registry<DamageType> damageRegistry = serverLevel.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE);
                if (!QiDamageTypes.isRegistered(damageRegistry, mark.attribute())) {
                    WenyanAddon.LOGGER.warn("属性伤害类型未加载，跳过转换: {}",
                            QiDamageTypes.keyOf(mark.attribute()).identifier());
                    return;
                }

                LivingEntity victim = event.getEntity();
                if (victim == null || !victim.isAlive() || victim.isRemoved()) {
                    return;
                }

                // 取消原伤害，用属性伤害类型重新造成伤害（正常护甲/无敌帧/减伤流程）
                event.setCanceled(true);
                victim.hurt(serverLevel.damageSources().source(
                        QiDamageTypes.keyOf(mark.attribute()), attacker, attacker), amount);
            }
        }
        // 受害者携带标记 → 对该属性伤害获得抗性
        ElementMarkInstance victimMark = markOf(event.getEntity());
        if (victimMark != null && victimMark.attribute() != null) {
            ResourceKey<DamageType> key = QiDamageTypes.keyOf(victimMark.attribute());
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
        // 强化生物（带标记）死亡 → 掉落灵石（纯度随机：杂质 5-30% / 纯质 50-70% / 精纯 90-100%）
        // 掉落概率随标记等级提升：基础 10% × 等级（等级 1 = 10%，等级 5 = 50%）
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ElementMarkInstance mark = markOf(event.getEntity());
        if (mark == null) {
            return;
        }
        double dropChance = BASE_DROP_CHANCE * mark.markLevel();
        if (serverLevel.getRandom().nextDouble() >= dropChance) {
            return;
        }
        double roll = serverLevel.getRandom().nextDouble();
        double purity;
        if (roll < 0.5) {
            purity = 0.05 + serverLevel.getRandom().nextDouble() * 0.25;      // 杂质 5-30%
        } else if (roll < 0.9) {
            purity = 0.50 + serverLevel.getRandom().nextDouble() * 0.20;      // 纯质 50-70%
        } else {
            purity = 0.90 + serverLevel.getRandom().nextDouble() * 0.10;      // 精纯 90-100%
        }
        var grade = org.wenyan.wenyan_addon.qi.storage.SpiritStoneItem.gradeOf(purity);
        net.minecraft.world.item.ItemStack stone = switch (grade) {
            case IMPURE -> org.wenyan.wenyan_addon.WenyanAddon.SPIRIT_STONE_IMPURE_ITEM.get().getDefaultInstance();
            case REFINED -> org.wenyan.wenyan_addon.WenyanAddon.SPIRIT_STONE_REFINED_ITEM.get().getDefaultInstance();
            case PURE -> org.wenyan.wenyan_addon.WenyanAddon.SPIRIT_STONE_ITEM.get().getDefaultInstance();
        };
        ElementAttribute attribute = mark.attribute();
        org.wenyan.wenyan_addon.qi.storage.SpiritStoneContainer.setPurity(stone, purity, attribute);
        org.wenyan.wenyan_addon.qi.storage.SpiritStoneItem.applyName(stone, attribute, grade);
        event.getEntity().spawnAtLocation(serverLevel, stone);
    }
}
