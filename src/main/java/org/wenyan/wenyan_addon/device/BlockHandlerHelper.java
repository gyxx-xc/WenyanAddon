package org.wenyan.wenyan_addon.device;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.WenyanException;
import indi.wenyan.judou.api.exec.request.IArgsRequest;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.StorageRuneBlockEntity;

public enum BlockHandlerHelper {
    ;

    public static final ArgsSpecBuilder.Step<?> singleVec3ArgsSpec = WenyanArgsResolver.build()
            .double_().double_().double_();

    @FunctionalInterface
    public interface BlockHandler {
        IWenyanValue execute(BlockRequest.BlockContext context, BlockRequest args) throws WenyanException;
    }

    @FunctionalInterface
    public interface VoidBlockHandler {
        void execute(BlockRequest.BlockContext context, BlockRequest args) throws WenyanException;
    }

    /**
     * Template method: wraps a BlockHandler (value-returning) with context casting.
     * Returns WenyanNull.NULL when the context is not a BlockContext.
     */
    public static HandlerPackageBuilder.HandlerReturnFunction wrap(BlockHandler handler) {
        return (ctx, args) ->
                ctx instanceof BlockRequest.BlockContext context &&
                        args instanceof BlockRequest blockRequest
                ? handler.execute(context, blockRequest)
                : WenyanNull.NULL;
    }

    /**
     * Template method: wraps a VoidBlockHandler (side-effect only) with context casting.
     * Auto-returns WenyanNull.NULL after execution.
     */
    public static HandlerPackageBuilder.HandlerReturnFunction wrapVoid(VoidBlockHandler handler) {
        return (ctx, args) -> {
            if (ctx instanceof BlockRequest.BlockContext context &&
                    args instanceof BlockRequest blockRequest) {
                handler.execute(context, blockRequest);
            }
            return WenyanNull.NULL;
        };
    }

    public static int absorbItems(Level level, StorageRuneBlockEntity storage, BlockPos pos, double radius) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        AABB area = new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
        int absorbed = 0;
        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, area, entity -> !entity.getItem().isEmpty())) {
            ItemStack stack = itemEntity.getItem();
            int accepted = storage.insert(stack);
            if (accepted <= 0) {
                continue;
            }
            absorbed += accepted;
            stack.shrink(accepted);
            if (stack.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(stack);
            }
        }
        return absorbed;
    }
}
