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