package io.github.blaezdev.rwbym.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ModelBox {
    private final float x1;
    private final float y1;
    private final float z1;
    private final float x2;
    private final float y2;
    private final float z2;
    private final float textureWidth;
    private final float textureHeight;
    private final float u;
    private final float v;
    private final float width;
    private final float height;
    private final float depth;

    public ModelBox(ModelRenderer renderer, int texU, int texV, float x, float y, float z,
            int width, int height, int depth, float grow, boolean mirror) {
        this.x1 = x - grow;
        this.y1 = y - grow;
        this.z1 = z - grow;
        this.x2 = x + width + grow;
        this.y2 = y + height + grow;
        this.z2 = z + depth + grow;
        this.textureWidth = renderer.model.textureWidth;
        this.textureHeight = renderer.model.textureHeight;
        this.u = texU;
        this.v = texV;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    void render(PoseStack poseStack, VertexConsumer consumer, float scale, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        float minX = x1 * scale;
        float minY = y1 * scale;
        float minZ = z1 * scale;
        float maxX = x2 * scale;
        float maxY = y2 * scale;
        float maxZ = z2 * scale;
        float u0 = u;
        float u1 = u + depth;
        float u2 = u + depth + width;
        float u3 = u + depth + width + width;
        float u4 = u + depth + width + depth;
        float v0 = v;
        float v1 = v + depth;
        float v2 = v + depth + height;
        quad(consumer, pose, normal, minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ,
                0, 0, 1, uv(u1), uv(v1, false), uv(u2), uv(v2, false), packedLight, packedOverlay, red, green, blue, alpha);
        quad(consumer, pose, normal, maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ,
                0, 0, -1, uv(u3), uv(v1, false), uv(u4), uv(v2, false), packedLight, packedOverlay, red, green, blue, alpha);
        quad(consumer, pose, normal, minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ,
                -1, 0, 0, uv(u0), uv(v1, false), uv(u1), uv(v2, false), packedLight, packedOverlay, red, green, blue, alpha);
        quad(consumer, pose, normal, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ,
                1, 0, 0, uv(u2), uv(v1, false), uv(u3), uv(v2, false), packedLight, packedOverlay, red, green, blue, alpha);
        quad(consumer, pose, normal, minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ,
                0, -1, 0, uv(u1), uv(v0, false), uv(u2), uv(v1, false), packedLight, packedOverlay, red, green, blue, alpha);
        quad(consumer, pose, normal, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ,
                0, 1, 0, uv(u2), uv(v0, false), uv(u3), uv(v1, false), packedLight, packedOverlay, red, green, blue, alpha);
    }

    private static void quad(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4,
            float nx, float ny, float nz, float minU, float minV, float maxU, float maxV, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        vertex(consumer, pose, normal, x1, y1, z1, minU, minV, nx, ny, nz, packedLight, packedOverlay, red, green, blue, alpha);
        vertex(consumer, pose, normal, x2, y2, z2, maxU, minV, nx, ny, nz, packedLight, packedOverlay, red, green, blue, alpha);
        vertex(consumer, pose, normal, x3, y3, z3, maxU, maxV, nx, ny, nz, packedLight, packedOverlay, red, green, blue, alpha);
        vertex(consumer, pose, normal, x4, y4, z4, minU, maxV, nx, ny, nz, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private float uv(float value) {
        return uv(value, true);
    }

    private float uv(float value, boolean horizontal) {
        return value / (horizontal ? textureWidth : textureHeight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
            float x, float y, float z, float u, float v, float nx, float ny, float nz,
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        consumer.vertex(pose, x, y, z).color(red, green, blue, alpha).uv(u, v)
                .overlayCoords(packedOverlay).uv2(packedLight).normal(normal, nx, ny, nz).endVertex();
    }
}
