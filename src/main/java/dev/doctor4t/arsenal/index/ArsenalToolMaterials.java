package dev.doctor4t.arsenal.index;

import net.minecraft.block.Block;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

public enum ArsenalToolMaterials implements ToolMaterial {
    SCYTHE(2031, 9.0F, 4.0F, 28, () -> Ingredient.ofItems(net.minecraft.item.Items.IRON_INGOT)),
    ANCHORBLADE(2031, 9.0F, 4.0F, 28, () -> Ingredient.ofItems(net.minecraft.item.Items.IRON_INGOT));

    private final int durability;
    private final float miningSpeedMultiplier;
    private final float attackDamage;
    private final int enchantability;
    private final java.util.function.Supplier<Ingredient> repairIngredient;

    ArsenalToolMaterials(int durability, float miningSpeedMultiplier, float attackDamage,
                         int enchantability, java.util.function.Supplier<Ingredient> repairIngredient) {
        this.durability          = durability;
        this.miningSpeedMultiplier = miningSpeedMultiplier;
        this.attackDamage        = attackDamage;
        this.enchantability      = enchantability;
        this.repairIngredient    = repairIngredient;
    }

    @Override public int getDurability()               { return durability; }
    @Override public float getMiningSpeedMultiplier()  { return miningSpeedMultiplier; }
    @Override public float getAttackDamage()           { return attackDamage; }
    @Override public int getEnchantability()           { return enchantability; }
    @Override public Ingredient getRepairIngredient()  { return repairIngredient.get(); }

    // FIX: getInverseTag() returns TagKey<Block>, not TagKey<Item>.
    // It represents blocks this material is INCORRECT for (wrong mining tier).
    // Arsenal weapons aren't tier-gated mining tools, so we use INCORRECT_FOR_NETHERITE_TOOL
    // (the highest tier) so they never incorrectly suppress block breaking feedback.
    @Override
    public TagKey<Block> getInverseTag() {
        return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
    }
}
