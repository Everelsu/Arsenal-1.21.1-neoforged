package dev.doctor4t.arsenal.mixin.client;

import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;"))
    private void arsenal$inputSlot(CallbackInfo ci) {
        if (this.player != null) BackWeaponComponent.setHoldingBackWeapon(this.player, false);
    }

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z", ordinal = 1))
    private void arsenal$swapStop(CallbackInfo ci) {
        if (this.player != null) BackWeaponComponent.setHoldingBackWeapon(this.player, false);
    }

    @Inject(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;isHotbarSlot(I)Z"))
    private void arsenal$pickSlot(CallbackInfo ci) {
        if (this.player != null) BackWeaponComponent.setHoldingBackWeapon(this.player, false);
    }
}
