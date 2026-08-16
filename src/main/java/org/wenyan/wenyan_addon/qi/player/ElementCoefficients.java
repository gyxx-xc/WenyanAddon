package org.wenyan.wenyan_addon.qi.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 单个属性的灵气参数集：全部参数以键值对存入 Map（可无限扩展），
 * 提供类型安全便捷 getter 与通用读写接口。
 * 每属性一套（{@link org.wenyan.wenyan_addon.qi.element.ElementAttribute#defaultCoefficients()}），
 * 玩家可实时修改。
 */
public record ElementCoefficients(Map<String, Double> values) {

    // ===== 键名常量 =====

    public static final String GENERATE_COEFFICIENT = "generateCoefficient";
    public static final String SAME_COEFFICIENT = "sameCoefficient";
    public static final String NEUTRAL_COEFFICIENT = "neutralCoefficient";
    public static final String COUNTER_COEFFICIENT = "counterCoefficient";
    public static final String GENERATE_BOOST = "generateBoost";
    public static final String COUNTER_REDUCE = "counterReduce";
    public static final String YIN_YANG_COST_RATIO = "yinYangCostRatio";
    public static final String YIN_MATCH_COEFFICIENT = "yinMatchCoefficient";
    public static final String YIN_MATCH_GAIN = "yinMatchGain";
    public static final String YIN_MISMATCH_COEFFICIENT = "yinMismatchCoefficient";
    public static final String YIN_MISMATCH_GAIN = "yinMismatchGain";
    public static final String RESTORE_RATE = "restoreRate";
    public static final String RESTORE_AMOUNT = "restoreAmount";
    public static final String ENVIRONMENT_GAIN_BASE = "environmentGainBase";
    public static final String ENVIRONMENT_RATIO_WEIGHT = "environmentRatioWeight";
    public static final String ENVIRONMENT_MAIN_RATIO = "environmentMainRatio";
    public static final String ENVIRONMENT_SUB_RATIO = "environmentSubRatio";
    public static final String YIN_YANG_RESTORE_RATE = "yinYangRestoreRate";
    public static final String VEIN_STAGE_GAIN = "veinStageGain";
    public static final String DAMAGE_MULTIPLIER = "damageMultiplier";
    public static final String DAMAGE_REDUCTION = "damageReduction";
    public static final String DAMAGE_AMPLIFICATION = "damageAmplification";
    public static final String CRITICAL_CHANCE = "criticalChance";
    public static final String CRITICAL_DAMAGE_MULTIPLIER = "criticalDamageMultiplier";
    public static final String DAMAGE_RESISTANCE = "damageResistance";
    public static final String RESISTANCE_PENETRATION = "resistancePenetration";
    public static final String RESISTANCE_PENETRATION_PERCENT = "resistancePenetrationPercent";
    public static final String ARMOR_PENETRATION_PERCENT = "armorPenetrationPercent";
    public static final String KNOCKBACK = "knockback";

    /**
     * 默认参数集（全属性标准值）。
     */
    public static final ElementCoefficients DEFAULT = new Builder()
            .generateCoefficient(0.6)
            .sameCoefficient(0.8)
            .neutralCoefficient(1.0)
            .counterCoefficient(1.2)
            .generateBoost(1.2)
            .counterReduce(0.8)
            .yinYangCostRatio(0.3)
            .yinMatchCoefficient(0.8)
            .yinMatchGain(1.2)
            .yinMismatchCoefficient(1.3)
            .yinMismatchGain(0.8)
            .restoreRate(1.0)
            .restoreAmount(2.0)
            .environmentGainBase(1.0)
            .environmentRatioWeight(1.0)
            .environmentMainRatio(0.8)
            .environmentSubRatio(0.2)
            .yinYangRestoreRate(1.0)
            .veinStageGain(0.1)
            .damageMultiplier(1.0)
            .damageReduction(0.0)
            .damageAmplification(0.0)
            .criticalChance(0.0)
            .criticalDamageMultiplier(0.5)
            .damageResistance(0)
            .resistancePenetration(0)
            .resistancePenetrationPercent(0.0)
            .armorPenetrationPercent(0.0)
            .knockback(0.5)
            .buildRaw();

    public ElementCoefficients() {
        this(new HashMap<>());
    }

    // ===== 通用读写接口 =====

    /**
     * 读取参数（缺失返回 0）。
     */
    public double get(String key) {
        return values.getOrDefault(key, 0.0);
    }

    /**
     * 设置参数，返回新实例（不可变）。
     */
    public ElementCoefficients with(String key, double value) {
        Map<String, Double> updated = new HashMap<>(values);
        updated.put(key, value);
        return new ElementCoefficients(updated);
    }

    /**
     * 一次性批量设置。
     */
    public ElementCoefficients withAll(Map<String, Double> overrides) {
        Map<String, Double> updated = new HashMap<>(values);
        updated.putAll(overrides);
        return new ElementCoefficients(updated);
    }

    /**
     * 转为可变 Map（Builder 用）。
     */
    public Map<String, Double> toMap() {
        return new HashMap<>(values);
    }

    /**
     * 构造器：链式添加参数，支持任意键（含自定义扩展）。
     */
    public static final class Builder {
        private final Map<String, Double> values = new HashMap<>();

        public Builder() {
        }

        /**
         * 通用：添加/覆盖任意参数（键为 {@link ElementCoefficients} 常量或自定义）。
         */
        public Builder addAttribute(String key, double value) {
            values.put(key, value);
            return this;
        }

        public Builder generateCoefficient(double value) {
            return addAttribute(GENERATE_COEFFICIENT, value);
        }

        public Builder sameCoefficient(double value) {
            return addAttribute(SAME_COEFFICIENT, value);
        }

        public Builder neutralCoefficient(double value) {
            return addAttribute(NEUTRAL_COEFFICIENT, value);
        }

        public Builder counterCoefficient(double value) {
            return addAttribute(COUNTER_COEFFICIENT, value);
        }

        public Builder generateBoost(double value) {
            return addAttribute(GENERATE_BOOST, value);
        }

        public Builder counterReduce(double value) {
            return addAttribute(COUNTER_REDUCE, value);
        }

        public Builder yinYangCostRatio(double value) {
            return addAttribute(YIN_YANG_COST_RATIO, value);
        }

        public Builder yinMatchCoefficient(double value) {
            return addAttribute(YIN_MATCH_COEFFICIENT, value);
        }

        public Builder yinMatchGain(double value) {
            return addAttribute(YIN_MATCH_GAIN, value);
        }

        public Builder yinMismatchCoefficient(double value) {
            return addAttribute(YIN_MISMATCH_COEFFICIENT, value);
        }

        public Builder yinMismatchGain(double value) {
            return addAttribute(YIN_MISMATCH_GAIN, value);
        }

        public Builder restoreRate(double value) {
            return addAttribute(RESTORE_RATE, value);
        }

        public Builder restoreAmount(double value) {
            return addAttribute(RESTORE_AMOUNT, value);
        }

        public Builder environmentGainBase(double value) {
            return addAttribute(ENVIRONMENT_GAIN_BASE, value);
        }

        public Builder environmentRatioWeight(double value) {
            return addAttribute(ENVIRONMENT_RATIO_WEIGHT, value);
        }

        public Builder environmentMainRatio(double value) {
            return addAttribute(ENVIRONMENT_MAIN_RATIO, value);
        }

        public Builder environmentSubRatio(double value) {
            return addAttribute(ENVIRONMENT_SUB_RATIO, value);
        }

        public Builder yinYangRestoreRate(double value) {
            return addAttribute(YIN_YANG_RESTORE_RATE, value);
        }

        public Builder veinStageGain(double value) {
            return addAttribute(VEIN_STAGE_GAIN, value);
        }

        public Builder damageMultiplier(double value) {
            return addAttribute(DAMAGE_MULTIPLIER, value);
        }

        public Builder damageReduction(double value) {
            return addAttribute(DAMAGE_REDUCTION, value);
        }

        public Builder damageAmplification(double value) {
            return addAttribute(DAMAGE_AMPLIFICATION, value);
        }

        public Builder criticalChance(double value) {
            return addAttribute(CRITICAL_CHANCE, value);
        }

        public Builder criticalDamageMultiplier(double value) {
            return addAttribute(CRITICAL_DAMAGE_MULTIPLIER, value);
        }

        public Builder damageResistance(long value) {
            return addAttribute(DAMAGE_RESISTANCE, value);
        }

        public Builder resistancePenetration(long value) {
            return addAttribute(RESISTANCE_PENETRATION, value);
        }

        public Builder resistancePenetrationPercent(double value) {
            return addAttribute(RESISTANCE_PENETRATION_PERCENT, value);
        }

        public Builder armorPenetrationPercent(double value) {
            return addAttribute(ARMOR_PENETRATION_PERCENT, value);
        }

        public Builder knockback(double value) {
            return addAttribute(KNOCKBACK, value);
        }

        /**
         * 以默认参数为基础构建（未显式设置的值取 DEFAULT）。
         */
        public ElementCoefficients build() {
            Map<String, Double> merged = new HashMap<>(DEFAULT.values);
            merged.putAll(values);
            return new ElementCoefficients(merged);
        }

        /**
         * 仅构建显式设置的值（不含 DEFAULT，用于完全自定义）。
         */
        public ElementCoefficients buildRaw() {
            return new ElementCoefficients(new HashMap<>(values));
        }
    }

    // ===== 便捷 getter（消耗/增益） =====

    public double generateCoefficient() {
        return get(GENERATE_COEFFICIENT);
    }

    public double sameCoefficient() {
        return get(SAME_COEFFICIENT);
    }

    public double neutralCoefficient() {
        return get(NEUTRAL_COEFFICIENT);
    }

    public double counterCoefficient() {
        return get(COUNTER_COEFFICIENT);
    }

    public double generateBoost() {
        return get(GENERATE_BOOST);
    }

    public double counterReduce() {
        return get(COUNTER_REDUCE);
    }

    public double yinYangCostRatio() {
        return get(YIN_YANG_COST_RATIO);
    }

    public double yinMatchCoefficient() {
        return get(YIN_MATCH_COEFFICIENT);
    }

    public double yinMatchGain() {
        return get(YIN_MATCH_GAIN);
    }

    public double yinMismatchCoefficient() {
        return get(YIN_MISMATCH_COEFFICIENT);
    }

    public double yinMismatchGain() {
        return get(YIN_MISMATCH_GAIN);
    }

    // ===== 便捷 getter（恢复） =====

    public double restoreRate() {
        return get(RESTORE_RATE);
    }

    public double restoreAmount() {
        return get(RESTORE_AMOUNT);
    }

    public double environmentGainBase() {
        return get(ENVIRONMENT_GAIN_BASE);
    }

    public double environmentRatioWeight() {
        return get(ENVIRONMENT_RATIO_WEIGHT);
    }

    public double environmentMainRatio() {
        return get(ENVIRONMENT_MAIN_RATIO);
    }

    public double environmentSubRatio() {
        return get(ENVIRONMENT_SUB_RATIO);
    }

    public double yinYangRestoreRate() {
        return get(YIN_YANG_RESTORE_RATE);
    }

    public double veinStageGain() {
        return get(VEIN_STAGE_GAIN);
    }

    // ===== 便捷 getter（伤害） =====

    public double damageMultiplier() {
        return get(DAMAGE_MULTIPLIER);
    }

    public double damageReduction() {
        return get(DAMAGE_REDUCTION);
    }

    public double damageAmplification() {
        return get(DAMAGE_AMPLIFICATION);
    }

    public double criticalChance() {
        return get(CRITICAL_CHANCE);
    }

    public double criticalDamageMultiplier() {
        return get(CRITICAL_DAMAGE_MULTIPLIER);
    }

    public long damageResistance() {
        return (long) get(DAMAGE_RESISTANCE);
    }

    public long resistancePenetration() {
        return (long) get(RESISTANCE_PENETRATION);
    }

    public double resistancePenetrationPercent() {
        return get(RESISTANCE_PENETRATION_PERCENT);
    }

    public double armorPenetrationPercent() {
        return get(ARMOR_PENETRATION_PERCENT);
    }

    /**
     * 击退值：该属性伤害造成的击退强度（默认 1.0，一般伤害击退效果）。
     */
    public double knockback() {
        return get(KNOCKBACK);
    }

    // ===== 序列化 =====

    public static final MapCodec<ElementCoefficients> CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("values")
                    .xmap(ElementCoefficients::new, ElementCoefficients::values);
}
