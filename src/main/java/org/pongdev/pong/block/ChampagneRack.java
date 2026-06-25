package org.pongdev.pong.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;
import org.pongdev.pong.setup.PongRegistration;

public class ChampagneRack extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<ChampagneRack> CODEC = simpleCodec(ChampagneRack::new);
    public static final String ID = "champagne_rack";
    public static final String CONTAIN = "contain_number";
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final VoxelShape SHAPE_EMPTY_ZP = Block.box(0, 0, 10, 16, 16, 13);
    public static final VoxelShape SHAPE_EMPTY_ZN = Block.box(0, 0, 3, 16, 16, 6);
    public static final VoxelShape SHAPE_EMPTY_XP = Block.box(10, 0, 0, 13, 16, 16);
    public static final VoxelShape SHAPE_EMPTY_XN = Block.box(3, 0, 0, 6, 16, 16);
    public static final VoxelShape SHAPE_BOTTLE = Block.box(0, 0, 0, 16, 16, 16);

    public ChampagneRack(BlockBehaviour.Properties properties) {
        super(properties
                .strength(1.0F)
                .sound(SoundType.WOOD)
                .noOcclusion()
        );
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, POWERED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(FACING, pContext.getHorizontalDirection())
                .setValue(POWERED, Boolean.valueOf(false));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation direction) {
        return state.setValue(FACING, direction.rotate(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        BlockEntity block = pLevel.getBlockEntity(pPos);
        if (block == null) return super.getShape(pState, pLevel, pPos, pContext);
        if (block instanceof RackEntity rack && rack.getChampagneCount() != 0)
            return SHAPE_BOTTLE;
        else
            switch (pState.getValue(FACING)) {
                case NORTH -> {
                    return SHAPE_EMPTY_ZN;
                }
                case SOUTH -> {
                    return SHAPE_EMPTY_ZP;
                }
                case WEST -> {
                    return SHAPE_EMPTY_XN;
                }
                case EAST -> {
                    return SHAPE_EMPTY_XP;
                }
                default -> {
                    return super.getShape(pState, pLevel, pPos, pContext);
                }
            }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RackEntity(pPos, pState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit) {
        if (!(pLevel.getBlockEntity(pPos) instanceof RackEntity rack)) {
            return InteractionResult.FAIL;
        }
        int contain = rack.getChampagneCount();
        if (contain <= 0) return InteractionResult.FAIL;
        if (!pLevel.isClientSide()) {
            ItemStack newItemStack = new ItemStack(PongRegistration.CHAMPAGNE.get());
            if (!pPlayer.getInventory().add(newItemStack)) {
                pPlayer.drop(newItemStack, false);
            }
            rack.setChampagneCount(contain - 1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
        BlockEntity block = pLevel.getBlockEntity(pHit.getBlockPos());
        if (block instanceof RackEntity rack) {
            if (rack.getChampagneCount() > 0)
                rack.explode();
        }
    }

    @Override
    protected void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pNeighborBlock, @Nullable Orientation orientation, boolean pMovedByPiston) {
        if (pLevel.hasNeighborSignal(pPos)) {
            BlockEntity block = pLevel.getBlockEntity(pPos);
            if (block instanceof RackEntity rack) {
                if (rack.getChampagneCount() > 0)
                    pLevel.blockEvent(pPos, pState.getBlock(), 0, 0);
            }
        }
        super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, orientation, pMovedByPiston);
    }

    @Override
    protected boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam) {
        BlockEntity block = pLevel.getBlockEntity(pPos);
        if (block instanceof RackEntity rack) {
            if (rack.getChampagneCount() > 0)
                rack.explode();
        }
        return true;
    }
    protected int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.getValue(POWERED) ? 15 : 0;
    }

    protected boolean isSignalSource(BlockState pState) {
        return true;
    }

}
