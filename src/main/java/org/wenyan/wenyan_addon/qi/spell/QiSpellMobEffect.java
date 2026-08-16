package org.wenyan.wenyan_addon.qi.spell;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.List;

/**
 * 法术药水效果接口：实现此接口的药水效果是法术效果，对应文言函数的灵气属性。
 * 效果生效时（如持续伤害/每 tick）由调用方触发伤害应用。
 */
public interface QiSpellMobEffect extends QiSpellSource {

    @Override
    default List<ElementAttribute> spellElements() {
        return List.of(element());
    }

    default ElementAttribute element() {
        return spellElements().isEmpty() ? null : spellElements().get(0);
    }
}
