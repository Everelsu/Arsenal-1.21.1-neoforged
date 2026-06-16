package dev.doctor4t.arsenal.client.render.item;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.index.ArsenalCosmetics;
import dev.doctor4t.arsenal.item.AnchorbladeItem;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AnchorbladeDynamicItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    // Models registered via addModels(Identifier) are stored by plain Identifier (no ModelIdentifier).
    // They must be retrieved with FabricBakedModelManager.getModel(Identifier), not the vanilla getModel(ModelIdentifier).
    public static final List<Identifier> MODELS_TO_REGISTER = new ArrayList<>();

    // "item/" prefix so the path resolves to assets/arsenal/models/item/anchorblade_*.json
    public static final Pair<Identifier, Identifier> DEFAULT_MODEL_IDENTIFIER   = registerVariantModelPair("");
    public static final Pair<Identifier, Identifier> LUXINTRUS_MODEL_IDENTIFIER = registerVariantModelPair(AnchorbladeItem.Skin.LUXINTRUS.getName());
    public static final Pair<Identifier, Identifier> CARRION_MODEL_IDENTIFIER   = registerVariantModelPair(AnchorbladeItem.Skin.CARRION.getName());
    public static final Pair<Identifier, Identifier> GILDED_MODEL_IDENTIFIER    = registerVariantModelPair(AnchorbladeItem.Skin.GILDED.getName());
    public static final Pair<Identifier, Identifier> WINSWEEP_MODEL_IDENTIFIER  = registerVariantModelPair(AnchorbladeItem.Skin.WINSWEEP.getName());
    public static final Pair<Identifier, Identifier> AMBESSA_MODEL_IDENTIFIER   = registerVariantModelPair(AnchorbladeItem.Skin.AMBESSA.getName());

    private static @NotNull Pair<Identifier, Identifier> registerVariantModelPair(String name) {
        String s = "anchorblade" + (name.isEmpty() ? "" : "_") + name;
        Identifier inv   = Arsenal.id("item/" + s + "_inventory");
        Identifier inHnd = Arsenal.id("item/" + s + "_in_hand");
        MODELS_TO_REGISTER.add(inv);
        MODELS_TO_REGISTER.add(inHnd);
        return new Pair<>(inv, inHnd);
    }

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        boolean leftHanded  = mode == ModelTransformationMode.FIRST_PERSON_LEFT_HAND
                || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND;
        boolean inHand      = mode.isFirstPerson()
                || mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND
                || mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND
                || mode == ModelTransformationMode.HEAD
                || mode == ModelTransformationMode.FIXED;
        boolean inInventory = mode == ModelTransformationMode.GUI;

        matrices.push();
        matrices.translate(.5, .5, .5);

        Pair<Identifier, Identifier> pair = DEFAULT_MODEL_IDENTIFIER;
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

        // Models registered via addModels(Identifier) must be retrieved with FabricBakedModelManager.getModel(Identifier),
        // not the vanilla BakedModelManager.getModel(ModelIdentifier) — those are different registries.
        FabricBakedModelManager fabricManager = (FabricBakedModelManager) MinecraftClient.getInstance().getBakedModelManager();
        BakedModel model = fabricManager.getModel(inHand ? pair.getRight() : pair.getLeft());

        if (inInventory) DiffuseLighting.disableGuiDepthLighting();

        MinecraftClient.getInstance().getItemRenderer()
                .renderItem(stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, model);

        if (vertexConsumers instanceof VertexConsumerProvider.Immediate imm) imm.draw();
        if (inInventory) DiffuseLighting.enableGuiDepthLighting();

        matrices.pop();
    }
}