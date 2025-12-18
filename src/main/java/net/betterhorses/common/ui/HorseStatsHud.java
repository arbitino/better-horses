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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.text.Text;

public class HorseStatsHud {
    public enum HudPosition {
        TOP_LEFT,
        TOP_RIGHT,
        CENTER_ABOVE_HUD
    }

    public static void render(DrawContext context, TextRenderer textRenderer, HorseEntity horse) {
        BetterHorsesConfig.HudConfig config = getConfig().hud;
        
        int lineHeight = (int) (10 / config.fontScale);

        context.getMatrices().push();
        context.getMatrices().scale(config.fontScale, config.fontScale, 1.0f);

        HorseStats stats = collectHorseStats(horse);
        formatUnitsIfNeeded(stats, config);

        int[] position = calculateHudPosition(context, textRenderer, stats, config);
        int baseX = (int) (position[0] / config.fontScale);
        int baseY = (int) (position[1] / config.fontScale);
        
        drawHorseStats(context, textRenderer, stats, baseX, baseY, lineHeight, config);

        context.getMatrices().pop();
    }

    private static BetterHorsesConfig getConfig() {
        return AutoConfig.getConfigHolder(BetterHorsesConfig.class).getConfig();
    }

    private static int[] calculateHudPosition(DrawContext context, TextRenderer textRenderer, HorseStats stats, BetterHorsesConfig.HudConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int lineHeight = (int) (10 / config.fontScale);
        
        switch (config.hudPosition) {
            case TOP_RIGHT:
                int textWidth = getMaxTextWidth(textRenderer, stats, config);
                return new int[]{screenWidth - textWidth - 10, 10};
            case CENTER_ABOVE_HUD:
                int centerTextWidth = getMaxTextWidth(textRenderer, stats, config);
                int centerX = (screenWidth - centerTextWidth) / 2;
                int totalLines = getTotalLines(stats, config);
                int totalBlockHeight = totalLines * lineHeight;
                int centerY = screenHeight - 50 - totalBlockHeight;

                return new int[]{centerX, Math.max(10, centerY)};
            default:
                return new int[]{10, 10};
        }
    }

    private static int getTotalLines(HorseStats stats, BetterHorsesConfig.HudConfig config) {
        int lines = 0;
        
        if (config.showBreed || config.showAttributes) lines += 1; // breed + attributes in one line
        if (config.showLevels) lines += 2; // speed level + jump level
        if (config.showNextLevelInfo) lines += 2; // next speed + next jump
        if (config.showMaxAttributes) lines += 1; // max speed + max jump in one line
        if (config.showBaseAttributes) lines += 1; // orig speed + orig jump in one line
        if (config.showTotalProgress) lines += 1; // total distance + total jumps in one line
        
        return lines;
    }
    
    private static int getMaxTextWidth(TextRenderer textRenderer, HorseStats stats, BetterHorsesConfig.HudConfig config) {
        int maxWidth = 0;

        if (config.showBreed || config.showAttributes) {
            StringBuilder text = new StringBuilder();
            
            if (config.showBreed) {
                text.append(Text.translatable("hud.betterhorses.breed").getString()).append(" ").append(stats.breed.getDisplayName());
            }
            
            if (config.showAttributes) {
                if (!text.isEmpty()) {
                    text.append(" | ");
                }

                text
                    .append(Text.translatable("hud.betterhorses.speed").getString())
                    .append(" ")
                    .append(MathUtils.round2digits(stats.speed))
                    .append(stats.speedUnit)
                    .append(" | ")
                    .append(Text.translatable("hud.betterhorses.jump").getString())
                    .append(" ")
                    .append(MathUtils.round2digits(stats.jumpStrength))
                    .append(stats.jumpUnit);
            }
            
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(text.toString()));
        }
        
        if (config.showLevels) {
            String speedLevelText = Text.translatable("hud.betterhorses.speed_level").getString() + " " + stats.speedLevel + "/10 (" + MathUtils.round2digits(stats.speedProgress * 100) + "%)";
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(speedLevelText));
            
