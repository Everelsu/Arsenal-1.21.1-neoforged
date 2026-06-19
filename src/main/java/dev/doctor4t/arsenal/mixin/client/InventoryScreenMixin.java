package dev.doctor4t.arsenal.mixin.client;

import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import dev.doctor4t.arsenal.network.SetBackWeaponPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Back-weapon slot in the survival inventory. Rendered and interacted with entirely client-side
 * (mirroring the creative-screen handling) instead of injecting a Slot into the shared
 * InventoryMenu — the latter desynced and broke with other inventory-modifying mods.
 */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {
    private static final ResourceLocation ARSENAL$INVENTORY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");

    @Unique
    private boolean arsenal$hovering = false;

    public InventoryScreenMixin(InventoryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Unique
    private SimpleContainer arsenal$back() {
        Minecraft client = Minecraft.getInstance();
        return client.player == null ? null : BackWeaponComponent.getBackWeaponInventory(client.player);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void arsenal$drawSlot(GuiGraphics context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        SimpleContainer back = arsenal$back();
        if (back == null) return;
        int sx = this.leftPos + 76, sy = this.topPos + 43;
        context.blit(ARSENAL$INVENTORY_TEXTURE, sx, sy, 76, 61, 18, 18);
        if (arsenal$hovering) {
            context.fillGradient(sx + 1, sy + 1, sx + 17, sy + 17, 0x80FFFFFF, 0x80FFFFFF);
        }
        ItemStack stack = back.getItem(0);
        if (!stack.isEmpty()) {
            context.renderItem(stack, sx + 1, sy + 1);
            context.renderItemDecorations(this.font, stack, sx + 1, sy + 1);
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void arsenal$trackHover(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        arsenal$hovering = arsenal$isOverBackSlot(mouseX, mouseY);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void arsenal$click(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        SimpleContainer back = arsenal$back();
        if (back == null || !arsenal$isOverBackSlot(mouseX, mouseY)) return;
        if (button != 0 && button != 1) return;

        ItemStack cursor = this.menu.getCarried().copy();
        ItemStack inSlot = back.getItem(0).copy();
        ItemStack newSlot;

        if (button == 0) {
            this.menu.setCarried(inSlot);
            newSlot = cursor;
        } else {
            if (cursor.isEmpty()) {
                if (inSlot.isEmpty()) { cir.setReturnValue(true); return; }
                int half = (inSlot.getCount() + 1) / 2;
                this.menu.setCarried(inSlot.copyWithCount(half));
                newSlot = inSlot.copyWithCount(inSlot.getCount() - half);
            } else {
                if (!inSlot.isEmpty() && !ItemStack.isSameItemSameComponents(cursor, inSlot)) {
                    cir.setReturnValue(true);
                    return;
                }
                newSlot = cursor.copyWithCount(1);
                ItemStack remaining = cursor.copyWithCount(cursor.getCount() - 1);
                this.menu.setCarried(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            }
        }

        back.setItem(0, newSlot); // optimistic client update; server is authoritative via the payload
        PacketDistributor.sendToServer(new SetBackWeaponPayload(newSlot.copy()));
        cir.setReturnValue(true);
    }

    @Unique
    private boolean arsenal$isOverBackSlot(double mouseX, double mouseY) {
        int sx = this.leftPos + 76, sy = this.topPos + 43;
        return mouseX >= sx + 1 && mouseX < sx + 17 && mouseY >= sy + 1 && mouseY < sy + 17;
    }
}
