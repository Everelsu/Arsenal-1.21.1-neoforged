package dev.doctor4t.arsenal.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    // In 1.21.1 the hotbar was converted from the old widgets.png UV atlas to the GUI sprite system.
    // drawTexture(WIDGETS_TEXTURE, x, y, u, v, w, h) no longer works — the old atlas layout is gone
    // and the file isn't bound to a texture slot at HUD render time, producing a black rectangle.
    // Use drawGuiTexture(Identifier, x, y, w, h) with the logical sprite names instead.
    // The sprite files live at assets/minecraft/textures/gui/sprites/hud/<name>.png.
    // hotbar_offhand_left.png = 29x24, hotbar_offhand_right.png = 29x24 (verified from MC jar).
    private static final Identifier HOTBAR_OFFHAND_LEFT_TEXTURE  = Identifier.ofVanilla("hud/hotbar_offhand_left");
    private static final Identifier HOTBAR_OFFHAND_RIGHT_TEXTURE = Identifier.ofVanilla("hud/hotbar_offhand_right");
    private static final Identifier HOTBAR_SELECTION_TEXTURE     = Identifier.ofVanilla("hud/hotbar_selection");

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    private void arsenal$renderWeaponSlot(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        PlayerEntity player = this.getCameraPlayer();
        if (player == null) return;
        ItemStack stack = BackWeaponComponent.getBackWeapon(player);
        if (!stack.isEmpty()) {
            int scaledWidth = context.getScaledWindowWidth();
            int scaledHeight = context.getScaledWindowHeight();
            int i = scaledWidth / 2;
            if (BackWeaponComponent.isHoldingBackWeapon(player)) {
                // Draw the selection highlight (24x23) above the hotbar using the sprite system.
                context.drawGuiTexture(HOTBAR_SELECTION_TEXTURE, i - 12, scaledHeight - 23 - 70, 24, 23);
                int o = i - 90 + 4 * 20 + 2;
                int p = scaledHeight - 19 - 70;
                this.renderHotbarItem(context, o, p, tickCounter, player, stack, 1);
            } else {
                Arm arm = player.getMainArm().getOpposite();
                // Draw the offhand slot using the correct sprite for the player's arm side (29x24).
                if (arm == Arm.RIGHT) {
                    context.drawGuiTexture(HOTBAR_OFFHAND_LEFT_TEXTURE, i - 91 - 29, scaledHeight - 23, 29, 24);
                } else {
                    context.drawGuiTexture(HOTBAR_OFFHAND_RIGHT_TEXTURE, i + 91, scaledHeight - 23, 29, 24);
                }
                int n = scaledHeight - 16 - 3;
                if (arm == Arm.RIGHT) {
                    this.renderHotbarItem(context, i - 91 - 26, n, tickCounter, player, stack, 0);
                } else {
                    this.renderHotbarItem(context, i + 91 + 10, n, tickCounter, player, stack, 0);
                }
            }
        }
    }

    // In 1.21.1 the hotbar selection is drawn with drawGuiTexture(Identifier,IIII)V at ordinal 1,
    // not drawTexture. Ordinal 0 = hotbar background, ordinal 1 = selection highlight.
    @WrapOperation(method = "renderHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 1))
    private void arsenal$selection(DrawContext instance, Identifier texture, int x, int y, int width, int height, Operation<Void> original) {
        if (this.getCameraPlayer() != null && BackWeaponComponent.isHoldingBackWeapon(this.getCameraPlayer())) {
            return;
        }
        original.call(instance, texture, x, y, width, height);
    }
}