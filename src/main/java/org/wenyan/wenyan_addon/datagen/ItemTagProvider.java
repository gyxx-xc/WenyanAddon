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
                WenyanAddon.EXAMPLE_ITEM.get()
        );
    }
}
