package dev.doctor4t.arsenal.mixin;

import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import dev.doctor4t.arsenal.util.BackSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerScreenHandler.class)
// FIX: AbstractRecipeScreenHandler first type param must extend RecipeInput.
// CraftingInventory no longer implements RecipeInput in 1.21.1 — use CraftingRecipeInput instead.
public abstract class PlayerScreenHandlerMixin extends AbstractRecipeScreenHandler<CraftingRecipeInput, CraftingRecipe> {
    public PlayerScreenHandlerMixin(ScreenHandlerType<?> screenHandlerType, int i) {
        super(screenHandlerType, i);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void arsenal$init(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
        this.addSlot(new BackSlot(BackWeaponComponent.getBackWeaponInventory(inventory.player), 0, 77, 44));
    }

    // PlayerScreenHandler.quickMove() only handles slots 0-45. Our BackSlot has id=46.
    // Without this override, shift-clicking the back slot silently returns EMPTY and does nothing.
    // This override swaps the back slot item directly into the first available hotbar/inventory slot
    // (or vice versa), consistent with how other equipment slots behave on shift-click.
    @Inject(method = "quickMove", at = @At("HEAD"), cancellable = true)
    private void arsenal$quickMove(PlayerEntity player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index < 0 || index >= this.slots.size()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        Slot slot = this.slots.get(index);
        if (!(slot instanceof BackSlot)) return; // let vanilla handle all other slots

        // Shift-clicking the back slot: move its item into the player's inventory (slots 9-44).
        ItemStack held = slot.getStack();
        if (held.isEmpty()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        ItemStack copy = held.copy();
        // Try to insert into hotbar (slots 36-44) then main inventory (slots 9-35).
        if (!this.insertItem(held, 36, 45, false) && !this.insertItem(held, 9, 36, false)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        if (held.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        cir.setReturnValue(copy);
    }
}