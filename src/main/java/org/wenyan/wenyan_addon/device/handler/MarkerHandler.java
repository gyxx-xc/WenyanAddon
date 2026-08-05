package org.wenyan.wenyan_addon.device.handler;

import dev.anvilcraft.ping.network.payload.PositionPingPayload;
import dev.anvilcraft.ping.util.PingType;
import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author qq240
 * @version 1.0
 * @className MarkeHandler
 * @Description TODO
 * @date 2026/8/5 14:05
 */
public class MarkerHandler {
    public static final ArgsSpecBuilder.Step<?> markerArgsSpec = WenyanArgsResolver.build()
            .string_().double_().double_().double_().dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> MARKER_PACKAGE = (_, _) -> HandlerPackageBuilder.create()
            .description("在世界上标记一个普通坐标点")
            .handler(ChineseUtils.bracketOf("标点"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    BlockPos bp = blockContext.pos();
                    PacketDistributor.sendToPlayersNear(sl, null, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5,
                            BlockHandlerHelper.SAY_RANGE, new PositionPingPayload(Component.literal(args.get(0)),
                                    new Vec3(args.get(1), args.get(2), args.get(3)), PingType.GENERIC));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个警坐标点")
            .handler(ChineseUtils.bracketOf("警"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    BlockPos bp = blockContext.pos();
                    PacketDistributor.sendToPlayersNear(sl, null, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5,
                            BlockHandlerHelper.SAY_RANGE, new PositionPingPayload(Component.literal(args.get(0)),
                                    new Vec3(args.get(1), args.get(2), args.get(3)), PingType.WARNING));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个前往坐标点")
            .handler(ChineseUtils.bracketOf("往"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    BlockPos bp = blockContext.pos();
                    PacketDistributor.sendToPlayersNear(sl, null, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5,
                            BlockHandlerHelper.SAY_RANGE, new PositionPingPayload(Component.literal(args.get(0)),
                                    new Vec3(args.get(1), args.get(2), args.get(3)), PingType.GOTO));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个敌坐标点")
            .handler(ChineseUtils.bracketOf("敌"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    BlockPos bp = blockContext.pos();
                    PacketDistributor.sendToPlayersNear(sl, null, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5,
                            BlockHandlerHelper.SAY_RANGE, new PositionPingPayload(Component.literal(args.get(0)),
                                    new Vec3(args.get(1), args.get(2), args.get(3)), PingType.ENEMY));
                }
                return WenyanNull.NULL;
            })
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_MARKER_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("在世界上标记一个普通坐标点")
            .handler(ChineseUtils.bracketOf("标点"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity) && entity.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    BlockPos bp = entity.blockPosition();
                    PacketDistributor.sendToPlayersNear(sl, null, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5,
                            BlockHandlerHelper.SAY_RANGE, new PositionPingPayload(Component.literal(args.get(0)),
                                    new Vec3(args.get(1), args.get(2), args.get(3)), PingType.GENERIC));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个警坐标点")
            .handler(ChineseUtils.bracketOf("警"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity) && entity.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    BlockPos bp = entity.blockPosition();
                    PacketDistributor.sendToPlayersNear(sl, null, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5,
                            BlockHandlerHelper.SAY_RANGE, new PositionPingPayload(Component.literal(args.get(0)),
                                    new Vec3(args.get(1), args.get(2), args.get(3)), PingType.WARNING));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个前往坐标点")
            .handler(ChineseUtils.bracketOf("往"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity) && entity.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    BlockPos bp = entity.blockPosition();
                    PacketDistributor.sendToPlayersNear(sl, null, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5,
                            BlockHandlerHelper.SAY_RANGE, new PositionPingPayload(Component.literal(args.get(0)),
                                    new Vec3(args.get(1), args.get(2), args.get(3)), PingType.GOTO));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个敌坐标点")
            .handler(ChineseUtils.bracketOf("敌"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity) && entity.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    BlockPos bp = entity.blockPosition();
                    PacketDistributor.sendToPlayersNear(sl, null, bp.getX() + 0.5, bp.getY() + 0.5, bp.getZ() + 0.5,
                            BlockHandlerHelper.SAY_RANGE, new PositionPingPayload(Component.literal(args.get(0)),
                                    new Vec3(args.get(1), args.get(2), args.get(3)), PingType.ENEMY));
                }
                return WenyanNull.NULL;
            })
            .build();
}
