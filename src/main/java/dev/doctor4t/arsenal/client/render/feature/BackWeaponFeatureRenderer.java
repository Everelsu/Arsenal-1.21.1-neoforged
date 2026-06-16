package dev.doctor4t.arsenal.client.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import dev.doctor4t.arsenal.index.ArsenalTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BackWeaponFeatureRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public BackWeaponFeatureRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> context) {
        super(context);
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, AbstractClientPlayer abstractClientPlayerEntity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (BackWeaponComponent.isHoldingBackWeapon(abstractClientPlayerEntity)) return;
        ItemStack stack = BackWeaponComponent.getBackWeapon(abstractClientPlayerEntity);
        if (stack.isEmpty()) return;

        matrices.pushPose();

        boolean hasCape = abstractClientPlayerEntity.getSkin().capeTexture() != null
                && abstractClientPlayerEntity.isModelPartShown(PlayerModelPart.CAPE)
                && !abstractClientPlayerEntity.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
        boolean hasChestPlate = !abstractClientPlayerEntity.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
        matrices.translate(0.0F, 0.0F, 0.05F + (hasCape ? 0.05f : 0f) + (hasChestPlate ? .05f : 0f));
        double d = Mth.lerp(tickDelta, abstractClientPlayerEntity.xCloakO, abstractClientPlayerEntity.xCloak)
                - Mth.lerp(tickDelta, abstractClientPlayerEntity.xo, abstractClientPlayerEntity.getX());
        double e = Mth.lerp(tickDelta, abstractClientPlayerEntity.yCloakO, abstractClientPlayerEntity.yCloak)
                - Mth.lerp(tickDelta, abstractClientPlayerEntity.yo, abstractClientPlayerEntity.getY());
        double m = Mth.lerp(tickDelta, abstractClientPlayerEntity.zCloakO, abstractClientPlayerEntity.zCloak)
                - Mth.lerp(tickDelta, abstractClientPlayerEntity.zo, abstractClientPlayerEntity.getZ());
        float n = Mth.rotLerp(tickDelta, abstractClientPlayerEntity.yBodyRotO, abstractClientPlayerEntity.yBodyRot);
        double o = Mth.sin(n * (float) (Math.PI / 180.0));
        double p = (-Mth.cos(n * (float) (Math.PI / 180.0)));
        float q = (float) e * 10.0F;
        q = Mth.clamp(q, -6.0F, 0f);
        float r = (float) (d * o + m * p) * 100.0F;
        r = Mth.clamp(r, 0.0F, 40.0F);
        float s = (float) (d * p - m * o) * 100.0F;
        s = Mth.clamp(s, -20.0F, 20.0F);
        if (r < 0.0F) {
            r = 0.0F;
        }

        float t = Mth.lerp(tickDelta, abstractClientPlayerEntity.oBob, abstractClientPlayerEntity.bob);
        q += Mth.sin(Mth.lerp(tickDelta, abstractClientPlayerEntity.walkDistO, abstractClientPlayerEntity.walkDist) * 6.0F) * 32.0F * t;
        if (abstractClientPlayerEntity.isCrouching()) {
            q += 25.0F;
        }

        matrices.mulPose(Axis.XP.rotationDegrees(6f + r / 2.0F + q));
        matrices.mulPose(Axis.ZP.rotationDegrees(s / 2.0F));
        matrices.mulPose(Axis.YP.rotationDegrees(180.0F - s / 2.0F));

        float scale = .85f;
        if (stack.is(ArsenalTags.BIG_WEAPONS)) {
            scale = 1.6f;
            matrices.translate(0.0, 0.2, -0.15);
        } else {
            matrices.translate(0, 0.3, -0.1);
        }
        if (stack.is(ArsenalTags.SHIELDS)) {
            scale = 1.8f;
            matrices.translate(0.0, 0.2, 0.0);
        }

        matrices.scale(scale, scale, scale);

        Minecraft.getInstance().getItemRenderer().renderStatic(abstractClientPlayerEntity, stack, ItemDisplayContext.FIXED, false, matrices, vertexConsumers, abstractClientPlayerEntity.level(), light, OverlayTexture.NO_OVERLAY, 0);
        matrices.popPose();
    }
}
