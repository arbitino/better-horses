package net.betterhorses.common.breed;

import net.betterhorses.common.BetterHorses;
import net.minecraft.entity.passive.HorseEntity;

public class BreedUtils {
    public static void assignBreedIfMissing(HorseEntity horse) {
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
