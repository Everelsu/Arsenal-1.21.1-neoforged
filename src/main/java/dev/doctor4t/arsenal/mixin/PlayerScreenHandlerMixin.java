package dev.doctor4t.arsenal.mixin;

import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import dev.doctor4t.arsenal.util.BackSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public abstract class PlayerScreenHandlerMixin extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
    public PlayerScreenHandlerMixin(MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void arsenal$init(Inventory inventory, boolean onServer, Player owner, CallbackInfo ci) {
        this.addSlot(new BackSlot(BackWeaponComponent.getBackWeaponInventory(inventory.player), 0, 77, 44));
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void arsenal$quickMove(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index < 0 || index >= this.slots.size()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        Slot slot = this.slots.get(index);
        if (!(slot instanceof BackSlot)) return;

        ItemStack held = slot.getItem();
        if (held.isEmpty()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        ItemStack copy = held.copy();
        if (!this.moveItemStackTo(held, 36, 45, false) && !this.moveItemStackTo(held, 9, 36, false)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        if (held.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        cir.setReturnValue(copy);
    }
}
