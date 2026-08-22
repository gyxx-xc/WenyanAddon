package org.wenyan.wenyan_addon.spell;

import indi.wenyan.content.item.ItemCodeHolder;
import indi.wenyan.setup.definitions.RunnerTier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import org.wenyan.wenyan_addon.WenyanAddon;

/**
 * 法术代码读写工具：统一从物品栈读取/写入咒术代码。
 * 优先级：法术组件（wenyan_addon:spell_code）→ 本体 PROGRAM_CODE_DATA（ItemCodeHolder）。
 */
public final class SpellCodeHelper {
    private SpellCodeHelper() {
    }

    /**
     * 是否为剑类武器（法术剑铸灵配方可接受的目标）。
     * 26.1 剑为组件化武器（minecraft:swords 标签），另兼容 MaceItem 近战武器。
     */
    public static boolean isSwordLike(ItemStack stack) {
        return stack.is(ItemTags.SWORDS) || stack.getItem() instanceof MaceItem;
    }

    /**
     * 是否为代码载体：带法术组件或本体代码组件（手卷/符纸/符咒/符咒石等）。
     */
    public static boolean isCodeCarrier(ItemStack stack) {
        return stack.get(SpellDataComponent.SPELL_CODE.get()) != null
                || !ItemCodeHolder.getCodeCapability(stack).getCode().isBlank();
    }

    /**
     * 读取物品上携带的咒术代码；无代码返回空串。
     */
    public static String readCode(ItemStack stack) {
        String spell = stack.get(SpellDataComponent.SPELL_CODE.get());
        if (spell != null && !spell.isBlank()) {
            return spell;
        }
        String prog = ItemCodeHolder.getCodeCapability(stack).getCode();
        return prog == null ? "" : prog;
    }

    /**
     * 写入咒术代码；空代码表示清除法术。
     */
    public static void writeCode(ItemStack stack, String code) {
        if (code == null || code.isBlank()) {
            stack.remove(SpellDataComponent.SPELL_CODE.get());
        } else {
            stack.set(SpellDataComponent.SPELL_CODE.get(), code);
        }
    }

    /**
     * 清除物品上的法术代码。
     */
    public static void clearCode(ItemStack stack) {
        stack.remove(SpellDataComponent.SPELL_CODE.get());
    }

    /**
     * 摘要：取代码首行前 20 字，用于 tooltip 显示。
     */
    public static String summary(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }
        String first = code.lines().findFirst().orElse("").trim();
        return first.length() > 20 ? first.substring(0, 20) + "…" : first;
    }

    /**
     * 剑是否仍在玩家身上（背包+装备栏，含主手/副手）。
     * 用于熔断：施法期间剑被丢出/损毁（耐久耗尽被移除）即判定离手，中止运行。
     */
    public static boolean isSwordPresent(ServerPlayer player, String spellCode) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && isSwordLike(stack) && spellCode.equals(readCode(stack))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 依据符咒等级换算每 tick 授予的步数。
     * 纸类等级 1-6 对应 RunnerTier RUNNER_1..RUNNER_6 的 stepSpeed（10^n）。
     * 默认 4 步（略高于最低阶运行器）。
     */
    public static int stepOf(ItemStack stack) {
        Integer step = stack.get(SpellDataComponent.SPELL_STEP.get());
        if (step != null && step > 0) {
            return step;
        }
        Item item = stack.getItem();
        String id = item.builtInRegistryHolder().key().identifier().getPath();
        int level = switch (id) {
            case "hand_runner_0" -> 0;
            case "hand_runner_1" -> 1;
            case "hand_runner_2" -> 2;
            case "hand_runner_3" -> 3;
            case "hand_runner_4" -> 4;
            case "hand_runner_5" -> 5;
            case "hand_runner_6" -> 6;
            default -> -1;
        };
        if (level <= 0) {
            return 4;
        }
        return RunnerTier.values()[level].getStepSpeed();
    }
}