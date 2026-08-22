package org.wenyan.wenyan_addon.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 符咒包/拓展包容器组件：存储包内物品（带槽位的 ItemStackTemplate 列表）+ 当前选中槽位。
 * activeSlot 为 -1 表示未选中（仅符咒包使用；拓展包恒为 0）。
 * 最多 FuluPouchMenu.SLOT_COUNT 个；空槽不占位（序列化时省略）。
 * 保存按槽位索引，还原时恢复原槽位位置。
 */
public record FuluPouchComponent(List<SlotEntry> items, int activeSlot) {

    public static final int NO_SELECTION = -1;

    public record SlotEntry(int slot, ItemStackTemplate template) {
        public static final Codec<SlotEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("slot").forGetter(SlotEntry::slot),
                ItemStackTemplate.CODEC.fieldOf("item").forGetter(SlotEntry::template)
        ).apply(inst, SlotEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SlotEntry> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, SlotEntry::slot,
                ItemStackTemplate.STREAM_CODEC, SlotEntry::template,
                SlotEntry::new);
    }

    public static final FuluPouchComponent EMPTY = new FuluPouchComponent(List.of(), NO_SELECTION);

    public static final Codec<FuluPouchComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            SlotEntry.CODEC.sizeLimitedListOf(FuluPouchMenu.SLOT_COUNT)
                    .fieldOf("items").forGetter(FuluPouchComponent::items),
            Codec.INT.optionalFieldOf("active_slot", 0).forGetter(FuluPouchComponent::activeSlot)
    ).apply(inst, FuluPouchComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FuluPouchComponent> STREAM_CODEC = StreamCodec.composite(
            SlotEntry.STREAM_CODEC.apply(ByteBufCodecs.list(FuluPouchMenu.SLOT_COUNT)),
            FuluPouchComponent::items,
            ByteBufCodecs.VAR_INT,
            FuluPouchComponent::activeSlot,
            FuluPouchComponent::new);

    /**
     * 当前选中槽的物品；未选中或越界返回空栈。
     */
    public ItemStack selectedItem() {
        if (activeSlot < 0 || activeSlot >= FuluPouchMenu.SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return createItems().get(activeSlot);
    }

    /**
     * 还原为定长物品栈列表（SLOT_COUNT 个，空槽为 ItemStack.EMPTY）。
     */
    public List<ItemStack> createItems() {
        List<ItemStack> result = new ArrayList<>(FuluPouchMenu.SLOT_COUNT);
        for (int i = 0; i < FuluPouchMenu.SLOT_COUNT; i++) {
            result.add(ItemStack.EMPTY);
        }
        for (SlotEntry entry : items) {
            if (entry.slot() >= 0 && entry.slot() < FuluPouchMenu.SLOT_COUNT) {
                result.set(entry.slot(), entry.template().create());
            }
        }
        return result;
    }

    /**
     * 用新的物品栈列表生成组件（定长输入，仅保留非空项并记录槽位）。
     */
    public FuluPouchComponent withItems(List<ItemStack> stacks) {
        List<SlotEntry> entries = new ArrayList<>(FuluPouchMenu.SLOT_COUNT);
        for (int i = 0; i < stacks.size() && i < FuluPouchMenu.SLOT_COUNT; i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                entries.add(new SlotEntry(i, ItemStackTemplate.fromNonEmptyStack(stack)));
            }
        }
        return new FuluPouchComponent(entries, activeSlot);
    }

    public FuluPouchComponent withActiveSlot(int slot) {
        return new FuluPouchComponent(items, slot);
    }

    public FuluPouchComponent withItem(int slot, ItemStack stack) {
        Map<Integer, ItemStackTemplate> bySlot = new HashMap<>();
        for (SlotEntry entry : items) {
            bySlot.put(entry.slot(), entry.template());
        }
        if (stack.isEmpty()) {
            bySlot.remove(slot);
        } else {
            bySlot.put(slot, ItemStackTemplate.fromNonEmptyStack(stack));
        }
        List<SlotEntry> entries = bySlot.entrySet().stream()
                .map(e -> new SlotEntry(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(SlotEntry::slot))
                .limit(FuluPouchMenu.SLOT_COUNT)
                .toList();
        return new FuluPouchComponent(entries, activeSlot);
    }
}