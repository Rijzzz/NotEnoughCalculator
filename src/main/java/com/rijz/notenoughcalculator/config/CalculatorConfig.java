package com.rijz.notenoughcalculator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * User settings for the calculator mod.
 * Config file auto-reloads when changed, no restart needed.
 *
 * Note: History size is hardcoded at 15 equations and not configurable.
 */
public class CalculatorConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("notenoughcalculator.json");

    private static CalculatorConfig INSTANCE;
    private static long lastModified = 0;

    // User-configurable settings
    public int decimalPrecision = 10;
    public boolean showUnitSuggestions = true;
    public boolean enableHistoryNavigation = true;
    public boolean showInlineResults = true;
    public boolean enableCommaFormatting = true;
    public boolean enableAutoComplete = false;
    public String language = "en_us";

    // Note: maxHistorySize is NOT here - it's hardcoded at 15 in CalculatorManager

    public static CalculatorConfig getInstance() {
        // Hot reload: check if config file changed and reload if needed
        if (INSTANCE != null && Files.exists(CONFIG_PATH)) {
            try {
                long currentModified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
                if (currentModified > lastModified) {
                    LOGGER.info("Config file changed, reloading...");
                    INSTANCE = load();
                }
            } catch (IOException e) {
                // Can't check mod time, just keep current config
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
                LOGGER.info("Loaded config from {}", CONFIG_PATH);
                return config;
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to load config, using defaults: {}", e.getMessage());
        }

        // No config file yet, create default
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
            LOGGER.info("Saved config to {}", CONFIG_PATH);
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