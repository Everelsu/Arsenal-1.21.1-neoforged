package dev.doctor4t.arsenal.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ShockwaveParticle extends ExplosionLargeParticle {
    // Set to true for a camera-facing billboard (original behaviour).
    // Set to false for a flat ground-plane ring (looks correct from any angle).
    private static final boolean FACE_CAMERA = true;

    private final SpriteProvider spriteProvider;

    public ShockwaveParticle(ClientWorld world, double x, double y, double z, double d, SpriteProvider spriteProvider) {
        super(world, x, y, z, d, spriteProvider);
        this.spriteProvider = spriteProvider;
        this.maxAge = 8;
        this.scale = 5.6f; // reduced by 30% from 8f
        this.gravityStrength = 0;
        this.velocityX = 0;
        this.velocityY = 0;
        this.velocityZ = 0;
        this.red = 1;
        this.green = 1;
        this.blue = 1;
        this.alpha = 0.175f;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public float getSize(float tickDelta) {
        // FIX: was `scale * (age + tickDelta) / maxAge` — at age=0 tickDelta≈0 this returned
        // ~0.0, making the quad invisible on the first frame (and small for the first couple frames).
        // Now we start at a base of 0.2 (20% of full scale) and grow to 1.0, so the particle
        // is always visible as soon as it spawns.
        float progress = (this.age + tickDelta) / this.maxAge;
        return this.scale * (0.2f + 0.8f * MathHelper.clamp(progress, 0, 1));
    }

    @Override
    public void tick() {
        super.tick();
        // FIX: explicitly advance the sprite animation each tick.
        // super.tick() calls ExplosionLargeParticle.tick() which may or may not call
        // setSpriteForAge() depending on the 1.21.1 build. Calling it here guarantees
        // the texture animates through all 8 frames rather than staying stuck on frame 0.
        if (!this.dead) {
            this.setSpriteForAge(this.spriteProvider);
        }
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d vec3d = camera.getPos();
        float f = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - vec3d.getX());
        float g = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - vec3d.getY());
        float h = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - vec3d.getZ());

        float size = this.getSize(tickDelta);

        float lifeProgress = (float) this.age / this.maxAge;
        this.alpha = lifeProgress < 0.5f ? 0.175f : (float) MathHelper.lerp((lifeProgress - 0.5f) * 2.0f, 0.175f, 0.0f);

        Vector3f[] corners;
        if (FACE_CAMERA) {
            // Billboard mode: quad rotates to always face the camera (original behaviour).
            Quaternionf quaternion = camera.getRotation();
            corners = new Vector3f[]{new Vector3f(-1, 1, 0), new Vector3f(-1, -1, 0), new Vector3f(1, -1, 0), new Vector3f(1, 1, 0)};
            for (Vector3f corner : corners) {
                corner.rotate(quaternion);
                corner.mul(size);
                corner.add(f, g, h);
            }
        } else {
            // Ground-plane mode: flat ring on the XZ plane, looks correct from any angle.
            corners = new Vector3f[]{
                    new Vector3f(f - size, g, h - size),
                    new Vector3f(f - size, g, h + size),
                    new Vector3f(f + size, g, h + size),
                    new Vector3f(f + size, g, h - size),
            };
        }

        int brightness = this.getBrightness(tickDelta);
        this.vertex(vertexConsumer, corners[0], this.getMaxU(), this.getMaxV(), brightness);
        this.vertex(vertexConsumer, corners[1], this.getMaxU(), this.getMinV(), brightness);
        this.vertex(vertexConsumer, corners[2], this.getMinU(), this.getMinV(), brightness);
        this.vertex(vertexConsumer, corners[3], this.getMinU(), this.getMaxV(), brightness);
    }

    private void vertex(VertexConsumer vertexConsumer, Vector3f pos, float u, float v, int light) {
        vertexConsumer.vertex(pos.x(), pos.y(), pos.z()).texture(u, v).color(this.red, this.green, this.blue, this.alpha).light(light);
    }

    @Override
    public int getBrightness(float tint) {
        return 240;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType defaultParticleType, ClientWorld clientWorld, double d, double e, double f, double g, double h, double i) {
            return new ShockwaveParticle(clientWorld, d, e, f, g, this.spriteProvider);
        }
    }
}