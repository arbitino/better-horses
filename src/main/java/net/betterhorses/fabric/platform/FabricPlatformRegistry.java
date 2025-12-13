package net.betterhorses.fabric.platform;

import net.betterhorses.fabric.data.BreedDataLoader;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class FabricPlatformRegistry {
    public static void init() {
        ResourceManagerHelper
            .get(ResourceType.SERVER_DATA)
            .registerReloadListener(new BreedDataLoader());
    }
}
