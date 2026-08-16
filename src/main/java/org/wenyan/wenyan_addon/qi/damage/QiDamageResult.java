package org.wenyan.wenyan_addon.qi.damage;

/**
 * 五行伤害计算结果。
 */
public record QiDamageResult(double damage, boolean critical) {

    public static QiDamageResult of(double damage, boolean critical) {
        return new QiDamageResult(Math.max(0, damage), critical);
    }
}
