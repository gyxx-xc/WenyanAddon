package org.wenyan.pong.setup.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import org.wenyan.pong.setup.PongRegistration;

import java.util.concurrent.CompletableFuture;

public class PongRecipeProvider extends RecipeProvider {
    public PongRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.FOOD, PongRegistration.CHAMPAGNE.get())
                .define('B', Items.SWEET_BERRIES)
                .define('G', Items.GLASS_PANE)
                .define('S', Items.SUGAR)
                .define('C',PongRegistration.PLUG.get())
                .pattern(" C ")
                .pattern("GSG")
                .pattern("GBG")
                .group("pong")
                .unlockedBy("has_glass_bottle", has(Items.GLASS_BOTTLE))
                .save(output);

        shaped(RecipeCategory.TOOLS, PongRegistration.CHAMPAGNE_SABRE.get())
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .pattern("  I")
                .pattern(" I ")
                .pattern("S  ")
                .group("pong")
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(output);

        shaped(RecipeCategory.DECORATIONS, PongRegistration.GOBLET.get())
                .define('G', Items.GLASS_PANE)
                .pattern("G G")
                .pattern(" G ")
                .pattern(" G ")
                .group("pong")
                .unlockedBy("has_glass_pane", has(Items.GLASS_PANE))
                .save(output);

        shaped(RecipeCategory.DECORATIONS, PongRegistration.CHAMPAGNE_RACK_ITEM.get())
                .define('P', ItemTags.PLANKS)
                .define('S', Items.STICK)
                .pattern("PPP")
                .pattern("S S")
                .pattern("PPP")
                .group("pong")
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(output);

        shaped(RecipeCategory.DECORATIONS, PongRegistration.PLUG.get())
                .define('P', ItemTags.PLANKS)
                .pattern(" P ")
                .pattern("PPP")
                .group("pong")
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .save(output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new PongRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "Pong Recipes";
        }
    }
}
