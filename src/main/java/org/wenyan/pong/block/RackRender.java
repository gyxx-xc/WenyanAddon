package org.wenyan.pong.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.wenyan.pong.setup.PongRegistration;

public class RackRender implements BlockEntityRenderer<RackEntity, RackRenderState> {
    private static final double[][] BOTTLE_POSITIONS = {
            {-0.25, -0.25},
            {-0.25, 0.25},
            {0.25, -0.25},
            {0.25, 0.25}
    };

    private final BlockModelResolver blockModelResolver;

    public RackRender(BlockEntityRendererProvider.Context context) {
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public RackRenderState createRenderState() {
        return new RackRenderState();
    }

    @Override
    public void extractRenderState(
            RackEntity blockEntity,
            RackRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = blockEntity.getBlockState().getValue(ChampagneRack.FACING);
        state.bottleCount = Math.clamp(blockEntity.getChampagneCount(), 0, BOTTLE_POSITIONS.length);
        blockModelResolver.update(
                state.bottleModel,
                PongRegistration.CHAMPAGNE_BOTTLE_BLOCK.get().defaultBlockState(),
                BlockDisplayContext.create()
        );
    }

    @Override
    public void submit(
            RackRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);
        poseStack.mulPose(state.facing.getRotation());
        poseStack.translate(-0.5, -0.5, 0.0);

        for (int i = 0; i < state.bottleCount; i++) {
            poseStack.pushPose();
            poseStack.translate(BOTTLE_POSITIONS[i][0], 0.0, BOTTLE_POSITIONS[i][1]);
            state.bottleModel.submitMultiLayer(
                    poseStack,
                    submitNodeCollector,
                    state.lightCoords,
                    OverlayTexture.NO_OVERLAY,
                    0
            );
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
