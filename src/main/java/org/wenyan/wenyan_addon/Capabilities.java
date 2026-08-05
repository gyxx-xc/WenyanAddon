package org.wenyan.wenyan_addon;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.IWenyanBlockDevice;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.setup.capabilities.DeviceCapabilityRegisterer;
import indi.wenyan.setup.definitions.WyRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jspecify.annotations.NonNull;
import org.wenyan.wenyan_addon.device.handler.*;
import org.wenyan.wenyan_addon.device.handler.DyeHandlers;

public final class Capabilities {
    private Capabilities() {}

    public static void registerCapabilities(@NonNull RegisterCapabilitiesEvent event) {
        /// EXAMPLE is example code for humans to reference. Never edit, modify, or change it.
        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                (_, p, s, _, _) -> new IWenyanBlockDevice() {
                    @Override
                    public BlockState blockState() {
                        return s;
                    }

                    @Override
                    public BlockPos blockPos() {
                        return p;
                    }

                    @Override
                    public boolean isRemoved() {
                        return false;
                    }

                    @Override
                    public RawHandlerPackage getExecPackage() {
                        return HandlerPackageBuilder.create()
                                .handler("「example」",
                                        _ -> {
                                            WenyanAddon.LOGGER.info("example");
                                            return WenyanNull.NULL;
                                        })
                                // .handler... other
                                .build();
                    }

                    @Override
                    public String getPackageName() {
                        return "「example」";
                    }
                },
                WenyanAddon.EXAMPLE_BLOCK.get(),
                Blocks.BEDROCK
        );
        // EXAMPLE_BLOCK end

        DeviceCapabilityRegisterer registerer = new DeviceCapabilityRegisterer(event);

        registerer.registerToItem(EntityManipulationHandlers.ITEM_ENTITY_MANIPULATION_PACKAGE, ChineseUtils.bracketOf("移形"), WenyanAddon.ENTITY_MANIPULATION_BLOCK_ITEM);
        registerer.registerToItem(MusicHandlers.ITEM_MUSIC_PACKAGE, ChineseUtils.bracketOf("奏"), WenyanAddon.MUSIC_BLOCK_ITEM);
        registerer.registerToItem(FluidHandlers.ITEM_FLUID_PACKAGE, ChineseUtils.bracketOf("流体"), WenyanAddon.FLUID_BLOCK_ITEM);
        registerer.registerToItem(WorldInteractionHandlers.ITEM_WORLD_INTERACTION_PACKAGE, ChineseUtils.bracketOf("交感"), WenyanAddon.WORLD_INTERACTION_BLOCK_ITEM);
        registerer.registerToItem(ReadWriteHandlers.ITEM_READ_WRITE_PACKAGE, ChineseUtils.bracketOf("文本读写"), WenyanAddon.READ_WRITE_BLOCK_ITEM);
        registerer.registerToItem(ParticleHandlers.ITEM_PARTICLE_PACKAGE, ChineseUtils.bracketOf("粒子"), WenyanAddon.PARTICLE_BLOCK_ITEM);
        registerer.registerToItem(BlockEditHandlers.ITEM_BLOCK_EDIT_PACKAGE, ChineseUtils.bracketOf("方块操作"), WenyanAddon.BLOCK_EDIT_BLOCK_ITEM);
        registerer.registerToItem(EnchantHandlers.ITEM_ENCHANT_PACKAGE, ChineseUtils.bracketOf("附魔"), WenyanAddon.ENCHANT_BLOCK_ITEM);

        registerer.registerToItem(PotionHandlers.ITEM_POTION_PACKAGE, ChineseUtils.bracketOf("药水"), WenyanAddon.POTION_BLOCK_ITEM);
        registerer.registerToItem(MarkerHandler.ITEM_MARKER_PACKAGE, ChineseUtils.bracketOf("标记"), WenyanAddon.MARKER_BLOCK_ITEM);
        registerer.registerToItem(ProjectileHandlers.ITEM_PROJECTILE_SPAWNER_PACKAGE, ChineseUtils.bracketOf("投射"), WenyanAddon.PROJECTILE_SPAWNER_BLOCK_ITEM);
        registerer.registerToItem(NamingHandlers.ITEM_NAMING_PACKAGE, ChineseUtils.bracketOf("命名"), WenyanAddon.NAMING_BLOCK_ITEM);

        registerer.registerToItem(MessageHandlers.ITEM_NOTE_PACKAGE, ChineseUtils.bracketOf("消息"),WenyanAddon.MESSAGE_BLOCK_ITEM);

        registerer.registerToBlock(MarkerHandler.MARKER_PACKAGE, ChineseUtils.bracketOf("标记"), WenyanAddon.MARKER_BLOCK.get());
        registerer.registerToBlock(ProjectileHandlers.PROJECTILE_SPAWNER_PACKAGE, ChineseUtils.bracketOf("投射"), WenyanAddon.PROJECTILE_SPAWNER_BLOCK.get());
        registerer.registerToBlock(FluidHandlers.FLUID_PACKAGE, ChineseUtils.bracketOf("流体"), WenyanAddon.FLUID_BLOCK.get());
        registerer.registerToBlock(WorldInteractionHandlers.WORLD_INTERACTION_PACKAGE, ChineseUtils.bracketOf("交感"), WenyanAddon.WORLD_INTERACTION_BLOCK.get());
        registerer.registerToBlock(ReadWriteHandlers.READ_WRITE_PACKAGE, ChineseUtils.bracketOf("文本读写"), WenyanAddon.READ_WRITE_BLOCK.get());
        registerer.registerToBlock(NamingHandlers.NAMING_PACKAGE, ChineseUtils.bracketOf("命名"), WenyanAddon.NAMING_BLOCK.get());
        registerer.registerToBlock(EntityManipulationHandlers.ENTITY_MANIPULATION_PACKAGE, ChineseUtils.bracketOf("移形"), WenyanAddon.ENTITY_MANIPULATION_BLOCK.get());
        registerer.registerToBlock(MusicHandlers.MUSIC_BLOCK_PACKAGE, ChineseUtils.bracketOf("奏"), WenyanAddon.MUSIC_BLOCK.get());
        registerer.registerToBlock(ParticleHandlers.PARTICLE_PACKAGE, ChineseUtils.bracketOf("粒子"), WenyanAddon.PARTICLE_BLOCK.get());
        registerer.registerToBlock(DataDiskHandlers.STORAGE_RUNE_PACKAGE, ChineseUtils.bracketOf("纳"), WenyanAddon.STORAGE_RUNE_BLOCK.get());

        registerer.registerToBlock(DyeHandlers.DYE_PACKAGE, ChineseUtils.bracketOf("染"), WenyanAddon.DYE_BLOCK.get());

        registerer.registerToBlock(PotionHandlers.POTION_PACKAGE, ChineseUtils.bracketOf("药"), WenyanAddon.POTION_BLOCK.get());
        registerer.registerToBlock(BlockEditHandlers.BLOCK_EDIT_PACKAGE, ChineseUtils.bracketOf("方块操作"), WenyanAddon.BLOCK_EDIT_BLOCK.get());
        registerer.registerToBlock(EnchantHandlers.ENCHANT_PACKAGE, ChineseUtils.bracketOf("附魔"), WenyanAddon.ENCHANT_BLOCK.get());

        registerer.registerToBlock(MessageHandlers.NOTE_BLOCK_PACKAGE, ChineseUtils.bracketOf("消息"), WenyanAddon.MESSAGE_BLOCK.get());

    }
}
