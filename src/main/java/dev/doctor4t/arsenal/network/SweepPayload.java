package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SweepPayload(int color, int shadowColor, double x, double y, double z) implements CustomPacketPayload {
    public static final Type<SweepPayload> TYPE = new Type<>(Arsenal.id("sweep"));
    public static final StreamCodec<FriendlyByteBuf, SweepPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.color());
                buf.writeInt(payload.shadowColor());
                buf.writeDouble(payload.x());
                buf.writeDouble(payload.y());
                buf.writeDouble(payload.z());
            },
            buf -> new SweepPayload(buf.readInt(), buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    @Override
    public Type<SweepPayload> type() {
        return TYPE;
    }
}
