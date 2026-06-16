package dev.doctor4t.arsenal.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.entity.BloodScytheEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BloodScytheEntityRenderer<T extends BloodScytheEntity> extends EntityRenderer<T> {
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Arsenal.MOD_ID, "textures/entity/blood_scythe.png");

    public BloodScytheEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T bloodScythe, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i) {
        matrixStack.pushPose();
        matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(g, bloodScythe.yRotO, bloodScythe.getYRot()) - 90.0f));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(g, bloodScythe.xRotO, bloodScythe.getXRot())));
        matrixStack.mulPose(Axis.XP.rotationDegrees(45.0f));
        matrixStack.scale(0.4f, 0.4f, 0.4f);
        matrixStack.translate(-4.0, 0.0, 0.0);
        for (int u = 0; u < 4; ++u) {
            matrixStack.mulPose(Axis.XP.rotationDegrees(90f));
        }
        matrixStack.popPose();
        super.render(bloodScythe, f, g, matrixStack, vertexConsumerProvider, i);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }

    public void vertex(PoseStack.Pose entry, VertexConsumer vertexConsumer, int x, int y, int z, float u, float v, int normalX, int normalZ, int normalY, int light) {
        vertexConsumer.addVertex(entry.pose(), x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(entry, normalX, normalY, normalZ);
    }
}
