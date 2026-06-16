package dev.doctor4t.arsenal.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import dev.doctor4t.arsenal.util.ProjectileSlotHolder;
import dev.doctor4t.arsenal.util.WeaponSlotHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TridentItem.class)
public class TridentItemMixin {
    @WrapOperation(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean arsenal$spawnEntity(Level world, Entity entity, Operation<Boolean> operation, @Local(ordinal = 0) ItemStack stack, @Local(ordinal = 0) LivingEntity user) {
        if (user instanceof Player player) {
            BackWeaponComponent backWeaponComponent = BackWeaponComponent.get(player);
            if (backWeaponComponent.getBackWeapon().is(Items.TRIDENT)) {
                backWeaponComponent.getBackWeapon().shrink(1);
            }

            if (player.getInventory() instanceof WeaponSlotHolder holder && entity instanceof ProjectileSlotHolder slotHolder) {
                int index = holder.arsenal$getSlotHolding(stack);
                if (index != -1) {
                    slotHolder.arsenal$setOwnedSlot(index);
                }
            }
        }
        return operation.call(world, entity);
    }
}
