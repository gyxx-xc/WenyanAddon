package org.wenyan.wenyan_addon.qi.storage;

import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

/**
 * 灵气容器：物品（随身携带）与方块（驻扎使用）共用的存储抽象。
 * 容量：每个属性独立上限 {@link #CAPACITY}。
 */
public interface QiContainer {
    /**
     * 每属性容量（五行/阴阳/无属性/衍生各 1000）。
     */
    double CAPACITY = 1000.0;

    double get(ElementAttribute element);

    default boolean has(ElementAttribute element, double amount) {
        return get(element) >= amount;
    }

    /**
     * 扣除指定数量，返回实际扣除量（不足时部分扣除）。
     */
    double consume(ElementAttribute element, double amount);

    /**
     * 注入指定数量，返回实际注入量（超出容量时部分注入）。
     */
    double add(ElementAttribute element, double amount);
}
