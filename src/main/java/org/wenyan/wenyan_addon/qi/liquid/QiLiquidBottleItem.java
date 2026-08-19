package org.wenyan.wenyan_addon.qi.liquid;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.wenyan.wenyan_addon.item.TooltipItem;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * 灵液水瓶：从灵液收集方块接取，用于灵液纯化产出灵气恢复药水。
 * tooltip 显示当前灵液属性与液量。
 */
public class QiLiquidBottleItem extends TooltipItem {
    public QiLiquidBottleItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        ElementAttribute attribute = QiLiquidNbt.liquidAttribute(stack);
        double amount = QiLiquidNbt.liquidAmount(stack);
        if (attribute != null && amount > 0) {
            tooltip.accept(Component.literal(
                            attribute.displayName() + "灵液：" + String.format(Locale.ROOT, "%.0f", amount))
                    .withStyle(ChatFormatting.AQUA));
        }
    }
}