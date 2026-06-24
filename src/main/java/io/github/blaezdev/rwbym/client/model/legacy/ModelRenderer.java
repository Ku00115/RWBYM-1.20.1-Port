package io.github.blaezdev.rwbym.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ModelRenderer {
    public final List<ModelBox> cubeList = new ArrayList<>();
    private final List<ModelRenderer> children = new ArrayList<>();
    final ModelBase<?> model;
    public float rotationPointX;
    public float rotationPointY;
    public float rotationPointZ;
    public float rotateAngleX;
    public float rotateAngleY;
    public float rotateAngleZ;
    public float offsetX;
    public float offsetY;
    public float offsetZ;
    public boolean mirror;
    private int textureOffsetX;
    private int textureOffsetY;

    public ModelRenderer(ModelBase<?> model) {
        this(model, 0, 0);
    }

    public ModelRenderer(ModelBase<?> model, int textureOffsetX, int textureOffsetY) {
        this.model = model;
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
    }

    public void setRotationPoint(float x, float y, float z) {
        this.rotationPointX = x;
        this.rotationPointY = y;
        this.rotationPointZ = z;
    }

    public ModelRenderer setTextureOffset(int textureOffsetX, int textureOffsetY) {
        this.textureOffsetX = textureOffsetX;
        this.textureOffsetY = textureOffsetY;
        return this;
    }

    public void setTextureSize(int textureWidth, int textureHeight) {
        this.model.textureWidth = textureWidth;
        this.model.textureHeight = textureHeight;
    }

    public void addChild(ModelRenderer child) {
        this.children.add(child);
    }

    public void addBox(float x, float y, float z, int width, int height, int depth) {
        addBox(x, y, z, width, height, depth, 0.0F);
    }

    public void addBox(float x, float y, float z, int width, int height, int depth, float grow) {
        this.cubeList.add(new ModelBox(this, textureOffsetX, textureOffsetY, x, y, z, width, height, depth, grow, mirror));
    }

    public void addBox(double x, double y, double z, int width, int height, int depth, float grow) {
        addBox((float) x, (float) y, (float) z, width, height, depth, grow);
    }

    public void addBox(String name, float x, float y, float z, int width, int height, int depth) {
        addBox(x, y, z, width, height, depth);
    }

    public void addBox(String name, float x, float y, float z, int width, int height, int depth, float grow) {
        addBox(x, y, z, width, height, depth, grow);
    }

    public void render(float scale) {
        LegacyModelRenderContext.Frame frame = LegacyModelRenderContext.get();
        if (frame == null) {
            return;
        }
        PoseStack poseStack = frame.poseStack();
        poseStack.pushPose();
        poseStack.translate(this.offsetX, this.offsetY, this.offsetZ);
        poseStack.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
        if (this.rotateAngleZ != 0.0F) {
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotation(this.rotateAngleZ));
        }
        if (this.rotateAngleY != 0.0F) {
            poseStack.mulPose(com.mojang.math.Axis.YP.rotation(this.rotateAngleY));
        }
        if (this.rotateAngleX != 0.0F) {
            poseStack.mulPose(com.mojang.math.Axis.XP.rotation(this.rotateAngleX));
        }
        for (ModelBox cube : this.cubeList) {
            cube.render(poseStack, frame.consumer(), scale, frame.packedLight(), frame.packedOverlay(),
                    frame.red(), frame.green(), frame.blue(), frame.alpha());
        }
        for (ModelRenderer child : this.children) {
            child.render(scale);
        }
        poseStack.popPose();
    }

    public void renderWithRotation(float scale) {
        render(scale);
    }
}
