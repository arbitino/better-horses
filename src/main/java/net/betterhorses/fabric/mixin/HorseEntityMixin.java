package net.betterhorses.fabric.mixin;

import net.betterhorses.common.accessor.jump.JumpingHeightAccessor;
import net.betterhorses.common.accessor.speed.MoveSpeedAccessor;
import net.betterhorses.common.breed.BreedableHorse;
import net.betterhorses.common.breed.Breed;
import net.betterhorses.common.breed.BreedRegistry;
import net.betterhorses.common.progress.Progress;
import net.betterhorses.common.progress.ProgressableHorse;

import net.betterhorses.common.util.MathUtils;
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

import java.util.Objects;

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
    private static final TrackedData<NbtCompound> INITIAL_SPEED = DataTracker.registerData(
            HorseEntityMixin.class,
            TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    @Unique
    private static final TrackedData<NbtCompound> INITIAL_JUMP = DataTracker.registerData(
            HorseEntityMixin.class,
            TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    @Unique
    private static final String CUSTOM_SPEED_KEY = "HorseCustomSpeed";

    @Unique
    private static final String CUSTOM_JUMP_KEY = "HorseCustomJump";

    @Unique
    private static final String INITIAL_SPEED_KEY = "HorseInitialSpeed";

    @Unique
    private static final String INITIAL_JUMP_KEY = "HorseInitialJump";

    protected HorseEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    /* ------------------- BREED ------------------- */

    @Override
    public Breed getHorseBreed() {
        NbtCompound breedNbt = this.dataTracker.get(HORSE_BREED);
        if (breedNbt.isEmpty()) {
            return null;
        }

        return Breed.fromNbt(breedNbt);
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
        return Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.JUMP_STRENGTH)).getBaseValue();
    }

    @Override
    public double getJumpStrength() {
        return Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.JUMP_STRENGTH)).getValue();
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

    @Override
    public void setInitialMoveSpeed(double value) {
        if (this.getWorld().isClient()) return;
        
        NbtCompound nbt = new NbtCompound();
        nbt.putDouble(INITIAL_SPEED_KEY, value);
        this.dataTracker.set(INITIAL_SPEED, nbt);
    }

    @Override
    public double getInitialMoveSpeed() {
        return this.dataTracker.get(INITIAL_SPEED).getDouble(INITIAL_SPEED_KEY);
    }

    @Override
    public boolean hasInitialMoveSpeed() {
        return this.dataTracker.get(INITIAL_SPEED).getDouble(INITIAL_SPEED_KEY) > 0.0;
    }

    @Override
    public void setInitialJumpStrength(double value) {
        if (this.getWorld().isClient()) return;
        
        NbtCompound nbt = new NbtCompound();
        nbt.putDouble(INITIAL_JUMP_KEY, value);
        this.dataTracker.set(INITIAL_JUMP, nbt);
    }

    @Override
    public double getInitialJumpStrength() {
        return this.dataTracker.get(INITIAL_JUMP).getDouble(INITIAL_JUMP_KEY);
    }

    @Override
    public boolean hasInitialJumpStrength() {
        return this.dataTracker.get(INITIAL_JUMP).getDouble(INITIAL_JUMP_KEY) > 0.0;
    }

    /* ------------------- INIT TRACKER ------------------- */

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTrackerInject(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(HORSE_BREED, new NbtCompound()); // Пустое значение, порода будет установлена позже
        builder.add(HORSE_PROGRESS, Progress.empty().toNbt());

        NbtCompound speedNbt = new NbtCompound();
        speedNbt.putDouble(CUSTOM_SPEED_KEY, 0.0);
        builder.add(CUSTOM_BASE_SPEED, speedNbt);

        NbtCompound jumpNbt = new NbtCompound();
        jumpNbt.putDouble(CUSTOM_JUMP_KEY, 0.0);
        builder.add(CUSTOM_BASE_JUMP, jumpNbt);

        NbtCompound initialSpeedNbt = new NbtCompound();
        initialSpeedNbt.putDouble(INITIAL_SPEED_KEY, 0.0);
        builder.add(INITIAL_SPEED, initialSpeedNbt);

        NbtCompound initialJumpNbt = new NbtCompound();
        initialJumpNbt.putDouble(INITIAL_JUMP_KEY, 0.0);
        builder.add(INITIAL_JUMP, initialJumpNbt);
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

        // Initial values
        if (!hasInitialMoveSpeed()) {
            setInitialMoveSpeed(baseSpeed);
        }
        if (!hasInitialJumpStrength()) {
            setInitialJumpStrength(baseJump);
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

        nbt.putDouble(INITIAL_SPEED_KEY,
                this.dataTracker.get(INITIAL_SPEED).getDouble(INITIAL_SPEED_KEY));
        nbt.putDouble(INITIAL_JUMP_KEY,
                this.dataTracker.get(INITIAL_JUMP).getDouble(INITIAL_JUMP_KEY));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(Breed.KEY, NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(HORSE_BREED, nbt.getCompound(Breed.KEY));
        }

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

        // Initial values
        if (nbt.contains(INITIAL_SPEED_KEY, NbtElement.DOUBLE_TYPE)) {
            double initialSpeed = nbt.getDouble(INITIAL_SPEED_KEY);
            setInitialMoveSpeed(initialSpeed);
        }

        if (nbt.contains(INITIAL_JUMP_KEY, NbtElement.DOUBLE_TYPE)) {
            double initialJump = nbt.getDouble(INITIAL_JUMP_KEY);
            setInitialJumpStrength(initialJump);
        }
    }
}
