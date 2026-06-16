package dev.doctor4t.arsenal.client.particle;

import dev.doctor4t.arsenal.client.particle.type.SweepParticleType;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

public class SweepAttackParticle extends TextureSheetParticle {
    private final SpriteSet spriteWithAge;

    private SweepAttackParticle(ClientLevel world, double x, double y, double z, SpriteSet spriteWithAge) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.spriteWithAge = spriteWithAge;
        this.lifetime = 4;
        this.quadSize = 1.0F;
        this.setSpriteFromAge(spriteWithAge);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.spriteWithAge);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SweepParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public @Nullable SweepAttackParticle createParticle(SweepParticleType parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            SweepAttackParticle instance = new SweepAttackParticle(world, x, y, z, this.spriteProvider);
            if (parameters.initialData != null) {
                Color color = new Color(parameters.initialData.color, true);
                instance.setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
                instance.setAlpha(color.getAlpha() / 255f);
            }
            return instance;
        }
    }
}
