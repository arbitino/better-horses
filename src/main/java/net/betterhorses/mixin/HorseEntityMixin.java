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
    private static final TrackedData<NbtCompound> CUSTOM_BASE_SPEED = DataTracker.registerData(
            HorseEntityMixin.class,
            TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    @Unique
    private static final TrackedData<NbtCompound> CUSTOM_BASE_JUMP = DataTracker.registerData(
            HorseEntityMixin.class,
            TrackedDataHandlerRegistry.NBT_COMPOUND
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
        if (this.getWorld().isClient()) return;

        EntityAttributeInstance jumpAttr = this.getAttributeInstance(EntityAttributes.JUMP_STRENGTH);
        if (jumpAttr != null) {
            jumpAttr.setBaseValue(value);
        }

        NbtCompound nbt = new NbtCompound();
        nbt.putDouble(CUSTOM_JUMP_KEY, value);
        this.dataTracker.set(CUSTOM_BASE_JUMP, nbt);
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
        if (this.getWorld().isClient()) return;

        EntityAttributeInstance speedAttr = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(value);
        }

        NbtCompound nbt = new NbtCompound();
        nbt.putDouble(CUSTOM_SPEED_KEY, value);
        this.dataTracker.set(CUSTOM_BASE_SPEED, nbt);
    }

    /* ------------------- INIT TRACKER ------------------- */

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTrackerInject(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(HORSE_BREED, BreedRegistry.getRandomBreed().toNbt());
        builder.add(HORSE_PROGRESS, Progress.empty().toNbt());

        NbtCompound speedNbt = new NbtCompound();
        speedNbt.putDouble(CUSTOM_SPEED_KEY, 0.0);
        builder.add(CUSTOM_BASE_SPEED, speedNbt);

        NbtCompound jumpNbt = new NbtCompound();
        jumpNbt.putDouble(CUSTOM_JUMP_KEY, 0.0);
        builder.add(CUSTOM_BASE_JUMP, jumpNbt);
    }

    /* ------------------- ATTRIBUTES INIT ------------------- */

    @Inject(method = "initAttributes", at = @At("TAIL"))
    private void onInitAttributes(Random random, CallbackInfo ci) {
        // Speed
        double baseSpeed = this.getBaseMoveSpeed();
        double savedSpeed = this.dataTracker.get(CUSTOM_BASE_SPEED).getDouble(CUSTOM_SPEED_KEY);
        if (savedSpeed <= 0.0) {
            setBaseMoveSpeed(baseSpeed);
        } else {
            setBaseMoveSpeed(savedSpeed);
        }

        // Jump
        double baseJump = this.getBaseJumpStrength();
        double savedJump = this.dataTracker.get(CUSTOM_BASE_JUMP).getDouble(CUSTOM_JUMP_KEY);
        if (savedJump <= 0.0) {
            setBaseJumpStrength(baseJump);
        } else {
            setBaseJumpStrength(savedJump);
        }
    }

    /* ------------------- SAVE / LOAD ------------------- */

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put(Breed.KEY, this.dataTracker.get(HORSE_BREED));
        nbt.put(Progress.KEY, this.dataTracker.get(HORSE_PROGRESS));

        nbt.putDouble(CUSTOM_SPEED_KEY,
                this.dataTracker.get(CUSTOM_BASE_SPEED).getDouble(CUSTOM_SPEED_KEY));
        nbt.putDouble(CUSTOM_JUMP_KEY,
                this.dataTracker.get(CUSTOM_BASE_JUMP).getDouble(CUSTOM_JUMP_KEY));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomData(NbtCompound nbt, CallbackInfo ci) {
        // Breed
        if (nbt.contains(Breed.KEY, NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(HORSE_BREED, nbt.getCompound(Breed.KEY));
        } else if (!this.getWorld().isClient()) {
            Breed breed = BreedRegistry.getRandomBreed();
            if (breed != null) setHorseBreed(breed);
        }

        // Progress
        if (nbt.contains(Progress.KEY, NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(HORSE_PROGRESS, nbt.getCompound(Progress.KEY));
        } else {
            this.dataTracker.set(HORSE_PROGRESS, Progress.empty().toNbt());
        }

        // Speed
        if (nbt.contains(CUSTOM_SPEED_KEY, NbtElement.DOUBLE_TYPE)) {
            double savedSpeed = nbt.getDouble(CUSTOM_SPEED_KEY);
            setBaseMoveSpeed(savedSpeed);
        }

        // Jump
        if (nbt.contains(CUSTOM_JUMP_KEY, NbtElement.DOUBLE_TYPE)) {
            double savedJump = nbt.getDouble(CUSTOM_JUMP_KEY);
            setBaseJumpStrength(savedJump);
        }
    }

    /* ------------------- UTILS ------------------- */

    @Unique
    private double calcJumpHeight(double value) {
        return -0.1817584952 * value * value + 3.689713992 * value + 0.4842167484;
    }
}
