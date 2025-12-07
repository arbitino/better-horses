package net.betterhorses.common.accessor;

public interface JumpingAccessor {
    boolean isJumping();
    boolean wasJumpingLastTick();
    double getBaseJumpStrength();
    double getJumpStrength();
    double getInitialJumpStrength();
    boolean hasInitialJumpStrength();

    void setWasJumpingLastTick(boolean value);
    void setBaseJumpStrength(double value);
    void setInitialJumpStrength(double value);
}