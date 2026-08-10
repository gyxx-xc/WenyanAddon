package org.wenyan.wenyan_addon.qi.element;

import java.util.List;
import java.util.Map;

public final class ElementRelations {
    public static final List<ElementType> ELEMENTS = List.of(
            ElementType.METAL, ElementType.WOOD, ElementType.WATER,
            ElementType.FIRE, ElementType.EARTH);

    // 相生循环：金生水，水生木，木生火，火生土，土生金
    private static final Map<ElementType, ElementType> GENERATES = Map.of(
            ElementType.METAL, ElementType.WATER,
            ElementType.WATER, ElementType.WOOD,
            ElementType.WOOD, ElementType.FIRE,
            ElementType.FIRE, ElementType.EARTH,
            ElementType.EARTH, ElementType.METAL
    );

    // 相克循环：金克木，木克土，土克水，水克火，火克金
    private static final Map<ElementType, ElementType> COUNTERS = Map.of(
            ElementType.METAL, ElementType.WOOD,
            ElementType.WOOD, ElementType.EARTH,
            ElementType.EARTH, ElementType.WATER,
            ElementType.WATER, ElementType.FIRE,
            ElementType.FIRE, ElementType.METAL
    );

    private ElementRelations() {
    }

    public static ElementType generates(ElementType element) {
        return GENERATES.get(element);
    }

    public static ElementType generatedBy(ElementType element) {
        return GENERATES.entrySet().stream()
                .filter(entry -> entry.getValue() == element)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public static ElementType counters(ElementType element) {
        return COUNTERS.get(element);
    }

    public static ElementType counteredBy(ElementType element) {
        return COUNTERS.entrySet().stream()
                .filter(entry -> entry.getValue() == element)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public static RelationType relation(ElementType a, ElementType b) {
        if (a == b) {
            return RelationType.SAME;
        }
        if (generates(a) == b) {
            return RelationType.GENERATING;
        }
        if (generatedBy(a) == b) {
            return RelationType.GENERATED;
        }
        if (counters(a) == b) {
            return RelationType.COUNTER;
        }
        if (counteredBy(a) == b) {
            return RelationType.COUNTERED;
        }
        return RelationType.NONE;
    }
}
