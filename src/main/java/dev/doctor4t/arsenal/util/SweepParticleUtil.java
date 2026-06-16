package dev.doctor4t.arsenal.util;

import dev.doctor4t.arsenal.network.SweepPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;

public class SweepParticleUtil {
    public static void sendSweepPacketToClient(ServerWorld world, Pair<Integer, Integer> colorPair, double x, double y, double z) {
        SweepPayload payload = new SweepPayload(colorPair.getLeft(), colorPair.getRight(), x, y, z);
        for (ServerPlayerEntity serverPlayerEntity : world.getPlayers()) {
            ServerPlayNetworking.send(serverPlayerEntity, payload);
        }
    }
}
