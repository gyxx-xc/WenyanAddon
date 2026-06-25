package org.wenyan.pong.fluid;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.joml.Vector3f;
import org.wenyan.pong.setup.PongRegistration;

public class ChampagneFluidType {
    public static final Identifier WATER_STILL_RL = Identifier.withDefaultNamespace("block/water_still");
    public static final Identifier WATER_FLOWING_RL = Identifier.withDefaultNamespace("block/water_flow");

    public static final DeferredHolder<FluidType, FluidType> CHAMPAGNE_FLUID_TYPE = register("champagne_fluid",
            FluidType.Properties.create()
                    .lightLevel(2)
                    .density(5)
                    .viscosity(1)
                    .canHydrate(true)
                    .sound(SoundAction.get("drink"),
                    SoundEvents.HONEY_DRINK.value()));
    private static DeferredHolder<FluidType, FluidType> register(String name, FluidType.Properties properties) {
        return PongRegistration.FLUID_TYPES.register(name, () -> new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, null,
                0xF7F7D1A0, new Vector3f(224f / 255f, 56f / 255f, 208f / 255f), properties));
    }
}
