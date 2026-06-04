package org.pongdev.pong.setup;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.pongdev.pong.entity.PlugRender;
import org.pongdev.pong.fluid.ChampagneFluidType;
import org.pongdev.pong.particle.SplashParticles;
import org.wenyan.wenyan_addon.WenyanAddon;

@EventBusSubscriber(modid = WenyanAddon.MODID, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void propertyOverrideRegistry(FMLClientSetupEvent event) {
        // Item predicate registration was replaced by the 26.1 item model system.
    }

    @SubscribeEvent
    public static void registerRender(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PongRegistration.PLUG_ENTITY.get(), PlugRender::new);
    }

    @SubscribeEvent
    public static void registerParticleProvider(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(PongRegistration.SPLASH_PARTICLES.get(), SplashParticles.Provider::new);
    }

    @SubscribeEvent
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        Material still = new Material(ChampagneFluidType.WATER_STILL_RL);
        Material flowing = new Material(ChampagneFluidType.WATER_FLOWING_RL);
        FluidModel.Unbaked model = new FluidModel.Unbaked(still, flowing, null, null);
        event.register(model, PongRegistration.SOURCE_CHAMPAGNE, PongRegistration.FLOWING_CHAMPAGNE);
    }
}
