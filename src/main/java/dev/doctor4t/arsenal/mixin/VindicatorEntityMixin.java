package dev.doctor4t.arsenal.mixin;

import dev.doctor4t.arsenal.index.ArsenalItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Vindicator.class)
public abstract class VindicatorEntityMixin extends AbstractIllager {
    protected VindicatorEntityMixin(EntityType<? extends AbstractIllager> entityType, Level world) {
        super(entityType, world);
    }

    @Unique
    private void randomlyGiveScythe(RandomSource random) {
        if ((double) random.nextFloat() > 0.9) {
            int i = random.nextInt(16);
            if (i < 10) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ArsenalItems.SCYTHE.get()));
                this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            }
        }
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At(value = "TAIL"))
    protected void arsenal$equipScytheOnVindicators(RandomSource random, DifficultyInstance localDifficulty, CallbackInfo ci) {
        this.randomlyGiveScythe(random);
    }

    @Inject(method = "applyRaidBuffs", at = @At(value = "TAIL"))
    public void arsenal$equipScytheOnRaidVindicators(ServerLevel world, int wave, boolean unused, CallbackInfo ci) {
        this.randomlyGiveScythe(this.getRandom());
    }
}
