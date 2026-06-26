package io.github.blaezdev.rwbym.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.blaezdev.rwbym.client.model.legacy.ModelBase;
import io.github.blaezdev.rwbym.entity.RagoraEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

public class LegacyGrimmRenderer<T extends Mob> extends MobRenderer<T, ModelBase<T>> {
    private final ResourceLocation texture;
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final float translateX;
    private final float translateY;
    private final float translateZ;

    @SuppressWarnings("unchecked")
    public LegacyGrimmRenderer(EntityRendererProvider.Context context, ModelBase<?> model, ResourceLocation texture,
            float scale, float shadowRadius) {
        this(context, model, texture, scale, scale, scale, 0.0F, 0.0F, 0.0F, shadowRadius);
    }

    @SuppressWarnings("unchecked")
    public LegacyGrimmRenderer(EntityRendererProvider.Context context, ModelBase<?> model, ResourceLocation texture,
            float scaleX, float scaleY, float scaleZ, float translateX, float translateY, float translateZ,
            float shadowRadius) {
        super(context, (ModelBase<T>) model, shadowRadius);
        this.texture = texture;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.translateX = translateX;
        this.translateY = translateY;
        this.translateZ = translateZ;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        float ragoraScale = 1.0F;
        if (entity instanceof RagoraEntity ragora) {
            ragoraScale = ragora.getRenderScale();
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Legacy renderers used per-axis preRenderCallback transforms; keep them data-driven in 1.20.
        poseStack.scale(this.scaleX * ragoraScale, this.scaleY * ragoraScale, this.scaleZ * ragoraScale);
        poseStack.translate(this.translateX, this.translateY, this.translateZ);
    }
}
