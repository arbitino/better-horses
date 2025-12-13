package net.betterhorses.fabric.mixin;

import net.betterhorses.common.BetterHorses;
import net.betterhorses.common.accessor.MoveSpeedAccessor;

import net.betterhorses.common.progress.HorseAttributeProgression;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(HorseEntity.class)
public abstract class HorseMoveSpeedMixin extends AnimalEntity implements MoveSpeedAccessor {
    @Unique
    private static final TrackedData<NbtCompound> CUSTOM_BASE_SPEED = DataTracker.registerData(
            HorseMoveSpeedMixin.class,
            TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    @Unique
    private static final TrackedData<NbtCompound> INITIAL_SPEED = DataTracker.registerData(
            HorseMoveSpeedMixin.class,
            TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    @Unique
    private static final String CUSTOM_SPEED_KEY = "HorseCustomSpeed";

    @Unique
    private static final String INITIAL_SPEED_KEY = "HorseInitialSpeed";

    protected HorseMoveSpeedMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public double betterHorses$getBaseMoveSpeed() {
        return Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED)).getBaseValue();
    }

    @Override
    public double betterHorses$getMoveSpeed() {
        return Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED)).getValue();
    }

    @Override
    public void betterHorses$setBaseMoveSpeed(double value) {
        if (this.getWorld().isClient()) return;

        BetterHorses.LOGGER.info("HorseMoveSpeedMixin.setBaseMoveSpeed() для лошади " + this.getUuid() + " - устанавливается скорость: " + value);

        EntityAttributeInstance speedAttr = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            double oldValue = speedAttr.getBaseValue();
            speedAttr.setBaseValue(value);
            BetterHorses.LOGGER.info("Атрибут скорости изменен с " + oldValue + " на " + value);
        }

        NbtCompound nbt = new NbtCompound();
        nbt.putDouble(CUSTOM_SPEED_KEY, value);
        this.dataTracker.set(CUSTOM_BASE_SPEED, nbt);
    }

    @Override
    public void betterHorses$setInitialMoveSpeed(double value) {
        if (this.getWorld().isClient()) return;
        
        NbtCompound nbt = new NbtCompound();
        nbt.putDouble(INITIAL_SPEED_KEY, value);
        this.dataTracker.set(INITIAL_SPEED, nbt);
    }

    @Override
    public double betterHorses$getInitialMoveSpeed() {
        return this.dataTracker.get(INITIAL_SPEED).getDouble(INITIAL_SPEED_KEY);
    }

    @Override
    public boolean betterHorses$hasInitialMoveSpeed() {
        return this.dataTracker.get(INITIAL_SPEED).getDouble(INITIAL_SPEED_KEY) > 0.0;
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initSpeedDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        NbtCompound speedNbt = new NbtCompound();
        speedNbt.putDouble(CUSTOM_SPEED_KEY, 0.0);
        builder.add(CUSTOM_BASE_SPEED, speedNbt);

        NbtCompound initialSpeedNbt = new NbtCompound();
        initialSpeedNbt.putDouble(INITIAL_SPEED_KEY, 0.0);
        builder.add(INITIAL_SPEED, initialSpeedNbt);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeSpeedData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putDouble(CUSTOM_SPEED_KEY,
                this.dataTracker.get(CUSTOM_BASE_SPEED).getDouble(CUSTOM_SPEED_KEY));

        nbt.putDouble(INITIAL_SPEED_KEY,
                this.dataTracker.get(INITIAL_SPEED).getDouble(INITIAL_SPEED_KEY));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readSpeedData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(CUSTOM_SPEED_KEY, NbtElement.DOUBLE_TYPE)) {
            double savedSpeed = nbt.getDouble(CUSTOM_SPEED_KEY);
            betterHorses$setBaseMoveSpeed(savedSpeed);
        }

        if (nbt.contains(INITIAL_SPEED_KEY, NbtElement.DOUBLE_TYPE)) {
            double initialSpeed = nbt.getDouble(INITIAL_SPEED_KEY);
            betterHorses$setInitialMoveSpeed(initialSpeed);
        }

        restoreAttributesFromProgress();
    }
    
    @Unique
    private void restoreAttributesFromProgress() {
        if (this.getWorld().isClient()) return;

        Objects.requireNonNull(this.getWorld().getServer()).execute(() -> {
            HorseAttributeProgression.restoreSpeedFromProgress((HorseEntity)(Object)this);
        });
    }
}