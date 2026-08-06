package org.wenyan.wenyan_addon.device.handler.data_disk;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.data_storage.DataDiskStorage;

import java.util.Optional;
import java.util.UUID;

public class StorageRuneBlockEntity extends BlockEntity {
    public static final int DISK_SLOT_COUNT = 9;

    private final ItemStacksResourceHandler disks = new ItemStacksResourceHandler(DISK_SLOT_COUNT) {
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return !resource.isEmpty() && resource.toStack(1).is(WenyanAddon.DATA_DISK_ITEM.get());
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }

        @Override
        public void deserialize(ValueInput input) {
            super.deserialize(input);
            if (stacks.size() != DISK_SLOT_COUNT) {
                NonNullList<ItemStack> resized = NonNullList.withSize(DISK_SLOT_COUNT, ItemStack.EMPTY);
                for (int i = 0; i < Math.min(stacks.size(), DISK_SLOT_COUNT); i++) {
                    resized.set(i, stacks.get(i));
                }
                setStacks(resized);
            }
        }
    };

    public StorageRuneBlockEntity(BlockPos pos, BlockState blockState) {
        super(WenyanAddon.STORAGE_RUNE_BLOCK_ENTITY.get(), pos, blockState);
    }

    // ===== 磁盘管理方法 =====

    public ItemStacksResourceHandler getDisks() {
        return disks;
    }

    public int getDiskSlots() {
        return disks.size();
    }

    public ItemStack getDisk(int slot) {
        if (slot < 0 || slot >= disks.size()) {
            return ItemStack.EMPTY;
        }
        return disks.getResource(slot).toStack(disks.getAmountAsInt(slot));
    }

    public ItemStack takeDisk(int slot) {
        if (slot < 0 || slot >= disks.size()) {
            return ItemStack.EMPTY;
        }
        ItemResource resource = disks.getResource(slot);
        int amount = disks.getAmountAsInt(slot);
        if (resource.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = resource.toStack(amount);
        disks.set(slot, ItemResource.EMPTY, 0);
        return stack;
    }

    public Optional<UUID> ensureDiskId(int slot) {
        if (slot < 0 || slot >= disks.size()) {
            return Optional.empty();
        }
        ItemResource resource = disks.getResource(slot);
        int amount = disks.getAmountAsInt(slot);
        if (resource.isEmpty()) {
            return Optional.empty();
        }
        ItemStack stack = resource.toStack(amount);
        UUID id = DataDiskStorage.getOrCreateDiskId(stack);
        disks.set(slot, ItemResource.of(stack), stack.getCount());
        return Optional.of(id);
    }

    public boolean hasDisk(UUID diskId) {
        for (int slot = 0; slot < disks.size(); slot++) {
            ItemStack disk = getDisk(slot);
            if (!disk.isEmpty()) {
                Optional<UUID> id = DataDiskStorage.getDiskId(disk);
                if (id.isPresent() && id.get().equals(diskId)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ===== 方块移除时掉落所有磁盘 =====

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level instanceof ServerLevel serverLevel) {
            for (int slot = 0; slot < disks.size(); slot++) {
                ItemStack disk = takeDisk(slot);
                if (!disk.isEmpty()) {
                    Block.popResource(serverLevel, pos, disk);
                }
            }
        }
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
