package org.wenyan.wenyan_addon.qi.damage;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;

/**
 * 五行伤害参与者快照：攻击者/受害者的属性伤害参数（纯数值，异步可用）。
 * 攻击者：伤害倍率/加深/暴击/暴击伤害/抗性穿透/穿透%/盔甲穿透。
 * 受害者：伤害减免/抗性/穿透。
 */
public final class QiDamageSnapshot {
    private final double damageMultiplier;
    private final double damageReduction;
    private final double damageAmplification;
    private final double criticalChance;
    private final double criticalDamageMultiplier;
    private final long damageResistance;
    private final long resistancePenetration;
    private final double resistancePenetrationPercent;
    private final double armorPenetrationPercent;
    private final double knockback;

    private QiDamageSnapshot(double damageMultiplier, double damageReduction, double damageAmplification,
                             double criticalChance, double criticalDamageMultiplier,
                             long damageResistance, long resistancePenetration,
                             double resistancePenetrationPercent, double armorPenetrationPercent,
                             double knockback) {
        this.damageMultiplier = damageMultiplier;
        this.damageReduction = damageReduction;
        this.damageAmplification = damageAmplification;
        this.criticalChance = criticalChance;
        this.criticalDamageMultiplier = criticalDamageMultiplier;
        this.damageResistance = damageResistance;
        this.resistancePenetration = resistancePenetration;
        this.resistancePenetrationPercent = resistancePenetrationPercent;
        this.armorPenetrationPercent = armorPenetrationPercent;
        this.knockback = knockback;
    }

    public static QiDamageSnapshot attacker(ElementCoefficients c) {
        return new QiDamageSnapshot(c.damageMultiplier(), 0, c.damageAmplification(),
                c.criticalChance(), c.criticalDamageMultiplier(),
                0, c.resistancePenetration(),
                c.resistancePenetrationPercent(), c.armorPenetrationPercent(),
                c.knockback());
    }

    public static QiDamageSnapshot victim(ElementCoefficients c) {
        return new QiDamageSnapshot(1, c.damageReduction(), 0,
                0, 0,
                c.damageResistance(), 0,
                0, 0,
                0);
    }

    public double damageMultiplier() {
        return damageMultiplier;
    }

    public double damageReduction() {
        return damageReduction;
    }

    public double damageAmplification() {
        return damageAmplification;
    }

    public double criticalChance() {
        return criticalChance;
    }

    public double criticalDamageMultiplier() {
        return criticalDamageMultiplier;
    }

    public long damageResistance() {
        return damageResistance;
    }

    public long resistancePenetration() {
        return resistancePenetration;
    }

    public double resistancePenetrationPercent() {
        return resistancePenetrationPercent;
    }

    public double armorPenetrationPercent() {
        return armorPenetrationPercent;
    }

    public double knockback() {
        return knockback;
    }
}
