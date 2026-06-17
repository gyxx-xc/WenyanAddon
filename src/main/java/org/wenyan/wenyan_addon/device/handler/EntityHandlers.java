package org.wenyan.wenyan_addon.device.handler;

import dev.anvilcraft.ping.network.payload.PositionPingPayload;
import dev.anvilcraft.ping.util.PingType;
import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.interpreter_impl.args.ResolvedArgs;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
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

@SuppressWarnings("resource")
public final class EntityHandlers {
    private EntityHandlers() {
    }

    public static final ArgsSpecBuilder.Step<?> markerArgsSpec = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> MARKER_PACKAGE = (_, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("标点"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(3)),
                            new Vec3(args.get(0), args.get(1), args.get(2)), PingType.GENERIC));
                }
                return WenyanNull.NULL;
            })
            .handler(ChineseUtils.bracketOf("警"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(3)),
                            new Vec3(args.get(0), args.get(1), args.get(2)), PingType.WARNING));
                }
                return WenyanNull.NULL;
            })
            .handler(ChineseUtils.bracketOf("往"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                            new Vec3(args.get(0), args.get(1), args.get(2)), PingType.GOTO));
                }
                return WenyanNull.NULL;
            })
            .handler(ChineseUtils.bracketOf("敌"), (ctx, request) -> {
                if (ctx instanceof BlockRequest.BlockContext blockContext && blockContext.level() instanceof ServerLevel sl) {
                    var args = markerArgsSpec.resolve(request);
                    PacketDistributor.sendToPlayersInDimension(sl, new PositionPingPayload(Component.literal(args.get(0)),
                            new Vec3(args.get(0), args.get(1), args.get(2)), PingType.ENEMY));
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
            .handler(ChineseUtils.bracketOf("箭"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = projectileSpawnerArgsSpec.resolve(request);
                Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                Arrow arrow = new Arrow(ctx.level(), bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5, ItemStack.EMPTY, null);
                arrow.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                ctx.level().addFreshEntity(arrow);
            }))
            .handler(ChineseUtils.bracketOf("煙火"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = projectileSpawnerArgsSpec.resolve(request);
                Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                FireworkRocketEntity firework = new FireworkRocketEntity(ctx.level(), bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5,
                        new ItemStack(Items.FIREWORK_ROCKET));
                firework.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                ctx.level().addFreshEntity(firework);
            }))
            .handler(ChineseUtils.bracketOf("雪丸"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = projectileSpawnerArgsSpec.resolve(request);
                Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                Snowball snowball = new Snowball(ctx.level(), bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5, ItemStack.EMPTY);
                snowball.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                ctx.level().addFreshEntity(snowball);
            }))
            .handler(ChineseUtils.bracketOf("火丸"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = projectileSpawnerArgsSpec.resolve(request);
                Vec3 dir = new Vec3(args.get(0), args.get(1), args.get(2));
                SmallFireball fireball = new SmallFireball(ctx.level(), bp.getX() + 0.5, bp.getY() + 1, bp.getZ() + 0.5, dir);
                ctx.level().addFreshEntity(fireball);
            }))
            .build();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> NAMING_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("命名"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(request);
                String name = args.get(3);
                Vec3 center = new Vec3(bp.getX() + (double) args.get(0), bp.getY() + (double) args.get(1), bp.getZ() + (double) args.get(2));
                for (Entity entity : ctx.level().getEntities(null, new AABB(center.subtract(0.5, 0.5, 0.5), center.add(0.5, 0.5, 0.5)))) {
                    entity.setCustomName(Component.literal(name));
                }
            }))
            .build();

    public static final ArgsSpecBuilder.Step<?> entityManipulationArgsSpec = BlockHandlerHelper.singleVec3ArgsSpec.copy()
            .double_().double_().double_().dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> ENTITY_MANIPULATION_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("传送"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = entityManipulationArgsSpec.resolve(request);
                Vec3 dest = new Vec3(bp.getX() + (double) args.get(3), bp.getY() + (double) args.get(4), bp.getZ() + (double) args.get(5));
                for (Entity entity : getEntitiesInBlockRange(ctx.level(), bp, args)) {
                    entity.teleportTo(dest.x, dest.y, dest.z);
                }
            }))
            .handler(ChineseUtils.bracketOf("閃"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = entityManipulationArgsSpec.resolve(request);
                Vec3 origin = new Vec3(bp.getX() + (double) args.get(0), bp.getY() + (double) args.get(1), bp.getZ() + (double) args.get(2));
                Vec3 delta = new Vec3(args.get(3), args.get(4), args.get(5));
                for (Entity entity : getEntitiesInBlockRange(ctx.level(), bp, args)) {
                    entity.teleportTo(origin.x + delta.x, origin.y + delta.y, origin.z + delta.z);
                }
            }))
            .handler(ChineseUtils.bracketOf("施力"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = entityManipulationArgsSpec.resolve(request);
                Vec3 force = new Vec3(args.get(3), args.get(4), args.get(5));
                for (Entity entity : getEntitiesInBlockRange(ctx.level(), bp, args)) {
                    entity.addDeltaMovement(force);
                }
            }))
            .build();

    private static List<Entity> getEntitiesInBlockRange(Level level, BlockPos bp, ResolvedArgs args) {
        Vec3 center = new Vec3(bp.getX() + (double) args.get(0), bp.getY() + (double) args.get(1), bp.getZ() + (double) args.get(2));
        return level.getEntities(null, new AABB(center.subtract(0.5, 0.5, 0.5), center.add(0.5, 0.5, 0.5)));
    }

    public static final ArgsSpecBuilder.Step<?> storageRuneArgsSpec = BlockHandlerHelper.singleVec3ArgsSpec.copy().double_().range(0, 16).dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> STORAGE_RUNE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("收纳"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    var args = storageRuneArgsSpec.resolve(request);
                    BlockPos pos = new BlockPos((int) (bp.getX() + (double) args.get(0)), (int) (bp.getY() + (double) args.get(1)), (int) (bp.getZ() + (double) args.get(2)));
                    double radius = Math.clamp(args.get(3), 0.0, 16.0);
                    int absorbed = BlockHandlerHelper.absorbItems(ctx.level(), storage, pos, radius);
                    return new WenyanDouble(absorbed);
                }
                return new WenyanDouble(0);
            }))
            .handler(ChineseUtils.bracketOf("吐出"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    var args = storageRuneArgsSpec.resolve(request);
                    BlockPos pos = new BlockPos((int) (bp.getX() + (double) args.get(0)), (int) (bp.getY() + (double) args.get(1)), (int) (bp.getZ() + (double) args.get(2)));
                    int count = (int) Math.clamp(args.get(3), 0.0, 2304.0);
                    ItemStack extracted = storage.extractAny(count);
                    if (!extracted.isEmpty()) {
                        ctx.level().addFreshEntity(new ItemEntity(ctx.level(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, extracted));
                    }
                    return new WenyanDouble(extracted.getCount());
                }
                return new WenyanDouble(0);
            }))
            .handler(ChineseUtils.bracketOf("藏量"), BlockHandlerHelper.wrap((ctx, _) -> {
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    return new WenyanDouble(storage.getStoredCount());
                }
                return new WenyanDouble(0);
            }))
            .build();
}
