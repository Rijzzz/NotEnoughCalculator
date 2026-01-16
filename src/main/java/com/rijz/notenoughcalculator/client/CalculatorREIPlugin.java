package com.rijz.notenoughcalculator.client;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// REI plugin registration
// We don't actually add any categories/displays, just need to register as a plugin
public class CalculatorREIPlugin implements REIClientPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatorREIPlugin.class);

    @Override
    public void registerCategories(CategoryRegistry registry) {
        LOGGER.debug("Not Enough Calculator - REI Categories registered");
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        LOGGER.debug("Not Enough Calculator - REI Displays registered");
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        LOGGER.debug("Not Enough Calculator - REI Entries registered");
    }
}