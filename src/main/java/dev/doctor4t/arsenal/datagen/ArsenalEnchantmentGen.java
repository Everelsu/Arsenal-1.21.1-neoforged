package dev.doctor4t.arsenal.datagen;

import dev.doctor4t.arsenal.index.ArsenalEnchantments;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ArsenalEnchantmentGen extends FabricDynamicRegistryProvider {
    public ArsenalEnchantmentGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        addEnchantment(registries, entries, ArsenalEnchantments.SPEWING);
        addEnchantment(registries, entries, ArsenalEnchantments.REELING);
    }

    private static void addEnchantment(RegistryWrapper.WrapperLookup registries,
                                       Entries entries,
                                       RegistryKey<Enchantment> key) {
        // WrapperLookup.getWrapperOrThrow() returns a RegistryWrapper.Impl<T> (a lookup wrapper).
        // Then getOptional(key) gives Optional<RegistryEntry.Reference<Enchantment>>.
        registries.getWrapperOrThrow(RegistryKeys.ENCHANTMENT)
                  .getOptional(key)
                  .ifPresent(entry -> entries.add(key, entry.value()));
    }

    @Override
    public String getName() {
        return "Arsenal Enchantments";
    }
}
