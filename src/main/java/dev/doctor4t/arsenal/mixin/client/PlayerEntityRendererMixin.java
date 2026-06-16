package dev.doctor4t.arsenal.mixin.client;

import dev.doctor4t.arsenal.client.render.feature.BackWeaponFeatureRenderer;
import dev.doctor4t.arsenal.index.ArsenalEnchantments;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.util.AnchorOwner;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public PlayerEntityRendererMixin(EntityRendererProvider.Context ctx, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(ctx, model, shadowRadius);
    }

    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private static void arsenal$swordPoses(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(ArsenalItems.ANCHORBLADE)) {
            boolean reeling = ArsenalEnchantments.getLevel(ArsenalEnchantments.REELING, stack, player.level()) > 0;
            if (player instanceof AnchorOwner owner && owner.arsenal$isAnchorActive(hand, reeling)) {
                cir.setReturnValue(HumanoidModel.ArmPose.EMPTY);
            }
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void arsenal$backBlade(EntityRendererProvider.Context ctx, boolean slim, CallbackInfo ci) {
        this.addLayer(new BackWeaponFeatureRenderer(this));
    }
}
