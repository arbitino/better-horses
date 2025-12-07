package net.betterhorses.common.progress;

import net.betterhorses.common.accessor.JumpingAccessor;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProgressUtils {
    private static final Map<UUID, Vec3d> lastHorsePositions = new HashMap<>();

    public static void setHorseJumps(HorseEntity horse) {
        JumpingAccessor jumpingAccessor = (JumpingAccessor) horse;
        Progress progress = ((ProgressableHorse) horse).getProgress();

        boolean currentlyJumping = jumpingAccessor.isJumping();
        boolean wasJumping = jumpingAccessor.wasJumpingLastTick();

        if (currentlyJumping && !wasJumping) {
            progress.addJump();
            ((ProgressableHorse) horse).setProgress(progress);

            HorseAttributeProgression.updateJumpFromJumps(horse);
        }

        jumpingAccessor.setWasJumpingLastTick(currentlyJumping);
    }

    public static void setHorseDistance(HorseEntity horse) {
        Vec3d currentPos = horse.getPos();
        UUID horseUuid = horse.getUuid();
        Vec3d lastPos = lastHorsePositions.get(horseUuid);

        if (lastPos != null) {
            double distance = currentPos.distanceTo(lastPos);

            if (distance >= 0.01) {
                Progress progress = ((ProgressableHorse) horse).getProgress();
                progress.addDistance((long) distance);
                ((ProgressableHorse) horse).setProgress(progress);
                HorseAttributeProgression.updateSpeedFromDistance(horse);

                lastHorsePositions.put(horseUuid, currentPos);
            }
        } else {
            lastHorsePositions.put(horseUuid, currentPos);
        }
    }

    public static void clearHorseSession(HorseEntity horse) {
        UUID horseUuid = horse.getUuid();
        lastHorsePositions.remove(horseUuid);
    }
}
