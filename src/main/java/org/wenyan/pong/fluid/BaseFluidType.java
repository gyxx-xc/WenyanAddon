package org.wenyan.pong.fluid;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.function.Consumer;


// copy from https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.19/blob/main/src/main/java/net/kaupenjoe/tutorialmod/fluid/BaseFluidType.java
// It is MIT
public class BaseFluidType extends FluidType {
    /**
     * Default constructor.
     *
     * @param properties the general properties of the fluid type
     */

    private final Identifier stillTexture;
    private final Identifier flowingTexture;
    private final Identifier overlayTexture;
    private final int tintColor;
    private final Vector3f fogColor;

    public BaseFluidType(final Identifier stillTexture, final Identifier flowingTexture, final Identifier overlayTexture,
                         final int tintColor, final Vector3f fogColor, final Properties properties) {
        super(properties);
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.overlayTexture = overlayTexture;
        this.tintColor = tintColor;
        this.fogColor = fogColor;
    }

    public Identifier getStillTexture() {
        return stillTexture;
    }

    public Identifier getFlowingTexture() {
        return flowingTexture;
    }

    public int getTintColor() {
        return tintColor;
    }

    public Identifier getOverlayTexture() {
        return overlayTexture;
    }

    public Vector3f getFogColor() {
        return fogColor;
    }

    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            public @Nullable Identifier getRenderOverlayTexture(net.minecraft.client.Minecraft mc) {
                return overlayTexture;
            }

            public int getTintColor() {
                return tintColor;
            }

            @Override
            public void modifyFogColor(Camera camera, float partialTick, ClientLevel level,
                                       int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
                fluidFogColor.set(fogColor.x, fogColor.y, fogColor.z, fluidFogColor.w);
            }

            @Override
            public void modifyFogRender(Camera camera, @Nullable FogEnvironment environment, float renderDistance,
                                        float partialTick, FogData fogData) {
                fogData.environmentalStart = 5f;
                fogData.environmentalEnd = 10f;
            }
        });
    }
}
