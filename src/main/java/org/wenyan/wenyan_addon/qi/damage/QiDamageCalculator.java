package org.wenyan.wenyan_addon.qi.damage;

import java.util.random.RandomGenerator;

/**
 * 五行伤害计算器（纯计算，可异步）：
 * 基础伤害 × 攻击者伤害倍率 × (1 + 攻击者伤害加深)
 * → 暴击判定：概率 criticalChance → ×(1 + criticalDamageMultiplier)
 * → 减伤：×(1 - 受害者伤害减免)
 * → 抗性：effectiveResistance = max(0, 受害者抗性 - 攻击者穿透)
 *         ×(1 - effectiveResistance × 0.01) ×(1 - 攻击者穿透%)
 * → 盔甲穿透% 由外部应用时使用（无视护甲）
 */
public final class QiDamageCalculator {
    private QiDamageCalculator() {
    }

    public static QiDamageResult calculate(QiDamageSnapshot attacker, QiDamageSnapshot victim,
                                           double baseDamage, RandomGenerator random) {
        double damage = baseDamage * attacker.damageMultiplier();
        damage *= 1.0 + attacker.damageAmplification();

        boolean critical = attacker.criticalChance() > 0
                && random.nextDouble() < attacker.criticalChance();
        if (critical) {
            damage *= 1.0 + attacker.criticalDamageMultiplier();
        }

        // 减伤
        damage *= 1.0 - victim.damageReduction();

        // 抗性（攻击者穿透值 vs 受害者抗性 + 穿透百分比）
        double effectiveResistance = Math.max(0, victim.damageResistance() - attacker.resistancePenetration());
        damage *= 1.0 - effectiveResistance * 0.01;
        damage *= 1.0 - attacker.resistancePenetrationPercent();

        return QiDamageResult.of(damage, critical);
    }
}
