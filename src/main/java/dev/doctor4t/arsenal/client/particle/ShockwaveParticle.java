package dev.doctor4t.arsenal.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ShockwaveParticle extends HugeExplosionParticle {
    private static final boolean FACE_CAMERA = true;

    private final SpriteSet spriteProvider;

    public ShockwaveParticle(ClientLevel world, double x, double y, double z, double d, SpriteSet spriteProvider) {
        super(world, x, y, z, d, spriteProvider);
        this.spriteProvider = spriteProvider;
        this.lifetime = 8;
        this.quadSize = 5.6f;
        this.gravity = 0;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.rCol = 1;
        this.gCol = 1;
        this.bCol = 1;
        this.alpha = 0.175f;
        this.setSpriteFromAge(spriteProvider);
    }

    @Override
    public float getQuadSize(float tickDelta) {
        float progress = (this.age + tickDelta) / this.lifetime;
        return this.quadSize * (0.2f + 0.8f * Mth.clamp(progress, 0, 1));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.spriteProvider);
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3 vec3d = camera.getPosition();
        float f = (float) (Mth.lerp(tickDelta, this.xo, this.x) - vec3d.x());
        float g = (float) (Mth.lerp(tickDelta, this.yo, this.y) - vec3d.y());
        float h = (float) (Mth.lerp(tickDelta, this.zo, this.z) - vec3d.z());

        float size = this.getQuadSize(tickDelta);

        float lifeProgress = (float) this.age / this.lifetime;
        this.alpha = lifeProgress < 0.5f ? 0.175f : Mth.lerp((lifeProgress - 0.5f) * 2.0f, 0.175f, 0.0f);

        Vector3f[] corners;
        if (FACE_CAMERA) {
            Quaternionf quaternion = camera.rotation();
            corners = new Vector3f[]{new Vector3f(-1, 1, 0), new Vector3f(-1, -1, 0), new Vector3f(1, -1, 0), new Vector3f(1, 1, 0)};
            for (Vector3f corner : corners) {
                corner.rotate(quaternion);
                corner.mul(size);
                corner.add(f, g, h);
            }
        } else {
            corners = new Vector3f[]{
                    new Vector3f(f - size, g, h - size),
                    new Vector3f(f - size, g, h + size),
                    new Vector3f(f + size, g, h + size),
                    new Vector3f(f + size, g, h - size),
            };
        }

        int brightness = this.getLightColor(tickDelta);
        this.vertex(vertexConsumer, corners[0], this.getU1(), this.getV1(), brightness);
        this.vertex(vertexConsumer, corners[1], this.getU1(), this.getV0(), brightness);
        this.vertex(vertexConsumer, corners[2], this.getU0(), this.getV0(), brightness);
        this.vertex(vertexConsumer, corners[3], this.getU0(), this.getV1(), brightness);
    }

    private void vertex(VertexConsumer vertexConsumer, Vector3f pos, float u, float v, int light) {
        vertexConsumer.addVertex(pos.x(), pos.y(), pos.z()).setUv(u, v).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setLight(light);
    }

    @Override
    public int getLightColor(float tint) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteProvider;

        public Factory(SpriteSet spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType defaultParticleType, ClientLevel clientWorld, double d, double e, double f, double g, double h, double i) {
            return new ShockwaveParticle(clientWorld, d, e, f, g, this.spriteProvider);
        }
    }
}
