package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SwapWeaponPayload() implements CustomPayload {
    public static final SwapWeaponPayload INSTANCE = new SwapWeaponPayload();
    public static final CustomPayload.Id<SwapWeaponPayload> ID =
            new CustomPayload.Id<>(Arsenal.id("swap_weapon"));
    public static final PacketCodec<PacketByteBuf, SwapWeaponPayload> CODEC =
            PacketCodec.unit(INSTANCE);

    @Override
    public Id<SwapWeaponPayload> getId() {
        return ID;
    }
}
