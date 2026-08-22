package org.wenyan.wenyan_addon.qi.element;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.wenyan.wenyan_addon.WenyanAddon;

import java.util.List;

/**
 * 物品属性标记 tooltip：被标记物品在 tooltip 末尾显示其五行属性（各属性名按属性颜色渲染）。
 */
@EventBusSubscriber(modid = WenyanAddon.MODID, value = Dist.CLIENT)
public final class ItemAttributeTooltip {
    private ItemAttributeTooltip() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        List<ElementAttribute> attrs = ItemAttributeRegistry.of(event.getItemStack());
        if (attrs.isEmpty()) {
            return;
        }
        MutableComponent line = Component.literal("属性标记：").withStyle(ChatFormatting.GRAY);
        for (int i = 0; i < attrs.size(); i++) {
            ElementAttribute attr = attrs.get(i);
            line.append(Component.literal(attr.displayName())
                    .withColor(attr.color() & 0xFFFFFF));
            if (i < attrs.size() - 1) {
                line.append(Component.literal("、").withStyle(ChatFormatting.GRAY));
            }
        }
        event.getToolTip().add(line);
    }
}