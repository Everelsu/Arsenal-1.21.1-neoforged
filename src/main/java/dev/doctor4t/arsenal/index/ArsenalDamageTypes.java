package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface ArsenalDamageTypes {
    ResourceKey<DamageType> ANCHOR = ResourceKey.create(Registries.DAMAGE_TYPE, Arsenal.id("anchor"));
    ResourceKey<DamageType> BLOOD_SCYTHE = ResourceKey.create(Registries.DAMAGE_TYPE, Arsenal.id("blood_scythe"));
    ResourceKey<DamageType> SPEWING = ResourceKey.create(Registries.DAMAGE_TYPE, Arsenal.id("spewing"));

    static DamageSource source(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
    }

    static DamageSource source(Level level, ResourceKey<DamageType> key, Entity causingEntity) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), causingEntity);
    }

    static DamageSource source(Level level, ResourceKey<DamageType> key, Entity directEntity, Entity causingEntity) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), directEntity, causingEntity);
    }
}
