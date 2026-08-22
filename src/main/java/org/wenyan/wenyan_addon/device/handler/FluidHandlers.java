package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.request.IArgsRequest;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.spell.MatchGrade;
import org.wenyan.wenyan_addon.qi.spell.PlayerCastContext;
import org.wenyan.wenyan_addon.qi.spell.QiArgsMatch;
import org.wenyan.wenyan_addon.qi.spell.QiBranch;
import org.wenyan.wenyan_addon.qi.spell.QiFunction;
import org.wenyan.wenyan_addon.qi.spell.QiSpellContext;
import org.wenyan.wenyan_addon.qi.spell.QiSpellRegistry;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 流体设备：同一套函数三种设备形态。
 * - 方块版：贴符于方块（BlockRequest.BlockContext，以符所在方块为锚点）
 * - 物品版：投掷符（ThrowEntityContext，以投掷实体为锚点）
 * - 玩家版：玩家施法（PlayerCastContext，以玩家为施法主体）
 */
public class FluidHandlers {
    // ===== 注册入口 =====
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> FLUID_PACKAGE =
            QiSpellRegistry.blockPackage(FluidHandlers.class);

    public static final Function<ItemStack, RawHandlerPackage> ITEM_FLUID_PACKAGE =
            QiSpellRegistry.itemPackage(FluidHandlers.class);

    public static final Function<ItemStack, RawHandlerPackage> PLAYER_FLUID_PACKAGE =
            QiSpellRegistry.playerPackage(FluidHandlers.class);

    // ===== 方块版方法（贴符于方块） =====
    @QiFunction(name = "水源", description = "在指定位置放置水源方块", primary = {"water"}, baseCost = 10)
    @QiArgsMatch(name = "水源", value = {WenyanVec3.class})
    public static IWenyanValue summonWater(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context)
            throws WenyanException {
        return summonWaterAt(ctx.level(), request, context);
    }

    @QiFunction(name = "熔岩", primary = {"fire", "earth"}, baseCost = 15)
    @QiArgsMatch(name = "熔岩", value = {WenyanVec3.class})
    public static IWenyanValue summonLava(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context)
            throws WenyanException {
        return summonLavaAt(ctx.level(), request);
    }

    @QiFunction(name = "冻水成冰", primary = {"water"}, baseCost = 8)
    @QiArgsMatch(name = "冻水成冰", value = {WenyanVec3.class})
    public static IWenyanValue freezeWater(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context)
            throws WenyanException {
        return freezeWaterAt(ctx.level(), request);
    }

    @QiFunction(name = "清除流体", baseCost = 5)
    @QiArgsMatch(name = "清除流体", value = {WenyanVec3.class})
    public static IWenyanValue clearFluid(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context)
            throws WenyanException {
        return clearFluidAt(ctx.level(), request, null);
    }

    @QiBranch(forPrimary = {"water"})
    public static IWenyanValue clearFluid_water(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context)
            throws WenyanException {
        return clearFluidAt(ctx.level(), request, Blocks.WATER);
    }

    @QiBranch(forPrimary = {"fire"})
    public static IWenyanValue clearFluid_lava(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context)
            throws WenyanException {
        return clearFluidAt(ctx.level(), request, Blocks.LAVA);
    }

    // ===== 物品版方法（投掷符） =====
    @QiFunction(name = "水源", primary = {"water"}, baseCost = 10)
    @QiArgsMatch(name = "水源", value = {WenyanVec3.class})
    public static IWenyanValue itemSummonWater(ThrowEntityContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return summonWaterAt(entityLevel(ctx), request, context);
    }

    @QiFunction(name = "熔岩", primary = {"fire", "earth"}, baseCost = 15)
    @QiArgsMatch(name = "熔岩", value = {WenyanVec3.class})
    public static IWenyanValue itemSummonLava(ThrowEntityContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return summonLavaAt(entityLevel(ctx), request);
    }

    @QiFunction(name = "冻水成冰", primary = {"water"}, baseCost = 8)
    @QiArgsMatch(name = "冻水成冰", value = {WenyanVec3.class})
    public static IWenyanValue itemFreezeWater(ThrowEntityContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return freezeWaterAt(entityLevel(ctx), request);
    }

    @QiFunction(name = "清除流体", baseCost = 5)
    @QiArgsMatch(name = "清除流体", value = {WenyanVec3.class})
    public static IWenyanValue itemClearFluid(ThrowEntityContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return clearFluidAt(entityLevel(ctx), request, null);
    }

