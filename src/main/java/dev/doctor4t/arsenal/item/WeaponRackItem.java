package dev.doctor4t.arsenal.item;

import dev.doctor4t.arsenal.entity.WeaponRackEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class WeaponRackItem extends Item {
    public WeaponRackItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos blockPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos blockPos2 = blockPos.relative(direction);
        Player playerEntity = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
            return InteractionResult.FAIL;
        } else {
            Level world = context.getLevel();
            HangingEntity hangingEntity = new WeaponRackEntity(world, blockPos2, direction);

            var entityData = itemStack.get(DataComponents.ENTITY_DATA);
            if (entityData != null) {
                EntityType.updateCustomEntityTag(world, playerEntity, hangingEntity, entityData);
            }

            if (hangingEntity.survives()) {
                if (!world.isClientSide) {
                    hangingEntity.playPlacementSound();
                    world.gameEvent(playerEntity, GameEvent.ENTITY_PLACE, hangingEntity.position());
                    world.addFreshEntity(hangingEntity);
                }

                itemStack.shrink(1);
                return InteractionResult.sidedSuccess(world.isClientSide);
            } else {
                return InteractionResult.CONSUME;
            }
        }
    }

    protected boolean canPlaceOn(Player player, Direction side, ItemStack stack, BlockPos pos) {
        return !player.level().isOutsideBuildHeight(pos) && player.mayUseItemAt(pos, side, stack);
    }
}
