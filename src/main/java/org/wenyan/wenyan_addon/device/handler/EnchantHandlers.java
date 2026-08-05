package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;


public class EnchantHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> ENCHANT_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("为玩家主手物品添加指定等级的附魔")
            .handler(ChineseUtils.bracketOf("附魔"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = request.args();
                String enchantName = args.get(0).as(WenyanString.TYPE).value();
                double level = args.get(1).as(WenyanDouble.TYPE).value();
                Player player = ctx.level().getEntitiesOfClass(Player.class, new AABB(bp).inflate(1.5))
                        .stream().findFirst().orElse(null);
                if (player == null) {
                    return new WenyanDouble(0);
                }
                var stack = player.getMainHandItem();
                if (stack.isEmpty()) {
                    return new WenyanDouble(0);
                }
                var holder = ctx.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .get(Identifier.parse(enchantName));
                if (holder.isEmpty()) {
                    return new WenyanDouble(0);
                }
                stack.enchant(holder.get(), (int) level);
                return new WenyanDouble(1);
            }))
            .description("移除玩家主手物品上的指定附魔")
            .handler(ChineseUtils.bracketOf("祛魔"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = request.args();
                String enchantName = args.get(0).as(WenyanString.TYPE).value();
                Player player = ctx.level().getEntitiesOfClass(Player.class, new AABB(bp).inflate(1.5))
                        .stream().findFirst().orElse(null);
                if (player == null) {
                    return new WenyanDouble(0);
                }
                var stack = player.getMainHandItem();
                if (stack.isEmpty()) {
                    return new WenyanDouble(0);
                }
                var holder = ctx.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                        .get(Identifier.parse(enchantName));
                if (holder.isEmpty()) {
                    return new WenyanDouble(0);
                }
                ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
                if (enchantments != null) {
                    var mutable = new ItemEnchantments.Mutable(enchantments);
                    mutable.removeIf(h -> h.getKey() == holder.get().getKey());
                    stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
                }
                return new WenyanDouble(1);
            }))
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_ENCHANT_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("为玩家主手物品添加指定等级的附魔")
            .handler(ChineseUtils.bracketOf("附魔"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    String enchantName = args.get(0).as(WenyanString.TYPE).value();
                    double level = args.get(1).as(WenyanDouble.TYPE).value();
                    Player player = entity.level().getEntitiesOfClass(Player.class, new AABB(entity.blockPosition()).inflate(1.5))
                            .stream().findFirst().orElse(null);
                    if (player == null) {
                        return new WenyanDouble(0);
                    }
                    var stack = player.getMainHandItem();
                    if (stack.isEmpty()) {
                        return new WenyanDouble(0);
                    }
                    var holder = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                            .get(Identifier.parse(enchantName));
                    if (holder.isEmpty()) {
                        return new WenyanDouble(0);
                    }
                    stack.enchant(holder.get(), (int) level);
                    return new WenyanDouble(1);
                }
                return WenyanNull.NULL;
            })
            .description("移除玩家主手物品上的指定附魔")
            .handler(ChineseUtils.bracketOf("祛魔"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    String enchantName = args.get(0).as(WenyanString.TYPE).value();
                    Player player = entity.level().getEntitiesOfClass(Player.class, new AABB(entity.blockPosition()).inflate(1.5))
                            .stream().findFirst().orElse(null);
                    if (player == null) {
                        return new WenyanDouble(0);
                    }
                    var stack = player.getMainHandItem();
                    if (stack.isEmpty()) {
                        return new WenyanDouble(0);
                    }
                    var holder = entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                            .get(Identifier.parse(enchantName));
                    if (holder.isEmpty()) {
                        return new WenyanDouble(0);
                    }
                    ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
                    if (enchantments != null) {
                        var mutable = new ItemEnchantments.Mutable(enchantments);
                        mutable.removeIf(h -> h.getKey() == holder.get().getKey());
                        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
                    }
                    return new WenyanDouble(1);
                }
                return WenyanNull.NULL;
            })
            .build();
}
