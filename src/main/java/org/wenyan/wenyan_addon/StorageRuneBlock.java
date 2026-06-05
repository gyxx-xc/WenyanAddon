package org.wenyan.wenyan_addon;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.Nullable;

public class StorageRuneBlock extends Block implements EntityBlock {
    public StorageRuneBlock(Properties properties) {
        super(properties
                .mapColor(MapColor.COLOR_GREEN)
                .strength(2.0f)
                .sound(SoundType.STONE));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageRuneBlockEntity(pos, state);
    }
}
