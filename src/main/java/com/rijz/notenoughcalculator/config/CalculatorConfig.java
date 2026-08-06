/*
 * This file is part of Not Enough Calculator.
 *
 * Not Enough Calculator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Not Enough Calculator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.rijz.notenoughcalculator.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// User configuration class. Supports live reloading on file changes without game restarts.
public class CalculatorConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = getConfigPath();

    private static Path getConfigPath() {
        try {
            if (FabricLoader.getInstance() != null && FabricLoader.getInstance().getConfigDir() != null) {
                return FabricLoader.getInstance().getConfigDir().resolve("notenoughcalculator.json");
            }
        } catch (Throwable ignored) {}
        return Path.of("build", "tmp", "notenoughcalculator.json");
    }

    private static CalculatorConfig INSTANCE;
    private static long lastModified = 0;
    private static long lastChecked = 0;
    private static final long CHECK_INTERVAL_MS = 2000;

    // Config options
    public int decimalPrecision = 10;
    public boolean showUnitSuggestions = true;
    public boolean enableHistoryNavigation = true;
    public boolean showInlineResults = true;
    public boolean enableCommaFormatting = true;
    public int bazaarFlipperLevel = 0; // 0 = 1.25%, 1 = 1.125%, 2 = 1.0%
    public boolean enableShorthandResults = false;
    public boolean enableSyntaxHighlighting = true;
    public java.util.Map<String, String> customVariables = new java.util.LinkedHashMap<>();

    public static CalculatorConfig getInstance() {
        // Poll for external config file edits every 2 seconds
        long now = System.currentTimeMillis();
        if (INSTANCE != null && (now - lastChecked) >= CHECK_INTERVAL_MS) {
            lastChecked = now;
            if (Files.exists(CONFIG_PATH)) {
                try {
                    long currentModified = Files.getLastModifiedTime(CONFIG_PATH).toMillis();
                    if (currentModified > lastModified) {
                        LOGGER.info("Config file changed, reloading...");
                        INSTANCE = load();
                    }
                } catch (IOException ignored) {}
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
            LOGGER.warn("Failed to load config, falling back to defaults: {}", e.getMessage());
        }

        // File doesn't exist, create it with defaults
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
        return com.rijz.notenoughcalculator.client.util.SyntaxHighlighter.COLOR_NUMBER;
    }

    public String getChatResultColorCode() {
        return com.rijz.notenoughcalculator.client.util.SyntaxHighlighter.COLOR_CHAT_RESULT;
    }

    public String getErrorColorCode() {
        return com.rijz.notenoughcalculator.client.util.SyntaxHighlighter.COLOR_ERROR;
    }

    public String getOperatorColorCode() {
        return com.rijz.notenoughcalculator.client.util.SyntaxHighlighter.COLOR_OP;
    }

    public double getBazaarTaxRate() {
        int level = Math.max(0, Math.min(2, bazaarFlipperLevel));
        return 1.25 - (level * 0.125);
    }
}