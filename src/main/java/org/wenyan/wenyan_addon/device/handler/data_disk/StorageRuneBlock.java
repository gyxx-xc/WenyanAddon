package org.wenyan.wenyan_addon.device.handler.data_disk;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.wenyan.wenyan_addon.WenyanAddon;

public class StorageRuneBlock extends Block implements EntityBlock {
    public StorageRuneBlock(Properties properties) {
        super(properties
                .mapColor(MapColor.COLOR_GREEN)
                .strength(2.0f)
                .sound(SoundType.STONE));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new StorageRuneBlockEntity(pos, state);
    }

    // ===== 交互：打开磁盘 GUI =====

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return openGui(level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return openGui(level, pos, player);
    }

    private InteractionResult openGui(Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof StorageRuneBlockEntity storage)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, _) -> new StorageRuneMenu(
                            containerId,
                            inventory,
                            storage.getDisks(),
                            ContainerLevelAccess.create(level, pos)),
                    Component.translatable("container." + WenyanAddon.MODID + ".storage_rune")));
        }
        return InteractionResult.SUCCESS;
    }
}
