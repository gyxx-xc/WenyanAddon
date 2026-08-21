package org.wenyan.wenyan_addon.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.wenyan.wenyan_addon.WenyanAddon;

/// Central handler for data generation.
/// Registers all data providers to be executed during data generation.
@EventBusSubscriber(modid = WenyanAddon.MODID)
public enum ModDataGeneratorHandler {
    ;

    /// Event handler for gathering data providers.
    /// Registers all necessary providers for mod assets and data.
    ///
    /// @param event The gather data event
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        var registries = event.getLookupProvider();
        PackOutput output = event.getGenerator().getPackOutput();
        var generator = event.getGenerator().getVanillaPack(true);
        generator.addProvider(packOutput -> new ItemTagProvider(packOutput, registries));
        generator.addProvider(packOutput -> new AddonRecipeProvider.Runner(packOutput, registries));
        event.addProvider(new AddonLanguageProvider(output, "zh_cn"));
        event.addProvider(new AddonLanguageProvider(output, "en_us"));
        event.addProvider(new QiDamageTypeProvider(output));

    }

    @SubscribeEvent
    public static void gatherDataServer(GatherDataEvent.Server event) {
    }
}
