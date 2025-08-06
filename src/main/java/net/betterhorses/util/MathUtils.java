package net.betterhorses.util;

public class MathUtils {
    public static double round2digits(double value) {
        return java.lang.Math.round(value * 10.0 * 100.0) / 100.0;
    }
}