            String jumpLevelText = Text.translatable("hud.betterhorses.jump_level").getString() + " " + stats.jumpLevel + "/10 (" + MathUtils.round2digits(stats.jumpProgress * 100) + "%)";
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(jumpLevelText));
        }
        
        if (config.showNextLevelInfo) {
            if (stats.speedLevel < 10) {
                String speedText = Text.translatable("hud.betterhorses.next_speed_level").getString() + " " + (stats.speedLevel + 1) + ": " + (int) stats.remainingDistance + " " + Text.translatable("hud.betterhorses.blocks").getString();
                maxWidth = Math.max(maxWidth, textRenderer.getWidth(speedText));
            } else {
                maxWidth = Math.max(maxWidth, textRenderer.getWidth(Text.translatable("hud.betterhorses.speed_max_level").getString()));
            }
            
            if (stats.jumpLevel < 10) {
                String jumpText = Text.translatable("hud.betterhorses.next_jump_level").getString() + " " + (stats.jumpLevel + 1) + ": " + (int) stats.remainingJumps + " " + Text.translatable("hud.betterhorses.jumps").getString();
                maxWidth = Math.max(maxWidth, textRenderer.getWidth(jumpText));
            } else {
                maxWidth = Math.max(maxWidth, textRenderer.getWidth(Text.translatable("hud.betterhorses.jump_max_level").getString()));
            }
        }
        
        if (config.showMaxAttributes) {
            String text = Text.translatable("hud.betterhorses.max_speed").getString() + " " + MathUtils.round2digits(stats.maxSpeed) + stats.speedUnit + " | " + Text.translatable("hud.betterhorses.max_jump").getString() + " " + MathUtils.round2digits(stats.maxJump) + stats.jumpUnit;
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(text));
        }
        
        if (config.showBaseAttributes) {
            String text = Text.translatable("hud.betterhorses.original_speed").getString() + " " + MathUtils.round2digits(stats.originalSpeed) + stats.speedUnit + " | " + Text.translatable("hud.betterhorses.original_jump").getString() + " " + MathUtils.round2digits(stats.originalJump) + stats.jumpUnit;
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(text));
        }
        
        if (config.showTotalProgress) {
            String text = Text.translatable("hud.betterhorses.total_distance").getString() + " " + stats.progress.getRunningDistance() + " | " + Text.translatable("hud.betterhorses.total_jumps").getString() + " " + stats.progress.getJumpCount();
            maxWidth = Math.max(maxWidth, textRenderer.getWidth(text));
        }
        
        return (int) (maxWidth * config.fontScale);
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

        s.speedUnit = " " + Text.translatable("hud.betterhorses.speedUnit").getString();
        s.jumpUnit = " "  + Text.translatable("hud.betterhorses.jumpUnit").getString();
    }

    private static void drawHorseStats(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int lineHeight, BetterHorsesConfig.HudConfig config) {
        int line = 0;

        line = drawGroupBreedAndAttributes(context, renderer, s, x, y, line, lineHeight, config);
        line = drawGroupLevels(context, renderer, s, x, y, line, lineHeight, config);
        line = drawGroupNextLevelInfo(context, renderer, s, x, y, line, lineHeight, config);
        line = drawGroupMaxAttributes(context, renderer, s, x, y, line, lineHeight, config);
        line = drawGroupBaseAttributes(context, renderer, s, x, y, line, lineHeight, config);
        drawGroupTotalProgress(context, renderer, s, x, y, line, lineHeight, config);
    }

    private static int drawGroupBreedAndAttributes(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showBreed && !config.showAttributes) return line;

        StringBuilder text = new StringBuilder();
        
        if (config.showBreed) {
            text.append(Text.translatable("hud.betterhorses.breed").getString()).append(" ").append(s.breed.getDisplayName());
        }
        
        if (config.showAttributes) {
            if (!text.isEmpty()) text.append(" | ");

            text
                .append(Text.translatable("hud.betterhorses.speed").getString())
                .append(" ")
                .append(MathUtils.round2digits(s.speed))
                .append(s.speedUnit)
                .append(" | ")
                .append(Text.translatable("hud.betterhorses.jump").getString())
                .append(" ")
                .append(MathUtils.round2digits(s.jumpStrength))
                .append(s.jumpUnit);
        }
        
        context.drawText(renderer, Text.literal(text.toString()), x, y + lineHeight * line, 0xFFFFFF, false);
        line++;

        return line;
    }

    private static int drawGroupLevels(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showLevels) return line;

        String speedLevelText = Text.translatable("hud.betterhorses.speed_level").getString() + " " + s.speedLevel + "/10 (" + MathUtils.round2digits(s.speedProgress * 100) + "%)";
        context.drawText(renderer, Text.literal(speedLevelText), x, y + lineHeight * line, 0x00FF00, false);
        line++;

        String jumpLevelText = Text.translatable("hud.betterhorses.jump_level").getString() + " " + s.jumpLevel + "/10 (" + MathUtils.round2digits(s.jumpProgress * 100) + "%)";
        context.drawText(renderer, Text.literal(jumpLevelText), x, y + lineHeight * line, 0x00FF00, false);
        line++;

        return line;
    }

    private static int drawGroupNextLevelInfo(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showNextLevelInfo) return line;

        if (s.speedLevel < 10) {
            String speedText = Text.translatable("hud.betterhorses.next_speed_level").getString() + " " + (s.speedLevel + 1) + ": " + (int) s.remainingDistance + " " + Text.translatable("hud.betterhorses.blocks").getString();
            context.drawText(renderer, Text.literal(speedText), x, y + lineHeight * line, 0x88FF88, false);
        } else {
            context.drawText(renderer, Text.translatable("hud.betterhorses.speed_max_level"), x, y + lineHeight * line, 0xFFD700, false);
        }
        line++;

        if (s.jumpLevel < 10) {
            String jumpText = Text.translatable("hud.betterhorses.next_jump_level").getString() + " " + (s.jumpLevel + 1) + ": " + (int) s.remainingJumps + " " + Text.translatable("hud.betterhorses.jumps").getString();
            context.drawText(renderer, Text.literal(jumpText), x, y + lineHeight * line, 0x88FF88, false);
        } else {
            context.drawText(renderer, Text.translatable("hud.betterhorses.jump_max_level"), x, y + lineHeight * line, 0xFFD700, false);
        }
        line++;

        return line;
    }

    private static int drawGroupMaxAttributes(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showMaxAttributes) return line;

        String text = Text.translatable("hud.betterhorses.max_speed").getString() + " " + MathUtils.round2digits(s.maxSpeed) + s.speedUnit + " | " + Text.translatable("hud.betterhorses.max_jump").getString() + " " + MathUtils.round2digits(s.maxJump) + s.jumpUnit;
        context.drawText(renderer, Text.literal(text), x, y + lineHeight * line, 0xFFFF00, false);
        line++;

        return line;
    }

    private static int drawGroupBaseAttributes(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showBaseAttributes) return line;

        String text = Text.translatable("hud.betterhorses.original_speed").getString() + " " + MathUtils.round2digits(s.originalSpeed) + s.speedUnit + " | " + Text.translatable("hud.betterhorses.original_jump").getString() + " " + MathUtils.round2digits(s.originalJump) + s.jumpUnit;
        context.drawText(renderer, Text.literal(text), x, y + lineHeight * line, 0xCCCCCC, false);
        line++;

        return line;
    }

    private static void drawGroupTotalProgress(DrawContext context, TextRenderer renderer, HorseStats s, int x, int y, int line, int lineHeight, BetterHorsesConfig.HudConfig config) {
        if (!config.showTotalProgress) return;

        String text = Text.translatable("hud.betterhorses.total_distance").getString() + " " + s.progress.getRunningDistance() + " | " + Text.translatable("hud.betterhorses.total_jumps").getString() + " " + s.progress.getJumpCount();
        context.drawText(renderer, Text.literal(text), x, y + lineHeight * line, 0x888888, false);
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
