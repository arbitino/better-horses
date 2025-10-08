package net.betterhorses.common.breed;

import net.minecraft.nbt.NbtCompound;

public record Breed(
        String id,
        String displayName,
        float maxJumpHeight,
        float maxSpeed,
        ObedienceLevel obedience
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

    public int getObedienceLevel() {
        return obedience.getLevel();
    }

    // Сериализация в NBT
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", id);
        nbt.putString("displayName", displayName);
        nbt.putFloat("maxJumpHeight", maxJumpHeight);
        nbt.putFloat("maxSpeed", maxSpeed);
        nbt.putInt("obedience", obedience.getLevel());
        return nbt;
    }

    // Десериализация из NBT
    public static Breed fromNbt(NbtCompound nbt) {
        String id = nbt.getString("id");
        String displayName = nbt.getString("displayName");
        float maxJumpHeight = nbt.getFloat("maxJumpHeight");
        float maxSpeed = nbt.getFloat("maxSpeed");
        int obedienceLevel = nbt.getInt("obedience");

        ObedienceLevel obedience = switch (obedienceLevel) {
            case 1 -> ObedienceLevel.VERY_OBEDIENT;
            case 3 -> ObedienceLevel.DISOBEDIENT;
            default -> ObedienceLevel.NORMAL;
        };

        return new Breed(id, displayName, maxJumpHeight, maxSpeed, obedience);
    }
}
