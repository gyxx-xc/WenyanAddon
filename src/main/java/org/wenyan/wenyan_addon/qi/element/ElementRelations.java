package org.wenyan.wenyan_addon.qi.element;

import java.util.List;
import java.util.Map;

/**
 * 五行关系：相生环（金生水，水生木，木生火，火生土，土生金）与
 * 相克环（金克木，木克土，土克水，水克火，火克金）。
 * <p>
 * 关系判定优先级（支持嵌套衍生与断开祖先链）：
 * 1. 显式自定义关系（自身 relations，未断开时沿基底链溯源）；
 * 2. 根节点逐对判定：五行根走五行环（相克优先、相生其次）；
 *    断开根查其显式 relations（无则 NONE）；
 * 3. 根重叠 → SAME；否则 NONE。
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
     * 某元素所生（相生对象）：取第一扁平根（五行）的相生对象。
     */
    public static ElementType generates(ElementAttribute element) {
        return GENERATES.get(firstWuxingRoot(element));
    }

    /**
     * 生某元素的属性（相生来源）：取第一扁平根（五行）的相生来源。
     */
    public static ElementType generatedBy(ElementAttribute element) {
        ElementType root = firstWuxingRoot(element);
        if (root == null) {
            return null;
        }
        return GENERATES.entrySet().stream()
                .filter(entry -> entry.getValue() == root)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * 某元素所克（相克对象）：取第一扁平根（五行）的相克对象。
     */
    public static ElementType counters(ElementAttribute element) {
        return COUNTERS.get(firstWuxingRoot(element));
    }

    /**
     * 克某元素的属性（相克来源）：取第一扁平根（五行）的相克来源。
     */
    public static ElementType counteredBy(ElementAttribute element) {
        ElementType root = firstWuxingRoot(element);
        if (root == null) {
            return null;
        }
        return COUNTERS.entrySet().stream()
                .filter(entry -> entry.getValue() == root)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private static ElementType firstWuxingRoot(ElementAttribute element) {
        for (ElementAttribute root : element.flattenedBases()) {
            if (root instanceof ElementType type) {
                return type;
            }
        }
        return null;
    }

    public static RelationType relation(ElementAttribute a, ElementAttribute b) {
        // 1. 显式自定义关系（方向感知，未断开沿基底链溯源）
        RelationType custom = a.customRelation(b);
        if (custom != null) {
            return custom;
        }
        custom = b.customRelation(a);
        if (custom != null) {
            return reverse(custom);
        }

        // 2. 根节点逐对判定
        List<ElementAttribute> rootsA = a.flattenedBases();
        List<ElementAttribute> rootsB = b.flattenedBases();
        boolean sameRoot = false;
        // 相克优先
        for (ElementAttribute rootA : rootsA) {
            for (ElementAttribute rootB : rootsB) {
                if (rootA == rootB) {
                    sameRoot = true;
                    continue;
                }
                RelationType r = rootRelation(rootA, rootB);
                if (r == RelationType.COUNTER || r == RelationType.COUNTERED) {
                    return r;
                }
            }
        }
        // 相生其次
        for (ElementAttribute rootA : rootsA) {
            for (ElementAttribute rootB : rootsB) {
                if (rootA == rootB) {
                    continue;
                }
                RelationType r = rootRelation(rootA, rootB);
                if (r == RelationType.GENERATING || r == RelationType.GENERATED) {
                    return r;
                }
            }
        }

        // 3. 根重叠
        if (sameRoot) {
            return RelationType.SAME;
        }
        return RelationType.NONE;
    }

    /**
     * 根节点对判定：五行根走五行环；断开根查其显式 relations（无则 NONE）。
     */
    private static RelationType rootRelation(ElementAttribute a, ElementAttribute b) {
        if (a instanceof ElementType typeA && b instanceof ElementType typeB) {
            return ringRelation(typeA, typeB);
        }
        // 断开根（或混合）：查显式关系
        RelationType custom = a.customRelation(b);
        if (custom != null) {
            return custom;
        }
        custom = b.customRelation(a);
        if (custom != null) {
            return reverse(custom);
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
