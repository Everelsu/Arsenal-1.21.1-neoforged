package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ArsenalSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Arsenal.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SCYTHE_HIT = create("item.scythe.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_SCYTHE_SPEWING = create("item.scythe.spewing");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_BLOOD_SCYTHE_HIT = create("entity.blood_scythe.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_ANCHORBLADE_HIT = create("item.anchorblade.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> ITEM_ANCHORBLADE_THROW = create("item.anchorblade.throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENTITY_ANCHORBLADE_LAND = create("entity.anchorblade.land");

    private static DeferredHolder<SoundEvent, SoundEvent> create(String path) {
        return SOUND_EVENTS.register(path, () ->
                SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Arsenal.MOD_ID, path)));
    }

    private ArsenalSounds() {}
}
