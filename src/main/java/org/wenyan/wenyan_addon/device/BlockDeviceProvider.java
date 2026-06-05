package org.wenyan.wenyan_addon.device;

import indi.wenyan.interpreter_impl.IWenyanBlockDevice;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;

public final class BlockDeviceProvider {
    private BlockDeviceProvider() {
    }

    public static IBlockCapabilityProvider<IWenyanBlockDevice, Void>
    create(String name, RawHandlerPackage handlerPackage) {
        return (_, pos, state, _, _) ->
                new SimpleDevice(state, pos, ChineseUtils.bracketOf(name), handlerPackage);
    }

    private record SimpleDevice(BlockState blockState, BlockPos blockPos, String packageName,
                                RawHandlerPackage execPackage) implements IWenyanBlockDevice {
        @Override
        public BlockState blockState() {
            return blockState;
        }

        @Override
        public BlockPos blockPos() {
            return blockPos;
        }

        @Override
        public boolean isRemoved() {
            return false;
        }

        @Override
        public RawHandlerPackage getExecPackage() {
            return execPackage;
        }

        @Override
        public String getPackageName() {
            return packageName;
        }
    }
}
