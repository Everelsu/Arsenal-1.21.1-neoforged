package dev.doctor4t.arsenal.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.doctor4t.arsenal.Arsenal;
import dev.doctor4t.arsenal.entity.WeaponRackEntity;
import dev.doctor4t.arsenal.index.ArsenalTags;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class WeaponRackEntityRenderer<T extends WeaponRackEntity> extends EntityRenderer<T> {
    public static final ResourceLocation MODEL = Arsenal.id("block/weapon_rack");
    private final ItemRenderer itemRenderer;
    private final BlockRenderDispatcher blockRenderManager;

    public WeaponRackEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.blockRenderManager = context.getBlockRenderDispatcher();
    }

    @Override
    protected int getBlockLightLevel(T itemFrameEntity, BlockPos blockPos) {
        return itemFrameEntity.getType() == EntityType.GLOW_ITEM_FRAME
                ? Math.max(5, super.getBlockLightLevel(itemFrameEntity, blockPos))
                : super.getBlockLightLevel(itemFrameEntity, blockPos);
    }

    @Override
    public void render(T itemFrameEntity, float f, float g, PoseStack matrices, MultiBufferSource vertexConsumerProvider, int i) {
        super.render(itemFrameEntity, f, g, matrices, vertexConsumerProvider, i);
        matrices.pushPose();
        Direction direction = itemFrameEntity.getDirection();
        Vec3 vec3d = this.getRenderOffset(itemFrameEntity, g);
        matrices.translate(-vec3d.x(), -vec3d.y(), -vec3d.z());
        double d = 0.46875;
        matrices.translate((double) direction.getStepX() * d, (double) direction.getStepY() * d, (double) direction.getStepZ() * d);
        matrices.mulPose(Axis.XP.rotationDegrees(itemFrameEntity.getXRot()));
        matrices.mulPose(Axis.YP.rotationDegrees(180.0F - itemFrameEntity.getYRot()));
        boolean bl = itemFrameEntity.isInvisible();
        ItemStack itemStack = itemFrameEntity.getItem();

        int rotation = itemFrameEntity.getRotation();
        matrices.mulPose(Axis.ZP.rotationDegrees((float) rotation * 360.0F / 8.0F));

        if (!bl) {
            ModelManager bakedModelManager = this.blockRenderManager.getBlockModelShaper().getModelManager();
            matrices.pushPose();
            matrices.translate(-0.5F, -0.5F, -0.5F);
            this.blockRenderManager
                    .getModelRenderer()
                    .renderModel(
                            matrices.last(),
                            vertexConsumerProvider.getBuffer(Sheets.cutoutBlockSheet()),
                            null,
                            bakedModelManager.getModel(ModelResourceLocation.standalone(MODEL)),
                            1.0F,
                            1.0F,
                            1.0F,
                            i,
                            OverlayTexture.NO_OVERLAY
                    );
            matrices.popPose();
        }

        if (!itemStack.isEmpty()) {
            float zRot = 135f;
            float scale = .85f;
            if (itemStack.is(ArsenalTags.BIG_WEAPONS)) {
                scale = 1.6f;
            }
            if (itemStack.is(ArsenalTags.RANGED_WEAPONS)) {
                zRot = 45f;
            }
            if (itemStack.is(ArsenalTags.SHIELDS)) {
                scale = 1.8f;
                zRot = 0f;
            }
            if (itemStack.is(ArsenalTags.TRIDENTS)) {
                zRot = -45f;
            }

            matrices.mulPose(Axis.ZP.rotationDegrees(zRot));

            float offset = (float) Mth.getSeed(itemFrameEntity.getBlockX(), itemFrameEntity.getBlockY(), itemFrameEntity.getBlockZ()) * 0.00000000000000001f;
            if (bl) {
                matrices.translate(0.0F + offset, 0.0F + offset, 0.4375F + offset);
            } else {
                matrices.translate(0.0F + offset, 0.0F + offset, 0.3f + offset);
            }

            int light = this.getLight(itemFrameEntity, LightTexture.FULL_BRIGHT, i);

            matrices.scale(scale, scale, scale);

            this.itemRenderer
                    .renderStatic(
                            itemStack,
                            ItemDisplayContext.FIXED,
                            light,
                            OverlayTexture.NO_OVERLAY,
                            matrices,
                            vertexConsumerProvider,
                            itemFrameEntity.level(),
                            itemFrameEntity.getId()
                    );
        }

        matrices.popPose();
    }

    private int getLight(T itemFrame, int glowLight, int regularLight) {
        return itemFrame.getType() == EntityType.GLOW_ITEM_FRAME ? glowLight : regularLight;
    }

    @Override
    public Vec3 getRenderOffset(T itemFrameEntity, float f) {
        return new Vec3(
                (float) itemFrameEntity.getDirection().getStepX() * 0.3F,
                -0.25,
                (float) itemFrameEntity.getDirection().getStepZ() * 0.3F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(T itemFrameEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    protected boolean shouldShowName(T itemFrameEntity) {
        return false;
    }
}
