package net.betterhorses.fabric.platform.events;

import net.betterhorses.common.ui.HorseStatsHud;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.passive.HorseEntity;

public class FabricPlatformClientEvents {

    public static void init() {
        initHudRenderEvent();
    }

    private static void initHudRenderEvent() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            renderHud(drawContext);
        });
    }

    private static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || !client.player.hasVehicle() || !(client.player.getVehicle() instanceof HorseEntity horse)) {
            return;
        }

        HorseStatsHud.render(context, client.textRenderer, horse);
    }
}