package org.pongdev.pong.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

final class PongStackData {
    private PongStackData() {
    }

    static CompoundTag get(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    static void update(ItemStack stack, java.util.function.Consumer<CompoundTag> updater) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    }

    static boolean getBoolean(ItemStack stack, String key) {
        return get(stack).getBoolean(key).orElse(false);
    }

    static int getInt(ItemStack stack, String key) {
        return get(stack).getInt(key).orElse(0);
    }

    static double getDouble(ItemStack stack, String key) {
        return get(stack).getDouble(key).orElse(0.0);
    }

    static String getString(ItemStack stack, String key) {
        return get(stack).getString(key).orElse("");
    }

    static void putBoolean(ItemStack stack, String key, boolean value) {
        update(stack, tag -> tag.putBoolean(key, value));
    }

    static void putInt(ItemStack stack, String key, int value) {
        update(stack, tag -> tag.putInt(key, value));
    }

    static void putDouble(ItemStack stack, String key, double value) {
        update(stack, tag -> tag.putDouble(key, value));
    }

    static void putString(ItemStack stack, String key, String value) {
        update(stack, tag -> tag.putString(key, value));
    }

    static void copyTo(ItemStack source, ItemStack target) {
        CompoundTag tag = get(source);
        if (!tag.isEmpty()) {
            CustomData.set(DataComponents.CUSTOM_DATA, target, tag);
        }
    }
}
