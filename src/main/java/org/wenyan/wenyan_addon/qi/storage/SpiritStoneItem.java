package org.wenyan.wenyan_addon.qi.storage;

import net.minecraft.world.item.ItemStack;
import org.wenyan.wenyan_addon.item.TooltipItem;

/**
 * 灵石：强化生物（携带灵气属性标记）掉落的一次性灵气容器。
 * 纯度随机（杂质 5-30% / 纯质 50-70% / 精纯 90-100%），灵气用光后消失；
 * 文言函数抽取灵力时优先级最低。
 */
public class SpiritStoneItem extends TooltipItem implements QiContainerProvider {
    public SpiritStoneItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public QiContainer containerOf(ItemStack stack) {
        return new SpiritStoneContainer(stack);
    }
}
