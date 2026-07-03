package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.client.model.RWBYMPlayerArmorModel;
import io.github.blaezdev.rwbym.registry.RWBYMMobEffects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class RWBYMArmorItem extends ArmorItem {
    public static final long MOVEMENTSPEED1 = 0x0001L;
    public static final long MOVEMENTSPEED2 = 0x0002L;
    public static final long DEFENSE1 = 0x0004L;
    public static final long DEFENSE2 = 0x0010L;
    public static final long VITALITY1 = 0x0020L;
    public static final long VITALITY2 = 0x0040L;
    public static final long VITALITY3 = 0x0080L;
    public static final long ATTACKBOOST1 = 0x0100L;
    public static final long ATTACKBOOST2 = 0x0200L;
    public static final long ATTACKBOOST3 = 0x0400L;
    public static final long ATTACKBOOST4 = 0x0800L;
    public static final long NIGHTVISION = 0x1000L;
    public static final long JUMPBOOST1 = 0x2000L;
    public static final long JUMPBOOST2 = 0x4000L;
    public static final long JUMPBOOST3 = 0x8000L;
    public static final long CRITICALSTRIKE1 = 0x10000L;
    public static final long CRITICALSTRIKE2 = 0x20000L;
    public static final long REACH1 = 0x40000L;
    public static final long REACH2 = 0x80000L;
    public static final long PUNCTURE1 = 0x100000L;
    public static final long PUNCTURE2 = 0x200000L;
    public static final long K01 = 0x400000L;
    public static final long K02 = 0x800000L;
    public static final long FOOTING1 = 0x1000000L;
    public static final long FOOTING2 = 0x2000000L;
    public static final long RUSH1 = 0x4000000L;
    public static final long RUSH2 = 0x8000000L;
    public static final long AURAREGEN = 0x10000000L;
    public static final long GLADIATOR1 = 0x20000000L;
    public static final long GLADIATOR2 = 0x40000000L;
    public static final long SILVERLIGHT = 0x100000000L;
    public static final long KINGSGAMBIT = 0x200000000L;
    public static final long KINGSPAWN = 0x400000000L;
    public static final long FIRESTARTER = 0x800000000L;
    public static final long MAIDEN = 0x800000000L;
    public static final long JAVELIN1 = 0x1000000000L;
    public static final long JAVELIN2 = 0x2000000000L;
    public static final long HandofBullets = 0x4000000000L;
    public static final long Predator = 0x8000000000L;
    public static final long AntiMagic = 0x10000000000L;

    private static final Map<String, Long> ARMOR_PERKS = createPerkMap();
    private static final UUID[] SPEED_UUIDS = slotUuids("b97e2987-d88c-4979-8459-c27226e51793");
    private static final UUID[] ARMOR_UUIDS = slotUuids("c575a9b5-6a04-4c50-9d9c-4407717bd8db");
    private static final UUID[] HEALTH_UUIDS = slotUuids("0305bc40-d3a3-4fa1-b644-185e12e2808e");
    private static final UUID[] ATTACK_UUIDS = slotUuids("0f5b7e9c-1dc7-4723-8b89-73ecc43f3fa5");
    private static final UUID[] KNOCKBACK_UUIDS = slotUuids("38fa9005-f5a5-4e0e-b87c-f34df448f3ca");
    private static final UUID[] ATTACK_SPEED_UUIDS = slotUuids("ebe83521-9738-4fb1-9a97-d31a07017ffd");
    private static final HumanoidModel<?>[] CLIENT_ARMOR_MODELS = new HumanoidModel<?>[EquipmentSlot.values().length * 4];

    private final String textureBase;
    private final long perks;
    private final String morphTarget;
    private final String formWeapon;
    private final boolean playerSkinTexture;
    private final String defaultTexture;
    private final String slimTexture;
    private final String layeredTexture;
    private final String layeredLegTexture;
    private final ResourceLocation morphTargetId;
    private final ResourceLocation formWeaponId;

    public RWBYMArmorItem(String itemName, ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
        this.textureBase = textureBaseFor(itemName);
        this.perks = ARMOR_PERKS.getOrDefault(itemName, 0L);
        this.morphTarget = morphTargetFor(itemName);
        this.formWeapon = formWeaponFor(itemName);
        this.playerSkinTexture = !itemName.startsWith("korekosmou");
        this.defaultTexture = RWBYM.MOD_ID + ":textures/models/armor/" + this.textureBase + "_default.png";
        this.slimTexture = RWBYM.MOD_ID + ":textures/models/armor/" + this.textureBase + "_slim.png";
        this.layeredTexture = RWBYM.MOD_ID + ":textures/models/armor/" + this.textureBase + "_layer_1.png";
        this.layeredLegTexture = RWBYM.MOD_ID + ":textures/models/armor/"
                + (this.textureBase.startsWith("korekosmou") ? "korekosmou" : this.textureBase) + "_layer_2.png";
        this.morphTargetId = this.morphTarget == null ? null : new ResourceLocation(this.morphTarget);
        this.formWeaponId = this.formWeapon == null ? null : new ResourceLocation(this.formWeapon);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player
                && player.getItemBySlot(this.getEquipmentSlot()) == stack) {
            if (player.tickCount % 40 == 0) {
                applyPassiveEffects(player);
            }
            if (this.formWeaponId != null && player.isShiftKeyDown() && player.swinging) {
                tryCreateKoreKosmouWeapon(stack, player);
            }
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot != this.getEquipmentSlot() || this.perks == 0L) {
            return super.getDefaultAttributeModifiers(slot);
        }

        return buildPerkAttributeModifiers(slot, this.perks, super.getDefaultAttributeModifiers(slot));
    }

    public static Multimap<Attribute, AttributeModifier> buildPerkAttributeModifiers(EquipmentSlot slot, long perks,
            Multimap<Attribute, AttributeModifier> baseModifiers) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(baseModifiers);
        int slotIndex = slot.getIndex();
        addModifier(builder, Attributes.MOVEMENT_SPEED, SPEED_UUIDS[slotIndex], "RWBYM armor movement",
                movementSpeed(perks), AttributeModifier.Operation.MULTIPLY_TOTAL);
        addModifier(builder, Attributes.ARMOR, ARMOR_UUIDS[slotIndex], "RWBYM armor defense",
                bonusArmor(perks), AttributeModifier.Operation.ADDITION);
        addModifier(builder, Attributes.MAX_HEALTH, HEALTH_UUIDS[slotIndex], "RWBYM armor vitality",
                bonusHealth(perks), AttributeModifier.Operation.ADDITION);
        addModifier(builder, Attributes.ATTACK_DAMAGE, ATTACK_UUIDS[slotIndex], "RWBYM armor attack",
                attackBoost(perks), AttributeModifier.Operation.MULTIPLY_TOTAL);
        addModifier(builder, Attributes.KNOCKBACK_RESISTANCE, KNOCKBACK_UUIDS[slotIndex], "RWBYM armor footing",
                footing(perks), AttributeModifier.Operation.ADDITION);
        addModifier(builder, Attributes.ATTACK_SPEED, ATTACK_SPEED_UUIDS[slotIndex], "RWBYM armor rush",
                rush(perks), AttributeModifier.Operation.MULTIPLY_TOTAL);
        return builder.build();
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        if (!this.playerSkinTexture) {
            return slot == EquipmentSlot.LEGS ? this.layeredLegTexture : this.layeredTexture;
        }
        return entity instanceof AbstractClientPlayer player && "slim".equals(player.getModelName())
                ? this.slimTexture
                : this.defaultTexture;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack,
                    EquipmentSlot slot, HumanoidModel<?> original) {
                if (!playerSkinTexture) {
                    return original;
                }
                boolean slim = entity instanceof AbstractClientPlayer player && "slim".equals(player.getModelName());
                boolean includeCompanionParts = shouldRenderCompanionParts(entity, slot);
                HumanoidModel<?> model = getOrCreateArmorModel(slot, slim, includeCompanionParts);
                copyPose(original, model);
                if (model instanceof RWBYMPlayerArmorModel<?> playerArmorModel) {
                    playerArmorModel.prepareForArmorRender();
                }
                return model;
            }
        });
    }

    private HumanoidModel<?> getOrCreateArmorModel(EquipmentSlot slot, boolean slim, boolean includeCompanionParts) {
        int index = armorModelIndex(slot, slim, includeCompanionParts);
        HumanoidModel<?> model = CLIENT_ARMOR_MODELS[index];
        if (model == null) {
            model = new RWBYMPlayerArmorModel<>(Minecraft.getInstance().getEntityModels()
                    .bakeLayer(slim ? RWBYMPlayerArmorModel.SLIM_LAYER : RWBYMPlayerArmorModel.DEFAULT_LAYER),
                    slim, slot, includeCompanionParts);
            CLIENT_ARMOR_MODELS[index] = model;
        }
        return model;
    }

    private static int armorModelIndex(EquipmentSlot slot, boolean slim, boolean includeCompanionParts) {
        return slot.ordinal() * 4 + (slim ? 1 : 0) + (includeCompanionParts ? 2 : 0);
    }

    private boolean shouldRenderCompanionParts(LivingEntity entity, EquipmentSlot slot) {
        if (slot == EquipmentSlot.HEAD) {
            return !hasMatchingArmor(entity, EquipmentSlot.CHEST) && !hasMatchingArmor(entity, EquipmentSlot.LEGS);
        }
        if (slot == EquipmentSlot.CHEST) {
            return !hasMatchingArmor(entity, EquipmentSlot.LEGS);
        }
        if (slot == EquipmentSlot.LEGS) {
            return !hasMatchingArmor(entity, EquipmentSlot.CHEST);
        }
        if (slot == EquipmentSlot.FEET) {
            return !hasMatchingArmor(entity, EquipmentSlot.LEGS);
        }
        return false;
    }

    private boolean hasMatchingArmor(LivingEntity entity, EquipmentSlot slot) {
        ItemStack equipped = entity.getItemBySlot(slot);
        return equipped.getItem() instanceof RWBYMArmorItem armorItem
                && armorItem.playerSkinTexture
                && armorItem.textureBase.equals(this.textureBase);
    }

    private static String textureBaseFor(String itemName) {
        if (itemName.equals("korekosmouoff")) {
            return "korekosmou";
        }
        if (itemName.equals("korekosmouice")) {
            return "korekosmouwater";
        }
        for (String suffix : new String[] { "_head", "_chest", "_legs", "_boots" }) {
            if (itemName.endsWith(suffix)) {
                return itemName.substring(0, itemName.length() - suffix.length());
            }
        }
        return itemName;
    }

    private static String morphTargetFor(String itemName) {
        return switch (itemName) {
            case "korekosmouoff" -> "rwbym:korekosmoufire";
            case "korekosmoufire" -> "rwbym:korekosmouice";
            case "korekosmouice" -> "rwbym:korekosmouwind";
            case "korekosmouwind" -> "rwbym:korekosmouoff";
            case "ruby2_head" -> "rwbym:rubyhood";
            case "summer2_head" -> "rwbym:summerhood";
            case "taylor_head" -> "rwbym:taylorhood";
            default -> null;
        };
    }

    private static String formWeaponFor(String itemName) {
        return switch (itemName) {
            case "korekosmoufire" -> "rwbym:kkfire";
            case "korekosmouice" -> "rwbym:kkice";
            case "korekosmouwind" -> "rwbym:kkwind";
            default -> null;
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && this.morphTarget != null) {
            if (!level.isClientSide()) {
                Item target = BuiltInRegistries.ITEM.get(this.morphTargetId);
                if (target != stack.getItem()) {
                    ItemStack morphed = new ItemStack(target, stack.getCount());
                    morphed.setTag(stack.getTag() == null ? null : stack.getTag().copy());
                    morphed.setDamageValue(Math.min(stack.getDamageValue(), morphed.getMaxDamage()));
                    player.setItemInHand(hand, morphed);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER,
                            SoundSource.PLAYERS, 0.65F, 1.15F);
                }
            }
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        return super.use(level, player, hand);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void copyPose(HumanoidModel<?> source, HumanoidModel<?> target) {
        ((HumanoidModel) source).copyPropertiesTo((HumanoidModel) target);
    }

    private void applyPassiveEffects(Player player) {
        applyPassiveEffects(player, this.perks);
    }

    public static void applyPassiveEffects(Player player, long perks) {
        if (has(perks, NIGHTVISION)) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, true, false));
        }
        int jump = jumpBoostAmplifier(perks);
        if (jump >= 0) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 80, jump, true, false));
        }
        if (has(perks, AURAREGEN)) {
            player.addEffect(new MobEffectInstance(RWBYMMobEffects.AURA_REGEN.get(), 80, 0, true, false));
            player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura -> aura.addAmount(2.0F));
        }
        if (has(perks, FIRESTARTER)) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 80, 0, true, false));
        }
        if (has(perks, AntiMagic)) {
            player.removeAllEffects();
        }
    }

    private void tryCreateKoreKosmouWeapon(ItemStack armorStack, Player player) {
        if (this.formWeapon == null
                || !player.isShiftKeyDown()
                || !player.getMainHandItem().isEmpty()
                || !player.swinging
                || player.getCooldowns().isOnCooldown(this)) {
            return;
        }

        Item weapon = BuiltInRegistries.ITEM.get(this.formWeaponId);
        if (weapon == Items.AIR) {
            return;
        }

        ItemStack weaponStack = new ItemStack(weapon);
        // Weapon-store generated Kore Kosmou keeps modifier NBT here; see RWBYMWeaponModifierHelper.java.
        weaponStack.setTag(armorStack.getTag() == null ? null : armorStack.getTag().copy());
        weaponStack.setDamageValue(Math.min(armorStack.getDamageValue(), weaponStack.getMaxDamage()));
        player.setItemInHand(InteractionHand.MAIN_HAND, weaponStack);
        armorStack.hurtAndBreak(30, player, wearer -> wearer.broadcastBreakEvent(EquipmentSlot.CHEST));
        player.getCooldowns().addCooldown(this, 10);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_LEATHER,
                SoundSource.PLAYERS, 0.55F, 1.3F);
    }

    private boolean has(long perk) {
        return (this.perks & perk) != 0L;
    }

    public long getPerks() {
        return this.perks;
    }

    public static boolean hasPerk(LivingEntity entity, long perk) {
        for (ItemStack stack : entity.getArmorSlots()) {
            if (stack.getItem() instanceof RWBYMArmorItem armor && (armor.getPerks() & perk) != 0L) {
                return true;
            }
            if (stack.getItem() instanceof RWBYMWearableItem wearable && (wearable.getPerks() & perk) != 0L) {
                return true;
            }
        }
        return false;
    }

    private static boolean has(long perks, long perk) {
        return (perks & perk) != 0L;
    }

    public static long perksForWearable(String itemName) {
        return switch (itemName) {
            case "antimagic_mask" -> Predator|AntiMagic;
            case "henchmenhat" -> VITALITY1|RUSH2;
            case "henchmenhatglasses" -> VITALITY1;
            case "mariaeyes" -> NIGHTVISION;
            case "mariamask" -> ATTACKBOOST1|REACH1;
            case "ozpinglasses" -> VITALITY1;
            case "rubyhood" -> MOVEMENTSPEED1|VITALITY1;
            case "rvnmask" -> AURAREGEN|ATTACKBOOST1;
            case "summerhood" -> MOVEMENTSPEED1|ATTACKBOOST1;
            case "taylorhood" -> REACH1|DEFENSE1;
            case "whtefng" -> MOVEMENTSPEED1;
            default -> ARMOR_PERKS.getOrDefault(itemName, 0L);
        };
    }

    private static double movementSpeed(long perks) {
        if (has(perks, MOVEMENTSPEED2)) {
            return 0.10D;
        }
        return has(perks, MOVEMENTSPEED1) ? 0.05D : 0.0D;
    }

    private static double bonusArmor(long perks) {
        if (has(perks, DEFENSE2)) {
            return 2.0D;
        }
        return has(perks, DEFENSE1) ? 1.0D : 0.0D;
    }

    private static double bonusHealth(long perks) {
        if (has(perks, VITALITY3)) {
            return 6.0D;
        }
        if (has(perks, VITALITY2)) {
            return 4.0D;
        }
        return has(perks, VITALITY1) ? 2.0D : 0.0D;
    }

    private static double attackBoost(long perks) {
        if (has(perks, ATTACKBOOST4)) {
            return 0.10D;
        }
        if (has(perks, ATTACKBOOST3)) {
            return 0.075D;
        }
        if (has(perks, ATTACKBOOST2)) {
            return 0.05D;
        }
        return has(perks, ATTACKBOOST1) ? 0.025D : 0.0D;
    }

    private static double footing(long perks) {
        if (has(perks, FOOTING2)) {
            return 0.5D;
        }
        return has(perks, FOOTING1) ? 0.25D : 0.0D;
    }

    private static double rush(long perks) {
        if (has(perks, RUSH2)) {
            return 0.10D;
        }
        return has(perks, RUSH1) ? 0.05D : 0.0D;
    }

    private static int jumpBoostAmplifier(long perks) {
        if (has(perks, JUMPBOOST3)) {
            return 2;
        }
        if (has(perks, JUMPBOOST2)) {
            return 1;
        }
        return has(perks, JUMPBOOST1) ? 0 : -1;
    }

    private static void addModifier(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder,
            Attribute attribute, UUID uuid, String name, double amount, AttributeModifier.Operation operation) {
        if (amount != 0.0D) {
            builder.put(attribute, new AttributeModifier(uuid, name, amount, operation));
        }
    }

    private static UUID[] slotUuids(String seed) {
        UUID base = UUID.fromString(seed);
        UUID[] uuids = new UUID[4];
        for (int i = 0; i < uuids.length; i++) {
            uuids[i] = new UUID(base.getMostSignificantBits(), base.getLeastSignificantBits() + i);
        }
        return uuids;
    }

    private static Map<String, Long> createPerkMap() {
        Map<String, Long> perks = new HashMap<>();
        put(perks, "adam_chest", ATTACKBOOST2|GLADIATOR1|RUSH1);
        put(perks, "adam_legs", FOOTING1|MOVEMENTSPEED2);
        put(perks, "adamv6_chest", ATTACKBOOST2|RUSH2|GLADIATOR2);
        put(perks, "adamv6_legs", FOOTING1|MOVEMENTSPEED2);
        put(perks, "amber_chest", AURAREGEN);
        put(perks, "amber_legs", MOVEMENTSPEED2);
        put(perks, "atlas_chest", DEFENSE1|ATTACKBOOST1);
        put(perks, "atlas_head", NIGHTVISION);
        put(perks, "atlas_legs", DEFENSE1|VITALITY1);
        put(perks, "atlasgreen_chest", DEFENSE1|ATTACKBOOST1);
        put(perks, "atlasgreen_head", NIGHTVISION);
        put(perks, "atlasgreen_legs", DEFENSE1|VITALITY1);
        put(perks, "atlasred_chest", DEFENSE1|ATTACKBOOST1);
        put(perks, "atlasred_head", NIGHTVISION);
        put(perks, "atlasred_legs", DEFENSE1|VITALITY1);
        put(perks, "atlasyellow_chest", DEFENSE1|ATTACKBOOST1);
        put(perks, "atlasyellow_head", NIGHTVISION);
        put(perks, "atlasyellow_legs", DEFENSE1|VITALITY1);
        put(perks, "attackcharm", ATTACKBOOST2);
        put(perks, "auracharm", AURAREGEN);
        put(perks, "bailey_chest", PUNCTURE2|ATTACKBOOST1);
        put(perks, "bailey_legs", FIRESTARTER|VITALITY1);
        put(perks, "blake1_chest", GLADIATOR2|ATTACKBOOST2);
        put(perks, "blake1_legs", MOVEMENTSPEED1|JUMPBOOST1);
        put(perks, "blake2_chest", GLADIATOR1);
        put(perks, "blake2_legs", MOVEMENTSPEED1);
        put(perks, "blake3_chest", GLADIATOR1|ATTACKBOOST1);
        put(perks, "blake3_legs", ATTACKBOOST1|JUMPBOOST2);
        put(perks, "blakev7_chest", GLADIATOR2|ATTACKBOOST2|RUSH2);
        put(perks, "blakev7_legs", JUMPBOOST3|FOOTING2|HandofBullets);
        put(perks, "carmine_chest", CRITICALSTRIKE1|ATTACKBOOST1|RUSH1);
        put(perks, "carmine_head", CRITICALSTRIKE1|ATTACKBOOST1|RUSH1);
        put(perks, "carmine_legs", RUSH1|ATTACKBOOST1);
        put(perks, "cinder1_chest", FIRESTARTER);
        put(perks, "cinder1_legs", ATTACKBOOST1);
        put(perks, "cinder2_chest", GLADIATOR1|MOVEMENTSPEED1);
        put(perks, "cinder2_legs", MOVEMENTSPEED2);
        put(perks, "cinder3_chest", FIRESTARTER|AURAREGEN|VITALITY1);
        put(perks, "cinder3_legs", AURAREGEN|ATTACKBOOST2|VITALITY1);
        put(perks, "coco_chest", ATTACKBOOST1|VITALITY1);
        put(perks, "coco_head", FOOTING1|HandofBullets);
        put(perks, "coco_legs", ATTACKBOOST1|RUSH1);
        put(perks, "criticalcharm", CRITICALSTRIKE1);
        put(perks, "dianna_chest", RUSH1|MOVEMENTSPEED1);
        put(perks, "dianna_legs", ATTACKBOOST1|GLADIATOR1);
        put(perks, "edgecharm", GLADIATOR1);
        put(perks, "emerald1_chest", MOVEMENTSPEED1);
        put(perks, "emerald1_legs", MOVEMENTSPEED1|CRITICALSTRIKE1);
        put(perks, "fairyking", JAVELIN2);
        put(perks, "feathercharm", JUMPBOOST2);
        put(perks, "firedancercharm", FIRESTARTER);
        put(perks, "fleetingcharm", MOVEMENTSPEED1);
        put(perks, "healthcharm", VITALITY2);
        put(perks, "henchman_chest", DEFENSE1|MOVEMENTSPEED1);
        put(perks, "henchman_legs", FOOTING1);
        put(perks, "ironwood1_chest", DEFENSE1|VITALITY1|ATTACKBOOST1);
        put(perks, "ironwood1_legs", DEFENSE1|ATTACKBOOST1);
        put(perks, "ironwood2_chest", DEFENSE1|MOVEMENTSPEED2);
        put(perks, "ironwood2_legs", HandofBullets|FOOTING1);
        put(perks, "juane1_chest", DEFENSE2|VITALITY1);
        put(perks, "juane1_legs", AURAREGEN|GLADIATOR1);
        put(perks, "kingsgambit", KINGSGAMBIT);
        put(perks, "kingsgambitpawn", KINGSPAWN);
        put(perks, "knockoutcharm", K01);
        put(perks, "maidencharm", MAIDEN);
        put(perks, "maria_chest", ATTACKBOOST3|MOVEMENTSPEED1);
        put(perks, "maria_legs", REACH2);
        put(perks, "mercury1_chest", ATTACKBOOST2|MOVEMENTSPEED1|FOOTING1);
        put(perks, "mercury1_legs", RUSH1|MOVEMENTSPEED1|K02);
        put(perks, "mercury2_chest", MOVEMENTSPEED1);
        put(perks, "mercury2_legs", RUSH1);
        put(perks, "neo_chest", PUNCTURE2|ATTACKBOOST1);
        put(perks, "neo_legs", DEFENSE1|VITALITY2);
        put(perks, "neptune_chest", FOOTING2);
        put(perks, "neptune_head", REACH1|MOVEMENTSPEED1);
        put(perks, "neptune_legs", REACH1|VITALITY1);
        put(perks, "nora1_chest", RUSH1|K01);
        put(perks, "nora1_legs", K02|FOOTING1);
        put(perks, "oscarv4_chest", VITALITY1|PUNCTURE1);
        put(perks, "oscarv4_legs", AURAREGEN|RUSH1);
        put(perks, "oscarv6_chest", PUNCTURE2|VITALITY2|MOVEMENTSPEED1);
        put(perks, "oscarv6_legs", RUSH1|ATTACKBOOST1);
        put(perks, "ozma1_chest", K01|VITALITY1);
        put(perks, "ozma1_legs", AURAREGEN);
        put(perks, "ozma2_chest", VITALITY1|AURAREGEN);
        put(perks, "ozma2_legs", K01);
        put(perks, "ozma3_chest", K02|VITALITY2);
        put(perks, "ozma3_legs", RUSH2|ATTACKBOOST4);
        put(perks, "ozpin_chest", VITALITY2);
        put(perks, "ozpin_legs", VITALITY1);
        put(perks, "penny_chest", VITALITY2|DEFENSE1);
        put(perks, "penny_legs", AURAREGEN|DEFENSE2|GLADIATOR2);
        put(perks, "pennyv7_chest", DEFENSE2|VITALITY1|ATTACKBOOST1);
        put(perks, "pennyv7_head", JAVELIN1|VITALITY2);
        put(perks, "pennyv7_legs", DEFENSE1|GLADIATOR2|AURAREGEN);
        put(perks, "puncturecharm", PUNCTURE1);
        put(perks, "pyrrha_chest", ATTACKBOOST1|DEFENSE1|REACH1);
        put(perks, "pyrrha_legs", GLADIATOR1|JAVELIN1|JUMPBOOST1);
        put(perks, "qrow_chest", MOVEMENTSPEED1|GLADIATOR1);
        put(perks, "qrow_legs", GLADIATOR1|ATTACKBOOST2);
        put(perks, "ragora_chest", JUMPBOOST1|REACH1);
        put(perks, "ragora_head", AURAREGEN);
        put(perks, "ragora_legs", AURAREGEN);
        put(perks, "raven_chest", ATTACKBOOST2|AURAREGEN);
        put(perks, "raven_legs", RUSH1|JUMPBOOST1|MOVEMENTSPEED1);
        put(perks, "reachcharm", REACH1);
        put(perks, "rimuru_chest", Predator);
        put(perks, "rimuru_legs", Predator);
        put(perks, "roman_chest", RUSH1);
        put(perks, "roman_head", MOVEMENTSPEED1|RUSH1);
        put(perks, "roman_legs", MOVEMENTSPEED1|K01);
        put(perks, "ruby1_chest", VITALITY1);
        put(perks, "ruby1_legs", MOVEMENTSPEED2);
        put(perks, "ruby2_chest", MOVEMENTSPEED1|VITALITY1);
        put(perks, "ruby2_head", MOVEMENTSPEED1|VITALITY1);
        put(perks, "ruby2_legs", MOVEMENTSPEED1|JUMPBOOST1);
        put(perks, "ruby3_chest", MOVEMENTSPEED2|ATTACKBOOST1);
        put(perks, "ruby3_legs", RUSH1|REACH1);
        put(perks, "rubyv7_chest", MOVEMENTSPEED2|REACH2|HandofBullets);
        put(perks, "rubyv7_legs", JUMPBOOST2|ATTACKBOOST3);
        put(perks, "rushcharm", RUSH1);
        put(perks, "sage_chest", FOOTING1|ATTACKBOOST1|VITALITY1);
        put(perks, "sage_legs", ATTACKBOOST1|FOOTING1);
        put(perks, "salem_chest", VITALITY1);
        put(perks, "salem_legs", VITALITY2);
        put(perks, "sasha_chest", CRITICALSTRIKE2|DEFENSE1);
        put(perks, "sasha_legs", VITALITY1|MOVEMENTSPEED1);
        put(perks, "scarlet_chest", MOVEMENTSPEED1|PUNCTURE1);
        put(perks, "scarlet_legs", PUNCTURE1|RUSH1);
        put(perks, "silvercharm", SILVERLIGHT);
        put(perks, "summer1_chest", MOVEMENTSPEED2|ATTACKBOOST1);
        put(perks, "summer1_legs", RUSH1|REACH1);
        put(perks, "summer2_chest", DEFENSE1|ATTACKBOOST2);
        put(perks, "summer2_head", MOVEMENTSPEED1|ATTACKBOOST1);
        put(perks, "summer2_legs", REACH1|MOVEMENTSPEED1);
        put(perks, "sun_chest", JUMPBOOST1|REACH1);
        put(perks, "sun_legs", JUMPBOOST2|RUSH2);
        put(perks, "tankcharm", DEFENSE2);
        put(perks, "taylor_chest", REACH1|VITALITY1);
        put(perks, "taylor_head", REACH1|DEFENSE1);
        put(perks, "taylor_legs", JUMPBOOST1);
        put(perks, "velvet_chest", GLADIATOR1|REACH1);
        put(perks, "velvet_legs", PUNCTURE1|K01|CRITICALSTRIKE1);
        put(perks, "weiss1_chest", AURAREGEN);
        put(perks, "weiss1_legs", MOVEMENTSPEED1|JUMPBOOST1);
        put(perks, "weiss2_chest", PUNCTURE1|FOOTING1);
        put(perks, "weiss2_legs", ATTACKBOOST1);
        put(perks, "weiss3_chest", PUNCTURE2|AURAREGEN|MOVEMENTSPEED1);
        put(perks, "weiss3_legs", ATTACKBOOST2);
        put(perks, "weissv7_chest", PUNCTURE2|AURAREGEN|MOVEMENTSPEED2);
        put(perks, "weissv7_legs", ATTACKBOOST3|MOVEMENTSPEED1|RUSH2);
        put(perks, "winter_chest", PUNCTURE1|ATTACKBOOST2);
        put(perks, "winter_legs", PUNCTURE1|ATTACKBOOST1);
        put(perks, "yang1_chest", RUSH1|K01);
        put(perks, "yang1_legs", K02|FOOTING1);
        put(perks, "yang2_chest", K01|VITALITY1);
        put(perks, "yang2_legs", FIRESTARTER|FOOTING1);
        put(perks, "yang3_chest", K01|ATTACKBOOST1);
        put(perks, "yang3_legs", K01|ATTACKBOOST1);
        put(perks, "yang4_chest", RUSH1|K02|FOOTING1);
        put(perks, "yang4_legs", RUSH1|K01);
        put(perks, "yangv7_chest", RUSH2|K02|DEFENSE2);
        put(perks, "yangv7_legs", FIRESTARTER|MOVEMENTSPEED1|ATTACKBOOST4);
        return perks;
    }

    private static void put(Map<String, Long> perks, String itemName, long value) {
        perks.put(itemName, value);
    }
}

