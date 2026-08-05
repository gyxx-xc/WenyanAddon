package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.utils.WenyanValues;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;

public class TimeHandlers {

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> TIME_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("返回系统时间戳（毫秒）")
            .handler(ChineseUtils.bracketOf("时间戳"), BlockHandlerHelper.wrap((ctx, request) -> {
                return WenyanValues.of(System.currentTimeMillis());
            }))
            .description("返回当前服务器游戏时间（tick）")
            .handler(ChineseUtils.bracketOf("游戏刻"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (ctx.level() instanceof ServerLevel serverLevel) {
                    return WenyanValues.of(serverLevel.getGameTime());
                }
                return WenyanValues.of(0);
            }))
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_TIME_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("返回系统时间戳（毫秒）")
            .handler(ChineseUtils.bracketOf("时间戳"), (ctx, request) -> {
                return WenyanValues.of(System.currentTimeMillis());
            })
            .description("返回当前服务器游戏时间（tick）")
            .handler(ChineseUtils.bracketOf("游戏刻"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    if (entity.level() instanceof ServerLevel serverLevel) {
                        serverLevel.getOverworldClockTime();
                        return WenyanValues.of(serverLevel.getGameTime());
                    }
                }
                return WenyanValues.of(0);
            })
            .build();
}