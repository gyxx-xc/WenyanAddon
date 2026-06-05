package org.wenyan.wenyan_addon.device;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
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

enum BlockHandlerHelper {
    ;

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

    public static double argDouble(IArgsRequest args, int index) throws WenyanException.WenyanTypeException {
        return args.args().get(index).as(WenyanDouble.TYPE).value();
    }

    public static int argInt(IArgsRequest args, int index) throws WenyanException.WenyanTypeException {
        return (int) args.args().get(index).as(WenyanDouble.TYPE).value();
    }

    public static String argString(IArgsRequest args, int index) throws WenyanException.WenyanTypeException {
        return args.args().get(index).as(WenyanString.TYPE).value();
    }

    public static int clampInt(IArgsRequest args, int index, int min, int max) throws WenyanException.WenyanTypeException {
        return (int) Math.clamp(argDouble(args, index), min, max);
    }

    public static double clampDouble(IArgsRequest args, int index, double min, double max) throws WenyanException.WenyanTypeException {
        return Math.clamp(argDouble(args, index), min, max);
    }

    public static BlockPos blockPos(IArgsRequest args, int startIndex, BlockPos base) throws WenyanException.WenyanTypeException {
        double x = argDouble(args, startIndex) + base.getX();
        double y = argDouble(args, startIndex + 1) + base.getY();
        double z = argDouble(args, startIndex + 2) + base.getZ();
        return new BlockPos((int) x, (int) y, (int) z);
    }

    public static Vec3 directionVec(IArgsRequest args, int startIndex) throws WenyanException.WenyanTypeException {
        return new Vec3(argDouble(args, startIndex), argDouble(args, startIndex + 1), argDouble(args, startIndex + 2));
    }

    /** AABB for entity search using precise floating-point coordinates (relative to base). */
    public static AABB searchAABB(IArgsRequest args, int startIndex, BlockPos base) throws WenyanException.WenyanTypeException {
        double x = argDouble(args, startIndex) + base.getX();
        double y = argDouble(args, startIndex + 1) + base.getY();
        double z = argDouble(args, startIndex + 2) + base.getZ();
        return new AABB(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5);
    }

    /** Teleport target using precise floating-point coordinates (relative to base). */
    public static Vec3 targetPos(IArgsRequest args, int startIndex, BlockPos base) throws WenyanException.WenyanTypeException {
        return new Vec3(
                argDouble(args, startIndex) + base.getX(),
                argDouble(args, startIndex + 1) + base.getY(),
                argDouble(args, startIndex + 2) + base.getZ()
        );
    }

    static int absorbItems(Level level, StorageRuneBlockEntity storage, BlockPos pos, double radius) {
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
