package org.pongdev.pong.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.pongdev.pong.Pong;
import org.pongdev.pong.entity.PlugEntity;
import org.pongdev.pong.setup.PongRegistration;

public class OpenChampagne {
    public static void open(ItemStack pStack, Entity entity, Level pLevel) {

        Pong.LOGGER.info("open");
        PongStackData.putInt(pStack, ChampagneBottle.CAPABILITY_TAG, 1000);
        PongStackData.putBoolean(pStack, ChampagneBottle.OPEN_TAG, true);
        ChampagneBottle.syncModelData(pStack);
        double power = PongStackData.getDouble(pStack, ChampagneBottle.POWER_TAG);
        Vec3 position = entity.getEyePosition();
        Vec3 lookWay = Vec3.directionFromRotation(
                entity.getXRot(),
                entity.getYRot());
        //playSound(position, power, pLevel);
        entity.playSound(SoundEvents.GENERIC_EXPLODE.value(), (float) power/40, 1.0F);
        emmitParticle(position, lookWay, power, pLevel);
        shootPlug(position, lookWay, power, pLevel);
    }

    // TODO: refer to how the bow do, create a plug entity
    // TODO: the power will decided the speed and the damage of a plug
    public static void shootPlug(Vec3 position, Vec3 lookWay, double power, Level pLevel) {
        PlugEntity plug = new PlugEntity(PongRegistration.PLUG_ENTITY.get(), pLevel);
        plug.setPos(position.x, position.y, position.z);
        plug.setBaseDamage(1);
        plug.shoot(lookWay.x, lookWay.y, lookWay.z, (float) ((power/10)+0.1), 20.0F);
        pLevel.addFreshEntity(plug);
    }

    // TODO: the power will affect the splash range of the particle
    public static void emmitParticle(Vec3 position, Vec3 lookWay, double power, Level level) {
        power = Math.max(power, 0.1);
        lookWay = lookWay.scale(power);
        int count = (int) (power / 10 + 1) * 5;
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(PongRegistration.SPLASH_PARTICLES.get(),
                    position.x, position.y - 0.1, position.z,
                    count, lookWay.x, lookWay.y, lookWay.z, 1.0);
        } else if (level.isClientSide()) {
            for (int i = 0; i < count; i++) {
                level.addParticle(PongRegistration.SPLASH_PARTICLES.get(),
                        position.x, position.y - 0.1, position.z,
                        lookWay.x, lookWay.y, lookWay.z);
            }
        }
    }
}
