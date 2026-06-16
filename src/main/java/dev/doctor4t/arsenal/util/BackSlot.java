package dev.doctor4t.arsenal.util;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class BackSlot extends Slot {
    public BackSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        // Accept all items in the back slot (survival inventory).
        // Tag-based filtering for rendering is handled separately by BackWeaponFeatureRenderer.
        return true;
    }
}