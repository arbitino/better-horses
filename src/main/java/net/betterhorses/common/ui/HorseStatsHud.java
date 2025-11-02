package net.betterhorses.common.ui;

import net.betterhorses.common.accessor.jump.JumpingHeightAccessor;
import net.betterhorses.common.accessor.speed.MoveSpeedAccessor;
import net.betterhorses.common.breed.Breed;
import net.betterhorses.common.breed.BreedableHorse;
import net.betterhorses.common.progress.Progress;
import net.betterhorses.common.progress.ProgressableHorse;
import net.betterhorses.common.progress.HorseAttributeProgression;
import net.betterhorses.common.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.text.Text;

public class HorseStatsHud {
    private static float fontScale = 0.5f;
    private static boolean useHumanFriendlyUnits = true;
    private static boolean showBreed = true;
    private static boolean showAttributes = true;
    private static boolean showLevels = true;
    private static boolean showNextLevelInfo = true;
    private static boolean showMaxAttributes = true;
    private static boolean showBaseAttributes = true;
    private static boolean showTotalProgress = true;

    public static void render(DrawContext context, TextRenderer textRenderer, HorseEntity horse) {
        int baseX = (int) (10 / fontScale);
        int baseY = (int) (10 / fontScale);
        int lineHeight = (int) (10 / fontScale);

        context.getMatrices().push();
        context.getMatrices().scale(fontScale, fontScale, 1.0f);

        HorseStats stats = collectHorseStats(horse);
        formatUnitsIfNeeded(stats);
        drawHorseStats(context, textRenderer, stats, baseX, baseY, lineHeight);

        context.getMatrices().pop();
    }

    // ---------- Сбор данных ----------
    private static HorseStats collectHorseStats(HorseEntity horse) {
        HorseStats s = new HorseStats();

        MoveSpeedAccessor speedAcc = (MoveSpeedAccessor) horse;
        JumpingHeightAccessor jumpAcc = (JumpingHeightAccessor) horse;

        s.breed = ((BreedableHorse) horse).getHorseBreed();
        s.progress = ((ProgressableHorse) horse).getProgress();

        s.speed = speedAcc.getMoveSpeed();
        s.jumpStrength = jumpAcc.getJumpStrength();

        s.maxSpeed = s.breed.maxSpeed();
        s.maxJump = s.breed.maxJumpHeight();
        s.originalSpeed = speedAcc.getInitialMoveSpeed();
        s.originalJump = jumpAcc.getInitialJumpStrength();

        s.speedLevel = HorseAttributeProgression.getSpeedLevel(horse);
        s.speedProgress = HorseAttributeProgression.getSpeedProgress(horse);
        s.jumpLevel = HorseAttributeProgression.getJumpLevel(horse);
        s.jumpProgress = HorseAttributeProgression.getJumpProgress(horse);

        s.remainingDistance = HorseAttributeProgression.getRemainingDistanceForNextSpeedLevel(horse);
        s.remainingJumps = HorseAttributeProgression.getRemainingJumpsForNextJumpLevel(horse);

        return s;
    }

    // ---------- Конвертация единиц ----------
    private static void formatUnitsIfNeeded(HorseStats s) {
        s.speedUnit = "";
        s.jumpUnit = "";

        if (!useHumanFriendlyUnits) return;

        s.speed = MathUtils.calcMoveSpeedValueInBlocks(s.speed);
        s.jumpStrength = MathUtils.calcMoveSpeedValueInBlocks(s.jumpStrength);
        s.maxSpeed = MathUtils.calcMoveSpeedValueInBlocks(s.maxSpeed);
        s.maxJump = MathUtils.calcMoveSpeedValueInBlocks(s.maxJump);
        s.originalSpeed = MathUtils.calcMoveSpeedValueInBlocks(s.originalSpeed);
        s.originalJump = MathUtils.calcMoveSpeedValueInBlocks(s.originalJump);

        s.speedUnit = " б/с";
        s.jumpUnit = " б";
    }

    // ---------- Основная отрисовка ----------
    private static void drawHorseStats(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int lineHeight) {
        int line = 0;

        line = drawGroupBreed(context, renderer, s, x, y, line, lineHeight);
        line = drawGroupAttributes(context, renderer, s, x, y, line, lineHeight);
        line = drawGroupLevels(context, renderer, s, x, y, line, lineHeight);
        line = drawGroupNextLevelInfo(context, renderer, s, x, y, line, lineHeight);
        line = drawGroupMaxAttributes(context, renderer, s, x, y, line, lineHeight);
        line = drawGroupBaseAttributes(context, renderer, s, x, y, line, lineHeight);
        drawGroupTotalProgress(context, renderer, s, x, y, line, lineHeight);
    }

