package dev.doctor4t.arsenal.compat;

import net.minecraft.world.entity.player.Player;

/**
 * Embedded replacement for ratatouille's CustomHitParticleItem (Fabric-only).
 * Items implementing this spawn custom particles on a fully-charged melee hit.
 */
public interface CustomHitParticleItem {
    void spawnHitParticles(Player player);
}
