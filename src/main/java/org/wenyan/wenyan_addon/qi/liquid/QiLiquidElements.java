package org.wenyan.wenyan_addon.qi.liquid;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.wenyan.wenyan_addon.qi.element.ElementType;

import java.util.Map;

/**
 * 五行矿石映射：收集方块放入对应物品确定收集的灵气属性。
 */
public final class QiLiquidElements {
    public static final Map<Item, ElementType> ORES = Map.of(
            Items.GOLD_INGOT, ElementType.METAL,
            Items.OAK_LOG, ElementType.WOOD,
            Items.GLASS_BOTTLE, ElementType.WATER,
            Items.BLAZE_ROD, ElementType.FIRE,
            Items.CLAY_BALL, ElementType.EARTH
    );

    private QiLiquidElements() {
    }

    public static ElementType elementOf(Item item) {
        return ORES.get(item);
    }
}
