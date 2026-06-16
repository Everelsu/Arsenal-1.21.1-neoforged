package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ShockwavePayload(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<ShockwavePayload> TYPE = new Type<>(Arsenal.id("shockwave"));
    public static final StreamCodec<FriendlyByteBuf, ShockwavePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeDouble(payload.x());
                buf.writeDouble(payload.y());
                buf.writeDouble(payload.z());
            },
            buf -> new ShockwavePayload(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    @Override
    public Type<ShockwavePayload> type() {
        return TYPE;
    }
}
