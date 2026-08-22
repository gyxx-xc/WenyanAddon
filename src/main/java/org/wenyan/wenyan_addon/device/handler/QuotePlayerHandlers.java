package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanPlayer;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.utils.WenyanValues;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.mixin_util.BlockContextCasterAccessor;
import org.wenyan.wenyan_addon.qi.spell.PlayerCastContext;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * QuotePlayer 处理器
 */
public class QuotePlayerHandlers {

    // === 方块版 ===
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> QUOTEPLAYER_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("返回玩家自我实体")
            .handler(ChineseUtils.bracketOf("引我"), BlockHandlerHelper.wrap((ctx, request) -> {
                // TODO: 实现逻辑
                Player caster = ((BlockContextCasterAccessor) (Object) ctx).getCaster();
                return new WenyanPlayer(caster);
            }))
            .build();

    // === 投掷版 ===
    public static final Function<ItemStack, RawHandlerPackage> ITEM_QUOTEPLAYER_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("返回玩家自我实体")
            .handler(ChineseUtils.bracketOf("引我"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    // TODO: 实现逻辑
                    Player caster = entity.getPlayer();
                    return new WenyanPlayer(caster);
                }
                return WenyanValues.of(false);
            })
            .build();

    public static final Function<ItemStack, RawHandlerPackage> PLAYER_QUOTEPLAYER_PACKAGE= _ -> HandlerPackageBuilder.create()
            .description("返回玩家自我实体")
            .handler(ChineseUtils.bracketOf("引我"), (ctx, request) -> {
                if (ctx instanceof PlayerCastContext playerCastContext) {
                    // TODO: 实现逻辑
                    return new WenyanPlayer(playerCastContext.player());
                }
                return WenyanValues.of(false);
            })
            .build();
}