package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.client.particle.BloodBubbleParticle;
import dev.doctor4t.arsenal.client.particle.BloodBubbleSplatterParticle;
import dev.doctor4t.arsenal.client.particle.ShockwaveParticle;
import dev.doctor4t.arsenal.client.particle.SweepAttackParticle;
import dev.doctor4t.arsenal.client.particle.type.SweepParticleType;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ArsenalParticles {
    Map<ParticleType<?>, Identifier> PARTICLES = new LinkedHashMap<>();

    SweepParticleType SWEEP_PARTICLE = create("sweep", new SweepParticleType(true));
    SweepParticleType SWEEP_SHADOW_PARTICLE = create("sweep_shadow", new SweepParticleType(true));
    SimpleParticleType BLOOD_BUBBLE = create("blood_bubble", FabricParticleTypes.simple(true));
    SimpleParticleType BLOOD_BUBBLE_SPLATTER = create("blood_bubble_splatter", FabricParticleTypes.simple(true));
    SimpleParticleType SHOCKWAVE = create("shockwave", FabricParticleTypes.simple(true));

    static void initialize() {
        PARTICLES.keySet().forEach(particle -> Registry.register(Registries.PARTICLE_TYPE, PARTICLES.get(particle), particle));
    }

    private static <T extends ParticleType<?>> T create(String name, T particle) {
        PARTICLES.put(particle, Arsenal.id(name));
        return particle;
    }

    static void registerFactories() {
        registerSweep(SWEEP_PARTICLE);
        registerSweep(SWEEP_SHADOW_PARTICLE);
        ParticleFactoryRegistry.getInstance().register(BLOOD_BUBBLE, BloodBubbleParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(BLOOD_BUBBLE_SPLATTER, BloodBubbleSplatterParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(SHOCKWAVE, ShockwaveParticle.Factory::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void registerSweep(SweepParticleType type) {
        // SimpleParticleType is self-typed as ParticleType<SimpleParticleType>, so the compiler
        // infers conflicting equality constraints for T when a SweepParticleType (subtype) is
        // passed: T=SweepParticleType from the argument vs T=SimpleParticleType from the class
        // declaration. No lambda or anonymous-class typing can resolve this.
        //
        // Solution: use a fully raw PendingParticleFactory so the lambda body is not
        // type-checked against any generic parameter, then call register() on a raw registry
        // reference to bypass inference entirely. The explicit FabricSpriteProvider parameter
        // type on the lambda is required so the compiler knows which functional method to target.
        // Safe at runtime: SweepParticleType IS-A SimpleParticleType and the factory produces
        // the correct particle — no heap pollution.
        ParticleFactoryRegistry.PendingParticleFactory factory =
                (FabricSpriteProvider spriteProvider) -> new SweepAttackParticle.Factory(spriteProvider);
        ParticleFactoryRegistry raw = ParticleFactoryRegistry.getInstance();
        raw.register(type, factory);
    }
}