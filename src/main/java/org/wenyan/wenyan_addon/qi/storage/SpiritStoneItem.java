package org.wenyan.wenyan_addon.qi.storage;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.wenyan.wenyan_addon.item.TooltipItem;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

/**
 * 灵石：强化生物（携带灵气属性标记）掉落的一次性灵气容器。
 * 按纯度分为三种（杂质/纯质/精纯），tooltip 显示剩余量与最大储量；
 * 掉落时名称带来源属性；文言函数抽取灵力时优先级最低。
 */
public class SpiritStoneItem extends TooltipItem implements QiContainerProvider {

    /**
     * 品级。
     */
    public enum Grade {
        IMPURE("杂质灵石"),
        PURE("纯质灵石"),
        REFINED("精纯灵石");

        private final String displayName;

        Grade(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    private final Grade grade;

    public SpiritStoneItem(Properties properties, String tooltipKey, Grade grade) {
        super(properties, tooltipKey);
        this.grade = grade;
    }

    public Grade grade() {
        return grade;
    }

    /**
     * 按纯度选择品级。
     */
    public static Grade gradeOf(double purity) {
        if (purity < 0.5) {
            return Grade.IMPURE;
        }
        if (purity < 0.9) {
            return Grade.PURE;
        }
        return Grade.REFINED;
    }

    /**
     * 灵石名称：&lt;属性&gt;之&lt;品级&gt;（如 火之纯质灵石）。
     */
    public static void applyName(ItemStack stack, ElementAttribute attribute, Grade grade) {
        String name = attribute != null
                ? attribute.displayName() + "之" + grade.displayName()
                : grade.displayName();
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                TooltipDisplay display,
                                java.util.function.Consumer<Component> builder,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        double remaining = new SpiritStoneContainer(stack).get(
                org.wenyan.wenyan_addon.qi.element.ElementType.NEUTRAL);
        double max = SpiritStoneContainer.maxAmount(stack);
        builder.accept(Component.literal(
                String.format(java.util.Locale.ROOT, "灵气: %.1f / %.1f", remaining, max))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public QiContainer containerOf(ItemStack stack) {
        return new SpiritStoneContainer(stack);
    }
}
