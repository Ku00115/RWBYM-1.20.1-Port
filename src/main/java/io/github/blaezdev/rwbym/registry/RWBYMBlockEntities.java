package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.block.entity.CrusherBlockEntity;
import io.github.blaezdev.rwbym.block.entity.GrimmBaitBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, RWBYM.MOD_ID);

    public static final RegistryObject<BlockEntityType<CrusherBlockEntity>> CRUSHER =
            BLOCK_ENTITY_TYPES.register("crusher", () -> BlockEntityType.Builder
                    .of(CrusherBlockEntity::new, RWBYMBlocks.BLOCKS_BY_NAME.get("crusher").get())
                    .build(null));

    public static final RegistryObject<BlockEntityType<GrimmBaitBlockEntity>> GRIMM_BAIT =
            BLOCK_ENTITY_TYPES.register("grimm_bait", () -> BlockEntityType.Builder
                    .of(GrimmBaitBlockEntity::new, RWBYMBlocks.BLOCKS_BY_NAME.get("bait").get())
                    .build(null));

    private RWBYMBlockEntities() {
    }
}
