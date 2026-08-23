package org.wenyan.wenyan_addon.qi.ritual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ItemAttributeRegistry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 淬体仪式配方：声明所需物品及数量（8 格盛放方块中满足即触发）与仪式效果。
 * 效果：提升指定属性（空 = 全部）的 ElementCoefficients extras 数值、最大血量、灵气条上限。
 * unlockQi：特殊配方标记（解锁灵气条），true 时配方指定的未解锁属性可获得灵气条上限。
 * 物品需求支持两种匹配：item（精确物品注册 id）或 attribute（属性标记相符的物品，见 ItemAttributeRegistry）。
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
     * 单物品需求：item 为物品注册 id（精确匹配），attribute 为元素属性 id（属性标记匹配，二选一）；
     * count 为数量。item 优先；两者均未指定时视为非法条目（永不满足）。
     */
    public record ItemRequirement(Optional<String> item, Optional<String> attribute, int count) {
        public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("item").forGetter(ItemRequirement::item),
                Codec.STRING.optionalFieldOf("attribute").forGetter(ItemRequirement::attribute),
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
     * 检查物品清单是否满足：逐条需求独立判定总量（item 精确匹配 / attribute 属性标记匹配）。
     */
    public boolean matches(List<ItemStack> stacks) {
        for (ItemRequirement requirement : items) {
            long count;
            if (requirement.item().isPresent()) {
                Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(requirement.item().get()));
                if (item == null) {
                    continue;
                }
                count = stacks.stream()
                        .filter(stack -> stack.getItem() == item)
                        .mapToLong(ItemStack::getCount)
                        .sum();
            } else if (requirement.attribute().isPresent()) {
                ElementAttribute attribute = ElementRegistry.byId(requirement.attribute().get());
                if (attribute == null) {
                    return false;
                }
                count = stacks.stream()
                        .filter(stack -> ItemAttributeRegistry.of(stack).contains(attribute))
                        .mapToLong(ItemStack::getCount)
                        .sum();
            } else {
                return false;
            }
            if (count < requirement.count()) {
                return false;
            }
        }
        return true;
    }
}