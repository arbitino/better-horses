package net.betterhorses.fabric.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.betterhorses.common.BetterHorses;
import net.betterhorses.common.breed.BreedRegistry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BreedDataLoader implements SimpleSynchronousResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_TYPE = "breeds";

    @Override
    public Identifier getFabricId() {
        return Identifier.of(BetterHorses.MOD_ID, "breed_data_loader");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<String, JsonObject> loadedBreedData = new HashMap<>();

        Map<Identifier, Resource> resources = manager.findResources(DATA_TYPE, id -> id.getPath().endsWith(".json"));
        
        BetterHorses.LOGGER.info("Найдено файлов пород для загрузки: {}", resources.size());
        
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            Resource resource = entry.getValue();
            
            try {
                String fileName = fileId.getPath().replace(DATA_TYPE + "/", "").replace(".json", "");
                String breedId = fileId.getNamespace() + ":" + fileName;
                JsonObject json = parseJsonFile(resource);

                if (json != null) {
                    loadedBreedData.put(breedId, json);
                    BetterHorses.LOGGER.info("Загружен JSON файл породы: {}", breedId);
                }
            } catch (Exception e) {
                BetterHorses.LOGGER.error("Ошибка загрузки JSON файла породы: {}", fileId, e);
            }
        }

        BreedRegistry.initFromJSON(loadedBreedData);
    }

    private JsonObject parseJsonFile(Resource resource) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        )) {
            JsonElement jsonElement = GSON.fromJson(reader, JsonElement.class);

            if (jsonElement != null && jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject();
            }
        } catch (IOException | JsonParseException e) {
            BetterHorses.LOGGER.error("Не удалось распарсить JSON файл", e);
        }

        return null;
    }
}