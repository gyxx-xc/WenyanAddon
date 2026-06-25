package org.pongdev.pong.setup.datagen;

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
        event.getGenerator()
                .getVanillaPack(true)
                .addProvider(output -> new PongRecipeProvider.Runner(output, registries));
    }
}
