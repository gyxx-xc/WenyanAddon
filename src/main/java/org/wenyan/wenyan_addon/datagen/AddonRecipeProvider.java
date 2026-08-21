package org.wenyan.wenyan_addon.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.spell.SpellImbueRecipe;

import java.util.concurrent.CompletableFuture;

public class AddonRecipeProvider extends RecipeProvider {
    public AddonRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        SpecialRecipeBuilder.special(() -> new SpellImbueRecipe(
                        tag(ItemTags.SWORDS)))
                .unlockedBy("has_sword", has(Items.IRON_SWORD))
                .save(output, WenyanAddon.MODID + ":spell_imbue");
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new AddonRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "Wenyan Addon Recipes";
        }
    }
}