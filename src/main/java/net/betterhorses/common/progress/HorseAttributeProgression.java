package net.betterhorses.common.progress;

import net.betterhorses.common.BetterHorses;
import net.betterhorses.common.accessor.JumpingAccessor;
import net.betterhorses.common.accessor.MoveSpeedAccessor;
import net.betterhorses.common.breed.Breed;
import net.betterhorses.common.breed.BreedableHorse;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class HorseAttributeProgression {
    private static final int MAX_LEVEL = 10;
    private static final int MIN_LEVEL = 1;

    private static final double DISTANCE_XP_MULTIPLIER = 1.0;
    private static final double JUMP_XP_MULTIPLIER = 1.0;

    public static int getSpeedLevel(HorseEntity horse) {
        MoveSpeedAccessor speedAccessor = (MoveSpeedAccessor) horse;
        if (!speedAccessor.betterHorses$hasInitialMoveSpeed()) return MIN_LEVEL;

        double initialSpeed = speedAccessor.betterHorses$getInitialMoveSpeed();
        double currentSpeed = speedAccessor.betterHorses$getBaseMoveSpeed();
        BreedableHorse breedableHorse = (BreedableHorse) horse;
        Breed breed = breedableHorse.betterHorses$getHorseBreed();
        double maxSpeed = breed.maxSpeed();

        return calculateLevelFromValue(currentSpeed, initialSpeed, maxSpeed);
    }

    public static int getJumpLevel(HorseEntity horse) {
        JumpingAccessor jumpAccessor = (JumpingAccessor) horse;
        if (!jumpAccessor.betterHorses$hasInitialJumpStrength()) return MIN_LEVEL;

        double initialJump = jumpAccessor.betterHorses$getInitialJumpStrength();
        double currentJump = jumpAccessor.betterHorses$getBaseJumpStrength();
        BreedableHorse breedableHorse = (BreedableHorse) horse;
        Breed breed = breedableHorse.betterHorses$getHorseBreed();
        double maxJump = breed.maxJumpHeight();

        return calculateLevelFromValue(currentJump, initialJump, maxJump);
    }

    public static double getSpeedProgress(HorseEntity horse) {
        MoveSpeedAccessor speedAccessor = (MoveSpeedAccessor) horse;
        if (!speedAccessor.betterHorses$hasInitialMoveSpeed()) return 0.0;

        int level = getSpeedLevel(horse);
        if (level >= MAX_LEVEL) return 0.0;

        double distanceProgress = getDistanceProgress(horse);

        double next = getRequiredDistanceForLevel(level + 1);
        double current = getRequiredDistanceForLevel(level);
        return Math.max(0.0, Math.min(1.0, (distanceProgress - current) / (next - current)));
    }

    public static double getJumpProgress(HorseEntity horse) {
        JumpingAccessor jumpAccessor = (JumpingAccessor) horse;
        if (!jumpAccessor.betterHorses$hasInitialJumpStrength()) return 0.0;

        int level = getJumpLevel(horse);
        if (level >= MAX_LEVEL) return 0.0;

        BreedableHorse breedableHorse = (BreedableHorse) horse;
        Breed breed = breedableHorse.betterHorses$getHorseBreed();

        ProgressableHorse progressableHorse = (ProgressableHorse) horse;
        Progress progress = progressableHorse.betterHorses$getProgress();
        long totalJumps = progress.getJumpCount();

        double jumpProgress = totalJumps * breed.jumpGrowthMultiplier();
        jumpProgress = applyObedienceMultiplier(breed.getObedience(), jumpProgress);
        jumpProgress *= JUMP_XP_MULTIPLIER;

        double next = getRequiredJumpsForLevel(level + 1);
        double current = getRequiredJumpsForLevel(level);

        return Math.max(0.0, Math.min(1.0, (jumpProgress - current) / (next - current)));
    }

    public static double getRemainingDistanceForNextSpeedLevel(HorseEntity horse) {
        int level = getSpeedLevel(horse);
        if (level >= MAX_LEVEL) return 0.0;

        double distanceProgress = getDistanceProgress(horse);

        double required = getRequiredDistanceForLevel(level + 1);

        return Math.max(0.0, required - distanceProgress);
    }

    public static double getRemainingJumpsForNextJumpLevel(HorseEntity horse) {
        int level = getJumpLevel(horse);
        if (level >= MAX_LEVEL) return 0.0;

        BreedableHorse breedableHorse = (BreedableHorse) horse;
        Breed breed = breedableHorse.betterHorses$getHorseBreed();

        ProgressableHorse progressableHorse = (ProgressableHorse) horse;
        Progress progress = progressableHorse.betterHorses$getProgress();
        long totalJumps = progress.getJumpCount();

        double jumpProgress = totalJumps * breed.jumpGrowthMultiplier();
        jumpProgress = applyObedienceMultiplier(breed.getObedience(), jumpProgress);
        jumpProgress *= JUMP_XP_MULTIPLIER;

        double required = getRequiredJumpsForLevel(level + 1);

        return Math.max(0.0, required - jumpProgress);
    }

    public static void updateSpeedFromDistance(HorseEntity horse) {
        MoveSpeedAccessor speedAccessor = (MoveSpeedAccessor) horse;

        if (!speedAccessor.betterHorses$hasInitialMoveSpeed()) return;

        double initialSpeed = speedAccessor.betterHorses$getInitialMoveSpeed();
        double currentSpeed = speedAccessor.betterHorses$getBaseMoveSpeed();
        
        BreedableHorse breedableHorse = (BreedableHorse) horse;
        Breed breed = breedableHorse.betterHorses$getHorseBreed();

        double maxSpeed = breed.maxSpeed();
        int currentLevel = calculateLevelFromValue(currentSpeed, initialSpeed, maxSpeed);

        if (currentLevel >= MAX_LEVEL) return;

        ProgressableHorse progressableHorse = (ProgressableHorse) horse;
        Progress progress = progressableHorse.betterHorses$getProgress();
        long totalDistance = progress.getRunningDistance();

        double distanceProgress = totalDistance * breed.speedGrowthMultiplier();
        distanceProgress = applyObedienceMultiplier(breed.getObedience(), distanceProgress);
        distanceProgress *= DISTANCE_XP_MULTIPLIER;

        double requiredDistance = getRequiredDistanceForLevel(currentLevel + 1);

        if (distanceProgress >= requiredDistance) {
            int newLevel = currentLevel + 1;
            double newSpeed = interpolateValue(initialSpeed, maxSpeed, newLevel);

            speedAccessor.betterHorses$setBaseMoveSpeed(newSpeed);

            sendLevelUpMessage(horse, "скорость", newLevel);

            BetterHorses.LOGGER.info(
                "Лошадь {} получила повышение уровня скорости: {} -> {} ({} -> {})",
                horse.getUuid(), currentLevel, newLevel, currentSpeed, newSpeed
            );
        }
    }

    public static void updateJumpFromJumps(HorseEntity horse) {
        JumpingAccessor jumpAccessor = (JumpingAccessor) horse;

        if (!jumpAccessor.betterHorses$hasInitialJumpStrength()) return;

        double initialJump = jumpAccessor.betterHorses$getInitialJumpStrength();
        double currentJump = jumpAccessor.betterHorses$getBaseJumpStrength();
        
        BreedableHorse breedableHorse = (BreedableHorse) horse;
        Breed breed = breedableHorse.betterHorses$getHorseBreed();

        double maxJump = breed.maxJumpHeight();
        int currentLevel = calculateLevelFromValue(currentJump, initialJump, maxJump);

        if (currentLevel >= MAX_LEVEL) return;

        ProgressableHorse progressableHorse = (ProgressableHorse) horse;
        Progress progress = progressableHorse.betterHorses$getProgress();
        long totalJumps = progress.getJumpCount();

        double jumpProgress = totalJumps * breed.jumpGrowthMultiplier();
        jumpProgress = applyObedienceMultiplier(breed.getObedience(), jumpProgress);
        jumpProgress *= JUMP_XP_MULTIPLIER;

        double requiredJumps = getRequiredJumpsForLevel(currentLevel + 1);

        if (jumpProgress >= requiredJumps) {
            int newLevel = currentLevel + 1;
            double newJump = interpolateValue(initialJump, maxJump, newLevel);

            jumpAccessor.betterHorses$setBaseJumpStrength(newJump);

            sendLevelUpMessage(horse, "сила прыжка", newLevel);

            BetterHorses.LOGGER.info(
                "Лошадь {} получила повышение уровня силы прыжка: {} -> {} ({} -> {})",
                horse.getUuid(), currentLevel, newLevel, currentJump, newJump
            );
        }
    }

    private static double getDistanceProgress(HorseEntity horse) {
        BreedableHorse breedableHorse = (BreedableHorse) horse;
        Breed breed = breedableHorse.betterHorses$getHorseBreed();

        ProgressableHorse progressableHorse = (ProgressableHorse) horse;
        Progress progress = progressableHorse.betterHorses$getProgress();
        long totalDistance = progress.getRunningDistance();

        double distanceProgress = totalDistance * breed.speedGrowthMultiplier();
        distanceProgress = applyObedienceMultiplier(breed.getObedience(), distanceProgress);
        distanceProgress *= DISTANCE_XP_MULTIPLIER;

        return distanceProgress;
    }

    private static double getRequiredDistanceForLevel(int level) {
        if (level <= MIN_LEVEL) return 0;
        double base = 100.0;
        double exponent = 1.5 + (MAX_LEVEL / 50.0);

        return base + base * Math.pow(level, exponent);
    }

    private static double getRequiredJumpsForLevel(int level) {
        if (level <= MIN_LEVEL) return 0;
        double base = 5.0;
        double exponent = 1.8 + (MAX_LEVEL / 60.0);

        return base + base * Math.pow(level, exponent);
    }

    private static double interpolateValue(double initialValue, double maxValue, int level) {
        if (level <= MIN_LEVEL) return initialValue;
        if (level >= MAX_LEVEL) return maxValue;
        double t = (level - MIN_LEVEL) / (double) (MAX_LEVEL - MIN_LEVEL);

        return initialValue + (maxValue - initialValue) * t;
    }

    private static int calculateLevelFromValue(double currentValue, double initialValue, double maxValue) {
        if (currentValue <= initialValue) return MIN_LEVEL;
        if (currentValue >= maxValue) return MAX_LEVEL;

        double ratio = (currentValue - initialValue) / (maxValue - initialValue);
        int level = (int) Math.round(ratio * (MAX_LEVEL - MIN_LEVEL) + MIN_LEVEL);

        return Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
    }

    private static double applyObedienceMultiplier(Breed.ObedienceLevel obedience, double progress) {
        return switch (obedience) {
            case VERY_OBEDIENT -> progress * 1.2;
            case DISOBEDIENT -> progress * 0.8;
            default -> progress;
        };
    }

    private static void sendLevelUpMessage(HorseEntity horse, String statType, int newLevel) {
        if (horse.getControllingPassenger() instanceof PlayerEntity player) {
            String message = String.format("Лошадь улучшила показатель %s! Нынешний уровень %d/%d", statType, newLevel, MAX_LEVEL);
            player.sendMessage(Text.literal(message), true);
        }
    }
}
