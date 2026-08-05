package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.utils.WenyanValues;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class WorldHandlers {
    public static final ArgsSpecBuilder.Step<?> enchantArgsSpec = WenyanArgsResolver.build()
            .string_().double_().dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> ENCHANT_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("为玩家主手物品添加指定等级的附魔")
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
            .description("移除玩家主手物品上的指定附魔")
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

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> BLOCK_EDIT_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("在指定位置放置方块")
            .handler(ChineseUtils.bracketOf("置"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                String blockId = args.get(3);
                try {
                    Identifier id = Identifier.parse(blockId);
                    if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                        return WenyanValues.of(0);
                    }
                    Block block = BuiltInRegistries.BLOCK.getValue(id);
                    boolean result = ctx.level().setBlock(pos, block.defaultBlockState(), 3);
                    return WenyanValues.of(result ? 1 : 0);
                } catch (Exception e) {
                    return WenyanValues.of(0);
                }
            }))
            .description("破坏指定位置的方块")
            .handler(ChineseUtils.bracketOf("毀"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                boolean result = ctx.level().destroyBlock(pos, true);
                return WenyanValues.of(result ? 1 : 0);
            }))
            .description("将指定位置的方块替换为另一种方块，尽量保留原方块属性")
            .handler(ChineseUtils.bracketOf("替"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                String toBlockId = args.get(3);
                try {
                    Identifier id = Identifier.parse(toBlockId);
                    if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                        return WenyanValues.of(0);
                    }
                    Block targetBlock = BuiltInRegistries.BLOCK.getValue(id);
                    BlockState oldState = ctx.level().getBlockState(pos);
                    BlockState newState;
                    try {
                        newState = targetBlock.withPropertiesOf(oldState);
                    } catch (IllegalArgumentException e) {
                        newState = targetBlock.defaultBlockState();
                    }
                    boolean result = ctx.level().setBlock(pos, newState, 3);
                    return WenyanValues.of(result ? 1 : 0);
                } catch (Exception e) {
                    return WenyanValues.of(0);
                }
            }))
            .build();









    public static final Function<ItemStack, RawHandlerPackage> ITEM_ENCHANT_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("为玩家主手物品添加指定等级的附魔")
            .handler(ChineseUtils.bracketOf("附靈"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = enchantArgsSpec.resolve(argsRequest);
                    String enchantName = args.get(0);
                    double level = args.get(1);
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
            .handler(ChineseUtils.bracketOf("去靈"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = enchantArgsSpec.resolve(argsRequest);
                    String enchantName = args.get(0);
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




    public static final Function<ItemStack, RawHandlerPackage> ITEM_BLOCK_EDIT_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("在指定位置放置方块")
            .handler(ChineseUtils.bracketOf("置"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(argsRequest);
                    BlockPos pos = BlockHandlerHelper.offsetPos(entity.blockPosition(), args);
                    String blockId = args.get(3);
                    try {
                        Identifier id = Identifier.parse(blockId);
                        if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                            return WenyanValues.of(0);
                        }
                        Block block = BuiltInRegistries.BLOCK.getValue(id);
                        boolean result = entity.level().setBlock(pos, block.defaultBlockState(), 3);
                        return WenyanValues.of(result ? 1 : 0);
                    } catch (Exception e) {
                        return WenyanValues.of(0);
                    }
                }
                return WenyanNull.NULL;
            })
            .description("破坏指定位置的方块")
            .handler(ChineseUtils.bracketOf("毀"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    boolean result = entity.level().destroyBlock(BlockHandlerHelper.offsetPos(entity.blockPosition(), args), true);
                    return WenyanValues.of(result ? 1 : 0);
                }
                return WenyanNull.NULL;
            })
            .description("将指定位置的方块替换为另一种方块，尽量保留原方块属性")
            .handler(ChineseUtils.bracketOf("替"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(argsRequest);
                    BlockPos pos = BlockHandlerHelper.offsetPos(entity.blockPosition(), args);
                    String toBlockId = args.get(3);
                    try {
                        Identifier id = Identifier.parse(toBlockId);
                        if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                            return WenyanValues.of(0);
                        }
                        Block targetBlock = BuiltInRegistries.BLOCK.getValue(id);
                        BlockState oldState = entity.level().getBlockState(pos);
                        BlockState newState;
                        try {
                            newState = targetBlock.withPropertiesOf(oldState);
                        } catch (IllegalArgumentException e) {
                            newState = targetBlock.defaultBlockState();
                        }
                        boolean result = entity.level().setBlock(pos, newState, 3);
                        return WenyanValues.of(result ? 1 : 0);
                    } catch (Exception e) {
                        return WenyanValues.of(0);
                    }
                }
                return WenyanNull.NULL;
            })
            .build();

    private WorldHandlers() {
    }
}
