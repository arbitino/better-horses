package net.betterhorses.common.accessor;

public interface MoveSpeedAccessor {
    double getMoveSpeed();
    double getBaseMoveSpeed();
    double getInitialMoveSpeed();
    boolean hasInitialMoveSpeed();

    void setBaseMoveSpeed(double value);
    void setInitialMoveSpeed(double value);
}
