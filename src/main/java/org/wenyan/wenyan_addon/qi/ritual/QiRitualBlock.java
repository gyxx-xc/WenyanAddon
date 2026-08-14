package org.wenyan.wenyan_addon.qi.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 淬体仪式中心方块：右键启动仪式（8 格盛放方块满足配方时），
 * 仪式进行中每 2 秒降下闪电淬炼玩家。
 */
public class QiRitualBlock extends Block implements EntityBlock {
    public QiRitualBlock(Properties properties) {
        super(properties
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(3.0f)
                .sound(SoundType.ANVIL));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new QiRitualBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof QiRitualBlockEntity ritual
                && level instanceof ServerLevel serverLevel
                && player instanceof ServerPlayer serverPlayer) {
            if (!ritual.isRunning()) {
                if (ritual.tryStart(serverLevel, serverPlayer)) {
                    serverPlayer.sendSystemMessage(Component.literal("淬体仪式开始"));
                } else {
                    serverPlayer.sendSystemMessage(Component.literal("仪式物品不满足或结构不完整"));
                }
            } else {
                serverPlayer.sendSystemMessage(Component.literal("仪式正在进行中"));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state,
                                                                             @NonNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (entityLevel, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof QiRitualBlockEntity ritual) {
                ritual.tick((ServerLevel) entityLevel);
            }
        };
    }
}
