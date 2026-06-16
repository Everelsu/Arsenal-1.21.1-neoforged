package dev.doctor4t.arsenal.client.render.entity;

import dev.doctor4t.arsenal.Arsenal;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public interface ModEntityModelLayers {
    Map<ModelLayerLocation, Supplier<LayerDefinition>> MODEL_LAYERS = new LinkedHashMap<>();

    ModelLayerLocation ANCHORBLADE = createModelLayer("anchorblade", AnchorBladeEntityModel::getTexturedModelData);

    private static ModelLayerLocation createModelLayer(String name, Supplier<LayerDefinition> provider) {
        ModelLayerLocation layer = create(name, "main");
        MODEL_LAYERS.put(layer, provider);
        return layer;
    }

    private static ModelLayerLocation create(String id, String layer) {
        return new ModelLayerLocation(Arsenal.id(id), layer);
    }
}
