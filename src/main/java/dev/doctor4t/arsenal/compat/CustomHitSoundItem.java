package dev.doctor4t.arsenal.compat;

import net.minecraft.world.entity.player.Player;

/**
 * Embedded replacement for ratatouille's CustomHitSoundItem (Fabric-only).
 * Items implementing this play a custom sound on a fully-charged melee hit.
 */
public interface CustomHitSoundItem {
    void playHitSound(Player player);
}
