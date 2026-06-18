package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;

public final class SpawnHandlers {
    private SpawnHandlers() {
    }

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> SPAWN_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
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
}
