package org.wenyan.wenyan_addon.qi.element;

import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;

import java.util.List;

/**
 * 元素属性定义：五行/阴阳/无属性（{@link ElementType} 枚举）与运行时注册的衍生属性。
 * 衍生属性可继承一个或多个五行基底，克制关系随基底参与五行环判定；
 * 也可单独指定对其它衍生属性的相生相克关系（自定义关系优先）。
 */
public interface ElementAttribute {
    /**
     * 注册 id（稳定标识，用于存档与注解引用）。
     */
    String id();

    /**
     * 显示名（文言名）。
     */
    String displayName();

    /**
     * 五行基底：衍生属性返回其继承的五行（可复数）；五行/阴阳/无属性返回自身。
     */
    List<ElementType> bases();

    /**
     * 默认系数：未显式设置时玩家使用的系数；衍生属性未覆盖时继承第一基底。
     */
    default ElementCoefficients defaultCoefficients() {
        return ElementCoefficients.DEFAULT;
    }

    /**
     * 对其它属性（通常为衍生属性）自定义的相生相克关系，null 表示未指定（按基底判定）。
     */
    default RelationType customRelation(ElementAttribute other) {
        return null;
    }

    /**
     * HUD 显示颜色（ARGB）。
     */
    default int color() {
        return 0xFF9AA5B1;
    }
}
