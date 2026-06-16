package dev.doctor4t.arsenal.cca;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Stores the owner UUID on an ItemStack using vanilla DataComponents.CUSTOM_DATA.
 */
public final class WeaponOwnerComponent {
    private static final String OWNER_KEY = "arsenal_owner";

    private WeaponOwnerComponent() {}

    public static @Nullable UUID getOwner(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        CompoundTag nbt = customData.copyTag();
        if (!nbt.hasUUID(OWNER_KEY)) return null;
        return nbt.getUUID(OWNER_KEY);
    }

    public static void setOwner(ItemStack stack, UUID uuid) {
        CompoundTag nbt = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        nbt.putUUID(OWNER_KEY, uuid);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }
}
