package net.betterhorses;

import net.betterhorses.accessor.jump.JumpingHeightAccessor;
import net.betterhorses.accessor.speed.MoveSpeedAccessor;
import net.betterhorses.breed.Breed;
import net.betterhorses.breed.BreedableHorse;
import net.betterhorses.progress.Progress;
import net.betterhorses.progress.ProgressableHorse;
import net.betterhorses.util.MathUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
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

            MoveSpeedAccessor accessor = (MoveSpeedAccessor) horse;
            JumpingHeightAccessor jAccessor = (JumpingHeightAccessor) horse;

            double speed = accessor.getMoveSpeedInBlocks();
            double jumpHeight = jAccessor.getJumpHeightInBlocks();

            Breed breed = ((BreedableHorse) horse).getHorseBreed();
            Progress progress = ((ProgressableHorse) horse).getProgress();

            context.drawText(textRenderer, Text.literal("Скорость: " + MathUtils.round2digits(speed) + " б/с"), x, y, 0xFFFFFF, false);
            context.drawText(textRenderer, Text.literal("Прыжок: " + MathUtils.round2digits(jumpHeight) + " б"), x, y + 10, 0xFFFFFF, false);
            context.drawText(textRenderer, Text.literal("Парода : " + breed.displayName()), x, y + 20, 0xFFFFFF, false);
            context.drawText(textRenderer, Text.literal("Прогресс. Дистанция : " + progress.getRunningDistance()), x, y + 30, 0xFFFFFF, false);
            context.drawText(textRenderer, Text.literal("Прогресс. Количество прыжков : " + progress.getJumpCount()), x, y + 40, 0xFFFFFF, false);
        }
    }
}
