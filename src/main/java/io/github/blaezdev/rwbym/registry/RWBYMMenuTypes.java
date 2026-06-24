package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.menu.CrusherMenu;
import io.github.blaezdev.rwbym.menu.RWBYMMerchantMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, RWBYM.MOD_ID);

    public static final RegistryObject<MenuType<CrusherMenu>> CRUSHER =
            MENU_TYPES.register("crusher", () -> IForgeMenuType.create((windowId, inv, data) ->
                    new CrusherMenu(windowId, inv)));

    public static final RegistryObject<MenuType<RWBYMMerchantMenu>> MERCHANT =
            MENU_TYPES.register("merchant", () -> IForgeMenuType.create((windowId, inv, data) ->
                    new RWBYMMerchantMenu(windowId, inv, data.readVarInt())));

    private RWBYMMenuTypes() {
    }
}
