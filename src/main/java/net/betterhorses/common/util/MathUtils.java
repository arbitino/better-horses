package net.betterhorses.common.util;

public class MathUtils {
    private static final double JUMP_A = -0.1817584952;
    private static final double JUMP_B =  3.689713992;
    private static final double JUMP_C =  2.128599134;
    private static final double JUMP_D = -0.343930367;

    private static final double JUMP_DA = 3 * JUMP_A;
    private static final double JUMP_DB = 2 * JUMP_B;
    private static final double JUMP_DC = JUMP_C;

    private static final double MOVE_MULTIPLIER = 42.157796;

    public static double round2digits(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static double moveAttributeToBlocksPerSecond(double value) {
        return value * MOVE_MULTIPLIER;
    }

    public static double blocksPerSecondToMoveAttribute(double blocks) {
        return blocks / MOVE_MULTIPLIER;
    }

    public static double jumpAttributeToBlocks(double value) {
        return JUMP_A * value * value * value
                + JUMP_B * value * value
                + JUMP_C * value
                + JUMP_D;
    }

    public static double blocksToJumpAttribute(double blocks) {
        double x = 1.0;

        for (int i = 0; i < 30; i++) {
            double f = JUMP_A * x * x * x
                    + JUMP_B * x * x
                    + JUMP_C * x
                    + JUMP_D
                    - blocks;

            double df = JUMP_DA * x * x
                    + JUMP_DB * x
                    + JUMP_DC;

            x -= f / df;
        }

        return x;
    }
}
