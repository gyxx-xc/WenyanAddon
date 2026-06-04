package org.pongdev.pong.setup;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.pongdev.pong.Pong;
import org.pongdev.pong.block.ChampagneBottleBlock;
import org.pongdev.pong.block.ChampagneRack;
import org.pongdev.pong.block.RackEntity;
import org.pongdev.pong.entity.PlugEntity;
import org.pongdev.pong.fluid.ChampagneFluidType;
import org.pongdev.pong.item.*;
import org.pongdev.pong.mobeffect.Drunk;
import org.pongdev.pong.particle.SplashParticles;

public class PongRegistration {
    public static void register(IEventBus modBus){
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
        MOB_EFFECTS.register(modBus);
        FLUIDS.register(modBus);
        FLUID_TYPES.register(modBus);
        PARTICLE_TYPES.register(modBus);
        ENTITY_TYPES.register(modBus);
    }

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Pong.MODID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Pong.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Pong.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Pong.MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Pong.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Pong.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Pong.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Pong.MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Pong.MODID);


    public static final DeferredItem<Item> CHAMPAGNE = ITEMS.registerItem(ChampagneBottle.ID, ChampagneBottle::new);
    public static final DeferredItem<Item> CHAMPAGNE_SABRE = ITEMS.registerItem(ChampagneSabre.ID, ChampagneSabre::new);
    public static final DeferredItem<Item> GOBLET = ITEMS.registerItem(Goblet.ID, Goblet::new);
    public static final DeferredItem<Item> PLUG = ITEMS.registerItem(PlugItem.ID, PlugItem::new);
    public static final DeferredItem<Item> DEBUG_ROD = ITEMS.registerItem(DebugRod.ID, DebugRod::new);

    public static final DeferredHolder<EntityType<?>, EntityType<PlugEntity>> PLUG_ENTITY = ENTITY_TYPES.register(PlugItem.ID,
            () -> EntityType.Builder.of(PlugEntity::new, MobCategory.MISC)
                    .sized(0.125f, 0.0625f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Pong.MODID, PlugItem.ID))));

    public static final DeferredBlock<Block> CHAMPAGNE_BOTTLE_BLOCK = BLOCKS.registerBlock(ChampagneBottle.ID, ChampagneBottleBlock::new);
    public static final DeferredBlock<Block> CHAMPAGNE_RACK_BLOCK = BLOCKS.registerBlock(ChampagneRack.ID, ChampagneRack::new);
    public static final DeferredItem<BlockItem> CHAMPAGNE_RACK_ITEM = ITEMS.registerSimpleBlockItem(CHAMPAGNE_RACK_BLOCK);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RackEntity>> CHAMPAGNE_RACK_ENTITY = BLOCK_ENTITIES.register(
            ChampagneRack.ID,
            () -> new BlockEntityType<>(RackEntity::new, CHAMPAGNE_RACK_BLOCK.get())
    );

    public static final DeferredHolder<MobEffect, MobEffect> DRUNK = MOB_EFFECTS.register("drunk", Drunk::new);

    public static final DeferredHolder<Fluid, FlowingFluid> SOURCE_CHAMPAGNE = FLUIDS.register("champagne_fluid",
            () -> new BaseFlowingFluid.Source(PongRegistration.CHAMPAGNE_FLUID_PROPERTIES));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CHAMPAGNE = FLUIDS.register("champagne_water",
            () -> new BaseFlowingFluid.Flowing(PongRegistration.CHAMPAGNE_FLUID_PROPERTIES));
    public static final DeferredBlock<LiquidBlock> CHAMPAGNE_FLUID_BLOCK = BLOCKS.registerBlock("champagne_fluid_block",
            properties -> new LiquidBlock(SOURCE_CHAMPAGNE.get(), properties));
    public static final BaseFlowingFluid.Properties CHAMPAGNE_FLUID_PROPERTIES = new BaseFlowingFluid.Properties(
            ChampagneFluidType.CHAMPAGNE_FLUID_TYPE, SOURCE_CHAMPAGNE, FLOWING_CHAMPAGNE)
            .slopeFindDistance(2).levelDecreasePerBlock(2).block(CHAMPAGNE_FLUID_BLOCK).bucket(null);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SPLASH_PARTICLES =
            PARTICLE_TYPES.register(SplashParticles.ID, () -> new SimpleParticleType(true));

    public static final String MODTAB_ID ="pong_tab";
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MODTAB = CREATIVE_MODE_TABS.register(MODTAB_ID,
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MODTAB_ID))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> CHAMPAGNE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(CHAMPAGNE.get());
                        output.accept(CHAMPAGNE_SABRE.get());
                        output.accept(GOBLET.get());
                        output.accept(CHAMPAGNE_RACK_ITEM.get());
                        output.accept(PLUG.get());
                    }).build() );
}
