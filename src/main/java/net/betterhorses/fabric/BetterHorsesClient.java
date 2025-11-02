package net.betterhorses.fabric;

import net.betterhorses.fabric.platform.events.FabricPlatformClientEvents;
import net.fabricmc.api.ClientModInitializer;

public class BetterHorsesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FabricPlatformClientEvents.init();
    }
}
