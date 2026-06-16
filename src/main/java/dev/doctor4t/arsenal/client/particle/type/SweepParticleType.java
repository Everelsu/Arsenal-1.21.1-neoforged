package dev.doctor4t.arsenal.client.particle.type;

import dev.doctor4t.arsenal.client.particle.contract.ColoredParticleInitialData;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.particle.ParticleEffect;

public class SweepParticleType extends SimpleParticleType {
    public ColoredParticleInitialData initialData;

    public SweepParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    public ParticleEffect setData(ColoredParticleInitialData target) {
        this.initialData = target;
        return this;
    }
}
