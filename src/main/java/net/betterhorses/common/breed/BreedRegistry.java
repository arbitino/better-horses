package net.betterhorses.common.breed;
import net.betterhorses.common.BetterHorses;
import net.betterhorses.common.util.MathUtils;

import java.util.*;

public class BreedRegistry {
    private static final Map<String, Breed> REGISTRY = new HashMap<>();

    private static final Random RANDOM = new Random();

    private static final Breed ARABIAN = new Breed("arabian", "Арабская", MathUtils.blocksToJumpAttribute(5.0), MathUtils.blocksPerSecondToMoveAttribute(15.0), Breed.ObedienceLevel.VERY_OBEDIENT, 1.2f, 0.8f);
    private static final Breed MUSTANG = new Breed("mustang", "Мустанг", MathUtils.blocksToJumpAttribute(4.0), MathUtils.blocksPerSecondToMoveAttribute(13.0), Breed.ObedienceLevel.DISOBEDIENT, 0.9f, 1.3f);
    private static final Breed DEFAULT = new Breed("default", "Обычная", MathUtils.blocksToJumpAttribute(3.5), MathUtils.blocksPerSecondToMoveAttribute(10.5), Breed.ObedienceLevel.NORMAL, 1.0f, 1.0f);

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
