package org.wenyan.wenyan_addon.qi.element;

import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;

import java.util.List;
import java.util.Map;

/**
 * 衍生属性自动系数计算：
 * 单基底 → 直接继承基底系数；
 * 多基底 → 基底对关系倍率调和平均 + 基底权重加权平均。
 * 关系倍率：相生 1.2（增强）、相克 0.8（削弱）、相同/无关 1.0（标准）。
 * 增益类字段（值越高越好）乘以倍率；消耗类字段（值越低越好）除以倍率。
 */
public final class DerivedCoefficientsCalculator {
    public static final double GENERATING_RATE = 1.2;
    public static final double COUNTER_RATE = 0.8;
    public static final double NEUTRAL_RATE = 1.0;

    private DerivedCoefficientsCalculator() {
    }

    /**
     * 基底对关系倍率：五行根走五行环；断开根查其显式 relations。
     */
    public static double relationRate(ElementAttribute a, ElementAttribute b) {
        if (a == b) {
            return NEUTRAL_RATE;
        }
        RelationType relation = ElementRelations.relation(a, b);
        return switch (relation) {
            case GENERATING, GENERATED -> GENERATING_RATE;
            case COUNTER, COUNTERED -> COUNTER_RATE;
            case SAME, NONE -> NEUTRAL_RATE;
        };
    }

    /**
     * 计算衍生属性系数。
     *
     * @param explicit 显式配置的系数（null = 未配置）
     */
    public static ElementCoefficients calculate(ElementAttribute element, ElementCoefficients explicit) {
        List<ElementAttribute> roots = element.flattenedBases();
        if (roots.isEmpty()) {
            return explicit != null ? explicit : ElementCoefficients.DEFAULT;
        }
        // 单基底：直接继承
        if (roots.size() == 1) {
            ElementCoefficients base = roots.get(0).defaultCoefficients();
            return explicit != null ? average(base, explicit) : base;
        }
        // 多基底：调和 + 权重 + 加权平均
        double harmonic = harmonicRate(roots);
        double[] weights = new double[roots.size()];
        for (int i = 0; i < roots.size(); i++) {
            double sum = 0;
            for (int j = 0; j < roots.size(); j++) {
                if (i != j) {
                    sum += relationRate(roots.get(i), roots.get(j));
                }
            }
            weights[i] = sum / roots.size();
        }
        ElementCoefficients merged = weightedAverage(roots, weights, harmonic);
        return explicit != null ? average(merged, explicit) : merged;
    }

    private static double harmonicRate(List<ElementAttribute> roots) {
        int pairs = 0;
        double sum = 0;
        for (int i = 0; i < roots.size(); i++) {
            for (int j = i + 1; j < roots.size(); j++) {
                sum += relationRate(roots.get(i), roots.get(j));
                pairs++;
            }
        }
        return pairs == 0 ? NEUTRAL_RATE : sum / pairs;
    }

    /**
     * 加权平均：最终 = Σ(基底系数 × 基底权重 × 调和系数) / 基底数量。
     * 从零基开始累加，避免初始默认值污染。
     */
    private static ElementCoefficients weightedAverage(List<ElementAttribute> roots, double[] weights, double harmonic) {
        ElementCoefficients result = new ElementCoefficients(new java.util.HashMap<>());
        for (int i = 0; i < roots.size(); i++) {
            ElementCoefficients base = roots.get(i).defaultCoefficients();
            double weight = weights[i] * harmonic / roots.size();
            result = blend(result, base, weight);
        }
        return result;
    }

    private static ElementCoefficients average(ElementCoefficients a, ElementCoefficients b) {
        Map<String, Double> merged = new java.util.HashMap<>(a.values());
        b.values().forEach((key, value) ->
                merged.merge(key, value, (x, y) -> (x + y) / 2));
        return new ElementCoefficients(merged);
    }

    /**
     * 累加混合：result = result + base × weight（零基累加）。
     */
    private static ElementCoefficients blend(ElementCoefficients result, ElementCoefficients base, double weight) {
        Map<String, Double> merged = new java.util.HashMap<>(result.values());
        base.values().forEach((key, value) ->
                merged.merge(key, value * weight, Double::sum));
        return new ElementCoefficients(merged);
    }
}
