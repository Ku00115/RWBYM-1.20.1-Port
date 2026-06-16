package io.github.blaezdev.rwbym.client;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.client.model.RWBYMPlayerArmorModel;
import io.github.blaezdev.rwbym.client.screen.CrusherScreen;
import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import io.github.blaezdev.rwbym.registry.RWBYMMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class RWBYMClientEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RWBYMItems.SIMPLE_ITEMS.values().forEach(RWBYMClientEvents::registerItemPredicates);
            MenuScreens.register(RWBYMMenuTypes.CRUSHER.get(), CrusherScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RWBYMEntityTypes.BEOWOLF.get(),
                context -> grimmRenderer(context, "beowolf", 1.05F));
        event.registerEntityRenderer(RWBYMEntityTypes.URSA.get(),
                context -> grimmRenderer(context, "ursa", 1.25F));
        event.registerEntityRenderer(RWBYMEntityTypes.BOARBATUSK.get(),
                context -> grimmRenderer(context, "boarbatusk", 0.8F));
        event.registerEntityRenderer(RWBYMEntityTypes.CREEP.get(),
                context -> grimmRenderer(context, "creep", 0.85F));
        event.registerEntityRenderer(RWBYMEntityTypes.SABYR.get(),
                context -> grimmRenderer(context, "sabyr", 0.95F));
        event.registerEntityRenderer(RWBYMEntityTypes.BERINGLE.get(),
                context -> grimmRenderer(context, "beringle", 1.35F));
        event.registerEntityRenderer(RWBYMEntityTypes.APATHY.get(),
                context -> grimmRenderer(context, "apathy", 0.95F));
        event.registerEntityRenderer(RWBYMEntityTypes.DEATHSTALKER.get(),
                context -> grimmRenderer(context, "deathstalker", 0.75F));
        event.registerEntityRenderer(RWBYMEntityTypes.LANCER.get(),
                context -> grimmRenderer(context, "lancer", 0.55F));
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
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new RWBYMAccessoryLayer(renderer));
            }
        }
    }

    private static TexturedGrimmRenderer grimmRenderer(
            net.minecraft.client.renderer.entity.EntityRendererProvider.Context context, String name, float scale) {
        return new TexturedGrimmRenderer(context,
                new ResourceLocation(RWBYM.MOD_ID, "textures/entity/" + name + ".png"), scale);
    }

    private static void registerItemPredicates(RegistryObject<Item> object) {
        Item item = object.get();
        registerPullSet(item, "", "pulling");
        registerPullSet(item, "1", "pulling1");
        registerPullSet(item, "2", "pulling2");
        registerPullSet(item, "3", "pulling3");
        registerPullSet(item, "4", "pulling4");
        registerPullSet(item, "5", "pulling5");
        ItemProperties.register(item, new ResourceLocation("blocking"),
                (stack, level, entity, seed) -> isUsing(entity, stack) ? 1.0F : 0.0F);
        ItemProperties.register(item, new ResourceLocation("offhand"),
                (stack, level, entity, seed) -> isOffhand(entity, stack) ? 1.0F : 0.0F);
        ItemProperties.register(item, new ResourceLocation("offhand1"),
                (stack, level, entity, seed) -> isOffhand(entity, stack) ? 1.0F : 0.0F);
        ItemProperties.register(item, new ResourceLocation("mainhand"),
                (stack, level, entity, seed) -> isMainhand(entity, stack) ? 1.0F : 0.0F);
        ItemProperties.register(item, new ResourceLocation("mainhand1"),
                (stack, level, entity, seed) -> isMainhand(entity, stack) ? 1.0F : 0.0F);
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
        return entity != null && entity.getOffhandItem().getItem() == stack.getItem();
    }

    private static boolean isMainhand(LivingEntity entity, ItemStack stack) {
        return entity != null && entity.getMainHandItem().getItem() == stack.getItem();
    }

    private RWBYMClientEvents() {
    }
}
