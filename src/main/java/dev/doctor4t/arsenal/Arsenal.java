package dev.doctor4t.arsenal;

import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import dev.doctor4t.arsenal.index.*;
import dev.doctor4t.arsenal.network.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class Arsenal implements ModInitializer {
    public static final String MOD_ID = "arsenal";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ArsenalEntities.initialize();
        ArsenalItems.initialize();
        ArsenalSounds.initialize();
        ArsenalParticles.initialize();
        ArsenalStatusEffects.initialize();

        // Register all payload types (must be done on both sides before any send/receive)
        PayloadTypeRegistry.playC2S().register(HoldWeaponPayload.ID, HoldWeaponPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SwapWeaponPayload.ID, SwapWeaponPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SwapInventoryPayload.ID, SwapInventoryPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetBackWeaponPayload.ID, SetBackWeaponPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SweepPayload.ID, SweepPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShockwavePayload.ID, ShockwavePayload.CODEC);

        // Server-side receivers
        ServerPlayNetworking.registerGlobalReceiver(HoldWeaponPayload.ID, (payload, context) ->
                context.server().execute(() ->
                        BackWeaponComponent.setHoldingBackWeapon(context.player(), payload.hold())
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(SwapWeaponPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    if (!player.isSpectator()) {
                        boolean toggled = BackWeaponComponent.isHoldingBackWeapon(player);
                        BackWeaponComponent.setHoldingBackWeapon(player, false);
                        ItemStack itemStack = BackWeaponComponent.getBackWeapon(player);
                        boolean success = BackWeaponComponent.setBackWeapon(player, player.getStackInHand(Hand.MAIN_HAND));
                        if (success) {
                            player.setStackInHand(Hand.MAIN_HAND, itemStack);
                        }
                        player.clearActiveItem();
                        BackWeaponComponent.setHoldingBackWeapon(player, toggled);
                    }
                })
        );

        // Creative back-slot: client sends the desired ItemStack directly.
        // Server applies it; BackWeaponComponent (CCA AutoSynced) handles the sync.
        ServerPlayNetworking.registerGlobalReceiver(SetBackWeaponPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    if (!player.isSpectator()) {
                        BackWeaponComponent.setBackWeapon(player, payload.stack());
                    }
                })
        );

        ServerPlayNetworking.registerGlobalReceiver(SwapInventoryPayload.ID, (payload, context) ->
                context.server().execute(() -> {
                    var player = context.player();
                    if (!player.isSpectator()) {
                        if (!player.currentScreenHandler.isValid(payload.slotId())) {
                            return;
                        }
                        Slot slot = player.currentScreenHandler.getSlot(payload.slotId());
                        ItemStack itemStack = BackWeaponComponent.getBackWeapon(player);
                        boolean success = BackWeaponComponent.setBackWeapon(player, slot.getStack());
                        if (success) {
                            slot.setStack(itemStack);
                        }
                    }
                })
        );
    }
}