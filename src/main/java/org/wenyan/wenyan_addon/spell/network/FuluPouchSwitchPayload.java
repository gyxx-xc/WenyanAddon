package org.wenyan.wenyan_addon.spell.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.spell.FuluPouchItem;

/**
 * 客户端→服务端：shift+滚轮切换符咒包/拓展包选中槽位。
 * delta 为滚轮方向（+1 上滚 / -1 下滚）。
 */
public record FuluPouchSwitchPayload(int delta) implements CustomPacketPayload {
    public static final Type<FuluPouchSwitchPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(WenyanAddon.MODID, "fulu_pouch_switch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FuluPouchSwitchPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, FuluPouchSwitchPayload::delta, FuluPouchSwitchPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FuluPouchSwitchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                FuluPouchItem.handleScrollSwitch(player, payload.delta());
            }
        });
    }
}