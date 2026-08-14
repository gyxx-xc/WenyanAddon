package org.wenyan.wenyan_addon.qi.element;

import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;

import java.util.List;
import java.util.Map;

/**
 * 衍生属性：继承一个或多个五行基底（克制关系随基底参与五行环判定），
 * 可自由指定对其它衍生属性的相生相克关系（自定义关系优先）。
 * 未显式定义系数时继承第一基底的默认系数；HUD 颜色可自定义。
 */
public record DerivedElement(String id, String displayName, List<ElementType> bases,
                             ElementCoefficients defaultCoefficients,
                             Map<String, RelationType> relations,
                             int color) implements ElementAttribute {

    public DerivedElement(String id, String displayName, ElementType base) {
        this(id, displayName, List.of(base), null, Map.of(), 0xFF9AA5B1);
    }

    public DerivedElement(String id, String displayName, List<ElementType> bases) {
        this(id, displayName, bases, null, Map.of(), 0xFF9AA5B1);
    }

    public DerivedElement withCoefficients(ElementCoefficients coefficients) {
        return new DerivedElement(id, displayName, bases, coefficients, relations, color);
    }

    /**
     * 指定对其它元素的自定义关系（GENERATING/GENERATED/COUNTER/COUNTERED）。
     */
    public DerivedElement withRelation(String targetId, RelationType relation) {
        Map<String, RelationType> updated = new java.util.HashMap<>(relations);
        updated.put(targetId, relation);
        return new DerivedElement(id, displayName, bases, defaultCoefficients, updated, color);
    }

    public DerivedElement withColor(int color) {
        return new DerivedElement(id, displayName, bases, defaultCoefficients, relations, color);
    }

    @Override
    public ElementCoefficients defaultCoefficients() {
        return defaultCoefficients != null ? defaultCoefficients : bases.get(0).defaultCoefficients();
    }

    @Override
    public RelationType customRelation(ElementAttribute other) {
        return relations.get(other.id());
    }
}
