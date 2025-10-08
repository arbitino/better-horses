package net.betterhorses.fabric.mixin;

import net.betterhorses.common.accessor.jump.IsJumpingAccessor;
import net.betterhorses.common.accessor.jump.JumpingLastTickAccessor;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractHorseEntity.class)
public class AbstractHorseEntityAccessorMixinIs implements JumpingLastTickAccessor, IsJumpingAccessor {
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
