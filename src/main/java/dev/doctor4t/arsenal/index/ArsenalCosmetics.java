package dev.doctor4t.arsenal.index;

import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Supporter weapon-skin cosmetics.
 *
 * <p>The original Fabric mod backed this with the "datasync" supporter-entitlement
 * web service, which has no NeoForge port. The skin system is therefore stubbed:
 * every weapon resolves to the "default" skin and skin changes are no-ops. The
 * public API is kept so renderers and items compile unchanged; reinstating real
 * skins would require a NeoForge-compatible entitlement backend.
 */
public interface ArsenalCosmetics {
    String DEFAULT_SKIN = "default";

    static String getSkin(ItemStack itemStack) {
        return DEFAULT_SKIN;
    }

    static void setSkin(UUID playerUuid, ItemStack itemStack, String skinName) {
        // no-op: supporter skin backend (datasync) is unavailable on NeoForge
    }

    static boolean isSupporter(UUID uuid) {
        return false;
    }
}
