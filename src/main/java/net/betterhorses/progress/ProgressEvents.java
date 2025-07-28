package net.betterhorses.progress;

import net.betterhorses.accessor.jump.IsJumpingAccessor;
import net.betterhorses.accessor.jump.JumpingLastTickAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.WeakHashMap;

public class ProgressEvents {
    private static final Map<HorseEntity, Vec3d> lastHorsePositions = new WeakHashMap<>();

    public static void init() {
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            long time = world.getTime();

            world.iterateEntities().forEach(entity -> {
                if (entity instanceof HorseEntity horse) {
                    var player = horse.getFirstPassenger();

                    if (player == null) {
                        return;
                    }

                    setHorseJumps(horse);
                    setHorseDistance(horse, time);
                }
            });
        });
    }

    private static void setHorseJumps(HorseEntity horse) {
        JumpingLastTickAccessor tracker = (JumpingLastTickAccessor) horse;
        IsJumpingAccessor jumpingAccessor = (IsJumpingAccessor) horse;
        Progress progress = ((ProgressableHorse) horse).getProgress();

        boolean currentlyJumping = jumpingAccessor.isJumping();
        boolean wasJumping = tracker.wasJumpingLastTick();

        if (currentlyJumping && !wasJumping) {
            progress.addJump();
            ((ProgressableHorse) horse).setProgress(progress);
        }

        tracker.setWasJumpingLastTick(currentlyJumping);
    }

    private static void setHorseDistance(HorseEntity horse, long time) {
        // === Каждую 1 секунду ===
        if (time % 20 == 0) {
            Vec3d currentPos = horse.getPos();
            Vec3d lastPos = lastHorsePositions.get(horse);

            if (lastPos != null) {
                double distance = currentPos.distanceTo(lastPos);
                Progress progress = ((ProgressableHorse) horse).getProgress();

                progress.addDistance((long) distance);
                ((ProgressableHorse) horse).setProgress(progress);
            }

            lastHorsePositions.put(horse, currentPos);
        }
    }
}
