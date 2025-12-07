package net.betterhorses.common.accessor;

public interface MoveSpeedAccessor {
    double betterHorses$getMoveSpeed();
    double betterHorses$getBaseMoveSpeed();
    double betterHorses$getInitialMoveSpeed();
    boolean betterHorses$hasInitialMoveSpeed();

    void betterHorses$setBaseMoveSpeed(double value);
    void betterHorses$setInitialMoveSpeed(double value);
}
