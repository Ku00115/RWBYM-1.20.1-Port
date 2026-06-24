package io.github.blaezdev.rwbym.client.model.legacy;

import net.minecraft.world.entity.Entity;

public class ModelBiped extends ModelBase {
    public ModelRenderer bipedHead = new ModelRenderer(this);
    public ModelRenderer bipedHeadwear = new ModelRenderer(this);
    public ModelRenderer bipedBody = new ModelRenderer(this);
    public ModelRenderer bipedRightArm = new ModelRenderer(this);
    public ModelRenderer bipedLeftArm = new ModelRenderer(this);
    public ModelRenderer bipedRightLeg = new ModelRenderer(this);
    public ModelRenderer bipedLeftLeg = new ModelRenderer(this);
    public boolean isChild;

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch, float scale) {
        bipedHead.render(scale);
        bipedHeadwear.render(scale);
        bipedBody.render(scale);
        bipedRightArm.render(scale);
        bipedLeftArm.render(scale);
        bipedRightLeg.render(scale);
        bipedLeftLeg.render(scale);
    }

    public static void copyModelAngles(ModelRenderer source, ModelRenderer target) {
        target.rotateAngleX = source.rotateAngleX;
        target.rotateAngleY = source.rotateAngleY;
        target.rotateAngleZ = source.rotateAngleZ;
        target.rotationPointX = source.rotationPointX;
        target.rotationPointY = source.rotationPointY;
        target.rotationPointZ = source.rotationPointZ;
    }
}
