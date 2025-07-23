package net.betterhorses.breed.payloads;

import net.betterhorses.BetterHorses;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

// Пакет запроса (клиент → сервер)
public record BreedClientToServerPayload(int entityId, NbtCompound breedData) implements CustomPayload {
    public static final Id<BreedClientToServerPayload> ID = new Id<>(Identifier.of(BetterHorses.MOD_ID, "request_breed_update"));

    public static final PacketCodec<RegistryByteBuf, BreedClientToServerPayload> CODEC = PacketCodec.of(
            (request, buf) -> {
                buf.writeVarInt(request.entityId);
                buf.writeNbt(request.breedData);
            },
            buf -> new BreedClientToServerPayload(buf.readVarInt(), buf.readNbt())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
