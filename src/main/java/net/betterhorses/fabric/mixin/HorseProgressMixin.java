package net.betterhorses.fabric.mixin;

import net.betterhorses.common.progress.Progress;
import net.betterhorses.common.progress.ProgressableHorse;

import net.minecraft.entity.EntityType;
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

@Mixin(HorseEntity.class)
public abstract class HorseProgressMixin extends AnimalEntity implements ProgressableHorse {
    @Unique
    private static final TrackedData<NbtCompound> HORSE_PROGRESS = DataTracker.registerData(
            HorseProgressMixin.class,
            TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    protected HorseProgressMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public Progress betterHorses$getProgress() {
        return Progress.fromNbt(this.dataTracker.get(HORSE_PROGRESS));
    }

    @Override
    public void betterHorses$setProgress(Progress progress) {
        this.dataTracker.set(HORSE_PROGRESS, progress.toNbt());
    }


    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initProgressDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(HORSE_PROGRESS, Progress.empty().toNbt());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeProgressData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put(Progress.KEY, this.dataTracker.get(HORSE_PROGRESS));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readProgressData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(Progress.KEY, NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(HORSE_PROGRESS, nbt.getCompound(Progress.KEY));
        } else {
            this.dataTracker.set(HORSE_PROGRESS, Progress.empty().toNbt());
        }
    }
}