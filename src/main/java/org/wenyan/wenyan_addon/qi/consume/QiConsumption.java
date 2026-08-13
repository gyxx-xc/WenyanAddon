package org.wenyan.wenyan_addon.qi.consume;

import net.minecraft.world.entity.player.Player;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;
import org.wenyan.wenyan_addon.qi.storage.QiContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public final class QiConsumption {
    private QiConsumption() {
    }

    public static ConsumptionResult tryConsume(Player player, QiConsumable consumable) {
        return tryConsume(player, consumable, List.of());
    }

    /**
     * 灵气消耗：五行 × 阴阳 优先级表；支付顺序为容器优先（依次扣除），玩家兜底。
     */
    public static ConsumptionResult tryConsume(Player player, QiConsumable consumable, List<QiContainer> containers) {
        ElementAttribute spell = consumable.spellElement();
        double n = consumable.baseQiCost();
        YinYangTendency tendency = consumable.tendency();
        PlayerQiData qi = PlayerQi.of(player);
        ElementCoefficients c = qi.coefficients(spell);

        for (LevelAttempt level : levels(c)) {
            ConsumptionResult result = tryLevel(player, qi, spell, n, tendency, level, containers);
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

    public static boolean checkSufficient(Player player, QiConsumable consumable) {
        return checkSufficient(player, consumable, List.of());
    }

    /**
     * 只检查不扣除：灵气（容器 + 玩家）是否足够支持该消耗。
     */
    public static boolean checkSufficient(Player player, QiConsumable consumable, List<QiContainer> containers) {
        ElementAttribute spell = consumable.spellElement();
        double n = consumable.baseQiCost();
        YinYangTendency tendency = consumable.tendency();
        PlayerQiData qi = PlayerQi.of(player);
        ElementCoefficients c = qi.coefficients(spell);

        for (LevelAttempt level : levels(c)) {
            Map<ElementAttribute, Double> need = level.need().apply(spell, n);
            boolean sufficient = hasSufficient(qi, containers, need);
            if (sufficient && !yinYangOptions(qi, containers, n, tendency).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static List<LevelAttempt> levels(ElementCoefficients c) {
        return List.of(
                new LevelAttempt(c.generateCoefficient(), c.generateBoost(),
                        (s, cost) -> generateNeed(c, s, cost)),
                new LevelAttempt(c.sameCoefficient(), 1.0,
                        (s, cost) -> Map.of(s, cost * c.sameCoefficient())),
                new LevelAttempt(c.neutralCoefficient(), 1.0,
                        (s, cost) -> Map.of(ElementType.NEUTRAL, cost * c.neutralCoefficient())),
                new LevelAttempt(c.counterCoefficient(), c.counterReduce(),
                        (s, cost) -> counterNeed(c, s, cost)));
    }

    private static Map<ElementAttribute, Double> generateNeed(ElementCoefficients c, ElementAttribute spell, double n) {
        Map<ElementAttribute, Double> need = new HashMap<>();
        ElementType generator = ElementRelations.generatedBy(spell);
        if (generator != null) {
            need.put(generator, n * c.generateCoefficient() / 3.0);
        }
        need.put(spell, n * c.generateCoefficient() * 2.0 / 3.0);
        return need;
    }

    private static Map<ElementAttribute, Double> counterNeed(ElementCoefficients c, ElementAttribute spell, double n) {
        ElementType countered = ElementRelations.counters(spell);
        if (countered == null) {
            return Map.of();
        }
        return Map.of(countered, n * c.counterCoefficient());
    }

    private static ConsumptionResult tryLevel(Player player, PlayerQiData qi, ElementAttribute spell, double n,
                                              YinYangTendency tendency, LevelAttempt level, List<QiContainer> containers) {
        for (YinYangOption option : yinYangOptions(qi, containers, n, tendency)) {
            Map<ElementAttribute, Double> need = level.need().apply(spell, n);
            boolean sufficient = hasSufficient(qi, containers, need);
            if (option.polarity() != null
                    && !hasSufficient(qi, containers, Map.of(option.polarity(), option.cost()))) {
                sufficient = false;
            }
            if (!sufficient) {
                continue;
            }
            Map<ElementAttribute, Double> deductedFromPlayer = pay(player, qi, containers, need);
            if (option.polarity() != null) {
                pay(player, qi, containers, Map.of(option.polarity(), option.cost()));
                deductedFromPlayer.put(option.polarity(), option.cost());
            }
            PlayerQi.markDirty(player);
            double totalCoefficient = level.coefficient() * option.coefficient();
            double boost = level.boost() * option.gain();
            return ConsumptionResult.success(deductedFromPlayer, totalCoefficient, boost);
        }
        return ConsumptionResult.insufficient();
    }

    private static boolean hasSufficient(PlayerQiData qi, List<QiContainer> containers,
                                         Map<ElementAttribute, Double> need) {
        for (Map.Entry<ElementAttribute, Double> entry : need.entrySet()) {
            double inContainers = 0;
            for (QiContainer container : containers) {
                inContainers += container.get(entry.getKey());
            }
            if (qi.get(entry.getKey()) + inContainers < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 支付：容器优先依次扣除，剩余从玩家扣除。返回玩家扣除部分。
     */
    private static Map<ElementAttribute, Double> pay(Player player, PlayerQiData qi, List<QiContainer> containers,
                                                     Map<ElementAttribute, Double> need) {
        Map<ElementAttribute, Double> deductedFromPlayer = new HashMap<>();
        for (Map.Entry<ElementAttribute, Double> entry : need.entrySet()) {
            double remaining = entry.getValue();
            for (QiContainer container : containers) {
                remaining -= container.consume(entry.getKey(), remaining);
                if (remaining <= 0) {
                    break;
                }
            }
            if (remaining > 0) {
                qi.consume(entry.getKey(), remaining);
                deductedFromPlayer.put(entry.getKey(), remaining);
            }
        }
        return deductedFromPlayer;
    }

    private static List<YinYangOption> yinYangOptions(PlayerQiData qi, List<QiContainer> containers,
                                                      double n, YinYangTendency tendency) {
        if (tendency == YinYangTendency.NONE) {
            return List.of(new YinYangOption(1.0, 1.0, null, 0));
        }
        ElementType matched = tendency == YinYangTendency.YIN ? ElementType.YIN : ElementType.YANG;
        ElementType opposite = tendency == YinYangTendency.YIN ? ElementType.YANG : ElementType.YIN;
        ElementCoefficients matchedCoefficients = qi.coefficients(matched);
        double matchedCost = n * matchedCoefficients.yinYangCostRatio() * matchedCoefficients.yinMatchCoefficient();
        if (hasSufficient(qi, containers, Map.of(matched, matchedCost))) {
            return List.of(new YinYangOption(matchedCoefficients.yinMatchCoefficient(),
                    matchedCoefficients.yinMatchGain(), matched, matchedCost));
        }
        ElementCoefficients oppositeCoefficients = qi.coefficients(opposite);
        double mismatchedCost = n * oppositeCoefficients.yinYangCostRatio() * oppositeCoefficients.yinMismatchCoefficient();
        if (hasSufficient(qi, containers, Map.of(opposite, mismatchedCost))) {
            return List.of(new YinYangOption(oppositeCoefficients.yinMismatchCoefficient(),
                    oppositeCoefficients.yinMismatchGain(), opposite, mismatchedCost));
        }
        return List.of();
    }

    private record YinYangOption(double coefficient, double gain, ElementAttribute polarity, double cost) {
    }

    private record LevelAttempt(double coefficient, double boost,
                                BiFunction<ElementAttribute, Double, Map<ElementAttribute, Double>> need) {
    }
}
