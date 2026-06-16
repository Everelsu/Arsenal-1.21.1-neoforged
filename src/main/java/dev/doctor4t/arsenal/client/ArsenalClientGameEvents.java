package dev.doctor4t.arsenal.client;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import dev.doctor4t.arsenal.network.SwapWeaponPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Arsenal.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ArsenalClientGameEvents {
    private ArsenalClientGameEvents() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        while (ArsenalClient.weaponKeybind.consumeClick()) {
            if (client.player != null) {
                BackWeaponComponent.setHoldingBackWeapon(client.player, !BackWeaponComponent.isHoldingBackWeapon(client.player));
            }
        }
        while (ArsenalClient.swapKeybind.consumeClick()) {
            if (client.player != null) {
                PacketDistributor.sendToServer(SwapWeaponPayload.INSTANCE);
            }
        }
    }
}
