package net.betterhorses.common.accessor.jump;

public interface JumpingHeightAccessor {
    void setBaseJumpStrength(double value);

    double getBaseJumpStrength();
    double getJumpStrength();

    void setInitialJumpStrength(double value);
    double getInitialJumpStrength();
    boolean hasInitialJumpStrength();
}
