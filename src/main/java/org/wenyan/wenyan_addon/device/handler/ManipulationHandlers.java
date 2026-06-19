package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.interpreter_impl.args.ResolvedArgs;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.List;
import java.util.function.BiFunction;

/// small context handler for llm to understand
public class ManipulationHandlers {
    public static final ArgsSpecBuilder.Step<?> entityManipulationArgsSpec = BlockHandlerHelper.singleVec3ArgsSpec.copy()
            .double_().double_().double_().dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> ENTITY_MANIPULATION_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("将指定范围的实体传送至目标位置")
            .handler(ChineseUtils.bracketOf("传送"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = entityManipulationArgsSpec.resolve(request);
                Vec3 dest = new Vec3(bp.getX() + (double) args.get(3), bp.getY() + (double) args.get(4), bp.getZ() + (double) args.get(5));
                for (Entity entity : getEntitiesInBlockRange(ctx.level(), bp, args)) {
                    entity.teleportTo(dest.x, dest.y, dest.z);
                }
            }))
            .description("将指定范围的实体沿相对方向瞬移")
            .handler(ChineseUtils.bracketOf("閃"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = entityManipulationArgsSpec.resolve(request);
                Vec3 origin = new Vec3(bp.getX() + (double) args.get(0), bp.getY() + (double) args.get(1), bp.getZ() + (double) args.get(2));
                Vec3 delta = new Vec3(args.get(3), args.get(4), args.get(5));
                for (Entity entity : getEntitiesInBlockRange(ctx.level(), bp, args)) {
                    entity.teleportTo(origin.x + delta.x, origin.y + delta.y, origin.z + delta.z);
                }
            }))
            .description("对指定范围的实体施加动量")
            .handler(ChineseUtils.bracketOf("施力"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = entityManipulationArgsSpec.resolve(request);
                Vec3 force = new Vec3(args.get(3), args.get(4), args.get(5));
                for (Entity entity : getEntitiesInBlockRange(ctx.level(), bp, args)) {
                    entity.addDeltaMovement(force);
                }
            }))
            .build();

    private static List<Entity> getEntitiesInBlockRange(Level level, BlockPos bp, ResolvedArgs args) {
        Vec3 center = new Vec3(bp.getX() + (double) args.get(0), bp.getY() + (double) args.get(1), bp.getZ() + (double) args.get(2));
        return level.getEntities(null, new AABB(center.subtract(0.5, 0.5, 0.5), center.add(0.5, 0.5, 0.5)));
    }
}
