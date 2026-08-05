package org.wenyan.wenyan_addon.device.handler.data_disk;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.data_storage.DataDiskStorage;

import java.util.Optional;
import java.util.UUID;

public class StorageRuneBlockEntity extends BlockEntity {
    public static final int DISK_SLOT_COUNT = 4;

    private final ItemStackHandler disks = new ItemStackHandler(DISK_SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return org.wenyan.wenyan_addon.data_storage.DataDiskStorage.isDataDisk(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public StorageRuneBlockEntity(BlockPos pos, BlockState blockState) {
        super(WenyanAddon.STORAGE_RUNE_BLOCK_ENTITY.get(), pos, blockState);
    }

    // ===== 磁盘管理方法 =====

    public ItemStack insertDisk(ItemStack stack) {
        if (!org.wenyan.wenyan_addon.data_storage.DataDiskStorage.isDataDisk(stack)) {
            return stack;
        }
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < disks.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = disks.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    public ItemStack getDisk(int slot) {
        if (slot < 0 || slot >= disks.getSlots()) {
            return ItemStack.EMPTY;
        }
        return disks.getStackInSlot(slot);
    }

    public ItemStack extractLastDisk() {
        for (int slot = disks.getSlots() - 1; slot >= 0; slot--) {
            ItemStack extracted = disks.extractItem(slot, 1, false);
            if (!extracted.isEmpty()) {
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    public int getDiskSlots() {
        return disks.getSlots();
    }

    public boolean hasDisk(UUID diskId) {
        for (int slot = 0; slot < disks.getSlots(); slot++) {
            ItemStack disk = disks.getStackInSlot(slot);
            if (!disk.isEmpty()) {
                Optional<UUID> id = DataDiskStorage.getDiskId(disk);
                if (id.isPresent() && id.get().equals(diskId)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ===== 序列化 =====

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        disks.serialize(output.child("DataDisks"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("DataDisks").ifPresent(disks::deserialize);
    }
}