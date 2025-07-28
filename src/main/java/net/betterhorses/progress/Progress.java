package net.betterhorses.progress;

import net.minecraft.nbt.NbtCompound;

public class Progress {
    public static final String KEY = "HorseProgress";

    private long jumps;
    private long runningDistance;

    // Пустой прогресс для новой лошади
    public static Progress empty() {
        return new Progress(0, 0);
    }

    public Progress(long jumps, long runningDistance) {
        this.jumps = jumps;
        this.runningDistance = runningDistance;
    }

    public long getJumpCount() {
        return this.jumps;
    }

    public long getRunningDistance() {
        return this.runningDistance;
    }

    // Сериализация в NBT
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putLong("runningDistance", this.runningDistance);
        nbt.putLong("jumpCount", this.jumps);
        return nbt;
    }

    // Десериализация из NBT
    public static Progress fromNbt(NbtCompound nbt) {
        return new Progress(
            nbt.getLong("jumpCount"),
            nbt.getLong("runningDistance")
        );
    }

    public void addDistance(long distance) {
        this.runningDistance += distance;
    }

    public void addJump() {
        this.jumps++;
    }
}
