package net.betterhorses.network;

import net.betterhorses.breed.payloads.BreedServerToClientPayload;
import net.betterhorses.breed.payloads.BreedClientToServerPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class BetterHorsesPayloads {
    public static void register() {
        // Регистрация серверного пакета
        PayloadTypeRegistry.playS2C().register(
            BreedServerToClientPayload.ID,
            BreedServerToClientPayload.CODEC
        );

        // Регистрация клиентского пакета
        PayloadTypeRegistry.playC2S().register(
            BreedClientToServerPayload.ID,
            BreedClientToServerPayload.CODEC
        );
    }
}
