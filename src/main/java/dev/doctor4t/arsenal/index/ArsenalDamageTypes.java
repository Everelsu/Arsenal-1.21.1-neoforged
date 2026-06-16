package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public interface ArsenalDamageTypes {
    ResourceKey<DamageType> ANCHOR = ResourceKey.create(Registries.DAMAGE_TYPE, Arsenal.id("anchor"));
    ResourceKey<DamageType> BLOOD_SCYTHE = ResourceKey.create(Registries.DAMAGE_TYPE, Arsenal.id("blood_scythe"));
    ResourceKey<DamageType> SPEWING = ResourceKey.create(Registries.DAMAGE_TYPE, Arsenal.id("spewing"));
}
