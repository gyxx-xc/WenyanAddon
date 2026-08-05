package org.wenyan.wenyan_addon.device.handler.data_disk;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
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
import org.wenyan.wenyan_addon.data_storage.DataDiskStorage;

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

    // ===== 交互：插入磁盘 =====

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!DataDiskStorage.isDataDisk(stack) || !(level.getBlockEntity(pos) instanceof StorageRuneBlockEntity storage)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack single = stack.copyWithCount(1);
            DataDiskStorage.getOrCreateDiskId(single);
            ItemStack remaining = storage.insertDisk(single);
            if (remaining.isEmpty() && !player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    // ===== 交互：潜行右键弹出磁盘 =====

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.isSecondaryUseActive() || !(level.getBlockEntity(pos) instanceof StorageRuneBlockEntity storage)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack extracted = storage.extractLastDisk();
            if (extracted.isEmpty()) {
                return InteractionResult.PASS;
            }
            popResource(level, pos.above(), extracted);
        }
        return InteractionResult.SUCCESS;
    }

    // ===== 方块破坏时弹出所有磁盘 =====

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof StorageRuneBlockEntity storage) {
            ItemStack extracted;
            while (!(extracted = storage.extractLastDisk()).isEmpty()) {
                popResource(level, pos, extracted);
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}