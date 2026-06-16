package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record SweepPayload(int color, int shadowColor, double x, double y, double z) implements CustomPayload {
    public static final CustomPayload.Id<SweepPayload> ID =
            new CustomPayload.Id<>(Arsenal.id("sweep"));
    public static final PacketCodec<PacketByteBuf, SweepPayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeInt(payload.color());
                buf.writeInt(payload.shadowColor());
                buf.writeDouble(payload.x());
                buf.writeDouble(payload.y());
                buf.writeDouble(payload.z());
            },
            buf -> new SweepPayload(buf.readInt(), buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    @Override
    public Id<SweepPayload> getId() {
        return ID;
    }
}
