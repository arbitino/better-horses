package net.betterhorses.breed;

import net.betterhorses.BetterHorses;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.passive.HorseEntity;

public class BreedEvents {
    public static void init() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof HorseEntity horse) {
                assignBreedIfMissing(horse);
            }
        });
    }

    private static void assignBreedIfMissing(HorseEntity horse) {
        if (horse instanceof BreedableHorse breedableHorse) {
            var breed = breedableHorse.getHorseBreed();

            if (breed == null) {
                Breed defaultBreed = BreedRegistry.get("default");

                if (defaultBreed != null) {
                    breedableHorse.setHorseBreed(defaultBreed);
                    BetterHorses.LOGGER.info("Установлена порода для существующей лошади: {} {}", horse.getUuid(), breedableHorse.getHorseBreed());
                }
            } else {
                BetterHorses.LOGGER.info("Существующая парода {} у лошади {}", breed.id(), horse.getUuid());
            }
        }
    }
}
