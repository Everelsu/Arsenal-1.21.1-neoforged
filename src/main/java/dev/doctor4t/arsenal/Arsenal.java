package dev.doctor4t.arsenal;

import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import dev.doctor4t.arsenal.index.*;
import dev.doctor4t.arsenal.network.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Arsenal.MOD_ID)
public class Arsenal {
    public static final String MOD_ID = "arsenal";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public Arsenal(IEventBus modEventBus) {
        // Registries
        ArsenalItems.ITEMS.register(modEventBus);
        ArsenalEntities.ENTITY_TYPES.register(modEventBus);
        ArsenalSounds.SOUND_EVENTS.register(modEventBus);
        ArsenalParticles.PARTICLE_TYPES.register(modEventBus);
        ArsenalStatusEffects.MOB_EFFECTS.register(modEventBus);
        ArsenalAttachments.ATTACHMENT_TYPES.register(modEventBus);

        // Mod-bus listeners
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::addCreativeTabEntries);
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // C2S
        registrar.playToServer(HoldWeaponPayload.TYPE, HoldWeaponPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() ->
                        BackWeaponComponent.setHoldingBackWeapon(context.player(), payload.hold())));

        registrar.playToServer(SwapWeaponPayload.TYPE, SwapWeaponPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    Player player = context.player();
                    if (!player.isSpectator()) {
                        boolean toggled = BackWeaponComponent.isHoldingBackWeapon(player);
                        BackWeaponComponent.setHoldingBackWeapon(player, false);
                        ItemStack itemStack = BackWeaponComponent.getBackWeapon(player);
                        boolean success = BackWeaponComponent.setBackWeapon(player, player.getItemInHand(InteractionHand.MAIN_HAND));
                        if (success) {
                            player.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                        }
                        player.stopUsingItem();
                        BackWeaponComponent.setHoldingBackWeapon(player, toggled);
                    }
                }));

        registrar.playToServer(SetBackWeaponPayload.TYPE, SetBackWeaponPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    Player player = context.player();
                    if (!player.isSpectator()) {
                        BackWeaponComponent.setBackWeapon(player, payload.stack());
                    }
                }));

        registrar.playToServer(SwapInventoryPayload.TYPE, SwapInventoryPayload.STREAM_CODEC, (payload, context) ->
                context.enqueueWork(() -> {
                    Player player = context.player();
                    if (!player.isSpectator()) {
                        AbstractContainerMenu menu = player.containerMenu;
                        int slotId = payload.slotId();
                        if (slotId < 0 || slotId >= menu.slots.size()) {
                            return;
                        }
                        Slot slot = menu.getSlot(slotId);
                        ItemStack itemStack = BackWeaponComponent.getBackWeapon(player);
                        boolean success = BackWeaponComponent.setBackWeapon(player, slot.getItem());
                        if (success) {
                            slot.set(itemStack);
                        }
                    }
                }));

        // S2C
        registrar.playToClient(SweepPayload.TYPE, SweepPayload.STREAM_CODEC,
                dev.doctor4t.arsenal.client.ArsenalClientNetworking::handleSweep);
        registrar.playToClient(ShockwavePayload.TYPE, ShockwavePayload.STREAM_CODEC,
                dev.doctor4t.arsenal.client.ArsenalClientNetworking::handleShockwave);
        registrar.playToClient(BackWeaponSyncPayload.TYPE, BackWeaponSyncPayload.STREAM_CODEC,
                dev.doctor4t.arsenal.client.ArsenalClientNetworking::handleBackWeaponSync);
    }

    private void addCreativeTabEntries(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.insertAfter(Items.TRIDENT.getDefaultInstance(), ArsenalItems.SCYTHE.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(ArsenalItems.SCYTHE.get().getDefaultInstance(), ArsenalItems.ANCHORBLADE.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.insertAfter(Items.GLOW_ITEM_FRAME.getDefaultInstance(), ArsenalItems.WEAPON_RACK.get().getDefaultInstance(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
