package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.effect.StunStatusEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ArsenalStatusEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Arsenal.MOD_ID);

    public static final Holder<MobEffect> STUN = MOB_EFFECTS.register("stun", StunStatusEffect::new);

    private ArsenalStatusEffects() {}
}
