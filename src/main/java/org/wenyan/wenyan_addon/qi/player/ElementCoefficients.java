package org.wenyan.wenyan_addon.qi.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 单个属性的灵气系数集（消耗/增益/恢复），每个属性可分别调整。
 * 核心消耗/阴阳字段为固定 record 参数；恢复等扩展字段存于 extras Map（无限扩展，
 * 新增参数只需在 DEFAULT 添加默认值 + 添加 getter，无需改 Codec 与构造器）。
 * 恢复量 = 恢复数额（restoreAmount）× 恢复系数（restoreRate），两个独立乘区。
 */
public record ElementCoefficients(
        /**
         * 相生消耗系数：消耗"该属性 + 其相生属性"时的五行消耗倍率（默认 0.6，低于 1 即便宜）。
         */
        double generateCoefficient,
        /**
         * 同属性消耗系数：消耗"纯该属性"时的五行消耗倍率（默认 0.8，效率提升）。
         */
        double sameCoefficient,
        /**
         * 无属性消耗系数：用无属性灵气支付时的五行消耗倍率（默认 1.0，标准）。
         */
        double neutralCoefficient,
        /**
         * 相克（对冲）消耗系数：用相克属性支付时的五行消耗倍率（默认 1.2，更贵）。
         */
        double counterCoefficient,
        /**
         * 相生增益：用相生组合消耗时对法术效果的倍率（默认 1.2，增强）。
         */
        double generateBoost,
        /**
         * 相克减益：用相克组合消耗时对法术效果的倍率（默认 0.8，减弱）。
         */
        double counterReduce,
        /**
         * 阴阳消耗比：施法时阴阳消耗量 = 基础消耗 N × 该比例（默认 0.3）。
         */
        double yinYangCostRatio,
        /**
         * 阴阳匹配系数：施法倾向与持有阴阳一致时，阴阳消耗的倍率（默认 0.8，便宜）。
         */
        double yinMatchCoefficient,
        /**
         * 阴阳匹配增益：阴阳匹配时对法术效果的倍率（默认 1.2）。
         */
        double yinMatchGain,
        /**
         * 阴阳不匹配系数：施法倾向与持有阴阳相反时，阴阳消耗的倍率（默认 1.3，更贵）。
         */
        double yinMismatchCoefficient,
        /**
         * 阴阳不匹配减益：阴阳不匹配时对法术效果的倍率（默认 0.8）。
         */
        double yinMismatchGain,
        /**
         * 扩展参数（恢复/环境增益等），key → 数值。
         */
        Map<String, Double> extras
) {

    public static final ElementCoefficients DEFAULT = new ElementCoefficients(
            0.6, 0.8, 1.0, 1.2,
            1.2, 0.8,
            0.3,
            0.8, 1.2, 1.3, 0.8,
            Map.of(
                    "restoreRate", 1.0,
                    "restoreAmount", 1.0,
                    "environmentGainBase", 1.0,
                    "environmentRatioWeight", 1.0,
                    "environmentMainRatio", 0.8,
                    "environmentSubRatio", 0.2,
                    "yinYangRestoreRate", 1.0,
                    "veinStageGain", 0.1
            )
    );

    // ===== 扩展字段 getter =====

    /**
     * 恢复系数：恢复量的倍率乘区（默认 1.0）。
     */
    public double restoreRate() {
        return extras.getOrDefault("restoreRate", 1.0);
    }

    /**
     * 恢复数额：每秒恢复的基础量（默认 1.0 点/秒）。
     */
    public double restoreAmount() {
        return extras.getOrDefault("restoreAmount", 1.0);
    }

    /**
     * 环境增益基础系数：环境恢复增益的基础项（默认 1.0）。
     * 增益公式 = environmentGainBase + environmentRatioWeight × n × m。
     */
    public double environmentGainBase() {
        return extras.getOrDefault("environmentGainBase", 1.0);
    }

    /**
     * 环境增益占比权重：区块结构占比 n × 区块余量 m 的权重（默认 1.0）。
     */
    public double environmentRatioWeight() {
        return extras.getOrDefault("environmentRatioWeight", 1.0);
    }

    /**
     * 环境恢复主占比：环境恢复量中分给主导元素的比例（默认 0.8）。
     */
    public double environmentMainRatio() {
        return extras.getOrDefault("environmentMainRatio", 0.8);
    }

    /**
     * 环境恢复副占比：环境恢复量中分给相生元素的比例（默认 0.2）。
     */
    public double environmentSubRatio() {
        return extras.getOrDefault("environmentSubRatio", 0.2);
    }

    /**
     * 阴阳恢复速率：该属性阴阳自我恢复的速度倍率（默认 1.0）。
     */
    public double yinYangRestoreRate() {
        return extras.getOrDefault("yinYangRestoreRate", 1.0);
    }

    /**
     * 灵脉恢复系数增幅：每级灵脉阶段对基础恢复数额的加成（默认 0.1，即 10%）。
     */
    public double veinStageGain() {
        return extras.getOrDefault("veinStageGain", 0.1);
    }

    /**
     * 仅修改恢复数额（无属性 5 点/秒，其余默认 2 点/秒）。
     */
    public ElementCoefficients withRestoreAmount(double amount) {
        Map<String, Double> updated = new HashMap<>(extras);
        updated.put("restoreAmount", amount);
        return new ElementCoefficients(
                generateCoefficient, sameCoefficient, neutralCoefficient, counterCoefficient,
                generateBoost, counterReduce,
                yinYangCostRatio,
                yinMatchCoefficient, yinMatchGain, yinMismatchCoefficient, yinMismatchGain,
                updated);
    }

    /**
     * 通用扩展参数设置：返回新实例（extras 中 key → 数值）。
     * 例：coefficients.withExtra("environmentGainBase", 2.0)
     */
    public ElementCoefficients withExtra(String key, double value) {
        Map<String, Double> updated = new HashMap<>(extras);
        updated.put(key, value);
        return new ElementCoefficients(
                generateCoefficient, sameCoefficient, neutralCoefficient, counterCoefficient,
                generateBoost, counterReduce,
                yinYangCostRatio,
                yinMatchCoefficient, yinMatchGain, yinMismatchCoefficient, yinMismatchGain,
                updated);
    }

    public static final MapCodec<ElementCoefficients> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("generateCoefficient", 0.6).forGetter(ElementCoefficients::generateCoefficient),
            Codec.DOUBLE.optionalFieldOf("sameCoefficient", 0.8).forGetter(ElementCoefficients::sameCoefficient),
            Codec.DOUBLE.optionalFieldOf("neutralCoefficient", 1.0).forGetter(ElementCoefficients::neutralCoefficient),
            Codec.DOUBLE.optionalFieldOf("counterCoefficient", 1.2).forGetter(ElementCoefficients::counterCoefficient),
            Codec.DOUBLE.optionalFieldOf("generateBoost", 1.2).forGetter(ElementCoefficients::generateBoost),
            Codec.DOUBLE.optionalFieldOf("counterReduce", 0.8).forGetter(ElementCoefficients::counterReduce),
            Codec.DOUBLE.optionalFieldOf("yinYangCostRatio", 0.3).forGetter(ElementCoefficients::yinYangCostRatio),
            Codec.DOUBLE.optionalFieldOf("yinMatchCoefficient", 0.8).forGetter(ElementCoefficients::yinMatchCoefficient),
            Codec.DOUBLE.optionalFieldOf("yinMatchGain", 1.2).forGetter(ElementCoefficients::yinMatchGain),
            Codec.DOUBLE.optionalFieldOf("yinMismatchCoefficient", 1.3).forGetter(ElementCoefficients::yinMismatchCoefficient),
            Codec.DOUBLE.optionalFieldOf("yinMismatchGain", 0.8).forGetter(ElementCoefficients::yinMismatchGain),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("extras", Map.of())
                    .forGetter(ElementCoefficients::extras)
    ).apply(instance, ElementCoefficients::new));
}
