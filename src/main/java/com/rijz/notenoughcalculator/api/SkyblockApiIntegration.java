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

package com.rijz.notenoughcalculator.api;

import tech.thatgravyboat.skyblockapi.api.profile.currency.CurrencyAPI;

import java.util.function.Supplier;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SkyblockApiIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(SkyblockApiIntegration.class);
	private static Boolean skyblockApiAvailable = null;

	public static boolean isAvailable() {
		if (skyblockApiAvailable == null) {
			try {
				boolean modLoaded = FabricLoader.getInstance().isModLoaded("skyblock-api");
				if (modLoaded) {
					Class.forName(CurrencyAPI.class.getName());
					skyblockApiAvailable = true;
					LOGGER.info("SkyBlock API detected and successfully bound!");
				} else {
					skyblockApiAvailable = false;
				}
			} catch (Throwable t) {
				skyblockApiAvailable = false;
				LOGGER.debug("SkyBlock API is not available on classpath");
			}
		}
		return skyblockApiAvailable;
	}

	public static <T> T safeQuery(Supplier<T> query) {
		if (!isAvailable())
			return null;
		try {
			return query.get();
		} catch (Throwable ignored) {
			return null;
		}
	}
}
