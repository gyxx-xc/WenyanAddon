package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;

public final class EntityStatusHandlers {
    private EntityStatusHandlers() {
    }

    public static final ArgsSpecBuilder.Step<?> expArgsSpec = WenyanArgsResolver.build().double_().dummy();
    public static final ArgsSpecBuilder.Step<?> messageArgsSpec = WenyanArgsResolver.build().string_().dummy();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> ENTITY_STATUS_PACKAGE = (bp, _) -> HandlerPackageBuilder.create().handler(ChineseUtils.bracketOf("療"), BlockHandlerHelper.wrapVoid((ctx, _) -> ctx.level().getEntitiesOfClass(Player.class, new AABB(bp).inflate(1.5))
            .stream().findFirst().ifPresent(player -> player.heal(player.getMaxHealth())))).handler(ChineseUtils.bracketOf("飽"), BlockHandlerHelper.wrapVoid((ctx, _) -> ctx.level().getEntitiesOfClass(Player.class, new AABB(bp).inflate(1.5))
            .stream().findFirst().ifPresent(player -> player.getFoodData().eat(20, 20)))).handler(ChineseUtils.bracketOf("賜經驗"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
        var args = expArgsSpec.resolve(request);
        ctx.level().getEntitiesOfClass(Player.class, new AABB(bp).inflate(1.5))
                .stream().findFirst().ifPresent(player -> player.giveExperienceLevels(args.get(0)));
    })).handler(ChineseUtils.bracketOf("告"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
        var args = messageArgsSpec.resolve(request);
        ctx.level().getEntitiesOfClass(Player.class, new AABB(bp).inflate(1.5))
                .stream().findFirst().ifPresent(player -> player.sendSystemMessage(Component.literal(args.get(0))));
    })).build();
}
