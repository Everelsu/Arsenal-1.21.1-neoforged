package dev.doctor4t.arsenal.util;

import com.mojang.datafixers.util.Pair;
import dev.doctor4t.arsenal.network.SweepPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class SweepParticleUtil {
    public static void sendSweepPacketToClient(ServerLevel world, Pair<Integer, Integer> colorPair, double x, double y, double z) {
        SweepPayload payload = new SweepPayload(colorPair.getFirst(), colorPair.getSecond(), x, y, z);
        for (ServerPlayer serverPlayer : world.players()) {
            PacketDistributor.sendToPlayer(serverPlayer, payload);
        }
    }
}
