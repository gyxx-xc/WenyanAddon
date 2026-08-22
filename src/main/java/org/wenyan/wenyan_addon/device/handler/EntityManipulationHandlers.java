package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.interpreter_impl.value.WenyanEntity;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.request.IArgsRequest;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.qi.spell.PlayerCastContext;
import org.wenyan.wenyan_addon.qi.spell.QiArgsMatch;
import org.wenyan.wenyan_addon.qi.spell.QiFunction;
import org.wenyan.wenyan_addon.qi.spell.QiSpellContext;
import org.wenyan.wenyan_addon.qi.spell.QiSpellRegistry;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 移形设备：三种设备形态，全部走灵气消耗（符咒式）。
 * - 方块版：贴符于方块（BlockRequest.BlockContext）
 * - 物品版：投掷符（ThrowEntityContext）
 * - 玩家版：玩家施法（PlayerCastContext）
 * 消耗设计：初始玩家仅有无属性灵气（上限 100）。
 * 传送（相对瞬移，无距离上限）35 / 闪（视线瞬移，≤20 格）25 / 施力（动量冲击）15。
 */
public class EntityManipulationHandlers {
    // ===== 注册入口 =====
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> ENTITY_MANIPULATION_PACKAGE =
            QiSpellRegistry.blockPackage(EntityManipulationHandlers.class);

    public static final Function<ItemStack, RawHandlerPackage> ITEM_ENTITY_MANIPULATION_PACKAGE =
            QiSpellRegistry.itemPackage(EntityManipulationHandlers.class);

    public static final Function<ItemStack, RawHandlerPackage> PLAYER_ENTITY_MANIPULATION_PACKAGE =
            QiSpellRegistry.playerPackage(EntityManipulationHandlers.class);

    // ===== 方块版方法（贴符于方块） =====
    @QiFunction(name = "传送", description = "将指定实体传送至相对位置", primary = {"water"}, baseCost = 35)
    @QiArgsMatch(name = "传送", value = {WenyanEntity.class, WenyanVec3.class})
    public static IWenyanValue teleport(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context)
            throws WenyanException {
        return teleportBy(request);
    }

    @QiFunction(name = "闪", description = "将指定实体沿视线方向瞬移", primary = {"water"}, baseCost = 25)
    @QiArgsMatch(name = "闪", value = {WenyanEntity.class, WenyanDouble.class})
    public static IWenyanValue blink(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context)
            throws WenyanException {
        return blinkBy(request);
    }

    @QiFunction(name = "施力", description = "对指定实体施加动量", primary = {"fire"}, baseCost = 15)
    @QiArgsMatch(name = "施力", value = {WenyanEntity.class, WenyanVec3.class})
    public static IWenyanValue push(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context)
            throws WenyanException {
        return pushBy(request);
    }

    // ===== 物品版方法（投掷符） =====
    @QiFunction(name = "传送", primary = {"water"}, baseCost = 35)
    @QiArgsMatch(name = "传送", value = {WenyanEntity.class, WenyanVec3.class})
    public static IWenyanValue itemTeleport(ThrowEntityContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return teleportBy(request);
    }

    @QiFunction(name = "闪", primary = {"water"}, baseCost = 25)
    @QiArgsMatch(name = "闪", value = {WenyanEntity.class, WenyanDouble.class})
    public static IWenyanValue itemBlink(ThrowEntityContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return blinkBy(request);
    }

    @QiFunction(name = "施力", primary = {"fire"}, baseCost = 15)
    @QiArgsMatch(name = "施力", value = {WenyanEntity.class, WenyanVec3.class})
    public static IWenyanValue itemPush(ThrowEntityContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return pushBy(request);
    }

    // ===== 玩家版方法（玩家施法） =====
    @QiFunction(name = "传送", primary = {"water"}, baseCost = 35)
    @QiArgsMatch(name = "传送", value = {WenyanEntity.class, WenyanVec3.class})
    public static IWenyanValue castTeleport(PlayerCastContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return teleportBy(request);
    }

    @QiFunction(name = "闪", primary = {"water"}, baseCost = 25)
    @QiArgsMatch(name = "闪", value = {WenyanEntity.class, WenyanDouble.class})
    public static IWenyanValue castBlink(PlayerCastContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return blinkBy(request);
    }

    @QiFunction(name = "施力", primary = {"fire"}, baseCost = 15)
    @QiArgsMatch(name = "施力", value = {WenyanEntity.class, WenyanVec3.class})
    public static IWenyanValue castPush(PlayerCastContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return pushBy(request);
    }

    // ===== 公共逻辑（只依赖请求参数，三版共用） =====

    private static IWenyanValue teleportBy(IArgsRequest request) throws WenyanException.WenyanTypeException {
        var args = request.args();
        Entity objective = args.get(0).as(WenyanEntity.TYPE).value();
        Vec3 delta = args.get(1).as(WenyanVec3.TYPE).value();
        objective.teleportTo(objective.getX() + delta.x, objective.getY() + delta.y, objective.getZ() + delta.z);
        return WenyanNull.NULL;
    }

    private static IWenyanValue blinkBy(IArgsRequest request) throws WenyanException {
        var args = request.args();
        Entity objective = args.get(0).as(WenyanEntity.TYPE).value();
        double distance = args.get(1).as(WenyanDouble.TYPE).value();
        if (distance > 20) {
            throw new WenyanException.WenyanDataException("施法距离过远");
        }
        Vec3 lookAngle = objective.getLookAngle().scale(distance);
        objective.teleportTo(objective.getX() + lookAngle.x, objective.getY() + lookAngle.y, objective.getZ() + lookAngle.z);
        return WenyanNull.NULL;
    }

    private static IWenyanValue pushBy(IArgsRequest request) throws WenyanException.WenyanTypeException {
        var args = request.args();
        Entity objective = args.get(0).as(WenyanEntity.TYPE).value();
        Vec3 force = args.get(1).as(WenyanVec3.TYPE).value();
        objective.addDeltaMovement(force);
        if (objective instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }
        return WenyanNull.NULL;
    }
}