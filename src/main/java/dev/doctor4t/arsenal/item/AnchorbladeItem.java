package dev.doctor4t.arsenal.item;

import com.mojang.datafixers.util.Pair;
import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.cca.WeaponOwnerComponent;
import dev.doctor4t.arsenal.compat.CustomHitParticleItem;
import dev.doctor4t.arsenal.compat.CustomHitSoundItem;
import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import dev.doctor4t.arsenal.index.ArsenalCosmetics;
import dev.doctor4t.arsenal.index.ArsenalEnchantments;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.index.ArsenalSounds;
import dev.doctor4t.arsenal.util.AnchorOwner;
import dev.doctor4t.arsenal.util.SweepParticleUtil;
import dev.doctor4t.arsenal.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AnchorbladeItem extends PickaxeItem implements CustomHitParticleItem, CustomHitSoundItem, ArsenalWeaponItem {
    public AnchorbladeItem(Tier material, int attackDamage, float attackSpeed, Properties settings) {
        super(material, settings.attributes(
                PickaxeItem.createAttributes(material, attackDamage, attackSpeed)
        ));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState blockStateClicked = context.getLevel().getBlockState(context.getClickedPos());
        Player user = context.getPlayer();
        if (user != null && user.isShiftKeyDown() && (blockStateClicked.is(BlockTags.ANVIL) || blockStateClicked.is(Blocks.SMITHING_TABLE))) {
            if (!context.getLevel().isClientSide) {
                Skin currentSkin = Skin.fromString(ArsenalCosmetics.getSkin(context.getItemInHand()));
                if (currentSkin == null) {
                    currentSkin = Skin.DEFAULT;
                }
                ArsenalCosmetics.setSkin(context.getItemInHand(), Skin.getNext(currentSkin).getName());
            }
            user.playSound(SoundEvents.SMITHING_TABLE_USE, 0.5f, 1.0f);
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (user instanceof AnchorOwner owner) {
            boolean reeling = ArsenalEnchantments.getLevel(ArsenalEnchantments.REELING, stack, world) > 0;
            AnchorbladeEntity activeAnchor = owner.arsenal$getAnchor(hand, reeling);
            if (activeAnchor == null || !activeAnchor.isAlive()) {
                activeAnchor = owner.arsenal$getAnchor(hand, !reeling);
            }
            if (activeAnchor != null && activeAnchor.isAlive()) {
                // Right-click recall, exactly as in Arsenal: the blade flies back to the player
                // (reeling them along) only if they hold an anchorblade in the other hand.
                boolean otherHandAnchor = user.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND).is(ArsenalItems.ANCHORBLADE.get());
                activeAnchor.setRecalled(otherHandAnchor);
                return InteractionResultHolder.fail(stack);
            }
            int riptide = world.registryAccess()
                    .registryOrThrow(Registries.ENCHANTMENT)
                    .getHolder(Enchantments.RIPTIDE)
                    .map(entry -> EnchantmentHelper.getItemEnchantmentLevel(entry, stack))
                    .orElse(0);

            if (riptide == 0) {
                if (!world.isClientSide) {
                    stack.hurtAndBreak(1, user, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                    AnchorbladeEntity anchorbladeEntity = new AnchorbladeEntity(world, user, stack);
                    anchorbladeEntity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 2.5F, 1.0F);
                    owner.arsenal$setAnchor(hand, anchorbladeEntity);
                    world.addFreshEntity(anchorbladeEntity);
                    world.playSound(null, anchorbladeEntity, ArsenalSounds.ITEM_ANCHORBLADE_THROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                user.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.sidedSuccess(user.getItemInHand(hand), world.isClientSide());
            } else if (user.isInWaterOrRain()) {
                if (!world.isClientSide) {
                    stack.hurtAndBreak(1, user, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                }
                user.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.sidedSuccess(user.getItemInHand(hand), world.isClientSide());
            }
        }
        return InteractionResultHolder.success(user.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        Skin skin = Skin.fromString(ArsenalCosmetics.getSkin(stack));

        if (skin != null && skin != Skin.DEFAULT) {
            tooltip.add(Component.literal(skin.tooltipName != null ? skin.tooltipName : TextUtils.formatValueString(skin.getName())).withStyle(style -> style.withColor(skin.getFirstColor())));
            if (skin.lore != null) {
                if (Screen.hasShiftDown()) {
                    MutableComponent translatable = Component.translatable(skin.lore);
                    for (String line : translatable.getString().split("\n")) {
                        tooltip.add(Component.literal(line).withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY)));
                    }
                } else {
                    tooltip.add(Component.translatable("tooltip.arsenal.hidden").withStyle(style -> style.withColor(ChatFormatting.DARK_GRAY)));
                }
            }
        }

        super.appendHoverText(stack, context, tooltip, type);
    }

    @Override
    public void spawnHitParticles(Player player) {
        if (player.level() instanceof ServerLevel serverWorld) {
            Skin skin = Skin.DEFAULT;
            Skin toSkin = Skin.fromString(ArsenalCosmetics.getSkin(player.getMainHandItem()));
            if (toSkin != null) {
                skin = toSkin;
            }

            Pair<Integer, Integer> colorPair = new Pair<>(skin.getFirstColor(), skin.getSecondColor());
            SweepParticleUtil.sendSweepPacketToClient(serverWorld, colorPair, player.getX() + -Mth.sin((float) (player.getYRot() * (Math.PI / 180F))), player.getY(0.5D), player.getZ() + Mth.cos((float) (player.getYRot() * (Math.PI / 180F))));
        }
    }

    @Override
    public void playHitSound(Player player) {
        player.playSound(ArsenalSounds.ITEM_ANCHORBLADE_HIT.get(), 1.0F, (float) (1.0F + player.getRandom().nextGaussian() / 10f));
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player miner) {
        return !miner.isCreative();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (entity instanceof Player player) {
            WeaponOwnerComponent.setOwner(stack, player.getUUID());
        }
    }

    public enum Skin {
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
        public final ResourceLocation anchorbladeEntityModel;
        public final ResourceLocation chainTexture;

        Skin(int color, int shadowColor, @Nullable String tooltipName, @Nullable String lore,
             ResourceLocation anchorbladeEntityModel, ResourceLocation chainTexture) {
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
