package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record HoldWeaponPayload(boolean hold) implements CustomPacketPayload {
    public static final Type<HoldWeaponPayload> TYPE = new Type<>(Arsenal.id("hold_weapon"));
    public static final StreamCodec<FriendlyByteBuf, HoldWeaponPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, HoldWeaponPayload::hold, HoldWeaponPayload::new);

    @Override
    public Type<HoldWeaponPayload> type() {
        return TYPE;
    }
}
