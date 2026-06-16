package dev.doctor4t.arsenal.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import dev.doctor4t.arsenal.index.ArsenalCosmetics;
import dev.doctor4t.arsenal.item.AnchorbladeItem;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class AnchorbladeEntityRenderer extends EntityRenderer<AnchorbladeEntity> {
    private final ItemRenderer itemRenderer;
    private final ModelManager bakedModelManager;

    public AnchorbladeEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
        this.bakedModelManager = ctx.getModelManager();
    }

    public static ModelResourceLocation standalone(ResourceLocation location) {
        return ModelResourceLocation.standalone(location);
    }

    @Override
    public void render(AnchorbladeEntity anchorbladeEntity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
        float yawAngle = Mth.lerp(tickDelta, anchorbladeEntity.yRotO, anchorbladeEntity.getYRot());
        float pitchAngle = Mth.lerp(tickDelta, anchorbladeEntity.xRotO, anchorbladeEntity.getXRot());

        matrices.pushPose();
        matrices.translate(0, .6, 0);

        float scale = 1.6f;
        matrices.scale(scale, scale, scale);

        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yawAngle + 90));
        matrices.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(-pitchAngle + 45));

        BakedModel model = this.bakedModelManager.getModel(standalone(AnchorbladeItem.Skin.DEFAULT.anchorbladeEntityModel));
        RenderType chainLayer = RenderType.entitySmoothCutout(AnchorbladeItem.Skin.DEFAULT.chainTexture);
        ItemStack stack = anchorbladeEntity.getStack();
        AnchorbladeItem.Skin skin = AnchorbladeItem.Skin.fromString(ArsenalCosmetics.getSkin(stack));
        if (skin != null) {
            model = this.bakedModelManager.getModel(standalone(skin.anchorbladeEntityModel));
            chainLayer = RenderType.entitySmoothCutout(skin.chainTexture);
        }
        this.itemRenderer.render(stack, ItemDisplayContext.FIXED, false, matrices, vertexConsumers, light, OverlayTexture.NO_OVERLAY, model);

        matrices.popPose();

        if (anchorbladeEntity.getOwner() instanceof LivingEntity livingOwner) {
            matrices.pushPose();
            Vec3 ringPos = new Vec3((skin == AnchorbladeItem.Skin.AMBESSA ? 0f : 1f), 0, 0).zRot(pitchAngle * Mth.DEG_TO_RAD).yRot((yawAngle + 90) * Mth.DEG_TO_RAD).add(0, anchorbladeEntity.getBbHeight() / 2f, 0);
            Vec3 pos = new Vec3(
                    Mth.lerp(tickDelta, anchorbladeEntity.xo, anchorbladeEntity.getX()),
                    Mth.lerp(tickDelta, anchorbladeEntity.yo, anchorbladeEntity.getY()),
                    Mth.lerp(tickDelta, anchorbladeEntity.zo, anchorbladeEntity.getZ()));
            Vec3 leashPos = livingOwner.getRopeHoldPosition(tickDelta);

            Vec3 ownerPos = leashPos.subtract(pos);

            float length = (float) ringPos.distanceTo(ownerPos);
            PoseStack.Pose matrixEntry = matrices.last();
            Matrix4f modelMatrix = matrixEntry.pose();
            float minU = 0;
            float maxU = 1;
            float minV = 0;
            float maxV = length / 8f;
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(chainLayer);
            Vec3 offset = ownerPos.subtract(ringPos).normalize().multiply(0.25, 0, 0.25).yRot((float) (Math.PI / 2));

            Vec3 vert1 = ringPos.add(offset);
            Vec3 vert2 = ownerPos.add(offset);
            Vec3 vert3 = ownerPos.subtract(offset);
            Vec3 vert4 = ringPos.subtract(offset);
            int chainLight = LightTexture.pack(this.getBlockLightLevel(anchorbladeEntity, livingOwner.blockPosition()), this.getSkyLightLevel(anchorbladeEntity, livingOwner.blockPosition()));
            this.vertex(vert1, vertexConsumer, minU, minV, matrixEntry, light);
            this.vertex(vert2, vertexConsumer, minU, maxV, matrixEntry, chainLight);
            this.vertex(vert3, vertexConsumer, maxU, maxV, matrixEntry, chainLight);
            this.vertex(vert4, vertexConsumer, maxU, minV, matrixEntry, light);
            matrices.popPose();
        }
    }

    private void vertex(Vec3 vec, VertexConsumer vertexConsumer, float u, float v, PoseStack.Pose entry, int light) {
        vertexConsumer.addVertex(entry.pose(), (float) vec.x, (float) vec.y, (float) vec.z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(entry, 0, 1, 0);
    }

    @Override
    public ResourceLocation getTextureLocation(AnchorbladeEntity entity) {
        return null;
    }
}
