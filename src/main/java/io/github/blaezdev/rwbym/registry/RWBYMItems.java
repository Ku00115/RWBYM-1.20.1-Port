package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.item.BasicCharmItem;
import io.github.blaezdev.rwbym.item.BasicFoodItem;
import io.github.blaezdev.rwbym.item.BasicGunItem;
import io.github.blaezdev.rwbym.item.BasicWeaponItem;
import io.github.blaezdev.rwbym.item.RWBYMArmorItem;
import io.github.blaezdev.rwbym.item.RWBYMArmorMaterials;
import io.github.blaezdev.rwbym.item.RWBYMLimbItem;
import io.github.blaezdev.rwbym.item.RWBYMWearableItem;
import io.github.blaezdev.rwbym.item.RWBYMWeaponItem;
import io.github.blaezdev.rwbym.item.RWBYMWeaponProfiles;
import io.github.blaezdev.rwbym.item.SemblanceCoinItem;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RWBYM.MOD_ID);
    public static final Map<String, RegistryObject<Item>> SIMPLE_ITEMS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> BLOCK_ITEMS = new LinkedHashMap<>();

    private static final String[] SIMPLE_ITEM_NAMES = {
            "50bmg",
            "7scroll_black",
            "7scroll_blue",
            "7scroll_green",
            "7scroll_pink",
            "7scroll_red",
            "7scroll_white",
            "7scroll_yellow",
            "adam_chest",
            "adam_legs",
            "adamgun",
            "adamswd",
            "adamv6_chest",
            "adamv6_legs",
            "amber_chest",
            "amber_legs",
            "amberstafffire",
            "amberstaffwind",
            "amesardent",
            "amesardentgun",
            "ammmo",
            "ammo",
            "ammonv",
            "ammov",
            "angelcane",
            "angelscab",
            "angelsword",
            "antimagic_mask",
            "antlerlarge",
            "antlersmall",
            "aquaealatlbow",
            "aquaealatlbow1",
            "aquaealatlbow125",
            "aquaealatlbow150",
            "aquaealatlbow175",
            "aquaealatlsword",
            "armagigas",
            "armasword",
            "armaswordsummon",
            "arslan",
            "atlas_chest",
            "atlas_head",
            "atlas_legs",
            "atlasgreen_chest",
            "atlasgreen_head",
            "atlasgreen_legs",
            "atlasknight",
            "atlaspistol",
            "atlasred_chest",
            "atlasred_head",
            "atlasred_legs",
            "atlasrifle",
            "atlasyellow_chest",
            "atlasyellow_head",
            "atlasyellow_legs",
            "attackcharm",
            "auracharm",
            "bailey_chest",
            "bailey_legs",
            "bangle",
            "beacon_chest",
            "beacon_legs",
            "beacon1_chest",
            "beacon1_legs",
            "blackbullhorns",
            "blackcatear",
            "blackdragontail",
            "blackfoxear",
            "blackfoxtail",
            "blackwolfears",
            "blackwolftail",
            "blake1_chest",
            "blake1_legs",
            "blake2_chest",
            "blake2_legs",
            "blake3_chest",
            "blake3_legs",
            "blakev7_chest",
            "blakev7_legs",
            "blondecatear",
            "blondefoxear",
            "blondefoxtail",
            "blondewolfears",
            "blondewolftail",
            "bluedragontail",
            "bolin",
            "bolinblade",
            "bolt",
            "boltfire",
            "boltgrav",
            "boltice",
            "boltlight",
            "boltwind",
            "bonebullhorns",
            "bonesword",
            "bourbon",
            "brandy",
            "brawnz",
            "browncatear",
            "brownfoxear",
            "brownfoxtail",
            "brownwolfears",
            "brownwolftail",
            "cardin",
            "carmine_chest",
            "carmine_head",
            "carmine_legs",
            "carminesai",
            "carminestaff",
            "cassandra",
            "cassandragun",
            "cattail",
            "cattailblonde",
            "cattailbrown",
            "cattailgray",
            "cattailgrey",
            "cattailorange",
            "charm",
            "chastifol",
            "chastifolincrease",
            "chatareus",
            "chatareusgun",
            "chatareusgun1",
            "chatareusgun125",
            "chatareusgun150",
            "chatareusgun175",
            "chatelectricmag",
            "chatfiremag",
            "chatgravmag",
            "chatmag",
            "chisel",
            "chissel2",
            "cinder",
            "cinder1_chest",
            "cinder1_legs",
            "cinder2_chest",
            "cinder2_legs",
            "cinder3_chest",
            "cinder3_legs",
            "cinderbow",
            "cinderbow1",
            "cinderbow125",
            "cinderbow150",
            "cinderbow175",
            "cinderbowglass",
            "cinderbowglass1",
            "cinderbowglass125",
            "cinderbowglass150",
            "cinderbowglass175",
            "cinderglass",
            "clearbody",
            "clearears",
            "clearhead",
            "clearleftarm",
            "clearleftleg",
            "clearrightarm",
            "clearrightleg",
            "cleartail",
            "coco_chest",
            "coco_head",
            "coco_legs",
            "cocobag",
            "cocobagv",
            "cocogun",
            "cocogunv",
            "coconutmilk",
            "coffee",
            "coin_clover",
            "coin_harriet",
            "coin_lysette",
            "coin_penny",
            "coin_pyrrha",
            "coin_ragora",
            "coin_ren",
            "coin_valour",
            "coinb",
            "coinjaune",
            "coinjuane",
            "coinnora",
            "coinqrow",
            "coinr",
            "coinraven",
            "coinw",
            "coiny",
            "container",
            "corsac",
            "corsacdouble",
            "cr1",
            "cr2",
            "cr3",
            "cr4",
            "cr5",
            "cr6",
            "crelectricmag",
            "crescent",
            "crescentfrost",
            "crescentgun",
            "crescentgunfrost",
            "crescentgunv",
            "crescentscy",
            "crescentscyv",
            "crescentv",
            "crfiremag",
            "crgravmag",
            "criticalcharm",
            "crmag",
            "cro1",
            "cro2",
            "cro3",
            "croceashld",
            "croceaswd",
            "daedalus",
            "dao",
            "darkrepulser",
            "deemace",
            "dew",
            "dianna_chest",
            "dianna_legs",
            "dorsalfinblack",
            "dorsalfinbrown",
            "dorsalfingrey",
            "dorsalfinpearl",
            "dorsalfinsilver",
            "dove",
            "dust",
            "dustcrystal",
            "dustcrystalcut",
            "dustcrystalhardlight",
            "dustcutgem",
            "dustpouch",
            "dustrock",
            "dustrockhardlight",
            "edgecharm",
            "elucidator",
            "em1",
            "em2",
            "em3",
            "emammo",
            "ember",
            "ember2",
            "ember2oh",
            "embermh",
            "embermhv",
            "emberoh",
            "emberohv",
            "emberv",
            "emerald1_chest",
            "emerald1_legs",
            "emerald2_chest",
            "emerald2_legs",
            "emeraldblade",
            "emeraldgun",
            "emfireammo",
            "emfireshell",
            "emflareammo",
            "emflareshell",
            "emmag",
            "emshell",
            "entitybolt",
            "entityboltfire",
            "entityboltgrav",
            "entityboltice",
            "entityboltlight",
            "entityboltwind",
            "entitybullet",
            "entitybulletv",
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
            "extasis",
            "extasisammo",
            "extasislance",
            "extasisload",
            "fairyking",
            "feathercharm",
            "fennec",
            "fennecdouble",
            "fetch",
            "fetchboomerang",
            "firedancercharm",
            "firedust",
            "firedustcrystal",
            "firedustcrystalcut",
            "firedustrock",
            "fishramen",
            "flashaceballot",
            "fleetingcharm",
            "flyingthundergod",
            "flynt",
            "forestiron",
            "fox",
            "freyr",
            "frostediron",
            "gam1",
            "gam2",
            "gam3",
            "gamammo",
            "gambol",
            "gambolgun",
            "gambolgunv",
            "gambolsheath",
            "gambolsheathv",
            "gambolswd",
            "gambolswdv",
            "gambolv",
            "gamfiremag",
            "gamgravmag",
            "gamicemag",
            "gammag",
            "gemini",
            "gildediron",
            "glider",
            "glider1",
            "gliderdeployed",
            "golddragontail",
            "goodwitch",
            "gravitydust",
            "gravitydustcrystal",
            "gravitydustcrystalcut",
            "gravitydustrock",
            "grayfoxtail",
            "graywolfears",
            "graywolftail",
            "greendragontail",
            "greycatear",
            "greyfoxear",
            "grimmbody",
            "grimmhead",
            "grimmhorn",
            "grimmrapier",
            "grimmscy",
            "grimmwhip",
            "grimmwhipsicle",
            "gwai1",
            "gwai2",
            "gwai3",
            "gwai4",
            "gwai5",
            "gwen",
            "gwenknife",
            "hadesgun",
            "hadesgunrecoil",
            "hadesmag",
            "hadesscy",
            "hardlightmagazines",
            "hbangle",
            "hchoc",
            "he1",
            "he2",
            "he3",
            "he4",
            "he5",
            "he6",
            "healthcharm",
            "hecate_mag",
            "hecate2",
            "henchman_chest",
            "henchman_legs",
            "henchmen",
            "henchmenaxe",
            "henchmenhat",
            "henchmenhatglasses",
            "heroshield",
            "hexen",
            "hexenaxe",
            "historians",
            "hollowtome",
            "hollowtomefire",
            "hollowtomegravity",
            "hollowtomeice",
            "hollowtomelightning",
            "hollowtomewater",
            "hollowtomewind",
            "hsanrei",
            "icedust",
            "icedustcrystal",
            "icedustcrystalcut",
            "icedustrock",
            "icon",
            "iliasword",
            "infinity",
            "inject",
            "injectg",
            "ironwood",
            "ironwood1_chest",
            "ironwood1_legs",
            "ironwood2",
            "ironwood2_chest",
            "ironwood2_legs",
            "jauneshield_vol7",
            "jnrammo",
            "jnrbat",
            "jnrrocket",
            "juane",
            "juane1_chest",
            "juane1_legs",
            "juane2",
            "juaneshield",
            "juaneshieldaxe",
            "juanev",
            "kag",
            "kag2",
            "kingfisher",
            "kingfishercast",
            "kingsgambit",
            "kingsgambitpawn",
            "kkfire",
            "kkice",
            "kkiceshield",
            "kkwind",
            "knockoutcharm",
            "korekosmoufire",
            "korekosmouice",
            "korekosmouoff",
            "korekosmouwind",
            "kyoshifire",
            "kyoshigrav",
            "kyoshiice",
            "kyoshiwind",
            "lark",
            "leafshield",
            "letztstil",
            "letztstil1",
            "letztstil125",
            "letztstil150",
            "letztstil175",
            "lgrimmarm",
            "lgrimmleg",
            "lichtroze_closed",
            "lichtroze_closedfire",
            "lichtroze_closedice",
            "lichtroze_closedwind",
            "lichtroze_open",
            "lichtroze_openfire",
            "lichtroze_openice",
            "lichtroze_openwind",
            "lien1",
            "lien10",
            "lien100",
            "lien20",
            "lien5",
            "lien50",
            "lien500",
            "lienwallet",
            "lieutenant",
            "lightdust",
            "lightdustcrystal",
            "lightdustcrystalcut",
            "lightdustrock",
            "lionheart",
            "lucidroseboard",
            "lucidroseboardride",
            "lucidroserifle",
            "lucidrosescythe",
            "lysetteshield",
            "lysettesword",
            "magn1",
            "magn2",
            "magn3",
            "magn4",
            "magnammo",
            "magngrenade",
            "magnumgun",
            "magnumsword",
            "maria_chest",
            "maria_legs",
            "mariacane",
            "mariaeyes",
            "mariamask",
            "mariascythe",
            "mariascythedouble",
            "mayaxe",
            "mayrifle",
            "mercury1_chest",
            "mercury1_legs",
            "mercury2_chest",
            "mercury2_legs",
            "milo1",
            "milo2",
            "milo3",
            "model",
            "mondragon",
            "mondragonwclip",
            "moonskimmer",
            "myrteswd",
            "myrteswdv",
            "mytre1",
            "mytre2",
            "mytre3",
            "nadirgun",
            "nadirsword",
            "nebulabow",
            "nebulabow1",
            "nebulabow125",
            "nebulabow150",
            "nebulabow175",
            "nebulasword",
            "neo_chest",
            "neo_legs",
            "neonfire",
            "neonice",
            "neonnormal",
            "neonwind",
            "neoumb_closed",
            "neoumb_closed_blade",
            "neoumb_handle_blade",
            "neoumb_open",
            "neoumb_open_blade",
            "neptammo",
            "neptune_chest",
            "neptune_head",
            "neptune_legs",
            "neptunegun",
            "neptunespear",
            "neptunetrident",
            "nevermorefeather",
            "noctustraumfire",
            "noctustraumfirescy",
            "noctustraumgrav",
            "noctustraumgravscy",
            "noctustraumice",
            "noctustraumicescy",
            "noctustraumlight",
            "noctustraumlightscy",
            "noctustraumnormal",
            "noctustraumnormalscy",
            "nolan",
            "nora1_chest",
            "nora1_legs",
            "noragun",
            "noragunv",
            "norahammer",
            "norahammerv",
            "nornir",
            "octavia",
            "onoyari",
            "oobleckflamethrower",
            "oobleckthermos",
            "orangecatear",
            "orangefoxear",
            "orangefoxtail",
            "orangewolfears",
            "orangewolftail",
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
            "ozmacane",
            "ozmacanefire",
            "ozmacanegravity",
            "ozmacaneice",
            "ozmacanelightning",
            "ozmacanewater",
            "ozmacanewind",
            "ozpin_chest",
            "ozpin_legs",
            "ozpincane",
            "ozpincanetravel",
            "ozpinglasses",
            "p90",
            "p90_mag",
            "p90_original",
            "p90bullet",
            "pancakes",
            "partspouch",
            "peach",
            "penny_chest",
            "penny_legs",
            "pennygun",
            "pennyswd",
            "pennyv7_chest",
            "pennyv7_head",
            "pennyv7_legs",
            "pickaxeshield",
            "plg",
            "port",
            "portgun",
            "pugzbow",
            "pugzbow1",
            "pugzbow125",
            "pugzbow150",
            "pugzbow175",
            "pugzsword",
            "puncturecharm",
            "pyrrha_chest",
            "pyrrha_legs",
            "pyrrharifle",
            "pyrrhashield",
            "pyrrhaspear",
            "pyrrhaspearv",
            "pyrrhasword",
            "pyrrhaswordv",
            "qro1",
            "qro2",
            "qro3",
            "qrow",
            "qrow_chest",
            "qrow_legs",
            "qrowflask",
            "qrowgun",
            "qrowscy",
            "qrowsword",
            "rabbitearsblack",
            "rabbitearsblonde",
            "rabbitearsbrown",
            "rabbitearsgrey",
            "rabbitearsorange",
            "rabbitearswhite",
            "rabbittailblack",
            "rabbittailblonde",
            "rabbittailbrown",
            "rabbittailgrey",
            "rabbittailorange",
            "rabbittailwhite",
            "rageshield",
            "ragora_chest",
            "ragora_head",
            "ragora_legs",
            "ramen",
            "ramhorns",
            "raven_chest",
            "raven_legs",
            "razorbolt",
            "razorboltknife",
            "reachcharm",
            "reddragontail",
            "reese",
            "reeseblock",
            "reesegun",
            "relicofknowledge",
            "remnants",
            "rgrimmarm",
            "rgrimmleg",
            "rimuru_chest",
            "rimuru_legs",
            "robyncrossbow",
            "robynfeather",
            "robynshield",
            "roman_chest",
            "roman_head",
            "roman_legs",
            "roseiron",
            "royg",
            "ruby1_chest",
            "ruby1_legs",
            "ruby2_chest",
            "ruby2_head",
            "ruby2_legs",
            "ruby3_chest",
            "ruby3_legs",
            "rubyhood",
            "rubyv7_chest",
            "rubyv7_legs",
            "rushcharm",
            "russelfire",
            "russelice",
            "russelnormal",
            "russelwind",
            "rvnmask",
            "rvnsheath",
            "rvnswd",
            "rvnswdele",
            "rvnswdice",
            "rzrbolt",
            "sage",
            "sage_chest",
            "sage_legs",
            "sake",
            "salem_chest",
            "salem_legs",
            "sanrei",
            "sanrei1",
            "sanrei125",
            "sanrei150",
            "sanrei175",
            "sasha_chest",
            "sasha_legs",
            "saw",
            "scarlet_chest",
            "scarlet_legs",
            "scarletgun",
            "scarletstormaxe",
            "scarletstormgun",
            "scarletsword",
            "scrap",
            "scroll",
            "scroll2",
            "shadowiron",
            "signcrow",
            "signdust",
            "silverdragontail",
            "spl",
            "splfire",
            "splgrav",
            "splice",
            "spllight",
            "stor1",
            "stor2",
            "stor3",
            "stor4",
            "stor5",
            "stor6",
            "stormag",
            "stormflower",
            "stormflower_projectile",
            "stormflower_vol7",
            "stormflower_vol7grappel",
            "stormflower_vol7gun",
            "stormflowerv",
            "summer1_chest",
            "summer1_legs",
            "summer2_chest",
            "summer2_head",
            "summer2_legs",
            "summerhood",
            "sun_chest",
            "sun_legs",
            "sunderaxe",
            "sunderrifle",
            "sunnunchuck",
            "sunrise",
            "sunstaff",
            "tankcharm",
            "taylor_chest",
            "taylor_head",
            "taylor_legs",
            "taylorhood",
            "thorn",
            "thornammo",
            "timber",
            "timberhammer",
            "tir",
            "tocksword",
            "tockswordoh",
            "torchquick",
            "torchwick",
            "torchwickgun",
            "tyrian",
            "velvet",
            "velvet_chest",
            "velvet_legs",
            "velvetcam",
            "vernal",
            "vidian",
            "vidiangun",
            "vidianhammer",
            "viridianiron",
            "vodka",
            "waterdust",
            "waterdustcrystal",
            "waterdustcrystalcut",
            "waterdustrock",
            "wattshield",
            "wattsshield",
            "weiss",
            "weiss1_chest",
            "weiss1_legs",
            "weiss2_chest",
            "weiss2_legs",
            "weiss3_chest",
            "weiss3_legs",
            "weissunf",
            "weissv",
            "weissv7_chest",
            "weissv7_legs",
            "weyland",
            "wfp",
            "whisperingblossom",
            "whisperingblossomammo",
            "whitedragontail",
            "whitefangmask",
            "whitefangrifle",
            "whitefangspear",
            "whitefangsword",
            "whitefoxear",
            "whitefoxtail",
            "whitewolfears",
            "whitewolftail",
            "whtefng",
            "winddust",
            "winddustcrystal",
            "winddustcrystalcut",
            "winddustrock",
            "wine",
            "winter_chest",
            "winter_legs",
            "winteroffhand",
            "winterswd",
            "winterswdempty",
            "yang1_chest",
            "yang1_legs",
            "yang2_chest",
            "yang2_legs",
            "yang3_chest",
            "yang3_legs",
            "yang4_chest",
            "yang4_legs",
            "yangv7_chest",
            "yangv7_legs",
            "yatsuhashi",
            "ammmmo",
            "ammmmmmo",
            "arslanammo",
            "chastifolammo",
            "chastifolincreaseammo",
            "emammmo",
            "fetchammo",
            "firedust2",
            "letztammo",
            "magnaampammo",
            "noctu",
            "noctufire",
            "noctugrav",
            "noctuice",
            "noctulight",
            "pennyswdammo",
            "pyrrhaspearammo",
            "pyrrhaspearvammo",
            "sanreiammo",
            "sawblade",
            "thundergodammo",
            "whisperammo",
            "carminesaiammo",
            "carminestaffammo",
            "zwei"
    };

    static {
        for (String name : SIMPLE_ITEM_NAMES) {
            SIMPLE_ITEMS.put(name, ITEMS.register(name, () -> createSimpleItem(name)));
        }
        RWBYMBlocks.BLOCKS_BY_NAME.forEach((name, block) ->
                BLOCK_ITEMS.put(name, ITEMS.register(name, () -> new BlockItem(block.get(), blockItemProperties(name)))));
        SIMPLE_ITEMS.put("beowolf_spawn_egg", ITEMS.register("beowolf_spawn_egg",
                () -> new ForgeSpawnEggItem(RWBYMEntityTypes.BEOWOLF, 0x111111, 0xD8D8D8, new Item.Properties())));
        SIMPLE_ITEMS.put("ursa_spawn_egg", ITEMS.register("ursa_spawn_egg",
                () -> new ForgeSpawnEggItem(RWBYMEntityTypes.URSA, 0x1A1A1A, 0xAFAFAF, new Item.Properties())));
        SIMPLE_ITEMS.put("boarbatusk_spawn_egg", ITEMS.register("boarbatusk_spawn_egg",
                () -> new ForgeSpawnEggItem(RWBYMEntityTypes.BOARBATUSK, 0x202020, 0x9B2D2D, new Item.Properties())));
        SIMPLE_ITEMS.put("creep_spawn_egg", ITEMS.register("creep_spawn_egg",
                () -> new ForgeSpawnEggItem(RWBYMEntityTypes.CREEP, 0x191919, 0x6F6F6F, new Item.Properties())));
        SIMPLE_ITEMS.put("sabyr_spawn_egg", ITEMS.register("sabyr_spawn_egg",
                () -> new ForgeSpawnEggItem(RWBYMEntityTypes.SABYR, 0x151515, 0xB0B0B0, new Item.Properties())));
        SIMPLE_ITEMS.put("beringle_spawn_egg", ITEMS.register("beringle_spawn_egg",
                () -> new ForgeSpawnEggItem(RWBYMEntityTypes.BERINGLE, 0x101010, 0x8A1F1F, new Item.Properties())));
        SIMPLE_ITEMS.put("apathy_spawn_egg", ITEMS.register("apathy_spawn_egg",
                () -> new ForgeSpawnEggItem(RWBYMEntityTypes.APATHY, 0x2D2D2D, 0xCFCFCF, new Item.Properties())));
        SIMPLE_ITEMS.put("deathstalker_spawn_egg", ITEMS.register("deathstalker_spawn_egg",
                () -> new ForgeSpawnEggItem(RWBYMEntityTypes.DEATHSTALKER, 0x090909, 0x6D1414, new Item.Properties())));
        SIMPLE_ITEMS.put("lancer_spawn_egg", ITEMS.register("lancer_spawn_egg",
                () -> new ForgeSpawnEggItem(RWBYMEntityTypes.LANCER, 0x121212, 0xD9D9D9, new Item.Properties())));
    }

    public static final RegistryObject<Item> ICON = SIMPLE_ITEMS.containsKey("icon")
            ? SIMPLE_ITEMS.get("icon")
            : SIMPLE_ITEMS.values().iterator().next();

    private static Item createSimpleItem(String name) {
        ArmorItem.Type armorType = armorTypeFor(name);
        if (armorType != null) {
            return new RWBYMArmorItem(name, RWBYMArmorMaterials.HUNTSMAN, armorType, new Item.Properties());
        }
        Item.Properties properties = propertiesFor(name);
        String limbSlot = limbSlotFor(name);
        if (limbSlot != null) {
            return new RWBYMLimbItem(name, limbSlot, properties);
        }
        if (isHeadWearable(name)) {
            return new RWBYMWearableItem(name, EquipmentSlot.HEAD, properties);
        }
        if (isFoodLike(name)) {
            return new BasicFoodItem(name, properties);
        }
        if (isCharmLike(name)) {
            return new BasicCharmItem(name, properties);
        }
        if (isSemblanceCoin(name)) {
            return new SemblanceCoinItem(name, properties);
        }
        if (RWBYMWeaponProfiles.contains(name)) {
            return new RWBYMWeaponItem(RWBYMWeaponProfiles.get(name), properties, shootSoundFor(name));
        }
        if (isGunLike(name)) {
            return new BasicGunItem(properties, shootSoundFor(name));
        }
        if (isWeaponLike(name)) {
            return new BasicWeaponItem(properties, attackDamageFor(name), attackSpeedFor(name));
        }
        return new Item(properties);
    }

    private static Item.Properties propertiesFor(String name) {
        Item.Properties properties = new Item.Properties();
        if (isSingleStackItem(name)) {
            properties.stacksTo(1);
        } else if (isFoodLike(name)) {
            properties.stacksTo(16);
        } else if (isMagazineLike(name)) {
            properties.stacksTo(16);
        } else if (isAmmoLike(name)) {
            properties.stacksTo(64);
        }
        if (RWBYMWeaponProfiles.contains(name)) {
            properties.durability(RWBYMWeaponProfiles.get(name).durability());
        } else if (name.equals("chisel")) {
            properties.durability(255);
        } else if (isWeaponLike(name)) {
            properties.durability(500);
        }
        return properties;
    }

    private static Item.Properties blockItemProperties(String name) {
        Item.Properties properties = new Item.Properties();
        if (name.equals("crush")) {
            properties.stacksTo(1).durability(255);
        }
        return properties;
    }

    private static ArmorItem.Type armorTypeFor(String name) {
        if (name.equals("korekosmouoff")
                || name.equals("korekosmoufire")
                || name.equals("korekosmouice")
                || name.equals("korekosmouwind")) {
            return ArmorItem.Type.CHESTPLATE;
        }
        if (name.endsWith("_head")) {
            return ArmorItem.Type.HELMET;
        }
        if (name.endsWith("_chest")) {
            return ArmorItem.Type.CHESTPLATE;
        }
        if (name.endsWith("_legs")) {
            return ArmorItem.Type.LEGGINGS;
        }
        if (name.endsWith("_boots")) {
            return ArmorItem.Type.BOOTS;
        }
        return null;
    }

    private static boolean isSingleStackItem(String name) {
        return name.contains("scroll")
                || name.endsWith("charm")
                || name.endsWith("bangle")
                || name.endsWith("camera")
                || name.endsWith("cam")
                || name.equals("chisel")
                || isHeadWearable(name);
    }

    private static boolean isAmmoLike(String name) {
        return name.contains("ammo")
                || name.contains("ammmo")
                || name.contains("bullet")
                || name.contains("shell")
                || name.equals("bolt")
                || name.startsWith("bolt")
                || name.equals("sawblade");
    }

    private static boolean isFoodLike(String name) {
        return name.equals("coffee")
                || name.equals("coconutmilk")
                || name.equals("pancakes")
                || name.equals("fishramen")
                || name.equals("bourbon")
                || name.equals("brandy")
                || name.equals("vodka")
                || name.equals("wine")
                || name.equals("sake")
                || name.equals("torchquick");
    }

    private static boolean isCharmLike(String name) {
        return name.endsWith("charm");
    }

    private static boolean isSemblanceCoin(String name) {
        return name.equals("coinr")
                || name.equals("coinw")
                || name.equals("coinb")
                || name.equals("coiny")
                || name.equals("coinjaune")
                || name.equals("coinjuane")
                || name.equals("coinnora")
                || name.equals("coin_ren")
                || name.equals("coin_lysette")
                || name.equals("coinqrow")
                || name.equals("coinraven")
                || name.equals("coin_ragora")
                || name.equals("coin_clover")
                || name.equals("coin_harriet")
                || name.equals("coin_pyrrha")
                || name.equals("coin_valour")
                || name.equals("coin_penny");
    }

    private static boolean isHeadWearable(String name) {
        return name.endsWith("hood")
                || name.endsWith("mask")
                || name.endsWith("glasses")
                || name.endsWith("hat");
    }

    private static String limbSlotFor(String name) {
        if (name.equals("rgrimmarm") || name.equals("clearrightarm")) {
            return "RightArm";
        }
        if (name.equals("lgrimmarm") || name.equals("clearleftarm")) {
            return "LeftArm";
        }
        if (name.equals("rgrimmleg") || name.equals("clearrightleg")) {
            return "RightLeg";
        }
        if (name.equals("lgrimmleg") || name.equals("clearleftleg")) {
            return "LeftLeg";
        }
        if (name.equals("grimmhead") || name.equals("clearhead")) {
            return "Head";
        }
        if (name.equals("grimmbody") || name.equals("clearbody")) {
            return "Body";
        }
        if (name.equals("clearears")
                || name.endsWith("ear")
                || name.endsWith("ears")
                || name.endsWith("horn")
                || name.endsWith("horns")
                || name.startsWith("antler")) {
            return "Ears";
        }
        if (name.equals("cleartail")
                || name.endsWith("tail")
                || name.contains("tail")
                || name.startsWith("dorsalfin")
                || name.equals("kag")
                || name.equals("kag2")) {
            return "Tail";
        }
        return null;
    }

    private static boolean isMagazineLike(String name) {
        return name.endsWith("mag")
                || name.endsWith("_mag")
                || name.contains("magazine");
    }

    private static boolean isWeaponLike(String name) {
        if (isAmmoLike(name) || name.startsWith("entity")) {
            return false;
        }
        return RWBYMWeaponProfiles.contains(name)
                || isGunLike(name)
                || name.endsWith("swd")
                || name.endsWith("sword")
                || name.endsWith("blade")
                || name.endsWith("bow")
                || name.endsWith("staff")
                || name.endsWith("cane")
                || name.endsWith("scy")
                || name.endsWith("scythe")
                || name.endsWith("spear")
                || name.endsWith("axe")
                || name.endsWith("hammer")
                || name.endsWith("knife")
                || name.endsWith("dagger")
                || name.endsWith("rapier")
                || name.endsWith("shield")
                || name.endsWith("tome")
                || name.endsWith("sai")
                || name.endsWith("mace")
                || name.endsWith("trident")
                || name.endsWith("boomerang")
                || name.endsWith("whip");
    }

    private static boolean isGunLike(String name) {
        return name.endsWith("gun")
                || name.endsWith("rifle")
                || name.endsWith("pistol")
                || name.contains("shotgun");
    }

    private static RegistryObject<SoundEvent> shootSoundFor(String name) {
        if (name.contains("crescent")) {
            return RWBYMSounds.CRESCENT_ROSE_SHOOT;
        }
        if (name.contains("gambol")) {
            return RWBYMSounds.GAMBOL_SHROUD_SHOOT;
        }
        if (name.contains("ember")) {
            return RWBYMSounds.EMBER_CELICA_SHOOT;
        }
        if (name.contains("myrte") || name.contains("weiss")) {
            return RWBYMSounds.MYRTENASTER_SHOOT;
        }
        if (name.contains("stormflower")) {
            return RWBYMSounds.STORMFLOWER_SHOOT;
        }
        if (name.contains("magn") || name.contains("nora")) {
            return RWBYMSounds.MAGNHILD_SHOOT;
        }
        if (name.contains("port")) {
            return RWBYMSounds.PORT_SHOOT;
        }
        if (name.contains("junior")) {
            return RWBYMSounds.JUNIOR_SHOOT;
        }
        if (name.contains("torchwick")) {
            return RWBYMSounds.TORCHWICK_SHOOT;
        }
        return RWBYMSounds.RIFLE_SHOOT;
    }

    private static double attackDamageFor(String name) {
        if (name.endsWith("scy") || name.endsWith("scythe") || name.endsWith("hammer") || name.endsWith("axe")) {
            return 8.0D;
        }
        if (name.endsWith("spear") || name.endsWith("rapier")) {
            return 6.0D;
        }
        if (name.endsWith("knife") || name.endsWith("dagger")) {
            return 4.0D;
        }
        return 5.0D;
    }

    private static double attackSpeedFor(String name) {
        if (name.endsWith("scy") || name.endsWith("scythe") || name.endsWith("hammer") || name.endsWith("axe")) {
            return -3.0D;
        }
        if (name.endsWith("knife") || name.endsWith("dagger")) {
            return -1.6D;
        }
        return -2.4D;
    }

    private RWBYMItems() {
    }
}
