package net.betterhorses.common.util;

import org.spongepowered.asm.mixin.Unique;

public class MathUtils {
    public static double round2digits(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Unique
    public static double calcJumpHeightValueInBlocks(double value) {
        return - 0.1817584952 * (Math.pow(value, 3))
                + 3.689713992 * (Math.pow(value, 2))
                + 2.128599134 * value
                - 0.343930367;
    }

    @Unique
    public static double calcMoveSpeedValueInBlocks(double value) {
        return value  * 42.157796;
    }
}
