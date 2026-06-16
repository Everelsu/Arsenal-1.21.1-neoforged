package dev.doctor4t.arsenal.mixin.client;

import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import dev.doctor4t.arsenal.network.SetBackWeaponPayload;
import dev.doctor4t.arsenal.util.BackSlot;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    @Shadow private static ItemGroup selectedTab;

    @Unique private BackSlot arsenal$backSlot = null;
    @Unique private boolean arsenal$hovering = false;

    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
    }

    @Unique
    private boolean arsenal$isInventoryTab() {
        RegistryKey<ItemGroup> key = Registries.ITEM_GROUP.getKey(selectedTab).orElse(null);
        return ItemGroups.INVENTORY.equals(key);
    }

    /**
     * Rebuild our BackSlot when the tab changes.
     * We do NOT add it to handler.slots — vanilla's getSlotAt/onMouseClick/clickSlot
     * pipeline cannot handle a slot it doesn't own server-side, causing index OOB crashes
     * and slot corruption. We own hit-testing and interaction entirely ourselves.
     */
    @Inject(method = "setSelectedTab", at = @At("RETURN"))
    private void arsenal$rebuildBackSlot(ItemGroup group, CallbackInfo ci) {
        arsenal$backSlot = null;
        if (!arsenal$isInventoryTab()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // PlayerScreenHandlerMixin injects a BackSlot into PlayerScreenHandler at index 46.
        // setSelectedTab(INVENTORY) copies ALL playerScreenHandler slots into the creative
        // handler as LockableSlot wrappers — including our BackSlot. Vanilla then draws it,
        // causing the visual duplicate. Remove it from the creative handler slot list here
        // since we render and handle it entirely ourselves.
        var backInv = BackWeaponComponent.getBackWeaponInventory(client.player);
        this.handler.slots.removeIf(slot -> slot.inventory == backInv);

        // Position: texture border at (127,20), slot interior 1px inside = (128,21).
        arsenal$backSlot = new BackSlot(backInv, 0, 128, 21);
    }

    /**
     * Draw the slot background and item ourselves since the slot isn't in handler.slots.
     */
    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void arsenal$drawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!arsenal$isInventoryTab() || arsenal$backSlot == null) return;
        int sx = this.x + 127, sy = this.y + 20;
        // Slot border frame
        context.drawTexture(HandledScreen.BACKGROUND_TEXTURE, sx, sy, 76, 61, 18, 18);
        // Highlight when hovered (same colour HandledScreen uses: white 0x80FFFFFF)
        if (arsenal$hovering) {
            context.fillGradient(sx + 1, sy + 1, sx + 17, sy + 17, 0x80FFFFFF, 0x80FFFFFF);
        }
        // Draw the item in the slot
        ItemStack stack = arsenal$backSlot.getStack();
        if (!stack.isEmpty()) {
            context.drawItem(stack, sx + 1, sy + 1);
            context.drawItemInSlot(this.textRenderer, stack, sx + 1, sy + 1);
        }
    }

    /**
     * Track hover state so we can highlight the slot in drawBackground.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void arsenal$trackHover(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        arsenal$hovering = arsenal$isOverBackSlot(mouseX, mouseY);
    }

    /**
     * Handle clicks on our back slot entirely ourselves.
     * Returning true from mouseClicked cancels vanilla processing for this click.
     */
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void arsenal$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!arsenal$isInventoryTab() || arsenal$backSlot == null) return;
        if (!arsenal$isOverBackSlot(mouseX, mouseY)) return;
        if (button != 0 && button != 1) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) { cir.setReturnValue(true); return; }

        ItemStack cursor = this.handler.getCursorStack().copy();
        ItemStack inSlot = arsenal$backSlot.getStack().copy();
        ItemStack newSlot;

        if (button == 0) {
            // Left click: full swap
            this.handler.setCursorStack(inSlot);
            newSlot = cursor;
        } else {
            // Right click: put one from cursor into slot, or take half if cursor empty
            if (cursor.isEmpty()) {
                if (inSlot.isEmpty()) { cir.setReturnValue(true); return; }
                int half = (inSlot.getCount() + 1) / 2;
                this.handler.setCursorStack(inSlot.copyWithCount(half));
                newSlot = inSlot.copyWithCount(inSlot.getCount() - half);
            } else {
                if (!inSlot.isEmpty() && !ItemStack.areItemsEqual(cursor, inSlot)) {
                    // Different item — do nothing
                    cir.setReturnValue(true);
                    return;
                }
                newSlot = cursor.copyWithCount(1);
                ItemStack remaining = cursor.copyWithCount(cursor.getCount() - 1);
                this.handler.setCursorStack(remaining.isEmpty() ? ItemStack.EMPTY : remaining);
            }
        }

        // Update client-side immediately for responsive feel
        arsenal$backSlot.setStack(newSlot);
        // Tell server; BackWeaponComponent (CCA AutoSynced) propagates back to all clients
        ClientPlayNetworking.send(new SetBackWeaponPayload(newSlot.copy()));

        cir.setReturnValue(true);
    }

    @Unique
    private boolean arsenal$isOverBackSlot(double mouseX, double mouseY) {
        if (arsenal$backSlot == null) return false;
        int sx = this.x + arsenal$backSlot.x - 1; // -1 for the border
        int sy = this.y + arsenal$backSlot.y - 1;
        return mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18;
    }
}