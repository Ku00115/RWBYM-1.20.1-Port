package io.github.blaezdev.rwbym.item;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class RWBYMWeaponProfiles {
    public static final int RAPIER = 0x1;
    public static final int SCYTHE = 0x2;
    public static final int OFFHAND = 0x4;
    public static final int SCARLET = 0x8;
    public static final int JUNIOR = 0x10;
    public static final int EMBER_CELICA = 0x20;
    public static final int WINTER = 0x40;
    public static final int BOW = 0x80;
    public static final int INT_MAG = 0x100;
    public static final int WHIP = 0x200;
    public static final int LION_HEART = 0x400;
    public static final int DAGGER = 0x800;
    public static final int SWORD = 0x1000;
    public static final int SANREI = 0x2000;
    public static final int LETZT = 0x4000;
    public static final int AURAWEAP = 0x8000;
    public static final int TOOL = 0x10000;
    public static final int STAFF = 0x20000;
    public static final int ROCKET = 0x40000;
    public static final int UMBRELLA = 0x80000;
    public static final int AXE = 0x100000;
    public static final int PICKAXE = 0x200000;
    public static final int TOME = 0x400000;
    public static final int FIST = 0x800000;
    public static final int HAMMER = 0x1000000;
    public static final int THROWN = 0x2000000;
    public static final int WALLCLIMB = 0x4000000;
    public static final int FLIGHT = 0x8000000;
    public static final int BOOMERANG = 0x10000000;

    private static final Map<String, WeaponProfile> PROFILES = createProfiles();
    private static final Set<String> CREATIVE_WEAPONS = createCreativeWeapons(PROFILES);

    public static WeaponProfile get(String name) {
        return PROFILES.get(name);
    }

    public static boolean contains(String name) {
        return PROFILES.containsKey(name);
    }

    public static boolean isCreativeWeapon(String name) {
        return CREATIVE_WEAPONS.contains(name);
    }

    private static Map<String, WeaponProfile> createProfiles() {
        Map<String, WeaponProfile> profiles = new LinkedHashMap<>();
        put(profiles, new WeaponProfile("crescent", 2500, 16, 0x2, "rwbym:crescentgun", "rwbym:crmag,rwbym:crfiremag,rwbym:crgravmag,rwbym:crelectricmag", true, 1.00F, false, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("hadesgun", 2500, 14, 0x0, "rwbym:hadesscy", "rwbym:nullest,rwbym:hadesmag", true, 3.00F, false, false, 0, 1, "rwbym:hadesgunrecoil", null, 8));
        put(profiles, new WeaponProfile("hadesgunrecoil", 2500, 14, 0x0, "rwbym:hadesscy", "rwbym:nullest,rwbym:hadesmag", true, 3.00F, false, false, 1, 1, "rwbym:hadesgun", null, 20));
        put(profiles, new WeaponProfile("hadesscy", 2500, 22, 0x2, "rwbym:hadesgunrecoil", "rwbym:nullest", true, 3.00F, false, false, 0, 1, null, null, 15));
        put(profiles, new WeaponProfile("grimmscy", 2500, 16, 0x2, null, "rwbym:nuller,rwbym:nulls", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("grimmrapier", 2500, 14, 0x1, null, "rwbym:nuller,rwbym:nulls", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("grimmwhip", 2500, 14, 0x800, null, "rwbym:nuller", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("crescentfrost", 2500, 16, 0x2, "rwbym:crescentgunfrost", "rwbym:crmag,rwbym:crfiremag,rwbym:crgravmag,rwbym:crelectricmag", true, 1.00F, false, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("sunderaxe", 2500, 16, 0x2, "rwbym:sunderrifle", "rwbym:nuller", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("gambol", 2500, 14, 0x1000, "rwbym:gambolgun", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("rvnswd", 2500, 14, 0x1000, "rwbym:rvnswdele", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, "fire", 0));
        put(profiles, new WeaponProfile("rvnswdele", 2500, 9, 0x0, "rwbym:rvnswdice", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, "wind", 0));
        put(profiles, new WeaponProfile("rvnswdice", 2500, 14, 0x1000, "rwbym:rvnswd", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("qrow", 2500, 18, 0x2, "rwbym:qrowgun", "rwbym:ammmmo,rwbym:ammmmmmo", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("qrowsword", 2500, 16, 0x1000, "rwbym:qrow", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("juane", 2500, 14, 0x1000, "rwbym:juaneshieldaxe", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("juaneshieldaxe", 2500, 16, 0x1000, "rwbym:juane", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("norahammer", 2500, 16, 0x1000000, "rwbym:noragun", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("pyrrhaspear", 2500, 14, 0x2000002, "rwbym:pyrrharifle", "rwbym:pyrrhaspearammo", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("pyrrhasword", 2500, 14, 0x800, "rwbym:pyrrhaspear", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("velvet", 1250, 0, 0x0, "rwbym:crescentv", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("oobleckthermos", 2500, 0, 0x0, "rwbym:oobleckflamethrower", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("crescentv", 1250, 16, 0x102, "rwbym:crescentgunv", "rwbym:ammov", true, 1.00F, false, false, 1, 1, null, null, 5));
        put(profiles, new WeaponProfile("weissv", 1250, 10, 0x1, "rwbym:gambolv", "rwbym:ammmmo", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("gambolv", 1250, 14, 0x1000, "rwbym:gambolgunv", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("juanev", 1250, 14, 0x1000, "rwbym:norahammerv", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("norahammerv", 1250, 16, 0x1000000, "rwbym:noragunv", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("pyrrhaspearv", 1250, 14, 0x2000002, "rwbym:pyrrhaswordv", "rwbym:pyrrhaspearvammo", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("pyrrhaswordv", 1250, 14, 0x800, "rwbym:stormflowerv", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("yatsuhashi", 2500, 19, 0x1000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("cocobag", 1250, 14, 0x0, "rwbym:cocogun", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("fox", 2500, 14, 0x1000000, null, "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", false, 1.00F, false, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("cocobagv", 1250, 14, 0x0, "rwbym:cocogunv", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("winterswd", 2500, 14, 0x40, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("pennyswd", 2500, 13, 0x12001000, "rwbym:pennygun", "rwbym:pennyswdammo", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("ozpincane", 2500, 14, 0x1000, "rwbym:ozpincanetravel", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("ozpincanetravel", 2500, 1, 0x1000, "rwbym:ozpincane", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("port", 2500, 16, 0x0, "rwbym:portgun", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("kkfire", 2500, 19, 0x1000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "fire", 0));
        put(profiles, new WeaponProfile("kkice", 2500, 14, 0x1000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, "ice", 0));
        put(profiles, new WeaponProfile("kkwind", 2500, 14, 0x1, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, "wind", 0));
        put(profiles, new WeaponProfile("torchwick", 2500, 15, 0x1000000, "rwbym:torchwickgun", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 3, null, null, 0));
        put(profiles, new WeaponProfile("neoumb_closed", 2500, 13, 0x80000, "rwbym:neoumb_closed_blade", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("neoumb_closed_blade", 2500, 13, 0x80001, "rwbym:neoumb_handle_blade", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("neoumb_handle_blade", 2500, 14, 0x1, "rwbym:neoumb_closed", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("jnrbat", 2500, 15, 0x1000000, "rwbym:jnrrocket", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("jnrrocket", 2500, 0, 0x10, "rwbym:jnrbat", "rwbym:jnrammo", true, 1.00F, false, false, 0, 4, null, null, 20));
        put(profiles, new WeaponProfile("timberhammer", 5000, 18, 0x1000000, "rwbym:timber", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("timber", 5000, 0, 0x10, "rwbym:timberhammer", "rwbym:jnrammo", true, 1.00F, false, false, 0, 2, null, null, 30));
        put(profiles, new WeaponProfile("adamswd", 2500, 13, 0x1000, "rwbym:adamgun", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("cinder", 2500, 14, 0x1000, "rwbym:cinderbow", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("ozmacane", 2500, 12, 0x1020000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("ozmacanefire", 2500, 12, 0x1020000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "fire", 0));
        put(profiles, new WeaponProfile("ozmacanegravity", 2500, 12, 0x1020000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "grav", 0));
        put(profiles, new WeaponProfile("ozmacaneice", 2500, 12, 0x1020000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "ice", 0));
        put(profiles, new WeaponProfile("ozmacanelightning", 2500, 12, 0x1020000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "light", 0));
        put(profiles, new WeaponProfile("ozmacanewater", 2500, 12, 0x1020000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "water", 0));
        put(profiles, new WeaponProfile("ozmacanewind", 2500, 12, 0x1020000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "wind", 0));
        put(profiles, new WeaponProfile("mariacane", 2500, 14, 0x1000000, "rwbym:mariascythe", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("mariascythe", 2500, 16, 0x4000002, "rwbym:mariascythedouble", "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", true, 1.00F, false, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("angelcane", 2500, 14, 0x0, "rwbym:angelsword", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("angelsword", 2500, 16, 0x1, "rwbym:angelcane", "none", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("mariascythedouble", 2500, 18, 0x2, "rwbym:mariacane", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("chatareus", 2500, 16, 0x0, "rwbym:chatareusgun", "rwbym:chatmag,rwbym:chatfiremag,rwbym:chatgravmag,rwbym:chatelectricmag", true, 1.00F, false, false, 0, 1, null, null, 10));
        put(profiles, new WeaponProfile("chatareusgun", 2500, 16, 0x0, "rwbym:chatareus", "rwbym:chatmag,rwbym:chatfiremag,rwbym:chatgravmag,rwbym:chatelectricmag", true, 3.00F, false, false, 0, 1, null, null, 10));
        put(profiles, new WeaponProfile("lark", 2500, 16, 0x2, null, "rwbym:chatmag,rwbym:chatfiremag,rwbym:chatgravmag,rwbym:chatelectricmag", true, 3.00F, false, false, 0, 1, null, null, 10));
        put(profiles, new WeaponProfile("razorbolt", 2500, 16, 0x801100, "rwbym:razorboltknife", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("razorboltknife", 2500, 8, 0x900, "rwbym:razorbolt", "rwbym:rzrbolt", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("lucidroserifle", 2500, 0, 0x0, "rwbym:lucidrosescythe", "rwbym:crmag,rwbym:crfiremag,rwbym:crgravmag,rwbym:crelectricmag", true, 3.00F, false, false, 0, 1, null, null, 10));
        put(profiles, new WeaponProfile("lucidrosescythe", 2500, 17, 0x2, "rwbym:lucidroseboard", "rwbym:crmag,rwbym:crfiremag,rwbym:crgravmag,rwbym:crelectricmag", true, 1.00F, false, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("lucidroseboard", 2500, 4, 0x0, "rwbym:lucidroserifle", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("reesegun", 2500, 14, 0x0, "rwbym:reese", "rwbym:spl,rwbym:splfire,rwbym:splgrav,rwbym:splice,rwbym:spllight", true, 1.00F, false, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("reese", 2500, 12, 0x0, "rwbym:reesegun", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("bolin", 2500, 16, 0x0, "rwbym:bolinblade", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("bolinblade", 2500, 17, 0x0, "rwbym:bolin", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("dew", 2500, 17, 0x2, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("octavia", 2500, 14, 0x800, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("lysettesword", 2500, 14, 0x1000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("flashaceballot", 2500, 18, 0x1000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "flash", 0));
        put(profiles, new WeaponProfile("bonesword", 2500, 18, 0x1000, null, "rwbym:gravitydust,rwbym:winddust,rwbym:icedust,rwbym:waterdust,rwbym:firedust,rwbym:lightdust,rwbym:gravitydustcrystal,rwbym:winddustcrystal,rwbym:icedustcrystal,rwbym:waterdustcrystal,rwbym:firedustcrystal,rwbym:lightdustcrystal", true, 1.00F, false, true, 0, 1, null, "flash", 0));
        put(profiles, new WeaponProfile("crescentgun", 2500, 0, 0x0, "rwbym:crescent", "rwbym:crmag,rwbym:crfiremag,rwbym:crgravmag,rwbym:crelectricmag", true, 3.00F, false, false, 1, 1, null, null, 10));
        put(profiles, new WeaponProfile("crescentgunfrost", 2500, 0, 0x0, "rwbym:crescentfrost", "rwbym:crmag,rwbym:crfiremag,rwbym:crgravmag,rwbym:crelectricmag", true, 3.00F, false, false, 1, 1, null, null, 10));
        put(profiles, new WeaponProfile("sunderrifle", 2500, 0, 0x0, "rwbym:sunderaxe", "rwbym:crmag,rwbym:crfiremag,rwbym:crgravmag,rwbym:crelectricmag", true, 3.00F, false, false, 1, 1, null, null, 10));
        put(profiles, new WeaponProfile("weiss", 2500, 14, 0x1, null, "rwbym:gravitydust,rwbym:winddust,rwbym:icedust,rwbym:waterdust,rwbym:firedust,rwbym:lightdust,rwbym:gravitydustcrystal,rwbym:winddustcrystal,rwbym:icedustcrystal,rwbym:waterdustcrystal,rwbym:firedustcrystal,rwbym:lightdustcrystal", true, 1.00F, false, false, 3, 1, null, null, 3));
        put(profiles, new WeaponProfile("flynt", 2500, 14, 0x1, null, "rwbym:gravitydust,rwbym:winddust,rwbym:icedust,rwbym:waterdust,rwbym:firedust,rwbym:lightdust,rwbym:gravitydustcrystal,rwbym:winddustcrystal,rwbym:icedustcrystal,rwbym:waterdustcrystal,rwbym:firedustcrystal,rwbym:lightdustcrystal", true, 1.00F, false, false, 0, 3, null, null, 3));
        put(profiles, new WeaponProfile("crescentgunv", 1250, 0, 0x100, "rwbym:emberv", "rwbym:ammov", true, 3.00F, false, false, 1, 1, null, null, 10));
        put(profiles, new WeaponProfile("ember", 2500, 14, 0x800000, "rwbym:ember2", "rwbym:emammo,rwbym:emfireammo,rwbym:emflareammo", true, 0.80F, false, false, 0, 4, null, null, 7));
        put(profiles, new WeaponProfile("ember2", 2500, 14, 0x800020, "rwbym:ember", "rwbym:emammo,rwbym:emfireammo,rwbym:emflareammo", true, 0.80F, false, false, 0, 4, null, null, 7));
        put(profiles, new WeaponProfile("gambolgun", 2500, 0, 0x4, "rwbym:gambol", "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", true, 1.00F, true, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("p90", 2500, 0, 0x100, null, "rwbym:p90_mag,rwbym:p90bullet", false, 2.50F, false, false, 0, 1, null, null, 2));
        put(profiles, new WeaponProfile("hecate2", 2500, 0, 0x100, null, "rwbym:hecate_mag,rwbym:50bmg", true, 4.00F, false, false, 0, 1, null, null, 18));
        put(profiles, new WeaponProfile("gambolgunv", 1250, 0, 0x104, "rwbym:juanev", "rwbym:ammov", true, 1.00F, true, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("stormflower", 2500, 14, 0x4000800, null, "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", false, 1.00F, false, false, 0, 1, null, null, 3));
        put(profiles, new WeaponProfile("noragun", 2500, 0, 0x0, "rwbym:norahammer", "rwbym:magnammo,rwbym:magnaampammo", true, 1.00F, false, false, 0, 1, null, null, 7));
        put(profiles, new WeaponProfile("stormflowerv", 1250, 14, 0x900, "rwbym:cocobagv", "rwbym:ammov", false, 1.00F, false, false, 0, 1, null, null, 3));
        put(profiles, new WeaponProfile("noragunv", 1250, 0, 0x100, "rwbym:pyrrhaspearv", "rwbym:ammov", true, 1.00F, false, false, 0, 1, null, null, 7));
        put(profiles, new WeaponProfile("emberv", 1250, 14, 0x800100, "rwbym:weissv", "rwbym:ammov", true, 1.00F, false, false, 0, 4, null, null, 7));
        put(profiles, new WeaponProfile("qrowgun", 2500, 0, 0x0, "rwbym:qrowsword", "rwbym:emammo,rwbym:emfireammo,rwbym:emflareammo", true, 0.80F, false, false, 0, 4, null, null, 7));
        put(profiles, new WeaponProfile("cocogun", 1250, 0, 0x100, "rwbym:cocobag", "rwbym:ammmo", false, 1.00F, false, false, 0, 1, null, null, 1));
        put(profiles, new WeaponProfile("cocogunv", 1250, 0, 0x100, "rwbym:velvet", "rwbym:ammmo", false, 1.00F, false, false, 0, 1, null, null, 1));
        put(profiles, new WeaponProfile("infinity", 0, 0, 0x100, null, "rwbym:ammmo", false, 1.00F, false, false, 0, 2, null, null, 1));
        put(profiles, new WeaponProfile("sanrei", 0, 0, 0x2000, "rwbym:letztstil", "rwbym:sanreiammo", true, 1.00F, false, false, 0, 3, null, null, 0));
        put(profiles, new WeaponProfile("letztstil", 0, 0, 0x4000, "rwbym:bangle", "rwbym:letztammo", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("bangle", 0, 0, 0x0, "rwbym:sanrei", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("hbangle", 0, 0, 0x0, "rwbym:hsanrei", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("hsanrei", 0, 18, 0x2000, "rwbym:hbangle", "rwbym:sanreiammo", true, 1.00F, false, false, 0, 3, null, null, 0));
        put(profiles, new WeaponProfile("gwai1", 2500, 14, 0x1000, "rwbym:gwai2", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("gwai2", 2500, 14, 0x9000, "rwbym:gwai3", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "gwai2", 0));
        put(profiles, new WeaponProfile("gwai3", 2500, 14, 0x9000, "rwbym:gwai4", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "gwai3", 0));
        put(profiles, new WeaponProfile("gwai4", 2500, 14, 0x3000, "rwbym:gwai5", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "gwai4", 0));
        put(profiles, new WeaponProfile("gwai5", 2500, 14, 0x3000, "rwbym:gwai1", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "gwai5", 0));
        put(profiles, new WeaponProfile("torchwickgun", 2500, 0, 0x0, "rwbym:torchwick", "rwbym:emammmo,rwbym:emflareammo", true, 0.80F, false, false, 0, 1, null, null, 7));
        put(profiles, new WeaponProfile("portgun", 2500, 0, 0x0, "rwbym:port", "rwbym:emammo,rwbym:emammmo", true, 0.80F, false, false, 0, 4, null, null, 7));
        put(profiles, new WeaponProfile("emeraldgun", 2500, 0, 0x0, "rwbym:emeraldblade", "rwbym:spl,rwbym:splfire,rwbym:splgrav,rwbym:splice,rwbym:spllight", true, 1.00F, false, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("emeraldblade", 2500, 14, 0x4000800, "rwbym:emeraldgun", "rwbym:spl,rwbym:splfire,rwbym:splgrav,rwbym:splice,rwbym:spllight", true, 1.00F, false, false, 4, 1, null, null, 5));
        put(profiles, new WeaponProfile("extasis", 2500, 0, 0x40000, "rwbym:extasislance", "rwbym:extasisammo,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, null, 20));
        put(profiles, new WeaponProfile("extasislance", 2500, 14, 0x1, "rwbym:extasis", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("cinderbow", 2500, 0, 0x80, "rwbym:cinder", "minecraft:arrow,minecraft:tipped_arrow,rwbym:bolt,rwbym:boltwind,rwbym:boltlight,rwbym:boltice,rwbym:boltgrav,rwbym:boltfire", true, 2.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("robyncrossbow", 2500, 0, 0x80, "rwbym:robynshield", "minecraft:arrow,minecraft:tipped_arrow,rwbym:bolt,rwbym:boltwind,rwbym:boltlight,rwbym:boltice,rwbym:boltgrav,rwbym:boltfire", true, 2.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("robynshield", 2500, 14, 0x800000, "rwbym:robyncrossbow", "minecraft:arrows32,minecraft:nullest", true, 2.00F, true, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("wattshield", 2500, 14, 0x800000, null, "minecraft:arrows32,minecraft:nullest", true, 2.00F, true, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("adamgun", 2500, 0, 0x0, "rwbym:adamswd", "rwbym:emammo,rwbym:emfireammo,rwbym:emflareammo", true, 0.80F, false, false, 0, 3, null, null, 7));
        put(profiles, new WeaponProfile("sunstaff", 2500, 16, 0x1000002, "rwbym:sunnunchuck", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("sunnunchuck", 2500, 14, 0x1000000, "rwbym:sunstaff", "rwbym:emammo,rwbym:emfireammo,rwbym:emflareammo", true, 0.80F, false, false, 0, 4, null, null, 7));
        put(profiles, new WeaponProfile("neptunegun", 2500, 0, 0x0, "rwbym:neptunespear", "rwbym:neptammo,rwbym:magnaampammo", true, 2.00F, false, false, 0, 1, null, null, 8));
        put(profiles, new WeaponProfile("neptunespear", 2500, 15, 0x2, "rwbym:neptunetrident", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("neptunetrident", 2500, 16, 0x2, "rwbym:neptunegun", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("sage", 2500, 19, 0x1000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("arslan", 2500, 8, 0x12000800, null, "rwbym:arslanammo", false, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("scarletsword", 2500, 14, 0x8, null, "rwbym:spl,rwbym:splfire,rwbym:splgrav,rwbym:splice,rwbym:spllight", true, 2.00F, false, false, 0, 1, null, null, 8));
        put(profiles, new WeaponProfile("nadirgun", 2500, 0, 0x0, "rwbym:nadirsword", "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", false, 1.00F, false, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("nadirsword", 2500, 14, 0x1000, "rwbym:nadirgun", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("hexen", 2500, 14, 0x0, "rwbym:hexenaxe", "rwbym:emammo,rwbym:emfireammo,rwbym:emflareammo", false, 0.70F, false, false, 0, 4, null, null, 5));
        put(profiles, new WeaponProfile("hexenaxe", 2500, 18, 0x2, "rwbym:hexen", "rwbym:emammo,rwbym:emfireammo,rwbym:emflareammo", true, 0.80F, false, false, 0, 4, null, null, 2));
        put(profiles, new WeaponProfile("magnumgun", 2500, 12, 0x0, "rwbym:magnumsword", "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", false, 1.00F, false, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("magnumsword", 2500, 16, 0x2, "rwbym:magnumgun", "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", true, 1.00F, false, false, 2, 1, null, null, 5));
        put(profiles, new WeaponProfile("pyrrharifle", 2500, 0, 0x0, "rwbym:pyrrhasword", "rwbym:crmag,rwbym:crfiremag,rwbym:crgravmag,rwbym:crelectricmag", true, 3.00F, true, false, 0, 1, null, null, 10));
        put(profiles, new WeaponProfile("ironwood", 2500, 0, 0x0, null, "rwbym:spl,rwbym:splfire,rwbym:splgrav,rwbym:splice,rwbym:spllight", true, 2.00F, false, false, 0, 1, null, null, 4));
        put(profiles, new WeaponProfile("ironwood2", 2500, 0, 0x0, null, "rwbym:spl,rwbym:splfire,rwbym:splgrav,rwbym:splice,rwbym:spllight", true, 2.00F, false, false, 1, 1, null, null, 4));
        put(profiles, new WeaponProfile("goodwitch", 2500, 16, 0x0, null, "rwbym:firedust,rwbym:lightdust,rwbym:firedustcrystal,rwbym:lightdustcrystal", true, 1.00F, false, false, 0, 2, null, null, 3));
        put(profiles, new WeaponProfile("oobleckflamethrower", 2500, 13, 0x0, "rwbym:oobleckthermos", "rwbym:firedust,rwbym:firedustcrystal", false, 1.00F, false, false, 0, 3, null, null, 3));
        put(profiles, new WeaponProfile("cardin", 2500, 14, 0x1000000, null, "rwbym:firedust,rwbym:firedustcrystal", true, 1.00F, false, false, 0, 1, null, "fire", 3));
        put(profiles, new WeaponProfile("nebulabow", 2500, 0, 0x80, "rwbym:nebulasword", "minecraft:arrow,minecraft:tipped_arrow,rwbym:bolt,rwbym:boltwind,rwbym:boltlight,rwbym:boltice,rwbym:boltgrav,rwbym:boltfire", true, 2.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("nebulasword", 2500, 14, 0x1000, "rwbym:nebulabow", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("lieutenant", 2500, 10, 0x100000, null, "rwbym:nuller,rwbym:nullest", true, 0.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("henchmenaxe", 2500, 13, 0x100000, null, "rwbym:nuller,rwbym:nullest", true, 0.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("fetch", 2500, 0, 0x0, "rwbym:fetchboomerang", "rwbym:neptammo,rwbym:magnaampammo", true, 2.00F, false, false, 0, 1, null, null, 8));
        put(profiles, new WeaponProfile("fetchboomerang", 2500, 18, 0x12000000, "rwbym:fetch", "rwbym:fetchammo", true, 0.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("atlaspistol", 2500, 0, 0x0, null, "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", true, 1.00F, false, false, 0, 1, null, null, 3));
        put(profiles, new WeaponProfile("atlasrifle", 2500, 0, 0x0, null, "rwbym:crmag,rwbym:crfiremag,rwbym:crgravmag,rwbym:crelectricmag", true, 1.00F, false, false, 0, 1, null, null, 4));
        put(profiles, new WeaponProfile("vernal", 2500, 15, 0x0, null, "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", true, 0.80F, true, false, 0, 2, null, null, 7));
        put(profiles, new WeaponProfile("dove", 2500, 15, 0x1000, null, "rwbym:spl,rwbym:splfire,rwbym:splgrav,rwbym:splice,rwbym:spllight", true, 2.00F, false, false, 0, 2, null, null, 5));
        put(profiles, new WeaponProfile("tyrian", 2500, 15, 0x800000, null, "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", false, 1.00F, false, false, 0, 2, null, null, 5));
        put(profiles, new WeaponProfile("russelnormal", 2500, 14, 0x800, "rwbym:russelfire", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("russelfire", 2500, 14, 0x800, "rwbym:russelice", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, "fire", 0));
        put(profiles, new WeaponProfile("russelice", 2500, 14, 0x800, "rwbym:russelwind", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, "ice", 0));
        put(profiles, new WeaponProfile("russelwind", 2500, 14, 0x800, "rwbym:russelnormal", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, "wind", 0));
        put(profiles, new WeaponProfile("vidiangun", 2500, 0, 0x100, "rwbym:vidianhammer", "rwbym:ammmo", false, 1.00F, false, false, 0, 1, null, null, 3));
        put(profiles, new WeaponProfile("vidian", 2500, 14, 0x0, "rwbym:vidiangun", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("vidianhammer", 2500, 16, 0x1000000, "rwbym:vidian", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("cinderbowglass", 2500, 0, 0x80, "rwbym:cinderglass", "minecraft:arrow,minecraft:tipped_arrow,rwbym:bolt,rwbym:boltwind,rwbym:boltlight,rwbym:boltice,rwbym:boltgrav,rwbym:boltfire", true, 2.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("cinderglass", 2500, 14, 0x1000, "rwbym:cinderbowglass", "rwbym:nuller,rwbym:nullest", true, 2.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("henchmen", 2500, 14, 0x1000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("fennec", 2500, 8, 0x800, "rwbym:fennecdouble", "rwbym:firedust,rwbym:firedustcrystal", true, 1.00F, false, false, 0, 1, null, null, 3));
        put(profiles, new WeaponProfile("fennecdouble", 2500, 16, 0x0, "rwbym:fennec", "rwbym:firedust2", true, 1.00F, false, false, 0, 1, null, null, 3));
        put(profiles, new WeaponProfile("corsac", 2500, 8, 0x800, "rwbym:corsacdouble", "rwbym:winddust,rwbym:winddustcrystal", true, 1.00F, false, false, 0, 1, null, "wind", 0));
        put(profiles, new WeaponProfile("corsacdouble", 2500, 16, 0x0, "rwbym:corsac", "rwbym:firedust2", true, 1.00F, false, false, 0, 1, null, "wind", 0));
        put(profiles, new WeaponProfile("aquaealatlbow", 2500, 0, 0x80, "rwbym:aquaealatlsword", "minecraft:arrow,minecraft:tipped_arrow,rwbym:bolt,rwbym:boltwind,rwbym:boltlight,rwbym:boltice,rwbym:boltgrav,rwbym:boltfire", true, 2.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("aquaealatlsword", 2500, 16, 0x1000, "rwbym:aquaealatlbow", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("brawnz", 2500, 16, 0x800000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("mayaxe", 2500, 16, 0x2, "rwbym:mayrifle", "rwbym:nullest,rwbym:nulls", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("mayrifle", 2500, 0, 0x0, "rwbym:mayaxe", "rwbym:chatmag,rwbym:chatfiremag,rwbym:chatgravmag,rwbym:chatelectricmag", true, 3.00F, false, false, 0, 1, null, null, 10));
        put(profiles, new WeaponProfile("whitefangspear", 2500, 15, 0x2, null, "rwbym:nullest,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("scarletstormgun", 2500, 0, 0x100, "rwbym:scarletstormaxe", "rwbym:ammmo", false, 1.00F, false, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("scarletstormaxe", 2500, 16, 0x2, "rwbym:scarletstormgun", "rwbym:nullest,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("iliasword", 2500, 12, 0x200, null, "rwbym:nullest,rwbym:nuller", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("lionheart", 2500, 6, 0x400, null, "rwbym:firedust,rwbym:firedustcrystal", true, 1.00F, false, true, 0, 1, null, null, 3));
        put(profiles, new WeaponProfile("heroshield", 0, 12, 0x0, "rwbym:leafshield", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("leafshield", 0, 12, 0x8000, "rwbym:pickaxeshield", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("pickaxeshield", 0, 12, 0x208000, "rwbym:rageshield", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("rageshield", 0, 16, 0x8000, "rwbym:heroshield", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("nolan", 2500, 14, 0x1000000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("royg", 2500, 0, 0x800100, null, "rwbym:sawblade", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("wfp", 2500, 0, 0x0, null, "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", true, 1.00F, false, false, 0, 1, null, null, 6));
        put(profiles, new WeaponProfile("gwenknife", 2500, 8, 0x810, null, "rwbym:gwen", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("flyingthundergod", 2500, 10, 0x800, null, "rwbym:thundergodammo", true, 1.00F, false, true, 0, 3, null, null, 0));
        put(profiles, new WeaponProfile("deemace", 2500, 18, 0x1000000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("noctustraumnormal", 2500, 0, 0x100, "rwbym:noctustraumnormalscy", "rwbym:noctu", true, 2.00F, false, false, 0, 1, "rwbym:noctustraumfire", null, 5));
        put(profiles, new WeaponProfile("noctustraumfire", 2500, 0, 0x100, "rwbym:noctustraumfirescy", "rwbym:noctufire", true, 2.00F, false, false, 0, 1, "rwbym:noctustraumice", null, 5));
        put(profiles, new WeaponProfile("noctustraumice", 2500, 0, 0x100, "rwbym:noctustraumicescy", "rwbym:noctuice", true, 2.00F, false, false, 0, 1, "rwbym:noctustraumlight", null, 5));
        put(profiles, new WeaponProfile("noctustraumlight", 2500, 0, 0x100, "rwbym:noctustraumlightscy", "rwbym:noctulight", true, 2.00F, false, false, 0, 1, "rwbym:noctustraumgrav", null, 5));
        put(profiles, new WeaponProfile("noctustraumgrav", 2500, 0, 0x100, "rwbym:noctustraumgravscy", "rwbym:noctugrav", true, 2.00F, false, false, 0, 1, "rwbym:noctustraumnormal", null, 5));
        put(profiles, new WeaponProfile("noctustraumnormalscy", 2500, 16, 0x2, "rwbym:noctustraumnormal", "rwbym:nullest,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("noctustraumfirescy", 2500, 16, 0x2, "rwbym:noctustraumfire", "rwbym:nullest,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, "fire", 0));
        put(profiles, new WeaponProfile("noctustraumicescy", 2500, 16, 0x2, "rwbym:noctustraumice", "rwbym:nullest,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, "ice", 0));
        put(profiles, new WeaponProfile("noctustraumgravscy", 2500, 16, 0x2, "rwbym:noctustraumgrav", "rwbym:nullest,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, "grav", 0));
        put(profiles, new WeaponProfile("noctustraumlightscy", 2500, 16, 0x2, "rwbym:noctustraumlight", "rwbym:nullest,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, "wind", 0));
        put(profiles, new WeaponProfile("neonnormal", 2500, 15, 0x1000000, "rwbym:neonfire", "rwbym:nullest,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("neonfire", 2500, 15, 0x1000000, "rwbym:neonice", "rwbym:nullest,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, "fire", 0));
        put(profiles, new WeaponProfile("neonice", 2500, 15, 0x1000000, "rwbym:neonwind", "rwbym:nullest,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, "ice", 0));
        put(profiles, new WeaponProfile("neonwind", 2500, 15, 0x1000000, "rwbym:neonnormal", "rwbym:nullest,rwbym:nuller", true, 1.00F, false, false, 0, 1, null, "wind", 0));
        put(profiles, new WeaponProfile("tocksword", 2500, 14, 0x40, null, "rwbym:nuller", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("lichtroze_closedfire", 2500, 14, 0x80001, "rwbym:lichtroze_closedice", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "fire", 0));
        put(profiles, new WeaponProfile("lichtroze_closedice", 2500, 14, 0x80001, "rwbym:lichtroze_closedwind", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "ice", 0));
        put(profiles, new WeaponProfile("lichtroze_closedwind", 2500, 14, 0x80001, "rwbym:lichtroze_closedfire", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "wind", 0));
        put(profiles, new WeaponProfile("kyoshifire", 2500, 14, 0x1000, "rwbym:kyoshigrav", "rwbym:firedust,rwbym:firedustcrystal", true, 2.00F, true, false, 0, 1, null, "fire", 3));
        put(profiles, new WeaponProfile("kyoshigrav", 2500, 14, 0x1000, "rwbym:kyoshiice", "rwbym:gravitydust,rwbym:gravitydustcrystal", true, 2.00F, true, false, 0, 1, null, "grav", 3));
        put(profiles, new WeaponProfile("kyoshiice", 2500, 14, 0x1000, "rwbym:kyoshiwind", "rwbym:icedust,rwbym:icedustcrystal", true, 2.00F, true, false, 0, 1, null, "ice", 3));
        put(profiles, new WeaponProfile("kyoshiwind", 2500, 14, 0x1000, "rwbym:kyoshifire", "rwbym:winddust,rwbym:winddustcrystal", true, 2.00F, true, false, 0, 1, null, "wind", 3));
        put(profiles, new WeaponProfile("whitefangsword", 2500, 14, 0x1000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("whitefangrifle", 2500, 0, 0x0, null, "rwbym:crmag,rwbym:crfiremag,rwbym:crgravmag,rwbym:crelectricmag", true, 1.00F, false, false, 0, 1, null, null, 6));
        put(profiles, new WeaponProfile("amberstafffire", 2500, 12, 0x1020000, "rwbym:amberstaffwind", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "fire", 0));
        put(profiles, new WeaponProfile("amberstaffwind", 2500, 12, 0x1020000, "rwbym:amberstafffire", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, "wind", 0));
        put(profiles, new WeaponProfile("amesardentgun", 2500, 0, 0x0, "rwbym:amesardent", "rwbym:emammo,rwbym:emfireammo,rwbym:emflareammo", true, 0.80F, false, false, 0, 4, null, null, 7));
        put(profiles, new WeaponProfile("amesardent", 2500, 16, 0x1000, "rwbym:amesardentgun", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("onoyari", 2500, 17, 0x2, null, "rwbym:neptammo,rwbym:magnaampammo", true, 2.00F, false, false, 0, 1, null, null, 8));
        put(profiles, new WeaponProfile("pugzbow", 2500, 0, 0x80, "rwbym:pugzsword", "minecraft:arrow,minecraft:tipped_arrow,rwbym:bolt,rwbym:boltwind,rwbym:boltlight,rwbym:boltice,rwbym:boltgrav,rwbym:boltfire", true, 2.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("pugzsword", 2500, 14, 0x1000, "rwbym:pugzbow", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("whisperingblossom", 2500, 17, 0x80102, null, "rwbym:whisperammo", true, 1.00F, false, false, 0, 1, null, "grav", 0));
        put(profiles, new WeaponProfile("cassandra", 2500, 16, 0x1000, "rwbym:cassandragun", "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("cassandragun", 2500, 0, 0x0, "rwbym:cassandra", "rwbym:gammag,rwbym:gamgravmag,rwbym:gamicemag,rwbym:gamfiremag", true, 1.00F, true, false, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("freyr", 2700, 16, 0x1, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("nornir", 2700, 0, 0x0, null, "rwbym:spl,rwbym:splfire", true, 2.00F, false, false, 0, 1, null, null, 4));
        put(profiles, new WeaponProfile("hollowtome", 2500, 10, 0x400000, "rwbym:hollowtomefire", "rwbym:nuller,rwbym:nullest", true, 1.00F, true, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("hollowtomefire", 2500, 10, 0x400000, "rwbym:hollowtomegravity", "rwbym:firedust,rwbym:firedustcrystal", true, 1.00F, true, false, 0, 1, null, "fire", 0));
        put(profiles, new WeaponProfile("hollowtomegravity", 2500, 10, 0x400000, "rwbym:hollowtomewind", "rwbym:gravitydust,rwbym:gravitydustcrystal", true, 1.00F, true, false, 0, 1, null, "grav", 0));
        put(profiles, new WeaponProfile("hollowtomewind", 2500, 10, 0x400000, "rwbym:hollowtomelightning", "rwbym:winddust,rwbym:winddustcrystal", true, 1.00F, true, false, 0, 1, null, "wind", 0));
        put(profiles, new WeaponProfile("hollowtomelightning", 2500, 10, 0x400000, "rwbym:hollowtomewater", "rwbym:lightdust,rwbym:lightdustcrystal", true, 1.00F, true, false, 0, 1, null, "light", 0));
        put(profiles, new WeaponProfile("hollowtomewater", 2500, 10, 0x400000, "rwbym:hollowtomeice", "rwbym:waterdust,rwbym:waterdustcrystal", true, 1.00F, true, false, 0, 1, null, "water", 0));
        put(profiles, new WeaponProfile("hollowtomeice", 2500, 10, 0x400000, "rwbym:hollowtome", "rwbym:icedust,rwbym:icedustcrystal", true, 1.00F, true, false, 0, 1, null, "ice", 0));
        put(profiles, new WeaponProfile("chastifol", 2500, 16, 0x12000002, "rwbym:chastifolincrease", "rwbym:chastifolammo", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("thorn", 2500, 16, 0x12800000, null, "rwbym:thornammo", true, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("chastifolincrease", 2500, 8, 0x900, "rwbym:chastifol", "rwbym:chastifolincreaseammo", true, 0.60F, false, false, 0, 8, null, null, 0));
        put(profiles, new WeaponProfile("carminesai", 2500, 8, 0x2000800, "rwbym:carminestaff", "rwbym:carminesaiammo", false, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("carminestaff", 2500, 15, 0x2000002, "rwbym:carminesai", "rwbym:carminestaffammo", false, 1.00F, false, false, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("moonskimmer", 2500, 1, 0x8000000, null, "rwbym:nuller", false, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("mondragon", 2500, 14, 0x8000002, null, "rwbym:gravitydustcrystal,rwbym:icedustcrystal,rwbym:firedustcrystal,rwbym:lightdustcrystal", true, 2.00F, false, true, 0, 1, null, null, 5));
        put(profiles, new WeaponProfile("darkrepulser", 2500, 14, 0x1000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("elucidator", 2500, 14, 0x1000, null, "rwbym:nuller,rwbym:nullest", true, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("pennygun", 2500, 0, 0x100, "rwbym:pennyswd", "rwbym:ammmo", false, 1.00F, false, false, 0, 2, null, null, 1));
        put(profiles, new WeaponProfile("armasword", 2500, 19, 0x1000, null, "rwbym:nuller,rwbym:nullest", false, 1.00F, false, true, 0, 1, null, null, 0));
        put(profiles, new WeaponProfile("armaswordsummon", 2500, 19, 0x1000, null, "rwbym:nuller,rwbym:nullest", false, 1.00F, false, true, 0, 1, null, null, 0));
        return Collections.unmodifiableMap(profiles);
    }

    private static void put(Map<String, WeaponProfile> profiles, WeaponProfile profile) {
        profiles.put(profile.name(), profile);
    }

    private static Set<String> createCreativeWeapons(Map<String, WeaponProfile> profiles) {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        for (String name : profiles.keySet()) {
            graph.put(name, new LinkedHashSet<>());
        }
        for (WeaponProfile profile : profiles.values()) {
            link(graph, profile.name(), profile.morph());
            link(graph, profile.name(), profile.element());
        }

        Set<String> creative = new LinkedHashSet<>();
        Set<String> visited = new HashSet<>();
        for (String name : profiles.keySet()) {
            if (!visited.contains(name)) {
                creative.add(name);
                visitMorphComponent(name, graph, visited);
            }
        }
        return Collections.unmodifiableSet(creative);
    }

    private static void link(Map<String, Set<String>> graph, String source, String targetId) {
        String target = itemPath(targetId);
        if (target == null || !graph.containsKey(target)) {
            return;
        }
        graph.get(source).add(target);
        graph.get(target).add(source);
    }

    private static void visitMorphComponent(String name, Map<String, Set<String>> graph, Set<String> visited) {
        if (!visited.add(name)) {
            return;
        }
        for (String next : graph.getOrDefault(name, Collections.emptySet())) {
            visitMorphComponent(next, graph, visited);
        }
    }

    private static String itemPath(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        int colon = id.indexOf(':');
        if (colon >= 0) {
            return "rwbym".equals(id.substring(0, colon)) ? id.substring(colon + 1) : null;
        }
        return null;
    }

    private RWBYMWeaponProfiles() {
    }

    public record WeaponProfile(String name, int durability, int damage, int type, String morph, String ammo,
            boolean charges, float projectileSpeed, boolean shield, boolean canBlock, int recoilType,
            int bulletCount, String element, String elementMelee, int shotRecoil) {
        public boolean hasType(int flag) {
            return (this.type & flag) != 0;
        }

        public boolean hasMorph() {
            return this.morph != null && !this.morph.isBlank();
        }
    }
}
