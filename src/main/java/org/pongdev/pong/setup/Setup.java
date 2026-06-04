package org.pongdev.pong.setup;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import org.pongdev.pong.Pong;
import org.wenyan.wenyan_addon.WenyanAddon;

@EventBusSubscriber(modid = WenyanAddon.MODID)
public class Setup {
    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent event) {
        Pong.LOGGER.info("HELLO FROM PONG!");
        Pong.LOGGER.info("TAKE CARE: OVER DRINK IS HARMFUL TO YOUR HEALTH"); // 过度饮酒,有害健康 (?)
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        event.getEntity().removeEffect(PongRegistration.DRUNK);
    }
}
