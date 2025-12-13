package net.betterhorses.common.breed;

import com.google.gson.JsonObject;
import net.betterhorses.common.BetterHorses;
import net.betterhorses.common.util.MathUtils;

import java.util.*;

public class BreedRegistry {
    private static final Map<String, Breed> REGISTRY = new HashMap<>();
    private static final Random RANDOM = new Random();

    public static Breed getRandomBreed() {
        List<Breed> breeds = new ArrayList<>(REGISTRY.values());
        
        if (breeds.isEmpty()) {
            BetterHorses.LOGGER.error("Нет зарегистрированных пород! JSON файлы не загружены.");
            return null;
        }

        Breed breed = breeds.get(RANDOM.nextInt(breeds.size()));
        BetterHorses.LOGGER.info("Выбрана порода: {}", breed.getDisplayName());

        return breed;
    }

    public static void initFromJSON(Map<String, JsonObject> breedJsonData) {
        REGISTRY.clear();
        BetterHorses.LOGGER.info("BreedRegistry инициализирован");
        
        if (breedJsonData.isEmpty()) {
            BetterHorses.LOGGER.error("КРИТИЧЕСКАЯ ОШИБКА: JSON данные пород пусты! Мод не будет работать корректно.");
            return;
        }
        
        int successCount = 0;
        int failCount = 0;
        
        for (Map.Entry<String, JsonObject> entry : breedJsonData.entrySet()) {
            String breedId = entry.getKey();
            JsonObject json = entry.getValue();
            
            try {
                Breed breed = parseBreedFromJson(breedId, json);
                if (breed != null) {
                    REGISTRY.put(breedId, breed);
                    successCount++;
                    BetterHorses.LOGGER.info("Успешно загружена порода: {} ({})", breedId, breed.translationKey());
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                BetterHorses.LOGGER.error("Ошибка парсинга породы {}: {}", breedId, e.getMessage());
                failCount++;
            }
        }
        
        if (successCount > 0) {
            BetterHorses.LOGGER.info("Загрузка пород завершена: успешно {} из {} (неудач: {})", 
                successCount, breedJsonData.size(), failCount);
        } else {
            BetterHorses.LOGGER.error("КРИТИЧЕСКАЯ ОШИБКА: Ни одна порода не загружена! Мод не будет работать корректно.");
        }
    }

    private static Breed parseBreedFromJson(String breedId, JsonObject json) {
        try {
            if (!json.has("translation_key")) {
                BetterHorses.LOGGER.error("У породы {} отсутствует обязательное поле: translation_key", breedId);
                return null;
            }
            
            String translationKey = json.get("translation_key").getAsString();
            double maxJumpHeight = json.has("max_jump_height") ? json.get("max_jump_height").getAsDouble() : 3.5;
            double maxSpeed = json.has("max_speed") ? json.get("max_speed").getAsDouble() : 10.5;
            int obedienceLevel = json.has("obedience") ? json.get("obedience").getAsInt() : 2;
            float speedGrowthMultiplier = json.has("speed_growth_multiplier") ? json.get("speed_growth_multiplier").getAsFloat() : 1.0f;
            float jumpGrowthMultiplier = json.has("jump_growth_multiplier") ? json.get("jump_growth_multiplier").getAsFloat() : 1.0f;

            Breed.ObedienceLevel obedience = switch (obedienceLevel) {
                case 1 -> Breed.ObedienceLevel.VERY_OBEDIENT;
                case 3 -> Breed.ObedienceLevel.DISOBEDIENT;
                default -> Breed.ObedienceLevel.NORMAL;
            };

            double jumpAttribute = MathUtils.blocksToJumpAttribute(maxJumpHeight);
            double speedAttribute = MathUtils.blocksPerSecondToMoveAttribute(maxSpeed);
            
            return new Breed(
                breedId,
                translationKey,
                jumpAttribute,
                speedAttribute,
                obedience,
                speedGrowthMultiplier,
                jumpGrowthMultiplier
            );
            
        } catch (Exception e) {
            BetterHorses.LOGGER.error("Не удалось распарсить породу {}: {}", breedId, e.getMessage());
            return null;
        }
    }
}
