package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;


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
            .handler(ChineseUtils.bracketOf("除流"), (ctx, argsRequest) -> {
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
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> FLUID_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("在指定位置放置水源方块")
            .handler(ChineseUtils.bracketOf("水源"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                ctx.level().setBlock(BlockHandlerHelper.offsetPos(bp, args), Blocks.WATER.defaultBlockState(), 3);
            }))
            .description("在指定位置放置熔岩方块")
            .handler(ChineseUtils.bracketOf("熔岩"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                ctx.level().setBlock(BlockHandlerHelper.offsetPos(bp, args), Blocks.LAVA.defaultBlockState(), 3);
            }))
            .description("清除指定位置的流体（水或熔岩）")
            .handler(ChineseUtils.bracketOf("除流"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                if (!ctx.level().getBlockState(pos).getFluidState().isEmpty()) {
                    ctx.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }))
            .description("将指定位置的水冻结成冰")
            .handler(ChineseUtils.bracketOf("冻水成冰"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                if (ctx.level().getBlockState(pos).is(Blocks.WATER)) {
                    ctx.level().setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
                }
            }))
            .build();
}
