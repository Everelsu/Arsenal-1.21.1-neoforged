package dev.doctor4t.arsenal.mixin;

import dev.doctor4t.arsenal.util.WeaponSlotHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
public abstract class PersistentProjectileEntityMixin extends ProjectileEntityMixin {
    @Shadow
    public AbstractArrow.Pickup pickup;

    @Shadow
    protected abstract ItemStack getPickupItem();

    @Inject(method = "tryPickup", at = @At("HEAD"), cancellable = true)
    private void arsenal$pickupSlot(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (this.arsenal$getOwnedSlot() == -1) return;
        if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
            if (player.getInventory() instanceof WeaponSlotHolder holder) {
                if (holder.arsenal$tryInsertIntoSlot(this.arsenal$getOwnedSlot(), this.getPickupItem())) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
