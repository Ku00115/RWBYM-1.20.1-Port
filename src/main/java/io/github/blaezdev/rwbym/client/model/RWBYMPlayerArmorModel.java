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
    private static final CubeDeformation PLAYER_SKIN_ARMOR_DEFORMATION = new CubeDeformation(0.28F);

    private final EquipmentSlot slot;
    private final boolean includeCompanionParts;

    public RWBYMPlayerArmorModel(ModelPart root, boolean slim, EquipmentSlot slot) {
        this(root, slim, slot, false);
    }

    public RWBYMPlayerArmorModel(ModelPart root, boolean slim, EquipmentSlot slot, boolean includeCompanionParts) {
        super(root, slim);
        this.slot = slot;
        this.includeCompanionParts = includeCompanionParts;
        setVisibleParts();
    }

    public static LayerDefinition createDefaultBodyLayer() {
        return LayerDefinition.create(PlayerModel.createMesh(PLAYER_SKIN_ARMOR_DEFORMATION, false), 64, 64);
    }

    public static LayerDefinition createSlimBodyLayer() {
        return LayerDefinition.create(PlayerModel.createMesh(PLAYER_SKIN_ARMOR_DEFORMATION, true), 64, 64);
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
                if (this.includeCompanionParts) {
                    setUpperBodyVisible(true);
                    setLegsVisible(true);
                }
            }
            case CHEST -> {
                setUpperBodyVisible(true);
                if (this.includeCompanionParts) {
                    setLegsVisible(true);
                }
            }
            case LEGS -> {
                setLegsVisible(true);
                if (this.includeCompanionParts) {
                    setUpperBodyVisible(true);
                }
            }
            case FEET -> {
                setLegsVisible(true);
            }
            default -> setAllVisible(true);
        }
    }

    private void setUpperBodyVisible(boolean visible) {
        this.body.visible = visible;
        this.jacket.visible = visible;
        this.leftArm.visible = visible;
        this.leftSleeve.visible = visible;
        this.rightArm.visible = visible;
        this.rightSleeve.visible = visible;
    }

    private void setLegsVisible(boolean visible) {
        this.leftLeg.visible = visible;
        this.leftPants.visible = visible;
        this.rightLeg.visible = visible;
        this.rightPants.visible = visible;
    }
}
