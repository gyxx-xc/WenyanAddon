package org.wenyan.wenyan_addon.qi.liquid;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;

/**
 * 灵液 NBT 工具：灵液水瓶（属性 + 量）与纯化产物（恢复药水）的数据读写。
 */
public final class QiLiquidNbt {
    public static final String LIQUID_KEY = "WenyanLiquid";
    public static final String POTION_KEY = "WenyanRestorePotion";

    private QiLiquidNbt() {
    }

    public static ItemStack liquidBottle(ElementAttribute attribute, double amount) {
        ItemStack stack = org.wenyan.wenyan_addon.WenyanAddon.QI_LIQUID_BOTTLE_ITEM.get().getDefaultInstance();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, outer -> {
            CompoundTag tag = new CompoundTag();
            tag.putString("attribute", attribute.id());
            tag.putDouble("amount", amount);
            outer.put(LIQUID_KEY, tag);
        });
        return stack;
    }

    public static ElementAttribute liquidAttribute(ItemStack stack) {
        CompoundTag tag = liquidTag(stack);
        if (tag == null) {
            return null;
        }
        return ElementRegistry.byId(tag.getString("attribute").orElse(""));
    }

    public static double liquidAmount(ItemStack stack) {
        CompoundTag tag = liquidTag(stack);
        return tag != null ? tag.getDoubleOr("amount", 0.0) : 0;
    }

    private static CompoundTag liquidTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag outer = data.copyTag();
            if (outer.contains(LIQUID_KEY)) {
                return outer.getCompoundOrEmpty(LIQUID_KEY);
            }
        }
        return null;
    }
}
