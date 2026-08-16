package org.wenyan.wenyan_addon.qi.spell;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.List;

/**
 * 法术物品接口：实现此接口的物品是法术物品，对应文言函数的灵气属性。
 */
public interface QiSpellItem extends QiSpellSource {

    @Override
    default List<ElementAttribute> spellElements() {
        return List.of(element());
    }

    default ElementAttribute element() {
        return spellElements().isEmpty() ? null : spellElements().get(0);
    }
}
