package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanEntity;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;


public class NamingHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> NAMING_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("为指定实体命名")
            .handler(ChineseUtils.bracketOf("命名"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = request.args();
                WenyanEntity target=args.get(0).as(WenyanEntity.TYPE);
                String name = args.get(1).as(WenyanString.TYPE).value();
                target.value().setCustomName(Component.literal(name));
            }))
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_NAMING_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("为指定实体命名")
            .handler(ChineseUtils.bracketOf("命名"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    WenyanEntity target=args.get(0).as(WenyanEntity.TYPE);
                    String name = args.get(1).as(WenyanString.TYPE).value();
                    target.value().setCustomName(Component.literal(name));
                }
                return WenyanNull.NULL;
            })
            .build();
}
