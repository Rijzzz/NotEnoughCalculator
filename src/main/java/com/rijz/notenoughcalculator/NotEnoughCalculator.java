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

package com.rijz.notenoughcalculator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotEnoughCalculator implements ModInitializer {

	public static final String MOD_ID = "notenoughcalculator";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static String getVersion() {
		return FabricLoader.getInstance().getModContainer(MOD_ID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("DEVELOPMENT");
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Not Enough Calculator v{} initialized", getVersion());
	}
}
