package io.github.blaezdev.rwbym.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;

public class TexturedGrimmRenderer extends ZombieRenderer {
    private final ResourceLocation texture;
    private final float scale;

    public TexturedGrimmRenderer(EntityRendererProvider.Context context, ResourceLocation texture, float scale) {
        super(context);
        this.texture = texture;
        this.scale = scale;
    }

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.world.entity.monster.Zombie entity) {
        return this.texture;
    }

    @Override
    protected void scale(net.minecraft.world.entity.monster.Zombie entity, PoseStack poseStack, float partialTick) {
        super.scale(entity, poseStack, partialTick);
        poseStack.scale(this.scale, this.scale, this.scale);
    }
}
