package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.block.CrusherBlock;
import io.github.blaezdev.rwbym.block.GrimmFluidBlock;
import io.github.blaezdev.rwbym.block.RWBYMInteractiveBlock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RWBYM.MOD_ID);
    public static final Map<String, RegistryObject<Block>> BLOCKS_BY_NAME = new LinkedHashMap<>();
    private static final Set<String> NOT_FULL_BLOCKS = Set.of(
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
            "viridianironblock");
    private static final Set<String> DUST_CASE_BLOCKS = Set.of(
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
            "viridianironblock");
    public static final RegistryObject<LiquidBlock> GRIMM_FLUID_BLOCK =
            BLOCKS.register("fluidgrimm", () -> new GrimmFluidBlock(RWBYMFluids.GRIMM,
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLACK)
                            .replaceable()
                            .noCollission()
                            .strength(100.0F)
                            .noLootTable()));

    private static final String[] BLOCK_NAMES = {
            "bait",
            "crusher",
            "fireblock",
            "fireore",
            "forestironblock",
            "frostedironblock",
            "gildedironblock",
            "gravityblock",
            "gravityore",
            "hrdltfence",
            "iceblock",
            "iceore",
            "impureblock",
            "impureore",
            "lantern",
            "lightblock",
            "lightore",
            "roseironblock",
            "shadowironblock",
            "smrgrave",
            "toolkit",
            "viridianironblock",
            "waterblock",
            "waterore",
            "windblock",
            "windore"
    };

    static {
        for (String name : BLOCK_NAMES) {
            BLOCKS_BY_NAME.put(name, BLOCKS.register(name, () -> createBlock(name)));
        }
    }

    private static Block createBlock(String name) {
        if ("hrdltfence".equals(name)) {
            return new FenceBlock(stoneProperties(name));
        }
        if ("crusher".equals(name)) {
            return new CrusherBlock(stoneProperties(name));
        }
        if ("bait".equals(name) || "toolkit".equals(name)) {
            return new RWBYMInteractiveBlock(name, stoneProperties(name));
        }
        return new Block(stoneProperties(name));
    }

    private static BlockBehaviour.Properties stoneProperties(String name) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(mapColor(name))
                .sound(soundType(name))
                .strength(strength(name), resistance(name));

        if (requiresTool(name)) {
            properties.requiresCorrectToolForDrops();
        }

        if ("lantern".equals(name)) {
            // Original RWBYLantern used setLightLevel(1.0F), which maps to full block light in modern MC.
            properties.lightLevel(state -> "lantern".equals(name) ? 15 : 0);
        }
        if (isNotFullBlock(name)) {
            // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
            // Legacy RWBYNotFullBlock-style blocks were translucent/non-full and should not cull neighbors.
            properties.noOcclusion();
        }

        return properties;
    }

    private static MapColor mapColor(String name) {
        if (name.contains("fire")) {
            return MapColor.COLOR_RED;
        }
        if (name.contains("ice") || name.contains("water") || name.contains("frost")) {
            return MapColor.ICE;
        }
        if (name.contains("gravity") || name.contains("shadow") || name.contains("grimm")) {
            return MapColor.COLOR_BLACK;
        }
        if (name.contains("light") || name.contains("gilded")) {
            return MapColor.GOLD;
        }
        if (name.contains("forest") || name.contains("viridian") || name.contains("wind")) {
            return MapColor.COLOR_GREEN;
        }
        if (name.contains("rose")) {
            return MapColor.COLOR_PINK;
        }
        return MapColor.STONE;
    }

    private static SoundType soundType(String name) {
        if ("crusher".equals(name)) {
            return SoundType.METAL;
        }
        if ("lantern".equals(name)) {
            return SoundType.LANTERN;
        }
        if (DUST_CASE_BLOCKS.contains(name) || "bait".equals(name) || "hrdltfence".equals(name)) {
            return SoundType.GLASS;
        }
        return SoundType.STONE;
    }

    private static float strength(String name) {
        if (name.endsWith("ore")) {
            return 5.0F;
        }
        if ("bait".equals(name) || "hrdltfence".equals(name)) {
            return 2.5F;
        }
        if ("crusher".equals(name)) {
            return 1.0F;
        }
        if (DUST_CASE_BLOCKS.contains(name) || "lantern".equals(name)) {
            return 0.0F;
        }
        if ("smrgrave".equals(name) || "toolkit".equals(name)) {
            return 5.0F;
        }
        if (name.endsWith("block")) {
            return 5.0F;
        }
        return 2.0F;
    }

    private static float resistance(String name) {
        if (name.endsWith("ore")) {
            return 0.0F;
        }
        if ("bait".equals(name) || "hrdltfence".equals(name)) {
            return 45.0F;
        }
        if ("smrgrave".equals(name) || "toolkit".equals(name)) {
            return 15.0F;
        }
        if ("crusher".equals(name) || DUST_CASE_BLOCKS.contains(name) || "lantern".equals(name)) {
            return 1.0F;
        }
        if (name.endsWith("block")) {
            return 6.0F;
        }
        return 3.0F;
    }

    private static boolean requiresTool(String name) {
        return name.endsWith("ore") || "smrgrave".equals(name) || "crusher".equals(name);
    }

    private static boolean isNotFullBlock(String name) {
        return NOT_FULL_BLOCKS.contains(name)
                || "bait".equals(name)
                || "crusher".equals(name)
                || "toolkit".equals(name)
                || "lantern".equals(name);
    }

    private RWBYMBlocks() {
    }
}