    // ---------- Группы ----------

    private static int drawGroupBreed(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight) {
        if (!showBreed) return line;

        context.drawText(renderer, Text.literal("Порода: " + s.breed.displayName()), x, y + lineHeight * line, 0xFFFFFF, false);
        line++;

        return line;
    }

    private static int drawGroupAttributes(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight) {
        if (!showAttributes) return line;

        context.drawText(renderer, Text.literal("Скорость: " + MathUtils.round2digits(s.speed) + s.speedUnit), x, y + lineHeight * line, 0xFFFFFF, false);
        line++;

        context.drawText(renderer, Text.literal("Прыжок: " + MathUtils.round2digits(s.jumpStrength) + s.jumpUnit), x, y + lineHeight * line, 0xFFFFFF, false);
        line++;

        return line;
    }

    private static int drawGroupLevels(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight) {
        if (!showLevels) return line;

        context.drawText(renderer, Text.literal("Уровень скорости: " + s.speedLevel + "/10 (" + MathUtils.round2digits(s.speedProgress * 100) + "%)"), x, y + lineHeight * line, 0x00FF00, false);
        line++;

        context.drawText(renderer, Text.literal("Уровень прыжков: " + s.jumpLevel + "/10 (" + MathUtils.round2digits(s.jumpProgress * 100) + "%)"), x, y + lineHeight * line, 0x00FF00, false);
        line++;

        return line;
    }

    private static int drawGroupNextLevelInfo(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight) {
        if (!showNextLevelInfo) return line;

        if (s.speedLevel < 10) {
            context.drawText(renderer, Text.literal("До след. ур. скорости " + (s.speedLevel + 1) + ": " + (int) s.remainingDistance + " блоков"), x, y + lineHeight * line, 0x88FF88, false);
        } else {
            context.drawText(renderer, Text.literal("Скорость: МАКС УРОВЕНЬ"), x, y + lineHeight * line, 0xFFD700, false);
        }
        line++;

        if (s.jumpLevel < 10) {
            context.drawText(renderer, Text.literal("До след. ур. прыжков " + (s.jumpLevel + 1) + ": " + (int) s.remainingJumps + " прыжков"), x, y + lineHeight * line, 0x88FF88, false);
        } else {
            context.drawText(renderer, Text.literal("Прыжки: МАКС УРОВЕНЬ"), x, y + lineHeight * line, 0xFFD700, false);
        }
        line++;

        return line;
    }

    private static int drawGroupMaxAttributes(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight) {
        if (!showMaxAttributes) return line;

        context.drawText(renderer, Text.literal("Макс. скорость: " + MathUtils.round2digits(s.maxSpeed) + s.speedUnit), x, y + lineHeight * line, 0xFFFF00, false);
        line++;

        context.drawText(renderer, Text.literal("Макс. прыжок: " + MathUtils.round2digits(s.maxJump) + s.jumpUnit), x, y + lineHeight * line, 0xFFFF00, false);
        line++;

        return line;
    }

    private static int drawGroupBaseAttributes(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight) {
        if (!showBaseAttributes) return line;

        context.drawText(renderer, Text.literal("Ориг. скорость: " + MathUtils.round2digits(s.originalSpeed) + s.speedUnit), x, y + lineHeight * line, 0xCCCCCC, false);
        line++;

        context.drawText(renderer, Text.literal("Ориг. прыжок: " + MathUtils.round2digits(s.originalJump) + s.jumpUnit), x, y + lineHeight * line, 0xCCCCCC, false);
        line++;

        return line;
    }

    private static void drawGroupTotalProgress(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight) {
        if (!showTotalProgress) return;

        context.drawText(renderer, Text.literal("Общая дистанция: " + s.progress.getRunningDistance()), x, y + lineHeight * line, 0x888888, false);
        line++;

        context.drawText(renderer, Text.literal("Общие прыжки: " + s.progress.getJumpCount()), x, y + lineHeight * line, 0x888888, false);
    }

    // ---------- Вспомогательная структура ----------
    private static class HorseStats {
        Breed breed;
        Progress progress;

        double speed;
        double jumpStrength;
        double maxSpeed;
        double maxJump;
        double originalSpeed;
        double originalJump;

        int speedLevel;
        double speedProgress;
        int jumpLevel;
        double jumpProgress;

        double remainingDistance;
        double remainingJumps;

        String speedUnit;
        String jumpUnit;
    }
}
