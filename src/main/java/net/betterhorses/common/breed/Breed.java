package net.betterhorses.common.breed;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

public record Breed(
        String id,
        String translationKey,
        double maxJumpHeight,
        double maxSpeed,
        ObedienceLevel obedience,
        float speedGrowthMultiplier,
        float jumpGrowthMultiplier
) {
    public static final String KEY = "HorseBreed";

    public enum ObedienceLevel {
        VERY_OBEDIENT(1),  // Очень послушная
        NORMAL(2),         // Обычная
        DISOBEDIENT(3);    // Совсем не послушная

        private final int level;

        ObedienceLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    public ObedienceLevel getObedience() {
        return obedience;
    }

    public float speedGrowthMultiplier() {
        return speedGrowthMultiplier;
    }

    public float jumpGrowthMultiplier() {
        return jumpGrowthMultiplier;
    }

    public String getDisplayName() {
        return Text.translatable(translationKey).getString();
    }

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", id);
        nbt.putString("translationKey", translationKey);
        nbt.putDouble("maxJumpHeight", maxJumpHeight);
        nbt.putDouble("maxSpeed", maxSpeed);
        nbt.putInt("obedience", obedience.getLevel());
        nbt.putFloat("speedGrowthMultiplier", speedGrowthMultiplier);
        nbt.putFloat("jumpGrowthMultiplier", jumpGrowthMultiplier);
        return nbt;
    }

    public static Breed fromNbt(NbtCompound nbt) {
        String id = nbt.getString("id");
        String translationKey;

        if (nbt.contains("translationKey")) {
            translationKey = nbt.getString("translationKey");
        } else if (nbt.contains("displayName")) {
            // Fallback для старых сохранений - создаем translation key из старого displayName
            String oldDisplayName = nbt.getString("displayName");
            translationKey = "breed." + id.replace(":", ".").toLowerCase();
        } else {
            translationKey = "breed.betterhorses.unknown";
        }
        
        double maxJumpHeight = nbt.getDouble("maxJumpHeight");
        double maxSpeed = nbt.getDouble("maxSpeed");
        int obedienceLevel = nbt.getInt("obedience");
        float speedGrowthMultiplier = nbt.contains("speedGrowthMultiplier") ? nbt.getFloat("speedGrowthMultiplier") : 1.0f;
        float jumpGrowthMultiplier = nbt.contains("jumpGrowthMultiplier") ? nbt.getFloat("jumpGrowthMultiplier") : 1.0f;

        ObedienceLevel obedience = switch (obedienceLevel) {
            case 1 -> ObedienceLevel.VERY_OBEDIENT;
            case 3 -> ObedienceLevel.DISOBEDIENT;
            default -> ObedienceLevel.NORMAL;
        };

        return new Breed(id, translationKey, maxJumpHeight, maxSpeed, obedience, speedGrowthMultiplier, jumpGrowthMultiplier);
    }
}
