package net.betterhorses.fabric.platform;

import net.betterhorses.common.breed.BreedUtils;
import net.betterhorses.common.progress.ProgressUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.passive.HorseEntity;

public class FabricPlatformEvents {
    public static void init() {
        FabricPlatformEvents.initHorseBreedAssignEvent();
        FabricPlatformEvents.initSaveHorseProgressEvent();
    }

    private static void initHorseBreedAssignEvent() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof HorseEntity horse) {
                BreedUtils.assignBreedIfMissing(horse);
            }
        });
    }

    private static void initSaveHorseProgressEvent() {
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            long time = world.getTime();

            world.iterateEntities().forEach(entity -> {
                if (entity instanceof HorseEntity horse) {
                    var player = horse.getFirstPassenger();

                    if (player == null) {
                        return;
                    }

                    ProgressUtils.setHorseJumps(horse);

                    // === Каждую 1 секунду ===
                    if (time % 20 == 0) {
                        ProgressUtils.setHorseDistance(horse);
                    }
                }
            });
        });
    }
}
