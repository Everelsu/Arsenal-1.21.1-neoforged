package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SwapInventoryPayload(int slotId) implements CustomPayload {
    public static final CustomPayload.Id<SwapInventoryPayload> ID =
            new CustomPayload.Id<>(Arsenal.id("swap_inventory"));
    public static final PacketCodec<PacketByteBuf, SwapInventoryPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.VAR_INT, SwapInventoryPayload::slotId, SwapInventoryPayload::new);

    @Override
    public Id<SwapInventoryPayload> getId() {
        return ID;
    }
}
