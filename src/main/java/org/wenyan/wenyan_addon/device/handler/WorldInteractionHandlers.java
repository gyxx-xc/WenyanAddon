package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;


public class WorldInteractionHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> WORLD_INTERACTION_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("对指定位置使用骨粉催生植物")
            .handler(ChineseUtils.bracketOf("催生"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BoneMealItem.applyBonemeal(ItemStack.EMPTY, ctx.level(), BlockHandlerHelper.offsetPos(bp, args), null);
            }))
            .description("在指定位置上方点燃火焰")
            .handler(ChineseUtils.bracketOf("点燃"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos above = BlockHandlerHelper.offsetPos(bp, args).above();
                if (ctx.level().getBlockState(above).isAir()) {
                    ctx.level().setBlock(above, Blocks.FIRE.defaultBlockState(), 3);
                }
            }))
            .description("扑灭指定位置的火焰并清除附近实体的着火状态")
            .handler(ChineseUtils.bracketOf("熄灭"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                if (ctx.level().getBlockState(pos).is(Blocks.FIRE)) {
                    ctx.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
                for (Entity entity : ctx.level().getEntities(null, new AABB(pos).inflate(3.0))) {
                    entity.clearFire();
                }
            }))
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_WORLD_INTERACTION_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("对指定位置使用骨粉催生植物")
            .handler(ChineseUtils.bracketOf("催生"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BoneMealItem.applyBonemeal(ItemStack.EMPTY, entity.level(), BlockHandlerHelper.offsetPos(entity.blockPosition(), args), null);
                }
                return WenyanNull.NULL;
            })
            .description("在指定位置上方点燃火焰")
            .handler(ChineseUtils.bracketOf("点燃"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BlockPos above = BlockHandlerHelper.offsetPos(entity.blockPosition(), args).above();
                    if (entity.level().getBlockState(above).isAir()) {
                        entity.level().setBlock(above, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
                return WenyanNull.NULL;
            })
            .description("扑灭指定位置的火焰并清除附近实体的着火状态")
            .handler(ChineseUtils.bracketOf("熄灭"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BlockPos pos = BlockHandlerHelper.offsetPos(entity.blockPosition(), args);
                    if (entity.level().getBlockState(pos).is(Blocks.FIRE)) {
                        entity.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                    for (Entity e : entity.level().getEntities(null, new AABB(pos).inflate(3.0))) {
                        e.clearFire();
                    }
                }
                return WenyanNull.NULL;
            })
            .build();

}
