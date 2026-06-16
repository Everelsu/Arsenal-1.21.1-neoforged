package dev.doctor4t.arsenal.cca;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Stores the owner UUID on an ItemStack using vanilla DataComponentTypes.CUSTOM_DATA.
 * Replaces the CCA item component that was removed in CCA 6.0.
 */
public final class WeaponOwnerComponent {
    private static final String OWNER_KEY = "arsenal_owner";

    private WeaponOwnerComponent() {}

    public static @Nullable UUID getOwner(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return null;
        NbtCompound nbt = customData.copyNbt();
        if (!nbt.containsUuid(OWNER_KEY)) return null;
        return nbt.getUuid(OWNER_KEY);
    }

    public static void setOwner(ItemStack stack, UUID uuid) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
        nbt.putUuid(OWNER_KEY, uuid);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
}