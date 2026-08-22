package org.wenyan.wenyan_addon.spell.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.wenyan.wenyan_addon.WenyanAddon;

/**
 * 法术网络注册：客户端→服务端 payload 统一注册。
 */
@EventBusSubscriber(modid = WenyanAddon.MODID)
public final class SpellNetworks {
    private SpellNetworks() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(WenyanAddon.MODID).versioned("1");
        registrar.playToServer(
                FuluPouchSwitchPayload.TYPE,
                FuluPouchSwitchPayload.STREAM_CODEC,
                FuluPouchSwitchPayload::handle);
        registrar.playToServer(
                FuluPouchClearPayload.TYPE,
                FuluPouchClearPayload.STREAM_CODEC,
                FuluPouchClearPayload::handle);
    }
}