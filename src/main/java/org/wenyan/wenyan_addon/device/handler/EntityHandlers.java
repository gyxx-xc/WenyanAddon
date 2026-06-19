package org.wenyan.wenyan_addon.device.handler;

import dev.anvilcraft.ping.network.payload.PositionPingPayload;
import dev.anvilcraft.ping.util.PingType;
import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.wenyan.wenyan_addon.StorageRuneBlockEntity;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.List;
import java.util.function.BiFunction;

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

    public static final ArgsSpecBuilder.Step<?> expArgsSpec = WenyanArgsResolver.build().double_().dummy();
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
                arrow.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                ctx.level().addFreshEntity(arrow);
            }))
            .description("发射烟花火箭")
            .handler(ChineseUtils.bracketOf("煙火"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = projectileSpawnerArgsSpec.resolve(request);
                Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                FireworkRocketEntity firework = new FireworkRocketEntity(ctx.level(), bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5,
                        new ItemStack(Items.FIREWORK_ROCKET));
                firework.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                ctx.level().addFreshEntity(firework);
            }))
            .description("发射雪球")
            .handler(ChineseUtils.bracketOf("雪丸"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = projectileSpawnerArgsSpec.resolve(request);
                Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                Snowball snowball = new Snowball(ctx.level(), bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5, ItemStack.EMPTY);
                snowball.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                ctx.level().addFreshEntity(snowball);
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
}
