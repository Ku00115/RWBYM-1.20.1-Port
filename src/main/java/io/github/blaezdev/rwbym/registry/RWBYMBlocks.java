package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.block.RWBYMInteractiveBlock;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RWBYM.MOD_ID);
    public static final Map<String, RegistryObject<Block>> BLOCKS_BY_NAME = new LinkedHashMap<>();

    private static final String[] BLOCK_NAMES = {
            "bait",
            "crush",
            "crusher",
            "fireblock",
            "fireore",
            "fluidgrimm",
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
            return new FenceBlock(stoneProperties(name).strength(2.0F, 6.0F));
        }
        if ("bait".equals(name) || "crusher".equals(name) || "crush".equals(name) || "toolkit".equals(name)) {
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

        if ("lantern".equals(name) || name.endsWith("block")) {
            properties.lightLevel(state -> "lantern".equals(name) ? 14 : 0);
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
        if ("lantern".equals(name)) {
            return SoundType.LANTERN;
        }
        if ("hrdltfence".equals(name)) {
            return SoundType.NETHER_BRICKS;
        }
        return SoundType.STONE;
    }

    private static float strength(String name) {
        if (name.endsWith("ore")) {
            return 3.0F;
        }
        if (name.endsWith("block")) {
            return 5.0F;
        }
        if ("lantern".equals(name) || "bait".equals(name)) {
            return 0.3F;
        }
        return 2.0F;
    }

    private static float resistance(String name) {
        if (name.endsWith("block")) {
            return 6.0F;
        }
        return 3.0F;
    }

    private static boolean requiresTool(String name) {
        return name.endsWith("ore") || name.endsWith("block") || "crusher".equals(name) || "crush".equals(name);
    }

    private RWBYMBlocks() {
    }
}