    @QiBranch(forPrimary = {"water"})
    public static IWenyanValue itemClearFluid_water(ThrowEntityContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return clearFluidAt(entityLevel(ctx), request, Blocks.WATER);
    }

    @QiBranch(forPrimary = {"fire"})
    public static IWenyanValue itemClearFluid_lava(ThrowEntityContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return clearFluidAt(entityLevel(ctx), request, Blocks.LAVA);
    }

    // ===== 玩家版方法（玩家施法） =====
    @QiFunction(name = "水源", primary = {"water"}, baseCost = 15)
    @QiArgsMatch(name = "水源", value = {WenyanVec3.class})
    public static IWenyanValue castSummonWater(PlayerCastContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return summonWaterAt(ctx.level(), request, context);
    }

    @QiFunction(name = "熔岩", primary = {"fire", "earth"}, baseCost = 15)
    @QiArgsMatch(name = "熔岩", value = {WenyanVec3.class})
    public static IWenyanValue castSummonLava(PlayerCastContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return summonLavaAt(ctx.level(), request);
    }

    @QiFunction(name = "冻水成冰", primary = {"water"}, baseCost = 8)
    @QiArgsMatch(name = "冻水成冰", value = {WenyanVec3.class})
    public static IWenyanValue castFreezeWater(PlayerCastContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return freezeWaterAt(ctx.level(), request);
    }

    @QiFunction(name = "清除流体", baseCost = 5)
    @QiArgsMatch(name = "清除流体", value = {WenyanVec3.class})
    public static IWenyanValue castClearFluid(PlayerCastContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return clearFluidAt(ctx.level(), request, null);
    }

    @QiBranch(forPrimary = {"water"})
    public static IWenyanValue castClearFluid_water(PlayerCastContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return clearFluidAt(ctx.level(), request, Blocks.WATER);
    }

    @QiBranch(forPrimary = {"fire"})
    public static IWenyanValue castClearFluid_lava(PlayerCastContext ctx, IArgsRequest request, QiSpellContext context)
            throws WenyanException {
        return clearFluidAt(ctx.level(), request, Blocks.LAVA);
    }

    // ===== 公共逻辑（以 Level 为中心，三版共用） =====

    private static Level entityLevel(ThrowEntityContext ctx) {
        if (!(ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity))) {
            return null;
        }
        return entity.level();
    }

    private static IWenyanValue summonWaterAt(Level level, IArgsRequest request, QiSpellContext context) throws WenyanException.WenyanTypeException {
        if (level == null) {
            return WenyanNull.NULL;
        }
        Vec3 vec = request.args().get(0).as(WenyanVec3.TYPE).value();
        BlockPos pos = BlockPos.containing(vec);
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        }
        if (context.match().grade() == MatchGrade.BEST) {
            context.require(ElementType.WATER, context.match().costMultiplier() * 10 * 0.5);
        }
        return WenyanNull.NULL;
    }

    private static IWenyanValue summonLavaAt(Level level, IArgsRequest request) throws WenyanException.WenyanTypeException {
        if (level == null) {
            return WenyanNull.NULL;
        }
        Vec3 vec = request.args().get(0).as(WenyanVec3.TYPE).value();
        level.setBlock(BlockPos.containing(vec), Blocks.LAVA.defaultBlockState(), 3);
        return WenyanNull.NULL;
    }

    private static IWenyanValue freezeWaterAt(Level level, IArgsRequest request) throws WenyanException.WenyanTypeException {
        if (level == null) {
            return WenyanNull.NULL;
        }
        Vec3 vec = request.args().get(0).as(WenyanVec3.TYPE).value();
        BlockPos pos = BlockPos.containing(vec);
        if (level.getBlockState(pos).is(Blocks.WATER)) {
            level.setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
        }
        return WenyanNull.NULL;
    }

    private static IWenyanValue clearFluidAt(Level level, IArgsRequest request, Block only) throws WenyanException.WenyanTypeException {
        if (level == null) {
            return WenyanNull.NULL;
        }
        if (request.args().isEmpty()) {
            return WenyanNull.NULL;
        }
        Vec3 vec = request.args().get(0).as(WenyanVec3.TYPE).value();
        BlockPos pos = BlockPos.containing(vec);
        BlockState state = level.getBlockState(pos);
        if (state.getFluidState().isEmpty()) {
            return WenyanNull.NULL;
        }
        if (only == null || state.is(only)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        return WenyanNull.NULL;
    }
}