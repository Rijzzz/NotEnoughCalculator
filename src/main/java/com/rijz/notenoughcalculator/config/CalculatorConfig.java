package com.rijz.notenoughcalculator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Manages all user settings with automatic reload when config file changes
// Settings persist across game sessions
public class CalculatorConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("notenoughcalculator.json");

    private static CalculatorConfig INSTANCE;
    private static long lastModified = 0;

    // User-configurable settings
    public int maxHistorySize = 100;
    public int decimalPrecision = 10;
    public boolean showUnitSuggestions = true;
    public boolean enableHistoryNavigation = true;
    public boolean showInlineResults = true;
    public boolean enableCommaFormatting = true;
    public boolean enableAutoComplete = false;
    public String language = "en_us";

    public static CalculatorConfig getInstance() {
        // Hot reload: check if config file was edited and reload if needed
        if (INSTANCE != null && Files.exists(CONFIG_PATH)) {
            try {
                long currentModified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
                if (currentModified > lastModified) {
                    LOGGER.info("Config file changed, reloading...");
                    INSTANCE = load();
                }
            } catch (IOException e) {
                // Can't check modification time, just keep using current config
            }
        }

        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static CalculatorConfig load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);
                CalculatorConfig config = GSON.fromJson(json, CalculatorConfig.class);
                lastModified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
                LOGGER.info("Loaded configuration from {}", CONFIG_PATH);
                return config;
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load config, using defaults: {}", e.getMessage());
        }

        // No config file exists yet, create one with defaults
        CalculatorConfig config = new CalculatorConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
            lastModified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
            LOGGER.info("Saved configuration to {}", CONFIG_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to save config: {}", e.getMessage());
        }
    }

    public String getResultColorCode() {
        return "§f"; // White
    }

    public String getErrorColorCode() {
        return "§c"; // Red
    }

    public String getOperatorColorCode() {
        return "§f"; // White
    }
}