package org.wenyan.wenyan_addon.qi.consume;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.Map;

public record ConsumptionResult(
        boolean success,
        boolean explosion,
        double coefficient,
        double boost,
        Map<ElementAttribute, Double> deducted,
        ElementAttribute explosionElement
) {
    public static ConsumptionResult success(Map<ElementAttribute, Double> deducted, double coefficient, double boost) {
        return new ConsumptionResult(true, false, coefficient, boost, deducted, null);
    }

    public static ConsumptionResult explosion(ElementAttribute explosionElement) {
        return new ConsumptionResult(false, true, 0, 0, Map.of(), explosionElement);
    }

    public static ConsumptionResult insufficient() {
        return new ConsumptionResult(false, false, 0, 0, Map.of(), null);
    }
}
