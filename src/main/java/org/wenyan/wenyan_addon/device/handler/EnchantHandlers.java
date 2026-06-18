package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;

public final class EnchantHandlers {
    private EnchantHandlers() {
    }

    public static final ArgsSpecBuilder.Step<?> enchantArgsSpec = WenyanArgsResolver.build()
            .string_().double_().dummy();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> ENCHANT_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("附靈"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = enchantArgsSpec.resolve(request);
                String enchantName = args.get(0);
                double level = args.get(1);

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
            .handler(ChineseUtils.bracketOf("去靈"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = enchantArgsSpec.resolve(request);
                String enchantName = args.get(0);

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
}
