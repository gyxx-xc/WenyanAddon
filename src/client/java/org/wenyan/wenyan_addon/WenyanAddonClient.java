package org.wenyan.wenyan_addon;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.wenyan.wenyan_addon.device.handler.data_disk.StorageRuneScreen;
import org.wenyan.wenyan_addon.spell.FuluPouchScreen;

@EventBusSubscriber(modid = WenyanAddon.MODID, value = Dist.CLIENT)
public final class WenyanAddonClient {
    private WenyanAddonClient() {
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(WenyanAddon.STORAGE_RUNE_MENU.get(), StorageRuneScreen::new);
        event.register(WenyanAddon.FULU_POUCH_MENU.get(), FuluPouchScreen::new);
    }
}
