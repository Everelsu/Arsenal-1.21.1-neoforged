package dev.doctor4t.arsenal.index;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Weapon-skin cosmetics.
 *
 * <p>Skins are stored per-ItemStack in vanilla {@code CUSTOM_DATA} and may be changed
 * by anyone (no supporter/subscription gate). The original Fabric mod backed skins with
 * the "datasync" supporter service, which has no NeoForge port; this local per-stack
 * storage replaces it.
 */
public interface ArsenalCosmetics {
    String DEFAULT_SKIN = "default";
    String SKIN_KEY = "arsenal_skin";

    static String getSkin(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return DEFAULT_SKIN;
        CompoundTag tag = data.copyTag();
        return tag.contains(SKIN_KEY) ? tag.getString(SKIN_KEY) : DEFAULT_SKIN;
    }

    static void setSkin(ItemStack stack, String skinName) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(SKIN_KEY, skinName);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
