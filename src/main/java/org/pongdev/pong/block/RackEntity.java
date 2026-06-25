package org.pongdev.pong.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.pongdev.pong.Pong;
import org.pongdev.pong.setup.PongRegistration;

import static org.pongdev.pong.item.OpenChampagne.*;

public class RackEntity extends BlockEntity {
    public RackEntity(BlockPos pPos, BlockState pBlockState) {
        super(PongRegistration.CHAMPAGNE_RACK_ENTITY.get(), pPos, pBlockState);
    }

    public int getChampagneCount() {
        return getPersistentData().getInt(ChampagneRack.CONTAIN).orElse(0);
    }

    public void setChampagneCount(int count) {
        getPersistentData().putInt(ChampagneRack.CONTAIN, Math.clamp(count, 0, 4));
        setChanged();
        if (level instanceof ServerLevel) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    public void explode(){
        assert this.level != null;
        Pong.LOGGER.info(level+"");
        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(ChampagneRack.POWERED, true), 3);
        Vec3 lookWay = Vec3.atLowerCornerOf(getBlockState().getValue(ChampagneRack.FACING).getUnitVec3i());
        int number = getChampagneCount();
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
