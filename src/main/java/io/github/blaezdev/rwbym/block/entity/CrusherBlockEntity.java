package io.github.blaezdev.rwbym.block.entity;

import io.github.blaezdev.rwbym.menu.CrusherMenu;
import io.github.blaezdev.rwbym.recipe.CrusherRecipe;
import io.github.blaezdev.rwbym.registry.RWBYMBlockEntities;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import io.github.blaezdev.rwbym.registry.RWBYMRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CrusherBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {
    public static final int INPUT_SLOT = 0;
    public static final int TOOL_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int OUTPUT_SLOT = 3;
    private static final int[] INPUT_SLOTS = {INPUT_SLOT, TOOL_SLOT};
    private static final int[] FUEL_SLOTS = {FUEL_SLOT};
    private static final int[] OUTPUT_SLOTS = {OUTPUT_SLOT};
    private static Map<Item, ItemStack> legacyCrushRecipes;
    private static Map<Item, ItemStack> legacyChiselRecipes;

    private final net.minecraft.core.NonNullList<ItemStack> items =
            net.minecraft.core.NonNullList.withSize(4, ItemStack.EMPTY);
    private int burnTime;
    private int maxBurnTime;
    private int cookTime;
    private int maxCookTime = 200;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CrusherBlockEntity.this.burnTime;
                case 1 -> CrusherBlockEntity.this.maxBurnTime;
                case 2 -> CrusherBlockEntity.this.cookTime;
                case 3 -> CrusherBlockEntity.this.maxCookTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> CrusherBlockEntity.this.burnTime = value;
                case 1 -> CrusherBlockEntity.this.maxBurnTime = value;
                case 2 -> CrusherBlockEntity.this.cookTime = value;
                case 3 -> CrusherBlockEntity.this.maxCookTime = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public CrusherBlockEntity(BlockPos pos, BlockState state) {
        super(RWBYMBlockEntities.CRUSHER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrusherBlockEntity crusher) {
        if (level.isClientSide()) {
            return;
        }
        boolean changed = false;
        if (crusher.burnTime > 0) {
            crusher.burnTime--;
            changed = true;
        }
        if (crusher.burnTime <= 0 && crusher.canProcess() && isFuel(crusher.items.get(FUEL_SLOT))) {
            ItemStack fuel = crusher.items.get(FUEL_SLOT);
            crusher.maxBurnTime = fuelTime(fuel);
            crusher.burnTime = crusher.maxBurnTime;
            crusher.consumeFuel(fuel);
            changed = true;
        }
        if (crusher.burnTime > 0 && crusher.canProcess()) {
            crusher.cookTime++;
            if (crusher.cookTime >= crusher.maxCookTime) {
                crusher.process();
                crusher.cookTime = 0;
            }
            changed = true;
        } else if (crusher.cookTime != 0) {
            crusher.cookTime = 0;
            changed = true;
        }
        if (changed) {
            setChanged(level, pos, state);
        }
    }

    public ContainerData data() {
        return this.data;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putInt("BurnTime", this.burnTime);
        tag.putInt("MaxBurnTime", this.maxBurnTime);
        tag.putInt("CookTime", this.cookTime);
        tag.putInt("MaxCookTime", this.maxCookTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, this.items);
        this.burnTime = tag.getInt("BurnTime");
        this.maxBurnTime = tag.getInt("MaxBurnTime");
        this.cookTime = tag.getInt("CookTime");
        this.maxCookTime = tag.contains("MaxCookTime") ? tag.getInt("MaxCookTime") : 200;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.rwbym.crusher");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CrusherMenu(containerId, inventory, this, this.data);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return OUTPUT_SLOTS;
        }
        if (side == Direction.UP) {
            return INPUT_SLOTS;
        }
        return FUEL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return slot == OUTPUT_SLOT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // Legacy slot 0 accepted any input; the recipe table decides later whether the stack can process.
            case INPUT_SLOT -> true;
            case TOOL_SLOT -> isCrusherTool(stack);
            case FUEL_SLOT -> isFuel(stack);
            default -> false;
        };
    }

    private boolean canProcess() {
        ItemStack result = this.crushedResult(this.items.get(INPUT_SLOT), this.items.get(TOOL_SLOT));
        if (result.isEmpty()) {
            return false;
        }
        ItemStack output = this.items.get(OUTPUT_SLOT);
        return output.isEmpty() || ItemStack.isSameItemSameTags(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void process() {
        ItemStack result = this.crushedResult(this.items.get(INPUT_SLOT), this.items.get(TOOL_SLOT));
        if (result.isEmpty()) {
            return;
        }
        ItemStack output = this.items.get(OUTPUT_SLOT);
        if (output.isEmpty()) {
            this.items.set(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }
        this.items.get(INPUT_SLOT).shrink(1);
        damageCrusherTool();
    }

    private void consumeFuel(ItemStack fuel) {
        ItemStack remaining = fuel.getCraftingRemainingItem();
        fuel.shrink(1);
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // The 1.12 crusher consumed fuel via getContainerItem, so preserve bucket-style remainders.
        if (fuel.isEmpty() && !remaining.isEmpty()) {
            this.items.set(FUEL_SLOT, remaining);
        }
    }

    private void damageCrusherTool() {
        ItemStack tool = this.items.get(TOOL_SLOT);
        if (tool.isEmpty()) {
            return;
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Legacy crush/chisel used getContainerItem, so ask the stack for its modern crafting remainder first.
        if (tool.hasCraftingRemainingItem()) {
            this.items.set(TOOL_SLOT, tool.getCraftingRemainingItem());
        } else {
            tool.shrink(1);
        }
    }

    public static boolean isFuel(ItemStack stack) {
        return fuelTime(stack) > 0;
    }

    private static int fuelTime(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        if (item == net.minecraft.world.item.Items.COAL || item == net.minecraft.world.item.Items.CHARCOAL) {
            return 1600;
        }
        if (item == net.minecraft.world.item.Items.BLAZE_ROD) {
            return 2400;
        }
        if (item == net.minecraft.world.item.Items.LAVA_BUCKET) {
            return 20000;
        }
        return net.minecraftforge.common.ForgeHooks.getBurnTime(stack, null);
    }

    private static ItemStack legacyCrushedResult(ItemStack input, ItemStack tool) {
        if (input.isEmpty() || !isCrusherTool(tool)) {
            return ItemStack.EMPTY;
        }
        Map<Item, ItemStack> recipes = idPath(tool).equals("chisel") ? legacyChiselRecipes() : legacyCrushRecipes();
        ItemStack result = recipes.get(input.getItem());
        return result == null ? ItemStack.EMPTY : result.copy();
    }

    private ItemStack crushedResult(ItemStack input, ItemStack tool) {
        Optional<CrusherRecipe> recipe = this.crusherRecipe(input, tool);
        return recipe.map(value -> value.getResultItem(this.level.registryAccess()).copy())
                .orElseGet(() -> legacyCrushedResult(input, tool));
    }

    private Optional<CrusherRecipe> crusherRecipe(ItemStack input, ItemStack tool) {
        if (this.level == null || input.isEmpty() || !isCrusherTool(tool)) {
            return Optional.empty();
        }
        SimpleContainer recipeInput = new SimpleContainer(input, tool);
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // The original recipe table matched a two-stack pair; the modern port asks the datapack recipe manager for the same pair.
        return this.level.getRecipeManager().getRecipeFor(RWBYMRecipeTypes.CRUSHER.get(), recipeInput, this.level);
    }

    private static Map<Item, ItemStack> legacyCrushRecipes() {
        if (legacyCrushRecipes == null) {
            legacyCrushRecipes = buildCrushRecipes();
        }
        return legacyCrushRecipes;
    }

    private static Map<Item, ItemStack> legacyChiselRecipes() {
        if (legacyChiselRecipes == null) {
            legacyChiselRecipes = buildChiselRecipes();
        }
        return legacyChiselRecipes;
    }

    private static Map<Item, ItemStack> buildCrushRecipes() {
        Map<Item, ItemStack> recipes = new HashMap<>();
        addRecipe(recipes, "dustrock", "dust", 2);
        for (String element : new String[] {"fire", "ice", "water", "wind", "gravity", "light"}) {
            addRecipe(recipes, element + "dustrock", element + "dust", 2);
        }
        return Map.copyOf(recipes);
    }

    private static Map<Item, ItemStack> buildChiselRecipes() {
        Map<Item, ItemStack> recipes = new HashMap<>();
        addRecipe(recipes, "dustrock", "dustcrystal", 1);
        addRecipe(recipes, "dustrockhardlight", "dustcrystalhardlight", 1);
        for (String element : new String[] {"fire", "ice", "water", "wind", "gravity", "light"}) {
            addRecipe(recipes, element + "dustrock", element + "dustcrystal", 1);
            addRecipe(recipes, element + "dustcrystal", element + "dustcrystalcut", 1);
        }
        addRecipe(recipes, "dustcrystal", "dustcrystalcut", 1);
        RegistryObject<Item> scrap = RWBYMItems.SIMPLE_ITEMS.get("scrap");
        if (scrap != null) {
            recipes.put(scrap.get(), new ItemStack(Items.IRON_NUGGET, 9));
        }
        return Map.copyOf(recipes);
    }

    private static void addRecipe(Map<Item, ItemStack> recipes, String inputName, String outputName, int outputCount) {
        RegistryObject<Item> input = RWBYMItems.SIMPLE_ITEMS.get(inputName);
        RegistryObject<Item> output = RWBYMItems.SIMPLE_ITEMS.get(outputName);
        if (input != null && output != null) {
            recipes.put(input.get(), new ItemStack(output.get(), outputCount));
        }
    }

    private static boolean isCrusherTool(ItemStack stack) {
        String path = idPath(stack);
        return path.equals("crush") || path.equals("chisel");
    }

    private static String idPath(ItemStack stack) {
        if (stack.isEmpty()) {
            return "";
        }
        return net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath();
    }
}
