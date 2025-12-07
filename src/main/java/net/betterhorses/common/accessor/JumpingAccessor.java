package net.betterhorses.common.accessor;

public interface JumpingAccessor {
    boolean betterHorses$isJumping();
    boolean betterHorses$wasJumpingLastTick();
    double betterHorses$getBaseJumpStrength();
    double betterHorses$getJumpStrength();
    double betterHorses$getInitialJumpStrength();
    boolean betterHorses$hasInitialJumpStrength();

    void betterHorses$setWasJumpingLastTick(boolean value);
    void betterHorses$setBaseJumpStrength(double value);
    void betterHorses$setInitialJumpStrength(double value);
}