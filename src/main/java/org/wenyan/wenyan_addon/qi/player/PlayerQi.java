package org.wenyan.wenyan_addon.qi.player;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.wenyan.wenyan_addon.WenyanAddon;

public final class PlayerQi {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.ATTACHMENT_TYPES, WenyanAddon.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerQiData>> PLAYER_QI = ATTACHMENT_TYPES.register(
            "player_qi",
            () -> AttachmentType.builder(PlayerQiData::new)
                    .serialize(PlayerQiData.CODEC, _ -> true)
                    .sync(PlayerQiData.STREAM_CODEC)
                    .build()
    );

    private PlayerQi() {
    }

    public static PlayerQiData of(Player player) {
        return player.getData(PLAYER_QI.get());
    }

    public static void markDirty(Player player) {
        player.setData(PLAYER_QI.get(), player.getData(PLAYER_QI.get()));
    }
}
