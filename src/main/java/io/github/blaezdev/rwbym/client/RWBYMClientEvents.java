package io.github.blaezdev.rwbym.client;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.client.model.grimm.ModelApathy;
import io.github.blaezdev.rwbym.client.model.grimm.ModelArachne;
import io.github.blaezdev.rwbym.client.model.grimm.ModelAtlasKnight;
import io.github.blaezdev.rwbym.client.model.grimm.ModelBeowolf;
import io.github.blaezdev.rwbym.client.model.grimm.ModelBeringle;
import io.github.blaezdev.rwbym.client.model.grimm.ModelBlake;
import io.github.blaezdev.rwbym.client.model.grimm.ModelBoarbatusk;
import io.github.blaezdev.rwbym.client.model.grimm.ModelCreep;
import io.github.blaezdev.rwbym.client.model.grimm.ModelDeathStalker;
import io.github.blaezdev.rwbym.client.model.grimm.ModelGeist;
import io.github.blaezdev.rwbym.client.model.grimm.ModelGoliath;
import io.github.blaezdev.rwbym.client.model.grimm.ModelHollow;
import io.github.blaezdev.rwbym.client.model.grimm.ModelLancer;
import io.github.blaezdev.rwbym.client.model.grimm.ModelMutantDeathStalker;
import io.github.blaezdev.rwbym.client.model.grimm.ModelNeverMore;
import io.github.blaezdev.rwbym.client.model.grimm.ModelNuckleeve;
import io.github.blaezdev.rwbym.client.model.grimm.ModelRagora;
import io.github.blaezdev.rwbym.client.model.grimm.ModelRavager;
import io.github.blaezdev.rwbym.client.model.grimm.ModelSabyr;
import io.github.blaezdev.rwbym.client.model.grimm.ModelSeer;
import io.github.blaezdev.rwbym.client.model.grimm.ModelStore;
import io.github.blaezdev.rwbym.client.model.grimm.ModelUrsa;
import io.github.blaezdev.rwbym.client.model.grimm.ModelUrsaMajor;
import io.github.blaezdev.rwbym.client.model.grimm.ModelWinterbeowolf;
import io.github.blaezdev.rwbym.client.model.grimm.ModelWyvern;
import io.github.blaezdev.rwbym.client.model.grimm.ModelZwei;
import io.github.blaezdev.rwbym.client.model.legacy.ModelBase;
import io.github.blaezdev.rwbym.client.model.RWBYMPlayerArmorModel;
import io.github.blaezdev.rwbym.client.screen.CrusherScreen;
import io.github.blaezdev.rwbym.client.screen.RWBYMMerchantScreen;
import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import io.github.blaezdev.rwbym.item.RWBYMCutGemItem;
import io.github.blaezdev.rwbym.item.RWBYMFishingWeaponItem;
import io.github.blaezdev.rwbym.item.RWBYMGliderItem;
import io.github.blaezdev.rwbym.item.RWBYMLimbItem;
import io.github.blaezdev.rwbym.item.RWBYMMagazineItem;
import io.github.blaezdev.rwbym.item.RWBYMWearableItem;
import io.github.blaezdev.rwbym.item.RWBYMWeaponItem;
import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import io.github.blaezdev.rwbym.registry.RWBYMMenuTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RWBYMClientEvents {
    private static final Set<String> ALWAYS_PREDICATE_ITEMS = Set.of(
            "aquaealatlbow", "cinderbow", "cinderbowglass", "nebulabow", "pugzbow", "kingfisher",
            "lucidroseboard", "reese");

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RWBYMItems.SIMPLE_ITEMS.values().forEach(RWBYMClientEvents::registerItemPredicates);
            MenuScreens.register(RWBYMMenuTypes.CRUSHER.get(), CrusherScreen::new);
            MenuScreens.register(RWBYMMenuTypes.MERCHANT.get(), RWBYMMerchantScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RWBYMEntityTypes.BEOWOLF.get(),
                context -> grimmRenderer(context, new ModelBeowolf(), "beowolf", 1.0F, 0.55F));
        event.registerEntityRenderer(RWBYMEntityTypes.URSA.get(),
                context -> grimmRenderer(context, new ModelUrsa(), "ursa", 1.5F, 0.8F));
        event.registerEntityRenderer(RWBYMEntityTypes.BOARBATUSK.get(),
                context -> grimmRenderer(context, new ModelBoarbatusk(), "boarbatusk", 1.5F, 0.55F));
        event.registerEntityRenderer(RWBYMEntityTypes.CREEP.get(),
                context -> grimmRenderer(context, new ModelCreep(), "creep", 1.3F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.SABYR.get(),
                context -> grimmRenderer(context, new ModelSabyr(), "sabyr", 0.5F, 0.65F));
        event.registerEntityRenderer(RWBYMEntityTypes.BERINGLE.get(),
                context -> grimmRenderer(context, new ModelBeringle(), "beringle", 0.5F, 0.85F));
        event.registerEntityRenderer(RWBYMEntityTypes.APATHY.get(),
                context -> grimmRenderer(context, new ModelApathy(), "apathy", 0.425F, 0.45F));
        event.registerEntityRenderer(RWBYMEntityTypes.DEATHSTALKER.get(),
                context -> grimmRenderer(context, new ModelDeathStalker(), "deathstalker", 2.5F, 0.0F, -0.2F, 0.0F, 0.85F));
        event.registerEntityRenderer(RWBYMEntityTypes.LANCER.get(),
                context -> grimmRenderer(context, new ModelLancer(), "lancer", 1.0F, 0.35F));
        registerTexturedGrimmRenderers(event);
        registerNpcRenderers(event);
        event.registerEntityRenderer(RWBYMEntityTypes.PROJECTILES.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(RWBYMEntityTypes.FIREBALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(RWBYMEntityTypes.LARGE_FIREBALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(RWBYMEntityTypes.WEAPON_PROJECTILE.get(), ThrownItemRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RWBYMPlayerArmorModel.DEFAULT_LAYER,
                RWBYMPlayerArmorModel::createDefaultBodyLayer);
        event.registerLayerDefinition(RWBYMPlayerArmorModel.SLIM_LAYER,
                RWBYMPlayerArmorModel::createSlimBodyLayer);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(new DynamicFluidContainerModel.Colors(), RWBYMItems.GRIMM_BUCKET.get());
    }

    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new RWBYMAccessoryLayer(renderer));
                renderer.addLayer(new RWBYMGliderLayer(renderer, Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer()));
            }
        }
    }

    private static <T extends Mob> LegacyGrimmRenderer<T> grimmRenderer(
            net.minecraft.client.renderer.entity.EntityRendererProvider.Context context, ModelBase<?> model, String name,
            float scale, float shadowRadius) {
        return new LegacyGrimmRenderer(context, model,
                new ResourceLocation(RWBYM.MOD_ID, "textures/entity/" + name + ".png"), scale, shadowRadius);
    }

    private static <T extends Mob> LegacyGrimmRenderer<T> grimmRenderer(
            net.minecraft.client.renderer.entity.EntityRendererProvider.Context context, ModelBase<?> model, String name,
            float scale, float translateX, float translateY, float translateZ, float shadowRadius) {
        return grimmRenderer(context, model, name, scale, scale, scale, translateX, translateY, translateZ, shadowRadius);
    }

    private static <T extends Mob> LegacyGrimmRenderer<T> grimmRenderer(
            net.minecraft.client.renderer.entity.EntityRendererProvider.Context context, ModelBase<?> model, String name,
            float scaleX, float scaleY, float scaleZ, float translateX, float translateY, float translateZ,
            float shadowRadius) {
        return new LegacyGrimmRenderer(context, model,
                new ResourceLocation(RWBYM.MOD_ID, "textures/entity/" + name + ".png"),
                scaleX, scaleY, scaleZ, translateX, translateY, translateZ, shadowRadius);
    }

    private static void registerTexturedGrimmRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RWBYMEntityTypes.WINTER_BEOWOLF.get(),
                context -> grimmRenderer(context, new ModelWinterbeowolf(), "winter_beowolf", 1.0F, 0.6F));
        event.registerEntityRenderer(RWBYMEntityTypes.WINTER_URSA.get(),
                context -> grimmRenderer(context, new ModelUrsa(), "winter_ursa", 1.5F, 0.9F));
        event.registerEntityRenderer(RWBYMEntityTypes.URSA_MAJOR.get(),
                context -> grimmRenderer(context, new ModelUrsaMajor(), "ursa", 1.5F, 1.2F));
        event.registerEntityRenderer(RWBYMEntityTypes.WINTER_BOARBATUSK.get(),
                context -> grimmRenderer(context, new ModelBoarbatusk(), "winter_boarbatusk", 1.5F, 0.55F));
        event.registerEntityRenderer(RWBYMEntityTypes.MUTANT_DEATHSTALKER.get(),
                context -> grimmRenderer(context, new ModelMutantDeathStalker(), "deathstalkermutant", 2.5F, 0.0F, -0.2F, 0.0F, 1.1F));
        event.registerEntityRenderer(RWBYMEntityTypes.TINY_DEATHSTALKER.get(),
                context -> grimmRenderer(context, new ModelDeathStalker(), "deathstalker", 0.3F, 0.0F, -0.2F, 0.0F, 0.45F));
        event.registerEntityRenderer(RWBYMEntityTypes.QUEEN_LANCER.get(),
                context -> grimmRenderer(context, new ModelLancer(), "lancer", 5.0F, 3.0F, 5.0F, 0.0F, 0.5F, 0.0F, 0.8F));
        event.registerEntityRenderer(RWBYMEntityTypes.GOLIATH.get(),
                context -> grimmRenderer(context, new ModelGoliath(), "goliath", 4.5F, 1.6F));
        event.registerEntityRenderer(RWBYMEntityTypes.NEVERMORE.get(),
                context -> grimmRenderer(context, new ModelNeverMore(), "nevermore", 0.2F, 0.9F));
        event.registerEntityRenderer(RWBYMEntityTypes.GIANT_NEVERMORE.get(),
                context -> grimmRenderer(context, new ModelNeverMore(), "nevermore", 2.0F, 0.0F, 2.0F, 0.0F, 1.8F));
        event.registerEntityRenderer(RWBYMEntityTypes.ARMORGEIST.get(),
                context -> grimmRenderer(context, new ModelGeist(), "armorgeist", 3.0F, 0.8F));
        event.registerEntityRenderer(RWBYMEntityTypes.WINTER_ARMORGEIST.get(),
                context -> grimmRenderer(context, new ModelGeist(), "winter_armorgeist", 1.5F, 0.8F));
        event.registerEntityRenderer(RWBYMEntityTypes.GEIST.get(),
                context -> grimmRenderer(context, new ModelGeist(), "geist", 0.75F, 0.7F));
        event.registerEntityRenderer(RWBYMEntityTypes.NUCKLEEVE.get(),
                context -> grimmRenderer(context, new ModelNuckleeve(), "nuckleeve", 1.5F, 1.0F));
        event.registerEntityRenderer(RWBYMEntityTypes.WYVERN.get(),
                context -> grimmRenderer(context, new ModelWyvern(), "wyvern", 3.5F, 0.0F, 1.5F, 0.0F, 2.0F));
        event.registerEntityRenderer(RWBYMEntityTypes.RAVAGER.get(),
                context -> grimmRenderer(context, new ModelRavager(), "wyvern", 0.5F, 0.5F, 0.55F, 0.0F, 0.0F, 0.0F, 0.6F));
        event.registerEntityRenderer(RWBYMEntityTypes.SEER.get(),
                context -> grimmRenderer(context, new ModelSeer(), "seer", 0.1F, 0.0F, -0.3F, 0.0F, 0.6F));
        event.registerEntityRenderer(RWBYMEntityTypes.ARACHNE.get(),
                context -> grimmRenderer(context, new ModelArachne(), "arachne", 0.2F, 0.8F));
        event.registerEntityRenderer(RWBYMEntityTypes.ARACHNE_CLONE.get(),
                context -> grimmRenderer(context, new ModelBlake(), "blake", 1.0F, 0.8F));
        event.registerEntityRenderer(RWBYMEntityTypes.HOLLOW.get(),
                context -> grimmRenderer(context, new ModelHollow(), "hollow", 0.325F, 0.7F));
    }

    private static void registerNpcRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RWBYMEntityTypes.ATLAS_KNIGHT.get(),
                context -> grimmRenderer(context, new ModelAtlasKnight(), "atlasknight", 1.0F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.BLAKE.get(),
                context -> grimmRenderer(context, new ModelBlake(), "blake", 1.0F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.BLAKE_FIRE.get(),
                context -> grimmRenderer(context, new ModelBlake(), "blakefire", 1.0F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.BLAKE_ICE.get(),
                context -> grimmRenderer(context, new ModelBlake(), "blakeice", 1.0F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.STORE.get(),
                context -> grimmRenderer(context, new ModelStore(), "shop", 1.0F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.WEAPON_STORE.get(),
                context -> grimmRenderer(context, new ModelStore(), "wepshop", 1.0F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.BLACK_STORE.get(),
                context -> grimmRenderer(context, new ModelStore(), "whitefang", 1.0F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.ARMOR_STORE.get(),
                context -> grimmRenderer(context, new ModelStore(), "armshop", 1.0F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.CROWBAR.get(),
                context -> grimmRenderer(context, new ModelBlake(), "crowbar", 1.0F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.REN.get(),
                context -> grimmRenderer(context, new ModelBlake(), "ren", 1.0F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.RAGORA.get(),
                context -> grimmRenderer(context, new ModelRagora(), "modelragora", 1.0F, 0.5F));
        event.registerEntityRenderer(RWBYMEntityTypes.ZWEI.get(),
                context -> grimmRenderer(context, new ModelZwei(), "zwei", 1.0F, 0.0F, 0.15F, 0.0F, 0.35F));
    }

    private static void registerTexturedGrimm(EntityRenderersEvent.RegisterRenderers event,
            RegistryObject<net.minecraft.world.entity.EntityType<BasicGrimmEntity>> type, String texture, float scale) {
        event.registerEntityRenderer(type.get(), context -> texturedRenderer(context, texture, scale));
    }

    private static void registerTexturedNpc(EntityRenderersEvent.RegisterRenderers event,
            RegistryObject<? extends net.minecraft.world.entity.EntityType<? extends Zombie>> type, String texture, float scale) {
        event.registerEntityRenderer(type.get(), context -> texturedRenderer(context, texture, scale));
    }

    private static TexturedGrimmRenderer texturedRenderer(
            net.minecraft.client.renderer.entity.EntityRendererProvider.Context context, String texture, float scale) {
        return new TexturedGrimmRenderer(context,
                new ResourceLocation(RWBYM.MOD_ID, "textures/entity/" + texture + ".png"), scale);
    }

    private static void registerItemPredicates(RegistryObject<Item> object) {
        Item item = object.get();
        String name = object.getId().getPath();
        if (!needsPredicates(item, name)) {
            return;
        }
        if (needsPullPredicates(item, name)) {
            registerPullSet(item, "", "pulling");
            registerPullSet(item, "1", "pulling1");
            registerPullSet(item, "2", "pulling2");
            registerPullSet(item, "3", "pulling3");
            registerPullSet(item, "4", "pulling4");
            registerPullSet(item, "5", "pulling5");
        }
        ItemProperties.register(item, new ResourceLocation("blocking"),
                (stack, level, entity, seed) -> isUsing(entity, stack)
                        || RWBYMWeaponItem.isActiveKineticBoard(stack, entity) ? 1.0F : 0.0F);
        ItemProperties.register(item, new ResourceLocation("offhand"),
                (stack, level, entity, seed) -> isOffhand(entity, stack) ? 1.0F : 0.0F);
        ItemProperties.register(item, new ResourceLocation("offhand1"),
                (stack, level, entity, seed) -> isOffhand(entity, stack) ? 1.0F : 0.0F);
        ItemProperties.register(item, new ResourceLocation("mainhand"),
                (stack, level, entity, seed) -> isMainhand(entity, stack) ? 1.0F : 0.0F);
        ItemProperties.register(item, new ResourceLocation("mainhand1"),
                (stack, level, entity, seed) -> isMainhand(entity, stack) ? 1.0F : 0.0F);
        if (item instanceof RWBYMFishingWeaponItem) {
            ItemProperties.register(item, new ResourceLocation("cast"),
                    (stack, level, entity, seed) -> entity instanceof net.minecraft.world.entity.player.Player player
                            && player.fishing != null
                            && (player.getMainHandItem() == stack || player.getOffhandItem() == stack) ? 1.0F : 0.0F);
        }
        if (item instanceof RWBYMWeaponItem && (name.equals("p90") || name.equals("hecate2"))) {
            registerSpecialGunPredicates(item);
        }
        if (item instanceof RWBYMMagazineItem) {
            ItemProperties.register(item, new ResourceLocation("bullets"),
                    (stack, level, entity, seed) -> RWBYMMagazineItem.getAmmoCount(stack));
        }
    }

    private static boolean needsPredicates(Item item, String name) {
        return item instanceof RWBYMWeaponItem
                || item instanceof RWBYMCutGemItem
                || item instanceof RWBYMFishingWeaponItem
                || item instanceof RWBYMGliderItem
                || item instanceof RWBYMMagazineItem
                || item instanceof RWBYMWearableItem
                || item instanceof RWBYMLimbItem
                || ALWAYS_PREDICATE_ITEMS.contains(name);
    }

    private static boolean needsPullPredicates(Item item, String name) {
        return item instanceof RWBYMWeaponItem
                || item instanceof RWBYMCutGemItem
                || item instanceof RWBYMGliderItem
                || ALWAYS_PREDICATE_ITEMS.contains(name);
    }

    private static void registerSpecialGunPredicates(Item item) {
        for (String predicate : new String[] {
                "chambered", "empty", "loaded", "mag", "mag_anim", "bullets", "bolt", "boltup", "boltback",
                "slide", "slideback", "charge_handle", "hammer", "fired", "auto", "mode", "modeindex", "ads",
                "burstcount", "magout", "boltopen"
        }) {
            ItemProperties.register(item, new ResourceLocation(predicate),
                    (stack, level, entity, seed) -> RWBYMWeaponItem.specialGunPredicate(stack, predicate));
        }
    }

    private static void registerPullSet(Item item, String suffix, String pullingName) {
        ItemProperties.register(item, new ResourceLocation(pullingName),
                (stack, level, entity, seed) -> isUsing(entity, stack) ? 1.0F : 0.0F);
        ItemProperties.register(item, new ResourceLocation("pull" + suffix), (stack, level, entity, seed) -> {
            if (!isUsing(entity, stack)) {
                return 0.0F;
            }
            int used = stack.getUseDuration() - entity.getUseItemRemainingTicks();
            return Math.min(1.0F, used / 20.0F);
        });
    }

    private static boolean isUsing(LivingEntity entity, ItemStack stack) {
        return entity != null && entity.isUsingItem() && entity.getUseItem() == stack;
    }

    private static boolean isOffhand(LivingEntity entity, ItemStack stack) {
        // Match the rendered stack, not just the item id, so offhand override models cannot bleed into mainhand.
        return entity != null && entity.getOffhandItem() == stack;
    }

    private static boolean isMainhand(LivingEntity entity, ItemStack stack) {
        // Match the original 1.12 property behavior: predicates describe the exact stack being rendered.
        return entity != null && entity.getMainHandItem() == stack;
    }

    private RWBYMClientEvents() {
    }
}
