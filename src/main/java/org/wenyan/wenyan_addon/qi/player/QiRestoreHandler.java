package org.wenyan.wenyan_addon.qi.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.environment.EnvironmentQi;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = WenyanAddon.MODID)
public final class QiRestoreHandler {
    private static final int SCAN_INTERVAL = 20;
    private static final Map<UUID, ElementType> DOMINANT_CACHE = new HashMap<>();
    private static long tickCounter = 0;

    private QiRestoreHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        tickCounter++;
        boolean rescan = tickCounter % SCAN_INTERVAL == 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (rescan) {
                DOMINANT_CACHE.put(player.getUUID(),
                        EnvironmentQi.dominantElement(player.level(), player.blockPosition()));
            }
            ElementType dominant = DOMINANT_CACHE.get(player.getUUID());
            PlayerQiData qi = PlayerQi.of(player);
            if (dominant == null) {
                qi.restoreNatural(PlayerQiData.NATURAL_RESTORE_PER_SECOND / 20.0);
            } else {
                qi.restoreEnvironment(dominant, PlayerQiData.NATURAL_RESTORE_PER_SECOND / 20.0);
            }
            PlayerQi.markDirty(player);
        }
    }
}
