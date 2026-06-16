package dev.doctor4t.arsenal.entity;

import com.google.common.collect.Sets;
import dev.doctor4t.arsenal.index.ArsenalDamageTypes;
import dev.doctor4t.arsenal.index.ArsenalEntities;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.index.ArsenalParticles;
import dev.doctor4t.arsenal.index.ArsenalSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BloodScytheEntity extends PersistentProjectileEntity {
    private final Set<StatusEffectInstance> effects = Sets.newHashSet();
    public int ticksUntilRemove = 5;
    public final List<LivingEntity> hitEntities = new ArrayList<>();

    public BloodScytheEntity(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        // FIX: use the two-arg constructor (EntityType, World) — correct for 1.21.1
        super(entityType, world);
    }

    public BloodScytheEntity(World world, LivingEntity owner) {
        // In 1.21.1, PersistentProjectileEntity requires the 5th arg (weapon) to be non-empty.
        // Passing ItemStack.EMPTY throws "Invalid weapon firing an arrow".
        // A fresh scythe stack is sufficient — the weapon arg is only used for enchantment dispatch.
        super(ArsenalEntities.BLOOD_SCYTHE, owner, world, new ItemStack(ArsenalItems.SCYTHE), new ItemStack(ArsenalItems.SCYTHE));
    }

    // Required abstract method in 1.21.1
    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(ArsenalItems.SCYTHE);
    }

    @Override
    protected ItemStack asItemStack() {
        return ItemStack.EMPTY;
    }

    public void addEffect(StatusEffectInstance effect) {
        this.effects.add(effect);
    }

    @Override
    public void tick() {
        super.tick();

        for (float x = -3; x <= 3; x += 0.1f) {
            this.getWorld().addParticle(ArsenalParticles.BLOOD_BUBBLE, this.getX() + x * Math.cos(this.getYaw()), this.getY(), this.getZ() + x * Math.sin(this.getYaw()), this.getVelocity().getX(), this.getVelocity().getY(), this.getVelocity().getZ());
        }

        if (this.inGround || this.age > 20) {
            for (int i = 0; i < 50; i++) {
                this.getWorld().addParticle(ArsenalParticles.BLOOD_BUBBLE_SPLATTER, this.getX() + (this.random.nextGaussian() * 2) * Math.cos(this.getYaw()), this.getY(), this.getZ() + (this.random.nextGaussian() * 2) * Math.sin(this.getYaw()), this.random.nextGaussian() / 10, this.random.nextFloat() / 2, this.random.nextGaussian() / 10);
            }
            this.ticksUntilRemove--;
        }

        if (this.ticksUntilRemove <= 0) {
            this.discard();
        }

        if (!this.getWorld().isClient) {
            for (LivingEntity livingEntity : this.getWorld().getEntitiesByClass(LivingEntity.class, this.getBoundingBox(), livingEntity -> this.getOwner() != livingEntity)) {
                if (!hitEntities.contains(livingEntity)) {
                    livingEntity.damage(this.getWorld().getDamageSources().create(ArsenalDamageTypes.BLOOD_SCYTHE, this, this.getOwner()), 12.0f);
                    for (StatusEffectInstance effect : this.effects) {
                        livingEntity.addStatusEffect(effect);
                    }
                    hitEntities.add(livingEntity);
                }
            }
        }
    }

    @Override
    protected SoundEvent getHitSound() {
        return ArsenalSounds.ENTITY_BLOOD_SCYTHE_HIT;
    }

    @Override
    public boolean hasNoGravity() {
        return true;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
    }
}