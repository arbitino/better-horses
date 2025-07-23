package net.betterhorses.mixin;

import net.betterhorses.breed.BreedableHorse;
import net.betterhorses.breed.Breed;
import net.betterhorses.breed.BreedRegistry;
import net.betterhorses.breed.payloads.BreedServerToClientPayload;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseEntity.class)
public abstract class MixinHorseEntity extends AnimalEntity implements BreedableHorse {
    private static final TrackedData<NbtCompound> HORSE_BREED =
            DataTracker.registerData(HorseEntity.class, TrackedDataHandlerRegistry.NBT_COMPOUND);

    private boolean breedInitialized = false;

    protected MixinHorseEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initBreedData(DataTracker.Builder builder, CallbackInfo ci) {
        builder.add(HORSE_BREED, BreedRegistry.getDefault().toNbt());
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeBreedData(NbtCompound nbt, CallbackInfo ci) {
        nbt.put("HorseBreed", this.dataTracker.get(HORSE_BREED));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readBreedData(NbtCompound nbt, CallbackInfo ci) {
        if (this.breedInitialized) return;

        if (nbt.contains("HorseBreed", NbtElement.COMPOUND_TYPE)) {
            this.dataTracker.set(HORSE_BREED, nbt.getCompound("HorseBreed"));
            this.breedInitialized = true;
        } else if (!this.getWorld().isClient()) {
            Breed mustang = BreedRegistry.get("mustang");

            if (mustang != null) {
                this.setHorseBreed(mustang);
            }
        }
    }

    @Override
    public Breed getHorseBreed() {
        return Breed.fromNbt(this.dataTracker.get(HORSE_BREED));
    }

    @Override
    public void setHorseBreed(Breed breed) {
        this.dataTracker.set(HORSE_BREED, breed.toNbt());
        this.breedInitialized = true;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);

        if (data.equals(HORSE_BREED) && !this.getWorld().isClient()) {
            this.syncBreedToClients();
        }
    }

    private void syncBreedToClients() {
        NbtCompound breedData = this.dataTracker.get(HORSE_BREED);
        BreedServerToClientPayload packet = new BreedServerToClientPayload(this.getId(), breedData);

        PlayerLookup.tracking(this).forEach(player -> {
            ServerPlayNetworking.send(player, packet);
        });
    }
}
