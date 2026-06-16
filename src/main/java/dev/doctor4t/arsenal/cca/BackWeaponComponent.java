package dev.doctor4t.arsenal.cca;

import dev.doctor4t.arsenal.index.ArsenalAttachments;
import dev.doctor4t.arsenal.network.BackWeaponSyncPayload;
import dev.doctor4t.arsenal.network.HoldWeaponPayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * Per-player back-weapon storage. Ported from the Fabric Cardinal Components
 * AutoSyncedComponent to a NeoForge data attachment. Server-side mutations are
 * broadcast to tracking clients via {@link BackWeaponSyncPayload}.
 */
public class BackWeaponComponent {
    private final @Nullable Player player;
    private final SimpleContainer backWeapon;
    private boolean holdingBackWeapon = false;

    public BackWeaponComponent(@Nullable IAttachmentHolder holder) {
        this.player = holder instanceof Player p ? p : null;
        this.backWeapon = new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                BackWeaponComponent.this.sync();
            }
        };
    }

    // --- instance API ---

    public ItemStack getBackWeapon() {
        return this.backWeapon.getItem(0);
    }

    public boolean setBackWeapon(ItemStack stack) {
        this.backWeapon.setItem(0, stack);
        this.sync();
        return true;
    }

    public SimpleContainer getBackWeaponInventory() {
        return this.backWeapon;
    }

    public boolean isHoldingBackWeapon() {
        return this.holdingBackWeapon;
    }

    public void setHoldingBackWeapon(boolean holding) {
        this.holdingBackWeapon = holding;
        this.sync();
    }

    private void sync() {
        if (this.player != null && !this.player.level().isClientSide()) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(this.player,
                    new BackWeaponSyncPayload(this.player.getId(), this.getBackWeapon(), this.holdingBackWeapon));
        }
    }

    // --- (de)serialization ---

    public CompoundTag save(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ItemStack stack = this.getBackWeapon();
        if (!stack.isEmpty()) {
            tag.put("backWeapon", stack.save(provider));
        }
        tag.putBoolean("holdingBackWeapon", this.holdingBackWeapon);
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains("backWeapon")) {
            this.backWeapon.setItem(0, ItemStack.parseOptional(provider, tag.getCompound("backWeapon")));
        }
        this.holdingBackWeapon = tag.getBoolean("holdingBackWeapon");
    }

    public static final IAttachmentSerializer<CompoundTag, BackWeaponComponent> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public BackWeaponComponent read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                    BackWeaponComponent component = new BackWeaponComponent(holder);
                    component.load(tag, provider);
                    return component;
                }

                @Override
                public @Nullable CompoundTag write(BackWeaponComponent attachment, HolderLookup.Provider provider) {
                    return attachment.save(provider);
                }
            };

    // --- static helpers (preserve original API surface) ---

    public static BackWeaponComponent get(Player player) {
        return player.getData(ArsenalAttachments.BACK_WEAPON.get());
    }

    public static ItemStack getBackWeapon(Player player) {
        return get(player).getBackWeapon();
    }

    public static boolean setBackWeapon(Player player, ItemStack stack) {
        return get(player).setBackWeapon(stack);
    }

    public static SimpleContainer getBackWeaponInventory(Player player) {
        return get(player).getBackWeaponInventory();
    }

    public static boolean isHoldingBackWeapon(Player player) {
        return get(player).isHoldingBackWeapon();
    }

    public static void setHoldingBackWeapon(Player player, boolean holding) {
        if (player.level().isClientSide()) {
            PacketDistributor.sendToServer(new HoldWeaponPayload(holding));
            return;
        }
        get(player).setHoldingBackWeapon(holding);
    }

    /** Client-side application of a sync packet from the server. */
    public static void applyClientSync(Entity entity, ItemStack stack, boolean holding) {
        if (entity instanceof Player player) {
            BackWeaponComponent component = get(player);
            component.backWeapon.setItem(0, stack);
            component.holdingBackWeapon = holding;
        }
    }
}
