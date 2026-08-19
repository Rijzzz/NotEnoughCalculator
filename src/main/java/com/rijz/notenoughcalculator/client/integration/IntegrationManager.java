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

package com.rijz.notenoughcalculator.client.integration;

import com.rijz.notenoughcalculator.config.CalculatorConfig;
import me.shedaniel.rei.api.client.REIRuntime;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationManager.class);
    private static Boolean reiLoaded = null;
    private static SearchFieldAdapter activeAdapter = null;
    private static StandaloneSearchField standaloneField = null;

    private static Boolean itemListLoaded = null;

    public static boolean isREILoaded() {
        if (reiLoaded == null) {
            try {
                reiLoaded = FabricLoader.getInstance().isModLoaded("roughlyenoughitems");
            } catch (Throwable e) {
                reiLoaded = false;
            }
            LOGGER.info("IntegrationManager detected REI present: {}", reiLoaded);
        }
        return reiLoaded;
    }

    public static boolean isItemListLoaded() {
        if (itemListLoaded == null) {
            try {
                itemListLoaded = FabricLoader.getInstance().isModLoaded("skyblock-item-list");
            } catch (Throwable e) {
                itemListLoaded = false;
            }
            LOGGER.info("IntegrationManager detected Skyblock Item Viewer present: {}", itemListLoaded);
        }
        return itemListLoaded;
    }

    public static StandaloneSearchField getStandaloneField() {
        if (standaloneField == null) {
            standaloneField = new StandaloneSearchField();
        }
        return standaloneField;
    }

    public static boolean isREIOverlayVisible() {
        if (!isREILoaded()) return false;
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null) {
                return runtime.isOverlayVisible();
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean isStandaloneActive() {
        if (isREILoaded() && isREIOverlayVisible()) return false;
        if (isItemListLoaded() && CalculatorConfig.getInstance().enableItemListIntegration) return false;
        return true;
    }

    public static SearchFieldAdapter getActiveAdapter() {
        if (isREILoaded() && isREIOverlayVisible()) {
            if (activeAdapter == null || !(activeAdapter instanceof REISearchAdapter)) {
                activeAdapter = createREISearchAdapter();
            }
            return activeAdapter;
        }
        if (isItemListLoaded() && CalculatorConfig.getInstance().enableItemListIntegration) {
            if (activeAdapter == null || !(activeAdapter instanceof SkyblockItemListAdapter)) {
                activeAdapter = createSkyblockItemListAdapter();
            }
            return activeAdapter;
        }
        return getStandaloneField();
    }

    // Separated method to isolate REISearchAdapter instantiation and avoid class loading errors when REI is missing
    private static SearchFieldAdapter createREISearchAdapter() {
        try {
            return new REISearchAdapter();
        } catch (Throwable e) {
            LOGGER.warn("Failed to initialize Roughly Enough Items (REI) search adapter (falling back to Standalone search bar): {}", e.getMessage());
            return getStandaloneField();
        }
    }

    private static SearchFieldAdapter createSkyblockItemListAdapter() {
        try {
            return new SkyblockItemListAdapter();
        } catch (Throwable e) {
            LOGGER.warn("Failed to initialize Skyblock Item Viewer search adapter (falling back to Standalone search bar): {}", e.getMessage());
            return getStandaloneField();
        }
    }
}
