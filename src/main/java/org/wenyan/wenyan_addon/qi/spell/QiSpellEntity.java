package org.wenyan.wenyan_addon.qi.spell;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.List;

/**
 * 法术实体接口：实现此接口的实体是法术生成的实体，对应文言函数的灵气属性。
 * 实体碰撞/命中目标时由调用方触发 {@link org.wenyan.wenyan_addon.qi.damage.QiDamageHelper} 应用伤害。
 */
public interface QiSpellEntity extends QiSpellSource {

    @Override
    default List<ElementAttribute> spellElements() {
        return List.of(element());
    }

    /**
     * 单属性便捷：复数属性覆写 {@link #spellElements()}。
     */
    default ElementAttribute element() {
        return spellElements().isEmpty() ? null : spellElements().get(0);
    }
}
