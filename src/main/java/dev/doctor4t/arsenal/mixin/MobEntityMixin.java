package dev.doctor4t.arsenal.mixin;

import dev.doctor4t.arsenal.index.ArsenalStatusEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobEntityMixin extends LivingEntity {
    protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"), cancellable = true)
    public void arsenal$preventStunnedMobsFromAttacking(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (this.hasEffect(ArsenalStatusEffects.STUN)) {
            cir.setReturnValue(false);
        }
    }
}
