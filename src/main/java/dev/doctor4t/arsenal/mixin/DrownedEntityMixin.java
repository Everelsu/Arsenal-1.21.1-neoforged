package dev.doctor4t.arsenal.mixin;

import dev.doctor4t.arsenal.index.ArsenalItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Drowned.class)
public abstract class DrownedEntityMixin extends Zombie {
    public DrownedEntityMixin(EntityType<? extends Zombie> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Drowned;setItemSlot(Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;)V", ordinal = 0, shift = At.Shift.AFTER))
    protected void arsenal$guaranteeDrownedTridentDrop(RandomSource random, DifficultyInstance localDifficulty, CallbackInfo ci) {
        this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At(value = "TAIL"))
    protected void arsenal$equipAnchorbladeOnDrowned(RandomSource random, DifficultyInstance localDifficulty, CallbackInfo ci) {
        if ((double) random.nextFloat() > 0.9) {
            int i = random.nextInt(16);
            if (i < 10) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ArsenalItems.ANCHORBLADE.get()));
                this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            }
        }
    }
}
