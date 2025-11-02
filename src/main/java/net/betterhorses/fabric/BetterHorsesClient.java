package net.betterhorses.fabric;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.betterhorses.common.config.BetterHorsesConfig;
import net.betterhorses.fabric.platform.events.FabricPlatformClientEvents;
import net.fabricmc.api.ClientModInitializer;

public class BetterHorsesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AutoConfig.register(BetterHorsesConfig.class, GsonConfigSerializer::new);
        
        FabricPlatformClientEvents.init();
    }
}
