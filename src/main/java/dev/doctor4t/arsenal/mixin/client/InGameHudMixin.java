package dev.doctor4t.arsenal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class InGameHudMixin {
    private static final ResourceLocation HOTBAR_OFFHAND_LEFT_TEXTURE  = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left");
    private static final ResourceLocation HOTBAR_OFFHAND_RIGHT_TEXTURE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_right");
    private static final ResourceLocation HOTBAR_SELECTION_TEXTURE     = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract void renderSlot(GuiGraphics context, int x, int y, DeltaTracker tickCounter, Player player, ItemStack stack, int seed);

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void arsenal$renderWeaponSlot(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        Player player = this.getCameraPlayer();
        if (player == null) return;
        ItemStack stack = BackWeaponComponent.getBackWeapon(player);
        if (!stack.isEmpty()) {
            int scaledWidth = context.guiWidth();
            int scaledHeight = context.guiHeight();
            int i = scaledWidth / 2;
            if (BackWeaponComponent.isHoldingBackWeapon(player)) {
                context.blitSprite(HOTBAR_SELECTION_TEXTURE, i - 12, scaledHeight - 23 - 70, 24, 23);
                int o = i - 90 + 4 * 20 + 2;
                int p = scaledHeight - 19 - 70;
                this.renderSlot(context, o, p, tickCounter, player, stack, 1);
            } else {
                HumanoidArm arm = player.getMainArm().getOpposite();
                if (arm == HumanoidArm.RIGHT) {
                    context.blitSprite(HOTBAR_OFFHAND_LEFT_TEXTURE, i - 91 - 29, scaledHeight - 23, 29, 24);
                } else {
                    context.blitSprite(HOTBAR_OFFHAND_RIGHT_TEXTURE, i + 91, scaledHeight - 23, 29, 24);
                }
                int n = scaledHeight - 16 - 3;
                if (arm == HumanoidArm.RIGHT) {
                    this.renderSlot(context, i - 91 - 26, n, tickCounter, player, stack, 0);
                } else {
                    this.renderSlot(context, i + 91 + 10, n, tickCounter, player, stack, 0);
                }
            }
        }
    }

    @WrapOperation(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 1))
    private void arsenal$selection(GuiGraphics instance, ResourceLocation texture, int x, int y, int width, int height, Operation<Void> original) {
        if (this.getCameraPlayer() != null && BackWeaponComponent.isHoldingBackWeapon(this.getCameraPlayer())) {
            return;
        }
        original.call(instance, texture, x, y, width, height);
    }
}
