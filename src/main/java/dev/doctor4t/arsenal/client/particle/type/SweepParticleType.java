package dev.doctor4t.arsenal.client.particle.type;

import dev.doctor4t.arsenal.client.particle.contract.ColoredParticleInitialData;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;

public class SweepParticleType extends SimpleParticleType {
    public ColoredParticleInitialData initialData;

    public SweepParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    public ParticleOptions setData(ColoredParticleInitialData target) {
        this.initialData = target;
        return this;
    }
}
