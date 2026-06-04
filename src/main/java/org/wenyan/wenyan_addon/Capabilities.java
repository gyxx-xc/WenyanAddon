package org.wenyan.wenyan_addon;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.IWenyanBlockDevice;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.setup.definitions.WyRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jspecify.annotations.NonNull;

import static org.wenyan.wenyan_addon.WenyanAddon.MODID;

@EventBusSubscriber(modid = MODID)
public enum Capabilities {
    ;

    @SubscribeEvent
    public static void registerCapabilities(@NonNull RegisterCapabilitiesEvent event) {
        event.registerBlock(
                WyRegistration.WENYAN_BLOCK_DEVICE_CAPABILITY,
                Capabilities::exampleDevice,
                WenyanAddon.EXAMPLE_BLOCK.get(),
                Blocks.BEDROCK
        );
    }

    static IWenyanBlockDevice exampleDevice(Level level, BlockPos pos, BlockState state, BlockEntity entity, Void ignore) {
        return new IWenyanBlockDevice() {
            @Override
            public BlockState blockState() {
                return state;
            }

            @Override
            public BlockPos blockPos() {
                return pos;
            }

            @Override
            public boolean isRemoved() {
                return false;
            }

            @Override
            public RawHandlerPackage getExecPackage() {
                return HandlerPackageBuilder.create()
                        .handler(ChineseUtils.bracketOf("crush"),
                                _ -> {
                                    throw new NullPointerException();
                                })
                        .build();
            }

            @Override
            public String getPackageName() {
                return ChineseUtils.bracketOf("crush game");
            }
        };
    }
}
