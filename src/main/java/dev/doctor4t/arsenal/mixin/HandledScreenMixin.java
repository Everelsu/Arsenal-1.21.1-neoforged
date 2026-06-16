package dev.doctor4t.arsenal.mixin;

import dev.doctor4t.arsenal.client.ArsenalClient;
import dev.doctor4t.arsenal.network.SwapInventoryPayload;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class HandledScreenMixin<T extends AbstractContainerMenu> {
    @Shadow
    @Nullable
    protected Slot hoveredSlot;
    @Shadow
    @Final
    protected T menu;

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void arsenal$onMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.hoveredSlot != null && this.menu.getCarried().isEmpty()) {
            if (ArsenalClient.swapKeybind.matchesMouse(button)) {
                PacketDistributor.sendToServer(new SwapInventoryPayload(this.hoveredSlot.index));
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "checkHotbarKeyPressed", at = @At("HEAD"), cancellable = true)
    private void arsenal$handleHotbarKeyPressed(int keyCode, int scanCode, CallbackInfoReturnable<Boolean> cir) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null) {
            if (ArsenalClient.swapKeybind.matchesKey(keyCode, scanCode)) {
                PacketDistributor.sendToServer(new SwapInventoryPayload(this.hoveredSlot.index));
                cir.setReturnValue(true);
            }
        }
    }
}
