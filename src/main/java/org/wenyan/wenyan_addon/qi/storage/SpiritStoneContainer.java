package org.wenyan.wenyan_addon.qi.storage;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

/**
 * 灵石 NBT 容器：一次性灵气容器（纯度决定初始灵气量，用光后物品消失）。
 * 纯度：杂质 5-30% / 纯质 50-70% / 精纯 90-100%。
 */
public final class SpiritStoneContainer implements QiContainer {
    public static final String NBT_KEY = "WenyanSpiritStone";
    public static final double TOTAL_QI = 1000.0; // 固定总量

    private final ItemStack stack;

    public SpiritStoneContainer(ItemStack stack) {
        this.stack = stack;
    }

    /**
     * 纯度（0~1），未指定时默认 0.5。
     */
    public static double purity(ItemStack stack) {
        CompoundTag tag = tags(stack);
        return tag.contains("purity") ? tag.getDoubleOr("purity", 0.5) : 0.5;
    }

    public static void setPurity(ItemStack stack, double purity) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, outer -> {
            CompoundTag tag = outer.contains(NBT_KEY) ? outer.getCompoundOrEmpty(NBT_KEY) : new CompoundTag();
            tag.putDouble("purity", purity);
            tag.putDouble("amount", TOTAL_QI * purity);
            outer.put(NBT_KEY, tag);
        });
    }

    @Override
    public double get(ElementAttribute element) {
        if (element.id().equals("neutral")) {
            return amount();
        }
        return 0;
    }

    /**
     * 当前灵气量（用光后物品消失）。
     */
    private double amount() {
        CompoundTag tag = tags(stack);
        return tag.contains("amount") ? tag.getDoubleOr("amount", 0.0) : TOTAL_QI * purity(stack);
    }

    @Override
    public double consume(ElementAttribute element, double amount) {
        if (!element.id().equals("neutral")) {
            return 0;
        }
        double current = amount();
        double removed = Math.min(amount, current);
        if (removed > 0) {
            double remaining = current - removed;
            CustomData.update(DataComponents.CUSTOM_DATA, stack, outer -> {
                CompoundTag tag = outer.contains(NBT_KEY) ? outer.getCompoundOrEmpty(NBT_KEY) : new CompoundTag();
                tag.putDouble("amount", remaining);
                outer.put(NBT_KEY, tag);
            });
            if (remaining <= 0) {
                stack.shrink(1); // 用光后消失
            }
        }
        return removed;
    }

    @Override
    public double add(ElementAttribute element, double amount) {
        return 0; // 灵石不可注入
    }

    @Override
    public int priority() {
        return 100; // 抽取优先级最低（最后支付）
    }

    private static CompoundTag tags(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag outer = data.copyTag();
            if (outer.contains(NBT_KEY)) {
                return outer.getCompoundOrEmpty(NBT_KEY);
            }
        }
        return new CompoundTag();
    }
}
