package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * C2S packet sent by the creative inventory back-slot when the player clicks it.
 * Carries the full ItemStack to place into the back slot. The server validates and
 * applies it via BackWeaponComponent.setBackWeapon(), then syncs to all clients.
 *
 * Uses RegistryByteBuf (not PacketByteBuf) because ItemStack.OPTIONAL_PACKET_CODEC requires
 * registry access for components (data-driven items, enchantments, etc.).
 */
public record SetBackWeaponPayload(ItemStack stack) implements CustomPayload {
    public static final CustomPayload.Id<SetBackWeaponPayload> ID =
            new CustomPayload.Id<>(Arsenal.id("set_back_weapon"));
    public static final PacketCodec<RegistryByteBuf, SetBackWeaponPayload> CODEC =
            PacketCodec.tuple(ItemStack.OPTIONAL_PACKET_CODEC, SetBackWeaponPayload::stack, SetBackWeaponPayload::new);

    @Override
    public Id<SetBackWeaponPayload> getId() {
        return ID;
    }
}