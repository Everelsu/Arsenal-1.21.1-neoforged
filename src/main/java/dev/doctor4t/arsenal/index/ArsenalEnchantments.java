package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public final class ArsenalEnchantments {
    public static final RegistryKey<Enchantment> SPEWING =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, Arsenal.id("spewing"));
    public static final RegistryKey<Enchantment> REELING =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, Arsenal.id("reeling"));

    private ArsenalEnchantments() {}

    public static int getLevel(RegistryKey<Enchantment> key, ItemStack stack, World world) {
        // DynamicRegistryManager.get() returns the Registry<Enchantment>,
        // then getEntry(RegistryKey) gives the RegistryEntry for EnchantmentHelper.
        return world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .getEntry(key)
                .map(entry -> EnchantmentHelper.getLevel(entry, stack))
                .orElse(0);
    }

    public static int getEquipmentLevel(RegistryKey<Enchantment> key, LivingEntity entity) {
        return entity.getWorld().getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .getEntry(key)
                .map(entry -> EnchantmentHelper.getEquipmentLevel(entry, entity))
                .orElse(0);
    }
}
