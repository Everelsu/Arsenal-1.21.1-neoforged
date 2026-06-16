package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.effect.StunStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public interface ArsenalStatusEffects {
    // registerReference registers the effect AND returns a live RegistryEntry<StatusEffect>
    // which is what all 1.21.1 LivingEntity status-effect APIs require.
    RegistryEntry<StatusEffect> STUN = register("stun", new StunStatusEffect());

    static void initialize() {
        // Calling this from Arsenal.onInitialize() triggers class loading,
        // which runs the field initialisers above and performs all registrations.
    }

    private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Arsenal.id(name), effect);
    }
}
