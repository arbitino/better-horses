package net.betterhorses.fabric;

import net.betterhorses.common.BetterHorses;
import net.betterhorses.common.accessor.jump.JumpingHeightAccessor;
import net.betterhorses.common.accessor.speed.MoveSpeedAccessor;
import net.betterhorses.common.breed.Breed;
import net.betterhorses.common.breed.BreedableHorse;
import net.betterhorses.common.progress.Progress;
import net.betterhorses.common.progress.ProgressableHorse;
import net.betterhorses.common.progress.HorseAttributeProgression;
import net.betterhorses.common.util.MathUtils;
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

            double speed = accessor.getMoveSpeed();
            double jumpHeight = jAccessor.getJumpStrength();

            double speedInBlocks = accessor.getMoveSpeedInBlocks();
            double jumpHeightInBlocks = jAccessor.getJumpHeightInBlocks();

            Breed breed = ((BreedableHorse) horse).getHorseBreed();
            Progress progress = ((ProgressableHorse) horse).getProgress();

            context.drawText(textRenderer, Text.literal("Скорость: " + MathUtils.round2digits(speed) + " б/с"), x, y, 0xFFFFFF, false);
            context.drawText(textRenderer, Text.literal("Скорость б\\c: " + MathUtils.round2digits(speedInBlocks) + " б/с"), x, y + 10, 0xFFFFFF, false);
            context.drawText(textRenderer, Text.literal("Прыжок: " + MathUtils.round2digits(jumpHeight) + " б"), x, y + 20, 0xFFFFFF, false);
            context.drawText(textRenderer, Text.literal("Прыжок б\\c: " + MathUtils.round2digits(jumpHeightInBlocks) + " б"), x, y + 30, 0xFFFFFF, false);
            context.drawText(textRenderer, Text.literal("Порода: " + breed.displayName()), x, y + 40, 0xFFFFFF, false);

            // Новая уровневая система
            int speedLevel = HorseAttributeProgression.getSpeedLevel(horse);
            double speedProgress = HorseAttributeProgression.getSpeedProgress(horse);
            int jumpLevel = HorseAttributeProgression.getJumpLevel(horse);
            double jumpProgress = HorseAttributeProgression.getJumpProgress(horse);

            // Оставшееся до следующего уровня
            double remainingDistance = HorseAttributeProgression.getRemainingDistanceForNextSpeedLevel(horse);
            double remainingJumps = HorseAttributeProgression.getRemainingJumpsForNextJumpLevel(horse);

            // Максимальные значения для отладки
            double maxSpeed = breed.maxSpeed();
            double maxJump = breed.maxJumpHeight();
            double originalSpeed = HorseAttributeProgression.getOriginalSpeed(horse);
            double originalJump = HorseAttributeProgression.getOriginalJump(horse);

            // Вычисляем теоретические уровни на основе текущих характеристик
            double speedRatio = speed / maxSpeed;
            double jumpRatio = jumpHeight / maxJump;
            int theoreticalSpeedLevel = (int) Math.round(speedRatio * 9.0 + 1.0);
            int theoreticalJumpLevel = (int) Math.round(jumpRatio * 9.0 + 1.0);

            context.drawText(textRenderer, Text.literal("Уровень скорости: " + speedLevel + "/10 (" + MathUtils.round2digits(speedProgress * 100) + "%)"), x, y + 60, 0x00FF00, false);
            context.drawText(textRenderer, Text.literal("Уровень прыжков: " + jumpLevel + "/10 (" + MathUtils.round2digits(jumpProgress * 100) + "%)"), x, y + 70, 0x00FF00, false);

            // Отображение оставшегося до следующего уровня
            if (speedLevel < 10) {
                context.drawText(textRenderer, Text.literal("До ур. скорости " + (speedLevel + 1) + ": " + (int)remainingDistance + " блоков"), x, y + 80, 0x88FF88, false);
            } else {
                context.drawText(textRenderer, Text.literal("Скорость: МАКС УРОВЕНЬ"), x, y + 80, 0xFFD700, false);
            }

            if (jumpLevel < 10) {
                context.drawText(textRenderer, Text.literal("До ур. прыжков " + (jumpLevel + 1) + ": " + (int)remainingJumps + " прыжков"), x, y + 90, 0x88FF88, false);
            } else {
                context.drawText(textRenderer, Text.literal("Прыжки: МАКС УРОВЕНЬ"), x, y + 90, 0xFFD700, false);
            }

            // Отладочная информация
            context.drawText(textRenderer, Text.literal("Макс. скорость: " + MathUtils.round2digits(maxSpeed)), x, y + 110, 0xFFFF00, false);
            context.drawText(textRenderer, Text.literal("Макс. прыжок: " + MathUtils.round2digits(maxJump)), x, y + 120, 0xFFFF00, false);
            context.drawText(textRenderer, Text.literal("Теор. ур. скорости: " + theoreticalSpeedLevel + " (ratio: " + MathUtils.round2digits(speedRatio) + ")"), x, y + 130, 0x00FFFF, false);
            context.drawText(textRenderer, Text.literal("Теор. ур. прыжков: " + theoreticalJumpLevel + " (ratio: " + MathUtils.round2digits(jumpRatio) + ")"), x, y + 140, 0x00FFFF, false);
            context.drawText(textRenderer, Text.literal("Ориг. скорость: " + MathUtils.round2digits(originalSpeed)), x, y + 150, 0xCCCCCC, false);
            context.drawText(textRenderer, Text.literal("Ориг. прыжок: " + MathUtils.round2digits(originalJump)), x, y + 160, 0xCCCCCC, false);

            // Общий прогресс (для отладки)
            context.drawText(textRenderer, Text.literal("Общая дистанция: " + progress.getRunningDistance()), x, y + 180, 0x888888, false);
            context.drawText(textRenderer, Text.literal("Общие прыжки: " + progress.getJumpCount()), x, y + 190, 0x888888, false);
        }
    }
}
