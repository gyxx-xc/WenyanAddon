package org.wenyan.wenyan_addon.device.handler.data_disk;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.wenyan.wenyan_addon.WenyanAddon;

public class StorageRuneMenu extends AbstractContainerMenu {
    private static final int PLAYER_INV_END = 36;
    private static final int DISK_SLOTS_START = PLAYER_INV_END;
    private static final int DISK_SLOT_COUNT = StorageRuneBlockEntity.DISK_SLOT_COUNT;

    private final ContainerLevelAccess access;
    private final ItemStacksResourceHandler disks;

    public StorageRuneMenu(int containerId, Inventory inv) {
        this(containerId, inv, new ItemStacksResourceHandler(DISK_SLOT_COUNT), ContainerLevelAccess.NULL);
    }

    public StorageRuneMenu(int containerId, Inventory inv, ItemStacksResourceHandler disks, ContainerLevelAccess access) {
        super(WenyanAddon.STORAGE_RUNE_MENU.get(), containerId);
        this.access = access;
        this.disks = disks;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        for (int i = 0; i < disks.size(); i++) {
            this.addSlot(new ResourceHandlerSlot(disks, disks::set, i, 8 + i * 18, 18));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index < DISK_SLOTS_START) {
                if (!moveItemStackTo(stack, DISK_SLOTS_START, DISK_SLOTS_START + DISK_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < DISK_SLOTS_START + DISK_SLOT_COUNT) {
                if (!moveItemStackTo(stack, 0, PLAYER_INV_END, true)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, WenyanAddon.STORAGE_RUNE_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 49 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 107));
        }
    }
}
