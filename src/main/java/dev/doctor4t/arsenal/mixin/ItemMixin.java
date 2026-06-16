package dev.doctor4t.arsenal.mixin;

import dev.doctor4t.arsenal.cca.WeaponOwnerComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void arsenal$throw(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        Item item = (Item) (Object) this;
        if (item == Items.FIRE_CHARGE) {
            ItemStack stack = user.getItemInHand(hand);
            if (!world.isClientSide()) {
                world.levelEvent(null, 1018, user.blockPosition(), 0);
                Vec3 vec3d = user.getViewVector(1.0F).normalize().scale(2);
                SmallFireball smallFireballEntity = new SmallFireball(world, user, vec3d);
                smallFireballEntity.setPos(smallFireballEntity.getX(), user.getEyeY(), smallFireballEntity.getZ());
                world.addFreshEntity(smallFireballEntity);
                stack.shrink(1);
                user.getCooldowns().addCooldown(Items.FIRE_CHARGE, 6);
            }
            cir.setReturnValue(InteractionResultHolder.success(stack));
        }
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void arsenal$setTridentOwner(ItemStack stack, Level world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (stack.is(Items.TRIDENT) && entity instanceof Player player) {
            WeaponOwnerComponent.setOwner(stack, player.getUUID());
        }
    }
}
