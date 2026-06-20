package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanEntity;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;

public class ManipulationHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> ENTITY_MANIPULATION_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("将指定实体传送至相对位置")
            .handler(ChineseUtils.bracketOf("传送"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = request.args();
                Entity objective = args.get(0).as(WenyanEntity.TYPE).value();
                Vec3 delta = args.get(1).as(WenyanVec3.TYPE).value();
                objective.teleportTo(objective.getX() + delta.x, objective.getY() + delta.y, objective.getZ() + delta.z);
            }))
            .description("将指定实体沿视线方向瞬移")
            .handler(ChineseUtils.bracketOf("閃"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = request.args();
                Entity objective = args.get(0).as(WenyanEntity.TYPE).value();
                double distance = args.get(1).as(WenyanDouble.TYPE).value();
                if (distance > 20) {
                    throw new WenyanException.WenyanDataException("施法距离过远");
                }
                Vec3 lookAngle = objective.getLookAngle().scale(distance);
                objective.teleportTo(objective.getX() + lookAngle.x, objective.getY() + lookAngle.y, objective.getZ() + lookAngle.z);
            }))
            .description("对指定实体施加动量")
            .handler(ChineseUtils.bracketOf("施力"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = request.args();
                Entity objective = args.get(0).as(WenyanEntity.TYPE).value();
                Vec3 force = args.get(1).as(WenyanVec3.TYPE).value();
                objective.addDeltaMovement(force);
                if (objective instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                }
            }))
            .build();
}
