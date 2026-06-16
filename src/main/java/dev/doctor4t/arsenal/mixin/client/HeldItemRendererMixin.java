package dev.doctor4t.arsenal.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.doctor4t.arsenal.index.ArsenalEnchantments;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.util.AnchorOwner;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class HeldItemRendererMixin {
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    public void arsenal$renderAnchor(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ArsenalItems.ANCHORBLADE)) {
            boolean reeling = ArsenalEnchantments.getLevel(ArsenalEnchantments.REELING, stack, player.level()) > 0;
            if (player instanceof AnchorOwner owner && owner.arsenal$isAnchorActive(hand, reeling)) {
                ci.cancel();
            }
        }
    }
}
