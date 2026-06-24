package io.github.blaezdev.rwbym.client.model.legacy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

final class LegacyModelRenderContext {
    private static final ThreadLocal<Frame> FRAME = ThreadLocal.withInitial(Frame::new);

    static void begin(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        FRAME.get().set(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    static Frame get() {
        return FRAME.get();
    }

    static void end() {
        FRAME.get().clear();
    }

    static final class Frame {
        private PoseStack poseStack;
        private VertexConsumer consumer;
        private int packedLight;
        private int packedOverlay;
        private float red;
        private float green;
        private float blue;
        private float alpha;

        void set(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                float red, float green, float blue, float alpha) {
            this.poseStack = poseStack;
            this.consumer = consumer;
            this.packedLight = packedLight;
            this.packedOverlay = packedOverlay;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        void clear() {
            this.poseStack = null;
            this.consumer = null;
        }

        PoseStack poseStack() {
            return this.poseStack;
        }

        VertexConsumer consumer() {
            return this.consumer;
        }

        int packedLight() {
            return this.packedLight;
        }

        int packedOverlay() {
            return this.packedOverlay;
        }

        float red() {
            return this.red;
        }

        float green() {
            return this.green;
        }

        float blue() {
            return this.blue;
        }

        float alpha() {
            return this.alpha;
        }
    }

    private LegacyModelRenderContext() {
    }
}
