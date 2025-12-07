package net.betterhorses.fabric.mixin;

import net.betterhorses.common.breed.BreedableHorse;
import net.betterhorses.common.breed.Breed;

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
public abstract class HorseBreedMixin extends AnimalEntity implements BreedableHorse {
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

    protected HorseBreedMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    private static final TrackedData<NbtCompound> HORSE_BREED = DataTracker.registerData(
            HorseBreedMixin.class,
            TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initBreedDataTracker(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(HORSE_BREED, new NbtCompound()); // Пустое значение, порода будет установлена позже
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeBreedData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put(Breed.KEY, this.dataTracker.get(HORSE_BREED));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readBreedData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(Breed.KEY, NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(HORSE_BREED, nbt.getCompound(Breed.KEY));
        }
    }
}