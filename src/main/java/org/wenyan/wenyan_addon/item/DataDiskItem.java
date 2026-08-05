package org.wenyan.wenyan_addon.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.wenyan.wenyan_addon.data_storage.DataDiskStorage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DataDiskItem extends TooltipItem {
    public DataDiskItem(Properties properties, String tooltipKey) {
        super(properties, tooltipKey);
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, Player player) {
        super.onCraftedBy(itemStack, player);
        DataDiskStorage.getOrCreateDiskId(itemStack);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, level, owner, slot);
        DataDiskStorage.getOrCreateDiskId(itemStack);
    }
}
