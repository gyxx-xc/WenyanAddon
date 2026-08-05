package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.judou.api.values.exception.WenyanException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class HandlerUtils {

    private HandlerUtils() {
    }
    public static Vec3 lampToRangeByBiFunction(BlockPos bp, Vec3 target) throws WenyanException.WenyanDataException {
        double dx = target.x - (bp.getX() + 0.5);
        double dy = target.y - (bp.getY() + 0.5);
        double dz = target.z - (bp.getZ() + 0.5);
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > 3) {
            throw new WenyanException.WenyanDataException(Component.translatable("wenyan_addon.error.address_to_loog").getString());
        }
        return target;
    }
    public static Vec3 lampToRangeByFunction(Entity entity, Vec3 target) throws WenyanException.WenyanDataException {
        double dx = target.x - (entity.getX() + 0.5);
        double dy = target.y - (entity.getY() + 0.5);
        double dz = target.z - (entity.getZ() + 0.5);
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist > 3) {
            throw new WenyanException.WenyanDataException(Component.translatable("wenyan_addon.error.address_to_loog").getString());
        }
        return target;
    }
    public static LivingEntity findNearestLivingEntity(Level level, BlockPos bp) {
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
