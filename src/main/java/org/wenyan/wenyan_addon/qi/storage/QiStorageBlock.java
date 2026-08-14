package org.wenyan.wenyan_addon.qi.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 灵气池方块：长时间驻扎使用的灵气容器。
 */
public class QiStorageBlock extends Block implements EntityBlock {
    public QiStorageBlock(Properties properties) {
        super(properties
                .mapColor(MapColor.COLOR_CYAN)
                .strength(2.0f)
                .sound(SoundType.STONE));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new QiStorageBlockEntity(pos, state);
    }
}
