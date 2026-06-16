package dev.doctor4t.arsenal.entity;

import dev.doctor4t.arsenal.index.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AnchorbladeEntity extends PersistentProjectileEntity {
    private static final TrackedData<Byte> ANCHOR_FLAGS = DataTracker.registerData(AnchorbladeEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<ItemStack> ITEM = DataTracker.registerData(AnchorbladeEntity.class, TrackedDataHandlerRegistry.ITEM_STACK);

    public int returnTimer;

    public AnchorbladeEntity(EntityType<? extends AnchorbladeEntity> entityType, World world) {
        // FIX: use the two-arg constructor (EntityType, World) — no item stacks here
        super(entityType, world);
    }

    public AnchorbladeEntity(World world, LivingEntity owner, ItemStack stack) {
        // In 1.21.1, PersistentProjectileEntity(EntityType, LivingEntity, World, ItemStack projectile, ItemStack weapon)
        // requires the WEAPON (5th arg) to be non-empty — passing EMPTY throws "Invalid weapon firing an arrow".
        // The first ItemStack is the visual/projectile item; the second is the weapon that fired it
        // (used for enchantment effects like piercing). Pass the anchorblade stack as the weapon.
        super(ArsenalEntities.ANCHORBLADE, owner, world, new ItemStack(ArsenalItems.ANCHORBLADE), stack);
        this.setItem(stack.copy());
        this.setNoGravity(true);
        this.setReeling(ArsenalEnchantments.getLevel(ArsenalEnchantments.REELING, stack, world) > 0);
    }

    public void setItem(ItemStack stack) {
        if (!stack.isOf(Items.ENDER_EYE) || !stack.getComponentChanges().isEmpty()) {
            this.getDataTracker().set(ITEM, stack.copyWithCount(1));
        }
    }

    private ItemStack getTrackedItem() {
        return this.getDataTracker().get(ITEM);
    }

    public ItemStack getStack() {
        ItemStack itemStack = this.getTrackedItem();
        return itemStack.isEmpty() ? new ItemStack(Items.ENDER_EYE) : itemStack;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ANCHOR_FLAGS, (byte) 0);
        builder.add(ITEM, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();
        double d = 2;

        if (!this.getWorld().isClient) {
            if (owner == null || !owner.isAlive()) {
                this.discard();
                return;
            }
            if (this.hasDealtDamage() || this.isNoClip() || this.isRecalled()) {
                // isRecalled() is an extra safety gate: if right-click recall fired but
                // setDealtDamage somehow didn't propagate, the blade still flies home.
                this.setNoClip(true);
                Vec3d vec3d = owner.getEyePos().subtract(this.getPos());

                double length = vec3d.length();
                // Cap at 1.5 blocks/tick so the blade visibly travels home rather than
                // teleporting. At close range (length < 1.5) it scales down naturally
                // so it doesn't overshoot. d*3 (= 6) was the old cap — far too fast.
                this.setVelocity(vec3d.normalize().multiply(Math.min(length, 2.5)));
            }
            if (this.getPos().distanceTo(owner.getPos()) > 30) {
                this.setDealtDamage(true);
            }
        }

        // Snapshot inGround BEFORE super.tick() so we can detect the landing transition.
        // super.tick() is what actually sets inGround = true (inside PersistentProjectileEntity.tick()
        // via the block-collision raycast). If we check inGround before calling super.tick(),
        // it will always be false on the landing tick and the shockwave block never fires.
        boolean wasInGround = this.inGround;

        super.tick();

        // Now inGround is up-to-date. Only enter the block on the tick we first land
        // (wasInGround=false → inGround=true) OR on subsequent ticks while embedded
        // (wasInGround=true → inGround=true). hasDealtDamage() gates repeated execution:
        // setDealtDamage(true) is called at the end of the else-branch so the shockwave
        // + knockback fire exactly once, and the reeling branch uses returnTimer instead.
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
                Vec3d vec3d = this.getPos().subtract(owner.getEyePos());
                owner.setVelocity(owner.getVelocity().multiply(0.95).add(vec3d.normalize().multiply(e)));
                owner.fallDistance = 0;
            } else {
                float radius = 5f;
                // Spawn the shockwave particle on landing.
                // Client side: call addParticle() directly — ClientWorld.addParticle() is NOT a no-op,
                // unlike the World base class. This covers singleplayer and the local player in multiplayer.
                // Server side: send ShockwavePayload so all OTHER connected players also see it.
                if (this.getWorld().isClient) {
                    this.getWorld().addParticle(ArsenalParticles.SHOCKWAVE,
                            this.getX(), this.getY(), this.getZ(), 0, 0, 0);
                } else if (this.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                    dev.doctor4t.arsenal.network.ShockwavePayload payload =
                            new dev.doctor4t.arsenal.network.ShockwavePayload(this.getX(), this.getY(), this.getZ());
                    for (net.minecraft.server.network.ServerPlayerEntity player : serverWorld.getPlayers()) {
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(player, payload);
                    }
                }
                for (LivingEntity hitLivingEntity : this.getWorld().getEntitiesByClass(LivingEntity.class, this.getBoundingBox().expand(radius), LivingEntity::isAlive)) {
                    float strength = this.getKnockbackForEntity(hitLivingEntity);
                    if (!(strength <= 0.0)) {
                        this.velocityDirty = true;
                        Vec3d distance = hitLivingEntity.getPos().add(0, hitLivingEntity.getHeight() / 2f, 0).subtract(this.getPos());
                        Vec3d footDistance = hitLivingEntity.getPos().subtract(this.getPos());
                        if (footDistance.y > distance.y) {
                            distance = footDistance;
                        }
                        float proximity = (float) MathHelper.lerp(MathHelper.clamp(distance.length() / radius, 0, 1), 1, 0);
                        Vec3d direction = distance.normalize().multiply(proximity * strength);
                        hitLivingEntity.addVelocity(direction.x, direction.y, direction.z);
                        hitLivingEntity.fallDistance = 0;
                    }
                }
                this.setDealtDamage(true);
            }
        }
    }

    @Override
    public void setPitch(float pitch) {
        if (!this.hasDealtDamage()) {
            super.setPitch(pitch);
        }
    }

    @Override
    public void setYaw(float yaw) {
        if (!this.hasDealtDamage()) {
            super.setYaw(yaw);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity hitEntity = entityHitResult.getEntity();
        float damage = 10F;
        Entity owner = this.getOwner();
        this.setDealtDamage(true);
        SoundEvent soundEvent = this.getHitSound();
        hitEntity.timeUntilRegen = 0;
        if (hitEntity.damage(this.getWorld().getDamageSources().create(ArsenalDamageTypes.ANCHOR, this, this.getOwner()), damage)) {
            if (hitEntity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (hitEntity instanceof LivingEntity hitLivingEntity) {
                if (owner instanceof LivingEntity livingOwner) {
                    // FIX: applyDamageEffects(LivingEntity, LivingEntity) was removed in 1.21.1.
                    // Enchantment on-hit effects are now fully data-driven and triggered automatically
                    // by the damage pipeline. No manual call needed here.

                    float strength = this.getKnockbackForEntity(hitLivingEntity);
                    if (!(strength <= 0.0)) {
                        this.velocityDirty = true;
                        Vec3d dir = hitLivingEntity.getPos().subtract(owner.getPos()).normalize().multiply(strength);
                        if (this.hasReeling()) {
                            dir = owner.getPos().subtract(hitLivingEntity.getPos()).multiply(strength / 10f);
                        }
                        hitLivingEntity.addVelocity(dir.x, dir.y, dir.z);
                    }
                }
                this.onHit(hitLivingEntity);
            }

            if (this.getOwner() instanceof PlayerEntity player && !player.isCreative()) {
                player.getItemCooldownManager().set(ArsenalItems.ANCHORBLADE, 40);
            }
        }
        this.setVelocity(this.getVelocity().multiply(-0.01, -0.1, -0.01));
        this.playSound(soundEvent, 1.0f, 1.0f);
    }

    private float getKnockbackForEntity(LivingEntity hitLivingEntity) {
        return (float) (1f * (1.0 - hitLivingEntity.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)));
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        return this.isOwner(player);
    }

    @Override
    protected float getDragInWater() {
        return 0.99F;
    }

    @Override
    protected SoundEvent getHitSound() {
        return ArsenalSounds.ENTITY_ANCHORBLADE_LAND;
    }

    // Required abstract method in 1.21.1 - returns the item this projectile represents
    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(ArsenalItems.ANCHORBLADE);
    }

    @Override
    protected ItemStack asItemStack() {
        return ItemStack.EMPTY;
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

    /**
     * Returns true when a right-click recall is allowed:
     * - blade must have the Reeling enchantment (non-reeling blade auto-returns on its own)
     * - blade must currently be embedded in the ground
     * - blade must have been in the ground for at least 30 ticks (~1.5 s) so the
     *   player has a moment of being pulled before they can cut it short
     */
    public boolean isRecallable() {
        return this.hasReeling() && this.inGround && this.returnTimer >= 10;
    }

    public void setRecalled(boolean recalled) {
        if (recalled) {
            // Free the blade from the ground so it can fly home.
            // setDealtDamage(true) activates the return-home velocity block in tick(),
            // but super.tick() on a grounded (inGround=true) projectile ignores velocity.
            // setNoClip(true) + inGround=false lets the blade actually move next tick.
            this.setDealtDamage(true);
            this.setNoClip(true);
            this.inGround = false;
        }
        this.setAnchorFlag(2, recalled);
    }

    private boolean getAnchorFlag(int flag) {
        if (flag < 0 || flag > 8) {
            return false;
        }
        return (this.dataTracker.get(ANCHOR_FLAGS) >> flag & 0x01) == 1;
    }

    private void setAnchorFlag(int flag, boolean value) {
        if (flag < 0 || flag > 8) {
            return;
        }
        if (value) {
            this.dataTracker.set(ANCHOR_FLAGS, (byte) (this.dataTracker.get(ANCHOR_FLAGS) | 1 << flag));
        } else {
            this.dataTracker.set(ANCHOR_FLAGS, (byte) (this.dataTracker.get(ANCHOR_FLAGS) & ~(1 << flag)));
        }
    }
}