package org.wenyan.wenyan_addon.spell;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 铸灵配方：剑 + 符咒/符咒石（带代码物品）→ 剑写入咒术代码。
 * 无代码则清除剑的法术组件（计划规定）。
 */
public class SpellImbueRecipe extends CustomRecipe {
    public static final String ID = "spell_imbue";

    public static final MapCodec<SpellImbueRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("sword").forGetter(SpellImbueRecipe::sword)
    ).apply(inst, SpellImbueRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpellImbueRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, SpellImbueRecipe::sword,
                    SpellImbueRecipe::new);

    public static final RecipeSerializer<SpellImbueRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Ingredient sword;

    public SpellImbueRecipe(Ingredient sword) {
        this.sword = sword;
    }

    private Ingredient sword() {
        return sword;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        boolean hasSword = false;
        boolean hasCode = false;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (sword.test(stack)) {
                if (hasSword) {
                    return false;
                }
                hasSword = true;
            } else if (SpellCodeHelper.isCodeCarrier(stack)) {
                if (hasCode) {
                    return false;
                }
                hasCode = true;
            } else {
                return false;
            }
        }
        return hasSword && hasCode;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = ItemStack.EMPTY;
        ItemStack carrier = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (sword.test(stack)) {
                result = stack.copy();
            } else {
                carrier = stack;
            }
        }
        if (result.isEmpty()) {
            return result;
        }
        SpellCodeHelper.writeCode(result, SpellCodeHelper.readCode(carrier));
        result.set(SpellDataComponent.SPELL_STEP.get(), SpellCodeHelper.stepOf(carrier));
        result.setCount(1);
        return result;
    }

    @Override
    public RecipeSerializer<SpellImbueRecipe> getSerializer() {
        return SERIALIZER;
    }
}