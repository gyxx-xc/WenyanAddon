package org.wenyan.wenyan_addon.qi.gathering;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 聚灵阵核心方块：抽取区块主属性灵气，填充附近玩家灵气条。
 */
public class QiGatheringArrayBlock extends Block implements EntityBlock {
    public QiGatheringArrayBlock(Properties properties) {
        super(properties
                .mapColor(MapColor.COLOR_YELLOW)
                .strength(3.0f)
                .sound(SoundType.METAL));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new QiGatheringArrayBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state,
                                                                             @NonNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (entityLevel, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof QiGatheringArrayBlockEntity array) {
                array.tick((ServerLevel) entityLevel);
            }
        };
    }
}
