package org.wenyan.wenyan_addon.qi.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 单个属性的灵气系数集（消耗/增益/恢复），每个属性可分别调整，供后续功能使用。
 */
public record ElementCoefficients(
        double generateCoefficient,
        double sameCoefficient,
        double neutralCoefficient,
        double counterCoefficient,
        double generateBoost,
        double counterReduce,
        double yinYangCostRatio,
        double yinMatchCoefficient,
        double yinMatchGain,
        double yinMismatchCoefficient,
        double yinMismatchGain,
        double restoreRate,
        double environmentMainRatio,
        double environmentSubRatio,
        double yinYangRestoreRate
) {
    public static final ElementCoefficients DEFAULT = new ElementCoefficients(
            0.6, 0.8, 1.0, 1.2,
            1.2, 0.8,
            0.3,
            0.8, 1.2, 1.3, 0.8,
            1.0,
            0.8, 0.2,
            1.0
    );

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
            Codec.DOUBLE.optionalFieldOf("restoreRate", 1.0).forGetter(ElementCoefficients::restoreRate),
            Codec.DOUBLE.optionalFieldOf("environmentMainRatio", 0.8).forGetter(ElementCoefficients::environmentMainRatio),
            Codec.DOUBLE.optionalFieldOf("environmentSubRatio", 0.2).forGetter(ElementCoefficients::environmentSubRatio),
            Codec.DOUBLE.optionalFieldOf("yinYangRestoreRate", 1.0).forGetter(ElementCoefficients::yinYangRestoreRate)
    ).apply(instance, ElementCoefficients::new));
}
