package dev.doctor4t.arsenal.datagen;

import dev.doctor4t.arsenal.index.ArsenalDamageTypes;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.index.ArsenalTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

public class ArsenalTagGen {
    public static class ArsenalDamageTagGen extends FabricTagProvider<DamageType> {
        public ArsenalDamageTagGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
            super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            this.getOrCreateTagBuilder(DamageTypeTags.IS_PROJECTILE)
                    .addOptional(ArsenalDamageTypes.ANCHOR.getValue())
                    .addOptional(ArsenalDamageTypes.BLOOD_SCYTHE.getValue());

            this.getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ENCHANTMENTS)
                    .addOptional(ArsenalDamageTypes.SPEWING.getValue());

            this.getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR)
                    .addOptional(ArsenalDamageTypes.SPEWING.getValue());
        }
    }

    public static class ArsenalItemTagGen extends FabricTagProvider.ItemTagProvider {
        public ArsenalItemTagGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
            super(output, completableFuture);
        }

        @Override
        protected void configure(RegistryWrapper.WrapperLookup arg) {
            this.getOrCreateTagBuilder(ArsenalTags.DISPLAYABLE)
                    // FIX: ItemTags.TOOLS was removed in 1.21.1 and WRONG_TIER_TOOL_TIPS doesn't exist.
                    // Use individual tool-category tags instead. SWORDS covers the most relevant case;
                    // the TODO comment in the original source notes this section needs proper conventional tags anyway.
                    .addOptionalTag(ItemTags.SWORDS)
                    .addOptionalTag(ItemTags.AXES)
                    .addOptionalTag(ItemTags.PICKAXES)
                    .addTag(ArsenalTags.BIG_WEAPONS)
                    .addTag(ArsenalTags.SHIELDS)
                    .addTag(ArsenalTags.RANGED_WEAPONS)
                    .addTag(ArsenalTags.TRIDENTS)
            ;

            this.getOrCreateTagBuilder(ArsenalTags.BIG_WEAPONS)
                    .add(ArsenalItems.SCYTHE)
                    .add(ArsenalItems.ANCHORBLADE)
                    .addTag(ArsenalTags.TRIDENTS)
            ;

            this.getOrCreateTagBuilder(ArsenalTags.TRIDENTS)
                    .add(Items.TRIDENT)
            ;

            this.getOrCreateTagBuilder(ArsenalTags.SHIELDS)
                    .add(Items.SHIELD)
            ;

            this.getOrCreateTagBuilder(ArsenalTags.RANGED_WEAPONS)
                    .add(Items.BOW)
                    .add(Items.CROSSBOW)
            ;
        }
    }
}
