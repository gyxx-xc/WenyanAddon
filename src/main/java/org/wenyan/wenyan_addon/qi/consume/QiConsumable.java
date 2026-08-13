package org.wenyan.wenyan_addon.qi.consume;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

/**
 * 灵气消耗接口：符咒设备实现此接口，声明符咒属性、基础消耗与阴阳倾向。
 * 消耗操作由 {@link QiConsumption#tryConsume} 执行（五行 × 阴阳 优先级表）。
 */
public interface QiConsumable {
    /**
     * 符咒属性（五行或衍生属性，施法时按此元素参与相生/相克判定）。
     */
    ElementAttribute spellElement();

    /**
     * 基础消耗量（原消耗量 N）。
     */
    double baseQiCost();

    /**
     * 符咒阴阳倾向。
     */
    YinYangTendency tendency();
}
