package net.betterhorses.common.progress;

import net.betterhorses.common.accessor.jump.IsJumpingAccessor;
import net.betterhorses.common.accessor.jump.JumpingLastTickAccessor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProgressUtils {
    private static final Map<UUID, Vec3d> lastHorsePositions = new HashMap<>();

    public static void setHorseJumps(HorseEntity horse) {
        JumpingLastTickAccessor tracker = (JumpingLastTickAccessor) horse;
        IsJumpingAccessor jumpingAccessor = (IsJumpingAccessor) horse;
        Progress progress = ((ProgressableHorse) horse).getProgress();

        boolean currentlyJumping = jumpingAccessor.isJumping();
        boolean wasJumping = tracker.wasJumpingLastTick();

        if (currentlyJumping && !wasJumping) {
            progress.addJump();
            ((ProgressableHorse) horse).setProgress(progress);

            HorseAttributeProgression.updateJumpFromJumps(horse);
        }

        tracker.setWasJumpingLastTick(currentlyJumping);
    }

    public static void setHorseDistance(HorseEntity horse) {
        Vec3d currentPos = horse.getPos();
        UUID horseUuid = horse.getUuid();
        Vec3d lastPos = lastHorsePositions.get(horseUuid);

        if (lastPos != null) {
            double distance = currentPos.distanceTo(lastPos);
            Progress progress = ((ProgressableHorse) horse).getProgress();

            progress.addDistance((long) distance);
            ((ProgressableHorse) horse).setProgress(progress);

            HorseAttributeProgression.updateSpeedFromDistance(horse);
        }

        lastHorsePositions.put(horseUuid, currentPos);
    }

    public static void clearHorseSession(HorseEntity horse) {
        UUID horseUuid = horse.getUuid();
        lastHorsePositions.remove(horseUuid);
    }
}
