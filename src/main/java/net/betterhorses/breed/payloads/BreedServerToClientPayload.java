package net.betterhorses.breed.payloads;

import net.betterhorses.BetterHorses;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BreedServerToClientPayload(int entityId, NbtCompound breedData) implements CustomPayload {
    public static final Id<BreedServerToClientPayload> ID = new Id<>(Identifier.of(BetterHorses.MOD_ID, "sync_horse_breed"));

    public static final PacketCodec<RegistryByteBuf, BreedServerToClientPayload> CODEC = PacketCodec.of(
            (packet, buf) -> {
                buf.writeVarInt(packet.entityId);
                buf.writeNbt(packet.breedData);
            },
            buf -> new BreedServerToClientPayload(buf.readVarInt(), buf.readNbt())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
