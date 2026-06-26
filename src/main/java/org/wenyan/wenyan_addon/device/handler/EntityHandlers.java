package org.wenyan.wenyan_addon.device.handler;

import dev.anvilcraft.ping.network.payload.PositionPingPayload;
import dev.anvilcraft.ping.util.PingType;
import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanBoolean;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.wenyan.wenyan_addon.StorageRuneBlockEntity;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.storage.DataDiskStorage;
import org.wenyan.wenyan_addon.storage.WorldSnapshotMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class EntityHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> SPAWN_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("在指定位置召唤实体")
            .handler(ChineseUtils.bracketOf("召"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (ctx.level() instanceof ServerLevel serverLevel) {
                    var args = WenyanArgsResolver.build()
                            .string_().double_().double_().double_().dummy()
                            .resolve(request);
                    var entityTypeRef = BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(args.get(0)));
                    if (entityTypeRef.isPresent()) {
                        EntityType<?> entityType = entityTypeRef.get().value();
                        BlockPos pos = new BlockPos(
                                (int) (bp.getX() + (double) args.get(1)),
                                (int) (bp.getY() + (double) args.get(2)),
                                (int) (bp.getZ() + (double) args.get(3))
                        );
                        Entity entity = entityType.spawn(serverLevel, pos, EntitySpawnReason.COMMAND);
                        return new WenyanDouble(entity != null ? 1 : 0);
                    }
                }
                return new WenyanDouble(0);
            }))
            .description("对指定范围内的生物造成伤害")
            .handler(ChineseUtils.bracketOf("傷"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (ctx.level() instanceof ServerLevel serverLevel) {
                    var args = WenyanArgsResolver.build()
                            .double_().double_().double_().double_().dummy()
                            .resolve(request);
                    Vec3 center = new Vec3(
                            bp.getX() + (double) args.get(0),
                            bp.getY() + (double) args.get(1),
                            bp.getZ() + (double) args.get(2)
                    );
                    float amount = (float) (double) args.get(3);
                    int count = 0;
                    for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class,
                            new AABB(center.subtract(0.5, 0.5, 0.5), center.add(0.5, 0.5, 0.5)))) {
                        entity.hurtServer(serverLevel, serverLevel.damageSources().generic(), amount);
                        count++;
                    }
                    return new WenyanDouble(count);
                }
                return new WenyanDouble(0);
            }))
            .build();

    public static final ArgsSpecBuilder.Step<?> GRANT_EFFECT_ARGS_SPEC = WenyanArgsResolver.build()
            .string_().double_().double_().dummy();
    public static final ArgsSpecBuilder.Step<?> REMOVE_EFFECT_ARGS_SPEC = WenyanArgsResolver.build()
            .string_().dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> POTION_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("为附近的生物或玩家添加指定药水效果")
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
            .description("驱除附近生物或玩家身上的指定药水效果")
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

    public static final ArgsSpecBuilder.Step<?> expArgsSpec = WenyanArgsResolver.build().int_().dummy();
    public static final ArgsSpecBuilder.Step<?> messageArgsSpec = WenyanArgsResolver.build().string_().dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> ENTITY_STATUS_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("将附近玩家生命值恢复至满")
            .handler(ChineseUtils.bracketOf("療"), BlockHandlerHelper.wrapVoid((ctx, _) -> ctx.level().getEntitiesOfClass(Player.class, new AABB(bp).inflate(1.5))
                    .stream().findFirst().ifPresent(player -> player.heal(player.getMaxHealth()))))
            .description("将附近玩家饱食度和饱和度补满")
            .handler(ChineseUtils.bracketOf("飽"), BlockHandlerHelper.wrapVoid((ctx, _) -> ctx.level().getEntitiesOfClass(Player.class, new AABB(bp).inflate(1.5))
                    .stream().findFirst().ifPresent(player -> player.getFoodData().eat(20, 20))))
            .description("为附近玩家增加指定等级的经验")
            .handler(ChineseUtils.bracketOf("賜經驗"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = expArgsSpec.resolve(request);
                ctx.level().getEntitiesOfClass(Player.class, new AABB(bp).inflate(1.5))
                        .stream().findFirst().ifPresent(player -> player.giveExperienceLevels(args.get(0)));
            }))
            .description("向附近玩家发送一条系统消息")
            .handler(ChineseUtils.bracketOf("告"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = messageArgsSpec.resolve(request);
                ctx.level().getEntitiesOfClass(Player.class, new AABB(bp).inflate(1.5))
                        .stream().findFirst().ifPresent(player -> player.sendSystemMessage(Component.literal(args.get(0))));
            }))
            .build();

    public static final ArgsSpecBuilder.Step<?> markerArgsSpec = WenyanArgsResolver.build()
            .string_().double_().double_().double_().dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> MARKER_PACKAGE = (_, _) -> HandlerPackageBuilder.create()
            .description("在世界上标记一个普通坐标点")
            .handler(ChineseUtils.bracketOf("标点"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                            new Vec3(args.get(1), args.get(2), args.get(3)), PingType.GENERIC));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个警坐标点")
            .handler(ChineseUtils.bracketOf("警"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                            new Vec3(args.get(1), args.get(2), args.get(3)), PingType.WARNING));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个前往坐标点")
            .handler(ChineseUtils.bracketOf("往"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                            new Vec3(args.get(1), args.get(2), args.get(3)), PingType.GOTO));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个敌坐标点")
            .handler(ChineseUtils.bracketOf("敌"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                            new Vec3(args.get(1), args.get(2), args.get(3)), PingType.ENEMY));
                }
                return WenyanNull.NULL;
            })
            .build();

    private static Vec3 lampToRangeByBiFunction(BlockPos bp, Vec3 target) throws WenyanException.WenyanDataException {
        double dx = target.x - (bp.getX() + 0.5);
        double dy = target.y - (bp.getY() + 0.5);
        double dz = target.z - (bp.getZ() + 0.5);
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > 3) {
            throw new WenyanException.WenyanDataException("施法距离过远");
        }
        return target;
    }

    private static IntList rgbListToColorsByBiFunction(List<IWenyanValue> rgbList) throws WenyanException.WenyanTypeException {
        IntList colors = new IntArrayList();
        if (rgbList == null || rgbList.isEmpty()) {
            return colors;
        }
        for (int i = 0; i < rgbList.size(); i += 3) {
            double r = rgbList.get(i).as(WenyanDouble.TYPE).value();
            double g = rgbList.get(i + 1).as(WenyanDouble.TYPE).value();
            double b = rgbList.get(i + 2).as(WenyanDouble.TYPE).value();
            colors.add(((int) r << 16) | ((int) g << 8) | (int) b);
        }
        return colors;
    }

    public static final ArgsSpecBuilder.Step<?> projectileSpawnerArgsSpec = WenyanArgsResolver.build()
            .double_().range(-1, 1)
            .double_().range(-1, 1)
            .double_().range(-1, 1)
            .dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> PROJECTILE_SPAWNER_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("发射箭矢")
            .handler(ChineseUtils.bracketOf("箭"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = projectileSpawnerArgsSpec.resolve(request);
                Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                Arrow arrow = new Arrow(ctx.level(), bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5, ItemStack.EMPTY, null);
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                arrow.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                ctx.level().addFreshEntity(arrow);
            }))
            .description("发射烟花火箭")
            .handler(ChineseUtils.bracketOf("煙火"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = request.args();
                int shapeId = (int) args.get(0).as(WenyanDouble.TYPE).value();
                FireworkExplosion.Shape shape = switch (shapeId) {
                    case 1 -> FireworkExplosion.Shape.SMALL_BALL;
                    case 3 -> FireworkExplosion.Shape.STAR;
                    case 4 -> FireworkExplosion.Shape.CREEPER;
                    case 5 -> FireworkExplosion.Shape.BURST;
                    default -> FireworkExplosion.Shape.LARGE_BALL;
                };

                List<IWenyanValue> arg_colors = args.get(1).as(WenyanList.TYPE).value();
                if (arg_colors.isEmpty()) {
                    throw new WenyanException.WenyanTypeException("第二参数应当为一个数字列表且必须是3的倍数");
                } else if (arg_colors.size() % 3 != 0) {
                    throw new WenyanException.WenyanTypeException("第二参数应当为一个数字列表且必须是3的倍数");
                }
                for (IWenyanValue v : arg_colors) {
                    v.as(WenyanDouble.TYPE);
                }
                List<IWenyanValue> arg_fadeColors = args.get(2).as(WenyanList.TYPE).value();
                if (arg_fadeColors.isEmpty()) {
                    throw new WenyanException.WenyanTypeException("第三参数应当为一个数字列表且必须是3的倍数");
                } else if (arg_fadeColors.size() % 3 != 0) {
                    throw new WenyanException.WenyanTypeException("第三参数应当为一个数字列表且必须是3的倍数");
                }
                for (IWenyanValue v : arg_fadeColors) {
                    v.as(WenyanDouble.TYPE);
                }
                boolean hasTrail = args.get(3).as(WenyanBoolean.TYPE).value();
                boolean hasTwinkle = args.get(4).as(WenyanBoolean.TYPE).value();
                int flightDuration = Math.clamp((int) Math.round(args.get(5).as(WenyanDouble.TYPE).value()), 1, 3);

                Vec3 target = lampToRangeByBiFunction(bp, args.get(6).as(WenyanVec3.TYPE).value());
                ItemStack fireworkItem = new ItemStack(Items.FIREWORK_ROCKET);

                FireworkExplosion explosion = new FireworkExplosion(
                        shape,
                        rgbListToColorsByBiFunction(arg_colors),
                        rgbListToColorsByBiFunction(arg_fadeColors),
                        hasTrail,
                        hasTwinkle
                );
                Fireworks fireworks = new Fireworks(flightDuration, List.of(explosion));
                fireworkItem.set(DataComponents.FIREWORKS, fireworks);

                FireworkRocketEntity firework = new FireworkRocketEntity(
                        ctx.level(),
                        target.x, target.y, target.z,
                        fireworkItem
                );
                ctx.level().addFreshEntity(firework);
            }))
            .description("发射雪球")
            .handler(ChineseUtils.bracketOf("雪丸"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = projectileSpawnerArgsSpec.resolve(request);
                Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                Snowball snowball = new Snowball(ctx.level(), bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5, ItemStack.EMPTY);
                snowball.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
            }))
            .description("发射火球")
            .handler(ChineseUtils.bracketOf("火丸"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = projectileSpawnerArgsSpec.resolve(request);
                Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                SmallFireball fireball = new SmallFireball(ctx.level(), bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5, dir);
                ctx.level().addFreshEntity(fireball);
            }))
            .build();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> NAMING_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("为指定位置的实体命名")
            .handler(ChineseUtils.bracketOf("命名"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(request);
                String name = args.get(3);
                Vec3 center = new Vec3(bp.getX() + (double) args.get(0), bp.getY() + (double) args.get(1), bp.getZ() + (double) args.get(2));
                for (Entity entity : ctx.level().getEntities(null, new AABB(center.subtract(0.5, 0.5, 0.5), center.add(0.5, 0.5, 0.5)))) {
                    entity.setCustomName(Component.literal(name));
                }
            }))
            .build();

    public static final ArgsSpecBuilder.Step<?> storageRuneArgsSpec = BlockHandlerHelper.singleVec3ArgsSpec.copy().double_().range(0, 16).dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> STORAGE_RUNE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("将指定范围内的物品收入存储符文")
            .handler(ChineseUtils.bracketOf("收纳"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    var args = storageRuneArgsSpec.resolve(request);
                    BlockPos pos = new BlockPos(
                            (int) (bp.getX() + (double) args.get(0)),
                            (int) (bp.getY() + (double) args.get(1)),
                            (int) (bp.getZ() + (double) args.get(2))
                    );
                    double radius = Math.clamp(args.get(3), 0.0, 16.0);
                    int absorbed = BlockHandlerHelper.absorbItems(ctx.level(), storage, pos, radius);
                    return new WenyanDouble(absorbed);
                }
                return new WenyanDouble(0);
            }))
            .description("从存储符文中吐出物品到指定位置")
            .handler(ChineseUtils.bracketOf("吐出"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    var args = storageRuneArgsSpec.resolve(request);
                    BlockPos pos = new BlockPos(
                            (int) (bp.getX() + (double) args.get(0)),
                            (int) (bp.getY() + (double) args.get(1)),
                            (int) (bp.getZ() + (double) args.get(2))
                    );
                    int count = (int) Math.clamp(args.get(3), 0.0, 2304.0);
                    ItemStack extracted = storage.extractAny(count);
                    if (!extracted.isEmpty()) {
                        ctx.level().addFreshEntity(new ItemEntity(ctx.level(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, extracted));
                    }
                    return new WenyanDouble(extracted.getCount());
                }
                return new WenyanDouble(0);
            }))
            .description("查询存储符文中的物品储量")
            .handler(ChineseUtils.bracketOf("藏量"), BlockHandlerHelper.wrap((ctx, _) -> {
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    return new WenyanDouble(storage.getStoredCount());
                }
                return new WenyanDouble(0);
            }))
            .description("读取指定位置附近实体的数据为图")
            .handler(ChineseUtils.bracketOf("讀實"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)) {
                    return WenyanNull.NULL;
                }
                var args = storageRuneArgsSpec.resolve(request);
                Vec3 center = new Vec3(
                        bp.getX() + (double) args.get(0),
                        bp.getY() + (double) args.get(1),
                        bp.getZ() + (double) args.get(2)
                );
                double radius = Math.clamp(args.get(3), 0.0, 16.0);
                Entity target = serverLevel.getEntities(null, new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius)))
                        .stream()
                        .filter(entity -> !(entity instanceof ItemEntity))
                        .min(Comparator.comparingDouble(a -> a.distanceToSqr(center)))
                        .orElse(null);
                return target == null ? WenyanNull.NULL : WorldSnapshotMapper.entity(target);
            }))
            .description("读取指定位置方块和方块实体的数据为图")
            .handler(ChineseUtils.bracketOf("讀方"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)) {
                    return WenyanNull.NULL;
                }
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                return WorldSnapshotMapper.block(serverLevel, new BlockPos(
                        (int) (bp.getX() + (double) args.get(0)),
                        (int) (bp.getY() + (double) args.get(1)),
                        (int) (bp.getZ() + (double) args.get(2))
                ));
            }))
            .description("列出已插入数据磁盘的UUID")
            .handler(ChineseUtils.bracketOf("盤列"), BlockHandlerHelper.wrap((ctx, _) -> {
                WenyanList result = new WenyanList();
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    for (int slot = 0; slot < storage.getDiskSlots(); slot++) {
                        ItemStack disk = storage.getDisk(slot);
                        if (!disk.isEmpty()) {
                            result.add(new WenyanString(DataDiskStorage.getOrCreateDiskId(disk).toString()));
                            storage.setChanged();
                        }
                    }
                }
                return result;
            }))
            .description("读取数据磁盘指定键")
            .handler(ChineseUtils.bracketOf("盤讀"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)
                        || !(ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage)) {
                    return WenyanNull.NULL;
                }
                Optional<UUID> disk = diskIdAt(storage, request);
                if (disk.isEmpty() || request.args().size() < 2) {
                    return WenyanNull.NULL;
                }
                return DataDiskStorage.readKey(serverLevel, disk.get(), request.args().get(1).as(WenyanString.TYPE).value());
            }))
            .description("写入数据磁盘指定键")
            .handler(ChineseUtils.bracketOf("盤寫"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)
                        || !(ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage)) {
                    return new WenyanDouble(0);
                }
                Optional<UUID> disk = diskIdAt(storage, request);
                if (disk.isEmpty() || request.args().size() < 3) {
                    return new WenyanDouble(0);
                }
                String key = request.args().get(1).as(WenyanString.TYPE).value();
                return new WenyanDouble(DataDiskStorage.writeKey(serverLevel, disk.get(), key, request.args().get(2)) ? 1 : 0);
            }))
            .description("删除数据磁盘指定键")
            .handler(ChineseUtils.bracketOf("盤刪"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)
                        || !(ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage)) {
                    return WenyanNull.NULL;
                }
                Optional<UUID> disk = diskIdAt(storage, request);
                if (disk.isEmpty() || request.args().size() < 2) {
                    return WenyanNull.NULL;
                }
                return DataDiskStorage.deleteKey(serverLevel, disk.get(), request.args().get(1).as(WenyanString.TYPE).value());
            }))
            .description("列出数据磁盘中的键")
            .handler(ChineseUtils.bracketOf("盤鍵"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)
                        || !(ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage)) {
                    return new WenyanList();
                }
                Optional<UUID> disk = diskIdAt(storage, request);
                if (disk.isEmpty()) {
                    return new WenyanList();
                }
                return DataDiskStorage.read(serverLevel, disk.get()).getAttribute("鍵");
            }))
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_SPAWN_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("在指定位置召唤实体")
            .handler(ChineseUtils.bracketOf("召"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity) && entity.level() instanceof ServerLevel serverLevel) {
                    var args = WenyanArgsResolver.build()
                            .string_().double_().double_().double_().dummy()
                            .resolve(request);
                    var entityTypeRef = BuiltInRegistries.ENTITY_TYPE.get(Identifier.parse(args.get(0)));
                    if (entityTypeRef.isPresent()) {
                        EntityType<?> entityType = entityTypeRef.get().value();
                        BlockPos pos = new BlockPos(
                                (int) (entity.blockPosition().getX() + (double) args.get(1)),
                                (int) (entity.blockPosition().getY() + (double) args.get(2)),
                                (int) (entity.blockPosition().getZ() + (double) args.get(3))
                        );
                        Entity spawned = entityType.spawn(serverLevel, pos, EntitySpawnReason.COMMAND);
                        return new WenyanDouble(spawned != null ? 1 : 0);
                    }
                }
                return new WenyanDouble(0);
            })
            .description("对指定范围内的生物造成伤害")
            .handler(ChineseUtils.bracketOf("傷"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity) && entity.level() instanceof ServerLevel serverLevel) {
                    var args = WenyanArgsResolver.build()
                            .double_().double_().double_().double_().dummy()
                            .resolve(request);
                    Vec3 center = new Vec3(
                            entity.blockPosition().getX() + (double) args.get(0),
                            entity.blockPosition().getY() + (double) args.get(1),
                            entity.blockPosition().getZ() + (double) args.get(2)
                    );
                    float amount = (float) (double) args.get(3);
                    int count = 0;
                    for (LivingEntity living : serverLevel.getEntitiesOfClass(LivingEntity.class,
                            new AABB(center.subtract(0.5, 0.5, 0.5), center.add(0.5, 0.5, 0.5)))) {
                        living.hurtServer(serverLevel, serverLevel.damageSources().generic(), amount);
                        count++;
                    }
                    return new WenyanDouble(count);
                }
                return new WenyanDouble(0);
            })
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_POTION_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("为附近的生物或玩家添加指定药水效果")
            .handler(ChineseUtils.bracketOf("賜效"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    Level level = entity.level();
                    var args = GRANT_EFFECT_ARGS_SPEC.resolve(request);
                    String effectId = args.get(0);
                    int duration = (int) ((double) args.get(1));
                    int amplifier = (int) ((double) args.get(2));
                    var effectOpt = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effectId));
                    if (effectOpt.isEmpty()) {
                        return new WenyanDouble(0);
                    }
                    LivingEntity target = findNearestLivingEntity(level, entity.blockPosition());
                    if (target == null) {
                        return new WenyanDouble(0);
                    }
                    target.addEffect(new MobEffectInstance(effectOpt.get(), duration, amplifier));
                    return new WenyanDouble(1);
                }
                return new WenyanDouble(0);
            })
            .description("驱除附近生物或玩家身上的指定药水效果")
            .handler(ChineseUtils.bracketOf("驅效"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    Level level = entity.level();
                    var args = REMOVE_EFFECT_ARGS_SPEC.resolve(request);
                    String effectId = args.get(0);
                    var effectOpt = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effectId));
                    if (effectOpt.isEmpty()) {
                        return new WenyanDouble(0);
                    }
                    LivingEntity target = findNearestLivingEntity(level, entity.blockPosition());
                    if (target == null) {
                        return new WenyanDouble(0);
                    }
                    target.removeEffect(effectOpt.get());
                    return new WenyanDouble(1);
                }
                return new WenyanDouble(0);
            })
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_ENTITY_STATUS_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("将附近玩家生命值恢复至满")
            .handler(ChineseUtils.bracketOf("療"), (ctx, _) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    entity.level().getEntitiesOfClass(Player.class, new AABB(entity.blockPosition()).inflate(1.5))
                            .stream().findFirst().ifPresent(player -> player.heal(player.getMaxHealth()));
                }
                return WenyanNull.NULL;
            })
            .description("将附近玩家饱食度和饱和度补满")
            .handler(ChineseUtils.bracketOf("飽"), (ctx, _) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    entity.level().getEntitiesOfClass(Player.class, new AABB(entity.blockPosition()).inflate(1.5))
                            .stream().findFirst().ifPresent(player -> player.getFoodData().eat(20, 20));
                }
                return WenyanNull.NULL;
            })
            .description("为附近玩家增加指定等级的经验")
            .handler(ChineseUtils.bracketOf("賜經驗"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = expArgsSpec.resolve(request);
                    entity.level().getEntitiesOfClass(Player.class, new AABB(entity.blockPosition()).inflate(1.5))
                            .stream().findFirst().ifPresent(player -> player.giveExperienceLevels(args.get(0)));
                }
                return WenyanNull.NULL;
            })
            .description("向附近玩家发送一条系统消息")
            .handler(ChineseUtils.bracketOf("告"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = messageArgsSpec.resolve(request);
                    entity.level().getEntitiesOfClass(Player.class, new AABB(entity.blockPosition()).inflate(1.5))
                            .stream().findFirst().ifPresent(player -> player.sendSystemMessage(Component.literal(args.get(0))));
                }
                return WenyanNull.NULL;
            })
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_MARKER_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("在世界上标记一个普通坐标点")
            .handler(ChineseUtils.bracketOf("标点"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity) && entity.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                            new Vec3(args.get(1), args.get(2), args.get(3)), PingType.GENERIC));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个警坐标点")
            .handler(ChineseUtils.bracketOf("警"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity) && entity.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                            new Vec3(args.get(1), args.get(2), args.get(3)), PingType.WARNING));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个前往坐标点")
            .handler(ChineseUtils.bracketOf("往"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity) && entity.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                            new Vec3(args.get(1), args.get(2), args.get(3)), PingType.GOTO));
                }
                return WenyanNull.NULL;
            })
            .description("在世界上标记一个敌坐标点")
            .handler(ChineseUtils.bracketOf("敌"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity) && entity.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                            new Vec3(args.get(1), args.get(2), args.get(3)), PingType.ENEMY));
                }
                return WenyanNull.NULL;
            })
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_PROJECTILE_SPAWNER_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("发射箭矢")
            .handler(ChineseUtils.bracketOf("箭"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = projectileSpawnerArgsSpec.resolve(request);
                    Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                    Arrow arrow = new Arrow(entity.level(), entity.blockPosition().getX() + 0.5, entity.blockPosition().getY() + 1, entity.blockPosition().getZ() + 0.5, ItemStack.EMPTY, null);
                    arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    arrow.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                    entity.level().addFreshEntity(arrow);
                }
                return WenyanNull.NULL;
            })
            .description("发射烟花火箭")
            .handler(ChineseUtils.bracketOf("煙火"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    int shapeId = (int) args.get(0).as(WenyanDouble.TYPE).value();
                    FireworkExplosion.Shape shape = switch (shapeId) {
                        case 1 -> FireworkExplosion.Shape.SMALL_BALL;
                        case 3 -> FireworkExplosion.Shape.STAR;
                        case 4 -> FireworkExplosion.Shape.CREEPER;
                        case 5 -> FireworkExplosion.Shape.BURST;
                        default -> FireworkExplosion.Shape.LARGE_BALL;
                    };

                    List<IWenyanValue> arg_colors = args.get(1).as(WenyanList.TYPE).value();
                    if (arg_colors.isEmpty()) {
                        throw new WenyanException.WenyanTypeException("第二参数应当为一个数字列表且必须是3的倍数");
                    } else if (arg_colors.size() % 3 != 0) {
                        throw new WenyanException.WenyanTypeException("第二参数应当为一个数字列表且必须是3的倍数");
                    }
                    for (IWenyanValue v : arg_colors) {
                        v.as(WenyanDouble.TYPE);
                    }
                    List<IWenyanValue> arg_fadeColors = args.get(2).as(WenyanList.TYPE).value();
                    if (arg_fadeColors.isEmpty()) {
                        throw new WenyanException.WenyanTypeException("第三参数应当为一个数字列表且必须是3的倍数");
                    } else if (arg_fadeColors.size() % 3 != 0) {
                        throw new WenyanException.WenyanTypeException("第三参数应当为一个数字列表且必须是3的倍数");
                    }
                    for (IWenyanValue v : arg_fadeColors) {
                        v.as(WenyanDouble.TYPE);
                    }
                    boolean hasTrail = args.get(3).as(WenyanBoolean.TYPE).value();
                    boolean hasTwinkle = args.get(4).as(WenyanBoolean.TYPE).value();
                    int flightDuration = Math.clamp((int) Math.round(args.get(5).as(WenyanDouble.TYPE).value()), 1, 3);

                    Vec3 target = lampToRangeByBiFunction(entity.blockPosition(), args.get(6).as(WenyanVec3.TYPE).value());
                    ItemStack fireworkItem = new ItemStack(Items.FIREWORK_ROCKET);

                    FireworkExplosion explosion = new FireworkExplosion(
                            shape,
                            rgbListToColorsByBiFunction(arg_colors),
                            rgbListToColorsByBiFunction(arg_fadeColors),
                            hasTrail,
                            hasTwinkle
                    );
                    Fireworks fireworks = new Fireworks(flightDuration, List.of(explosion));
                    fireworkItem.set(DataComponents.FIREWORKS, fireworks);

                    FireworkRocketEntity firework = new FireworkRocketEntity(
                            entity.level(),
                            target.x, target.y, target.z,
                            fireworkItem
                    );
                    entity.level().addFreshEntity(firework);
                }
                return WenyanNull.NULL;
            })
            .description("发射雪球")
            .handler(ChineseUtils.bracketOf("雪丸"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = projectileSpawnerArgsSpec.resolve(request);
                    Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                    Snowball snowball = new Snowball(entity.level(), entity.blockPosition().getX() + 0.5, entity.blockPosition().getY() + 1, entity.blockPosition().getZ() + 0.5, ItemStack.EMPTY);
                    snowball.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                }
                return WenyanNull.NULL;
            })
            .description("发射火球")
            .handler(ChineseUtils.bracketOf("火丸"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = projectileSpawnerArgsSpec.resolve(request);
                    Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                    SmallFireball fireball = new SmallFireball(entity.level(), entity.blockPosition().getX() + 0.5, entity.blockPosition().getY() + 1, entity.blockPosition().getZ() + 0.5, dir);
                    entity.level().addFreshEntity(fireball);
                }
                return WenyanNull.NULL;
            })
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_NAMING_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("为指定位置的实体命名")
            .handler(ChineseUtils.bracketOf("命名"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(request);
                    String name = args.get(3);
                    Vec3 center = new Vec3(
                            entity.blockPosition().getX() + (double) args.get(0),
                            entity.blockPosition().getY() + (double) args.get(1),
                            entity.blockPosition().getZ() + (double) args.get(2)
                    );
                    for (Entity e : entity.level().getEntities(null, new AABB(center.subtract(0.5, 0.5, 0.5), center.add(0.5, 0.5, 0.5)))) {
                        e.setCustomName(Component.literal(name));
                    }
                }
                return WenyanNull.NULL;
            })
            .build();

    private EntityHandlers() {
    }

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

    private static Optional<UUID> diskIdAt(StorageRuneBlockEntity storage, BlockRequest request) throws WenyanException.WenyanTypeException {
        if (request.args().isEmpty()) {
            return Optional.empty();
        }
        int slot = (int) request.args().get(0).as(WenyanDouble.TYPE).value() - 1;
        ItemStack disk = storage.getDisk(slot);
        if (disk.isEmpty()) {
            return Optional.empty();
        }
        UUID diskId = DataDiskStorage.getOrCreateDiskId(disk);
        storage.setChanged();
        return Optional.of(diskId);
    }
}
