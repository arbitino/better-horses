package net.betterhorses;

import net.betterhorses.network.BetterHorsesPayloads;
import net.betterhorses.breed.BreedEvents;
import net.betterhorses.breed.BreedRegistry;
import net.betterhorses.progress.ProgressEvents;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterHorses implements ModInitializer {
    public static final String MOD_ID = "betterhorses";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BreedRegistry.init();
        BreedEvents.init();

        ProgressEvents.init();

        BetterHorsesPayloads.register();
    }
}
