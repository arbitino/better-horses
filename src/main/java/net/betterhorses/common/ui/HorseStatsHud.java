package net.betterhorses.common.ui;

import me.shedaniel.autoconfig.AutoConfig;
import net.betterhorses.common.accessor.JumpingAccessor;
import net.betterhorses.common.accessor.MoveSpeedAccessor;
import net.betterhorses.common.breed.Breed;
import net.betterhorses.common.breed.BreedableHorse;
import net.betterhorses.common.config.BetterHorsesConfig;
import net.betterhorses.common.progress.Progress;
import net.betterhorses.common.progress.ProgressableHorse;
import net.betterhorses.common.progress.HorseAttributeProgression;
import net.betterhorses.common.util.MathUtils;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.text.Text;

public class HorseStatsHud {
    public static void render(DrawContext context, TextRenderer textRenderer, HorseEntity horse) {
        BetterHorsesConfig.HudConfig config = getConfig().hud;
        
        int baseX = (int) (10 / config.fontScale);
        int baseY = (int) (10 / config.fontScale);
        int lineHeight = (int) (10 / config.fontScale);

        context.getMatrices().push();
        context.getMatrices().scale(config.fontScale, config.fontScale, 1.0f);

        HorseStats stats = collectHorseStats(horse);
        formatUnitsIfNeeded(stats, config);
        drawHorseStats(context, textRenderer, stats, baseX, baseY, lineHeight, config);

        context.getMatrices().pop();
    }

    private static BetterHorsesConfig getConfig() {
        return AutoConfig.getConfigHolder(BetterHorsesConfig.class).getConfig();
    }

    private static HorseStats collectHorseStats(HorseEntity horse) {
        HorseStats s = new HorseStats();

        MoveSpeedAccessor speedAcc = (MoveSpeedAccessor) horse;
        JumpingAccessor jumpAcc = (JumpingAccessor) horse;

        s.breed = ((BreedableHorse) horse).betterHorses$getHorseBreed();
        s.progress = ((ProgressableHorse) horse).betterHorses$getProgress();

        s.speed = speedAcc.betterHorses$getMoveSpeed();
        s.jumpStrength = jumpAcc.betterHorses$getJumpStrength();

        s.maxSpeed = s.breed.maxSpeed();
        s.maxJump = s.breed.maxJumpHeight();
        s.originalSpeed = speedAcc.betterHorses$getInitialMoveSpeed();
        s.originalJump = jumpAcc.betterHorses$getInitialJumpStrength();

        s.speedLevel = HorseAttributeProgression.getSpeedLevel(horse);
        s.speedProgress = HorseAttributeProgression.getSpeedProgress(horse);
        s.jumpLevel = HorseAttributeProgression.getJumpLevel(horse);
        s.jumpProgress = HorseAttributeProgression.getJumpProgress(horse);

        s.remainingDistance = HorseAttributeProgression.getRemainingDistanceForNextSpeedLevel(horse);
        s.remainingJumps = HorseAttributeProgression.getRemainingJumpsForNextJumpLevel(horse);

        return s;
    }

    private static void formatUnitsIfNeeded(HorseStats s, BetterHorsesConfig.HudConfig config) {
        s.speedUnit = "";
        s.jumpUnit = "";

        if (!config.useHumanFriendlyUnits) return;

        s.speed = MathUtils.moveAttributeToBlocksPerSecond(s.speed);
        s.jumpStrength = MathUtils.jumpAttributeToBlocks(s.jumpStrength);
        s.maxSpeed = MathUtils.moveAttributeToBlocksPerSecond(s.maxSpeed);
        s.maxJump = MathUtils.jumpAttributeToBlocks(s.maxJump);
        s.originalSpeed = MathUtils.moveAttributeToBlocksPerSecond(s.originalSpeed);
        s.originalJump = MathUtils.jumpAttributeToBlocks(s.originalJump);

        s.speedUnit = " б/с";
        s.jumpUnit = " б";
    }

    private static void drawHorseStats(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int lineHeight, BetterHorsesConfig.HudConfig config) {
        int line = 0;

        line = drawGroupBreed(context, renderer, s, x, y, line, lineHeight, config);
        line = drawGroupAttributes(context, renderer, s, x, y, line, lineHeight, config);
        line = drawGroupLevels(context, renderer, s, x, y, line, lineHeight, config);
        line = drawGroupNextLevelInfo(context, renderer, s, x, y, line, lineHeight, config);
        line = drawGroupMaxAttributes(context, renderer, s, x, y, line, lineHeight, config);
        line = drawGroupBaseAttributes(context, renderer, s, x, y, line, lineHeight, config);
        drawGroupTotalProgress(context, renderer, s, x, y, line, lineHeight, config);
    }

    private static int drawGroupBreed(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showBreed) return line;

        context.drawText(renderer, Text.literal("Порода: " + s.breed.getDisplayName()), x, y + lineHeight * line, 0xFFFFFF, false);
        line++;

        return line;
    }

    private static int drawGroupAttributes(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showAttributes) return line;

        context.drawText(renderer, Text.literal("Скорость: " + MathUtils.round2digits(s.speed) + s.speedUnit), x, y + lineHeight * line, 0xFFFFFF, false);
        line++;

        context.drawText(renderer, Text.literal("Прыжок: " + MathUtils.round2digits(s.jumpStrength) + s.jumpUnit), x, y + lineHeight * line, 0xFFFFFF, false);
        line++;

        return line;
    }

    private static int drawGroupLevels(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showLevels) return line;

        context.drawText(renderer, Text.literal("Уровень скорости: " + s.speedLevel + "/10 (" + MathUtils.round2digits(s.speedProgress * 100) + "%)"), x, y + lineHeight * line, 0x00FF00, false);
        line++;

        context.drawText(renderer, Text.literal("Уровень прыжков: " + s.jumpLevel + "/10 (" + MathUtils.round2digits(s.jumpProgress * 100) + "%)"), x, y + lineHeight * line, 0x00FF00, false);
        line++;

        return line;
    }

    private static int drawGroupNextLevelInfo(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showNextLevelInfo) return line;

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

    private static int drawGroupMaxAttributes(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showMaxAttributes) return line;

        context.drawText(renderer, Text.literal("Макс. скорость: " + MathUtils.round2digits(s.maxSpeed) + s.speedUnit), x, y + lineHeight * line, 0xFFFF00, false);
        line++;

        context.drawText(renderer, Text.literal("Макс. прыжок: " + MathUtils.round2digits(s.maxJump) + s.jumpUnit), x, y + lineHeight * line, 0xFFFF00, false);
        line++;

        return line;
    }

    private static int drawGroupBaseAttributes(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showBaseAttributes) return line;

        context.drawText(renderer, Text.literal("Ориг. скорость: " + MathUtils.round2digits(s.originalSpeed) + s.speedUnit), x, y + lineHeight * line, 0xCCCCCC, false);
        line++;

        context.drawText(renderer, Text.literal("Ориг. прыжок: " + MathUtils.round2digits(s.originalJump) + s.jumpUnit), x, y + lineHeight * line, 0xCCCCCC, false);
        line++;

        return line;
    }

    private static void drawGroupTotalProgress(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showTotalProgress) return;

        context.drawText(renderer, Text.literal("Общая дистанция: " + s.progress.getRunningDistance()), x, y + lineHeight * line, 0x888888, false);
        line++;

        context.drawText(renderer, Text.literal("Общие прыжки: " + s.progress.getJumpCount()), x, y + lineHeight * line, 0x888888, false);
    }

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
