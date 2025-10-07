package net.betterhorses.util;

public class MathUtils {
    public static double round2digits(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
