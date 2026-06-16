package dev.doctor4t.arsenal.client.render.item;

import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.util.ArsenalConfig;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TridentDynamicItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    // Models registered via addModels(Identifier) are stored by plain Identifier (no ModelIdentifier).
    // They must be retrieved with FabricBakedModelManager.getModel(Identifier), not the vanilla getModel(ModelIdentifier).
    public static final List<Identifier> MODELS_TO_REGISTER = new ArrayList<>();

    public static final Pair<Identifier, Identifier> DEFAULT_MODEL_IDENTIFIER = registerVariantModelPair("");

    private static @NotNull Pair<Identifier, Identifier> registerVariantModelPair(String name) {
        String s = "trident" + (name.isEmpty() ? "" : "_") + name;
        Identifier inv   = Arsenal.id(s + "_inventory");
        Identifier inHnd = Arsenal.id(s + "_in_hand");
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

        // Custom models were loaded via addModels(Identifier) so they live in the Fabric model registry.
        // Vanilla TRIDENT_IN_HAND is a real ModelIdentifier registered by vanilla — use the vanilla lookup for that.
        BakedModel model = ArsenalConfig.CUSTOM_TRIDENT_RENDERING
                ? ((FabricBakedModelManager) MinecraftClient.getInstance().getBakedModelManager())
                  .getModel(inHand ? pair.getRight() : pair.getLeft())
                : MinecraftClient.getInstance().getBakedModelManager().getModel(ItemRenderer.TRIDENT_IN_HAND);

        if (inInventory) DiffuseLighting.disableGuiDepthLighting();

        MinecraftClient.getInstance().getItemRenderer()
                .renderItem(stack, mode, leftHanded, matrices, vertexConsumers, light, overlay, model);

        if (vertexConsumers instanceof VertexConsumerProvider.Immediate imm) imm.draw();
        if (inInventory) DiffuseLighting.enableGuiDepthLighting();

        matrices.pop();
    }
}