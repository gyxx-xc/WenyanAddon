package org.pongdev.pong.entity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.resources.Identifier;
import org.pongdev.pong.Pong;

public class PlugRender extends ArrowRenderer<PlugEntity, ArrowRenderState> {
    public static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Pong.MODID, "textures/entity/plug.png");

    public PlugRender(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    protected Identifier getTextureLocation(ArrowRenderState state) {
        return TEXTURE;
    }
}
