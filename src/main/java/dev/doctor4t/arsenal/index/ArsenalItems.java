package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.item.AnchorbladeItem;
import dev.doctor4t.arsenal.item.ScytheItem;
import dev.doctor4t.arsenal.item.WeaponRackItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ArsenalItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Arsenal.MOD_ID);

    public static final DeferredItem<Item> SCYTHE = ITEMS.registerItem("scythe",
            properties -> new ScytheItem(ArsenalToolMaterials.SCYTHE, 5.0f, -3.0f, properties.rarity(Rarity.COMMON)));
    public static final DeferredItem<Item> ANCHORBLADE = ITEMS.registerItem("anchorblade",
            properties -> new AnchorbladeItem(ArsenalToolMaterials.ANCHORBLADE, 5, -3.0f, properties.rarity(Rarity.COMMON)));
    public static final DeferredItem<Item> WEAPON_RACK = ITEMS.registerItem("weapon_rack",
            WeaponRackItem::new);

    private ArsenalItems() {}
}
