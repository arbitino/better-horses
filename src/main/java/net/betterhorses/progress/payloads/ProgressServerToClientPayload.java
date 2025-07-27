package net.betterhorses.progress.payloads;

import net.betterhorses.BetterHorses;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ProgressServerToClientPayload(int entityId, NbtCompound progressData) implements CustomPayload {
    public static final Id<ProgressServerToClientPayload> ID = new Id<>(Identifier.of(BetterHorses.MOD_ID, "sync_horse_progress"));

    public static final PacketCodec<RegistryByteBuf, ProgressServerToClientPayload> CODEC = PacketCodec.of(
        (packet, buf) -> {
            buf.writeVarInt(packet.entityId);
            buf.writeNbt(packet.progressData);
        },
        buf -> new ProgressServerToClientPayload(buf.readVarInt(), buf.readNbt())
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
