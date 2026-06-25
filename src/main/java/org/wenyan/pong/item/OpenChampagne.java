package org.wenyan.pong.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.wenyan.pong.Pong;
import org.wenyan.pong.entity.PlugEntity;
import org.wenyan.pong.setup.PongRegistration;

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
        playSound(position, power, pLevel);
        emmitParticle(position, lookWay, power, pLevel);
        shootPlug(position, lookWay, power, pLevel);
    }

    public static void playSound(Vec3 position, double power, Level level) {
        float volume = Math.max(0.6F, (float) power / 40.0F);
        if (level.isClientSide()) {
            level.playLocalSound(position.x, position.y, position.z,
                    PongRegistration.CHAMPAGNE_OPEN.get(), SoundSource.PLAYERS, volume, 1.0F, false);
        } else {
            level.playSound(null, position.x, position.y, position.z,
                    PongRegistration.CHAMPAGNE_OPEN.get(), SoundSource.PLAYERS, volume, 1.0F);
        }
    }

    // TODO: refer to how the bow do, create a plug entity
    // TODO: the power will decided the speed and the damage of a plug
    public static void shootPlug(Vec3 position, Vec3 lookWay, double power, Level pLevel) {
        if (pLevel.isClientSide()) return;
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
            for (int i = 0; i < count; i++) {
                serverLevel.sendParticles(PongRegistration.SPLASH_PARTICLES.get(),
                        position.x, position.y - 0.1, position.z,
                        0, lookWay.x, lookWay.y, lookWay.z, 1.0);
            }
        } else if (level.isClientSide()) {
            for (int i = 0; i < count; i++) {
                level.addParticle(PongRegistration.SPLASH_PARTICLES.get(),
                        position.x, position.y - 0.1, position.z,
                        lookWay.x, lookWay.y, lookWay.z);
            }
        }
    }
}
