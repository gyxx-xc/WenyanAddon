package org.wenyan.wenyan_addon.qi.spell;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.List;

/**
 * 法术伤害来源：实现者声明法术属性与基础伤害。
 * 伤害来源 = 施法者；各属性伤害参数自施法者系数引用。
 * 复数属性时，按各属性灵气值占比将单次伤害拆分为多段属性伤害。
 */
public interface QiSpellSource {
    /**
     * 法术属性列表（复数属性 = 复合伤害）。
     */
    List<ElementAttribute> spellElements();

    /**
     * 法术基础伤害（最终数值由文言函数与接口实现共同决定）。
     */
    double baseDamage();
}
