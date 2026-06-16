package dev.doctor4t.arsenal.entity;

import com.google.common.collect.Sets;
import dev.doctor4t.arsenal.index.ArsenalDamageTypes;
import dev.doctor4t.arsenal.index.ArsenalEntities;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.index.ArsenalParticles;
import dev.doctor4t.arsenal.index.ArsenalSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BloodScytheEntity extends AbstractArrow {
    private final Set<MobEffectInstance> effects = Sets.newHashSet();
    public int ticksUntilRemove = 5;
    public final List<LivingEntity> hitEntities = new ArrayList<>();

    public BloodScytheEntity(EntityType<? extends AbstractArrow> entityType, Level world) {
        super(entityType, world);
    }

    public BloodScytheEntity(Level world, LivingEntity owner) {
        super(ArsenalEntities.BLOOD_SCYTHE.get(), owner, world, new ItemStack(ArsenalItems.SCYTHE.get()), new ItemStack(ArsenalItems.SCYTHE.get()));
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ArsenalItems.SCYTHE.get());
    }

    public void addEffect(MobEffectInstance effect) {
        this.effects.add(effect);
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 velocity = this.getDeltaMovement();
        for (float x = -3; x <= 3; x += 0.1f) {
            this.level().addParticle(ArsenalParticles.BLOOD_BUBBLE.get(), this.getX() + x * Math.cos(this.getYRot()), this.getY(), this.getZ() + x * Math.sin(this.getYRot()), velocity.x, velocity.y, velocity.z);
        }

        if (this.inGround || this.tickCount > 20) {
            for (int i = 0; i < 50; i++) {
                this.level().addParticle(ArsenalParticles.BLOOD_BUBBLE_SPLATTER.get(), this.getX() + (this.random.nextGaussian() * 2) * Math.cos(this.getYRot()), this.getY(), this.getZ() + (this.random.nextGaussian() * 2) * Math.sin(this.getYRot()), this.random.nextGaussian() / 10, this.random.nextFloat() / 2, this.random.nextGaussian() / 10);
            }
            this.ticksUntilRemove--;
        }

        if (this.ticksUntilRemove <= 0) {
            this.discard();
        }

        if (!this.level().isClientSide) {
            for (LivingEntity livingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox(), livingEntity -> this.getOwner() != livingEntity)) {
                if (!hitEntities.contains(livingEntity)) {
                    livingEntity.hurt(ArsenalDamageTypes.source(this.level(), ArsenalDamageTypes.BLOOD_SCYTHE, this, this.getOwner()), 12.0f);
                    for (MobEffectInstance effect : this.effects) {
                        livingEntity.addEffect(effect);
                    }
                    hitEntities.add(livingEntity);
                }
            }
        }
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return ArsenalSounds.ENTITY_BLOOD_SCYTHE_HIT.get();
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
    }
}
