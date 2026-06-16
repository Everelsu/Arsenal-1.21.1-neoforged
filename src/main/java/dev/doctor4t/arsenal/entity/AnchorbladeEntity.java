package dev.doctor4t.arsenal.entity;

import dev.doctor4t.arsenal.index.*;
import dev.doctor4t.arsenal.network.ShockwavePayload;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class AnchorbladeEntity extends AbstractArrow {
    private static final EntityDataAccessor<Byte> ANCHOR_FLAGS = SynchedEntityData.defineId(AnchorbladeEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<ItemStack> ITEM = SynchedEntityData.defineId(AnchorbladeEntity.class, EntityDataSerializers.ITEM_STACK);

    public int returnTimer;

    public AnchorbladeEntity(EntityType<? extends AnchorbladeEntity> entityType, Level world) {
        super(entityType, world);
    }

    public AnchorbladeEntity(Level world, LivingEntity owner, ItemStack stack) {
        super(ArsenalEntities.ANCHORBLADE.get(), owner, world, new ItemStack(ArsenalItems.ANCHORBLADE.get()), stack);
        this.setItem(stack.copy());
        this.setNoGravity(true);
        this.setReeling(ArsenalEnchantments.getLevel(ArsenalEnchantments.REELING, stack, world) > 0);
    }

    public void setItem(ItemStack stack) {
        if (!stack.is(Items.ENDER_EYE) || !stack.getComponentsPatch().isEmpty()) {
            this.getEntityData().set(ITEM, stack.copyWithCount(1));
        }
    }

    private ItemStack getTrackedItem() {
        return this.getEntityData().get(ITEM);
    }

    public ItemStack getStack() {
        ItemStack itemStack = this.getTrackedItem();
        return itemStack.isEmpty() ? new ItemStack(Items.ENDER_EYE) : itemStack;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANCHOR_FLAGS, (byte) 0);
        builder.define(ITEM, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();
        double d = 2;

        if (!this.level().isClientSide) {
            if (owner == null || !owner.isAlive()) {
                this.discard();
                return;
            }
            if (this.hasDealtDamage() || this.isNoPhysics() || this.isRecalled()) {
                this.setNoPhysics(true);
                Vec3 vec3d = owner.getEyePosition().subtract(this.position());

                double length = vec3d.length();
                this.setDeltaMovement(vec3d.normalize().scale(Math.min(length, 2.5)));
            }
            if (this.position().distanceTo(owner.position()) > 30) {
                this.setDealtDamage(true);
            }
        }

        boolean wasInGround = this.inGround;

        super.tick();

        if (this.inGround && !this.hasDealtDamage()) {
            if (this.hasReeling()) {
                if (this.returnTimer++ > 100) {
                    this.setDealtDamage(true);
                }
                if (owner == null) {
                    this.setDealtDamage(true);
                    return;
                }
                float e = (float) (d / 5f);
                Vec3 vec3d = this.position().subtract(owner.getEyePosition());
                owner.setDeltaMovement(owner.getDeltaMovement().scale(0.95).add(vec3d.normalize().scale(e)));
                owner.fallDistance = 0;
            } else {
                float radius = 5f;
                if (this.level().isClientSide) {
                    this.level().addParticle(ArsenalParticles.SHOCKWAVE.get(),
                            this.getX(), this.getY(), this.getZ(), 0, 0, 0);
                } else if (this.level() instanceof ServerLevel serverWorld) {
                    ShockwavePayload payload = new ShockwavePayload(this.getX(), this.getY(), this.getZ());
                    for (ServerPlayer player : serverWorld.players()) {
                        PacketDistributor.sendToPlayer(player, payload);
                    }
                }
                for (LivingEntity hitLivingEntity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(radius), LivingEntity::isAlive)) {
                    float strength = this.getKnockbackForEntity(hitLivingEntity);
                    if (!(strength <= 0.0)) {
                        this.hasImpulse = true;
                        Vec3 distance = hitLivingEntity.position().add(0, hitLivingEntity.getBbHeight() / 2f, 0).subtract(this.position());
                        Vec3 footDistance = hitLivingEntity.position().subtract(this.position());
                        if (footDistance.y > distance.y) {
                            distance = footDistance;
                        }
                        float proximity = (float) Mth.lerp(Mth.clamp(distance.length() / radius, 0, 1), 1, 0);
                        Vec3 direction = distance.normalize().scale(proximity * strength);
                        hitLivingEntity.push(direction.x, direction.y, direction.z);
                        hitLivingEntity.fallDistance = 0;
                    }
                }
                this.setDealtDamage(true);
            }
        }
    }

    @Override
    public void setXRot(float pitch) {
        if (!this.hasDealtDamage()) {
            super.setXRot(pitch);
        }
    }

    @Override
    public void setYRot(float yaw) {
        if (!this.hasDealtDamage()) {
            super.setYRot(yaw);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity hitEntity = entityHitResult.getEntity();
        float damage = 10F;
        Entity owner = this.getOwner();
        this.setDealtDamage(true);
        SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
        hitEntity.invulnerableTime = 0;
        if (hitEntity.hurt(ArsenalDamageTypes.source(this.level(), ArsenalDamageTypes.ANCHOR, this, this.getOwner()), damage)) {
            if (hitEntity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (hitEntity instanceof LivingEntity hitLivingEntity) {
                if (owner instanceof LivingEntity) {
                    float strength = this.getKnockbackForEntity(hitLivingEntity);
                    if (!(strength <= 0.0)) {
                        this.hasImpulse = true;
                        Vec3 dir = hitLivingEntity.position().subtract(owner.position()).normalize().scale(strength);
                        if (this.hasReeling()) {
                            dir = owner.position().subtract(hitLivingEntity.position()).scale(strength / 10f);
                        }
                        hitLivingEntity.push(dir.x, dir.y, dir.z);
                    }
                }
                this.doPostHurtEffects(hitLivingEntity);
            }

            if (this.getOwner() instanceof Player player && !player.isCreative()) {
                player.getCooldowns().addCooldown(ArsenalItems.ANCHORBLADE.get(), 40);
            }
        }
        this.setDeltaMovement(this.getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        this.playSound(soundEvent, 1.0f, 1.0f);
    }

    private float getKnockbackForEntity(LivingEntity hitLivingEntity) {
        return (float) (1f * (1.0 - hitLivingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
    }

    @Override
    protected boolean tryPickup(Player player) {
        return this.ownedBy(player);
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return ArsenalSounds.ENTITY_ANCHORBLADE_LAND.get();
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ArsenalItems.ANCHORBLADE.get());
    }

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    public boolean hasDealtDamage() {
        return this.getAnchorFlag(0);
    }

    public void setDealtDamage(boolean dealtDamage) {
        this.setAnchorFlag(0, dealtDamage);
    }

    public boolean hasReeling() {
        return this.getAnchorFlag(1);
    }

    public void setReeling(boolean reeling) {
        this.setAnchorFlag(1, reeling);
    }

    public boolean isRecalled() {
        return this.getAnchorFlag(2);
    }

    public boolean isRecallable() {
        return this.hasReeling() && this.inGround && this.returnTimer >= 10;
    }

    public void setRecalled(boolean recalled) {
        if (recalled) {
            this.setDealtDamage(true);
            this.setNoPhysics(true);
            this.inGround = false;
        }
        this.setAnchorFlag(2, recalled);
    }

    private boolean getAnchorFlag(int flag) {
        if (flag < 0 || flag > 8) {
            return false;
        }
        return (this.getEntityData().get(ANCHOR_FLAGS) >> flag & 0x01) == 1;
    }

    private void setAnchorFlag(int flag, boolean value) {
        if (flag < 0 || flag > 8) {
            return;
        }
        if (value) {
            this.getEntityData().set(ANCHOR_FLAGS, (byte) (this.getEntityData().get(ANCHOR_FLAGS) | 1 << flag));
        } else {
            this.getEntityData().set(ANCHOR_FLAGS, (byte) (this.getEntityData().get(ANCHOR_FLAGS) & ~(1 << flag)));
        }
    }
}
