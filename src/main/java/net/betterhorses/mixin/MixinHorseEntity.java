package net.betterhorses.mixin;

import net.betterhorses.breed.BreedableHorse;
import net.betterhorses.breed.Breed;
import net.betterhorses.breed.BreedRegistry;
import net.betterhorses.breed.payloads.BreedServerToClientPayload;
import net.betterhorses.progress.Progress;
import net.betterhorses.progress.ProgressableHorse;
import net.betterhorses.progress.payloads.ProgressServerToClientPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
public abstract class MixinHorseEntity extends AnimalEntity implements BreedableHorse, ProgressableHorse {
    @Unique
    private static final TrackedData<NbtCompound> HORSE_BREED = DataTracker.registerData(
        MixinHorseEntity.class,
        TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    @Unique
    private static final TrackedData<NbtCompound> HORSE_PROGRESS = DataTracker.registerData(
        MixinHorseEntity.class,
        TrackedDataHandlerRegistry.NBT_COMPOUND
    );

    protected MixinHorseEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initBreedData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(HORSE_BREED, BreedRegistry.getDefault().toNbt());
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initProgressData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(HORSE_PROGRESS, Progress.empty().toNbt());  // пустой прогресс по умолчанию
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeBreedData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put(Breed.KEY, this.dataTracker.get(HORSE_BREED));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeProgressData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put(Progress.KEY, this.dataTracker.get(HORSE_PROGRESS));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readBreedData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(Breed.KEY, NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(HORSE_BREED, nbt.getCompound(Breed.KEY));
        } else if (!this.getWorld().isClient()) {
            Breed breed = BreedRegistry.get("default");

            if (breed != null) {
                this.setHorseBreed(breed);
            }
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readProgressData(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(Progress.KEY, NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(HORSE_PROGRESS, nbt.getCompound(Progress.KEY));
        } else {
            // Если нет данных — создаём пустой прогресс
            this.dataTracker.set(HORSE_PROGRESS, Progress.empty().toNbt());
        }
    }

    @Override
    public Breed getHorseBreed() {
        return Breed.fromNbt(this.dataTracker.get(HORSE_BREED));
    }

    @Override
    public void setHorseBreed(Breed breed) {
        this.dataTracker.set(HORSE_BREED, breed.toNbt());
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (this.getWorld().isClient()) {
            return;
        }

        if (data.equals(HORSE_BREED)) {
            this.syncBreedToClients();
        }

        if (data.equals(HORSE_PROGRESS)) {
            this.syncProgressToClients();
        }
    }

    @Override
    public Progress getProgress() {
        return Progress.fromNbt(this.dataTracker.get(HORSE_PROGRESS));
    }

    @Override
    public void setProgress(Progress progress) {
        this.dataTracker.set(HORSE_PROGRESS, progress.toNbt());
    }

    @Unique
    private void syncBreedToClients() {
        NbtCompound breedData = this.dataTracker.get(HORSE_BREED);
        BreedServerToClientPayload packet = new BreedServerToClientPayload(this.getId(), breedData);

        PlayerLookup.tracking(this).forEach(player -> {
            ServerPlayNetworking.send(player, packet);
        });
    }

    @Unique
    private void syncProgressToClients() {
        NbtCompound progressData = this.dataTracker.get(HORSE_PROGRESS);
        ProgressServerToClientPayload packet = new ProgressServerToClientPayload(this.getId(), progressData);

        PlayerLookup.tracking(this).forEach(player -> {
            ServerPlayNetworking.send(player, packet);
        });
    }
}
