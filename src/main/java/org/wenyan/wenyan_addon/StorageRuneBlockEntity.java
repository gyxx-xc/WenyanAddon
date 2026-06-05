package org.wenyan.wenyan_addon;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

public class StorageRuneBlockEntity extends BlockEntity {
    private static final int SLOT_COUNT = 27;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public StorageRuneBlockEntity(BlockPos pos, BlockState blockState) {
        super(WenyanAddon.STORAGE_RUNE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public int insert(ItemStack stack) {
        int before = stack.getCount();
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < items.getSlots() && !remaining.isEmpty(); slot++) {
            remaining = items.insertItem(slot, remaining, false);
        }
        return before - remaining.getCount();
    }

    public ItemStack extractAny(int maxCount) {
        int remaining = Math.max(0, maxCount);
        ItemStack result = ItemStack.EMPTY;
        for (int slot = 0; slot < items.getSlots() && remaining > 0; slot++) {
            ItemStack extracted = items.extractItem(slot, remaining, false);
            if (extracted.isEmpty()) {
                continue;
            }
            if (result.isEmpty()) {
                result = extracted;
            } else if (ItemStack.isSameItemSameComponents(result, extracted)) {
                result.grow(extracted.getCount());
            } else {
                items.insertItem(slot, extracted, false);
                break;
            }
            remaining -= extracted.getCount();
        }
        return result;
    }

    public int getStoredCount() {
        int count = 0;
        for (int slot = 0; slot < items.getSlots(); slot++) {
            count += items.getStackInSlot(slot).getCount();
        }
        return count;
    }

    public int getComparatorSignal() {
        int filled = 0;
        float fullness = 0.0f;
        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                fullness += (float) stack.getCount() / Math.min(items.getSlotLimit(slot), stack.getMaxStackSize());
                filled++;
            }
        }
        return filled == 0 ? 0 : (int) Math.floor((fullness / items.getSlots()) * 14.0f) + 1;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        items.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.deserialize(input);
    }
}
