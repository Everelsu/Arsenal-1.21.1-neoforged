package dev.doctor4t.arsenal.mixin;

import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class PlayerInventoryMixin {
    @Shadow
    @Final
    public Player player;

    @Inject(method = "getSelected", at = @At("HEAD"), cancellable = true)
    private void arsenal$mainHandSlot(CallbackInfoReturnable<ItemStack> cir) {
        if (BackWeaponComponent.isHoldingBackWeapon(this.player)) {
            if (!BackWeaponComponent.getBackWeapon(this.player).isEmpty()) {
                cir.setReturnValue(BackWeaponComponent.getBackWeapon(this.player));
            } else {
                BackWeaponComponent.setHoldingBackWeapon(this.player, false);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void arsenal$selectSlot(CallbackInfo ci) {
        if (!BackWeaponComponent.getBackWeapon(this.player).isEmpty()) {
            BackWeaponComponent.getBackWeapon(this.player).inventoryTick(this.player.level(), this.player, 0, BackWeaponComponent.isHoldingBackWeapon(this.player));
        }
    }

    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void arsenal$slotBreaking(BlockState block, CallbackInfoReturnable<Float> cir) {
        if (BackWeaponComponent.isHoldingBackWeapon(this.player)) {
            if (!BackWeaponComponent.getBackWeapon(this.player).isEmpty()) {
                cir.setReturnValue(BackWeaponComponent.getBackWeapon(this.player).getDestroySpeed(block));
            } else {
                BackWeaponComponent.setHoldingBackWeapon(this.player, false);
            }
        }
    }

    @Inject(method = "setPickedItem", at = @At("HEAD"))
    private void arsenal$nonPick(ItemStack stack, CallbackInfo ci) {
        BackWeaponComponent.setHoldingBackWeapon(this.player, false);
    }

    @Inject(method = "pickSlot", at = @At("HEAD"))
    private void arsenal$nonSwap(int slot, CallbackInfo ci) {
        BackWeaponComponent.setHoldingBackWeapon(this.player, false);
    }

    @Inject(method = "swapPaint", at = @At("HEAD"))
    private void arsenal$nonScroll(double scrollAmount, CallbackInfo ci) {
        BackWeaponComponent.setHoldingBackWeapon(this.player, false);
    }
}
