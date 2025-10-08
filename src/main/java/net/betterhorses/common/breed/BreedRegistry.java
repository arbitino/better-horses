package net.betterhorses.common.breed;
import net.betterhorses.common.BetterHorses;

import java.util.*;

public class BreedRegistry {
    private static final Map<String, Breed> REGISTRY = new HashMap<>();

    private static final Random RANDOM = new Random();

    private static final Breed ARABIAN = new Breed("arabian", "Арабская", 1.2f, 0.35f, Breed.ObedienceLevel.VERY_OBEDIENT);
    private static final Breed MUSTANG = new Breed("mustang", "Мустанг", 1.0f, 0.3f, Breed.ObedienceLevel.DISOBEDIENT);
    private static final Breed DEFAULT = new Breed("default", "Обычная", 0.9f, 0.25f, Breed.ObedienceLevel.NORMAL);

    public static void register(Breed breed) {
        REGISTRY.put(breed.id(), breed);
    }

    public static Breed get(String id) {
        return REGISTRY.get(id);
    }

    public static Breed getDefault() {
        return REGISTRY.get("default");
    }

    public static Breed getRandomBreed() {
        List<Breed> breeds = new ArrayList<>(REGISTRY.values());
        BetterHorses.LOGGER.info("{} breeds found", breeds.size());
        if (breeds.isEmpty()) return getDefault();

        Breed breed = breeds.get(RANDOM.nextInt(breeds.size()));

        BetterHorses.LOGGER.info("{} selected breed", breed.displayName());

        return breed;
    }

    public static void init() {
        register(ARABIAN);
        register(MUSTANG);
        register(DEFAULT);
    }
}
