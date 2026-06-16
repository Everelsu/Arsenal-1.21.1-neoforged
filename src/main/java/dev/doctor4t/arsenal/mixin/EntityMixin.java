package dev.doctor4t.arsenal.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.arsenal.index.ArsenalStatusEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @ModifyReturnValue(method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"))
    public boolean arsenal$preventStunnedMobsFromTargeting(boolean original) {
        if ((Object) this instanceof LivingEntity livingEntity) {
            return livingEntity.hasEffect(ArsenalStatusEffects.STUN);
        }
        return original;
    }
}
