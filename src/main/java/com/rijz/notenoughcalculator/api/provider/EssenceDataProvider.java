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

package com.rijz.notenoughcalculator.api.provider;

import com.rijz.notenoughcalculator.api.SkyblockApiIntegration;
import com.rijz.notenoughcalculator.core.ResultFormatter;

import tech.thatgravyboat.skyblockapi.api.data.Essence;
import tech.thatgravyboat.skyblockapi.api.events.info.TabWidget;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EssenceDataProvider {

	private static final Pattern NUMBER_PATTERN = Pattern.compile("([\\d,]+)");
	private static final Map<String, BigDecimal> CACHE = new ConcurrentHashMap<>();

	public static BigDecimal getEssence(String name) {
		if (!SkyblockApiIntegration.isAvailable() || name == null || name.isEmpty()) {
			return null;
		}
		return SkyblockApiIntegration.safeQuery(() -> {
			String query = name.trim().toLowerCase(Locale.ROOT);
			if (query.startsWith("essence_")) {
				query = query.substring("essence_".length());
			} else if (query.endsWith("essence")) {
				query = query.substring(0, query.length() - "essence".length());
			}
			if (query.isEmpty())
				return null;

			String normalizedQuery = query.replace(" ", "").replace("_", "");
			List<String> lines = TabWidget.ESSENCE.getCurrentLines();
			if (lines != null && !lines.isEmpty()) {
				for (String line : lines) {
					if (line == null)
						continue;
					String clean = ResultFormatter.stripMinecraftFormatting(line).trim();
					String normalizedLine = clean.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "");
					Matcher m = NUMBER_PATTERN.matcher(clean);
					if (m.find()) {
						String numStr = m.group(1).replace(",", "");
						BigDecimal val = new BigDecimal(numStr);
						if (normalizedLine.contains(normalizedQuery)) {
							CACHE.put(normalizedQuery, val);
						}
						for (String type : new String[]{"wither", "undead", "dragon", "spider", "ice", "diamond",
								"gold", "crimson", "forest", "fossil", "gecko", "safari"}) {
							if (normalizedLine.contains(type)) {
								CACHE.put(type, val);
							}
						}
					}
				}
			}
			return CACHE.get(normalizedQuery);
		});
	}

	public static BigDecimal getWither() {
		return getEssence(Essence.WITHER.name());
	}

	public static BigDecimal getUndead() {
		return getEssence(Essence.UNDEAD.name());
	}

	public static BigDecimal getDragon() {
		return getEssence(Essence.DRAGON.name());
	}

	public static BigDecimal getSpider() {
		return getEssence(Essence.SPIDER.name());
	}

	public static BigDecimal getIce() {
		return getEssence(Essence.ICE.name());
	}

	public static BigDecimal getDiamond() {
		return getEssence(Essence.DIAMOND.name());
	}

	public static BigDecimal getGold() {
		return getEssence(Essence.GOLD.name());
	}

	public static BigDecimal getCrimson() {
		return getEssence(Essence.CRIMSON.name());
	}

	public static BigDecimal getForest() {
		return getEssence(Essence.FOREST.name());
	}

	public static BigDecimal getFossil() {
		return getEssence(Essence.FOSSIL.name());
	}

	public static BigDecimal getSunGecko() {
		return getEssence(Essence.SUN_GECKO.name());
	}

	public static BigDecimal getSafari() {
		return getEssence("safari");
	}
}
