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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> ELEMENTAL_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("在指定位置放置水源方块")
            .handler(ChineseUtils.bracketOf("水源"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                ctx.level().setBlock(BlockHandlerHelper.offsetPos(bp, args), Blocks.WATER.defaultBlockState(), 3);
            }))
            .description("在指定位置放置熔岩方块")
            .handler(ChineseUtils.bracketOf("熔岩"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                ctx.level().setBlock(BlockHandlerHelper.offsetPos(bp, args), Blocks.LAVA.defaultBlockState(), 3);
            }))
            .description("清除指定位置的流体（水或熔岩）")
            .handler(ChineseUtils.bracketOf("除流"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                if (!ctx.level().getBlockState(pos).getFluidState().isEmpty()) {
                    ctx.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }))
            .description("将指定位置的水冻结成冰")
            .handler(ChineseUtils.bracketOf("冻水成冰"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                if (ctx.level().getBlockState(pos).is(Blocks.WATER)) {
                    ctx.level().setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
                }
            }))
            .build();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> WORLD_INTERACTION_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("对指定位置使用骨粉催生植物")
            .handler(ChineseUtils.bracketOf("化生"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BoneMealItem.applyBonemeal(ItemStack.EMPTY, ctx.level(), BlockHandlerHelper.offsetPos(bp, args), null);
            }))
            .description("在指定位置上方点燃火焰")
            .handler(ChineseUtils.bracketOf("燃"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos above = BlockHandlerHelper.offsetPos(bp, args).above();
                if (ctx.level().getBlockState(above).isAir()) {
                    ctx.level().setBlock(above, Blocks.FIRE.defaultBlockState(), 3);
                }
            }))
            .description("扑灭指定位置的火焰并清除附近实体的着火状态")
            .handler(ChineseUtils.bracketOf("滅"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                if (ctx.level().getBlockState(pos).is(Blocks.FIRE)) {
                    ctx.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
                for (Entity entity : ctx.level().getEntities(null, new AABB(pos).inflate(3.0))) {
                    entity.clearFire();
                }
            }))
            .build();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> READ_WRITE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("读取告示牌上的文字")
            .handler(ChineseUtils.bracketOf("讀示"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockEntity be = ctx.level().getBlockEntity(BlockHandlerHelper.offsetPos(bp, args));
                if (be instanceof SignBlockEntity sign) {
                    StringBuilder sb = new StringBuilder();
                    for (Component msg : sign.getFrontText().getMessages(false)) {
                        sb.append(msg.getString());
                    }
                    return new WenyanString(sb.toString());
                }
                return WenyanNull.NULL;
            }))
            .description("将文字写入告示牌的四面")
            .handler(ChineseUtils.bracketOf("書示"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.copy()
                        .string_().string_().string_().string_()
                        .resolve(request);
                BlockPos pos = BlockHandlerHelper.offsetPos(bp, args);
                BlockEntity be = ctx.level().getBlockEntity(pos);
                if (be instanceof SignBlockEntity sign) {
                    var text = sign.getFrontText()
                            .setMessage(0, Component.literal(args.get(3)))
                            .setMessage(1, Component.literal(args.get(4)))
                            .setMessage(2, Component.literal(args.get(5)))
                            .setMessage(3, Component.literal(args.get(6)));
                    sign.setText(text, true);
                }
            }))
            .description("读取讲台上书本的内容和标题")
            .handler(ChineseUtils.bracketOf("讀講臺"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockEntity be = ctx.level().getBlockEntity(BlockHandlerHelper.offsetPos(bp, args));
                if (be instanceof LecternBlockEntity lectern) {
                    ItemStack book = lectern.getBook();
                    if (!book.isEmpty()) {
                        WrittenBookContent content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
                        if (content != null) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(content.title().get(false)).append("\n");
                            for (var page : content.pages()) {
                                sb.append(page.get(false));
                            }
                            return new WenyanString(sb.toString());
                        }
                    }
                }
                return WenyanNull.NULL;
            }))
            .build();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> NOTE_BLOCK_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("演奏指定音高的音符盒音效")
            .handler(ChineseUtils.bracketOf("奏乐"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = WenyanArgsResolver.build().double_().range(0, 24).resolve(request);
                int note = (int) Math.clamp((double) args.get(0), 0, 24);
                float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
                ctx.level().playSound(null, bp, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 3.0F, pitch);
            }))
            .build();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> PARTICLE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("在符文周围生成指定颜色的粒子效果")
            .handler(ChineseUtils.bracketOf("放塵"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = WenyanArgsResolver.build()
                        .int_().range(0, 255)
                        .int_().range(0, 255)
                        .int_().range(0, 255)
                        .int_().range(1, 20)
                        .resolve(request);
                int color = 0xFF000000
                        | (((int) args.get(0) & 0xFF) << 16)
                        | (((int) args.get(1) & 0xFF) << 8)
                        | ((int) args.get(2) & 0xFF);
                DustParticleOptions dust = new DustParticleOptions(color, 1.0f);
                if (ctx.level() instanceof ServerLevel server) {
                    for (int i = 0; i < (int) args.get(3); i++) {
                        double sx = bp.getX() + server.getRandom().nextGaussian() * 0.5;
                        double sy = bp.getY() + server.getRandom().nextGaussian() * 0.5;
                        double sz = bp.getZ() + server.getRandom().nextGaussian() * 0.5;
                        server.sendParticles(dust, sx, sy, sz, 1, 0.0, 0.0, 0.0, 0.0);
                    }
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
    public static final Function<ItemStack, RawHandlerPackage> ITEM_NOTE_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("演奏指定音高的音符盒音效")
            .handler(ChineseUtils.bracketOf("奏乐"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = WenyanArgsResolver.build().double_().range(0, 24).resolve(argsRequest);
                    int note = (int) Math.clamp((double) args.get(0), 0, 24);
                    float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
                    entity.level().playSound(null, entity, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.PLAYERS, 3.0F, pitch);
                }
                return WenyanNull.NULL;
            })
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_ELEMENTAL_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("在指定位置放置水源方块")
            .handler(ChineseUtils.bracketOf("水源"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    entity.level().setBlock(BlockHandlerHelper.offsetPos(entity.blockPosition(), args), Blocks.WATER.defaultBlockState(), 3);
                }
                return WenyanNull.NULL;
            })
            .description("在指定位置放置熔岩方块")
            .handler(ChineseUtils.bracketOf("熔岩"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    entity.level().setBlock(BlockHandlerHelper.offsetPos(entity.blockPosition(), args), Blocks.LAVA.defaultBlockState(), 3);
                }
                return WenyanNull.NULL;
            })
            .description("清除指定位置的流体（水或熔岩）")
            .handler(ChineseUtils.bracketOf("除流"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BlockPos pos = BlockHandlerHelper.offsetPos(entity.blockPosition(), args);
                    if (!entity.level().getBlockState(pos).getFluidState().isEmpty()) {
                        entity.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
                return WenyanNull.NULL;
            })
            .description("将指定位置的水冻结成冰")
            .handler(ChineseUtils.bracketOf("冻水成冰"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BlockPos pos = BlockHandlerHelper.offsetPos(entity.blockPosition(), args);
                    if (entity.level().getBlockState(pos).is(Blocks.WATER)) {
                        entity.level().setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
                    }
                }
                return WenyanNull.NULL;
            })
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_WORLD_INTERACTION_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("对指定位置使用骨粉催生植物")
            .handler(ChineseUtils.bracketOf("化生"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BoneMealItem.applyBonemeal(ItemStack.EMPTY, entity.level(), BlockHandlerHelper.offsetPos(entity.blockPosition(), args), null);
                }
                return WenyanNull.NULL;
            })
            .description("在指定位置上方点燃火焰")
            .handler(ChineseUtils.bracketOf("燃"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BlockPos above = BlockHandlerHelper.offsetPos(entity.blockPosition(), args).above();
                    if (entity.level().getBlockState(above).isAir()) {
                        entity.level().setBlock(above, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
                return WenyanNull.NULL;
            })
            .description("扑灭指定位置的火焰并清除附近实体的着火状态")
            .handler(ChineseUtils.bracketOf("滅"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BlockPos pos = BlockHandlerHelper.offsetPos(entity.blockPosition(), args);
                    if (entity.level().getBlockState(pos).is(Blocks.FIRE)) {
                        entity.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                    for (Entity e : entity.level().getEntities(null, new AABB(pos).inflate(3.0))) {
                        e.clearFire();
                    }
                }
                return WenyanNull.NULL;
            })
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_READ_WRITE_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("读取告示牌上的文字")
            .handler(ChineseUtils.bracketOf("讀示"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BlockEntity be = entity.level().getBlockEntity(BlockHandlerHelper.offsetPos(entity.blockPosition(), args));
                    if (be instanceof SignBlockEntity sign) {
                        StringBuilder sb = new StringBuilder();
                        for (Component msg : sign.getFrontText().getMessages(false)) {
                            sb.append(msg.getString());
                        }
                        return new WenyanString(sb.toString());
                    }
                }
                return WenyanNull.NULL;
            })
            .description("将文字写入告示牌的四面")
            .handler(ChineseUtils.bracketOf("書示"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.copy()
                            .string_().string_().string_().string_()
                            .resolve(argsRequest);
                    BlockPos pos = BlockHandlerHelper.offsetPos(entity.blockPosition(), args);
                    BlockEntity be = entity.level().getBlockEntity(pos);
                    if (be instanceof SignBlockEntity sign) {
                        var text = sign.getFrontText()
                                .setMessage(0, Component.literal(args.get(3)))
                                .setMessage(1, Component.literal(args.get(4)))
                                .setMessage(2, Component.literal(args.get(5)))
                                .setMessage(3, Component.literal(args.get(6)));
                        sign.setText(text, true);
                    }
                }
                return WenyanNull.NULL;
            })
            .description("读取讲台上书本的内容和标题")
            .handler(ChineseUtils.bracketOf("讀講臺"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(argsRequest);
                    BlockEntity be = entity.level().getBlockEntity(BlockHandlerHelper.offsetPos(entity.blockPosition(), args));
                    if (be instanceof LecternBlockEntity lectern) {
                        ItemStack book = lectern.getBook();
                        if (!book.isEmpty()) {
                            WrittenBookContent content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
                            if (content != null) {
                                StringBuilder sb = new StringBuilder();
                                sb.append(content.title().get(false)).append("\n");
                                for (var page : content.pages()) {
                                    sb.append(page.get(false));
                                }
                                return new WenyanString(sb.toString());
                            }
                        }
                    }
                }
                return WenyanNull.NULL;
            })
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_PARTICLE_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("在符文周围生成指定颜色的粒子效果")
            .handler(ChineseUtils.bracketOf("放塵"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = WenyanArgsResolver.build()
                            .int_().range(0, 255)
                            .int_().range(0, 255)
                            .int_().range(0, 255)
                            .int_().range(1, 20)
                            .resolve(argsRequest);
                    int color = 0xFF000000
                            | (((int) args.get(0) & 0xFF) << 16)
                            | (((int) args.get(1) & 0xFF) << 8)
                            | ((int) args.get(2) & 0xFF);
                    DustParticleOptions dust = new DustParticleOptions(color, 1.0f);
                    if (entity.level() instanceof ServerLevel server) {
                        BlockPos bp = entity.blockPosition();
                        for (int i = 0; i < (int) args.get(3); i++) {
                            double sx = bp.getX() + server.getRandom().nextGaussian() * 0.5;
                            double sy = bp.getY() + server.getRandom().nextGaussian() * 0.5;
                            double sz = bp.getZ() + server.getRandom().nextGaussian() * 0.5;
                            server.sendParticles(dust, sx, sy, sz, 1, 0.0, 0.0, 0.0, 0.0);
                        }
                    }
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
