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
    private final float scale;

    @SuppressWarnings("unchecked")
    public LegacyGrimmRenderer(EntityRendererProvider.Context context, ModelBase<?> model, ResourceLocation texture,
            float scale, float shadowRadius) {
        super(context, (ModelBase<T>) model, shadowRadius);
        this.texture = texture;
        this.scale = scale;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        float actualScale = scale;
        if (entity instanceof RagoraEntity ragora) {
            actualScale *= ragora.getRenderScale();
        }
        poseStack.scale(actualScale, actualScale, actualScale);
    }
}
