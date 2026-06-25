package org.wenyan.pong.setup.datagen;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.wenyan.wenyan_addon.WenyanAddon;

@EventBusSubscriber(modid = WenyanAddon.MODID)
public final class PongDataGeneration {
    private PongDataGeneration() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        var registries = event.getLookupProvider();
        var output = event.getGenerator().getPackOutput();
        event.getGenerator()
                .getVanillaPack(true)
                .addProvider(packOutput -> new PongRecipeProvider.Runner(packOutput, registries));
        event.addProvider(new PongLanguageProvider(output, "zh_cn"));
        event.addProvider(new PongLanguageProvider(output, "en_us"));
    }
}
