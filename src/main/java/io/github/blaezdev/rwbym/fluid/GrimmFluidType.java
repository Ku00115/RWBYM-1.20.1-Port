package io.github.blaezdev.rwbym.fluid;

import io.github.blaezdev.rwbym.RWBYM;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;

import java.util.function.Consumer;

public class GrimmFluidType extends FluidType {
    private static final ResourceLocation STILL_TEXTURE =
            new ResourceLocation(RWBYM.MOD_ID, "block/fluid/grimm/fluidgrimm_still");
    private static final ResourceLocation FLOWING_TEXTURE =
            new ResourceLocation(RWBYM.MOD_ID, "block/fluid/grimm/fluidgrimm_flow");

    public GrimmFluidType() {
        super(FluidType.Properties.create()
                .descriptionId("fluid.grimm")
                .canSwim(false)
                .canDrown(false)
                .supportsBoating(false)
                .density(1800)
                .temperature(290)
                .viscosity(5000));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING_TEXTURE;
            }

            @Override
            public int getTintColor() {
                return 0xFF1A0B16;
            }
        });
    }
}
