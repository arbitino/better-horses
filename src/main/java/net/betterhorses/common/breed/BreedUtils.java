package net.betterhorses.common.breed;

import net.betterhorses.common.BetterHorses;
import net.minecraft.entity.passive.HorseEntity;

public class BreedUtils {
    public static void assignBreedIfMissing(HorseEntity horse) {
        if (horse instanceof BreedableHorse breedableHorse) {
            var breed = breedableHorse.betterHorses$getHorseBreed();

            if (breed == null) {
                Breed randomBreed = BreedRegistry.getRandomBreed();

                if (randomBreed != null) {
                    breedableHorse.betterHorses$setHorseBreed(randomBreed);
                    BetterHorses.LOGGER.info("Установлена случайная порода для новой лошади: {} {}", horse.getUuid(), randomBreed.id());
                }
            } else {
                BetterHorses.LOGGER.info("Существующая порода {} у лошади {}", breed.id(), horse.getUuid());
            }
        }
    }
}
