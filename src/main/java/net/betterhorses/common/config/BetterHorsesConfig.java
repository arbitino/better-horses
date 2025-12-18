package net.betterhorses.common.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import net.betterhorses.common.ui.HorseStatsHud;

@Config(name = "betterhorses")
public class BetterHorsesConfig implements ConfigData {
    
    @ConfigEntry.Category("hud")
    @ConfigEntry.Gui.TransitiveObject
    public HudConfig hud = new HudConfig();
    
    public static class HudConfig {
        @ConfigEntry.Gui.Tooltip
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public HorseStatsHud.HudPosition hudPosition = HorseStatsHud.HudPosition.CENTER_ABOVE_HUD;
        
        @ConfigEntry.Gui.Tooltip
        public float fontScale = 0.5f;
        
        @ConfigEntry.Gui.Tooltip
        public boolean useHumanFriendlyUnits = true;
        
        @ConfigEntry.Gui.Tooltip
        public boolean showBreed = true;
        
        @ConfigEntry.Gui.Tooltip
        public boolean showAttributes = true;
        
        @ConfigEntry.Gui.Tooltip
        public boolean showLevels = true;
        
        @ConfigEntry.Gui.Tooltip
        public boolean showNextLevelInfo = true;
        
        @ConfigEntry.Gui.Tooltip
        public boolean showMaxAttributes = true;
        
        @ConfigEntry.Gui.Tooltip
        public boolean showBaseAttributes = true;
        
        @ConfigEntry.Gui.Tooltip
        public boolean showTotalProgress = true;
    }
}