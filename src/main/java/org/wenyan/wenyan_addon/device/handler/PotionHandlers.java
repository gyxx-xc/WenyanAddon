package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanEntity;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.primitive.WenyanBoolean;
import indi.wenyan.judou.api.values.primitive.WenyanInteger;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;


public class PotionHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> POTION_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("为附近的生物或玩家添加指定药水效果")
            .handler(ChineseUtils.bracketOf("賜效"), BlockHandlerHelper.wrap((ctx, request) -> {
                Level level = ctx.level();
                var args = request.args();
                String effectId = args.get(0).as(WenyanString.TYPE).value();
                int duration = args.get(1).as(WenyanInteger.TYPE).value();
                int amplifier = args.get(2).as(WenyanInteger.TYPE).value();
                LivingEntity target = (LivingEntity) args.get(3).as(WenyanEntity.TYPE).value();
                var effectOpt = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effectId));
                if (effectOpt.isEmpty()) {
                    return WenyanBoolean.FALSE;
                }
                if (target == null) {
                    return WenyanBoolean.FALSE;
                }
                target.addEffect(new MobEffectInstance(effectOpt.get(), duration, amplifier));
                return WenyanBoolean.TRUE;
            }))
            .description("驱除附近生物或玩家身上的指定药水效果")
            .handler(ChineseUtils.bracketOf("驅效"), BlockHandlerHelper.wrap((ctx, request) -> {
                Level level = ctx.level();
                var args = request.args();
                String effectId = args.get(0).as(WenyanString.TYPE).value();
                LivingEntity target = (LivingEntity) args.get(1).as(WenyanEntity.TYPE).value();
                var effectOpt = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effectId));
                if (effectOpt.isEmpty()) {
                    return WenyanBoolean.FALSE;
                }
                if (target == null) {
                    return WenyanBoolean.FALSE;
                }
                target.removeEffect(effectOpt.get());
                return WenyanBoolean.TRUE;
            }))
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_POTION_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("为附近的生物或玩家添加指定药水效果")
            .handler(ChineseUtils.bracketOf("賜效"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    Level level = entity.level();
                    var args = request.args();
                    String effectId = args.get(0).as(WenyanString.TYPE).value();
                    int duration = args.get(1).as(WenyanInteger.TYPE).value();
                    int amplifier = args.get(2).as(WenyanInteger.TYPE).value();
                    LivingEntity target = (LivingEntity) args.get(3).as(WenyanEntity.TYPE).value();
                    var effectOpt = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effectId));
                    if (effectOpt.isEmpty()) {
                        return WenyanBoolean.FALSE;
                    }

                    if (target == null) {
                        return WenyanBoolean.FALSE;
                    }

                    target.addEffect(new MobEffectInstance(effectOpt.get(), duration, amplifier));
                    return WenyanBoolean.TRUE;
                }
                return WenyanBoolean.FALSE;
            })
            .description("驱除附近生物或玩家身上的指定药水效果")
            .handler(ChineseUtils.bracketOf("驅效"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    Level level = entity.level();
                    var args = request.args();
                    String effectId = args.get(0).as(WenyanString.TYPE).value();
                    LivingEntity target = (LivingEntity) args.get(1).as(WenyanEntity.TYPE).value();
                    var effectOpt = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effectId));
                    if (effectOpt.isEmpty()) {
                        return WenyanBoolean.FALSE;
                    }
                    if (target == null) {
                        return WenyanBoolean.FALSE;
                    }
                    target.removeEffect(effectOpt.get());
                    return WenyanBoolean.TRUE;
                }
                return WenyanBoolean.FALSE;
            })
            .build();
}

