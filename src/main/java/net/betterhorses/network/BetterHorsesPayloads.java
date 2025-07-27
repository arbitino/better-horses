package net.betterhorses.network;

import net.betterhorses.breed.payloads.BreedServerToClientPayload;
import net.betterhorses.breed.payloads.BreedClientToServerPayload;
import net.betterhorses.progress.payloads.ProgressClientToServerPayload;
import net.betterhorses.progress.payloads.ProgressServerToClientPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class BetterHorsesPayloads {
    public static void register() {
        //порода server -> client
        PayloadTypeRegistry.playS2C().register(
            BreedServerToClientPayload.ID,
            BreedServerToClientPayload.CODEC
        );

        //порода client -> server
        PayloadTypeRegistry.playC2S().register(
            BreedClientToServerPayload.ID,
            BreedClientToServerPayload.CODEC
        );

        //прогресс server -> client
        PayloadTypeRegistry.playS2C().register(
            ProgressServerToClientPayload.ID,
            ProgressServerToClientPayload.CODEC
        );

        //прогресс client -> server
        PayloadTypeRegistry.playC2S().register(
            ProgressClientToServerPayload.ID,
            ProgressClientToServerPayload.CODEC
        );
    }
}
