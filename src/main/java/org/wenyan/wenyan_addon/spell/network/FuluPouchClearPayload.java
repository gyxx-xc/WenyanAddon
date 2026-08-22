package org.wenyan.wenyan_addon.spell.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.spell.FuluPouchItem;

/**
 * 客户端→服务端：shift+左键清空符咒包选中（恢复自身材质）。
 */
public record FuluPouchClearPayload() implements CustomPacketPayload {
    public static final Type<FuluPouchClearPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(WenyanAddon.MODID, "fulu_pouch_clear"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FuluPouchClearPayload> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> {
            }, buf -> new FuluPouchClearPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FuluPouchClearPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                FuluPouchItem.clearSelection(player);
            }
        });
    }
}