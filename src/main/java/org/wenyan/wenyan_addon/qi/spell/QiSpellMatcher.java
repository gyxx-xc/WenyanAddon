package org.wenyan.wenyan_addon.qi.spell;

import org.wenyan.wenyan_addon.qi.consume.YinYangTendency;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.element.RelationType;
import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 标签匹配引擎：将主/辅属性标签与输入灵气对比，得出匹配等级、消耗系数与效果增益。
 * 所有系数引用玩家属性 {@link ElementCoefficients}（每个属性各有一套）；
 * 阴阳倾向单独参与匹配（匹配/不匹配/无倾向）；
 * 衍生属性的相生相克随其五行基底判定。
 */
public final class QiSpellMatcher {
    private QiSpellMatcher() {
    }

    public static final double MISSING_PENALTY_COST = 1.8;
    public static final double MISSING_PENALTY_GAIN = 0.5;
    public static final double PARTIAL_MISSING_STEP = 0.4;
    public static final double MINOR_INTERFERENCE_COST = 1.1;
    public static final double MINOR_INTERFERENCE_GAIN = 0.95;
    public static final double EXACT_BOOST = 1.1;
    public static final double PERFECT_GAIN = 1.4;

    /**
     * 五行 + 阴阳综合匹配，系数引用玩家属性数值。
     *
     * @param coefficients 主属性标签对应元素的玩家系数集
     */
    public static QiMatch match(List<ElementAttribute> primary, List<ElementAttribute> compatible,
                                YinYangTendency tendency, QiComposition input, ElementCoefficients coefficients) {
        QiMatch elementMatch = matchElements(primary, compatible, input, coefficients);
        double[] yinYang = yinYangFactors(tendency, input, coefficients);
        double cost = elementMatch.costMultiplier() * yinYang[0];
        double gain = elementMatch.gainMultiplier() * yinYang[1];
        String reason = yinYang[0] != 1.0 || yinYang[1] != 1.0
                ? elementMatch.reason() + "，阴阳" + (yinYang[0] < 1.0 ? "匹配" : yinYang[0] > 1.0 ? "不匹配" : "中性")
                : elementMatch.reason();
        return new QiMatch(elementMatch.grade(), cost, gain, elementMatch.dominant(), reason, input);
    }

    /**
     * 五行标签匹配。
     */
    public static QiMatch matchElements(List<ElementAttribute> primary, List<ElementAttribute> compatible,
                                        QiComposition input, ElementCoefficients coefficients) {
        ElementAttribute dominant = input.dominant();
        if (primary.isEmpty()) {
            return new QiMatch(MatchGrade.STANDARD, coefficients.neutralCoefficient(), 1.0,
                    dominant, "无属性倾向，标准消耗", input);
        }
        Set<ElementAttribute> tags = new HashSet<>(primary);
        Set<ElementAttribute> present = input.present();

        // 输入缺少主属性：有可支付的无属性灵气 → 标准（走无属性支付）；否则强行施放惩罚
        Set<ElementAttribute> missing = new HashSet<>(tags);
        missing.removeAll(present);
        if (missing.size() == primary.size()) {
            if (input.contains(ElementType.NEUTRAL)) {
                return new QiMatch(MatchGrade.STANDARD, coefficients.neutralCoefficient(), 1.0,
                        dominant, "缺少主属性，以无属性灵气支付", input);
            }
            return new QiMatch(MatchGrade.MISSING, MISSING_PENALTY_COST, MISSING_PENALTY_GAIN,
                    dominant, "缺少主属性，强行施放消耗大增", input);
        }
        if (!missing.isEmpty()) {
            double penalty = 1.0 + missing.size() * PARTIAL_MISSING_STEP;
            return new QiMatch(MatchGrade.INTERFERENCE, penalty, 0.8, dominant, "缺少" + missing + "属性，效果打折", input);
        }

        // 满足所有主属性：检查额外属性
        Set<ElementAttribute> extras = new HashSet<>(present);
        extras.removeAll(tags);

        // 相克干扰：额外属性与主/辅标签相克
        if (hasCounter(extras, tags, compatible)) {
            return new QiMatch(MatchGrade.INTERFERENCE, coefficients.counterCoefficient(), coefficients.counterReduce(),
                    dominant, "包含相克属性，干扰惩罚", input);
        }

        // 辅属性标签：全部满足 = 完美，否则回退到恰好主属性
        if (!compatible.isEmpty()) {
            boolean allCompatible = present.containsAll(compatible);
            return allCompatible
                    ? new QiMatch(MatchGrade.PERFECT, coefficients.generateCoefficient(), PERFECT_GAIN,
                            dominant, "主辅属性全部满足，完美匹配", input)
                    : new QiMatch(MatchGrade.EXACT, coefficients.sameCoefficient(), EXACT_BOOST,
                            dominant, "仅满足主属性，未满足辅属性，回退标准效率", input);
        }

        // 仅有主属性标签
        if (extras.isEmpty()) {
            return new QiMatch(MatchGrade.EXACT, coefficients.sameCoefficient(), EXACT_BOOST,
                    dominant, "纯主属性灵气，效率提升", input);
        }
        if (allGenerative(extras, tags)) {
            return new QiMatch(MatchGrade.BEST, coefficients.generateCoefficient(), coefficients.generateBoost(),
                    dominant, "主属性+相生属性，相生增益", input);
        }
        return new QiMatch(MatchGrade.MINOR, MINOR_INTERFERENCE_COST, MINOR_INTERFERENCE_GAIN,
                dominant, "混合中性属性，轻微干扰", input);
    }

    private static double[] yinYangFactors(YinYangTendency tendency, QiComposition input, ElementCoefficients coefficients) {
        if (tendency == YinYangTendency.NONE) {
            return new double[]{1.0, 1.0};
        }
        ElementType matched = tendency == YinYangTendency.YIN ? ElementType.YIN : ElementType.YANG;
        ElementType opposite = tendency == YinYangTendency.YIN ? ElementType.YANG : ElementType.YIN;
        if (input.contains(matched)) {
            return new double[]{coefficients.yinMatchCoefficient(), coefficients.yinMatchGain()};
        }
        if (input.contains(opposite)) {
            return new double[]{coefficients.yinMismatchCoefficient(), coefficients.yinMismatchGain()};
        }
        return new double[]{1.0, 1.0};
    }

    private static boolean hasCounter(Set<ElementAttribute> extras, Set<ElementAttribute> tags, List<ElementAttribute> compatible) {
        for (ElementAttribute extra : extras) {
            for (ElementAttribute tag : tags) {
                if (isCounter(extra, tag)) {
                    return true;
                }
            }
            for (ElementAttribute compatibleTag : compatible) {
                if (isCounter(extra, compatibleTag)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean allGenerative(Set<ElementAttribute> extras, Set<ElementAttribute> tags) {
        for (ElementAttribute extra : extras) {
            boolean generative = false;
            for (ElementAttribute tag : tags) {
                if (isGenerative(extra, tag)) {
                    generative = true;
                    break;
                }
            }
            if (!generative) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCounter(ElementAttribute a, ElementAttribute b) {
        RelationType relation = ElementRelations.relation(a, b);
        return relation == RelationType.COUNTER || relation == RelationType.COUNTERED;
    }

    private static boolean isGenerative(ElementAttribute a, ElementAttribute b) {
        RelationType relation = ElementRelations.relation(a, b);
        return relation == RelationType.GENERATING || relation == RelationType.GENERATED;
    }
}
