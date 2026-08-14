package org.wenyan.wenyan_addon.qi.storage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

/**
 * ItemStack NBT 灵气容器：储量存于 CUSTOM_DATA 内嵌 tag（属性 id → 数量）。
 */
public final class ItemQiContainer implements QiContainer {
    private static final String NBT_KEY = "WenyanQiVessel";

    private final ItemStack stack;

    public ItemQiContainer(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public double get(ElementAttribute element) {
        return tags().getDoubleOr(element.id(), 0.0);
    }

    @Override
    public double consume(ElementAttribute element, double amount) {
        CompoundTag tag = tags();
        double current = tag.getDoubleOr(element.id(), 0.0);
        double removed = Math.min(amount, current);
        if (removed > 0) {
            tag.putDouble(element.id(), current - removed);
            save(tag);
        }
        return removed;
    }

    @Override
    public double add(ElementAttribute element, double amount) {
        CompoundTag tag = tags();
        double current = tag.getDoubleOr(element.id(), 0.0);
        double added = Math.min(amount, CAPACITY - current);
        if (added > 0) {
            tag.putDouble(element.id(), current + added);
            save(tag);
        }
        return added;
    }

    private CompoundTag tags() {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag outer = data.copyTag();
            if (outer.contains(NBT_KEY)) {
                return outer.getCompoundOrEmpty(NBT_KEY);
            }
        }
        return new CompoundTag();
    }

    private void save(CompoundTag vesselTag) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, outer -> outer.put(NBT_KEY, vesselTag));
    }
}
