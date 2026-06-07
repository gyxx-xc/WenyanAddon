package org.wenyan.wenyan_addon;

import dev.anvilcraft.ping.network.payload.PositionPingPayload;
import dev.anvilcraft.ping.util.PingType;
import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.IWenyanBlockDevice;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.setup.capabilities.DeviceCapabilityRegisterer;
import indi.wenyan.setup.definitions.WyRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;
import org.wenyan.wenyan_addon.device.BlockDeviceProvider;
import org.wenyan.wenyan_addon.device.BlockHandlers;
import org.wenyan.wenyan_addon.dye.Dye;

public final class Capabilities {
    private Capabilities() {}

    public static void registerCapabilities(@NonNull RegisterCapabilitiesEvent event) {
        /// EXAMPLE_BLOCK is example code for humans to reference. Never edit, modify, or change it.
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
                                .handler("「碎」",
                                        _ -> {
                                            throw new NullPointerException();
                                        })
                                // .handler... other
                                .build();
                    }

                    @Override
                    public String getPackageName() {
                        return "「碎」";
                    }
                },
                WenyanAddon.EXAMPLE_BLOCK.get(),
                Blocks.BEDROCK
        );
        // EXAMPLE_BLOCK end

        DeviceCapabilityRegisterer registerer = new DeviceCapabilityRegisterer(event);

        registerer.registerToBlock((pos, state) -> HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("标点"), (ctx, request) -> {
                            if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                                var args = WenyanArgsResolver.build()
                                        .string_()
                                        .double_().range(-1000, 1000)
                                        .double_().range(-1000, 1000)
                                        .double_().range(-1000, 1000)
                                        .resolve(request);
                                PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                                        new Vec3(args.get(1), args.get(2), args.get(3)), PingType.GENERIC));
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("警"), (ctx, request) -> {
                            if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                                var args = WenyanArgsResolver.build()
                                        .string_().nonEmpty()
                                        .double_().range(-1000, 1000)
                                        .double_().range(-1000, 1000)
                                        .double_().range(-1000, 1000)
                                        .resolve(request);
                                PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                                        new Vec3(args.get(1), args.get(2), args.get(3)), PingType.WARNING));
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("往"), (ctx, request) -> {
                            if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                                var args = WenyanArgsResolver.build()
                                        .string_()
                                        .double_().range(-1000, 1000)
                                        .double_().range(-1000, 1000)
                                        .double_().range(-1000, 1000)
                                        .resolve(request);
                                PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                                        new Vec3(args.get(1), args.get(2), args.get(3)), PingType.GOTO));
                            }
                            return WenyanNull.NULL;
                        })
                        .handler(ChineseUtils.bracketOf("敌"), (ctx, request) -> {
                            if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                                var args = WenyanArgsResolver.build()
                                        .string_()
                                        .double_().range(-1000, 1000)
                                        .double_().range(-1000, 1000)
                                        .double_().range(-1000, 1000)
                                        .resolve(request);
                                PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                                        new Vec3(args.get(1), args.get(2), args.get(3)), PingType.ENEMY));
                            }
                            return WenyanNull.NULL;
                        })
                        .build(),
                ChineseUtils.bracketOf("标"), WenyanAddon.MARKER_BLOCK.get());

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                BlockDeviceProvider.create("投射", BlockHandlers.PROJECTILE_SPAWNER_PACKAGE),
                WenyanAddon.PROJECTILE_SPAWNER_BLOCK.get(),
                Blocks.DISPENSER
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                BlockDeviceProvider.create("元素", BlockHandlers.ELEMENTAL_PACKAGE),
                WenyanAddon.ELEMENTAL_BLOCK.get(),
                Blocks.CAULDRON
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                BlockDeviceProvider.create("交感", BlockHandlers.WORLD_INTERACTION_PACKAGE),
                WenyanAddon.WORLD_INTERACTION_BLOCK.get(),
                Blocks.COMPOSTER
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                BlockDeviceProvider.create("讀寫", BlockHandlers.READ_WRITE_PACKAGE),
                WenyanAddon.READ_WRITE_BLOCK.get(),
                Blocks.OAK_SIGN
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                BlockDeviceProvider.create("命名", BlockHandlers.NAMING_PACKAGE),
                WenyanAddon.NAMING_BLOCK.get(),
                Blocks.ANVIL
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                BlockDeviceProvider.create("移形", BlockHandlers.ENTITY_MANIPULATION_PACKAGE),
                WenyanAddon.ENTITY_MANIPULATION_BLOCK.get(),
                Blocks.BEACON
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                BlockDeviceProvider.create("奏", BlockHandlers.NOTE_BLOCK_PACKAGE),
                WenyanAddon.NOTE_BLOCK_FUNCTION_BLOCK.get(),
                Blocks.NOTE_BLOCK
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                BlockDeviceProvider.create("塵", BlockHandlers.PARTICLE_PACKAGE),
                WenyanAddon.PARTICLE_BLOCK.get(),
                Blocks.END_ROD
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                Dye.dyeDevice(),
                WenyanAddon.DYE_BLOCK.get()
        );

        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                BlockDeviceProvider.create("納", BlockHandlers.STORAGE_RUNE_PACKAGE),
                WenyanAddon.STORAGE_RUNE_BLOCK.get()
        );
    }
}
