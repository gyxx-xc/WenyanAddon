package org.wenyan.wenyan_addon.qi.element;

import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 衍生属性：继承一个或多个基底（可为五行或其它衍生属性，支持嵌套），
 * 可自由指定对其它衍生属性的相生相克关系（自定义关系优先，未断开时沿祖先链溯源）。
 * ancestorBreak：断开祖先链后不溯源，自身作为根节点，仅以显式关系为锚。
 * 未显式定义系数时继承第一基底的默认系数；HUD 颜色可自定义。
 */
public record DerivedElement(String id, String displayName, List<ElementAttribute> bases,
                             boolean ancestorBreak,
                             ElementCoefficients defaultCoefficients,
                             Map<String, RelationType> relations,
                             int color) implements ElementAttribute {

    public DerivedElement(String id, String displayName, ElementType base) {
        this(id, displayName, List.of(base), false, null, Map.of(), 0xFF9AA5B1);
    }

    public DerivedElement(String id, String displayName, List<ElementAttribute> bases) {
        this(id, displayName, bases, false, null, Map.of(), 0xFF9AA5B1);
    }

    public DerivedElement withCoefficients(ElementCoefficients coefficients) {
        return new DerivedElement(id, displayName, bases, ancestorBreak, coefficients, relations, color);
    }

    /**
     * 断开祖先链：不溯源、自身作根。
     */
    public DerivedElement withBreak() {
        return new DerivedElement(id, displayName, bases, true, defaultCoefficients, relations, color);
    }

    /**
     * 指定对其它元素的自定义关系（GENERATING/GENERATED/COUNTER/COUNTERED）。
     */
    public DerivedElement withRelation(String targetId, RelationType relation) {
        Map<String, RelationType> updated = new java.util.HashMap<>(relations);
        updated.put(targetId, relation);
        return new DerivedElement(id, displayName, bases, ancestorBreak, defaultCoefficients, updated, color);
    }

    public DerivedElement withColor(int color) {
        return new DerivedElement(id, displayName, bases, ancestorBreak, defaultCoefficients, relations, color);
    }

    @Override
    public ElementCoefficients defaultCoefficients() {
        if (defaultCoefficients != null) {
            return defaultCoefficients;
        }
        // 断开属性：flattenedBases = [自身]，直接使用默认系数（不递归）
        if (ancestorBreak) {
            return ElementCoefficients.DEFAULT;
        }
        return ElementRegistry.coefficients(this);
    }

    /**
     * 显式配置的系数（仅字段访问，不触发自动计算，供注册表判断）。
     */
    public ElementCoefficients explicit() {
        return defaultCoefficients;
    }

    @Override
    public List<ElementAttribute> flattenedBases() {
        // 断开：自身作根；否则递归展开所有基底并去重
        if (ancestorBreak) {
            return List.of(this);
        }
        Set<ElementAttribute> flattened = new LinkedHashSet<>();
        for (ElementAttribute base : bases) {
            flattened.addAll(base.flattenedBases());
        }
        return List.copyOf(flattened);
    }

    @Override
    public RelationType customRelation(ElementAttribute other) {
        RelationType direct = relations.get(other.id());
        if (direct != null || ancestorBreak) {
            return direct;
        }
        // 未断开：沿基底链溯源（近祖优先）
        for (ElementAttribute base : bases) {
            RelationType inherited = base.customRelation(other);
            if (inherited != null) {
                return inherited;
            }
        }
        return null;
    }
}
