package org.wenyan.wenyan_addon.qi.damage;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.async.QiAsyncExecutor;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.RelationType;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;

import java.util.List;
import net.minecraft.util.RandomSource;

/**
 * 五行伤害应用（主线程）：取攻击者/受害者系数 → 异步计算 → 主线程 hurt。
 * 伤害来源 = 施法者；伤害类型 = 属性对应的 qi_<id>。
 */
public final class QiDamageHelper {
    private QiDamageHelper() {
    }

    /**
     * 对目标造成属性伤害（来源=施法者）。
     *
     * @param element    伤害属性
     * @param baseDamage 法术基础伤害
     */
    public static void applyDamage(ServerLevel level, ServerPlayer caster,
                                   LivingEntity target, ElementAttribute element, double baseDamage) {
        if (!isDamageTypeLoaded(level, element)) {
            return;
        }
        QiDamageSnapshot attacker = QiDamageSnapshot.attacker(
                PlayerQi.of(caster).coefficients(element));
        QiDamageSnapshot victim = victimSnapshot(target, element);
        RandomSource random = level.getRandom();
        DamageSource source = level.damageSources().source(
                QiDamageTypes.keyOf(element), caster, caster);

        QiAsyncExecutor.submit(
                () -> QiDamageCalculator.calculate(attacker, victim, baseDamage, random),
                result -> level.getServer().execute(() -> {
                    if (!target.isRemoved() && target.isAlive() && result.damage() > 0) {
                        // 盔甲穿透：hurt 前临时调低目标护甲，hurt 后恢复（护甲减伤仅由 hurt 计算一次）
                        var armorAttr = target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
                        double originalArmor = armorAttr != null ? armorAttr.getBaseValue() : 0.0;
                        double reducedArmor = originalArmor * (1.0 - attacker.armorPenetrationPercent());
                        if (armorAttr != null && reducedArmor != originalArmor) {
                            armorAttr.setBaseValue(reducedArmor);
                        }
                        try {
                            target.hurt(source, (float) result.damage());
                            // 击退：按属性击退值（方向 = 施法者指向目标）
                            double knockback = attacker.knockback();
                            if (knockback > 0) {
                                double dx = target.getX() - caster.getX();
                                double dz = target.getZ() - caster.getZ();
                                target.knockback((float) knockback, dx, dz);
                            }
                        } finally {
                            if (armorAttr != null && reducedArmor != originalArmor) {
                                armorAttr.setBaseValue(originalArmor);
                            }
                        }
                    }
                }),
                () -> QiDamageCalculator.calculate(attacker, victim, baseDamage, random));
    }

    /**
     * 复合属性伤害：按各属性灵气值占比拆分基础伤害为多段属性伤害。
     * 每段独立计算（各属性系数/暴击/抗性）并独立 hurt——多段红字表现；
     * 段间清除目标无敌帧，保证同一次施法批次内所有段全部命中（不被无敌帧吞伤）。
     * 频率由施法次数控制（一次施法 = 一个批次），不会出现每 tick 重复触发。
     *
     * @param elements 复数属性（空 → 不造成伤害）
     */
    public static void applyCompoundDamage(ServerLevel level, ServerPlayer caster,
                                           LivingEntity target, List<ElementAttribute> elements,
                                           double baseDamage) {
        if (elements.isEmpty() || baseDamage <= 0) {
            return;
        }
        PlayerQiData casterQi = PlayerQi.of(caster);
        double totalQi = 0;
        for (ElementAttribute element : elements) {
            totalQi += casterQi.get(element);
        }
        if (totalQi <= 0) {
            // 无对应灵气值：等分拆分
            totalQi = elements.size();
        }
        // 批次开始：清除目标无敌帧（仅本批次内，多段全部命中）
        target.invulnerableTime = 0;
        for (ElementAttribute element : elements) {
            double share = casterQi.get(element) / totalQi;
            double segmentDamage = baseDamage * share;
            if (segmentDamage <= 0) {
                continue;
            }
            applyDamageNow(level, caster, target, element, segmentDamage);
        }
    }

    /**
     * 同步立即伤害（复合属性多段批次内使用）：计算 → 段前清无敌帧 → hurt。
     */
    private static void applyDamageNow(ServerLevel level, ServerPlayer caster,
                                       LivingEntity target, ElementAttribute element, double baseDamage) {
        QiDamageSnapshot attacker = QiDamageSnapshot.attacker(PlayerQi.of(caster).coefficients(element));
        QiDamageSnapshot victim = victimSnapshot(target, element);
        QiDamageResult result = QiDamageCalculator.calculate(attacker, victim, baseDamage, level.getRandom());
        if (target.isRemoved() || !target.isAlive() || result.damage() <= 0) {
            return;
        }
        if (!isDamageTypeLoaded(level, element)) {
            return;
        }
        // 盔甲穿透：hurt 前临时调低目标护甲，hurt 后恢复
        var armorAttr = target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
        double originalArmor = armorAttr != null ? armorAttr.getBaseValue() : 0.0;
        double reducedArmor = originalArmor * (1.0 - attacker.armorPenetrationPercent());
        if (armorAttr != null && reducedArmor != originalArmor) {
            armorAttr.setBaseValue(reducedArmor);
        }
        DamageSource source = level.damageSources().source(
                QiDamageTypes.keyOf(element), caster, caster);
        try {
            target.invulnerableTime = 0;
            target.hurt(source, (float) result.damage());
            double knockback = attacker.knockback();
            if (knockback > 0) {
                double dx = target.getX() - caster.getX();
                double dz = target.getZ() - caster.getZ();
                target.knockback((float) knockback, dx, dz);
            }
        } finally {
            if (armorAttr != null && reducedArmor != originalArmor) {
                armorAttr.setBaseValue(originalArmor);
            }
        }
    }

    /**
     * 属性伤害类型是否已加载（datapack JSON 未生效时跳过，防止构造伤害源抛异常）。
     */
    private static boolean isDamageTypeLoaded(ServerLevel level, ElementAttribute element) {
        Registry<DamageType> registry = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
        if (!QiDamageTypes.isRegistered(registry, element)) {
            WenyanAddon.LOGGER.warn("属性伤害类型未加载，跳过伤害: {}",
                    QiDamageTypes.keyOf(element).identifier());
            return false;
        }
        return true;
    }

    /**
     * 受害者快照：玩家仅主属性参与被攻击的五行减伤（与主属性有关系时用主属性系数）；
     * 辅属性不参与被攻击减伤；非玩家实体用属性默认系数。
     */
    private static QiDamageSnapshot victimSnapshot(LivingEntity target, ElementAttribute element) {
        if (target instanceof ServerPlayer player) {
            PlayerQiData qi = PlayerQi.of(player);
            if (qi.hasMainElement()) {
                ElementAttribute main = org.wenyan.wenyan_addon.qi.element.ElementRegistry.byId(qi.mainElement());
                if (main != null) {
                    RelationType relation = org.wenyan.wenyan_addon.qi.element.ElementRelations.relation(element, main);
                    if (relation != RelationType.NONE) {
                        // 主属性参与被攻击：应用主属性的减伤系数
                        return QiDamageSnapshot.victim(qi.coefficients(main));
                    }
                }
            }
            // 辅属性：不参与被攻击减伤
            return QiDamageSnapshot.victim(element.defaultCoefficients());
        }
        return QiDamageSnapshot.victim(element.defaultCoefficients());
    }
}
