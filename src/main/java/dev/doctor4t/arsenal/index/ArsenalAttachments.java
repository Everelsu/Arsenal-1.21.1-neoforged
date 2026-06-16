package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class ArsenalAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Arsenal.MOD_ID);

    public static final Supplier<AttachmentType<BackWeaponComponent>> BACK_WEAPON =
            ATTACHMENT_TYPES.register("back_weapon", () -> AttachmentType
                    .builder(BackWeaponComponent::new)
                    .serialize(BackWeaponComponent.SERIALIZER)
                    .copyOnDeath()
                    .build());

    private ArsenalAttachments() {}
}
