package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.List;
import java.util.function.BiFunction;

public final class PotionHandlers {
    private PotionHandlers() {
    }

    public static final ArgsSpecBuilder.Step<?> GRANT_EFFECT_ARGS_SPEC = WenyanArgsResolver.build()
            .string_().double_().double_().dummy();
    public static final ArgsSpecBuilder.Step<?> REMOVE_EFFECT_ARGS_SPEC = WenyanArgsResolver.build()
            .string_().dummy();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> POTION_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("賜效"), BlockHandlerHelper.wrap((ctx, request) -> {
                Level level = ctx.level();
                var args = GRANT_EFFECT_ARGS_SPEC.resolve(request);
                String effectId = args.get(0);
                int duration = (int) ((double) args.get(1));
                int amplifier = (int) ((double) args.get(2));
                var effectOpt = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effectId));
                if (effectOpt.isEmpty()) {
                    return new WenyanDouble(0);
                }
                LivingEntity target = findNearestLivingEntity(level, bp);
                if (target == null) {
                    return new WenyanDouble(0);
                }
                target.addEffect(new MobEffectInstance(effectOpt.get(), duration, amplifier));
                return new WenyanDouble(1);
            }))
            .handler(ChineseUtils.bracketOf("驅效"), BlockHandlerHelper.wrap((ctx, request) -> {
                Level level = ctx.level();
                var args = REMOVE_EFFECT_ARGS_SPEC.resolve(request);
                String effectId = args.get(0);
                var effectOpt = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effectId));
                if (effectOpt.isEmpty()) {
                    return new WenyanDouble(0);
                }
                LivingEntity target = findNearestLivingEntity(level, bp);
                if (target == null) {
                    return new WenyanDouble(0);
                }
                target.removeEffect(effectOpt.get());
                return new WenyanDouble(1);
            }))
            .build();

    private static LivingEntity findNearestLivingEntity(Level level, BlockPos bp) {
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(bp).inflate(1.5));
        LivingEntity target = null;
        for (LivingEntity entity : entities) {
            if (entity instanceof Player) {
                return entity;
            }
            if (target == null || entity.distanceToSqr(bp.getCenter()) < target.distanceToSqr(bp.getCenter())) {
                target = entity;
            }
        }
        return target;
    }
}
