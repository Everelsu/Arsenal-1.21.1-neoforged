package dev.doctor4t.arsenal.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.joml.Vector3f;

public class AnchorBladeEntityModel extends EntityModel<AnchorbladeEntity> {
    private final ModelPart bone;
    private final ModelPart attachment;

    public AnchorBladeEntityModel(ModelPart root) {
        this.bone = root.getChild("bone");
        this.attachment = this.bone.getChild("attachment");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        PartDefinition bone = modelPartData.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(0, 14).addBox(-1.0F, -9.0F, -1.0F, 2.0F, 18.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 10).addBox(-7.0F, -11.0F, -1.0F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-9.0F, 1.0F, 0.0F, 18.0F, 10.0F, 0.0F, new CubeDeformation(0.001F)),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        bone.addOrReplaceChild("bone2", CubeListBuilder.create()
                        .texOffs(0, 54).addBox(-9.0F, -3.0F, 7.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.001F))
                        .texOffs(0, 48).addBox(-12.0F, -6.0F, 6.0F, 5.0F, 0.0F, 6.0F, new CubeDeformation(0.001F))
                        .texOffs(0, 59).addBox(-12.0F, 0.0F, 4.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.001F))
                        .texOffs(6, 40).addBox(-12.0F, 1.0F, 6.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.001F))
                        .texOffs(6, 36).addBox(-4.0F, 1.0F, 6.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.001F)),
                PartPose.offset(8.0F, 4.0F, -8.0F));

        bone.addOrReplaceChild("attachment", CubeListBuilder.create()
                        .texOffs(8, 14).addBox(-2.0F, -13.75F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -1.25F, 0.0F));
        return LayerDefinition.create(modelData, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int color) {
        this.bone.render(matrices, vertexConsumer, light, overlay, color);
    }

    public Vector3f getAttachmentPosition() {
        return new Vector3f(this.attachment.x / 16.0F, this.attachment.y / 16.0F, this.attachment.z / 16.0F);
    }

    @Override
    public void setupAnim(AnchorbladeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
