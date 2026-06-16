package io.github.blaezdev.rwbym.client.model;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class RWBYMPlayerArmorModel<T extends LivingEntity> extends PlayerModel<T> {
    public static final ModelLayerLocation DEFAULT_LAYER = new ModelLayerLocation(
            new ResourceLocation("rwbym", "player_armor_default"), "main");
    public static final ModelLayerLocation SLIM_LAYER = new ModelLayerLocation(
            new ResourceLocation("rwbym", "player_armor_slim"), "main");

    private final EquipmentSlot slot;

    public RWBYMPlayerArmorModel(ModelPart root, boolean slim, EquipmentSlot slot) {
        super(root, slim);
        this.slot = slot;
        setVisibleParts();
    }

    public static LayerDefinition createDefaultBodyLayer() {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(0.08F), false), 64, 64);
    }

    public static LayerDefinition createSlimBodyLayer() {
        return LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(0.08F), true), 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        prepareForArmorRender();
    }

    public void prepareForArmorRender() {
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
        setVisibleParts();
    }

    private void setVisibleParts() {
        setAllVisible(false);
        switch (this.slot) {
            case HEAD -> {
                this.head.visible = true;
                this.hat.visible = true;
            }
            case CHEST -> {
                this.body.visible = true;
                this.jacket.visible = true;
                this.leftArm.visible = true;
                this.leftSleeve.visible = true;
                this.rightArm.visible = true;
                this.rightSleeve.visible = true;
            }
            case LEGS -> {
                this.leftLeg.visible = true;
                this.leftPants.visible = true;
                this.rightLeg.visible = true;
                this.rightPants.visible = true;
            }
            case FEET -> {
            }
            default -> setAllVisible(true);
        }
    }
}
