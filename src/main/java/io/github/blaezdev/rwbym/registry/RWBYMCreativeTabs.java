package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RWBYM.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwbym"))
                    .icon(() -> RWBYMItems.ICON.isPresent() ? new ItemStack(RWBYMItems.ICON.get()) : ItemStack.EMPTY)
                    .displayItems((parameters, output) -> RWBYMItems.ITEMS.getEntries()
                            .forEach(item -> {
                                String name = item.getId().getPath();
                                if (isVisibleInMain(name)) {
                                    output.accept(item.get());
                                }
                            }))
                    .build());

    public static final RegistryObject<CreativeModeTab> WEAPONS = CREATIVE_MODE_TABS.register("weapons", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwby_weapons"))
                    .icon(() -> stack("crescent"))
                    .displayItems((parameters, output) -> RWBYMItems.SIMPLE_ITEMS.forEach((name, item) -> {
                        if (isVisibleWeapon(name)) {
                            output.accept(item.get());
                        }
                    }))
                    .build());

    public static final RegistryObject<CreativeModeTab> ARMOR = CREATIVE_MODE_TABS.register("armor", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwby_armour"))
                    .icon(() -> stack("ruby2_chest"))
                    .displayItems((parameters, output) -> RWBYMItems.SIMPLE_ITEMS.forEach((name, item) -> {
                        if (isArmor(name)) {
                            output.accept(item.get());
                        }
                    }))
                    .build());

    public static final RegistryObject<CreativeModeTab> CHARMS = CREATIVE_MODE_TABS.register("charms", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwby_charms"))
                    .icon(() -> stack("auracharm"))
                    .displayItems((parameters, output) -> RWBYMItems.SIMPLE_ITEMS.forEach((name, item) -> {
                        if (isCharm(name)) {
                            output.accept(item.get());
                        }
                    }))
                    .build());

    public static final RegistryObject<CreativeModeTab> COSMETICS = CREATIVE_MODE_TABS.register("cosmetics", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwby_limbs"))
                    .icon(() -> stack("blackcatear"))
                    .displayItems((parameters, output) -> RWBYMItems.SIMPLE_ITEMS.forEach((name, item) -> {
                        if (isCosmetic(name)) {
                            output.accept(item.get());
                        }
                    }))
                    .build());

    public static final RegistryObject<CreativeModeTab> MATERIALS = CREATIVE_MODE_TABS.register("materials", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwby_items"))
                    .icon(() -> stack("firedust"))
                    .displayItems((parameters, output) -> RWBYMItems.SIMPLE_ITEMS.forEach((name, item) -> {
                        if (isVisibleMaterial(name) && !isWeapon(name) && !isArmor(name) && !isCharm(name) && !isCosmetic(name)
                                && !isSpawnEgg(name)) {
                            output.accept(item.get());
                        }
                    }))
                    .build());

    public static final RegistryObject<CreativeModeTab> BLOCKS = CREATIVE_MODE_TABS.register("blocks", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwbym.blocks"))
                    .icon(() -> blockStack("fireore"))
                    .displayItems((parameters, output) -> RWBYMItems.BLOCK_ITEMS.values()
                            .forEach(item -> output.accept(item.get())))
                    .build());

    public static final RegistryObject<CreativeModeTab> ENTITIES = CREATIVE_MODE_TABS.register("entities", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rwbym.entities"))
                    .icon(() -> stack("beowolf_spawn_egg"))
                    .displayItems((parameters, output) -> RWBYMItems.SIMPLE_ITEMS.forEach((name, item) -> {
                        if (isSpawnEgg(name)) {
                            output.accept(item.get());
                        }
                    }))
                    .build());

    private static ItemStack stack(String name) {
        RegistryObject<Item> item = RWBYMItems.SIMPLE_ITEMS.get(name);
        return item != null && item.isPresent() ? new ItemStack(item.get()) : ItemStack.EMPTY;
    }

    private static ItemStack blockStack(String name) {
        RegistryObject<Item> item = RWBYMItems.BLOCK_ITEMS.get(name);
        return item != null && item.isPresent() ? new ItemStack(item.get()) : ItemStack.EMPTY;
    }

    private static boolean isWeapon(String name) {
        if (isAmmoLike(name) || name.startsWith("entity")) {
            return false;
        }
        return io.github.blaezdev.rwbym.item.RWBYMWeaponProfiles.contains(name)
                || name.endsWith("gun")
                || name.endsWith("rifle")
                || name.endsWith("pistol")
                || name.endsWith("sword")
                || name.endsWith("swd")
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
                || name.endsWith("shield")
                || name.endsWith("tome")
                || name.endsWith("sai")
                || name.endsWith("mace")
                || name.endsWith("trident")
                || name.endsWith("boomerang")
                || name.endsWith("whip");
    }

    private static boolean isVisibleWeapon(String name) {
        if (!isWeapon(name) || isHiddenCreativeItem(name)) {
            return false;
        }
        if (isBowStage(name)) {
            return false;
        }
        if (isTransformationStage(name)) {
            return false;
        }
        return !io.github.blaezdev.rwbym.item.RWBYMWeaponProfiles.contains(name)
                || io.github.blaezdev.rwbym.item.RWBYMWeaponProfiles.isCreativeWeapon(name);
    }

    private static boolean isVisibleInMain(String name) {
        return !isHiddenCreativeItem(name) && (isVisibleWeapon(name) || isArmor(name) || isCharm(name)
                || isCosmetic(name) || isSpawnEgg(name) || isMaterial(name));
    }

    private static boolean isVisibleMaterial(String name) {
        return !isHiddenCreativeItem(name);
    }

    private static boolean isArmor(String name) {
        return name.endsWith("_head") || name.endsWith("_chest") || name.endsWith("_legs") || name.endsWith("_boots")
                || name.endsWith("hood") || name.endsWith("mask") || name.endsWith("glasses") || name.endsWith("hat");
    }

    private static boolean isCharm(String name) {
        return name.endsWith("charm") || name.endsWith("bangle") || name.equals("fairyking")
                || name.equals("kingsgambit") || name.equals("kingsgambitpawn") || name.equals("relicofknowledge");
    }

    private static boolean isCosmetic(String name) {
        return name.startsWith("clear")
                || name.equals("rgrimmarm")
                || name.equals("lgrimmarm")
                || name.equals("rgrimmleg")
                || name.equals("lgrimmleg")
                || name.equals("grimmhead")
                || name.equals("grimmbody")
                || name.endsWith("ear")
                || name.endsWith("ears")
                || name.endsWith("tail")
                || name.contains("tail")
                || name.endsWith("horn")
                || name.endsWith("horns")
                || name.startsWith("antler")
                || name.startsWith("dorsalfin")
                || name.equals("kag")
                || name.equals("kag2");
    }

    private static boolean isSpawnEgg(String name) {
        return name.endsWith("_spawn_egg");
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

    private static boolean isMaterial(String name) {
        return !isWeapon(name) && !isArmor(name) && !isCharm(name) && !isCosmetic(name) && !isSpawnEgg(name)
                && !isHiddenCreativeItem(name);
    }

    private static boolean isHiddenCreativeItem(String name) {
        return isInternalAnimationPart(name)
                || isBowStage(name)
                || isTransformationStage(name)
                || name.startsWith("entity")
                || name.equals("sawblade");
    }

    private static boolean isTransformationStage(String name) {
        return name.endsWith("v")
                || name.endsWith("oh")
                || name.endsWith("open")
                || name.endsWith("closed")
                || name.endsWith("deployed")
                || name.endsWith("recoil")
                || name.endsWith("summon")
                || name.endsWith("empty")
                || name.endsWith("ride")
                || name.endsWith("load");
    }

    private static boolean isInternalAnimationPart(String name) {
        return name.equals("crescentscy")
                || name.equals("croceaswd")
                || name.equals("gambolswd")
                || name.equals("juaneshield")
                || name.equals("kkiceshield")
                || name.equals("lysetteshield")
                || name.equals("myrteswd")
                || name.equals("neoumb_open_blade")
                || name.equals("pyrrhashield")
                || name.equals("qrowscy")
                || name.equals("scarletgun")
                || name.equals("stormflower_vol7gun")
                || name.equals("wattsshield");
    }

    private static boolean isBowStage(String name) {
        for (String suffix : new String[] { "125", "150", "175", "1" }) {
            if (name.endsWith(suffix)) {
                String baseName = name.substring(0, name.length() - suffix.length());
                return RWBYMItems.SIMPLE_ITEMS.containsKey(baseName) && isWeapon(baseName);
            }
        }
        return false;
    }

    private RWBYMCreativeTabs() {
    }
}
