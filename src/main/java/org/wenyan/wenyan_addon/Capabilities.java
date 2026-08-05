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
        registerer.registerToItem(MusicHandlers.ITEM_MUSIC_PACKAGE, ChineseUtils.bracketOf("奏"), WenyanAddon.NOTE_BLOCK_FUNCTION_BLOCK_ITEM);
        registerer.registerToItem(FluidHandlers.ITEM_ELEMENTAL_PACKAGE, ChineseUtils.bracketOf("元素"), WenyanAddon.ELEMENTAL_BLOCK_ITEM);
        registerer.registerToItem(WorldHandlers.ITEM_WORLD_INTERACTION_PACKAGE, ChineseUtils.bracketOf("交感"), WenyanAddon.WORLD_INTERACTION_BLOCK_ITEM);
        registerer.registerToItem(WorldHandlers.ITEM_READ_WRITE_PACKAGE, ChineseUtils.bracketOf("讀寫"), WenyanAddon.READ_WRITE_BLOCK_ITEM);
        registerer.registerToItem(WorldHandlers.ITEM_PARTICLE_PACKAGE, ChineseUtils.bracketOf("塵"), WenyanAddon.PARTICLE_BLOCK_ITEM);
        registerer.registerToItem(WorldHandlers.ITEM_BLOCK_EDIT_PACKAGE, ChineseUtils.bracketOf("地"), WenyanAddon.BLOCK_EDIT_BLOCK_ITEM);
        registerer.registerToItem(WorldHandlers.ITEM_ENCHANT_PACKAGE, ChineseUtils.bracketOf("靈"), WenyanAddon.ENCHANT_BLOCK_ITEM);

        registerer.registerToItem(PotionHandlers.ITEM_POTION_PACKAGE, ChineseUtils.bracketOf("藥"), WenyanAddon.POTION_BLOCK_ITEM);
        registerer.registerToItem(MarkerHandler.ITEM_MARKER_PACKAGE, ChineseUtils.bracketOf("标"), WenyanAddon.MARKER_BLOCK_ITEM);
        registerer.registerToItem(ProjectileHandlers.ITEM_PROJECTILE_SPAWNER_PACKAGE, ChineseUtils.bracketOf("投射"), WenyanAddon.PROJECTILE_SPAWNER_BLOCK_ITEM);
        registerer.registerToItem(NamingHandlers.ITEM_NAMING_PACKAGE, ChineseUtils.bracketOf("命名"), WenyanAddon.NAMING_BLOCK_ITEM);

        registerer.registerToItem(MessageHandlers.ITEM_NOTE_PACKAGE, ChineseUtils.bracketOf("消息"),WenyanAddon.MESSAGE_BLOCK_ITEM);

        registerer.registerToBlock(MarkerHandler.MARKER_PACKAGE, ChineseUtils.bracketOf("标"), WenyanAddon.MARKER_BLOCK.get());
        registerer.registerToBlock(ProjectileHandlers.PROJECTILE_SPAWNER_PACKAGE, ChineseUtils.bracketOf("投射"), WenyanAddon.PROJECTILE_SPAWNER_BLOCK.get());
        registerer.registerToBlock(FluidHandlers.ELEMENTAL_PACKAGE, ChineseUtils.bracketOf("元素"), WenyanAddon.ELEMENTAL_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.WORLD_INTERACTION_PACKAGE, ChineseUtils.bracketOf("交感"), WenyanAddon.WORLD_INTERACTION_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.READ_WRITE_PACKAGE, ChineseUtils.bracketOf("讀寫"), WenyanAddon.READ_WRITE_BLOCK.get());
        registerer.registerToBlock(NamingHandlers.NAMING_PACKAGE, ChineseUtils.bracketOf("命名"), WenyanAddon.NAMING_BLOCK.get());
        registerer.registerToBlock(EntityManipulationHandlers.ENTITY_MANIPULATION_PACKAGE, ChineseUtils.bracketOf("移形"), WenyanAddon.ENTITY_MANIPULATION_BLOCK.get());
        registerer.registerToBlock(MusicHandlers.MUSIC_BLOCK_PACKAGE, ChineseUtils.bracketOf("奏"), WenyanAddon.NOTE_BLOCK_FUNCTION_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.PARTICLE_PACKAGE, ChineseUtils.bracketOf("塵"), WenyanAddon.PARTICLE_BLOCK.get());
        registerer.registerToBlock(DataDiskHandlers.STORAGE_RUNE_PACKAGE, ChineseUtils.bracketOf("納"), WenyanAddon.STORAGE_RUNE_BLOCK.get());

        registerer.registerToBlock(DyeHandlers.DYE_PACKAGE, ChineseUtils.bracketOf("染"), WenyanAddon.DYE_BLOCK.get());

        registerer.registerToBlock(PotionHandlers.POTION_PACKAGE, ChineseUtils.bracketOf("藥"), WenyanAddon.POTION_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.BLOCK_EDIT_PACKAGE, ChineseUtils.bracketOf("地"), WenyanAddon.BLOCK_EDIT_BLOCK.get());
        registerer.registerToBlock(WorldHandlers.ENCHANT_PACKAGE, ChineseUtils.bracketOf("靈"), WenyanAddon.ENCHANT_BLOCK.get());

        registerer.registerToBlock(MessageHandlers.NOTE_BLOCK_PACKAGE, ChineseUtils.bracketOf("消息"), WenyanAddon.MESSAGE_BLOCK.get());

    }
}
