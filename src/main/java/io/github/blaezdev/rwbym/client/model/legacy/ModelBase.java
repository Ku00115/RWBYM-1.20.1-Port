package io.github.blaezdev.rwbym.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ModelBase<T extends LivingEntity> extends EntityModel<T> {
    public int textureWidth = 64;
    public int textureHeight = 32;
    protected float limbSwing;
    protected float limbSwingAmount;
    protected float ageInTicks;
    protected float netHeadYaw;
    protected float headPitch;
    protected T renderEntity;

    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale) {
    }

    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
    }

    public void setLivingAnimations(LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        this.renderEntity = entity;
        setLivingAnimations(entity, limbSwing, limbSwingAmount, ageInTicks - entity.tickCount);
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 1.0F / 16.0F, entity);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        LegacyModelRenderContext.begin(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        try {
            render(renderEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, 1.0F / 16.0F);
        } finally {
            LegacyModelRenderContext.end();
        }
    }
}
