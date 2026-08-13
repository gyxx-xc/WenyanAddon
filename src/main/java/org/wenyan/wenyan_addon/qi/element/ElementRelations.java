package org.wenyan.wenyan_addon.qi.element;

import java.util.List;
import java.util.Map;

/**
 * 五行关系：相生环（金生水，水生木，木生火，火生土，土生金）与
 * 相克环（金克木，木克土，土克水，水克火，火克金）。
 * <p>
 * 关系判定优先级：
 * 1. 显式自定义关系（衍生属性对其它衍生属性指定）；
 * 2. 复数基底归一化：任一基底间存在相克 → 相克；任一基底间存在相生 → 相生（相克优先）；
 * 3. 基底相交 → SAME；
 * 4. 否则 NONE。
 */
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

    /**
     * 某元素所生（相生对象）：取第一基底的相生对象。
     */
    public static ElementType generates(ElementAttribute element) {
        return GENERATES.get(element.bases().get(0));
    }

    /**
     * 生某元素的属性（相生来源）：取第一基底的相生来源。
     */
    public static ElementType generatedBy(ElementAttribute element) {
        return GENERATES.entrySet().stream()
                .filter(entry -> entry.getValue() == element.bases().get(0))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * 某元素所克（相克对象）：取第一基底的相克对象。
     */
    public static ElementType counters(ElementAttribute element) {
        return COUNTERS.get(element.bases().get(0));
    }

    /**
     * 克某元素的属性（相克来源）：取第一基底的相克来源。
     */
    public static ElementType counteredBy(ElementAttribute element) {
        return COUNTERS.entrySet().stream()
                .filter(entry -> entry.getValue() == element.bases().get(0))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public static RelationType relation(ElementAttribute a, ElementAttribute b) {
        // 1. 显式自定义关系（方向感知）
        RelationType custom = a.customRelation(b);
        if (custom != null) {
            return custom;
        }
        custom = b.customRelation(a);
        if (custom != null) {
            return reverse(custom);
        }

        // 2. 复数基底归一化：相克优先于相生
        boolean sameBase = false;
        for (ElementType baseA : a.bases()) {
            for (ElementType baseB : b.bases()) {
                if (baseA == baseB) {
                    sameBase = true;
                }
                RelationType ring = ringRelation(baseA, baseB);
                if (ring == RelationType.COUNTER || ring == RelationType.COUNTERED) {
                    return ring;
                }
            }
        }
        for (ElementType baseA : a.bases()) {
            for (ElementType baseB : b.bases()) {
                RelationType ring = ringRelation(baseA, baseB);
                if (ring == RelationType.GENERATING || ring == RelationType.GENERATED) {
                    return ring;
                }
            }
        }

        // 3. 基底相交
        if (sameBase) {
            return RelationType.SAME;
        }
        return RelationType.NONE;
    }

    private static RelationType ringRelation(ElementType a, ElementType b) {
        if (a == b) {
            return RelationType.SAME;
        }
        if (GENERATES.get(a) == b) {
            return RelationType.GENERATING;
        }
        if (GENERATES.get(b) == a) {
            return RelationType.GENERATED;
        }
        if (COUNTERS.get(a) == b) {
            return RelationType.COUNTER;
        }
        if (COUNTERS.get(b) == a) {
            return RelationType.COUNTERED;
        }
        return RelationType.NONE;
    }

    private static RelationType reverse(RelationType relation) {
        return switch (relation) {
            case GENERATING -> RelationType.GENERATED;
            case GENERATED -> RelationType.GENERATING;
            case COUNTER -> RelationType.COUNTERED;
            case COUNTERED -> RelationType.COUNTER;
            case SAME, NONE -> relation;
        };
    }
}
