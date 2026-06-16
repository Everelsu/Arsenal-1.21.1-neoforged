package dev.doctor4t.arsenal.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class StunStatusEffect extends StatusEffect {
    public StunStatusEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFFD220);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    // FIX: In 1.21.1 applyUpdateEffect returns boolean (was void in older versions, but
    // the compiler shows the supertype signature is boolean). Return true to indicate the
    // effect was applied successfully.
    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        return super.applyUpdateEffect(entity, amplifier);
    }

    // FIX: onRemoved signature in 1.21.1 only takes AttributeContainer — no LivingEntity or amplifier.
    @Override
    public void onRemoved(AttributeContainer attributes) {
        super.onRemoved(attributes);
    }
}
