package dev.doctor4t.arsenal.index;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public enum ArsenalToolMaterials implements Tier {
    SCYTHE(2031, 9.0F, 4.0F, 28, () -> Ingredient.of(Items.IRON_INGOT)),
    ANCHORBLADE(2031, 9.0F, 4.0F, 28, () -> Ingredient.of(Items.IRON_INGOT));

    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final Supplier<Ingredient> repairIngredient;

    ArsenalToolMaterials(int uses, float speed, float attackDamageBonus,
                         int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = repairIngredient;
    }

    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return attackDamageBonus; }
    @Override public int getEnchantmentValue() { return enchantmentValue; }
    @Override public Ingredient getRepairIngredient() { return repairIngredient.get(); }

    // Arsenal weapons aren't tier-gated mining tools, so use the highest tier's
    // incorrect-blocks tag so they never suppress block-breaking feedback.
    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
    }
}
