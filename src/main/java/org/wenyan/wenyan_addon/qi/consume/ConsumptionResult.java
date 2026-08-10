package org.wenyan.wenyan_addon.qi.consume;

import org.wenyan.wenyan_addon.qi.element.ElementType;

import java.util.Map;

public record ConsumptionResult(
        boolean success,
        Map<ElementType, Double> deducted
) {
    public static ConsumptionResult success(Map<ElementType, Double> deducted) {
        return new ConsumptionResult(true, deducted);
    }

    public static ConsumptionResult insufficient() {
        return new ConsumptionResult(false, Map.of());
    }
}
