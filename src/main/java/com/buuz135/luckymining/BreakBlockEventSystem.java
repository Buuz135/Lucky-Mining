package com.buuz135.luckymining;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;


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
        var uuid = player.getUuid();
        if (luckyMiningInfo.containsKey(uuid)) {
            if ((System.currentTimeMillis() > (luckyMiningInfo.get(uuid).lastBreak + (this.config.get().getMaxTime() * 1000L))) || !luckyMiningInfo.get(uuid).block.equals(block)) {
                luckyMiningInfo.remove(uuid);
                //int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Metal_Break");
                //player.playSoundEvent2d(soundEventIndex, SoundCategory.SFX);
            }
        }

        for (String whitelistOre : this.config.get().getWhitelistOres()) {
            if (block.contains(whitelistOre)) {
                if (luckyMiningInfo.containsKey(uuid) && random.nextDouble() < luckyMiningInfo.get(uuid).chance) {
                    //int soundEventIndex = SoundEvent.getAssetMap().getIndex("SFX_Crystal_Break");
                    //player.playSoundEvent2d(soundEventIndex, SoundCategory.SFX);

                    Vector3d position = new Vector3d(event.getTargetBlock().x + 0.5, event.getTargetBlock().y + 0.5, event.getTargetBlock().z + 0.5);
                    ParticleUtils.spawnParticleEffect("Alerted", position, store);
                    new Thread(() -> {
                        try {
                            Thread.sleep(1500);
                            player.getWorld().setBlock(event.getTargetBlock().x, event.getTargetBlock().y, event.getTargetBlock().z, block);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }).start();
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
