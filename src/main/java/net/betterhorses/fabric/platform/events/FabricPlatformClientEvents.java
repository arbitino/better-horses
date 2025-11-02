package net.betterhorses.fabric.platform.events;

import net.betterhorses.common.BetterHorses;
import net.betterhorses.common.ui.HorseStatsHud;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.Identifier;

public class FabricPlatformClientEvents {
    private static final Identifier STATS_LAYER = Identifier.of(BetterHorses.MOD_ID, "hud_stats_layer");

    public static void init() {
        initHudRenderEvent();
    }

    private static void initHudRenderEvent() {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> 
            layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, STATS_LAYER, FabricPlatformClientEvents::renderHud)
        );
    }

    private static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || !client.player.hasVehicle() || !(client.player.getVehicle() instanceof HorseEntity horse)) {
            return;
        }

        HorseStatsHud.render(context, client.textRenderer, horse);
    }
}