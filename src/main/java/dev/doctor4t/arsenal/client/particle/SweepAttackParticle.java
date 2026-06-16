package dev.doctor4t.arsenal.client.particle;

import dev.doctor4t.arsenal.client.particle.type.SweepParticleType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class SweepAttackParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteWithAge;

    private SweepAttackParticle(ClientWorld world, double x, double y, double z, SpriteProvider spriteWithAge) {
        super(world, x, y, z, 0.0D, 0.0D, 0.0D);
        this.spriteWithAge = spriteWithAge;
        this.maxAge = 4;
        this.scale = 1.0F;
        this.setSpriteForAge(spriteWithAge);
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else {
            this.setSpriteForAge(this.spriteWithAge);
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SweepParticleType> {
        private final SpriteProvider spriteProvider;

        // FabricSpriteProvider extends SpriteProvider, so this accepts what the
        // PendingParticleFactory registry injects while remaining compatible with
        // the rest of the particle internals that use SpriteProvider.
        public Factory(FabricSpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public @Nullable SweepAttackParticle createParticle(SweepParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
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