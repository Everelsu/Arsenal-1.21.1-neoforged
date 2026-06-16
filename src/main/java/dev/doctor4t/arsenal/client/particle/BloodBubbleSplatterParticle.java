package dev.doctor4t.arsenal.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class BloodBubbleSplatterParticle extends TextureSheetParticle {
    private final SpriteSet spriteProvider;

    public BloodBubbleSplatterParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.spriteProvider = spriteProvider;
        this.setSpriteFromAge(spriteProvider);
        this.quadSize *= 0.5f + this.random.nextFloat() * 0.50f;

        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    @Override
    public void tick() {
        this.setSpriteFromAge(this.spriteProvider);
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.yd -= 0.05;
        this.move(this.xd, this.yd, this.zd);
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType defaultParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i) {
            return new BloodBubbleSplatterParticle(clientWorld, d, e, f, g, h, i, this.spriteProvider);
        }
    }
}
