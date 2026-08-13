package org.wenyan.wenyan_addon.qi.storage;

import net.minecraft.world.item.ItemStack;
import org.wenyan.wenyan_addon.item.TooltipItem;

/**
 * 灵珠：随身携带的灵气容器（NBT 存储），可被消耗系统优先使用。
 */
public class QiVesselItem extends TooltipItem implements QiContainerProvider {
    public QiVesselItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public QiContainer containerOf(ItemStack stack) {
        return new ItemQiContainer(stack);
    }
}
