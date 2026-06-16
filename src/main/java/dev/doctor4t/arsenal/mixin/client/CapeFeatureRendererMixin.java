package dev.doctor4t.arsenal.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CapeLayer.class)
public class CapeFeatureRendererMixin {
    @ModifyConstant(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", constant = @Constant(floatValue = 32.0f, ordinal = 0))
    public float arsenal$clampCapeRotationR(float constant, @Local(argsOnly = true) AbstractClientPlayer abstractClientPlayerEntity) {
        return (!BackWeaponComponent.getBackWeapon(abstractClientPlayerEntity).isEmpty() && !BackWeaponComponent.isHoldingBackWeapon(abstractClientPlayerEntity)) ? 0f : constant;
    }

    @ModifyConstant(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V", constant = @Constant(floatValue = 150.0F, ordinal = 0))
    public float arsenal$clampCapeRotationQ(float constant, @Local(argsOnly = true) AbstractClientPlayer abstractClientPlayerEntity) {
        return (!BackWeaponComponent.getBackWeapon(abstractClientPlayerEntity).isEmpty() && !BackWeaponComponent.isHoldingBackWeapon(abstractClientPlayerEntity)) ? 40f : constant;
    }
}
