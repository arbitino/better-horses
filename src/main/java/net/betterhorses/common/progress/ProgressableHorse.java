package net.betterhorses.common.progress;

public interface ProgressableHorse {
    // Получение прогресса
    Progress getProgress();

    // Установка прогресса
    void setProgress(Progress progress);
}
