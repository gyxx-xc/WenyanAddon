package org.wenyan.wenyan_addon.qi.player;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * 玩家全部物品遍历：主背包（非装备槽）+ 装备栏（护甲/身体/主手/副手）。
 */
public final class PlayerEquipment {
    private PlayerEquipment() {
    }

    public static void forEachItem(Player player, Consumer<ItemStack> consumer) {
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            consumer.accept(stack);
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            consumer.accept(player.getItemBySlot(slot));
        }
    }
}
