package io.github.blaezdev.rwbym.entity;

import io.github.blaezdev.rwbym.registry.RWBYMItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.Difficulty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class BasicGrimmEntity extends Zombie {
    private String cachedGrimmKind;
    private Vec3 flightTarget = Vec3.ZERO;
    private Vec3 circleCenter = Vec3.ZERO;
    private int flightPhaseTicks;
    private float orbitAngle;
    private float orbitRadius;
    private int summonCooldown;
    private boolean swooping;
    private boolean rangedFlyingAttack;
    private int rangedFlyingTicks;
    private int rangedFlyingShots;
    private int flyingTargetScanCooldown = 10;
    private int spellCastingTickCount;
    private int summonSpellWarmup;
    private int nextSummonCastTime;
    private String pendingSummonKind;
    private Vec3 geistMoveTarget = Vec3.ZERO;
    private int geistRandomMoveCooldown;
    private int geistChargeCooldown;
    private int geistChargeTicks;
    private boolean geistCharging;
    private LivingEntity summonOwner;
    private boolean restoredDefaultEquipment;
    private int mutantFireballTimer;
    private int armorgeistInvulnerableTicks;
    private final ServerBossEvent originalDeathstalkerBossEvent =
            new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    public BasicGrimmEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
        this.originalDeathstalkerBossEvent.setDarkenScreen(true);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean isSunSensitive() {
        return false;
    }

    @Override
    public MobType getMobType() {
        // Original RWBYM uses a custom Grimm creature attribute; do not inherit Zombie's undead rules.
        return MobType.UNDEFINED;
    }

    @Override
    public boolean fireImmune() {
        // Geist was explicitly fire immune in the Blaez_Dev source.
        return "geist".equals(this.grimmKind()) || super.fireImmune();
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        String kind = this.grimmKind();
        if ("giantnevermore".equals(kind) || "armorgeist".equals(kind) || "nuckleeve".equals(kind)
                || "mutantdeathstalker".equals(kind) || "deathstalker".equals(kind) || "goliath".equals(kind)) {
            return false;
        }
        return super.removeWhenFarAway(distanceToClosestPlayer);
    }

    public void igniteArmorgeist() {
        if ("armorgeist".equals(this.grimmKind())) {
            this.armorgeistInvulnerableTicks = 220;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if ("armorgeist".equals(this.grimmKind()) && this.armorgeistInvulnerableTicks > 0
                && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if ("armorgeist".equals(this.grimmKind())) {
            tag.putInt("Invul", this.armorgeistInvulnerableTicks);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if ("armorgeist".equals(this.grimmKind())) {
            this.armorgeistInvulnerableTicks = tag.getInt("Invul");
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return switch (this.grimmKind()) {
            case "armorgeist" -> SoundEvents.IRON_GOLEM_STEP;
            case "geist" -> SoundEvents.VEX_AMBIENT;
            default -> SoundEvents.WITHER_SKELETON_AMBIENT;
        };
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return switch (this.grimmKind()) {
            case "ursa", "ursamajor" -> SoundEvents.POLAR_BEAR_HURT;
            case "boarbatusk" -> SoundEvents.ZOMBIFIED_PIGLIN_HURT;
            case "sabyr" -> SoundEvents.CAT_HURT;
            case "beringle" -> SoundEvents.ELDER_GUARDIAN_HURT;
            case "arachne" -> SoundEvents.SPIDER_HURT;
            case "armorgeist" -> SoundEvents.IRON_GOLEM_HURT;
            case "geist" -> SoundEvents.VEX_HURT;
            case "wyvern" -> SoundEvents.ENDER_DRAGON_GROWL;
            default -> SoundEvents.SQUID_DEATH;
        };
    }

    @Override
    protected SoundEvent getDeathSound() {
        return switch (this.grimmKind()) {
            case "beowolf" -> SoundEvents.WOLF_DEATH;
            case "ursa", "ursamajor" -> SoundEvents.POLAR_BEAR_DEATH;
            case "boarbatusk" -> SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
            case "sabyr" -> SoundEvents.CAT_DEATH;
            case "arachne" -> SoundEvents.SPIDER_DEATH;
            case "armorgeist" -> SoundEvents.IRON_GOLEM_DEATH;
            case "geist" -> SoundEvents.VEX_DEATH;
            default -> SoundEvents.ELDER_GUARDIAN_DEATH;
        };
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    protected void registerGoals() {
        String kind = this.grimmKind();
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Register Grimm goals explicitly so the shared Zombie base does not leak vanilla undead AI.
        this.goalSelector.addGoal(0, new FloatGoal(this));
        if (!isFlyingGrimm(kind) && !"geist".equals(kind)) {
            // Flying Grimm and Geist use original-style custom movement attacks instead of ground pathing AI.
            this.goalSelector.addGoal(2, new RangedMeleeAttackGoal(this, 1.0D, false, 0.5F));
        }
        if (!"armorgeist".equals(kind)) {
            this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ZweiEntity.class, 6.0F, 1.0D, 1.2D));
        }
        if ("arachne".equals(kind)) {
            this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, LivingEntity.class, 6.0F, 1.0D, 1.2D));
        }
        if ("beringle".equals(kind)) {
            this.goalSelector.addGoal(3, new LeapAtTargetGoal(this, 0.2F));
        }
        if (!isFlyingGrimm(kind) && !"geist".equals(kind)) {
            this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6D));
        }
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));

        if ("arachne".equals(kind)) {
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        } else if ("creep".equals(kind) || "arachneclone".equals(kind)) {
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Vindicator.class).setAlertOthers());
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        } else if ("goliath".equals(kind)) {
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        } else if (!isFlyingGrimm(kind)) {
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Vindicator.class).setAlertOthers());
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
            this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        String kind = this.grimmKind();
        if (isFlyingGrimm(kind) || "geist".equals(kind)) {
            if (!this.isNoGravity()) {
                this.setNoGravity(true);
            }
            this.fallDistance = 0.0F;
        }
        if (this.level().isClientSide()) {
            return;
        }
        updateOriginalDeathstalkerBossBar(kind);
        if (!this.restoredDefaultEquipment) {
            this.restoredDefaultEquipment = true;
            restoreOriginalDefaultEquipment(kind);
        }
        copySummonOwnerTarget(kind);
        if (tickArmorgeistInvulnerability(kind)) {
            return;
        }
        if ("apathy".equals(kind)) {
            tickApathyAura();
        } else if ("arachne".equals(kind)) {
            tickActiveSummonSpell();
            tickSummoner(kind);
        } else if ("geist".equals(kind)) {
            tickGeistBehavior();
        } else if ("hollow".equals(kind)) {
            tickHollowAura();
        } else if ("mutantdeathstalker".equals(kind)) {
            tickActiveSummonSpell();
            tickSummoner(kind);
            tickMutantDeathStalkerFireball();
        } else if (isFlyingGrimm(kind)) {
            tickFlyingTargetScan(kind);
            if ("giantnevermore".equals(kind) || "wyvern".equals(kind)) {
                this.heal(0.2F);
            }
            tickActiveSummonSpell();
            updateFlyingMovement();
            if (canSummonMinions(kind)) {
                tickSummoner(kind);
            }
        }
    }

    private boolean tickArmorgeistInvulnerability(String kind) {
        if (!"armorgeist".equals(kind) || this.armorgeistInvulnerableTicks <= 0) {
            return false;
        }
        --this.armorgeistInvulnerableTicks;
        this.setTarget(null);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        // Original AIDoNothing locks Arma Gigas while the summon invulnerability timer burns down.
        return true;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (hasOriginalDeathstalkerBossBar(this.grimmKind())) {
            this.originalDeathstalkerBossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        if (hasOriginalDeathstalkerBossBar(this.grimmKind())) {
            this.originalDeathstalkerBossEvent.removePlayer(player);
        }
    }

    private void updateOriginalDeathstalkerBossBar(String kind) {
        if (!hasOriginalDeathstalkerBossBar(kind)) {
            return;
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Original Death Stalker variants declared a purple darkening BossInfoServer; modern mobs need explicit sync.
        this.originalDeathstalkerBossEvent.setName(this.getDisplayName());
        this.originalDeathstalkerBossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    private boolean hasOriginalDeathstalkerBossBar(String kind) {
        return "deathstalker".equals(kind) || "tinyeathstalker".equals(kind);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        String kind = this.grimmKind();
        if ("creep".equals(kind)) {
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // Original Creep attackEntityAsMob only detonated; it did not also apply a normal melee hit.
            this.level().broadcastEntityEvent(this, (byte) 4);
            if (!this.level().isClientSide()) {
                this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, Level.ExplosionInteraction.MOB);
                this.discard();
            }
            return true;
        }
        if ("arachneclone".equals(kind)) {
            if (target instanceof LivingEntity living) {
                living.hurt(this.damageSources().mobAttack(this), 10.0F);
            }
            if (!this.level().isClientSide()) {
                this.discard();
            }
            return true;
        }
        boolean hurt = super.doHurtTarget(target);
        if (!hurt || this.level().isClientSide() || !(target instanceof LivingEntity living)) {
            return hurt;
        }
        if ("deathstalker".equals(kind) || "mutantdeathstalker".equals(kind)
                || "tinyeathstalker".equals(kind)) {
            int seconds = this.level().getDifficulty() == Difficulty.HARD ? 15
                    : this.level().getDifficulty() == Difficulty.NORMAL ? 7 : 0;
            if (seconds > 0) {
                living.addEffect(new MobEffectInstance(MobEffects.WITHER, seconds * 20, 1));
            }
        } else if ("winter_boarbatusk".equals(kind)) {
            living.knockback(1.2D, this.getX() - living.getX(), this.getZ() - living.getZ());
        } else if ("beringle".equals(kind) || "ursa".equals(kind) || "winter_ursa".equals(kind) || "ursamajor".equals(kind)
                || "goliath".equals(kind) || "armorgeist".equals(kind) || "winterarmorgeist".equals(kind)) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        } else if ("geist".equals(kind) || "hollow".equals(kind)) {
            living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
        } else if ("wyvern".equals(kind) || "giantnevermore".equals(kind) || "queenlancer".equals(kind)
                || "ravager".equals(kind)) {
            living.knockback(2.0D, this.getX() - living.getX(), this.getZ() - living.getZ());
        }
        return true;
    }

    private void restoreOriginalDefaultEquipment(String kind) {
        if ("armorgeist".equals(kind) && this.getMainHandItem().isEmpty()
                && RWBYMItems.SIMPLE_ITEMS.containsKey("armasword")) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(RWBYMItems.SIMPLE_ITEMS.get("armasword").get()));
        }
    }

    public void setSummonOwner(LivingEntity owner) {
        this.summonOwner = owner;
    }

    private void copySummonOwnerTarget(String kind) {
        if (!"creep".equals(kind) || this.summonOwner == null || !this.summonOwner.isAlive()) {
            return;
        }
        LivingEntity ownerTarget = this.summonOwner.getLastHurtMob();
        if (ownerTarget == null && this.summonOwner instanceof net.minecraft.world.entity.Mob ownerMob) {
            ownerTarget = ownerMob.getTarget();
        }
        if (ownerTarget != null && ownerTarget.isAlive() && ownerTarget != this.getTarget()
                && !this.isAlliedTo(ownerTarget)) {
            // Mutant Death Stalker summons used AICopyOwnerTarget in the original entity class.
            this.setTarget(ownerTarget);
        }
    }

    private void tickApathyAura() {
        for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0D))) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0, true, true));
        }
    }

    private void tickHollowAura() {
        for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5.0D))) {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 120, 1, true, true));
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, true, true));
        }
    }

    private void tickGeistBehavior() {
        tickFlyingTargetScan("geist");
        LivingEntity target = this.getTarget();
        if (this.geistCharging) {
            tickGeistCharge(target);
            return;
        }
        if (target != null && target.isAlive() && !this.getBoundingBox().inflate(2.0D).intersects(target.getBoundingBox())
                && --this.geistChargeCooldown <= 0 && this.random.nextInt(7) == 0) {
            this.geistMoveTarget = target.getEyePosition(1.0F);
            this.geistCharging = true;
            this.geistChargeTicks = 0;
            this.geistChargeCooldown = 20;
            this.playSound(SoundEvents.VEX_CHARGE, 1.0F, 1.0F);
            return;
        }
        if (--this.geistRandomMoveCooldown <= 0 || this.geistMoveTarget == Vec3.ZERO
                || this.position().distanceToSqr(this.geistMoveTarget) < 2.0D) {
            pickGeistRandomMoveTarget();
            this.geistRandomMoveCooldown = 7 + this.random.nextInt(14);
        }
        moveTowardGeistTarget(0.25D);
        faceGeistMovement(target);
    }

    private void tickGeistCharge(LivingEntity target) {
        if (target == null || !target.isAlive() || ++this.geistChargeTicks > 80) {
            stopGeistCharge();
            return;
        }
        if (this.getBoundingBox().intersects(target.getBoundingBox())) {
            this.doHurtTarget(target);
            stopGeistCharge();
            return;
        }
        if (this.distanceToSqr(target) < 9.0D) {
            this.geistMoveTarget = target.getEyePosition(1.0F);
        }
        moveTowardGeistTarget(1.0D);
        faceGeistMovement(target);
    }

    private void stopGeistCharge() {
        this.geistCharging = false;
        this.geistChargeTicks = 0;
        this.geistChargeCooldown = 10 + this.random.nextInt(20);
    }

    private void pickGeistRandomMoveTarget() {
        BlockPos origin = this.blockPosition();
        for (int i = 0; i < 3; i++) {
            BlockPos candidate = origin.offset(this.random.nextInt(15) - 7, this.random.nextInt(11) - 5,
                    this.random.nextInt(15) - 7);
            if (this.level().isEmptyBlock(candidate)) {
                this.geistMoveTarget = Vec3.atCenterOf(candidate);
                return;
            }
        }
        this.geistMoveTarget = this.position().add((this.random.nextDouble() - 0.5D) * 8.0D,
                (this.random.nextDouble() - 0.5D) * 4.0D, (this.random.nextDouble() - 0.5D) * 8.0D);
    }

    private void moveTowardGeistTarget(double speed) {
        Vec3 toTarget = this.geistMoveTarget.subtract(this.position());
        double distance = toTarget.length();
        if (distance < this.getBoundingBox().getSize()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5D));
            return;
        }
        this.setDeltaMovement(this.getDeltaMovement().add(toTarget.scale(0.05D * speed / distance)));
        this.hasImpulse = true;
    }

    private void faceGeistMovement(LivingEntity target) {
        double dx;
        double dz;
        if (target == null) {
            Vec3 movement = this.getDeltaMovement();
            dx = movement.x;
            dz = movement.z;
        } else {
            dx = target.getX() - this.getX();
            dz = target.getZ() - this.getZ();
        }
        if (dx * dx + dz * dz > 0.0001D) {
            this.setYRot((float) (-(Math.atan2(dx, dz) * 180.0D / Math.PI)));
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }
    }

    private void tickMutantDeathStalkerFireball() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.mutantFireballTimer = 0;
            return;
        }
        if (target.distanceTo(this) < 4096.0D && this.hasLineOfSight(target)) {
            ++this.mutantFireballTimer;
            if (this.mutantFireballTimer == 20) {
                spawnMutantDeathStalkerFireball(target);
                this.mutantFireballTimer = -40;
            }
        } else if (this.mutantFireballTimer > 0) {
            --this.mutantFireballTimer;
        }
    }

    private void spawnMutantDeathStalkerFireball(LivingEntity target) {
        Vec3 look = this.getLookAngle();
        double xPower = target.getX() - (this.getX() + look.x * 4.0D);
        double yPower = target.getBoundingBox().minY + target.getBbHeight() / 2.0D
                - (0.5D + this.getY() + this.getBbHeight() / 2.0D);
        double zPower = target.getZ() - (this.getZ() + look.z * 4.0D);
        this.level().levelEvent(null, 1016, this.blockPosition(), 0);
        LargeFireball fireball = new LargeFireball(this.level(), this, xPower, yPower, zPower, 0);
        fireball.setPos(this.getX() + look.x * 4.0D, this.getY() + this.getBbHeight() / 2.0D + 0.5D,
                this.getZ() + look.z * 4.0D);
        this.level().addFreshEntity(fireball);
    }

    private void updateFlyingMovement() {
        LivingEntity target = this.getTarget();
        if (target == null) {
            this.rangedFlyingAttack = false;
            this.rangedFlyingTicks = 0;
            updateIdleOrbit();
            return;
        }
        if (this.circleCenter == Vec3.ZERO || this.tickCount % 80 == 0) {
            resetCircleCenter(target);
        }
        if ("giantnevermore".equals(this.grimmKind()) && this.rangedFlyingAttack) {
            updateGiantNevermoreRangedAttack(target);
        } else if (this.swooping) {
            this.flightTarget = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            moveTowardFlightTarget(flightSpeed() * 1.55D, 0.35D);
            if (this.getBoundingBox().inflate(0.25D).intersects(target.getBoundingBox())) {
                this.doHurtTarget(target);
                this.swooping = false;
                this.flightPhaseTicks = nextFlyingCircleDelay(this.grimmKind());
                resetCircleCenter(target);
            } else if (this.horizontalCollision || this.verticalCollision || --this.flightPhaseTicks <= 0) {
                this.swooping = false;
                this.flightPhaseTicks = nextFlyingCircleDelay(this.grimmKind());
                resetCircleCenter(target);
            }
        } else {
            orbitAngle += orbitDirection() * 0.18F;
            double yWave = Math.sin((this.tickCount + this.getId()) * 0.07D) * 4.0D;
            this.flightTarget = this.circleCenter.add(Math.cos(orbitAngle) * orbitRadius, yWave,
                    Math.sin(orbitAngle) * orbitRadius);
            moveTowardFlightTarget(flightSpeed(), 0.22D);
            if (--this.flightPhaseTicks <= 0 && this.hasLineOfSight(target)) {
                if ("giantnevermore".equals(this.grimmKind()) && this.random.nextBoolean()) {
                    this.rangedFlyingAttack = true;
                    this.rangedFlyingTicks = 0;
                    this.rangedFlyingShots = 24 + this.random.nextInt(2);
                } else {
                    this.swooping = true;
                    this.flightPhaseTicks = 35 + this.random.nextInt(25);
                }
            }
        }
        faceMovement();
    }

    private void updateIdleOrbit() {
        if (this.flightTarget == Vec3.ZERO || this.tickCount % 120 == 0 || this.position().distanceToSqr(this.flightTarget) < 4.0D) {
            orbitAngle += 0.9F + this.random.nextFloat();
            orbitRadius = baseOrbitRadius();
            this.flightTarget = this.position().add(Math.cos(orbitAngle) * orbitRadius,
                    this.random.nextDouble() * 5.0D - 1.0D, Math.sin(orbitAngle) * orbitRadius);
        }
        moveTowardFlightTarget(flightSpeed() * 0.65D, 0.16D);
        faceMovement();
    }

    private void resetCircleCenter(LivingEntity target) {
        double y = Math.max(target.getY() + 20.0D + this.random.nextInt(20), this.level().getSeaLevel() + 1.0D);
        this.circleCenter = new Vec3(target.getX(), y, target.getZ());
        this.orbitRadius = baseOrbitRadius() + this.random.nextFloat() * orbitRadiusVariance();
        this.orbitAngle = this.random.nextFloat() * ((float) Math.PI * 2.0F);
        if (this.flightPhaseTicks <= 0) {
            // Original AIPickAttack starts with a short 10-tick circle before the first swoop.
            this.flightPhaseTicks = "giantnevermore".equals(this.grimmKind()) ? 4 : 10;
        }
    }

    private void moveTowardFlightTarget(double speed, double steering) {
        Vec3 toTarget = this.flightTarget.subtract(this.position());
        if (toTarget.lengthSqr() <= 0.01D) {
            return;
        }
        Vec3 desired = toTarget.normalize().scale(speed);
        this.setDeltaMovement(this.getDeltaMovement().scale(1.0D - steering).add(desired.scale(steering)));
        this.hasImpulse = true;
    }

    private void faceMovement() {
        Vec3 movement = this.getDeltaMovement();
        if (movement.horizontalDistanceSqr() > 0.0001D) {
            this.setYRot((float) (Math.atan2(movement.z, movement.x) * 180.0D / Math.PI) - 90.0F);
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.getYRot();
        }
    }

    private boolean isFlyingGrimm(String kind) {
        return "lancer".equals(kind)
                || "queenlancer".equals(kind)
                || "nevermore".equals(kind)
                || "giantnevermore".equals(kind)
                || "wyvern".equals(kind)
                || "ravager".equals(kind);
    }

    private double flightSpeed() {
        String kind = this.grimmKind();
        if ("giantnevermore".equals(kind) || "wyvern".equals(kind)) {
            return 0.34D;
        }
        if ("queenlancer".equals(kind) || "ravager".equals(kind)) {
            return 0.48D;
        }
        return 0.42D;
    }

    private float baseOrbitRadius() {
        String kind = this.grimmKind();
        if ("giantnevermore".equals(kind)) {
            return 40.0F;
        }
        return 5.0F;
    }

    private float orbitRadiusVariance() {
        return "giantnevermore".equals(this.grimmKind()) ? 20.0F : 10.0F;
    }

    private float orbitDirection() {
        return (this.getId() & 1) == 0 ? 1.0F : -1.0F;
    }

    private int nextFlyingCircleDelay(String kind) {
        int originalDelay = (8 + this.random.nextInt(4)) * 20;
        // Giant Nevermore's original AIPickAttack decremented this timer three times per tick.
        return "giantnevermore".equals(kind) ? Math.max(1, originalDelay / 3) : originalDelay;
    }

    private boolean canSummonMinions(String kind) {
        return "wyvern".equals(kind) || "queenlancer".equals(kind) || "arachne".equals(kind)
                || "mutantdeathstalker".equals(kind);
    }

    private void tickFlyingTargetScan(String kind) {
        LivingEntity currentTarget = this.getTarget();
        if (currentTarget != null && currentTarget.isAlive()) {
            return;
        }
        if (--this.flyingTargetScanCooldown > 0) {
            return;
        }
        this.flyingTargetScanCooldown = 20;
        double horizontalRange = "giantnevermore".equals(kind) ? 128.0D : 16.0D;
        double verticalRange = "giantnevermore".equals(kind) ? 128.0D : 64.0D;
        this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(horizontalRange, verticalRange, horizontalRange),
                        player -> player.isAlive() && !player.isSpectator() && !player.isCreative()
                                && !this.isAlliedTo(player) && this.hasLineOfSight(player))
                .stream()
                .sorted((first, second) -> Double.compare(second.getY(), first.getY()))
                .findFirst()
                .ifPresent(this::setTarget);
    }

    private void updateGiantNevermoreRangedAttack(LivingEntity target) {
        this.flightTarget = new Vec3(target.getX(), this.getY(), target.getZ());
        moveTowardFlightTarget(flightSpeed() * 0.8D, 0.24D);
        if (this.position().distanceToSqr(this.flightTarget) < 1.0D) {
            stopRangedFlyingAttack(target);
            return;
        }
        if (this.hurtTime > 0) {
            this.rangedFlyingAttack = false;
            this.swooping = true;
            this.flightPhaseTicks = 35 + this.random.nextInt(25);
            return;
        }
        if (this.horizontalCollision || this.verticalCollision) {
            stopRangedFlyingAttack(target);
            return;
        }
        this.rangedFlyingTicks++;
        if (this.rangedFlyingTicks > 60 && this.rangedFlyingTicks % 4 == 0 && this.hasLineOfSight(target)) {
            shootNevermoreFeather(target);
            if (--this.rangedFlyingShots <= 0) {
                stopRangedFlyingAttack(target);
            }
        }
    }

    private void stopRangedFlyingAttack(LivingEntity target) {
        this.rangedFlyingAttack = false;
        this.rangedFlyingTicks = 0;
        this.swooping = false;
        this.flightPhaseTicks = nextFlyingCircleDelay(this.grimmKind());
        resetCircleCenter(target);
    }

    private void shootNevermoreFeather(LivingEntity target) {
        if (!this.level().isClientSide() && RWBYMItems.SIMPLE_ITEMS.containsKey("nevermorefeather")) {
            ItemStack feather = new ItemStack(RWBYMItems.SIMPLE_ITEMS.get("nevermorefeather").get());
            RWBYMProjectileEntity projectile = new RWBYMProjectileEntity(
                    this.level(), this, feather, feather, 20.0F, "", false, false, false, true);
            projectile.setPos(this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ());
            projectile.shoot(target.getX() - this.getX(), target.getY() - this.getY(),
                    target.getZ() - this.getZ(), 5.0F, 2.0F);
            this.level().addFreshEntity(projectile);
        }
    }

    private void tickSummoner(String kind) {
        if (this.getTarget() == null || this.isCastingSummonSpell() || this.tickCount < this.nextSummonCastTime) {
            return;
        }
        int nearbyCreeps = this.level().getEntitiesOfClass(BasicGrimmEntity.class, this.getBoundingBox().inflate(16.0D),
                grimm -> grimm != this && "creep".equals(grimm.grimmKind())).size();
        if (this.random.nextInt(8) + 1 > nearbyCreeps) {
            this.summonSpellWarmup = 20;
            this.spellCastingTickCount = 100;
            this.nextSummonCastTime = this.tickCount + 340;
            this.pendingSummonKind = kind;
            this.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.0F, 1.0F);
        }
    }

    private void tickActiveSummonSpell() {
        if (this.spellCastingTickCount > 0) {
            --this.spellCastingTickCount;
        }
        if (this.summonSpellWarmup <= 0) {
            return;
        }
        if (this.getTarget() == null || this.pendingSummonKind == null) {
            this.summonSpellWarmup = 0;
            this.pendingSummonKind = null;
            return;
        }
        --this.summonSpellWarmup;
        if (this.summonSpellWarmup == 0) {
            castSummonSpell(this.pendingSummonKind);
            this.pendingSummonKind = null;
            this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, 1.0F);
        }
    }

    private boolean isCastingSummonSpell() {
        return this.spellCastingTickCount > 0 || this.summonSpellWarmup > 0;
    }

    private void castSummonSpell(String summonerKind) {
        int count = switch (summonerKind) {
            case "arachne" -> 2;
            case "mutantdeathstalker" -> 3;
            default -> 6;
        };
        for (int i = 0; i < count; i++) {
            spawnMinion(summonerKind);
        }
    }

    private void spawnMinion(String summonerKind) {
        EntityType<BasicGrimmEntity> type = switch (summonerKind) {
            case "wyvern" -> io.github.blaezdev.rwbym.registry.RWBYMEntityTypes.BEOWOLF.get();
            case "arachne" -> io.github.blaezdev.rwbym.registry.RWBYMEntityTypes.ARACHNE_CLONE.get();
            case "mutantdeathstalker" -> io.github.blaezdev.rwbym.registry.RWBYMEntityTypes.CREEP.get();
            default -> io.github.blaezdev.rwbym.registry.RWBYMEntityTypes.LANCER.get();
        };
        BasicGrimmEntity minion = type.create(this.level());
        if (minion == null) {
            return;
        }
        BlockPos spawnPos = this.blockPosition().offset(-2 + this.random.nextInt(5), 1, -2 + this.random.nextInt(5));
        minion.moveTo(spawnPos, this.random.nextFloat() * 360.0F, 0.0F);
        if (this.level() instanceof ServerLevelAccessor accessor) {
            minion.finalizeSpawn(accessor, this.level().getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED,
                    (SpawnGroupData) null, null);
        }
        if ("mutantdeathstalker".equals(summonerKind)) {
            minion.setSummonOwner(this);
        }
        minion.setTarget(this.getTarget());
        this.level().addFreshEntity(minion);
    }

    private void tickArachneSummon() {
        LivingEntity target = this.getTarget();
        if (target == null || --this.summonCooldown > 0) {
            return;
        }
        int clones = this.level().getEntitiesOfClass(BasicGrimmEntity.class, this.getBoundingBox().inflate(12.0D),
                grimm -> grimm != this && "arachneclone".equals(grimm.grimmKind())).size();
        if (clones < 3) {
            for (int i = clones; i < 3; i++) {
                BasicGrimmEntity clone = io.github.blaezdev.rwbym.registry.RWBYMEntityTypes.ARACHNE_CLONE.get()
                        .create(this.level());
                if (clone == null) {
                    continue;
                }
                double x = this.getX() + (this.random.nextDouble() - 0.5D) * 5.0D;
                double z = this.getZ() + (this.random.nextDouble() - 0.5D) * 5.0D;
                clone.moveTo(x, this.getY(), z, this.random.nextFloat() * 360.0F, 0.0F);
                clone.setTarget(target);
                this.level().addFreshEntity(clone);
            }
        }
        this.summonCooldown = 220 + this.random.nextInt(160);
    }

    private void pulseNearbyDebuff(double radius, net.minecraft.world.effect.MobEffect effect, int duration,
            int amplifier) {
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(radius), entity -> entity != this && !this.isAlliedTo(entity))) {
            entity.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    private String grimmKind() {
        if (this.cachedGrimmKind == null) {
            this.cachedGrimmKind = EntityType.getKey(this.getType()).getPath();
        }
        return this.cachedGrimmKind;
    }
}
