package com.buuz135.luckymining;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;


public class BreakBlockEventSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private final Random random = new Random();
    public static HashMap<UUID, LuckyMiningInfo> luckyMiningInfo = new HashMap<>();
    private final Config<LMConfig> config;

    public BreakBlockEventSystem(Config<LMConfig> config) {
        super(BreakBlockEvent.class);
        this.config = config;
    }

    @Override
    public void handle(final int index, @Nonnull final ArchetypeChunk<EntityStore> archetypeChunk, @Nonnull final Store<EntityStore> store, @Nonnull final CommandBuffer<EntityStore> commandBuffer, @Nonnull final BreakBlockEvent event) {
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        Player player = store.getComponent(ref, Player.getComponentType());
        var block = event.getBlockType().getId();
        if (block.equals("Empty")) return;
        var uuid = store.getComponent(ref, PlayerRef.getComponentType()).getUuid();
        if (luckyMiningInfo.containsKey(uuid)) {
            if ((System.currentTimeMillis() > (luckyMiningInfo.get(uuid).lastBreak + (this.config.get().getMaxTime() * 1000L))) || !luckyMiningInfo.get(uuid).block.equals(block)) {
                luckyMiningInfo.remove(uuid);
            }
        }
        for (String whitelistOre : this.config.get().getWhitelistOres()) {
            if (block.contains(whitelistOre)) {

                if (luckyMiningInfo.containsKey(uuid) && random.nextDouble() < luckyMiningInfo.get(uuid).chance) {
                    var checking = new Vector3d[]{new Vector3d(1,0,0), new Vector3d(-1,0,0), new Vector3d(0,0,1), new Vector3d(0,0,-1), new Vector3d(0,1,0), new Vector3d(0,-1,0)};
                    var positions = new ArrayList<Vector3d>();

                    for (var direction : checking) {
                        var blockType = player.getWorld().getBlockType((int) (direction.x + event.getTargetBlock().x), (int) (direction.y + event.getTargetBlock().y), (int) (direction.z + event.getTargetBlock().z));
                        if (Arrays.asList(this.config.get().getWhitelistReplaceBlocks()).stream().anyMatch(s -> blockType.getId().contains(s))
                                && anyAirBlockOrOre(player.getWorld(), (int) (direction.x + event.getTargetBlock().x), (int) (direction.y + event.getTargetBlock().y), (int) (direction.z + event.getTargetBlock().z), block)) {
                            positions.add(new Vector3d(direction.x + event.getTargetBlock().x, direction.y + event.getTargetBlock().y, direction.z + event.getTargetBlock().z));
                        }
                    }

                    if (positions.isEmpty()) {
                        for (int x = -1; x < 1; x++) {
                            for (int y = -1; y < 1; y++) {
                                for (int z = -1; z < 1; z++) {
                                    var blockType = player.getWorld().getBlockType(x + event.getTargetBlock().x, y + event.getTargetBlock().y, z + event.getTargetBlock().z);
                                    if (Arrays.asList(this.config.get().getWhitelistReplaceBlocks()).stream().anyMatch(s -> blockType.getId().contains(s))
                                            && anyAirBlockOrOre(player.getWorld(), x + event.getTargetBlock().x, y + event.getTargetBlock().y, z + event.getTargetBlock().z, block)) {
                                        positions.add(new Vector3d(x + event.getTargetBlock().x, y + event.getTargetBlock().y, z + event.getTargetBlock().z));
                                    }
                                }
                            }
                        }
                    }

                    if (!positions.isEmpty()){
                        var randomPosition = positions.get(random.nextInt(positions.size()));
                        player.getWorld().setBlock((int) randomPosition.x, (int) randomPosition.y, (int) randomPosition.z, block);
                        for (double i = 0; i <= 1; i += 0.1) {
                            for (double j = 0; j <= 1; j += 0.1) {
                                ParticleUtil.spawnParticleEffect("Buuz135_LuckyMining_Spawn", new Vector3d(randomPosition.x + i, randomPosition.y, randomPosition.z + j), store);
                                ParticleUtil.spawnParticleEffect("Buuz135_LuckyMining_Spawn_Still", new Vector3d(randomPosition.x + i, randomPosition.y + 1, randomPosition.z + j), store);
                            }
                        }
                        SoundUtil.playSoundEvent2dToPlayer(store.getComponent(ref, PlayerRef.getComponentType()), SoundEvent.getAssetMap().getIndex("SFX_Buuz135_LuckyMining_Lucky"), SoundCategory.SFX, 3, 1);
                    }
                    luckyMiningInfo.get(uuid).lastBreak = System.currentTimeMillis();
                }

                if (luckyMiningInfo.containsKey(uuid)) {
                    luckyMiningInfo.get(uuid).chance += this.config.get().getLuckIncreaseChance();
                } else {
                    luckyMiningInfo.put(uuid, new LuckyMiningInfo(block, this.config.get().getLuckStartChance(), System.currentTimeMillis()));
                }
                break;
            }
        }
    }

    public boolean anyAirBlockOrOre(World world, int x, int y, int z, String ore){
        return checkAirBlockOrOre(world, x + 1, y, z, ore)
                || checkAirBlockOrOre(world, x - 1, y, z, ore)
                || checkAirBlockOrOre(world, x, y + 1, z, ore)
                || checkAirBlockOrOre(world, x, y - 1, z, ore)
                || checkAirBlockOrOre(world, x, y, z + 1, ore)
                || checkAirBlockOrOre(world, x, y, z - 1, ore);
    }

    public boolean checkAirBlockOrOre(World world, int x, int y, int z, String ore){
        return world.getBlockType(x, y, z).getId().equals("Empty") || world.getBlockType(x, y, z).getId().equals(ore);
    }

    @Nullable
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }

    public static class LuckyMiningInfo {

        private String block;
        private double chance;
        private long lastBreak;

        public LuckyMiningInfo(String block, double chance, long lastBreak) {
            this.block = block;
            this.chance = chance;
            this.lastBreak = lastBreak;
        }

    }
}
