package org.wenyan.wenyan_addon.qi.ritual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * 淬体仪式配方：声明所需物品及数量（8 格盛放方块中满足即触发）与仪式效果。
 * 效果：提升指定属性（空 = 全部）的 ElementCoefficients extras 数值、最大血量、灵气条上限。
 * unlockQi：特殊配方标记（解锁灵气条），true 时配方指定的未解锁属性可获得灵气条上限。
 */
public record QiRitualRecipe(
        List<ItemRequirement> items,
        List<String> attributes,
        Map<String, Double> coefficients,
        double maxHealthBonus,
        double capBonus,
        boolean unlockQi
) {

    /**
     * 单物品需求：item 为物品注册 id，count 为数量。
     */
    public record ItemRequirement(String item, int count) {
        public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(ItemRequirement::item),
                Codec.INT.optionalFieldOf("count", 1).forGetter(ItemRequirement::count)
        ).apply(instance, ItemRequirement::new));
    }

    public static final Codec<QiRitualRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemRequirement.CODEC.listOf().fieldOf("items").forGetter(QiRitualRecipe::items),
            Codec.STRING.listOf().optionalFieldOf("attributes", List.of()).forGetter(QiRitualRecipe::attributes),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("coefficients", Map.of())
                    .forGetter(QiRitualRecipe::coefficients),
            Codec.DOUBLE.optionalFieldOf("maxHealthBonus", 0.0).forGetter(QiRitualRecipe::maxHealthBonus),
            Codec.DOUBLE.optionalFieldOf("capBonus", 0.0).forGetter(QiRitualRecipe::capBonus),
            Codec.BOOL.optionalFieldOf("unlockQi", false).forGetter(QiRitualRecipe::unlockQi)
    ).apply(instance, QiRitualRecipe::new));

    /**
     * 检查物品清单是否满足。
     */
    public boolean matches(List<ItemStack> stacks) {
        for (ItemRequirement requirement : items) {
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .getValue(net.minecraft.resources.Identifier.parse(requirement.item()));
            if (item == null) {
                continue;
            }
            long count = stacks.stream()
                    .filter(stack -> stack.getItem() == item)
                    .mapToLong(ItemStack::getCount)
                    .sum();
            if (count < requirement.count()) {
                return false;
            }
        }
        return true;
    }
}
