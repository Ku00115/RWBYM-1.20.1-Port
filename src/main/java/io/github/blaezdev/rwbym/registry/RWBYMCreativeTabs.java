package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.item.BasicGunItem;
import io.github.blaezdev.rwbym.item.BasicWeaponItem;
import io.github.blaezdev.rwbym.item.RWBYMArmorItem;
import io.github.blaezdev.rwbym.item.RWBYMFishingWeaponItem;
import io.github.blaezdev.rwbym.item.RWBYMGliderItem;
import io.github.blaezdev.rwbym.item.RWBYMLimbItem;
import io.github.blaezdev.rwbym.item.RWBYMWearableItem;
import io.github.blaezdev.rwbym.item.RWBYMWeaponItem;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMCreativeTabs {
    private static final String[] ITEM_ORDER = {
            "gravitydustcrystalcut",
            "winddustcrystalcut",
            "waterdustcrystalcut",
            "firedustcrystalcut",
            "dustcrystalcut",
            "lightdustcrystalcut",
            "icedustcrystalcut",
            // Legacy item tab placed raw crystals near the processed crystal cuts instead of appending them later.
            "dustcrystalhardlight",
            "dustcrystal",
            "winddustcrystal",
            "firedustcrystal",
            "gravitydustcrystal",
            "waterdustcrystal",
            "lightdustcrystal",
            "icedustcrystal",
            "ammo",
            "gamammo",
            "magngrenade",
            "emshell",
            "emflareshell",
            "emfireshell",
            "dustrock",
            "dustrockhardlight",
            "dust",
            "winddustrock",
            "firedustrock",
            "gravitydustrock",
            "waterdustrock",
            "lightdustrock",
            "icedustrock",
            "roseiron",
            "gildediron",
            "frostediron",
            "shadowiron",
            "viridianiron",
            "forestiron",
            "cr1",
            "cr2",
            "cr3",
            "cr4",
            "cr5",
            "cr6",
            "mytre1",
            "mytre2",
            "mytre3",
            "gam1",
            "gam2",
            "gam3",
            "em1",
            "em2",
            "em3",
            "cro1",
            "cro2",
            "cro3",
            "magn1",
            "magn2",
            "magn3",
            "magn4",
            "milo1",
            "milo2",
            "milo3",
            "stor1",
            "stor2",
            "stor3",
            "stor4",
            "stor5",
            "stor6",
            "he1",
            "he2",
            "he3",
            "he4",
            "he5",
            "he6",
            "taintedartefact",
            "scrap",
            "atlasknight",
            "remnants",
            "armagigas",
            "zwei",
            "coinfall",
            "coinr",
            "coinw",
            "coinb",
            "coiny",
            "coinjaune",
            "coinjuane",
            "coinnora",
            "coin_ren",
            "coin_lysette",
            "coinqrow",
            "coin_raven",
            "coinraven",
            "coin_ragora",
            "coin_clover",
            "coin_harriet",
            "coin_pyrrha",
            "coin_valour",
            "coin_penny",
            "chisel",
            "crush",
            "lien1",
            "lien5",
            "lien10",
            "lien20",
            "lien50",
            "lien100",
            "lien500",
            "scroll",
            "scroll2",
            "7scroll_white",
            "7scroll_black",
            "7scroll_blue",
            "7scroll_green",
            "7scroll_pink",
            "7scroll_red",
            "7scroll_yellow",
            "hchoc",
            "coffee",
            "sunrise",
            "plg",
            "torchquick",
            "qrowflask",
            "sake",
            "coconutmilk",
            "pancakes",
            "bourbon",
            "brandy",
            "vodka",
            "wine",
            "fishramen",
            "ramen",
            "peach",
            "gravityore",
            "fireore",
            "windore",
            "impureore",
            "waterore",
            "lightore",
            "toolkit",
            "iceore",
            "bait",
            "hrdltfence",
            "lantern",
            "smrgrave",
            "fireblock",
            "gravityblock",
            "iceblock",
            "impureblock",
            "lightblock",
            "waterblock",
            "windblock",
            "forestironblock",
            "frostedironblock",
            "gildedironblock",
            "roseironblock",
            "shadowironblock",
            "viridianironblock",
            "crusher"
    };

    private static final String[] WEAPON_ORDER = {
            "crescent",
            "hadesgunrecoil",
            "grimmscy",
            "grimmrapier",
            "grimmwhip",
            "sunderaxe",
            "gambol",
            "rvnswd",
            "qrowsword",
            "juane",
            "norahammer",
            "pyrrhaspear",
            "velvet",
            "oobleckthermos",
            "yatsuhashi",
            "cocobag",
            "fox",
            "winterswd",
            "pennyswd",
            "ozpincanetravel",
            "port",
            "torchwick",
            "neoumb_closed",
            "jnrbat",
            "timberhammer",
            "adamswd",
            "cinder",
            "ozmacane",
            "mariacane",
            "angelcane",
            "chatareus",
            "lark",
            "razorbolt",
            "lucidrosescythe",
            "bolin",
            "dew",
            "octavia",
            "lysettesword",
            "bonesword",
            "weiss",
            "flynt",
            "ember",
            "stormflower",
            "infinity",
            "gwai1",
            "emeraldgun",
            "extasis",
            "robyncrossbow",
            "wattshield",
            "neptunegun",
            "sage",
            "arslan",
            "scarletsword",
            "nadirgun",
            "magnumgun",
            "ironwood",
            "ironwood2",
            "goodwitch",
            "cardin",
            "nebulasword",
            "lieutenant",
            "henchmenaxe",
            "fetch",
            "atlaspistol",
            "atlasrifle",
            "vernal",
            "dove",
            "tyrian",
            "russelnormal",
            "vidian",
            "cinderglass",
            "henchmen",
            "fennec",
            "corsac",
            "aquaealatlsword",
            "brawnz",
            "mayrifle",
            "whitefangspear",
            "scarletstormgun",
            "iliasword",
            "lionheart",
            "nolan",
            "royg",
            "wfp",
            "gwenknife",
            "deemace",
            "noctustraumnormal",
            "neonnormal",
            "tocksword",
            "lichtroze_closedice",
            "kyoshifire",
            "whitefangsword",
            "whitefangrifle",
            "amberstafffire",
            "amesardent",
            "onoyari",
            "pugzsword",
            "whisperingblossom",
            "cassandra",
            "freyr",
            "nornir",
            "hollowtome",
            "thorn",
            "carminesai",
            "moonskimmer",
            "pennygun"
    };

    private static final String[] ARMOR_ORDER = {
            "qrow_chest",
            "qrow_legs",
            "juane1_chest",
            "juane1_legs",
            "weiss1_chest",
            "weiss1_legs",
            "adam_chest",
            "adam_legs",
            "atlas_chest",
            "atlas_legs",
            "atlas_head",
            "atlasyellow_chest",
            "atlasyellow_legs",
            "atlasyellow_head",
            "atlasred_chest",
            "atlasred_legs",
            "atlasred_head",
            "atlasgreen_chest",
            "atlasgreen_legs",
            "atlasgreen_head",
            "beacon_chest",
            "beacon_legs",
            "beacon1_chest",
            "beacon1_legs",
            "carmine_chest",
            "carmine_legs",
            "carmine_head",
            "blake1_chest",
            "blake1_legs",
            "blake2_chest",
            "blake2_legs",
            "blake3_chest",
            "blake3_legs",
            "amber_chest",
            "amber_legs",
            "cinder1_chest",
            "cinder1_legs",
            "cinder2_chest",
            "cinder2_legs",
            "cinder3_chest",
            "cinder3_legs",
            "coco_chest",
            "coco_legs",
            "coco_head",
            "roman_chest",
            "roman_legs",
            "roman_head",
            "emerald1_chest",
            "emerald1_legs",
            "emerald2_chest",
            "emerald2_legs",
            "penny_chest",
            "penny_legs",
            "pyrrha_chest",
            "pyrrha_legs",
            "raven_chest",
            "raven_legs",
            "ruby1_chest",
            "ruby1_legs",
            "ruby2_chest",
            "ruby2_legs",
            "ruby3_chest",
            "ruby3_legs",
            "salem_chest",
            "salem_legs",
            "velvet_chest",
            "velvet_legs",
            "weiss2_chest",
            "weiss2_legs",
            "weiss3_chest",
            "weiss3_legs",
            "winter_chest",
            "winter_legs",
            "yang1_chest",
            "yang1_legs",
            "nora1_chest",
            "nora1_legs",
            "yang2_chest",
            "yang2_legs",
            "yang3_chest",
            "yang3_legs",
            "yang4_chest",
            "yang4_legs",
            "ironwood1_chest",
            "ironwood1_legs",
            "ironwood2_chest",
            "ironwood2_legs",
            "mercury1_chest",
            "mercury1_legs",
            "mercury2_chest",
            "mercury2_legs",
            "ozpin_chest",
            "ozpin_legs",
            "summer1_chest",
            "summer1_legs",
            "summer2_chest",
            "summer2_legs",
            "neptune_chest",
            "neptune_legs",
            "neptune_head",
            "scarlet_chest",
            "scarlet_legs",
            "sun_chest",
            "sun_legs",
            "sage_chest",
            "sage_legs",
            "taylor_chest",
            "taylor_legs",
            "taylorhood",
            "bailey_chest",
            "bailey_legs",
            "sasha_chest",
            "sasha_legs",
            "dianna_chest",
            "dianna_legs",
            "maria_chest",
            "maria_legs",
            "henchman_chest",
            "henchman_legs",
            "rvnmask",
            "mariaeyes",
            "mariamask",
            "ozpinglasses",
            "henchmenhat",
            "henchmenhatglasses",
            "whtefng",
            "rubyhood",
            "summerhood",
            "adamv6_chest",
            "adamv6_legs",
            "neo_chest",
            "neo_legs",
            "oscarv4_chest",
            "oscarv4_legs",
            "oscarv6_chest",
            "oscarv6_legs",
            "ozma1_chest",
            "ozma1_legs",
            "ozma2_chest",
            "ozma2_legs",
            "ozma3_chest",
            "ozma3_legs",
            "pennyv7_chest",
            "pennyv7_legs",
            "pennyv7_head",
            "rubyv7_chest",
            "rubyv7_legs",
            "weissv7_chest",
            "weissv7_legs",
            "yangv7_chest",
            "yangv7_legs",
            "blakev7_chest",
            "blakev7_legs",
            "rimuru_chest",
            "rimuru_legs",
            "antimagic_mask",
            "ragora_head",
            "ragora_chest",
            "ragora_legs"
    };

    private static final String[] LIMB_ORDER = {
            "rgrimmarm",
            "clearrightarm",
            "lgrimmarm",
            "clearleftarm",
            "rgrimmleg",
            "clearrightleg",
            "lgrimmleg",
            "clearleftleg",
            "grimmhead",
            "clearhead",
            "clearbody",
            "grimmbody",
            "cattail",
            "cattailblonde",
            "cattailbrown",
            "cattailgrey",
            "cattailorange",
            "dorsalfinblack",
            "dorsalfingrey",
            "dorsalfinpearl",
            "dorsalfinsilver",
            "rabbittailblack",
            "rabbittailblonde",
            "rabbittailbrown",
            "rabbittailgrey",
            "rabbittailorange",
            "rabbittailwhite",
            "cleartail",
            "kag",
            "kag2",
            "blackdragontail",
            "blackfoxtail",
            "blackwolftail",
            "blondefoxtail",
            "blondewolftail",
            "bluedragontail",
            "brownfoxtail",
            "brownwolftail",
            "golddragontail",
            "grayfoxtail",
            "graywolftail",
            "greendragontail",
            "orangefoxtail",
            "orangewolftail",
            "reddragontail",
            "silverdragontail",
            "whitedragontail",
            "whitefoxtail",
            "whitewolftail",
            "whitefoxear",
            "antlerlarge",
            "antlersmall",
            "blackbullhorns",
            "blackwolfears",
            "blondewolfears",
            "bonebullhorns",
            "brownwolfears",
            "graywolfears",
            "orangewolfears",
            "ramhorns",
            "whitewolfears",
            "rabbitearsblack",
            "rabbitearsblonde",
            "rabbitearsbrown",
            "rabbitearsgrey",
            "rabbitearsorange",
            "rabbitearswhite",
            "grimmhorn",
            "clearears",
            "blackcatear",
            "blondecatear",
            "browncatear",
            "greycatear",
            "orangecatear",
            "blackfoxear",
            "blondefoxear",
            "brownfoxear",
            "greyfoxear",
            "orangefoxear"
    };

    private static final String[] CHARM_ORDER = {
            "kingsgambit",
            "kingsgambitpawn",
            "firedancercharm",
            "criticalcharm",
            "auracharm",
            "healthcharm",
            "reachcharm",
            "puncturecharm",
            "edgecharm",
            "knockoutcharm",
            "tankcharm",
            "attackcharm",
            "feathercharm",
            "fleetingcharm",
            "rushcharm",
            "fairyking",
            "relicofknowledge"
    };

    private static final Set<String> ORIGINAL_HIDDEN_FROM_CREATIVE = Set.of(
            "adamgun",
            "amberstaffwind",
            "amesardentgun",
            "ammmo",
            "ammov",
            "angelsword",
            "aquaealatlbow",
            "armasword",
            "armaswordsummon",
            "arslanammo",
            "bangle",
            "bolinblade",
            "carminesaiammo",
            "carminestaff",
            "carminestaffammo",
            "cassandragun",
            "charm",
            "chastifol",
            "chastifolammo",
            "chastifolincrease",
            "chastifolincreaseammo",
            "chatareusgun",
            "cinderbow",
            "cinderbowglass",
            "cocobagv",
            "cocogun",
            "cocogunv",
            "corsacdouble",
            "crescentfrost",
            "crescentgun",
            "crescentgunfrost",
            "crescentgunv",
            "crescentv",
            "darkrepulser",
            "dustcut",
            "elucidator",
            "ember2",
            "emberv",
            "emeraldblade",
            "entitybolt",
            "entityboltfire",
            "entityboltgrav",
            "entityboltice",
            "entityboltlight",
            "entityboltwind",
            "entitybullet",
            "entitybulletv",
            "entityextasisammo",
            "entityfireshell",
            "entityflareshell",
            "entitygrenade",
            "entityrocket",
            "entityshell",
            "entitysmallbullet",
            "entitythundergod",
            "entityweissfire",
            "entityweissgravity",
            "entityweissice",
            "entityweisslight",
            "entityweisswater",
            "entityweisswind",
            "extasislance",
            "fennecdouble",
            "fetchammo",
            "fetchboomerang",
            "firedustcut",
            "flashaceballot",
            "flyingthundergod",
            "gambolgun",
            "gambolgunv",
            "gambolv",
            "gravitydustcut",
            "gwai2",
            "gwai3",
            "gwai4",
            "gwai5",
            "gwen",
            "hadesgun",
            "hadesgunrecoil",
            "hadesscy",
            "hardlightmagazines",
            "hbangle",
            "heroshield",
            "hexenaxe",
            "hollowtomefire",
            "hollowtomegravity",
            "hollowtomeice",
            "hollowtomelightning",
            "hollowtomewater",
            "hollowtomewind",
            "hsanrei",
            "icedustcut",
            "icon",
            "jnrammo",
            "jnrrocket",
            "juaneshieldaxe",
            "juanev",
            "kkfire",
            "kkice",
            "kkwind",
            "kyoshigrav",
            "kyoshiice",
            "kyoshiwind",
            "leafshield",
            "letztammo",
            "letztstil",
            "lichtroze_closedfire",
            "lichtroze_closedwind",
            "lightdustcut",
            "lucidroserifle",
            "magnumsword",
            "mariascythe",
            "mariascythedouble",
            "mayaxe",
            "mondragon",
            "nadirsword",
            "nebulabow",
            "neonfire",
            "neonice",
            "neonwind",
            "neoumb_closed_blade",
            "neoumb_handle_blade",
            "neptunespear",
            "neptunetrident",
            "nevermorefeather",
            "noctu",
            "noctufire",
            "noctugrav",
            "noctuice",
            "noctulight",
            "noctustraumfire",
            "noctustraumfirescy",
            "noctustraumgrav",
            "noctustraumgravscy",
            "noctustraumice",
            "noctustraumicescy",
            "noctustraumlight",
            "noctustraumlightscy",
            "noctustraumnormalscy",
            "noragun",
            "noragunv",
            "norahammerv",
            "oobleckflamethrower",
            "ozmacanefire",
            "ozmacanegravity",
            "ozmacaneice",
            "ozmacanelightning",
            "ozmacanewater",
            "ozmacanewind",
            "ozpincane",
            "pennyswdammo",
            "pickaxeshield",
            "portgun",
            "pugzbow",
            "pyrrharifle",
            "pyrrhaspearammo",
            "pyrrhaspearv",
            "pyrrhaspearvammo",
            "pyrrhasword",
            "pyrrhaswordv",
            "qrow",
            "qrowgun",
            "rageshield",
            "ragorafireball",
            "razorboltknife",
            "reesegun",
            "robynshield",
            "russelfire",
            "russelice",
            "russelwind",
            "rvnswdele",
            "rvnswdice",
            "rzrbolt",
            "sanrei",
            "sanreiammo",
            "saw",
            "sawblade",
            "scarletstormaxe",
            "signcrow",
            "signdust",
            "stormflowerv",
            "sunderrifle",
            "sunstaff",
            "thornammo",
            "thundergodammo",
            "timber",
            "torchwickgun",
            "vidiangun",
            "vidianhammer",
            "waterdustcut",
            "weissv",
            "whisperammo",
            "whisperingblossomammo",
            "winddustcut");


    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RWBYM.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MATERIALS = CREATIVE_MODE_TABS.register("items", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwby_items"))
                    .icon(() -> stack("crmag"))
                    .displayItems((parameters, output) -> {
                        Set<Item> accepted = new HashSet<>();
                        acceptOrdered(output, accepted, ITEM_ORDER);
                        acceptSpawnEggs(output, accepted);
                        acceptUnlisted(output, accepted, RWBYMCreativeTabs::isMaterialTabItem);
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> WEAPONS = CREATIVE_MODE_TABS.register("weapons", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwby_weapons"))
                    .icon(() -> stack("icon"))
                    .displayItems((parameters, output) -> {
                        Set<Item> accepted = new HashSet<>();
                        acceptOrdered(output, accepted, WEAPON_ORDER);
                        acceptUnlisted(output, accepted, RWBYMCreativeTabs::isWeaponTabItem);
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> ARMOR = CREATIVE_MODE_TABS.register("armor", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwby_armour"))
                    .icon(() -> stack("ruby3_chest"))
                    .displayItems((parameters, output) -> {
                        Set<Item> accepted = new HashSet<>();
                        acceptOrdered(output, accepted, ARMOR_ORDER);
                        acceptUnlisted(output, accepted, RWBYMCreativeTabs::isArmorTabItem);
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> COSMETICS = CREATIVE_MODE_TABS.register("limbs", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwby_limbs"))
                    .icon(() -> stack("blackcatear"))
                    .displayItems((parameters, output) -> {
                        Set<Item> accepted = new HashSet<>();
                        acceptOrdered(output, accepted, LIMB_ORDER);
                        acceptUnlisted(output, accepted, item -> item instanceof RWBYMLimbItem);
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> CHARMS = CREATIVE_MODE_TABS.register("charms", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwby_charms"))
                    .icon(() -> stack("kingsgambit"))
                    .displayItems((parameters, output) -> {
                        Set<Item> accepted = new HashSet<>();
                        acceptOrdered(output, accepted, CHARM_ORDER);
                        acceptUnlisted(output, accepted, RWBYMCreativeTabs::isCharmTabItem);
                    })
                    .build());

    private static void acceptOrdered(CreativeModeTab.Output output, Set<Item> accepted, String[] names) {
        for (String name : names) {
            if (isOriginalHiddenFromCreative(name)) {
                continue;
            }
            RegistryObject<Item> item = RWBYMItems.SIMPLE_ITEMS.get(name);
            if (item == null) {
                item = RWBYMItems.BLOCK_ITEMS.get(name);
            }
            if (item != null && item.isPresent() && accepted.add(item.get())) {
                output.accept(item.get());
            }
        }
    }

    private static void acceptSpawnEggs(CreativeModeTab.Output output, Set<Item> accepted) {
        for (var entry : RWBYMItems.SIMPLE_ITEMS.entrySet()) {
            if (entry.getKey().endsWith("_spawn_egg") && entry.getValue().isPresent()
                    && accepted.add(entry.getValue().get())) {
                // Spawn eggs are registered after legacy item ordering, so append them explicitly to the items tab.
                output.accept(entry.getValue().get());
            }
        }
    }

    private static void acceptUnlisted(CreativeModeTab.Output output, Set<Item> accepted,
            java.util.function.Predicate<Item> predicate) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Legacy registration exposed broad item groups; append matching migrated items that are not in hand-written order lists.
        for (var entry : RWBYMItems.SIMPLE_ITEMS.entrySet()) {
            if (!isOriginalHiddenFromCreative(entry.getKey()) && entry.getValue().isPresent()) {
                Item item = entry.getValue().get();
                if (predicate.test(item) && accepted.add(item)) {
                    output.accept(item);
                }
            }
        }
        for (var entry : RWBYMItems.BLOCK_ITEMS.entrySet()) {
            if (entry.getValue().isPresent()) {
                Item item = entry.getValue().get();
                if (predicate.test(item) && accepted.add(item)) {
                    output.accept(item);
                }
            }
        }
        if (RWBYMItems.GRIMM_BUCKET.isPresent()) {
            Item item = RWBYMItems.GRIMM_BUCKET.get();
            if (predicate.test(item) && accepted.add(item)) {
                output.accept(item);
            }
        }
    }

    private static boolean isWeaponTabItem(Item item) {
        return item instanceof RWBYMWeaponItem
                || item instanceof BasicWeaponItem
                || item instanceof BasicGunItem
                || item instanceof RWBYMFishingWeaponItem
                || item instanceof RWBYMGliderItem;
    }

    private static boolean isArmorTabItem(Item item) {
        return item instanceof ArmorItem
                || item instanceof RWBYMArmorItem
                || item instanceof RWBYMWearableItem wearable && wearable.getEquipmentSlot() != net.minecraft.world.entity.EquipmentSlot.FEET;
    }

    private static boolean isCharmTabItem(Item item) {
        return item instanceof RWBYMWearableItem wearable
                && wearable.getEquipmentSlot() == net.minecraft.world.entity.EquipmentSlot.FEET;
    }

    private static boolean isMaterialTabItem(Item item) {
        return !(isWeaponTabItem(item) || isArmorTabItem(item) || isCharmTabItem(item)
                || item instanceof RWBYMLimbItem || isInternalDisplayItem(item));
    }

    private static boolean isInternalDisplayItem(Item item) {
        // Original Ragora fireball and entity* ids are projectile display items, not player-facing materials.
        net.minecraft.resources.ResourceLocation id = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
        return id != null && RWBYM.MOD_ID.equals(id.getNamespace())
                && (id.getPath().startsWith("entity") || id.getPath().equals("ragorafireball"));
    }

    private static ItemStack stack(String name) {
        RegistryObject<Item> item = RWBYMItems.SIMPLE_ITEMS.get(name);
        if (item == null) {
            item = RWBYMItems.BLOCK_ITEMS.get(name);
        }
        return item != null && item.isPresent() ? new ItemStack(item.get()) : ItemStack.EMPTY;
    }

    private static boolean isOriginalHiddenFromCreative(String name) {
        // Original ids constructed with a null CreativeTabs parameter are internal morph, ammo, or debug helpers.
        return ORIGINAL_HIDDEN_FROM_CREATIVE.contains(name);
    }

    private RWBYMCreativeTabs() {
    }
}
