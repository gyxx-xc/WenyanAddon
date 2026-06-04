package org.pongdev.pong.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.pongdev.pong.Pong;
import org.pongdev.pong.setup.PongRegistration;

import static org.pongdev.pong.item.OpenChampagne.*;

public class RackEntity extends BlockEntity {
    public RackEntity(BlockPos pPos, BlockState pBlockState) {
        super(PongRegistration.CHAMPAGNE_RACK_ENTITY.get(), pPos, pBlockState);
    }

    public void explode(){
        assert this.level != null;
        Pong.LOGGER.info(level+"");
        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(ChampagneRack.POWERED, true), 3);
        Vec3 lookWay = Vec3.atLowerCornerOf(getBlockState().getValue(ChampagneRack.FACING).getUnitVec3i());
        int number = getPersistentData().getInt(ChampagneRack.CONTAIN).orElse(0);
        for (int i = 0; i < number; i ++) {
            level.playLocalSound(worldPosition.getCenter().x, worldPosition.getCenter().y, worldPosition.getCenter().z,
                    SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1, 1.0F, false);
            emmitParticle(worldPosition.getCenter(), lookWay, 30, this.level);
            shootPlug(worldPosition.getCenter(), lookWay, 30, this.level);
        }

        this.level.setBlockAndUpdate(worldPosition,
                PongRegistration.SOURCE_CHAMPAGNE.get().defaultFluidState().createLegacyBlock());
        this.level.updateNeighborsAt(worldPosition.below(),
                this.level.getBlockState(worldPosition.below()).getBlock());
    }
}
