package io.github.blaezdev.rwbym.block.entity;

import io.github.blaezdev.rwbym.item.RWBYMWeaponModifierHelper;
import io.github.blaezdev.rwbym.registry.RWBYMBlockEntities;
import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import io.github.blaezdev.rwbym.registry.RWBYMItems;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public class GrimmBaitBlockEntity extends BlockEntity {
    private static final int BOSS_FREQUENCY = 3;
    private static final int SPAWN_RADIUS = 10;
    private static final int MAX_WAVE = 18;

    private int waveCount;
    private UUID playerId;
    private boolean stopping;
    private final List<UUID> currentWave = new ArrayList<>();
    private final List<UUID> alive = new ArrayList<>();
    private final List<ItemStack> rewards = new ArrayList<>();

    public GrimmBaitBlockEntity(BlockPos pos, BlockState state) {
        super(RWBYMBlockEntities.GRIMM_BAIT.get(), pos, state);
    }

    public void activate(ServerPlayer player) {
        if (isActive() || this.stopping || this.level == null || this.level.getDifficulty().getId() == 0) {
            return;
        }
        // Reward ejection is a terminal phase in the original tile entity, so only a fresh inactive bait may start.
        this.playerId = player.getUUID();
        this.waveCount = 0;
        this.stopping = false;
        this.currentWave.clear();
        this.alive.clear();
        this.rewards.clear();
        setChanged();
    }

    public boolean isActive() {
        return this.waveCount > 0;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GrimmBaitBlockEntity bait) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (bait.isActive()) {
            bait.spawnActiveSmoke(serverLevel, pos);
        }
        boolean changed = false;
        if (bait.stopping) {
            changed = bait.dropRewardTick(serverLevel, pos);
        } else {
            changed = bait.waveTick(serverLevel, pos);
        }
        if (changed) {
            setChanged(level, pos, state);
        }
    }

    private void spawnActiveSmoke(ServerLevel level, BlockPos pos) {
        if (level.getGameTime() % 4L != 0L) {
            return;
        }
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // The 1.12 block used client randomDisplayTick; server particles avoid a custom active-state sync packet.
        double x = pos.getX() + level.random.nextDouble() * 0.10000000149011612D;
        double y = pos.getY() + level.random.nextDouble();
        double z = pos.getZ() + level.random.nextDouble();
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private boolean waveTick(ServerLevel level, BlockPos pos) {
        if (this.playerId == null) {
            if (this.waveCount == 0 && this.currentWave.isEmpty() && this.alive.isEmpty()) {
                return false;
            }
            this.waveCount = 0;
            this.currentWave.clear();
            this.alive.clear();
            return true;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(this.playerId);
        pruneDeadEntities(level);
        if (player == null) {
            // Original TileEntityRWBYGrimmBait reset silently when its owner reference disappeared.
            this.waveCount = 0;
            this.playerId = null;
            this.currentWave.clear();
            this.alive.clear();
            return true;
        }
        if (!player.isAlive() || isCleared()) {
            beginStopping(level);
            return true;
        }
        if (this.waveCount < MAX_WAVE && (this.currentWave.isEmpty() || alivePercent(level) < 0.4F)) {
            this.waveCount++;
            this.currentWave.clear();
            spawnWave(level, pos);
            level.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 0.9F, 0.65F);
            return true;
        }
        return false;
    }

    private boolean dropRewardTick(ServerLevel level, BlockPos pos) {
        if (!this.rewards.isEmpty()) {
            ItemStack reward = this.rewards.remove(0);
            net.minecraft.world.entity.item.ItemEntity item =
                    new net.minecraft.world.entity.item.ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.0D,
                            pos.getZ() + 0.5D, reward);
            // Original TileEntityRWBYGrimmBait ejected one queued reward per server tick with a small upward pop.
            item.setDeltaMovement(level.random.nextGaussian() * 0.1D, 0.5D, level.random.nextGaussian() * 0.1D);
            level.addFreshEntity(item);
            return true;
        }
        level.destroyBlock(pos, false);
        return true;
    }

    private void beginStopping(ServerLevel level) {
        int completedWaveCount = this.waveCount;
        this.stopping = true;
        this.playerId = null;
        this.currentWave.clear();
        this.alive.clear();
        this.rewards.clear();
        buildRewards(level, completedWaveCount);
        // Original stop() queued rewards, then reset wavecount before the block ejected them.
        this.waveCount = 0;
    }

    private void spawnWave(ServerLevel level, BlockPos pos) {
        int entityCount = entityCount();
        EntityType<?> type = grimmType(entityTypeIndex(), level);
        for (int i = 0; i < entityCount; i++) {
            BlockPos spawnPos = topSpawnPos(level, pos.offset(
                    level.random.nextInt(SPAWN_RADIUS * 2 + 1) - SPAWN_RADIUS,
                    0,
                    level.random.nextInt(SPAWN_RADIUS * 2 + 1) - SPAWN_RADIUS));
            Entity entity = type.spawn(level, spawnPos, MobSpawnType.TRIGGERED);
            if (entity instanceof Mob mob) {
                this.currentWave.add(mob.getUUID());
                this.alive.add(mob.getUUID());
            }
        }
    }

    private BlockPos topSpawnPos(ServerLevel level, BlockPos pos) {
        BlockPos top = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        return top.getY() <= level.getMinBuildHeight() ? pos.above() : top;
    }

    private int entityCount() {
        return this.waveCount % BOSS_FREQUENCY == 0 ? (this.waveCount - 1) / (BOSS_FREQUENCY * 6) + 1 : 10;
    }

    private int entityTypeIndex() {
        return this.waveCount % BOSS_FREQUENCY == 0
                ? (this.waveCount / BOSS_FREQUENCY > 6 ? 15 : this.waveCount / BOSS_FREQUENCY + 9)
                : (this.waveCount - this.waveCount / BOSS_FREQUENCY > 9 ? 9 : this.waveCount - this.waveCount / BOSS_FREQUENCY);
    }

    private EntityType<?> grimmType(int difficulty, ServerLevel level) {
        return switch (difficulty) {
            case 1 -> RWBYMEntityTypes.BOARBATUSK.get();
            case 2 -> RWBYMEntityTypes.BEOWOLF.get();
            case 3 -> RWBYMEntityTypes.URSA.get();
            case 4 -> RWBYMEntityTypes.LANCER.get();
            case 5 -> RWBYMEntityTypes.GEIST.get();
            case 6 -> RWBYMEntityTypes.APATHY.get();
            case 7 -> RWBYMEntityTypes.CREEP.get();
            case 8 -> RWBYMEntityTypes.TINY_DEATHSTALKER.get();
            case 9 -> RWBYMEntityTypes.NEVERMORE.get();
            case 10 -> RWBYMEntityTypes.DEATHSTALKER.get();
            case 11 -> level.random.nextBoolean() ? RWBYMEntityTypes.GIANT_NEVERMORE.get() : RWBYMEntityTypes.WYVERN.get();
            case 12 -> RWBYMEntityTypes.MUTANT_DEATHSTALKER.get();
            case 13 -> RWBYMEntityTypes.QUEEN_LANCER.get();
            case 14 -> RWBYMEntityTypes.NUCKLEEVE.get();
            case 15 -> RWBYMEntityTypes.GOLIATH.get();
            default -> RWBYMEntityTypes.BEOWOLF.get();
        };
    }

    private float alivePercent(ServerLevel level) {
        if (this.currentWave.isEmpty()) {
            return 0.0F;
        }
        int living = 0;
        for (UUID id : this.currentWave) {
            Entity entity = level.getEntity(id);
            if (entity instanceof LivingEntity livingEntity && livingEntity.isAlive()) {
                living++;
            }
        }
        return (float) living / (float) this.currentWave.size();
    }

    private boolean isCleared() {
        return this.waveCount >= MAX_WAVE && this.alive.isEmpty();
    }

    private void pruneDeadEntities(ServerLevel level) {
        Iterator<UUID> iterator = this.alive.iterator();
        while (iterator.hasNext()) {
            Entity entity = level.getEntity(iterator.next());
            if (!(entity instanceof LivingEntity livingEntity) || !livingEntity.isAlive()) {
                iterator.remove();
            }
        }
    }

    private void buildRewards(ServerLevel level, int completedWaveCount) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Legacy stop() used separate <17 and >17 branches; wave 17 intentionally queues no reward.
        if (completedWaveCount == 17) {
            return;
        }
        int commonCount = completedWaveCount > 17 ? level.random.nextInt(100) + 5 : level.random.nextInt(20) + 5;
        for (int i = 0; i < commonCount; i++) {
            this.rewards.add(randomCommonReward(level, completedWaveCount));
        }
        if (completedWaveCount > 17) {
            this.rewards.add(randomRareReward(level));
        }
    }

    private ItemStack randomCommonReward(ServerLevel level, int completedWaveCount) {
        List<ItemStack> choices = new ArrayList<>();
        addReward(choices, "lien50", level.random.nextInt(3) + 1);
        addReward(choices, "lien100", level.random.nextInt(3) + 1);
        addReward(choices, "remnants", level.random.nextInt(3) + 1);
        if (completedWaveCount > 17) {
            addReward(choices, "lien500", 1);
            // Original reward id rwbyblock8 is the modern toolkit block item.
            addBlockReward(choices, "toolkit");
        }
        for (String blockName : new String[] {"waterblock", "windblock", "lightblock", "fireblock", "iceblock", "impureblock", "gravityblock"}) {
            addBlockReward(choices, blockName);
        }
        return choices.isEmpty() ? ItemStack.EMPTY : choices.get(level.random.nextInt(choices.size())).copy();
    }

    private ItemStack randomRareReward(ServerLevel level) {
        List<ItemStack> choices = new ArrayList<>();
        for (String itemName : new String[] {
                "grimmrapier", "grimmscy", "grimmwhip", "noctustraumnormal", "he1", "he2", "he3", "he4", "he5", "he6",
                "extasis", "amesardent", "lichtroze_closedfire", "kyoshifire", "hollowtome", "moonskimmer", "gwai1"
        }) {
            addReward(choices, itemName, 1);
        }
        return choices.isEmpty() ? ItemStack.EMPTY : choices.get(level.random.nextInt(choices.size())).copy();
    }

    private void addReward(List<ItemStack> rewards, String itemName, int count) {
        RegistryObject<Item> item = RWBYMItems.SIMPLE_ITEMS.get(itemName);
        if (item != null) {
            ItemStack stack = new ItemStack(item.get(), count);
            if (count == 1 && shouldUseGeneratedWeaponReward(itemName)) {
                stack = RWBYMWeaponModifierHelper.createGeneratedWeaponStack(stack, this.level.random);
            }
            rewards.add(stack);
        }
    }

    private void addBlockReward(List<ItemStack> rewards, String blockName) {
        RegistryObject<Item> item = RWBYMItems.BLOCK_ITEMS.get(blockName);
        if (item != null) {
            rewards.add(new ItemStack(item.get()));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("WaveCount", this.waveCount);
        tag.putBoolean("Stopping", this.stopping);
        if (this.playerId != null) {
            tag.putUUID("Player", this.playerId);
        }
        putUuidList(tag, "CurrentWave", this.currentWave);
        putUuidList(tag, "Alive", this.alive);
        net.minecraft.core.NonNullList<ItemStack> savedRewards = net.minecraft.core.NonNullList.withSize(this.rewards.size(), ItemStack.EMPTY);
        for (int i = 0; i < this.rewards.size(); i++) {
            savedRewards.set(i, this.rewards.get(i));
        }
        net.minecraft.world.ContainerHelper.saveAllItems(tag, savedRewards, true);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.waveCount = tag.getInt("WaveCount");
        this.stopping = tag.getBoolean("Stopping");
        this.playerId = tag.hasUUID("Player") ? tag.getUUID("Player") : null;
        this.currentWave.clear();
        this.alive.clear();
        readUuidList(tag, "CurrentWave", this.currentWave);
        readUuidList(tag, "Alive", this.alive);
        this.rewards.clear();
        net.minecraft.core.NonNullList<ItemStack> loadedRewards = net.minecraft.core.NonNullList.withSize(tag.getList("Items", 10).size(), ItemStack.EMPTY);
        net.minecraft.world.ContainerHelper.loadAllItems(tag, loadedRewards);
        this.rewards.addAll(loadedRewards.stream().filter(stack -> !stack.isEmpty()).toList());
    }

    private static void putUuidList(CompoundTag tag, String key, List<UUID> ids) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (UUID id : ids) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Id", id);
            list.add(entry);
        }
        tag.put(key, list);
    }

    private static void readUuidList(CompoundTag tag, String key, List<UUID> ids) {
        net.minecraft.nbt.ListTag list = tag.getList(key, 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("Id")) {
                ids.add(entry.getUUID("Id"));
            }
        }
    }

    private static boolean shouldUseGeneratedWeaponReward(String itemName) {
        return itemName.equals("grimmrapier")
                || itemName.equals("grimmscy")
                || itemName.equals("grimmwhip")
                || itemName.equals("noctustraumnormal")
                || itemName.equals("extasis")
                || itemName.equals("amesardent")
                || itemName.equals("lichtroze_closedfire")
                || itemName.equals("kyoshifire")
                || itemName.equals("hollowtome")
                || itemName.equals("moonskimmer")
                || itemName.equals("gwai1");
    }
}
