package org.pongdev.pong.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChampagneBottleBlock extends FallingBlock {
    public static final MapCodec<ChampagneBottleBlock> CODEC = simpleCodec(ChampagneBottleBlock::new);
    public static final VoxelShape SHAPE =
            Shapes.join(Block.box(5.25, 0, 5.25, 10.75, 8, 10.75),
                    Block.box(7, 9, 7, 9, 16, 9), BooleanOp.OR);
    public ChampagneBottleBlock(BlockBehaviour.Properties properties) {
        super(properties.noOcclusion());
    }

    @Override
    protected MapCodec<? extends FallingBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected void falling(FallingBlockEntity pEntity) {
        pEntity.dropItem = false;
    }

    @Override
    public void onBrokenAfterFall(Level pLevel, BlockPos pPos, FallingBlockEntity pFallingBlock) {
        // TODO: play sound of braking glass
        // TODO: make the cloud
        super.onBrokenAfterFall(pLevel, pPos, pFallingBlock);
        pLevel.playLocalSound(pPos.getCenter().x, pPos.getCenter().y, pPos.getCenter().z, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1, 1, true);
    }

    @Override
    protected void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
        super.onProjectileHit(pLevel, pState, pHit, pProjectile);
        BlockPos pPos = pHit.getBlockPos();
        pLevel.playLocalSound(pPos.getCenter().x, pPos.getCenter().y, pPos.getCenter().z, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 3, 1, true);
        pLevel.removeBlock(pHit.getBlockPos(), false);
    }

    @Override
    public int getDustColor(BlockState blockState, BlockGetter level, BlockPos pos) {
        return 0xF7F7D1;
    }
}
