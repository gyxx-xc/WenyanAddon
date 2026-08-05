package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class MessageHandlers {

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> NOTE_BLOCK_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("向指定范围内玩家发送消息")
            .handler(ChineseUtils.bracketOf("告"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = request.args();
                String message = args.get(0).as(WenyanString.TYPE).value();
                AABB area = new AABB(bp).inflate(BlockHandlerHelper.SAY_RANGE);
                for (Player player : ctx.level().getEntitiesOfClass(Player.class, area)) {
                    player.sendSystemMessage(Component.literal(message));
                }
            }))
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_NOTE_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("向指定范围内玩家发送消息")
            .handler(ChineseUtils.bracketOf("告"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = argsRequest.args();
                    String message = args.get(0).as(WenyanString.TYPE).value();
                    AABB area = entity.getBoundingBox().inflate(BlockHandlerHelper.SAY_RANGE);
                    for (Player player : entity.level().getEntitiesOfClass(Player.class, area)) {
                        player.sendSystemMessage(Component.literal(message));
                    }
                }
                return WenyanNull.NULL;
            })
            .build();
}
