package dev.doctor4t.arsenal.client.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.index.ArsenalCosmetics;
import dev.doctor4t.arsenal.item.AnchorbladeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnchorbladeDynamicItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final List<ResourceLocation> MODELS_TO_REGISTER = new ArrayList<>();

    public static final Pair<ResourceLocation, ResourceLocation> DEFAULT_MODEL_IDENTIFIER   = registerVariantModelPair("");
    public static final Pair<ResourceLocation, ResourceLocation> LUXINTRUS_MODEL_IDENTIFIER = registerVariantModelPair(AnchorbladeItem.Skin.LUXINTRUS.getName());
    public static final Pair<ResourceLocation, ResourceLocation> CARRION_MODEL_IDENTIFIER   = registerVariantModelPair(AnchorbladeItem.Skin.CARRION.getName());
    public static final Pair<ResourceLocation, ResourceLocation> GILDED_MODEL_IDENTIFIER    = registerVariantModelPair(AnchorbladeItem.Skin.GILDED.getName());
    public static final Pair<ResourceLocation, ResourceLocation> WINSWEEP_MODEL_IDENTIFIER  = registerVariantModelPair(AnchorbladeItem.Skin.WINSWEEP.getName());
    public static final Pair<ResourceLocation, ResourceLocation> AMBESSA_MODEL_IDENTIFIER   = registerVariantModelPair(AnchorbladeItem.Skin.AMBESSA.getName());

    public AnchorbladeDynamicItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private static @NotNull Pair<ResourceLocation, ResourceLocation> registerVariantModelPair(String name) {
        String s = "anchorblade" + (name.isEmpty() ? "" : "_") + name;
        ResourceLocation inv   = Arsenal.id("item/" + s + "_inventory");
        ResourceLocation inHnd = Arsenal.id("item/" + s + "_in_hand");
        MODELS_TO_REGISTER.add(inv);
        MODELS_TO_REGISTER.add(inHnd);
        return new Pair<>(inv, inHnd);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        boolean leftHanded = mode == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        boolean inHand = mode.firstPerson()
                || mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || mode == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || mode == ItemDisplayContext.HEAD
                || mode == ItemDisplayContext.FIXED;

        matrices.pushPose();
        matrices.translate(.5, .5, .5);

        Pair<ResourceLocation, ResourceLocation> pair = DEFAULT_MODEL_IDENTIFIER;
        AnchorbladeItem.Skin skin = AnchorbladeItem.Skin.fromString(ArsenalCosmetics.getSkin(stack));
        if (skin != null) {
            pair = switch (skin) {
                case LUXINTRUS -> LUXINTRUS_MODEL_IDENTIFIER;
                case CARRION   -> CARRION_MODEL_IDENTIFIER;
                case GILDED    -> GILDED_MODEL_IDENTIFIER;
                case WINSWEEP  -> WINSWEEP_MODEL_IDENTIFIER;
                case AMBESSA   -> AMBESSA_MODEL_IDENTIFIER;
                default        -> DEFAULT_MODEL_IDENTIFIER;
            };
        }

        ResourceLocation chosen = inHand ? pair.getSecond() : pair.getFirst();
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(chosen));

        Minecraft.getInstance().getItemRenderer()
                .render(stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, model);

        matrices.popPose();
    }
}
