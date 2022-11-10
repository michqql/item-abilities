package me.michqql.itemabilities;

import me.michqql.core.io.CommentFile;
import org.bukkit.configuration.ConfigurationSection;

public enum Settings {
    ITEM_TRACKER_AUTO_SAVE_TIME("item-tracker.auto-save-time-seconds", 3600),
    ITEM_TRACKER_LIMIT("item-tracker.limited-updating", 0),
    ITEM_TRACKER_WARN_STAFF("item-tracker.warn-staff-about-dupes.enabled", true),
    ITEM_TRACKER_WARN_STAFF_PERMISSION("item-tracker.warn-staff-about-dupes.permission", ItemAbilityPlugin.ADMIN_PERMISSION),
    ;

    private final String configPath;
    private String stringValue = null;
    private Integer intValue = null;
    private Long longValue = null;
    private Double doubleValue = null;
    private Boolean booleanValue = null;

    Settings(String configPath, String stringValue) {
        this.configPath = configPath;
        this.stringValue = stringValue;
    }

    Settings(String configPath, int intValue) {
        this.configPath = configPath;
        this.intValue = intValue;
    }

    Settings(String configPath, long longValue) {
        this.configPath = configPath;
        this.longValue = longValue;
    }

    Settings(String configPath, boolean booleanValue) {
        this.configPath = configPath;
        this.booleanValue = booleanValue;
    }

    Settings(String configPath, double doubleValue) {
        this.configPath = configPath;
        this.doubleValue = doubleValue;
    }

    public void load(ConfigurationSection config) {
        if(stringValue != null) {
            stringValue = config.getString(configPath, stringValue);
        } else if(intValue != null) {
            intValue = config.getInt(configPath, intValue);
        } else if(longValue != null) {
            longValue = config.getLong(configPath, longValue);
        } else if(booleanValue != null) {
            booleanValue = config.getBoolean(configPath, booleanValue);
        } else if(doubleValue != null) {
            doubleValue = config.getDouble(configPath, doubleValue);
        }
    }

    public String getString() {
        return stringValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public static void loadSettings(ItemAbilityPlugin plugin) {
        CommentFile file = new CommentFile(plugin, "", "config");

        ConfigurationSection config = file.getConfig();
        for(Settings setting : values())
            setting.load(config);
    }
}
