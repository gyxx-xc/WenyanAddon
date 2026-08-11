package org.wenyan.wenyan_addon.qi.consume;

import org.wenyan.wenyan_addon.qi.element.ElementType;

import java.util.Map;

public record ConsumptionResult(
        boolean success,
        boolean explosion,
        double coefficient,
        double boost,
        Map<ElementType, Double> deducted,
        ElementType explosionElement
) {
    public static ConsumptionResult success(Map<ElementType, Double> deducted, double coefficient, double boost) {
        return new ConsumptionResult(true, false, coefficient, boost, deducted, null);
    }

    public static ConsumptionResult explosion(ElementType explosionElement) {
        return new ConsumptionResult(false, true, 0, 0, Map.of(), explosionElement);
    }

    public static ConsumptionResult insufficient() {
        return new ConsumptionResult(false, false, 0, 0, Map.of(), null);
    }
}
