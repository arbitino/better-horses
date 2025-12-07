package net.betterhorses.fabric.mixin;

import net.betterhorses.common.accessor.JumpingAccessor;
import net.betterhorses.common.accessor.MoveSpeedAccessor;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseEntity.class)
public abstract class HorseAttributesMixin extends AnimalEntity {
    protected HorseAttributesMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initAttributes", at = @At("TAIL"))
    private void onInitAttributes(Random random, CallbackInfo ci) {
        MoveSpeedAccessor moveSpeedAccessor = (MoveSpeedAccessor) this;
        JumpingAccessor jumpingAccessor = (JumpingAccessor) this;

        double baseSpeed = moveSpeedAccessor.betterHorses$getBaseMoveSpeed();

        if (!moveSpeedAccessor.betterHorses$hasInitialMoveSpeed()) {
            moveSpeedAccessor.betterHorses$setInitialMoveSpeed(baseSpeed);
        }

        if (!jumpingAccessor.betterHorses$hasInitialJumpStrength()) {
            double baseJump = jumpingAccessor.betterHorses$getBaseJumpStrength();
            jumpingAccessor.betterHorses$setInitialJumpStrength(baseJump);
        }
    }
}