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
import org.wenyan.wenyan_addon.device.handler.EntityHandlers;
import org.wenyan.wenyan_addon.device.handler.ManipulationHandlers;
import org.wenyan.wenyan_addon.device.handler.WorldHandlers;
import org.wenyan.wenyan_addon.dye.Dye;

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

        registerer.registerToBlock(EntityHandlers.MARKER_PACKAGE, ChineseUtils.bracketOf("标"), WenyanAddon.MARKER_BLOCK.get());
        registerer.registerToBlock(EntityHandlers.PROJECTILE_SPAWNER_PACKAGE, ChineseUtils.bracketOf("投射"), WenyanAddon.PROJECTILE_SPAWNER_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.ELEMENTAL_PACKAGE, ChineseUtils.bracketOf("元素"), WenyanAddon.ELEMENTAL_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.WORLD_INTERACTION_PACKAGE, ChineseUtils.bracketOf("交感"), WenyanAddon.WORLD_INTERACTION_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.READ_WRITE_PACKAGE, ChineseUtils.bracketOf("讀寫"), WenyanAddon.READ_WRITE_BLOCK.get());
        registerer.registerToBlock(EntityHandlers.NAMING_PACKAGE, ChineseUtils.bracketOf("命名"), WenyanAddon.NAMING_BLOCK.get());
        registerer.registerToBlock(ManipulationHandlers.ENTITY_MANIPULATION_PACKAGE, ChineseUtils.bracketOf("移形"), WenyanAddon.ENTITY_MANIPULATION_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.NOTE_BLOCK_PACKAGE, ChineseUtils.bracketOf("奏"), WenyanAddon.NOTE_BLOCK_FUNCTION_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.PARTICLE_PACKAGE, ChineseUtils.bracketOf("塵"), WenyanAddon.PARTICLE_BLOCK.get());
        registerer.registerToBlock(EntityHandlers.STORAGE_RUNE_PACKAGE, ChineseUtils.bracketOf("納"), WenyanAddon.STORAGE_RUNE_BLOCK.get());

        registerer.registerToBlock(Dye.DYE_PACKAGE, ChineseUtils.bracketOf("染"), WenyanAddon.DYE_BLOCK.get());

        registerer.registerToBlock(EntityHandlers.ENTITY_STATUS_PACKAGE, ChineseUtils.bracketOf("愈"), WenyanAddon.ENTITY_STATUS_BLOCK.get());
        registerer.registerToBlock(EntityHandlers.SPAWN_PACKAGE, ChineseUtils.bracketOf("召"), WenyanAddon.ENTITY_SPAWN_BLOCK.get());
        registerer.registerToBlock(EntityHandlers.POTION_PACKAGE, ChineseUtils.bracketOf("藥"), WenyanAddon.POTION_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.BLOCK_EDIT_PACKAGE, ChineseUtils.bracketOf("地"), WenyanAddon.BLOCK_EDIT_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.ENCHANT_PACKAGE, ChineseUtils.bracketOf("靈"), WenyanAddon.ENCHANT_BLOCK.get());
    }
}
