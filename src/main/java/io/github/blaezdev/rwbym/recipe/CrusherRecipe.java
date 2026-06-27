package io.github.blaezdev.rwbym.recipe;

import com.google.gson.JsonObject;
import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.registry.RWBYMRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Data-driven Crusher recipe used by the 1.20.1 machine port.
 * <p>
 * Original RWBYM stored Crusher pairs in {@code Init/CrusherRecipe.java}; this class preserves the
 * same input/tool/result shape while letting Forge load the table from normal datapack JSON.
 * Linked files: {@code CrusherBlockEntity.java}, {@code RWBYMRecipeTypes.java}.
 */
public class CrusherRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final Ingredient input;
    private final Ingredient tool;
    private final ItemStack result;
    private final float experience;

    public CrusherRecipe(ResourceLocation id, Ingredient input, Ingredient tool, ItemStack result, float experience) {
        this.id = id;
        this.input = input;
        this.tool = tool;
        this.result = result;
        this.experience = experience;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return this.input.test(container.getItem(0)) && this.tool.test(container.getItem(1));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return this.result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RWBYMRecipeTypes.CRUSHER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RWBYMRecipeTypes.CRUSHER.get();
    }

    public float experience() {
        return this.experience;
    }

    public static final class Serializer implements RecipeSerializer<CrusherRecipe> {
        @Override
        public CrusherRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getNonNull(json, "input"));
            Ingredient tool = Ingredient.fromJson(GsonHelper.getNonNull(json, "tool"));
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
            return new CrusherRecipe(recipeId, input, tool, result, experience);
        }

        @Override
        public @Nullable CrusherRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            Ingredient tool = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            float experience = buffer.readFloat();
            return new CrusherRecipe(recipeId, input, tool, result, experience);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CrusherRecipe recipe) {
            recipe.input.toNetwork(buffer);
            recipe.tool.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeFloat(recipe.experience);
        }
    }

    public static final class Type implements RecipeType<CrusherRecipe> {
        @Override
        public String toString() {
            return RWBYM.MOD_ID + ":crusher";
        }
    }
}
