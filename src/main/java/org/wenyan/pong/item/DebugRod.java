package org.wenyan.pong.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.pong.block.ChampagneRack;
import org.wenyan.pong.block.RackEntity;

public class DebugRod extends Item {
    public static final String ID = "debug_rod";
    public DebugRod(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockEntity block = level.getBlockEntity(blockpos);
        if(block instanceof RackEntity) {
            ((RackEntity) block).explode();
        }
        return super.useOn(pContext);
    }
}
