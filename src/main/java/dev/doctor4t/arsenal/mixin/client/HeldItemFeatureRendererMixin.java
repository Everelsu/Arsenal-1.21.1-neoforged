package dev.doctor4t.arsenal.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.arsenal.index.ArsenalEnchantments;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.util.AnchorOwner;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class HeldItemFeatureRendererMixin {
    @Inject(method = "renderArmWithItem", at = @At(value = "HEAD"), cancellable = true)
    private void arsenal$thrown(LivingEntity entity, ItemStack stack, ItemDisplayContext transformationMode, HumanoidArm arm, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        if (stack.is(ArsenalItems.ANCHORBLADE)) {
            boolean reeling = ArsenalEnchantments.getLevel(ArsenalEnchantments.REELING, stack, entity.level()) > 0;
            if (entity instanceof AnchorOwner owner && owner.arsenal$isAnchorActive(entity.getMainHandItem().equals(stack) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND, reeling)) {
                ci.cancel();
            }
        }
    }
}
