package org.wenyan.pong.mobeffect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class Drunk extends MobEffect {
    public static final String DRUNK_LEVEL = "drunk_level";
    public Drunk() {
        super(MobEffectCategory.HARMFUL, 0);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity pLivingEntity, int pAmplifier) {
        pLivingEntity.xRotO = 180;
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
        return true;
    }
}
