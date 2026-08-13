package org.wenyan.wenyan_addon.qi.storage;

import net.minecraft.world.item.ItemStack;

/**
 * 物品灵气容器提供者：实现此接口的物品可持有灵气（灵珠等随身容器）。
 */
public interface QiContainerProvider {
    QiContainer containerOf(ItemStack stack);
}
