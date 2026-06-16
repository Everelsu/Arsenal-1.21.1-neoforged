package dev.doctor4t.arsenal.network;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

/**
 * S2C packet broadcasting a player's back-weapon state (stack + holding flag) to all
 * tracking clients. Replaces the auto-sync that Cardinal Components provided on Fabric.
 */
public record BackWeaponSyncPayload(int entityId, ItemStack stack, boolean holding) implements CustomPacketPayload {
    public static final Type<BackWeaponSyncPayload> TYPE = new Type<>(Arsenal.id("back_weapon_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BackWeaponSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BackWeaponSyncPayload::entityId,
            ItemStack.OPTIONAL_STREAM_CODEC, BackWeaponSyncPayload::stack,
            ByteBufCodecs.BOOL, BackWeaponSyncPayload::holding,
            BackWeaponSyncPayload::new
    );

    @Override
    public Type<BackWeaponSyncPayload> type() {
        return TYPE;
    }
}
