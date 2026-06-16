package dev.doctor4t.arsenal.index;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface ArsenalTags {
    TagKey<Item> DISPLAYABLE = create("displayable");
    TagKey<Item> BIG_WEAPONS = create("big_weapons");
    TagKey<Item> RANGED_WEAPONS = create("ranged_weapons");
    TagKey<Item> SHIELDS = create("shields");
    TagKey<Item> TRIDENTS = create("tridents");

    private static TagKey<Item> create(String id) {
        return TagKey.create(Registries.ITEM, Arsenal.id(id));
    }
}
