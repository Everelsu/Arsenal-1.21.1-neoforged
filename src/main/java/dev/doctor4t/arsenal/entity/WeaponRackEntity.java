package dev.doctor4t.arsenal.entity;

import dev.doctor4t.arsenal.index.ArsenalEntities;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.index.ArsenalTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

public class WeaponRackEntity extends ItemFrame {
    private static final Predicate<Entity> HANGABLE = entity -> entity instanceof HangingEntity;

    public WeaponRackEntity(EntityType<? extends WeaponRackEntity> entityType, Level world) {
        super(entityType, world);
    }

    public WeaponRackEntity(Level world, BlockPos pos, Direction facing) {
        this(ArsenalEntities.WEAPON_RACK.get(), world, pos, facing);
    }

    public WeaponRackEntity(EntityType<? extends WeaponRackEntity> type, Level world, BlockPos pos, Direction facing) {
        super(type, world, pos, facing);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown() && !this.getItem().isEmpty()) {
            this.setInvisible(!this.isInvisible());
            return InteractionResult.SUCCESS;
        }

        ItemStack stackInHand = player.getItemInHand(hand);
        if (!this.getItem().isEmpty() || (this.getItem().isEmpty() && stackInHand.is(ArsenalTags.DISPLAYABLE))) {
            return super.interact(player, hand);
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isInvisible() {
        return super.isInvisible() && !this.getItem().isEmpty();
    }

    @Override
    public boolean survives() {
        return this.level().getEntities(this, this.getBoundingBox(), HANGABLE).isEmpty();
    }

    @Override
    protected ItemStack getFrameItemStack() {
        return new ItemStack(ArsenalItems.WEAPON_RACK.get());
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        if (!(damageSource.getDirectEntity() instanceof Player player)) {
            return super.isInvulnerableTo(damageSource);
        }
        return !this.getItem().isEmpty() && !player.isShiftKeyDown();
    }
}
