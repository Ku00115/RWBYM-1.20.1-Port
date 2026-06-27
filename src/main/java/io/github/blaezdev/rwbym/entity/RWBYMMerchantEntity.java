package io.github.blaezdev.rwbym.entity;

import io.github.blaezdev.rwbym.item.RWBYMWeaponModifierHelper;
import io.github.blaezdev.rwbym.menu.RWBYMMerchantMenu;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;

public class RWBYMMerchantEntity extends AbstractVillager {
    private static final int LEGACY_EFFECTIVE_MAX_TRADE_USES = 9_900_007;
    private static final Map<String, String> LEGACY_TRADE_ITEM_IDS = Map.ofEntries(
            Map.entry("wallet", "lienwallet"),
            Map.entry("henchmanchest", "henchman_chest"),
            Map.entry("henchmanlegs", "henchman_legs"),
            Map.entry("rwbyblock7", "smrgrave"),
            Map.entry("rwbyblock8", "toolkit"),
            Map.entry("scrollwhite7", "7scroll_white"),
            Map.entry("scrollblack7", "7scroll_black"),
            Map.entry("scrollblue7", "7scroll_blue"),
            Map.entry("scrollgreen7", "7scroll_green"),
            Map.entry("scrollpink7", "7scroll_pink"),
            Map.entry("scrollred7", "7scroll_red"),
            Map.entry("scrollyellow7", "7scroll_yellow"));
    private String cachedKind;

