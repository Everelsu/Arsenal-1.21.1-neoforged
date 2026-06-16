package dev.doctor4t.arsenal.item;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.cca.WeaponOwnerComponent;
import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import dev.doctor4t.arsenal.index.ArsenalCosmetics;
import dev.doctor4t.arsenal.index.ArsenalEnchantments;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.index.ArsenalSounds;
import dev.doctor4t.arsenal.util.AnchorOwner;
import dev.doctor4t.arsenal.util.SweepParticleUtil;
import dev.doctor4t.ratatouille.item.CustomHitParticleItem;
import dev.doctor4t.ratatouille.item.CustomHitSoundItem;
import dev.doctor4t.ratatouille.util.TextUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class AnchorbladeItem extends PickaxeItem implements CustomHitParticleItem, CustomHitSoundItem, ArsenalWeaponItem {
    public AnchorbladeItem(ToolMaterial material, int attackDamage, float attackSpeed, Settings settings) {
        super(material, settings.attributeModifiers(
                PickaxeItem.createAttributeModifiers(material, attackDamage, attackSpeed)
        ));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState blockStateClicked = context.getWorld().getBlockState(context.getBlockPos());
        PlayerEntity user = context.getPlayer();
        if (user != null && user.isSneaking() && (blockStateClicked.isIn(BlockTags.ANVIL) || blockStateClicked.isOf(Blocks.SMITHING_TABLE)) && context.getWorld().isClient) {
            if (ArsenalCosmetics.isSupporter(user.getUuid())) {
                UUID weaponOwner = WeaponOwnerComponent.getOwner(user.getStackInHand(context.getHand()));
                Skin currentSkin = Skin.fromString(ArsenalCosmetics.getSkin(context.getStack()));

                if (currentSkin == null) {
                    currentSkin = Skin.DEFAULT;
                }

                ArsenalCosmetics.setSkin(weaponOwner, context.getStack(), Skin.getNext(currentSkin).getName());
                context.getPlayer().playSound(SoundEvents.BLOCK_SMITHING_TABLE_USE, 0.5f, 1.0f);

                return ActionResult.SUCCESS;
            } else {
                if (context.getWorld().isClient) {
                    user.sendMessage(Text.translatable("tooltip.supporter_only").styled(style -> style.withColor(0xCC0000)));
                    context.getPlayer().playSound(SoundEvents.ITEM_SHIELD_BREAK, 0.5f, 1.0f);
                }
                return ActionResult.FAIL;
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (user instanceof AnchorOwner owner) {
            boolean reeling = ArsenalEnchantments.getLevel(ArsenalEnchantments.REELING, stack, world) > 0;
            // Check both reeling states — the blade in flight might not match the current
            // enchantment state of the item (e.g. if the stack changed), so try both.
            // Prefer the state matching the current enchant, fall back to the other.
            AnchorbladeEntity activeAnchor = owner.arsenal$getAnchor(hand, reeling);
            if (activeAnchor == null || !activeAnchor.isAlive()) {
                activeAnchor = owner.arsenal$getAnchor(hand, !reeling);
            }
            if (activeAnchor != null && activeAnchor.isAlive()) {
                if (activeAnchor.isRecallable()) {
                    // isRecallable() requires: Reeling enchantment, blade in ground,
                    // and >= 30 ticks (~1.5 s) grounded. All three must pass.
                    activeAnchor.setRecalled(true);
                }
                // Either way, suppress a new throw while a blade is still out.
                return TypedActionResult.success(stack, world.isClient());
            }
            int riptide = world.getRegistryManager()
                    .get(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                    .getEntry(net.minecraft.enchantment.Enchantments.RIPTIDE)
                    .map(entry -> EnchantmentHelper.getLevel(entry, stack))
                    .orElse(0);

            // FIX: Original logic had `return TypedActionResult.success(...)` inside `!world.isClient`,
            // meaning the client never got a success result and the throw appeared to do nothing.
            // The entity spawning must stay server-only, but the success return must happen on both sides.
            // Also removed the `riptide <= 0 || isTouchingWaterOrRain()` outer condition that was
            // preventing throws on dry land without riptide — the anchorblade should always throw.
            if (riptide == 0) {
                // Normal throw — always allowed
                if (!world.isClient) {
                    stack.damage(1, user, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    AnchorbladeEntity anchorbladeEntity = new AnchorbladeEntity(world, user, stack);
                    anchorbladeEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 2.5F, 1.0F);
                    owner.arsenal$setAnchor(hand, anchorbladeEntity);
                    world.spawnEntity(anchorbladeEntity);
                    world.playSoundFromEntity(null, anchorbladeEntity, ArsenalSounds.ITEM_ANCHORBLADE_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                }
                user.incrementStat(Stats.USED.getOrCreateStat(this));
                return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
            } else if (user.isTouchingWaterOrRain()) {
                // Riptide throw — only in water/rain
                if (!world.isClient) {
                    stack.damage(1, user, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
                user.incrementStat(Stats.USED.getOrCreateStat(this));
                return TypedActionResult.success(user.getStackInHand(hand), world.isClient());
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        Skin skin = Skin.fromString(ArsenalCosmetics.getSkin(stack));

        if (skin != null && skin != Skin.DEFAULT) {
            tooltip.add(Text.literal(skin.tooltipName != null ? skin.tooltipName : TextUtils.formatValueString(skin.getName())).styled(style -> style.withColor(skin.getFirstColor())));
            if (skin.lore != null) {
                if (Screen.hasShiftDown()) {
                    MutableText translatable = Text.translatable(skin.lore);
                    for (String line : translatable.getString().split("\n")) {
                        tooltip.add(Text.literal(line).styled(style -> style.withColor(Formatting.DARK_GRAY)));
                    }
                } else {
                    tooltip.add(Text.translatable("tooltip.arsenal.hidden").styled(style -> style.withColor(Formatting.DARK_GRAY)));
                }
            }
        }

        super.appendTooltip(stack, context, tooltip, type);
    }

    @Override
    public void spawnHitParticles(PlayerEntity player) {
        if (player.getWorld() instanceof ServerWorld serverWorld) {
            Skin skin = Skin.DEFAULT;
            Skin toSkin = Skin.fromString(ArsenalCosmetics.getSkin(player.getMainHandStack()));
            if (toSkin != null) {
                skin = toSkin;
            }

            Pair<Integer, Integer> colorPair = new Pair<>(skin.getFirstColor(), skin.getSecondColor());
            SweepParticleUtil.sendSweepPacketToClient(serverWorld, colorPair, player.getX() + -MathHelper.sin((float) (player.getYaw() * (Math.PI / 180F))), player.getBodyY(0.5D), player.getZ() + MathHelper.cos((float) (player.getYaw() * (Math.PI / 180F))));
        }
    }

    @Override
    public void playHitSound(PlayerEntity player) {
        player.playSound(ArsenalSounds.ITEM_ANCHORBLADE_HIT, 1.0F, (float) (1.0F + player.getRandom().nextGaussian() / 10f));
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        return !miner.isCreative();
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (entity instanceof PlayerEntity player) {
            WeaponOwnerComponent.setOwner(stack, player.getUuid());
        }
    }

    public enum Skin {
        // color, shadowColor, tooltipName, lore
        // anchorbladeEntityModel: Identifier for the in-hand model used when the entity is rendered (thrown)
        // chainTexture: Identifier for the entity/chain texture drawn between player and thrown blade
        DEFAULT (0xFFD9D9D9, 0xFF7F8885, null, null,
                Arsenal.id("item/anchorblade_in_hand"),           Arsenal.id("textures/entity/chain.png")),
        LUXINTRUS(0xFF8E00FF, 0xFF5500AA, null, null,
                Arsenal.id("item/anchorblade_luxintrus_in_hand"), Arsenal.id("textures/entity/chain_luxintrus.png")),
        CARRION  (0xFFE9DFB8, 0xFF9D806E, null, null,
                Arsenal.id("item/anchorblade_carrion_in_hand"),   Arsenal.id("textures/entity/chain_carrion.png")),
        GILDED   (0xFFF1BC5A, 0xFFE28634, null, null,
                Arsenal.id("item/anchorblade_gilded_in_hand"),    Arsenal.id("textures/entity/chain_gilded.png")),
        WINSWEEP (0xFF00BFFF, 0xFF007BA3, null, null,
                Arsenal.id("item/anchorblade_winsweep_in_hand"),  Arsenal.id("textures/entity/chain_winsweep.png")),
        AMBESSA  (0xFFFF6600, 0xFFCC4400, null, null,
                Arsenal.id("item/anchorblade_ambessa_in_hand"),   Arsenal.id("textures/entity/chain_ambessa.png"));

        public final int color;
        public final int shadowColor;
        public final @Nullable String lore;
        public final @Nullable String tooltipName;
        /** Identifier for the BakedModel used when rendering the thrown AnchorbladeEntity. */
        public final net.minecraft.util.Identifier anchorbladeEntityModel;
        /** Identifier for the chain texture rendered between the player and the thrown blade. */
        public final net.minecraft.util.Identifier chainTexture;

        Skin(int color, int shadowColor, @Nullable String tooltipName, @Nullable String lore,
             net.minecraft.util.Identifier anchorbladeEntityModel, net.minecraft.util.Identifier chainTexture) {
            this.color = color;
            this.shadowColor = shadowColor;
            this.lore = lore;
            this.tooltipName = tooltipName;
            this.anchorbladeEntityModel = anchorbladeEntityModel;
            this.chainTexture = chainTexture;
        }

        public int getFirstColor() { return this.color; }
        public int getSecondColor() { return this.shadowColor; }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        @Nullable
        public static Skin fromString(String name) {
            for (Skin skin : Skin.values()) if (skin.getName().equalsIgnoreCase(name)) return skin;
            return null;
        }

        public static Skin getNext(Skin skin) {
            Skin[] values = Skin.values();
            return values[(skin.ordinal() + 1) % values.length];
        }
    }
}