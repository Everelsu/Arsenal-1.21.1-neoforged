package dev.doctor4t.arsenal.client.render.item;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.index.ArsenalCosmetics;
import dev.doctor4t.arsenal.item.ScytheItem;
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

public class ScytheDynamicItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final List<ResourceLocation> MODELS_TO_REGISTER = new ArrayList<>();

    public static final Pair<ResourceLocation, ResourceLocation> DEFAULT_MODEL_IDENTIFIER  = registerVariantModelPair("");
    public static final Pair<ResourceLocation, ResourceLocation> CLOWN_MODEL_IDENTIFIER    = registerVariantModelPair(ScytheItem.Skin.CLOWN.getName());
    public static final Pair<ResourceLocation, ResourceLocation> CARRION_MODEL_IDENTIFIER  = registerVariantModelPair(ScytheItem.Skin.CARRION.getName());
    public static final Pair<ResourceLocation, ResourceLocation> GILDED_MODEL_IDENTIFIER   = registerVariantModelPair(ScytheItem.Skin.GILDED.getName());
    public static final Pair<ResourceLocation, ResourceLocation> ROZE_MODEL_IDENTIFIER     = registerVariantModelPair(ScytheItem.Skin.ROZE.getName());
    public static final Pair<ResourceLocation, ResourceLocation> FOLLY_MODEL_IDENTIFIER    = registerVariantModelPair(ScytheItem.Skin.FOLLY.getName());
    public static final Pair<ResourceLocation, ResourceLocation> SCISSORS_MODEL_IDENTIFIER = registerVariantModelPair(ScytheItem.Skin.SCISSORS.getName());

    public ScytheDynamicItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    private static @NotNull Pair<ResourceLocation, ResourceLocation> registerVariantModelPair(String name) {
        String s = "scythe" + (name.isEmpty() ? "" : "_") + name;
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
        ScytheItem.Skin skin = ScytheItem.Skin.fromString(ArsenalCosmetics.getSkin(stack));
        if (skin != null) {
            pair = switch (skin) {
                case CLOWN    -> CLOWN_MODEL_IDENTIFIER;
                case CARRION  -> CARRION_MODEL_IDENTIFIER;
                case GILDED   -> GILDED_MODEL_IDENTIFIER;
                case ROZE     -> ROZE_MODEL_IDENTIFIER;
                case FOLLY    -> FOLLY_MODEL_IDENTIFIER;
                case SCISSORS -> SCISSORS_MODEL_IDENTIFIER;
                default       -> DEFAULT_MODEL_IDENTIFIER;
            };
        }

        ResourceLocation chosen = inHand ? pair.getSecond() : pair.getFirst();
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(chosen));

        boolean inInventory = mode == ItemDisplayContext.GUI;
        // Use flat (GUI) lighting for inventory rendering so the model isn't 3D-shaded dark.
        if (inInventory) Lighting.setupForFlatItems();
        Minecraft.getInstance().getItemRenderer()
                .render(stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, model);
        if (vertexConsumers instanceof MultiBufferSource.BufferSource bufferSource) bufferSource.endBatch();
        if (inInventory) Lighting.setupFor3DItems();

        matrices.popPose();
    }
}
