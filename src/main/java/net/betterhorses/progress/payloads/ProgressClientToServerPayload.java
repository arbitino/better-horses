package net.betterhorses.progress.payloads;

import net.betterhorses.BetterHorses;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ProgressClientToServerPayload(int entityId, NbtCompound progressData) implements CustomPayload {
    public static final CustomPayload.Id<ProgressClientToServerPayload> ID = new CustomPayload.Id<>(
        Identifier.of(BetterHorses.MOD_ID, "request_progress_update")
    );

    public static final PacketCodec<RegistryByteBuf, ProgressClientToServerPayload> CODEC = PacketCodec.of(
        (request, buf) -> {
            buf.writeVarInt(request.entityId);
            buf.writeNbt(request.progressData);
        },
        buf -> new ProgressClientToServerPayload(buf.readVarInt(), buf.readNbt())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
