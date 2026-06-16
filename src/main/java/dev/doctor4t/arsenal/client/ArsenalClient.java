package dev.doctor4t.arsenal.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.client.render.entity.AnchorbladeEntityRenderer;
import dev.doctor4t.arsenal.client.render.entity.BloodScytheEntityRenderer;
import dev.doctor4t.arsenal.client.render.entity.ModEntityModelLayers;
import dev.doctor4t.arsenal.client.render.entity.WeaponRackEntityRenderer;
import dev.doctor4t.arsenal.client.render.item.AnchorbladeDynamicItemRenderer;
import dev.doctor4t.arsenal.client.render.item.ScytheDynamicItemRenderer;
import dev.doctor4t.arsenal.client.particle.BloodBubbleParticle;
import dev.doctor4t.arsenal.client.particle.BloodBubbleSplatterParticle;
import dev.doctor4t.arsenal.client.particle.ShockwaveParticle;
import dev.doctor4t.arsenal.client.particle.SweepAttackParticle;
import dev.doctor4t.arsenal.index.ArsenalEntities;
import dev.doctor4t.arsenal.index.ArsenalItems;
import dev.doctor4t.arsenal.index.ArsenalParticles;
import dev.doctor4t.arsenal.item.AnchorbladeItem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.blockentity.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Arsenal.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ArsenalClient {
    public static final KeyMapping weaponKeybind = new KeyMapping(
            "key.arsenal.select_weapon", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.arsenal");
    public static final KeyMapping swapKeybind = new KeyMapping(
            "key.arsenal.swap_weapon", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.arsenal");

    private ArsenalClient() {}

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(weaponKeybind);
        event.register(swapKeybind);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ArsenalEntities.BLOOD_SCYTHE.get(), BloodScytheEntityRenderer::new);
        event.registerEntityRenderer(ArsenalEntities.ANCHORBLADE.get(), AnchorbladeEntityRenderer::new);
        event.registerEntityRenderer(ArsenalEntities.WEAPON_RACK.get(), WeaponRackEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ModEntityModelLayers.MODEL_LAYERS.forEach(event::registerLayerDefinition);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ArsenalParticles.SWEEP_PARTICLE.get(), SweepAttackParticle.Factory::new);
        event.registerSpriteSet(ArsenalParticles.SWEEP_SHADOW_PARTICLE.get(), SweepAttackParticle.Factory::new);
        event.registerSpriteSet(ArsenalParticles.BLOOD_BUBBLE.get(), BloodBubbleParticle.Factory::new);
        event.registerSpriteSet(ArsenalParticles.BLOOD_BUBBLE_SPLATTER.get(), BloodBubbleSplatterParticle.Factory::new);
        event.registerSpriteSet(ArsenalParticles.SHOCKWAVE.get(), ShockwaveParticle.Factory::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        ScytheDynamicItemRenderer.MODELS_TO_REGISTER.forEach(id -> event.register(ModelResourceLocation.standalone(id)));
        AnchorbladeDynamicItemRenderer.MODELS_TO_REGISTER.forEach(id -> event.register(ModelResourceLocation.standalone(id)));
        event.register(ModelResourceLocation.standalone(WeaponRackEntityRenderer.MODEL));
        for (AnchorbladeItem.Skin skin : AnchorbladeItem.Skin.values()) {
            event.register(ModelResourceLocation.standalone(skin.anchorbladeEntityModel));
        }
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = new ScytheDynamicItemRenderer();
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, ArsenalItems.SCYTHE.get());

        event.registerItem(new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = new AnchorbladeDynamicItemRenderer();
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        }, ArsenalItems.ANCHORBLADE.get());
    }
}
