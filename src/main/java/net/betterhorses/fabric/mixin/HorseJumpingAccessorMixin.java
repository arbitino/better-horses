package net.betterhorses.fabric.mixin;

import net.betterhorses.common.accessor.JumpingAccessor;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorseEntity.class)
public class HorseJumpingAccessorMixin implements JumpingAccessor {
    @Shadow
    protected boolean jumping;

    @Unique
    private boolean wasJumpingLastTick = false;

    @Unique
    private static final TrackedData<NbtCompound> CUSTOM_BASE_JUMP = DataTracker.registerData(
            AbstractHorseEntity.class,
            TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    @Unique
    private static final TrackedData<NbtCompound> INITIAL_JUMP = DataTracker.registerData(
            AbstractHorseEntity.class,
            TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    @Unique
    private static final String CUSTOM_JUMP_KEY = "HorseCustomJump";

    @Unique
    private static final String INITIAL_JUMP_KEY = "HorseInitialJump";

    @Override
    public boolean isJumping() {
        return this.jumping;
    }

    @Override
    public boolean wasJumpingLastTick() {
        return wasJumpingLastTick;
    }

    @Override
    public void setWasJumpingLastTick(boolean value) {
        this.wasJumpingLastTick = value;
    }

    @Override
    public double getBaseJumpStrength() {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        EntityAttributeInstance jumpAttr = self.getAttributeInstance(EntityAttributes.JUMP_STRENGTH);
        return jumpAttr != null ? jumpAttr.getBaseValue() : 0.0;
    }

    @Override
    public double getJumpStrength() {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        EntityAttributeInstance jumpAttr = self.getAttributeInstance(EntityAttributes.JUMP_STRENGTH);
        return jumpAttr != null ? jumpAttr.getValue() : 0.0;
    }

    @Override
    public void setBaseJumpStrength(double value) {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        if (self.getWorld().isClient()) return;

        EntityAttributeInstance jumpAttr = self.getAttributeInstance(EntityAttributes.JUMP_STRENGTH);
        if (jumpAttr != null) {
            jumpAttr.setBaseValue(value);
        }

        NbtCompound nbt = new NbtCompound();
        nbt.putDouble(CUSTOM_JUMP_KEY, value);
        self.getDataTracker().set(CUSTOM_BASE_JUMP, nbt);
    }

    @Override
    public void setInitialJumpStrength(double value) {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        if (self.getWorld().isClient()) return;
        
        NbtCompound nbt = new NbtCompound();
        nbt.putDouble(INITIAL_JUMP_KEY, value);
        self.getDataTracker().set(INITIAL_JUMP, nbt);
    }

    @Override
    public double getInitialJumpStrength() {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        return self.getDataTracker().get(INITIAL_JUMP).getDouble(INITIAL_JUMP_KEY);
    }

    @Override
    public boolean hasInitialJumpStrength() {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        return self.getDataTracker().get(INITIAL_JUMP).getDouble(INITIAL_JUMP_KEY) > 0.0;
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initJumpDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        NbtCompound jumpNbt = new NbtCompound();
        jumpNbt.putDouble(CUSTOM_JUMP_KEY, 0.0);
        builder.add(CUSTOM_BASE_JUMP, jumpNbt);

        NbtCompound initialJumpNbt = new NbtCompound();
        initialJumpNbt.putDouble(INITIAL_JUMP_KEY, 0.0);
        builder.add(INITIAL_JUMP, initialJumpNbt);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeJumpData(NbtCompound nbt, CallbackInfo ci) {
        AbstractHorseEntity self = (AbstractHorseEntity) (Object) this;
        nbt.putDouble(CUSTOM_JUMP_KEY,
                self.getDataTracker().get(CUSTOM_BASE_JUMP).getDouble(CUSTOM_JUMP_KEY));

        nbt.putDouble(INITIAL_JUMP_KEY,
                self.getDataTracker().get(INITIAL_JUMP).getDouble(INITIAL_JUMP_KEY));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readJumpData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(CUSTOM_JUMP_KEY, NbtElement.DOUBLE_TYPE)) {
            double savedJump = nbt.getDouble(CUSTOM_JUMP_KEY);
            setBaseJumpStrength(savedJump);
        }

        if (nbt.contains(INITIAL_JUMP_KEY, NbtElement.DOUBLE_TYPE)) {
            double initialJump = nbt.getDouble(INITIAL_JUMP_KEY);
            setInitialJumpStrength(initialJump);
        }
    }
}