package net.betterhorses.client;

import net.betterhorses.BetterHorses;
import net.betterhorses.breed.BreedableHorse;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BetterHorsesClient implements ClientModInitializer {
    private static final Identifier STATS_LAYER = Identifier.of(BetterHorses.MOD_ID, "hud_stats_layer");

    @Override
    public void onInitializeClient() {
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.CHAT, STATS_LAYER, BetterHorsesClient::render));
    }

    private static void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null && client.player.hasVehicle() && client.player.getVehicle() instanceof HorseEntity horse) {
            TextRenderer textRenderer = client.textRenderer;

            int x = 10;
            int y = 10;

            double speed = Math.round(horse.getMovementSpeed() * 10.0 * 100.0) / 100.0;
            double jumpHeight = getJumpHeight(horse.getAttributeInstance(EntityAttributes.JUMP_STRENGTH).getValue());

            var breed = ((BreedableHorse) horse).getHorseBreed();

            context.drawText(textRenderer, Text.literal("Скорость: " + speed + " б/с"), x, y, 0xFFFFFF, false);
            context.drawText(textRenderer, Text.literal("Прыжок: " + jumpHeight + " б"), x, y + 10, 0xFFFFFF, false);
            context.drawText(textRenderer, Text.literal("Парода : " + breed.displayName()), x, y + 20, 0xFFFFFF, false);
        }
    }

    private static double getJumpHeight(double jumpStrength) {
        return Math.round((-0.1817584952 * jumpStrength * jumpStrength + 3.689713992 * jumpStrength + 0.4842167484) * 100.0) / 100.0;
    }
}
