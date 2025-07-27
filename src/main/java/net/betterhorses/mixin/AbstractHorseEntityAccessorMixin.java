package net.betterhorses.mixin;

import net.betterhorses.accessor.JumpingAccessor;
import net.betterhorses.accessor.JumpingTracker;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractHorseEntity.class)
public class AbstractHorseEntityAccessorMixin implements JumpingTracker, JumpingAccessor {
    @Shadow
    protected boolean jumping;

    @Unique
    private boolean wasJumpingLastTick = false;

    @Override
    public boolean wasJumpingLastTick() {
        return wasJumpingLastTick;
    }

    @Override
    public void setWasJumpingLastTick(boolean value) {
        this.wasJumpingLastTick = value;
    }

    @Override
    public boolean isJumping() {
        return this.jumping;
    }
}
