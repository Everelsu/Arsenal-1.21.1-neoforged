package dev.doctor4t.arsenal.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {
    private static final ResourceLocation ARSENAL$INVENTORY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");

    public InventoryScreenMixin(InventoryMenu screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "renderBg", at = @At(value = "TAIL"))
    private void arsenal$drawSlots(GuiGraphics context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        int i = this.leftPos + 76;
        int j = this.topPos + 43;
        context.blit(ARSENAL$INVENTORY_TEXTURE, i, j, 76, 61, 18, 18);
    }
}
