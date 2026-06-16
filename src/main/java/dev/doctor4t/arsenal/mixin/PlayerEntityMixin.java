package dev.doctor4t.arsenal.mixin;

import dev.doctor4t.arsenal.compat.CustomHitParticleItem;
import dev.doctor4t.arsenal.compat.CustomHitSoundItem;
import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import dev.doctor4t.arsenal.index.ArsenalStatusEffects;
import dev.doctor4t.arsenal.item.AnchorbladeItem;
import dev.doctor4t.arsenal.item.ScytheItem;
import dev.doctor4t.arsenal.util.AnchorOwner;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("WrongEntityDataParameterClass")
@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements AnchorOwner {

    @Unique
    private static final EntityDataAccessor<Integer> BASIC_ANCHOR_MAIN = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> REELING_ANCHOR_MAIN = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> BASIC_ANCHOR_OFF = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> REELING_ANCHOR_OFF = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Shadow
    public abstract float getAttackStrengthScale(float baseTime);

    @Shadow
    public abstract void disableShield();

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void arsenal$initDataTracker(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BASIC_ANCHOR_MAIN, -1);
        builder.define(REELING_ANCHOR_MAIN, -1);
        builder.define(BASIC_ANCHOR_OFF, -1);
        builder.define(REELING_ANCHOR_OFF, -1);
    }

    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    public void arsenal$multiplyAnchorbladeMiningSpeedUnderwater(BlockState block, CallbackInfoReturnable<Float> cir) {
        if (this.getMainHandItem().getItem() instanceof AnchorbladeItem && this.isEyeInFluid(FluidTags.WATER)) {
            cir.setReturnValue(cir.getReturnValue() * 2f);
        }
    }

    // Embedded ratatouille hit hook: on a fully-charged melee hit, fire the weapon's
    // custom particles/sound. Ported from ratatouille's PlayerEntityMixin.
    @Inject(method = "attack", at = @At("HEAD"))
    private void arsenal$customHitEffects(Entity target, CallbackInfo ci) {
        if (this.getAttackStrengthScale(0.5f) > 0.9f) {
            Item item = this.getMainHandItem().getItem();
            if (item instanceof CustomHitParticleItem particleItem) {
                particleItem.spawnHitParticles((Player) (Object) this);
            }
            if (item instanceof CustomHitSoundItem soundItem) {
                soundItem.playHitSound((Player) (Object) this);
            }
        }
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;crit(Lnet/minecraft/world/entity/Entity;)V"))
    private void arsenal$scytheReelTargetOnCrit(Entity target, CallbackInfo ci) {
        if (this.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof ScytheItem) {
            float strength = 1f;
            if (target instanceof LivingEntity livingEntity) {
                strength = (float) (.25f * (1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
                livingEntity.addEffect(new MobEffectInstance(ArsenalStatusEffects.STUN, 10, 0, false, false, false));
            }
            target.setDeltaMovement(this.position().subtract(target.position()).scale(strength));
            target.hasImpulse = true;
        }
    }

    @Inject(method = "blockUsingShield", at = @At("HEAD"))
    protected void arsenal$scytheDisableShield(LivingEntity attacker, CallbackInfo ci) {
        if (attacker.getMainHandItem().getItem() instanceof ScytheItem) {
            this.disableShield();
        }
    }

    @Override
    public void arsenal$setAnchor(InteractionHand hand, AnchorbladeEntity anchor) {
        boolean reeling = anchor.hasReeling();
        if (hand == InteractionHand.MAIN_HAND) {
            this.entityData.set(reeling ? REELING_ANCHOR_MAIN : BASIC_ANCHOR_MAIN, anchor.getId());
        } else {
            this.entityData.set(reeling ? REELING_ANCHOR_OFF : BASIC_ANCHOR_OFF, anchor.getId());
        }
    }

    @Override
    public AnchorbladeEntity arsenal$getAnchor(InteractionHand hand, boolean reeling) {
        if (hand == InteractionHand.MAIN_HAND) {
            return this.level().getEntity(reeling ? this.entityData.get(REELING_ANCHOR_MAIN) : this.entityData.get(BASIC_ANCHOR_MAIN)) instanceof AnchorbladeEntity anchor ? anchor : null;
        } else {
            return this.level().getEntity(reeling ? this.entityData.get(REELING_ANCHOR_OFF) : this.entityData.get(BASIC_ANCHOR_OFF)) instanceof AnchorbladeEntity anchor ? anchor : null;
        }
    }

    @Override
    public boolean arsenal$isAnchorActive(InteractionHand hand, boolean reeling) {
        AnchorbladeEntity anchor = this.arsenal$getAnchor(hand, reeling);
        return anchor != null && anchor.isAlive();
    }
}
