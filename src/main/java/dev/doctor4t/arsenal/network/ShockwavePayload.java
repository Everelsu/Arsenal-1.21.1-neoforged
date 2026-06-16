package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ShockwavePayload(double x, double y, double z) implements CustomPayload {
    public static final Id<ShockwavePayload> ID =
            new Id<>(Arsenal.id("shockwave"));
    public static final PacketCodec<PacketByteBuf, ShockwavePayload> CODEC = PacketCodec.of(
            (payload, buf) -> {
                buf.writeDouble(payload.x());
                buf.writeDouble(payload.y());
                buf.writeDouble(payload.z());
            },
            buf -> new ShockwavePayload(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );

    @Override
    public Id<ShockwavePayload> getId() {
        return ID;
    }
}
