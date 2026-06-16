package dev.doctor4t.arsenal.client;

import dev.doctor4t.arsenal.cca.BackWeaponComponent;
import dev.doctor4t.arsenal.client.particle.contract.ColoredParticleInitialData;
import dev.doctor4t.arsenal.client.particle.type.SweepParticleType;
import dev.doctor4t.arsenal.index.ArsenalParticles;
import dev.doctor4t.arsenal.network.BackWeaponSyncPayload;
import dev.doctor4t.arsenal.network.ShockwavePayload;
import dev.doctor4t.arsenal.network.SweepPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-side handlers for S2C payloads. Only invoked on the client; method
 * signatures use common types so the class is safe to reference from common
 * payload registration without loading client-only classes on a server.
 */
public final class ArsenalClientNetworking {
    private ArsenalClientNetworking() {}

    public static void handleSweep(SweepPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                SweepParticleType sweep = (SweepParticleType) ArsenalParticles.SWEEP_PARTICLE.get();
                SweepParticleType sweepShadow = (SweepParticleType) ArsenalParticles.SWEEP_SHADOW_PARTICLE.get();
                level.addParticle(sweep.setData(new ColoredParticleInitialData(payload.color())), payload.x(), payload.y(), payload.z(), 0, 0, 0);
                level.addParticle(sweepShadow.setData(new ColoredParticleInitialData(payload.shadowColor())), payload.x(), payload.y(), payload.z(), 0, 0, 0);
            }
        });
    }

    public static void handleShockwave(ShockwavePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                level.addParticle(ArsenalParticles.SHOCKWAVE.get(), payload.x(), payload.y(), payload.z(), 0, 0, 0);
            }
        });
    }

    public static void handleBackWeaponSync(BackWeaponSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                Entity entity = level.getEntity(payload.entityId());
                if (entity != null) {
                    BackWeaponComponent.applyClientSync(entity, payload.stack(), payload.holding());
                }
            }
        });
    }
}
