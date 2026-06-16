package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

/**
 * C2S packet sent by the creative inventory back-slot when the player clicks it.
 * Carries the full ItemStack to place into the back slot. The server validates and
 * applies it via BackWeaponComponent.setBackWeapon(), then syncs to all clients.
 *
 * Uses RegistryFriendlyByteBuf because ItemStack.OPTIONAL_STREAM_CODEC requires
 * registry access for components (data-driven items, enchantments, etc.).
 */
public record SetBackWeaponPayload(ItemStack stack) implements CustomPacketPayload {
    public static final Type<SetBackWeaponPayload> TYPE = new Type<>(Arsenal.id("set_back_weapon"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetBackWeaponPayload> STREAM_CODEC =
            StreamCodec.composite(ItemStack.OPTIONAL_STREAM_CODEC, SetBackWeaponPayload::stack, SetBackWeaponPayload::new);

    @Override
    public Type<SetBackWeaponPayload> type() {
        return TYPE;
    }
}
