package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.spell.MatchGrade;
import org.wenyan.wenyan_addon.qi.spell.QiArgsMatch;
import org.wenyan.wenyan_addon.qi.spell.QiBranch;
import org.wenyan.wenyan_addon.qi.spell.QiFunction;
import org.wenyan.wenyan_addon.qi.spell.QiSpellContext;
import org.wenyan.wenyan_addon.qi.spell.QiSpellRegistry;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 流体设备：方块设备为符咒式（标签匹配 + 灵气消耗），物品设备保持原有行为。
 */
public class FluidHandlers {
    public static final Function<ItemStack, RawHandlerPackage> ITEM_FLUID_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("在指定位置放置水源方块")
            .handler(ChineseUtils.bracketOf("水源"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    entity.level().setBlock(BlockHandlerHelper.offsetPos(entity.blockPosition(), args), Blocks.WATER.defaultBlockState(), 3);
                }
                return WenyanNull.NULL;
            })
            .description("在指定位置放置熔岩方块")
            .handler(ChineseUtils.bracketOf("熔岩"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    entity.level().setBlock(BlockHandlerHelper.offsetPos(entity.blockPosition(), args), Blocks.LAVA.defaultBlockState(), 3);
                }
                return WenyanNull.NULL;
            })
            .description("清除指定位置的流体（水或熔岩）")
            .handler(ChineseUtils.bracketOf("清除流体"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BlockPos pos = BlockHandlerHelper.offsetPos(entity.blockPosition(), args);
                    if (!entity.level().getBlockState(pos).getFluidState().isEmpty()) {
                        entity.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
                return WenyanNull.NULL;
            })
            .description("将指定位置的水冻结成冰")
            .handler(ChineseUtils.bracketOf("冻水成冰"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BlockPos pos = BlockHandlerHelper.offsetPos(entity.blockPosition(), args);
                    if (entity.level().getBlockState(pos).is(Blocks.WATER)) {
                        entity.level().setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
                    }
                }
                return WenyanNull.NULL;
            })
            .build();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> FLUID_PACKAGE =
            QiSpellRegistry.blockPackage(FluidHandlers.class);

    @QiFunction(name = "水源", description = "在指定位置放置水源方块", primary = {"water"}, baseCost = 10)
    @QiArgsMatch(name = "水源", value = {WenyanVec3.class})
    public static IWenyanValue summonWater(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context) throws WenyanException {
        var args = request.args();
        Vec3 vec = args.get(0).as(WenyanVec3.TYPE).value();
        BlockPos pos = BlockPos.containing(vec);
        if (ctx.level().getBlockState(pos).isAir()) {
            ctx.level().setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        }
        // 自定义消耗示例：相生最佳增益时半价（未登记则按注解 baseCost × 系数默认扣除）
        if (context.match().grade() == MatchGrade.BEST) {
            context.require(ElementType.WATER, context.match().costMultiplier() * 10 * 0.5);
        }
        return WenyanNull.NULL;
    }

    @QiFunction(name = "熔岩", primary = {"fire","earth"}, baseCost = 15)
    @QiArgsMatch(name = "熔岩", value = {WenyanVec3.class})
    public static IWenyanValue summonLava(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context) throws WenyanException{
        var args = request.args();
        Vec3 vec=args.get(0).as(WenyanVec3.TYPE).value();
        ctx.level().setBlock(BlockPos.containing(vec), Blocks.LAVA.defaultBlockState(), 3);
        return WenyanNull.NULL;
    }

    @QiFunction(name = "冻水成冰", primary = {"water"}, baseCost = 8)
    @QiArgsMatch(name = "冻水成冰", value = {WenyanVec3.class})
    public static IWenyanValue freezeWater(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context) throws WenyanException{
        var args = request.args();
        Vec3 vec=args.get(0).as(WenyanVec3.TYPE).value();
        BlockPos pos=BlockPos.containing(vec);
        if (ctx.level().getBlockState(pos).is(Blocks.WATER)) {
            ctx.level().setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
        }
        return WenyanNull.NULL;
    }

    @QiFunction(name = "清除流体", baseCost = 5)
    @QiArgsMatch(name = "清除流体",value = {WenyanVec3.class})
    public static IWenyanValue clearFluid(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context) throws WenyanException {
        return clearFluidAt(ctx, request, null);
    }

    @QiBranch(forPrimary = {"water"})
    public static IWenyanValue clearFluid_water(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context) throws WenyanException {
        return clearFluidAt(ctx, request, Blocks.WATER);
    }

    @QiBranch(forPrimary = {"fire"})
    public static IWenyanValue clearFluid_lava(BlockRequest.BlockContext ctx, BlockRequest request, QiSpellContext context) throws WenyanException {
        return clearFluidAt(ctx, request, Blocks.LAVA);
    }

    private static IWenyanValue clearFluidAt(BlockRequest.BlockContext ctx, BlockRequest request, net.minecraft.world.level.block.Block only) throws WenyanException{
        var args = request.args();
        if (args.isEmpty()) {
            return WenyanNull.NULL;
        }
        Vec3 vec=args.get(0).as(WenyanVec3.TYPE).value();
        BlockPos pos=BlockPos.containing(vec);
        BlockState state = ctx.level().getBlockState(pos);
        if (state.getFluidState().isEmpty()) {
            return WenyanNull.NULL;
        }
        if (only == null || state.is(only)) {
            ctx.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        return WenyanNull.NULL;
    }
}
