package org.wenyan.pong.block;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class RackRenderState extends BlockEntityRenderState {
    public final BlockModelRenderState bottleModel = new BlockModelRenderState();
    public Direction facing = Direction.NORTH;
    public int bottleCount;
}
