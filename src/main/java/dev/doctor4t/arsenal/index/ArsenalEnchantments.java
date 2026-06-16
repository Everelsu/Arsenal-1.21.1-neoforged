package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public final class ArsenalEnchantments {
    public static final ResourceKey<Enchantment> SPEWING =
            ResourceKey.create(Registries.ENCHANTMENT, Arsenal.id("spewing"));
    public static final ResourceKey<Enchantment> REELING =
            ResourceKey.create(Registries.ENCHANTMENT, Arsenal.id("reeling"));

    private ArsenalEnchantments() {}

    public static int getLevel(ResourceKey<Enchantment> key, ItemStack stack, Level level) {
        return level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(key)
                .map(entry -> EnchantmentHelper.getItemEnchantmentLevel(entry, stack))
                .orElse(0);
    }

    public static int getEquipmentLevel(ResourceKey<Enchantment> key, LivingEntity entity) {
        return entity.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(key)
                .map(entry -> EnchantmentHelper.getEnchantmentLevel(entry, entity))
                .orElse(0);
    }
}
