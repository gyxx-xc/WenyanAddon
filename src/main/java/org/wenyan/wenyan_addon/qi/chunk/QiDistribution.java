package org.wenyan.wenyan_addon.qi.chunk;

import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;

import java.util.HashMap;
import java.util.Map;

/**
 * 区块灵气结构占比（区域趋同性）：主属性 70%，相生（我生）/生我（生我）各 12.5%，
 * 其余（相克）各 2.5%。同一群系的所有区块共用同一结构（主属性由群系决定）。
 * 结构占比写入 {@link ChunkQiData#proportions()} 持久化，决定各属性可恢复的目标比例。
 */
public record QiDistribution(ElementType dominant, ElementType generator,
                             ElementType generated, double dominantRatio,
                             double generatorRatio, double generatedRatio,
                             double otherRatio) {
    public static final double DOMINANT_RATIO = 0.7;
    public static final double GENERATOR_RATIO = 0.125;
    public static final double GENERATED_RATIO = 0.125;
    public static final double OTHER_RATIO = 0.025;

    public static QiDistribution of(ElementType dominant) {
        return new QiDistribution(dominant,
                ElementRelations.generatedBy(dominant),
                ElementRelations.generates(dominant),
                DOMINANT_RATIO, GENERATOR_RATIO, GENERATED_RATIO, OTHER_RATIO);
    }

    /**
     * 该元素在结构中的占比。
     */
    public double ratio(ElementType element) {
        if (element == dominant) {
            return dominantRatio;
        }
        if (element == generator) {
            return generatorRatio;
        }
        if (element == generated) {
            return generatedRatio;
        }
        return otherRatio;
    }

    /**
     * 结构占比表（属性 id → 占比），写入区块持久化。
     */
    public Map<String, Double> ratios() {
        Map<String, Double> ratios = new HashMap<>();
        ratios.put(dominant.id(), dominantRatio);
        if (generator != null) {
            ratios.put(generator.id(), generatorRatio);
        }
        if (generated != null) {
            ratios.put(generated.id(), generatedRatio);
        }
        for (ElementType element : ElementRelations.ELEMENTS) {
            if (element != dominant && element != generator && element != generated) {
                ratios.put(element.id(), otherRatio);
            }
        }
        return ratios;
    }
}
