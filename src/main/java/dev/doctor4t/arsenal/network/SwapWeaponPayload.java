package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SwapWeaponPayload() implements CustomPacketPayload {
    public static final SwapWeaponPayload INSTANCE = new SwapWeaponPayload();
    public static final Type<SwapWeaponPayload> TYPE = new Type<>(Arsenal.id("swap_weapon"));
    public static final StreamCodec<FriendlyByteBuf, SwapWeaponPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<SwapWeaponPayload> type() {
        return TYPE;
    }
}
