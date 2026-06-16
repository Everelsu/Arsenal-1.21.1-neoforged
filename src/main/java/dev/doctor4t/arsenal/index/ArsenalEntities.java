package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import dev.doctor4t.arsenal.entity.BloodScytheEntity;
import dev.doctor4t.arsenal.entity.WeaponRackEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ArsenalEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Arsenal.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<BloodScytheEntity>> BLOOD_SCYTHE =
            ENTITY_TYPES.register("blood_scythe", name -> EntityType.Builder.<BloodScytheEntity>of(BloodScytheEntity::new, MobCategory.MISC)
                    .noSave()
                    .sized(5.0f, 0.2f)
                    .build("blood_scythe"));

    public static final DeferredHolder<EntityType<?>, EntityType<AnchorbladeEntity>> ANCHORBLADE =
            ENTITY_TYPES.register("anchorblade", name -> EntityType.Builder.<AnchorbladeEntity>of(AnchorbladeEntity::new, MobCategory.MISC)
                    .noSave()
                    .sized(1.2f, 1.2f)
                    .build("anchorblade"));

    public static final DeferredHolder<EntityType<?>, EntityType<WeaponRackEntity>> WEAPON_RACK =
            ENTITY_TYPES.register("weapon_rack", name -> EntityType.Builder.<WeaponRackEntity>of(WeaponRackEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .build("weapon_rack"));

    private ArsenalEntities() {}
}
