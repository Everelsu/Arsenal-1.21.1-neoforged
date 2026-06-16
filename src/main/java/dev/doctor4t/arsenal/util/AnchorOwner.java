package dev.doctor4t.arsenal.util;

import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import net.minecraft.world.InteractionHand;

public interface AnchorOwner {
    void arsenal$setAnchor(InteractionHand hand, AnchorbladeEntity anchor);

    AnchorbladeEntity arsenal$getAnchor(InteractionHand hand, boolean reeling);

    boolean arsenal$isAnchorActive(InteractionHand hand, boolean reeling);
}
