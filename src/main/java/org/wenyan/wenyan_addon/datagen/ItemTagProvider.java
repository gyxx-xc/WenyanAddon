package org.wenyan.wenyan_addon.datagen;

import indi.wenyan.setup.definitions.WyRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.jspecify.annotations.NonNull;
import org.wenyan.wenyan_addon.WenyanAddon;

import java.util.concurrent.CompletableFuture;

public class ItemTagProvider extends ItemTagsProvider {

    public ItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, WenyanAddon.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider) {
        tag(WyRegistration.PAPER_MODULE_ITEM).add(
                WenyanAddon.EXAMPLE_BLOCK_ITEM.get(),
                WenyanAddon.PROJECTILE_SPAWNER_BLOCK_ITEM.get(),
                WenyanAddon.FLUID_BLOCK_ITEM.get(),
                WenyanAddon.WORLD_INTERACTION_BLOCK_ITEM.get(),
                WenyanAddon.ENTITY_MANIPULATION_BLOCK_ITEM.get(),
                WenyanAddon.NOTE_BLOCK_FUNCTION_BLOCK_ITEM.get(),
                WenyanAddon.READ_WRITE_BLOCK_ITEM.get(),
                WenyanAddon.NAMING_BLOCK_ITEM.get(),
                WenyanAddon.PARTICLE_BLOCK_ITEM.get(),
                WenyanAddon.DYE_BLOCK_ITEM.get(),
                WenyanAddon.MARKER_BLOCK_ITEM.get(),
                WenyanAddon.ENTITY_STATUS_BLOCK_ITEM.get(),
                WenyanAddon.ENTITY_SPAWN_BLOCK_ITEM.get(),
                WenyanAddon.POTION_BLOCK_ITEM.get(),
                WenyanAddon.BLOCK_EDIT_BLOCK_ITEM.get(),
                WenyanAddon.ENCHANT_BLOCK_ITEM.get(),
                WenyanAddon.STORAGE_RUNE_BLOCK_ITEM.get(),
                WenyanAddon.DATA_DISK_ITEM.get()
        );
    }
}
