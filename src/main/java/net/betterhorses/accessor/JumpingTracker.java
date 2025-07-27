package net.betterhorses.accessor;

public interface JumpingTracker {
    boolean wasJumpingLastTick();
    void setWasJumpingLastTick(boolean value);
}
