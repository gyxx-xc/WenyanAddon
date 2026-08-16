package org.wenyan.wenyan_addon.qi.spell;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.List;

/**
 * 法术方块接口：实现此接口的方块是法术生成的方块，对应文言函数的灵气属性。
 * 方块接触/交互目标时由调用方触发伤害应用。
 */
public interface QiSpellBlock extends QiSpellSource {

    @Override
    default List<ElementAttribute> spellElements() {
        return List.of(element());
    }

    default ElementAttribute element() {
        return spellElements().isEmpty() ? null : spellElements().get(0);
    }
}
