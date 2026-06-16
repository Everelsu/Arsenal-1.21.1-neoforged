package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import dev.doctor4t.arsenal.entity.BloodScytheEntity;
import dev.doctor4t.arsenal.entity.WeaponRackEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ArsenalEntities {
    Map<EntityType<? extends Entity>, Identifier> ENTITIES = new LinkedHashMap<>();

    // FIX: dimensions() takes (float width, float height) directly.
    // FIX: trackRangeChunks/trackRangeBlocks both don't exist in 1.21.1's EntityType.Builder.
    // The tracking range is now configured via the entity's SpawnGroup or left at the default.
    // trackedUpdateRate() is also removed. Simply omit these builder calls.
    EntityType<BloodScytheEntity> BLOOD_SCYTHE = createEntity("blood_scythe",
            EntityType.Builder.<BloodScytheEntity>create(BloodScytheEntity::new, SpawnGroup.MISC)
                    .disableSaving()
                    .dimensions(5.0f, 0.2f)
                    .build(Arsenal.MOD_ID + ":blood_scythe"));

    EntityType<AnchorbladeEntity> ANCHORBLADE = createEntity("anchorblade",
            EntityType.Builder.<AnchorbladeEntity>create(AnchorbladeEntity::new, SpawnGroup.MISC)
                    .disableSaving()
                    .dimensions(1.2f, 1.2f)
                    .build(Arsenal.MOD_ID + ":anchorblade"));

    EntityType<WeaponRackEntity> WEAPON_RACK = createEntity("weapon_rack",
            EntityType.Builder.<WeaponRackEntity>create(WeaponRackEntity::new, SpawnGroup.MISC)
                    .dimensions(0.4F, 0.4F)
                    .build(Arsenal.MOD_ID + ":weapon_rack"));

    private static <T extends EntityType<? extends Entity>> T createEntity(String name, T entity) {
        ENTITIES.put(entity, Identifier.of(Arsenal.MOD_ID, name));
        return entity;
    }

    static void initialize() {
        ENTITIES.keySet().forEach(entityType ->
                Registry.register(Registries.ENTITY_TYPE, ENTITIES.get(entityType), entityType));
    }
}
