package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.recipe.CrusherRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers custom recipe infrastructure used by RWBYM machines.
 * <p>
 * Linked files: {@code CrusherRecipe.java}, {@code CrusherBlockEntity.java}.
 */
public final class RWBYMRecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, RWBYM.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, RWBYM.MOD_ID);

    public static final RegistryObject<RecipeSerializer<CrusherRecipe>> CRUSHER_SERIALIZER =
            RECIPE_SERIALIZERS.register("crusher", CrusherRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<CrusherRecipe>> CRUSHER =
            RECIPE_TYPES.register("crusher", CrusherRecipe.Type::new);

    private RWBYMRecipeTypes() {
    }
}
