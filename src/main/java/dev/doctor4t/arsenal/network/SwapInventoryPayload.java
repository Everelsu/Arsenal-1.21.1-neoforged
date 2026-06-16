package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SwapInventoryPayload(int slotId) implements CustomPacketPayload {
    public static final Type<SwapInventoryPayload> TYPE = new Type<>(Arsenal.id("swap_inventory"));
    public static final StreamCodec<FriendlyByteBuf, SwapInventoryPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, SwapInventoryPayload::slotId, SwapInventoryPayload::new);

    @Override
    public Type<SwapInventoryPayload> type() {
        return TYPE;
    }
}
