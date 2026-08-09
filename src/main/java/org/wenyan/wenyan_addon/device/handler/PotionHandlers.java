package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanEntity;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.primitive.WenyanBoolean;
import indi.wenyan.judou.api.values.primitive.WenyanInteger;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.value.WenyanPotionType;

import java.util.function.BiFunction;
import java.util.function.Function;


public class PotionHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> POTION_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("获取实体身上的药水效果类型列表")
            .handler(ChineseUtils.bracketOf("取效"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = request.args();
                WenyanList result = new WenyanList();
                if (args.isEmpty()) {
                    return result;
                }
                Entity entity = args.get(0).as(WenyanEntity.TYPE).value();
                if (entity instanceof LivingEntity living) {
                    for (MobEffectInstance instance : living.getActiveEffects()) {
                        result.add(new WenyanPotionType(instance));
                    }
                }
                return result;
            }))
            .description("为指定实体添加指定药水效果")
            .handler(ChineseUtils.bracketOf("给予效果"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = request.args();
                if (args.size() < 4) {
                    return WenyanBoolean.FALSE;
                }
                LivingEntity target = args.get(0).as(WenyanEntity.TYPE).value() instanceof LivingEntity living ? living : null;
                WenyanPotionType potionType = args.get(1).as(WenyanPotionType.TYPE);
                int amplifier = args.get(2).as(WenyanInteger.TYPE).value();
                int duration = args.get(3).as(WenyanInteger.TYPE).value();
                if (target == null) {
                    return WenyanBoolean.FALSE;
                }
                target.addEffect(new MobEffectInstance(potionType.value().getEffect(), duration, amplifier));
                return WenyanBoolean.TRUE;
            }))
            .description("驱除指定实体身上的指定药水效果")
            .handler(ChineseUtils.bracketOf("祛除效果"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = request.args();
                if (args.size() < 2) {
                    return WenyanBoolean.FALSE;
                }
                LivingEntity target = args.get(0).as(WenyanEntity.TYPE).value() instanceof LivingEntity living ? living : null;
                WenyanPotionType potionType = args.get(1).as(WenyanPotionType.TYPE);
                if (target == null) {
                    return WenyanBoolean.FALSE;
                }
                target.removeEffect(potionType.value().getEffect());
                return WenyanBoolean.TRUE;
            }))
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_POTION_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("获取实体身上的药水效果类型列表")
            .handler(ChineseUtils.bracketOf("取效"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    WenyanList result = new WenyanList();
                    if (request.args().isEmpty()) {
                        return result;
                    }
                    Entity target = request.args().get(0).as(WenyanEntity.TYPE).value();
                    if (target instanceof LivingEntity living) {
                        for (MobEffectInstance instance : living.getActiveEffects()) {
                            result.add(new WenyanPotionType(instance));
                        }
                    }
                    return result;
                }
                return new WenyanList();
            })
            .description("为指定实体添加指定药水效果")
            .handler(ChineseUtils.bracketOf("给予效果"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    if (args.size() < 4) {
                        return WenyanBoolean.FALSE;
                    }
                    LivingEntity target = args.get(0).as(WenyanEntity.TYPE).value() instanceof LivingEntity living ? living : null;
                    WenyanPotionType potionType = args.get(1).as(WenyanPotionType.TYPE);
                    int amplifier = args.get(2).as(WenyanInteger.TYPE).value();
                    int duration = args.get(3).as(WenyanInteger.TYPE).value();
                    if (target == null) {
                        return WenyanBoolean.FALSE;
                    }
                    target.addEffect(new MobEffectInstance(potionType.value().getEffect(), duration, amplifier));
                    return WenyanBoolean.TRUE;
                }
                return WenyanBoolean.FALSE;
            })
            .description("驱除指定实体身上的指定药水效果")
            .handler(ChineseUtils.bracketOf("祛除效果"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    if (args.size() < 2) {
                        return WenyanBoolean.FALSE;
                    }
                    LivingEntity target = args.get(0).as(WenyanEntity.TYPE).value() instanceof LivingEntity living ? living : null;
                    WenyanPotionType potionType = args.get(1).as(WenyanPotionType.TYPE);
                    if (target == null) {
                        return WenyanBoolean.FALSE;
                    }
                    target.removeEffect(potionType.value().getEffect());
                    return WenyanBoolean.TRUE;
                }
                return WenyanBoolean.FALSE;
            })
            .build();
}
