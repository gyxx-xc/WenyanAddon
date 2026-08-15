package org.wenyan.wenyan_addon.qi.element;

import org.wenyan.wenyan_addon.qi.player.ElementCoefficients;

import java.util.List;

/**
 * 元素属性定义：五行/阴阳/无属性（{@link ElementType} 枚举）与运行时注册的衍生属性。
 * 衍生属性可嵌套继承（基底可为五行或其它衍生属性），克制关系随基底参与五行环判定；
 * 可指定 {@link #ancestorBreak()} 断开祖先链（自身成为根节点，仅以显式关系为锚）。
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
     * 直接基底：五行/阴阳/无属性返回自身；衍生属性返回其继承的基底（可嵌套）。
     */
    List<? extends ElementAttribute> bases();

    /**
     * 扁平化根节点集：递归展开嵌套基底（去重）。
     * 结果只含五行（{@link ElementType}）与断开祖先链的衍生属性（自身作根）。
     */
    List<ElementAttribute> flattenedBases();

    /**
     * 是否断开祖先链：断开后不向上溯源（不继承祖先显式关系、不展开祖先基底），
     * 自身作为根节点参与关系判定，仅以显式关系为锚。
     */
    default boolean ancestorBreak() {
        return false;
    }

    /**
     * 默认系数：未显式设置时玩家使用的系数；衍生属性未覆盖时继承第一基底。
     */
    default ElementCoefficients defaultCoefficients() {
        return ElementCoefficients.DEFAULT;
    }

    /**
     * 对其它属性（通常为衍生属性）自定义的相生相克关系，null 表示未指定（按基底判定）。
     * 未断开时沿基底链向上溯源（祖先显式关系继承）。
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
