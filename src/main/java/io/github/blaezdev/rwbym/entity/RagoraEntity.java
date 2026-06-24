package io.github.blaezdev.rwbym.entity;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

public class RagoraEntity extends TamableAnimal {
    private static final int TRACE_LENGTH = 200;
    private static final double FOLLOW_SPEED = 0.5D;
    private static final double MELEE_ORBIT_DISTANCE = 1.0D;
    private static final double RANGE_ORBIT_DISTANCE = 10.0D;
    private static final double RANGE_ORBIT_HEIGHT = 5.0D;
    private static final ResourceLocation RAGORA_FIREBALL_ID = new ResourceLocation(RWBYM.MOD_ID, "ragorafireball");

    private final Vec3[] ownerTrace = new Vec3[TRACE_LENGTH];
    private Vec3 targetPos;
    private double targetSpeed;
    private AttackPhase attackPhase = AttackPhase.MELEE;

    public RagoraEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.targetPos = Vec3.ZERO;
        this.targetSpeed = FOLLOW_SPEED;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RagoraFollowOwnerGoal());
        this.goalSelector.addGoal(1, new RagoraMeleeGoal());
        this.goalSelector.addGoal(1, new RagoraRangeGoal());
    }

    @Override
    public void aiStep() {
        this.setNoGravity(true);
        super.aiStep();
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();
        this.fallDistance = 0.0F;

        if (this.level().isClientSide()) {
            spawnTrailParticles();
            return;
        }

        Player owner = getPlayerOwner();
        if (!hasActiveRagoraOwner(owner)) {
            this.discard();
            return;
        }

        recordOwnerTrace(owner);
        updateOwnerTarget(owner);
        applyFlightMotion();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.level().broadcastEntityEvent(this, (byte) 4);
        boolean hurt = target.hurt(this.damageSources().mobAttack(this),
                (float) this.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE));
        if (hurt) {
            target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.4D, 0.0D));
            this.doEnchantDamageEffects(this, target);
            if (target instanceof Monster monster) {
                monster.setTarget(this);
            }
        }
        return hurt;
    }

    @Override
    public boolean canMate(net.minecraft.world.entity.animal.Animal otherAnimal) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBeLeashed(net.minecraft.world.entity.player.Player player) {
        return false;
    }

    @Override
    public MobType getMobType() {
        // Original Ragora used the Illager creature attribute despite being a tameable flying summon.
        return MobType.ILLAGER;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SQUID_DEATH;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WOLF_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource damageSource) {
        return false;
    }

    public void bindOwner(Player owner) {
        this.tame(owner);
        this.setPos(owner.getX(), owner.getY() + owner.getBbHeight(), owner.getZ());
        this.setYRot(owner.getYRot());
        this.setOldPosAndRot();
        this.targetPos = this.position();
        Arrays.fill(this.ownerTrace, owner.position());
    }

    public float getRenderScale() {
        return Math.min(1.0F, this.tickCount / 50.0F);
    }

    private void updateOwnerTarget(Player owner) {
        LivingEntity currentTarget = this.getTarget();
        if (currentTarget != null && (!currentTarget.isAlive() || currentTarget.distanceToSqr(owner) > 256.0D
                || currentTarget instanceof ZombifiedPiglin piglin
                && piglin.getTarget() != this && piglin.getTarget() != owner)) {
            this.setTarget(null);
            this.attackPhase = AttackPhase.MELEE;
            currentTarget = null;
        }

        if (this.tickCount <= 100) {
            return;
        }

        LivingEntity ownerAttackTarget = owner.getLastHurtMob();
        if (isValidOwnerDirectedTarget(owner, ownerAttackTarget) && ownerAttackTarget != currentTarget) {
            this.setTarget(ownerAttackTarget);
            this.attackPhase = AttackPhase.MELEE;
            return;
        }

        LivingEntity ownerRevengeTarget = owner.getLastHurtByMob();
        if (isValidOwnerDirectedTarget(owner, ownerRevengeTarget) && ownerRevengeTarget != currentTarget) {
            this.setTarget(ownerRevengeTarget);
            this.attackPhase = AttackPhase.MELEE;
            return;
        }

        if (currentTarget == null) {
            Monster nearest = null;
            double bestDistance = Double.MAX_VALUE;
            for (Monster monster : this.level().getEntitiesOfClass(Monster.class,
                    owner.getBoundingBox().inflate(16.0D))) {
                if (!isValidNearbyThreat(owner, monster)) {
                    continue;
                }
                double distance = monster.distanceToSqr(owner);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    nearest = monster;
                }
            }
            if (nearest != null) {
                this.setTarget(nearest);
                this.attackPhase = AttackPhase.MELEE;
            }
        }
    }

    private boolean isValidOwnerDirectedTarget(Player owner, @Nullable LivingEntity target) {
        // Original owner-hurt target tasks followed the player's target without restricting it to EntityMob.
        return target != null && target.isAlive() && target != this && target != owner;
    }

    private boolean isValidNearbyThreat(Player owner, @Nullable Monster monster) {
        if (monster == null || !monster.isAlive()) {
            return false;
        }
        return !(monster instanceof ZombifiedPiglin piglin)
                || piglin.getTarget() == this
                || piglin.getTarget() == owner;
    }

    private void recordOwnerTrace(Player owner) {
        for (int i = TRACE_LENGTH - 1; i > 0; i--) {
            this.ownerTrace[i] = this.ownerTrace[i - 1];
        }
        this.ownerTrace[0] = owner.position();
    }

    private void applyFlightMotion() {
        double dx = this.targetPos.x - this.getX();
        double dy = this.targetPos.y - this.getY();
        double dz = this.targetPos.z - this.getZ();
        double accel = 0.25D;
        Vec3 currentMotion = this.getDeltaMovement();

        if (dx * dx + dy * dy + dz * dz < 1.0E-4D) {
            this.setDeltaMovement(currentMotion.scale(0.8D));
            return;
        }

        Vec3 max = new Vec3(Math.abs(dx), Math.abs(dy), Math.abs(dz));
        if (max.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 clamped = max.normalize().scale(this.targetSpeed * 2.0D / Math.sqrt(accel));
        double motionX = currentMotion.x + Mth.clamp(dx, -clamped.x, clamped.x) * accel
                - currentMotion.x * 2.0D * Math.sqrt(accel);
        double motionY = currentMotion.y + Mth.clamp(dy, -clamped.y, clamped.y) * accel
                - currentMotion.y * 2.0D * Math.sqrt(accel);
        double motionZ = currentMotion.z + Mth.clamp(dz, -clamped.z, clamped.z) * accel
                - currentMotion.z * 2.0D * Math.sqrt(accel);

        this.setDeltaMovement(motionX, motionY, motionZ);
        this.hasImpulse = true;
    }

    private void setFlightTarget(Vec3 pos, double speed) {
        this.targetPos = pos;
        this.targetSpeed = speed;
    }

    private void faceTowards(Vec3 target) {
        Vec3 delta = target.subtract(this.position());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) (-Mth.atan2(delta.x, delta.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (-Mth.atan2(delta.y, horizontal) * Mth.RAD_TO_DEG);
        this.setYRot(yaw);
        this.setXRot(pitch);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
        this.getLookControl().setLookAt(target.x, target.y, target.z, 360.0F, 90.0F);
    }

    private boolean canSeePos(Vec3 pos) {
        return this.level().clip(new ClipContext(
                new Vec3(this.getX(), this.getEyeY(), this.getZ()),
                pos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this)).getType() == HitResult.Type.MISS;
    }

    private void spawnTrailParticles() {
        for (int i = 0; i < 3; i++) {
            double offsetX = this.random.nextGaussian() * this.getBbWidth() * 0.25D;
            double offsetY = this.random.nextGaussian() * this.getBbHeight() * 0.25D;
            double offsetZ = this.random.nextGaussian() * this.getBbWidth() * 0.25D;
            this.level().addParticle(ParticleTypes.DRAGON_BREATH,
                    this.getX() + offsetX,
                    this.getY() + this.getBbHeight() * 0.5D + offsetY,
                    this.getZ() + offsetZ,
                    this.random.nextGaussian() * 0.01D,
                    0.02D,
                    this.random.nextGaussian() * 0.01D);
        }
    }

    @Nullable
    private Player getPlayerOwner() {
        LivingEntity owner = this.getOwner();
        return owner instanceof Player player ? player : null;
    }

    private boolean hasActiveRagoraOwner(@Nullable Player owner) {
        if (owner == null || !owner.isAlive()) {
            return false;
        }
        return owner.getCapability(RWBYMCapabilities.SEMBLANCE)
                .map(semblance -> "ragora".equals(semblance.getName()) && semblance.isActive())
                .orElse(false);
    }

    private void spawnRagoraFireball(Vec3 motion) {
        Item item = ForgeRegistries.ITEMS.getValue(RAGORA_FIREBALL_ID);
        if (item == null) {
            return;
        }
        ItemStack stack = new ItemStack(item);
        RWBYMProjectileEntity projectile = new RWBYMProjectileEntity(
                this.level(), this, stack, stack, 0.0F, "", false, false, false, true);
        projectile.setPos(this.getX(), this.getEyeY() - 0.15D, this.getZ());
        projectile.setNoGravity(true);
        projectile.setDeltaMovement(motion);
        this.level().addFreshEntity(projectile);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ENDER_DRAGON_SHOOT, this.getSoundSource(), 0.8F, 1.0F);
    }

    private enum AttackPhase {
        MELEE,
        RANGE
    }

    private final class RagoraFollowOwnerGoal extends Goal {
        private static final Vec3 FOLLOW_OFFSET = new Vec3(0.0D, 3.0D, -2.0D);

        @Override
        public boolean canUse() {
            return RagoraEntity.this.getTarget() == null && RagoraEntity.this.getPlayerOwner() != null;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            Player owner = RagoraEntity.this.getPlayerOwner();
            if (owner == null) {
                return;
            }

            if (RagoraEntity.this.tickCount < 100) {
                double rise = Mth.clamp(2.5D * RagoraEntity.this.tickCount / 50.0D, 0.0D, 2.5D);
                RagoraEntity.this.setFlightTarget(
                        new Vec3(owner.getX(), owner.getY() + owner.getBbHeight() + rise, owner.getZ()),
                        FOLLOW_SPEED);
                RagoraEntity.this.setYRot(owner.getYRot());

                float yaw = -RagoraEntity.this.getYRot();
                float pitch = Mth.clamp(60.0F - RagoraEntity.this.tickCount, 0.0F, 20.0F) / 20.0F * 90.0F;
                double yawRad = Math.toRadians(yaw);
                double pitchRad = Math.toRadians(pitch);
                Vec3 look = new Vec3(
                        Math.cos(pitchRad) * Math.sin(yawRad),
                        Math.sin(pitchRad),
                        Math.cos(pitchRad) * Math.cos(yawRad));
                RagoraEntity.this.faceTowards(RagoraEntity.this.position().add(look));
                return;
            }

            double yaw = Math.toRadians(-owner.getYRot());
            double offsetZ = FOLLOW_OFFSET.z * Math.cos(yaw) - FOLLOW_OFFSET.x * Math.sin(yaw);
            double offsetX = FOLLOW_OFFSET.z * Math.sin(yaw) + FOLLOW_OFFSET.x * Math.cos(yaw);
            Vec3 desiredPos = new Vec3(owner.getX() + offsetX, owner.getY() + FOLLOW_OFFSET.y, owner.getZ() + offsetZ);

            Vec3 chosenPos = desiredPos;
            if (!RagoraEntity.this.canSeePos(desiredPos)) {
                int visible = 0;
                for (int i = 0; i < TRACE_LENGTH; i++) {
                    Vec3 trace = RagoraEntity.this.ownerTrace[i];
                    if (trace != null && RagoraEntity.this.canSeePos(trace)) {
                        visible++;
                        if (visible > 5) {
                            chosenPos = trace;
                            break;
                        }
                    }
                    if (i > 0 && trace == null && RagoraEntity.this.ownerTrace[i - 1] != null) {
                        chosenPos = RagoraEntity.this.ownerTrace[i - 1];
                    }
                }
                if (visible > 0 && RagoraEntity.this.ownerTrace[TRACE_LENGTH - 1] != null) {
                    chosenPos = RagoraEntity.this.ownerTrace[TRACE_LENGTH - 1];
                }
            }

            RagoraEntity.this.setFlightTarget(chosenPos, FOLLOW_SPEED);
            RagoraEntity.this.faceTowards(owner.position().add(0.0D, owner.getBbHeight() * 0.5D, 0.0D));
        }
    }

    private final class RagoraMeleeGoal extends Goal {
        private int timer;
        private int attackCooldown;

        @Override
        public boolean canUse() {
            return RagoraEntity.this.getTarget() != null && RagoraEntity.this.attackPhase == AttackPhase.MELEE;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() && RagoraEntity.this.getTarget() != null && RagoraEntity.this.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = RagoraEntity.this.getTarget();
            if (target == null) {
                return;
            }

            if (RagoraEntity.this.hasLineOfSight(target)) {
                Player owner = RagoraEntity.this.getPlayerOwner();
                double angleDegrees = owner == null
                        ? RagoraEntity.this.random.nextDouble() * 360.0D
                        : Math.toDegrees(Math.atan2(target.getX() - owner.getX(), target.getZ() - owner.getZ()));
                double radius = MELEE_ORBIT_DISTANCE + target.getBbWidth();
                double z = target.getZ() + Math.cos(Math.toRadians(angleDegrees)) * radius;
                double x = target.getX() + Math.sin(Math.toRadians(angleDegrees)) * radius;
                double y = target.getY() + (target.getBbHeight() - RagoraEntity.this.getBbHeight()) * 0.5D;

                RagoraEntity.this.setFlightTarget(new Vec3(x, y, z), FOLLOW_SPEED);
                if (this.attackCooldown > 0) {
                    this.attackCooldown--;
                }
                if (RagoraEntity.this.distanceToSqr(target) <= attackReachSqr(target) && this.attackCooldown <= 0) {
                    this.attackCooldown = 20;
                    RagoraEntity.this.doHurtTarget(target);
                }
            } else {
                RagoraEntity.this.setFlightTarget(target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D),
                        FOLLOW_SPEED);
            }

            RagoraEntity.this.faceTowards(target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D));
            if (this.timer < 100) {
                this.timer++;
            } else {
                this.timer = RagoraEntity.this.random.nextInt(100);
                RagoraEntity.this.attackPhase = AttackPhase.RANGE;
            }
        }

        private double attackReachSqr(LivingEntity target) {
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // Ragora's melee goal used EntityAIAttackMeleeWithRange(..., 1), wider than standard melee.
            double reach = RagoraEntity.this.getBbWidth() + target.getBbWidth() + 1.0D;
            return reach * reach;
        }
    }

    private final class RagoraRangeGoal extends Goal {
        private float angle;
        private int direction;
        private int time;

        @Override
        public boolean canUse() {
            return RagoraEntity.this.getTarget() != null && RagoraEntity.this.attackPhase == AttackPhase.RANGE;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() && RagoraEntity.this.getTarget() != null && RagoraEntity.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            this.angle = RagoraEntity.this.random.nextFloat() * 360.0F;
            this.direction = RagoraEntity.this.random.nextBoolean() ? 1 : -1;
            this.time = RagoraEntity.this.random.nextInt(20);
        }

        @Override
        public void tick() {
            LivingEntity target = RagoraEntity.this.getTarget();
            if (target == null) {
                return;
            }

            double x = Math.sin(Math.toRadians(this.angle)) * RANGE_ORBIT_DISTANCE + target.getX();
            double z = Math.cos(Math.toRadians(this.angle)) * RANGE_ORBIT_DISTANCE + target.getZ();
            double y = RANGE_ORBIT_HEIGHT + target.getY();

            Vec3 look = target.position()
                    .add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                    .subtract(RagoraEntity.this.position().add(0.0D, RagoraEntity.this.getBbHeight() * 0.5D, 0.0D))
                    .normalize()
                    .scale(2.0D)
                    .add(target.getDeltaMovement());

            this.angle += this.direction;
            RagoraEntity.this.setFlightTarget(new Vec3(x, y, z), FOLLOW_SPEED);
            RagoraEntity.this.faceTowards(RagoraEntity.this.position().add(look));

            this.time++;
            if (this.time > 100) {
                RagoraEntity.this.spawnRagoraFireball(look.lengthSqr() < 1.0E-6D
                        ? RagoraEntity.this.getLookAngle().scale(2.0D)
                        : look);
                RagoraEntity.this.attackPhase = AttackPhase.MELEE;
            }
        }
    }
}
