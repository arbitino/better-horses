package net.betterhorses.common.accessor.speed;

public interface MoveSpeedAccessor {
    void setBaseMoveSpeed(double value);

    double getMoveSpeed();
    double getBaseMoveSpeed();
    double getBaseMoveSpeedInBlocks();
    double getMoveSpeedInBlocks();

    void setInitialMoveSpeed(double value);
    double getInitialMoveSpeed();
    boolean hasInitialMoveSpeed();
}