    public RWBYMMerchantEntity(EntityType<? extends AbstractVillager> type, Level level) {
        super(type, level);
        if (this.getNavigation() instanceof GroundPathNavigation groundNavigation) {
            groundNavigation.setCanOpenDoors(true);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 0.6D, 0.6D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Evoker.class, 12.0F, 0.8D, 0.8D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vindicator.class, 8.0F, 0.8D, 0.8D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vex.class, 8.0F, 0.6D, 0.6D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, BasicGrimmEntity.class, 8.0F, 0.6D, 0.6D));
        this.goalSelector.addGoal(4, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.6D));
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.tickCount == 1 && merchantKind().equals("blackstore")) {
            ItemStack mask = itemStack("whtefng", 1);
            if (!mask.isEmpty()) {
                this.setItemSlot(EquipmentSlot.HEAD, mask);
            }
        }
        for (Monster monster : this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(60.0D))) {
            if (monster.getTarget() == this) {
                monster.setTarget(null);
            }
        }
        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(0.5F);
        }
        if (this.tickCount > 0 && this.tickCount % 18000 == 0) {
            this.getOffers().clear();
            this.updateTrades();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(Items.NAME_TAG)) {
            return super.mobInteract(player, hand);
        }
        if (!this.level().isClientSide()) {
            if (this.getOffers().isEmpty()) {
                return super.mobInteract(player, hand);
            }
            if (player instanceof ServerPlayer serverPlayer) {
                this.setTradingPlayer(player);
                // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
                // The original opened RWBYM's custom GuiVillager; NetworkHooks opens the modern menu while
                // ServerPlayer#sendMerchantOffers keeps vanilla merchant offer synchronization intact.
                NetworkHooks.openScreen(serverPlayer,
                        new SimpleMenuProvider((containerId, inventory, menuPlayer) ->
                                new RWBYMMerchantMenu(containerId, inventory, this), this.getDisplayName()),
                        buffer -> buffer.writeVarInt(this.getId()));
                serverPlayer.sendMerchantOffers(serverPlayer.containerMenu.containerId, this.getOffers(), 1,
                        this.getVillagerXp(), false, true);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    @Override
    protected void updateTrades() {
        MerchantOffers offers = this.getOffers();
        if (!offers.isEmpty()) {
            return;
        }
        switch (merchantKind()) {
            case "store" -> addStoreTrades(offers);
            case "weaponstore" -> addWeaponStoreTrades(offers);
            case "blackstore" -> addBlackStoreTrades(offers);
            case "armorstore" -> addArmorStoreTrades(offers);
            case "crowbar" -> addCrowbarTrades(offers);
        }
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // Original RWBYM merchants rejected all incoming damage.
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    private void addStoreTrades(MerchantOffers offers) {
        for (String dust : new String[] {
                "firedust", "dust", "gravitydust", "icedust", "lightdust", "waterdust", "winddust"
        }) {
            addTrade(offers, "lien10", 1, dust, 1);
        }
        for (String crystal : new String[] {
                "dustcrystal", "firedustcrystal", "gravitydustcrystal", "icedustcrystal",
                "lightdustcrystal", "waterdustcrystal", "winddustcrystal"
        }) {
            addTrade(offers, "lien10", 1, crystal, 1);
        }
        for (String dust : new String[] {
                "firedust", "dust", "gravitydust", "icedust", "lightdust", "waterdust", "winddust"
        }) {
            addTrade(offers, dust, 1, "lien5", 1);
        }
        for (String crystal : new String[] {
                "dustcrystal", "firedustcrystal", "gravitydustcrystal", "icedustcrystal",
                "lightdustcrystal", "waterdustcrystal", "winddustcrystal"
        }) {
            addTrade(offers, crystal, 1, "lien5", 1);
        }
        for (String coin : new String[] {
                "coinr", "coinw", "coinb", "coiny", "coinjaune", "coin_pyrrha", "coinnora",
                "coin_ren", "coin_lysette", "coin_ragora", "coin_clover", "coin_harriet",
                "coinqrow", "coin_valour"
        }) {
            addTrade(offers, "bait", 1, "remnants", 16, coin, 1);
        }
        addTrade(offers, "lien500", 1, "signdust", 1);
        addTrade(offers, "lien500", 6, "zwei", 1);
        addTrade(offers, "lien1", 5, "lien5", 1);
        addTrade(offers, "lien5", 2, "lien10", 1);
        addTrade(offers, "lien10", 2, "lien20", 1);
        addTrade(offers, "lien20", 2, "lien10", 1, "lien50", 1);
        addTrade(offers, "lien10", 5, "lien50", 1);
        addTrade(offers, "lien50", 2, "lien100", 1);
        addTrade(offers, "lien100", 5, "lien500", 1);
        addTrade(offers, "lien500", 1, "lien100", 5);
        addTrade(offers, "lien100", 1, "lien50", 2);
        addTrade(offers, "lien50", 1, "lien10", 5);
        addTrade(offers, "lien20", 1, "lien10", 2);
        addTrade(offers, "lien10", 1, "lien5", 2);
        addTrade(offers, "lien5", 1, "lien1", 5);
    }

    private void addBlackStoreTrades(MerchantOffers offers) {
        addTrade(offers, "lien500", 2, "rvnmask", 1);
        for (String itemName : new String[] {
                "whtefng", "henchmenhat", "henchmenhatglasses", "henchmanchest", "henchmanlegs",
                "atlasknight", "crush", "chisel", "crusher",
                "scroll", "scroll2", "container", "lieutenant", "whitefangspear", "whitefangsword",
                "whitefangrifle", "wfp", "henchmen", "henchmenaxe"
        }) {
            int price = switch (itemName) {
                case "crush", "chisel" -> 2;
                case "crusher", "container" -> 10;
                case "lieutenant", "whitefangspear", "whitefangsword", "whitefangrifle", "wfp", "henchmen", "henchmenaxe" -> 4;
                default -> 1;
            };
            addTrade(offers, "lien500", price, itemName, 1);
        }
        // Original BlackStore sold storage pouches for lien50, not lien500 like the other black-market items.
        for (String pouch : new String[] { "wallet", "dustpouch", "partspouch" }) {
            addTrade(offers, "lien50", 1, pouch, 1);
        }
        addTrade(offers, "lien500", 1, "hrdltfence", 15);
        addTrade(offers, "lien100", 3, "lantern", 32);
        for (String coin : new String[] {
                "coinr", "coinw", "coinb", "coiny", "coinjaune", "coin_pyrrha", "coinnora",
                "coin_ren", "coin_lysette", "coin_ragora", "coin_clover", "coin_harriet",
                "coinqrow", "coin_valour"
        }) {
            addTrade(offers, "lien500", 25, coin, 1);
        }
        for (String summerItem : new String[] {
                "summer1_chest", "summer1_legs", "summer2_chest", "summer2_legs", "summerhood"
        }) {
            addTrade(offers, "rwbyblock7", 1, summerItem, 1);
        }
        for (String scrollVariant : new String[] {
                "scrollred7", "scrollwhite7", "scrollblack7", "scrollyellow7",
                "scrollblue7", "scrollgreen7", "scrollpink7"
        }) {
            addTrade(offers, "lien500", 3, scrollVariant, 1);
        }
        addTrade(offers, "remnants", 1, "lien20", 2);
        addTrade(offers, "lien500", 6, "grimm_bucket", 1);
        addTrade(offers, "bait", 1, "lien500", 1);
        addTrade(offers, "lien1", 5, "lien5", 1);
        addTrade(offers, "lien5", 2, "lien10", 1);
        addTrade(offers, "lien10", 2, "lien20", 1);
        addTrade(offers, "lien20", 2, "lien10", 1, "lien50", 1);
        addTrade(offers, "lien10", 5, "lien50", 1);
        addTrade(offers, "lien50", 2, "lien100", 1);
        addTrade(offers, "lien100", 5, "lien500", 1);
        addTrade(offers, "lien500", 1, "lien100", 5);
        addTrade(offers, "lien100", 1, "lien50", 2);
        addTrade(offers, "lien50", 1, "lien10", 5);
        addTrade(offers, "lien20", 1, "lien10", 2);
        addTrade(offers, "lien10", 1, "lien5", 2);
        addTrade(offers, "lien5", 1, "lien1", 5);
    }

    private void addWeaponStoreTrades(MerchantOffers offers) {
        addTrade(offers, "lien50", 1, "roseiron", 1);
        addTrade(offers, "lien50", 1, "frostediron", 1);
        addTrade(offers, "lien50", 1, "shadowiron", 1);
        addTrade(offers, "lien50", 1, "gildediron", 1);
        addTrade(offers, "lien50", 1, "viridianiron", 1);
        addTrade(offers, "lien50", 1, "forestiron", 1);
        addOffer(offers, createOffer(itemStack("lien50", 1), new ItemStack(Items.IRON_INGOT)));
        addTrade(offers, "lien20", 1, "scrap", 10);

        List<MerchantOffer> weaponPool = new ArrayList<>();
        addGeneratedTrade(weaponPool, "lien500", 4, "pennyswd");
        addGeneratedTrade(weaponPool, "lien500", 4, "henchmen");
        addGeneratedTrade(weaponPool, "lien500", 4, "henchmenaxe");
        addGeneratedTrade(weaponPool, "lien500", 4, "fennec");
        addGeneratedTrade(weaponPool, "lien500", 4, "corsac");
        addGeneratedTrade(weaponPool, "lien500", 4, "neoumb_closed");
        addGeneratedTrade(weaponPool, "lien500", 4, "torchwick");
        addGeneratedTrade(weaponPool, "lien500", 4, "lieutenant");
        addGeneratedTrade(weaponPool, "lien500", 4, "whitefangspear");
        addGeneratedTrade(weaponPool, "lien500", 4, "whitefangsword");
        addGeneratedTrade(weaponPool, "lien500", 4, "whitefangrifle");
        addGeneratedTrade(weaponPool, "lien500", 4, "wfp");
        addGeneratedTrade(weaponPool, "lien500", 4, "iliasword");
        addGeneratedTrade(weaponPool, "lien500", 4, "deemace");
        addGeneratedTrade(weaponPool, "lien500", 4, "cardin");
        addGeneratedTrade(weaponPool, "lien500", 4, "dove");
        addGeneratedTrade(weaponPool, "lien500", 4, "russelnormal");
        addGeneratedTrade(weaponPool, "lien500", 4, "lark");
        addGeneratedTrade(weaponPool, "lien500", 4, "nebulabow");
        addGeneratedTrade(weaponPool, "lien500", 4, "dew");
        addGeneratedTrade(weaponPool, "lien500", 4, "gwenknife");
        addGeneratedTrade(weaponPool, "lien500", 4, "octavia");
        addGeneratedTrade(weaponPool, "lien500", 4, "atlaspistol");
        addGeneratedTrade(weaponPool, "lien500", 4, "atlasrifle");
        addGeneratedTrade(weaponPool, "lien500", 4, "frostediron", 4, "cinder");
        addGeneratedTrade(weaponPool, "lien500", 4, "shadowiron", 4, "cinderglass");
        addGeneratedTrade(weaponPool, "lien500", 4, "shadowiron", 8, "jnrbat");
        addGeneratedTrade(weaponPool, "lien500", 4, "forestiron", 4, "emeraldgun");
        addGeneratedTrade(weaponPool, "lien500", 4, "shadowiron", 4, "adamswd");
        addGeneratedTrade(weaponPool, "lien500", 5, "shadowiron", 2, "tyrian");
        addGeneratedTrade(weaponPool, "lien500", 6, "frostediron", 8, "winterswd");
        addGeneratedTrade(weaponPool, "lien500", 6, "brawnz");
        addGeneratedTrade(weaponPool, "lien500", 6, "nornir");
        addGeneratedTrade(weaponPool, "lien500", 6, "freyr");
        addGeneratedTrade(weaponPool, "lien500", 6, "royg");
        addGeneratedTrade(weaponPool, "lien500", 6, "nolan");
        addGeneratedTrade(weaponPool, "lien500", 6, "mayrifle");
        addGeneratedTrade(weaponPool, "lien500", 6, "ozpincane");
        addGeneratedTrade(weaponPool, "lien500", 6, "portgun");
        addGeneratedTrade(weaponPool, "lien500", 6, "ironwood");
        addGeneratedTrade(weaponPool, "lien500", 6, "ironwood2");
        addGeneratedTrade(weaponPool, "lien500", 6, "kingfisher");
        addGeneratedTrade(weaponPool, "lien500", 6, "fetch");
        addGeneratedTrade(weaponPool, "lien500", 6, "goodwitch");
        addGeneratedTrade(weaponPool, "lien500", 6, "oobleckthermos");
        addGeneratedTrade(weaponPool, "lien500", 6, "lionheart");
        addGeneratedTrade(weaponPool, "lien500", 8, "qrowsword");
        addGeneratedTrade(weaponPool, "lien500", 8, "rvnswd");
        addGeneratedTrade(weaponPool, "lien500", 7, "vernal");
        addGeneratedTrade(weaponPool, "lien500", 7, "robynshield");
        addGeneratedTrade(weaponPool, "lien500", 7, "sunstaff");
        addGeneratedTrade(weaponPool, "lien500", 7, "sage");
        addGeneratedTrade(weaponPool, "lien500", 7, "scarletsword");
        addGeneratedTrade(weaponPool, "lien500", 7, "neptunegun");
        addGeneratedTrade(weaponPool, "lien500", 3, "wattshield");
        addGeneratedTrade(weaponPool, "lien500", 7, "arslan");
        addGeneratedTrade(weaponPool, "lien500", 7, "bolin");
        addGeneratedTrade(weaponPool, "lien500", 7, "shadowiron", 16, "reese");
        addGeneratedTrade(weaponPool, "lien500", 7, "nadirgun");
        addGeneratedTrade(weaponPool, "lien500", 8, "roseiron", 8, "crescent");
        addGeneratedTrade(weaponPool, "lien500", 8, "roseiron", 8, "lucidroseboard");
        addGeneratedTrade(weaponPool, "lien500", 8, "frostediron", 8, "weiss");
        addGeneratedTrade(weaponPool, "lien500", 8, "shadowiron", 8, "gambol");
        addGeneratedTrade(weaponPool, "lien500", 8, "gildediron", 8, "ember");
        addGeneratedTrade(weaponPool, "lien500", 8, "gildediron", 8, "juane");
        addGeneratedTrade(weaponPool, "lien500", 8, "frostediron", 8, "norahammer");
        addGeneratedTrade(weaponPool, "lien500", 8, "roseiron", 8, "pyrrhaspear");
        addGeneratedTrade(weaponPool, "lien500", 8, "forestiron", 8, "stormflower");
        addGeneratedTrade(weaponPool, "lien500", 10, "viridianiron", 8, "korekosmouoff");
        addGeneratedTrade(weaponPool, "lien500", 10, "viridianiron", 8, "chatareusgun");
        addGeneratedTrade(weaponPool, "lien500", 10, "viridianiron", 8, "razorbolt");
        addGeneratedTrade(weaponPool, "lien500", 10, "viridianiron", 8, "hexen");
        addGeneratedTrade(weaponPool, "lien500", 10, "viridianiron", 8, "magnumgun");
        addGeneratedTrade(weaponPool, "lien500", 10, "viridianiron", 8, "lysettesword");
        addGeneratedTrade(weaponPool, "lien500", 12, "vidian");
        addGeneratedTrade(weaponPool, "lien500", 12, "aquaealatlsword");
        addGeneratedTrade(weaponPool, "lien500", 12, "scarletstormgun");
        addGeneratedTrade(weaponPool, "lien500", 12, "mariacane");
        addGeneratedTrade(weaponPool, "lien500", 12, "tocksword");
        addGeneratedTrade(weaponPool, "lien500", 12, "ozmacane");
        addGeneratedTrade(weaponPool, "lien500", 12, "amberstafffire");
        addGeneratedTrade(weaponPool, "lien500", 12, "cocobag");
        addGeneratedTrade(weaponPool, "lien500", 12, "velvet");
        addGeneratedTrade(weaponPool, "lien500", 12, "yatsuhashi");
        addGeneratedTrade(weaponPool, "lien500", 12, "fox");
        addGeneratedTrade(weaponPool, "lien500", 12, "angelcane");
        addGeneratedTrade(weaponPool, "lien500", 12, "pugzsword");
        addGeneratedTrade(weaponPool, "lien500", 12, "onoyari");
        addGeneratedTrade(weaponPool, "lien500", 12, "whisperingblossom");
        addGeneratedTrade(weaponPool, "lien500", 12, "cassandra");
        addGeneratedTrade(weaponPool, "lien500", 14, "noctustraumnormal");
        addGeneratedTrade(weaponPool, "lien500", 14, "carminesai");
        addRandomOfferSubset(offers, weaponPool, 81);

        addTrade(offers, "lien500", 4, "rwbyblock8", 1);
        addTrade(offers, "lien100", 2, "extasisammo", 3);
        addTrade(offers, "lien100", 1, "bolt", 10);
        addTrade(offers, "lien100", 1, "boltfire", 10);
        addTrade(offers, "lien100", 1, "boltwind", 10);
        addTrade(offers, "lien100", 1, "boltlight", 10);
        addTrade(offers, "lien100", 1, "boltice", 10);
        addTrade(offers, "lien100", 1, "boltgrav", 10);
        addTrade(offers, "lien100", 5, "p90_mag", 1);
        addTrade(offers, "lien50", 1, "p90bullet", 50);
        addTrade(offers, "lien20", 1, "50bmg", 7);
        addTrade(offers, "lien100", 5, "hecate_mag", 1);
        addTrade(offers, "remnants", 1, "lien10", 3);

        addTrade(offers, "lien1", 5, "lien5", 1);
        addTrade(offers, "lien5", 2, "lien10", 1);
        addTrade(offers, "lien10", 2, "lien20", 1);
        addTrade(offers, "lien20", 2, "lien10", 1, "lien50", 1);
        addTrade(offers, "lien10", 5, "lien50", 1);
        addTrade(offers, "lien50", 2, "lien100", 1);
        addTrade(offers, "lien100", 5, "lien500", 1);
        addTrade(offers, "lien500", 1, "lien100", 5);
        addTrade(offers, "lien100", 1, "lien50", 2);
        addTrade(offers, "lien50", 1, "lien10", 5);
        addTrade(offers, "lien20", 1, "lien10", 2);
        addTrade(offers, "lien10", 1, "lien5", 2);
        addTrade(offers, "lien5", 1, "lien1", 5);
    }

    private void addArmorStoreTrades(MerchantOffers offers) {
        List<MerchantOffer> armorPool = new ArrayList<>();
        for (String armorItem : new String[] {
                "qrow_chest", "qrow_legs", "ragora_legs", "ragora_chest", "ragora_head", "juane1_chest",
                "juane1_legs", "nora1_chest", "nora1_legs", "weiss1_chest", "weiss1_legs", "adam_chest",
                "adam_legs", "carmine_chest", "carmine_head", "carmine_legs", "atlas_chest", "atlas_head",
                "atlas_legs", "atlasred_chest", "atlasred_head", "atlasred_legs", "atlasyellow_chest",
                "atlasyellow_head", "atlasyellow_legs", "atlasgreen_chest", "atlasgreen_head", "atlasgreen_legs",
                "beacon1_chest", "beacon1_legs", "beacon_chest", "beacon_legs", "blake1_chest", "blake1_legs",
                "blake2_chest", "blake2_legs", "blake3_chest", "blake3_legs", "amber_chest", "amber_legs",
                "cinder1_chest", "cinder1_legs", "cinder2_chest", "cinder2_legs", "cinder3_chest", "cinder3_legs",
                "coco_chest", "coco_head", "coco_legs", "emerald1_chest", "emerald1_legs", "emerald2_chest",
                "emerald2_legs", "penny_chest", "penny_legs", "pyrrha_chest", "pyrrha_legs", "raven_chest",
                "raven_legs", "rubyhood", "ruby1_chest", "ruby1_legs", "ruby2_chest", "ruby2_legs", "ruby3_chest",
                "ruby3_legs", "salem_chest", "salem_legs", "velvet_chest", "velvet_legs", "weiss2_chest",
                "weiss2_legs", "weiss3_chest", "weiss3_legs", "winter_chest", "winter_legs", "yang1_chest",
                "yang1_legs", "yang2_chest", "yang2_legs", "yang3_chest", "yang3_legs", "yang4_chest", "yang4_legs",
                "neptune_head", "neptune_chest", "neptune_legs", "roman_head", "roman_chest", "roman_legs",
                "ironwood1_chest", "ironwood1_legs", "ironwood2_chest", "ironwood2_legs", "mercury1_chest",
                "mercury1_legs", "mercury2_chest", "mercury2_legs", "ozpin_chest", "ozpin_legs", "sage_chest",
                "sage_legs", "sun_chest", "sun_legs", "scarlet_chest", "scarlet_legs", "adamv6_chest",
                "adamv6_legs", "neo_chest", "neo_legs", "oscarv4_chest", "oscarv4_legs", "oscarv6_chest",
                "oscarv6_legs", "ozma1_chest", "ozma1_legs", "ozma2_chest", "ozma2_legs", "ozma3_chest",
                "ozma3_legs", "pennyv7_chest", "pennyv7_legs", "pennyv7_head", "rvnmask", "whtefng", "mariaeyes",
                "mariamask", "ozpinglasses", "maria_chest", "maria_legs", "henchmenhat", "henchmenhatglasses",
                "henchman_chest", "henchman_legs", "taylor_head", "taylorhood", "taylor_chest", "taylor_legs",
                "sasha_chest", "sasha_legs", "dianna_chest", "dianna_legs", "bailey_chest", "bailey_legs"
        }) {
            addPoolTrade(armorPool, "lien50", 3, armorItem, 1);
        }
        addPoolTrade(armorPool, "lien100", 4, "scroll", 1);
        for (String premiumArmor : new String[] {
                "antimagic_mask", "rimuru_chest", "rimuru_legs", "rubyv7_chest", "rubyv7_legs", "yangv7_chest",
                "yangv7_legs", "blakev7_chest", "blakev7_legs", "weissv7_chest", "weissv7_legs"
        }) {
            addPoolTrade(armorPool, "lien100", 2, premiumArmor, 1);
        }
        addPoolTrade(armorPool, "lien100", 4, "scroll2", 1);
        addRandomOfferSubset(offers, armorPool, 46);

        addTrade(offers, "lien500", 4, "rwbyblock8", 1);
        addTrade(offers, "remnants", 1, "lien10", 3);
        addTrade(offers, "lien1", 5, "lien5", 1);
        addTrade(offers, "lien5", 2, "lien10", 1);
        addTrade(offers, "lien10", 2, "lien20", 1);
        addTrade(offers, "lien20", 2, "lien10", 1, "lien50", 1);
        addTrade(offers, "lien10", 5, "lien50", 1);
        addTrade(offers, "lien50", 2, "lien100", 1);
        addTrade(offers, "lien100", 5, "lien500", 1);
        addTrade(offers, "lien500", 1, "lien100", 5);
        addTrade(offers, "lien100", 1, "lien50", 2);
        addTrade(offers, "lien50", 1, "lien10", 5);
        addTrade(offers, "lien20", 1, "lien10", 2);
        addTrade(offers, "lien10", 1, "lien5", 2);
        addTrade(offers, "lien5", 1, "lien1", 5);
    }

    private void addCrowbarTrades(MerchantOffers offers) {
        addTrade(offers, "lien20", 1, "torchquick", 1);
        addTrade(offers, "lien50", 1, "hchoc", 1);
        addTrade(offers, "lien50", 1, "plg", 1);
        addTrade(offers, "lien20", 3, "sunrise", 1);
        addTrade(offers, "lien20", 3, "coconutmilk", 1);
        addTrade(offers, "lien20", 4, "qrowflask", 1);
        addTrade(offers, "lien20", 4, "coffee", 1);
        addTrade(offers, "lien20", 4, "sake", 1);
        addTrade(offers, "lien50", 3, "pancakes", 1);
        addTrade(offers, "lien50", 2, "fishramen", 1);
        addTrade(offers, "lien50", 2, "ramen", 1);
        addTrade(offers, "lien500", 1, "signcrow", 1);

        addTrade(offers, "lien1", 5, "lien5", 1);
        addTrade(offers, "lien5", 2, "lien10", 1);
        addTrade(offers, "lien10", 2, "lien20", 1);
        addTrade(offers, "lien20", 2, "lien10", 1, "lien50", 1);
        addTrade(offers, "lien10", 5, "lien50", 1);
        addTrade(offers, "lien50", 2, "lien100", 1);
        addTrade(offers, "lien100", 5, "lien500", 1);
        addTrade(offers, "lien500", 1, "lien100", 5);
        addTrade(offers, "lien100", 1, "lien50", 2);
        addTrade(offers, "lien50", 1, "lien10", 5);
        addTrade(offers, "lien20", 1, "lien10", 2);
        addTrade(offers, "lien10", 1, "lien5", 2);
        addTrade(offers, "lien5", 1, "lien1", 5);
    }

    private void addTrade(MerchantOffers offers, String costItem, int costCount, String resultItem, int resultCount) {
        addOffer(offers, createOffer(itemStack(costItem, costCount), itemStack(resultItem, resultCount)));
    }

    private void addTrade(MerchantOffers offers, String costItemA, int costCountA,
            String costItemB, int costCountB, String resultItem, int resultCount) {
        addOffer(offers, createOffer(itemStack(costItemA, costCountA), itemStack(costItemB, costCountB),
                itemStack(resultItem, resultCount)));
    }

    private void addGeneratedTrade(List<MerchantOffer> offers, String costItem, int costCount, String resultItem) {
        MerchantOffer offer = createOffer(itemStack(costItem, costCount), generatedWeaponStack(resultItem));
        if (offer != null) {
            offers.add(offer);
        }
    }

    private void addGeneratedTrade(List<MerchantOffer> offers, String costItemA, int costCountA,
            String costItemB, int costCountB, String resultItem) {
        MerchantOffer offer = createOffer(itemStack(costItemA, costCountA), itemStack(costItemB, costCountB),
                generatedWeaponStack(resultItem));
        if (offer != null) {
            offers.add(offer);
        }
    }

    private void addPoolTrade(List<MerchantOffer> offers, String costItem, int costCount,
            String resultItem, int resultCount) {
        MerchantOffer offer = createOffer(itemStack(costItem, costCount), itemStack(resultItem, resultCount));
        if (offer != null) {
            offers.add(offer);
        }
    }

    private void addRandomOfferSubset(MerchantOffers offers, List<MerchantOffer> offerPool, int originalCount) {
        if (offerPool.isEmpty()) {
            return;
        }
        int targetCount = Math.min(originalCount, offerPool.size());
        int nextRandom = this.random.nextInt(offerPool.size());
        Set<Integer> selected = new HashSet<>();
        selected.add(nextRandom);
        for (int i = 1; i < targetCount; i++) {
            while (selected.contains(nextRandom)) {
                nextRandom = this.random.nextInt(offerPool.size());
            }
            selected.add(nextRandom);
        }
        for (int index : selected) {
            offers.add(offerPool.get(index));
        }
    }

    private ItemStack generatedWeaponStack(String itemName) {
        ItemStack stack = itemStack(itemName, 1);
        return stack.isEmpty() ? ItemStack.EMPTY : RWBYMWeaponModifierHelper.createGeneratedWeaponStack(stack, this.random);
    }

    private void addOffer(MerchantOffers offers, @Nullable MerchantOffer offer) {
        if (offer != null) {
            offers.add(offer);
        }
    }

    @Nullable
    private MerchantOffer createOffer(ItemStack cost, ItemStack result) {
        if (cost.isEmpty() || result.isEmpty()) {
            return null;
        }
        // Original GuiVillager raised disabled selected trades by 9,900,000 uses; 1.20 maxUses is final.
        return new MerchantOffer(cost, result, LEGACY_EFFECTIVE_MAX_TRADE_USES, 1, 0.0F);
    }

    @Nullable
    private MerchantOffer createOffer(ItemStack costA, ItemStack costB, ItemStack result) {
        if (costA.isEmpty() || costB.isEmpty() || result.isEmpty()) {
            return null;
        }
        // Original GuiVillager raised disabled selected trades by 9,900,000 uses; 1.20 maxUses is final.
        return new MerchantOffer(costA, costB, result, LEGACY_EFFECTIVE_MAX_TRADE_USES, 1, 0.0F);
    }

    private ItemStack itemStack(String itemName, int count) {
        String registryName = LEGACY_TRADE_ITEM_IDS.getOrDefault(itemName, itemName);
        if ("grimm_bucket".equals(registryName)) {
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // Grimm fluid buckets are registered outside SIMPLE_ITEMS because they bind to the modern fluid registry.
            return new ItemStack(RWBYMItems.GRIMM_BUCKET.get(), count);
        }
        RegistryObject<Item> item = RWBYMItems.SIMPLE_ITEMS.get(registryName);
        if (item == null) {
            item = RWBYMItems.BLOCK_ITEMS.get(registryName);
        }
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get(), count);
    }

    private String merchantKind() {
        if (this.cachedKind == null) {
            this.cachedKind = EntityType.getKey(this.getType()).getPath();
        }
        return this.cachedKind;
    }
}
