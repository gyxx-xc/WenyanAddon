package org.pongdev.pong.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

// TODO: change the name of the class
// SplashParticles is not a util class of SplashParticle
public class SplashParticles extends SingleQuadParticle {

    public static final String ID = "splash_particles";

    public SplashParticles(ClientLevel pLevel, double pX, double pY, double pZ,
                              SpriteSet spriteSet, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, spriteSet.get(0, 1));
        this.friction = 0.999999999999999999999F;
        this.lifetime = 50;
        this.gravity = 0.4F;
        this.setSpriteFromAge(spriteSet);
        this.speedUpWhenYMotionIsBlocked = true;
    }

    @Override
    public void tick() {
        super.tick();
        fadeout();
    }

    private void fadeout(){
        this.alpha = age > 30 ? (1 - (float)(age-30)/(lifetime-30)) : 1;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz, RandomSource random) {
            return new SplashParticles(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
