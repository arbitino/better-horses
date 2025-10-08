package net.betterhorses.fabric;

import net.betterhorses.fabric.platform.FabricPlatformEvents;
import net.betterhorses.fabric.platform.FabricPlatformRegistry;
import net.fabricmc.api.ModInitializer;

public class BetterHorses implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricPlatformRegistry.init();
        FabricPlatformEvents.init();
    }
}
