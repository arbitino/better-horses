package net.betterhorses.mixin;

import net.betterhorses.accessor.jump.JumpingHeightAccessor;
import net.betterhorses.accessor.speed.MoveSpeedAccessor;
import net.betterhorses.breed.BreedableHorse;
import net.betterhorses.breed.Breed;
import net.betterhorses.breed.BreedRegistry;
import net.betterhorses.progress.Progress;
import net.betterhorses.progress.ProgressableHorse;

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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseEntity.class)
public abstract class HorseEntityMixin extends AnimalEntity implements BreedableHorse, ProgressableHorse, JumpingHeightAccessor, MoveSpeedAccessor {
    @Unique
    private static final TrackedData<NbtCompound> HORSE_BREED = DataTracker.registerData(
        HorseEntityMixin.class,
        TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    @Unique
    private static final TrackedData<NbtCompound> HORSE_PROGRESS = DataTracker.registerData(
        HorseEntityMixin.class,
        TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    @Unique
    private static final TrackedData<Float> CUSTOM_BASE_SPEED = DataTracker.registerData(
        HorseEntityMixin.class,
        TrackedDataHandlerRegistry.FLOAT
    );

    @Unique
    private static final TrackedData<Float> CUSTOM_BASE_JUMP = DataTracker.registerData(
        HorseEntityMixin.class,
        TrackedDataHandlerRegistry.FLOAT
    );

    @Unique
    private static final String CUSTOM_SPEED_KEY = "HorseCustomSpeed";

    @Unique
    private static final String CUSTOM_JUMP_KEY = "HorseCustomJump";

    protected HorseEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    /* ------------------- BREED ------------------- */

    @Override
    public Breed getHorseBreed() {
        return Breed.fromNbt(this.dataTracker.get(HORSE_BREED));
    }

    @Override
    public void setHorseBreed(Breed breed) {
        this.dataTracker.set(HORSE_BREED, breed.toNbt());
    }

    /* ------------------- PROGRESS ------------------- */

    @Override
    public Progress getProgress() {
        return Progress.fromNbt(this.dataTracker.get(HORSE_PROGRESS));
    }

    @Override
    public void setProgress(Progress progress) {
        this.dataTracker.set(HORSE_PROGRESS, progress.toNbt());
    }

    /* ------------------- JUMP ------------------- */

    @Override
    public double getBaseJumpStrength() {
        return this.getAttributeInstance(EntityAttributes.JUMP_STRENGTH).getBaseValue();
    }

    @Override
    public double getJumpStrength() {
        return this.getAttributeInstance(EntityAttributes.JUMP_STRENGTH).getValue();
    }

    @Override
    public double getBaseJumpHeight() {
        return this.calcJumpHeight(this.getBaseJumpStrength());
    }

    @Override
    public double getJumpHeight() {
        return this.calcJumpHeight(this.getJumpStrength());
    }

    @Override
    public void setBaseJumpStrength(double value) {
        if (this.getWorld().isClient()) {
            return;
        }

        EntityAttributeInstance jumpStrengthAttribute = this.getAttributeInstance(EntityAttributes.JUMP_STRENGTH);
        if (jumpStrengthAttribute != null) {
            jumpStrengthAttribute.setBaseValue(value);
        }

        this.dataTracker.set(CUSTOM_BASE_JUMP, (float)value);
    }

    /* ------------------- SPEED ------------------- */

    @Override
    public double getBaseMoveSpeed() {
        return this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).getBaseValue();
    }

    @Override
    public double getMoveSpeed() {
        return this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED).getValue();
    }

    @Override
    public void setBaseMoveSpeed(double value) {
        if (this.getWorld().isClient()) {
            return;
        }

        EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(value);
        }
        this.dataTracker.set(CUSTOM_BASE_SPEED, (float)value);
    }

    /* ------------------- INIT TRACKER ------------------- */

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTrackerInject(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(HORSE_BREED, BreedRegistry.getDefault().toNbt());
        builder.add(HORSE_PROGRESS, Progress.empty().toNbt());
        builder.add(CUSTOM_BASE_SPEED, (float)0.0); // временно
        builder.add(CUSTOM_BASE_JUMP, (float)0.0); // временно
    }

    /* ------------------- ATTRIBUTES INIT ------------------- */

    @Inject(method = "initAttributes", at = @At("TAIL"))
    private void onInitAttributes(Random random, CallbackInfo ci) {
        double baseSpeed = this.getBaseMoveSpeed();

        if (this.dataTracker.get(CUSTOM_BASE_SPEED) <= 0.0) {
            this.dataTracker.set(CUSTOM_BASE_SPEED, (float)baseSpeed);
        } else {
            this.setBaseMoveSpeed(this.dataTracker.get(CUSTOM_BASE_SPEED));
        }

        double baseJumpStrength = this.getBaseJumpStrength();

        if (this.dataTracker.get(CUSTOM_BASE_JUMP) <= 0.0) {
            this.dataTracker.set(CUSTOM_BASE_JUMP, (float)baseJumpStrength);
        } else {
            this.setBaseJumpStrength(this.dataTracker.get(CUSTOM_BASE_JUMP));
        }
    }

    /* ------------------- SAVE / LOAD ------------------- */

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put(Breed.KEY, this.dataTracker.get(HORSE_BREED));
        nbt.put(Progress.KEY, this.dataTracker.get(HORSE_PROGRESS));

        double speed = this.dataTracker.get(CUSTOM_BASE_SPEED);
        nbt.putDouble(CUSTOM_SPEED_KEY, speed);

        double jumpStrength = this.dataTracker.get(CUSTOM_BASE_JUMP);
        nbt.putDouble(CUSTOM_JUMP_KEY, jumpStrength);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(Breed.KEY, NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(HORSE_BREED, nbt.getCompound(Breed.KEY));
        } else if (!this.getWorld().isClient()) {
            Breed breed = BreedRegistry.get("default");
            if (breed != null) {
                this.setHorseBreed(breed);
            }
        }

        if (nbt.contains(Progress.KEY, NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(HORSE_PROGRESS, nbt.getCompound(Progress.KEY));
        } else {
            this.dataTracker.set(HORSE_PROGRESS, Progress.empty().toNbt());
        }

        if (nbt.contains(CUSTOM_SPEED_KEY, NbtElement.DOUBLE_TYPE)) {
            double savedSpeed = nbt.getDouble(CUSTOM_SPEED_KEY);
            this.dataTracker.set(CUSTOM_BASE_SPEED, (float)savedSpeed);

            EntityAttributeInstance speedAttribute = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.setBaseValue(savedSpeed);
            }
        }

        if (nbt.contains(CUSTOM_JUMP_KEY, NbtElement.DOUBLE_TYPE)) {
            double savedJumpStrength = nbt.getDouble(CUSTOM_JUMP_KEY);
            this.dataTracker.set(CUSTOM_BASE_SPEED, (float)savedJumpStrength);

            EntityAttributeInstance jumpAttribute = this.getAttributeInstance(EntityAttributes.JUMP_STRENGTH);
            if (jumpAttribute != null) {
                jumpAttribute.setBaseValue(savedJumpStrength);
            }
        }
    }

    /* ------------------- UTILS ------------------- */

    @Unique
    private double calcJumpHeight(double value) {
        return -0.1817584952 * value * value + 3.689713992 * value + 0.4842167484;
    }
}
