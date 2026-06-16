package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.client.particle.type.SweepParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ArsenalParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Arsenal.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SweepParticleType> SWEEP_PARTICLE =
            PARTICLE_TYPES.register("sweep", () -> new SweepParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SweepParticleType> SWEEP_SHADOW_PARTICLE =
            PARTICLE_TYPES.register("sweep_shadow", () -> new SweepParticleType(true));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD_BUBBLE =
            PARTICLE_TYPES.register("blood_bubble", () -> new SimpleParticleType(true) {});
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BLOOD_BUBBLE_SPLATTER =
            PARTICLE_TYPES.register("blood_bubble_splatter", () -> new SimpleParticleType(true) {});
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SHOCKWAVE =
            PARTICLE_TYPES.register("shockwave", () -> new SimpleParticleType(true) {});

    private ArsenalParticles() {}
}
