package dev.doctor4t.arsenal.mixin.client;

import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import dev.doctor4t.arsenal.network.SetBackWeaponPayload;
import dev.doctor4t.arsenal.util.BackSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {
    @Shadow private static CreativeModeTab selectedTab;

    private static final ResourceLocation ARSENAL$INVENTORY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");

    @Unique private BackSlot arsenal$backSlot = null;
    @Unique private boolean arsenal$hovering = false;

    public CreativeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu handler, Inventory inv, Component title) {
        super(handler, inv, title);
    }

    @Unique
    private boolean arsenal$isInventoryTab() {
        ResourceKey<CreativeModeTab> key = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(selectedTab).orElse(null);
        return CreativeModeTabs.INVENTORY.equals(key);
    }

    @Inject(method = "selectTab", at = @At("RETURN"))
    private void arsenal$rebuildBackSlot(CreativeModeTab group, CallbackInfo ci) {
        arsenal$backSlot = null;
        if (!arsenal$isInventoryTab()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        var backInv = BackWeaponComponent.getBackWeaponInventory(client.player);
        this.menu.slots.removeIf(slot -> slot.container == backInv);

        arsenal$backSlot = new BackSlot(backInv, 0, 128, 21);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void arsenal$drawBackground(GuiGraphics context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!arsenal$isInventoryTab() || arsenal$backSlot == null) return;
        int sx = this.leftPos + 127, sy = this.topPos + 20;
        context.blit(ARSENAL$INVENTORY_TEXTURE, sx, sy, 76, 61, 18, 18);
        if (arsenal$hovering) {
            context.fillGradient(sx + 1, sy + 1, sx + 17, sy + 17, 0x80FFFFFF, 0x80FFFFFF);
        }
        ItemStack stack = arsenal$backSlot.getItem();
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
    private void arsenal$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!arsenal$isInventoryTab() || arsenal$backSlot == null) return;
        if (!arsenal$isOverBackSlot(mouseX, mouseY)) return;
        if (button != 0 && button != 1) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) { cir.setReturnValue(true); return; }

        ItemStack cursor = this.menu.getCarried().copy();
        ItemStack inSlot = arsenal$backSlot.getItem().copy();
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

        arsenal$backSlot.set(newSlot);
        PacketDistributor.sendToServer(new SetBackWeaponPayload(newSlot.copy()));

        cir.setReturnValue(true);
    }

    @Unique
    private boolean arsenal$isOverBackSlot(double mouseX, double mouseY) {
        if (arsenal$backSlot == null) return false;
        int sx = this.leftPos + arsenal$backSlot.x - 1;
        int sy = this.topPos + arsenal$backSlot.y - 1;
        return mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18;
    }
}
