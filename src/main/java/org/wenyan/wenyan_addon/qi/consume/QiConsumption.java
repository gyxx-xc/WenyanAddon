package org.wenyan.wenyan_addon.qi.consume;

import net.minecraft.world.entity.player.Player;
import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public final class QiConsumption {
    private QiConsumption() {
    }

    public static ConsumptionResult tryConsume(Player player, QiConsumable consumable) {
        ElementType spell = consumable.spellElement();
        double n = consumable.baseQiCost();
        YinYangTendency tendency = consumable.tendency();
        PlayerQiData qi = PlayerQi.of(player);
        ElementCoefficients c = qi.coefficients(spell);

        List<LevelAttempt> levels = List.of(
                new LevelAttempt(c.generateCoefficient(), c.generateBoost(),
                        (s, cost) -> generateNeed(c, s, cost)),
                new LevelAttempt(c.sameCoefficient(), 1.0,
                        (s, cost) -> Map.of(s, cost * c.sameCoefficient())),
                new LevelAttempt(c.neutralCoefficient(), 1.0,
                        (s, cost) -> Map.of(ElementType.NEUTRAL, cost * c.neutralCoefficient())),
                new LevelAttempt(c.counterCoefficient(), c.counterReduce(),
                        (s, cost) -> counterNeed(c, s, cost)));

        for (LevelAttempt level : levels) {
            ConsumptionResult result = tryLevel(player, qi, spell, n, tendency, level);
            if (result.success() || result.explosion()) {
                return result;
            }
        }

        // Step 5 - 失败检测
        ElementType counter = ElementRelations.counteredBy(spell);
        if (counter != null && qi.get(counter) > 0) {
            return ConsumptionResult.explosion(counter);
        }
        if (tendency == YinYangTendency.YANG && qi.get(ElementType.YANG) <= 0 && qi.get(ElementType.YIN) > 0) {
            return ConsumptionResult.explosion(ElementType.YIN);
        }
        if (tendency == YinYangTendency.YIN && qi.get(ElementType.YIN) <= 0 && qi.get(ElementType.YANG) > 0) {
            return ConsumptionResult.explosion(ElementType.YANG);
        }
        return ConsumptionResult.insufficient();
    }

    private static Map<ElementType, Double> generateNeed(ElementCoefficients c, ElementType spell, double n) {
        Map<ElementType, Double> need = new HashMap<>();
        ElementType generator = ElementRelations.generatedBy(spell);
        if (generator != null) {
            need.put(generator, n * c.generateCoefficient() / 3.0);
        }
        need.put(spell, n * c.generateCoefficient() * 2.0 / 3.0);
        return need;
    }

    private static Map<ElementType, Double> counterNeed(ElementCoefficients c, ElementType spell, double n) {
        ElementType countered = ElementRelations.counters(spell);
        if (countered == null) {
            return Map.of();
        }
        return Map.of(countered, n * c.counterCoefficient());
    }

    private static ConsumptionResult tryLevel(Player player, PlayerQiData qi, ElementType spell, double n,
                                              YinYangTendency tendency, LevelAttempt level) {
        for (YinYangOption option : yinYangOptions(qi, n, tendency)) {
            Map<ElementType, Double> need = level.need().apply(spell, n);
            boolean sufficient = need.entrySet().stream()
                    .allMatch(entry -> qi.has(entry.getKey(), entry.getValue()));
            if (option.polarity() != null && !qi.has(option.polarity(), option.cost())) {
                sufficient = false;
            }
            if (!sufficient) {
                continue;
            }
            Map<ElementType, Double> deducted = new HashMap<>(need);
            for (Map.Entry<ElementType, Double> entry : need.entrySet()) {
                qi.consume(entry.getKey(), entry.getValue());
            }
            if (option.polarity() != null) {
                qi.consume(option.polarity(), option.cost());
                deducted.put(option.polarity(), option.cost());
            }
            PlayerQi.markDirty(player);
            double totalCoefficient = level.coefficient() * option.coefficient();
            double boost = level.boost() * option.gain();
            return ConsumptionResult.success(deducted, totalCoefficient, boost);
        }
        return ConsumptionResult.insufficient();
    }

    private static List<YinYangOption> yinYangOptions(PlayerQiData qi, double n, YinYangTendency tendency) {
        if (tendency == YinYangTendency.NONE) {
            return List.of(new YinYangOption(1.0, 1.0, null, 0));
        }
        ElementType matched = tendency == YinYangTendency.YIN ? ElementType.YIN : ElementType.YANG;
        ElementType opposite = tendency == YinYangTendency.YIN ? ElementType.YANG : ElementType.YIN;
        ElementCoefficients matchedCoefficients = qi.coefficients(matched);
        double matchedCost = n * matchedCoefficients.yinYangCostRatio() * matchedCoefficients.yinMatchCoefficient();
        if (qi.has(matched, matchedCost)) {
            return List.of(new YinYangOption(matchedCoefficients.yinMatchCoefficient(),
                    matchedCoefficients.yinMatchGain(), matched, matchedCost));
        }
        ElementCoefficients oppositeCoefficients = qi.coefficients(opposite);
        double mismatchedCost = n * oppositeCoefficients.yinYangCostRatio() * oppositeCoefficients.yinMismatchCoefficient();
        if (qi.has(opposite, mismatchedCost)) {
            return List.of(new YinYangOption(oppositeCoefficients.yinMismatchCoefficient(),
                    oppositeCoefficients.yinMismatchGain(), opposite, mismatchedCost));
        }
        return List.of();
    }

    private record YinYangOption(double coefficient, double gain, ElementType polarity, double cost) {
    }

    private record LevelAttempt(double coefficient, double boost,
                                BiFunction<ElementType, Double, Map<ElementType, Double>> need) {
    }
}
