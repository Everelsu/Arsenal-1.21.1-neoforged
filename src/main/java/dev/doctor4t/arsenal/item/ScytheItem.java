package dev.doctor4t.arsenal.item;

import com.mojang.datafixers.util.Pair;
import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.cca.WeaponOwnerComponent;
import dev.doctor4t.arsenal.compat.CustomHitParticleItem;
import dev.doctor4t.arsenal.compat.CustomHitSoundItem;
import dev.doctor4t.arsenal.entity.BloodScytheEntity;
import dev.doctor4t.arsenal.index.ArsenalCosmetics;
import dev.doctor4t.arsenal.index.ArsenalDamageTypes;
import dev.doctor4t.arsenal.index.ArsenalEnchantments;
import dev.doctor4t.arsenal.index.ArsenalSounds;
import dev.doctor4t.arsenal.util.SweepParticleUtil;
import dev.doctor4t.arsenal.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ScytheItem extends DiggerItem implements CustomHitParticleItem, CustomHitSoundItem, ArsenalWeaponItem {
    private static final AttributeModifier REACH_MODIFIER =
            new AttributeModifier(Arsenal.id("scythe_reach"), 0.5, AttributeModifier.Operation.ADD_VALUE);

    public ScytheItem(Tier material, float damage, float speed, Properties settings) {
        super(material, BlockTags.MINEABLE_WITH_HOE,
                settings.attributes(
                        ItemAttributeModifiers.builder()
                                .add(Attributes.ATTACK_DAMAGE,
                                        new AttributeModifier(BASE_ATTACK_DAMAGE_ID,
                                                damage + material.getAttackDamageBonus(),
                                                AttributeModifier.Operation.ADD_VALUE),
                                        EquipmentSlotGroup.MAINHAND)
                                .add(Attributes.ATTACK_SPEED,
                                        new AttributeModifier(BASE_ATTACK_SPEED_ID,
                                                speed,
                                                AttributeModifier.Operation.ADD_VALUE),
                                        EquipmentSlotGroup.MAINHAND)
                                .add(Attributes.ENTITY_INTERACTION_RANGE, REACH_MODIFIER, EquipmentSlotGroup.MAINHAND)
                                .build()
                ));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState blockStateClicked = context.getLevel().getBlockState(context.getClickedPos());
        Player user = context.getPlayer();
        if (user != null && user.isShiftKeyDown() && (blockStateClicked.is(BlockTags.ANVIL) || blockStateClicked.is(Blocks.SMITHING_TABLE)) && context.getLevel().isClientSide) {
            if (ArsenalCosmetics.isSupporter(user.getUUID())) {
                UUID weaponOwner = WeaponOwnerComponent.getOwner(user.getItemInHand(context.getHand()));
                Skin currentSkin = Skin.fromString(ArsenalCosmetics.getSkin(context.getItemInHand()));

                if (currentSkin == null) {
                    currentSkin = Skin.DEFAULT;
                }

                ArsenalCosmetics.setSkin(weaponOwner, context.getItemInHand(), Skin.getNext(currentSkin).getName());
                user.playSound(SoundEvents.SMITHING_TABLE_USE, 0.5f, 1.0f);
                return InteractionResult.SUCCESS;
            } else {
                user.displayClientMessage(Component.translatable("tooltip.supporter_only").withStyle(style -> style.withColor(0xCC0000)), false);
                user.playSound(SoundEvents.SHIELD_BREAK, 0.5f, 1.0f);
                return InteractionResult.FAIL;
            }
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (ArsenalEnchantments.getEquipmentLevel(ArsenalEnchantments.SPEWING, player) > 0) {
            float f = 1.0f;

            if (!world.isClientSide) {
                BloodScytheEntity bloodScythe = new BloodScytheEntity(world, player);
                bloodScythe.setOwner(player);
                bloodScythe.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, f * 3.0f, 1.0f);
                bloodScythe.setBaseDamage(bloodScythe.getBaseDamage());
                player.getItemInHand(hand).hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                bloodScythe.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;

                ArrayList<MobEffectInstance> statusEffectsHalved = new ArrayList<>();
                float absorption = player.getAbsorptionAmount();
                for (MobEffectInstance statusEffect : player.getActiveEffects()) {
                    MobEffectInstance statusHalved = new MobEffectInstance(statusEffect.getEffect(), statusEffect.getDuration() / 2, statusEffect.getAmplifier(), statusEffect.isAmbient(), statusEffect.isVisible(), statusEffect.showIcon());
                    bloodScythe.addEffect(statusHalved);
                    statusEffectsHalved.add(statusHalved);
                }
                player.removeAllEffects();
                for (MobEffectInstance statusEffectInstance : statusEffectsHalved) {
                    player.addEffect(statusEffectInstance);
                }
                player.setAbsorptionAmount(absorption);

                player.hurt(ArsenalDamageTypes.source(world, ArsenalDamageTypes.SPEWING), 3f);
                player.getCooldowns().addCooldown(this, 20);

                world.addFreshEntity(bloodScythe);

                if (player.level() instanceof ServerLevel serverWorld) {
                    Skin skin = Skin.DEFAULT;
                    Skin toSkin = Skin.fromString(ArsenalCosmetics.getSkin(player.getMainHandItem()));
                    if (toSkin != null) {
                        skin = toSkin;
                    }

                    Pair<Integer, Integer> colorPair = new Pair<>(skin.color, skin.shadowColor);
                    SweepParticleUtil.sendSweepPacketToClient(serverWorld, colorPair, player.getX() + -Mth.sin((float) (player.getYRot() * (Math.PI / 180F))), player.getY(0.5D), player.getZ() + Mth.cos((float) (player.getYRot() * (Math.PI / 180F))));
                }
            }
            world.playSound(null, player.getX(), player.getY(), player.getZ(), ArsenalSounds.ITEM_SCYTHE_SPEWING.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        return super.use(world, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag type) {
        Skin skin = Skin.fromString(ArsenalCosmetics.getSkin(stack));

        if (skin != null && skin != Skin.DEFAULT) {
            tooltip.add(Component.literal(skin.tooltipName != null ? skin.tooltipName : TextUtils.formatValueString(skin.getName())).withStyle(style -> style.withColor(skin.color)));
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

            Pair<Integer, Integer> colorPair = new Pair<>(skin.color, skin.shadowColor);
            SweepParticleUtil.sendSweepPacketToClient(serverWorld, colorPair, player.getX() + -Mth.sin((float) (player.getYRot() * (Math.PI / 180F))), player.getY(0.5D), player.getZ() + Mth.cos((float) (player.getYRot() * (Math.PI / 180F))));
        }
    }

    @Override
    public void playHitSound(Player player) {
        player.playSound(ArsenalSounds.ITEM_SCYTHE_HIT.get(), 1.0F, (float) (1.0F + player.getRandom().nextGaussian() / 10f));
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
        DEFAULT(0xFFD9D9D9, 0xFF7F8885, null, null),
        CLOWN(0xFFD90420, 0xFF8C0420, null, "tooltip.arsenal.scythe_clown"),
        CARRION(0xFFE9DFB8, 0xFF9D806E, null, null),
        GILDED(0xFFF1BC5A, 0xFFE28634, null, null),
        ROZE(0xFFB70066, 0xFF710949, null, null),
        FOLLY(0xFFFF005A, 0xFFBC0045, "Folly Tree Branch", null),
        SCISSORS(0xFFB9B1AF, 0xFF6F686F, null, null);

        public final int color;
        public final int shadowColor;
        public final @Nullable String lore;
        public final @Nullable String tooltipName;

        Skin(int color, int shadowColor, @Nullable String tooltipName, @Nullable String lore) {
            this.color = color;
            this.shadowColor = shadowColor;
            this.lore = lore;
            this.tooltipName = tooltipName;
        }

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
