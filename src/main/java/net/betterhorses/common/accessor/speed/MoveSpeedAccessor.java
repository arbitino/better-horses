package net.betterhorses.common.accessor.speed;

public interface MoveSpeedAccessor {
    void setBaseMoveSpeed(double value);

    double getMoveSpeed();
    double getBaseMoveSpeed();

    void setInitialMoveSpeed(double value);
    double getInitialMoveSpeed();
    boolean hasInitialMoveSpeed();
}
