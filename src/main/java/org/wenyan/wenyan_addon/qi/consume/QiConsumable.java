package org.wenyan.wenyan_addon.qi.consume;

import java.util.List;

/**
 * 灵气消耗接口：任何需要消耗灵气的设备（符咒设备、灵气石等）实现此接口，
 * 声明需要消耗的灵气构成（可包含多种元素）。消耗操作由 {@link QiConsumption#tryConsume} 执行。
 */
public interface QiConsumable {
    /**
     * 需要消耗的灵气清单（可包含多种元素及其数量）。
     */
    List<QiCost> qiCosts();
}
