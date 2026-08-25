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

import tech.thatgravyboat.skyblockapi.api.remote.hypixel.itemdata.ItemData;
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.pricing.BazaarAPI;
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.pricing.LowestBinAPI;
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.pricing.Pricing;

import java.math.BigDecimal;
import java.util.function.Function;

public class MarketDataProvider {

	private static String cleanId(String id) {
		if (id == null)
			return null;
		String s = id.trim();
		if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
			if (s.length() >= 2) {
				s = s.substring(1, s.length() - 1).trim();
			}
		}
		return s.toUpperCase();
	}

	private static BigDecimal lookupWithFallback(String id, Function<String, BigDecimal> lookup) {
		if (!SkyblockApiIntegration.isAvailable() || id == null || id.isEmpty())
			return null;
		String formattedId = cleanId(id);
		try {
			BigDecimal result = lookup.apply(formattedId);
			if (result != null)
				return result;
		} catch (Throwable ignored) {
		}
		try {
			return lookup.apply(id);
		} catch (Throwable ignored) {
		}
		return null;
	}

	public static BigDecimal getBazaarBuyPrice(String id) {
		return lookupWithFallback(id, key -> {
			BazaarAPI.BazaarProduct p = BazaarAPI.INSTANCE.getProduct(key);
			return (p != null && p.getBuyPrice() > 0) ? BigDecimal.valueOf(p.getBuyPrice()) : null;
		});
	}

	public static BigDecimal getBazaarSellPrice(String id) {
		return lookupWithFallback(id, key -> {
			BazaarAPI.BazaarProduct p = BazaarAPI.INSTANCE.getProduct(key);
			return (p != null && p.getSellPrice() > 0) ? BigDecimal.valueOf(p.getSellPrice()) : null;
		});
	}

	public static BigDecimal getBazaarMargin(String id) {
		return lookupWithFallback(id, key -> {
			BazaarAPI.BazaarProduct p = BazaarAPI.INSTANCE.getProduct(key);
			return (p != null && p.getBuyPrice() > 0 && p.getSellPrice() > 0)
					? BigDecimal.valueOf(p.getBuyPrice() - p.getSellPrice())
					: null;
		});
	}

	public static BigDecimal getLowestBinPrice(String id) {
		return lookupWithFallback(id, key -> {
			Long price = LowestBinAPI.INSTANCE.getLowestPrice(key);
			return (price != null && price > 0) ? BigDecimal.valueOf(price) : null;
		});
	}

	public static BigDecimal getLowestBinAvgPrice(String id) {
		return lookupWithFallback(id, key -> {
			LowestBinAPI.AuctionItem item = LowestBinAPI.INSTANCE.getPrice(key);
			return (item != null && item.getMean() > 0) ? BigDecimal.valueOf(item.getMean()) : null;
		});
	}

	public static BigDecimal getNpcSellPrice(String id) {
		return lookupWithFallback(id, key -> {
			Float price = ItemData.INSTANCE.getNpcSellPrice(key);
			return (price != null && price > 0) ? BigDecimal.valueOf(price) : null;
		});
	}

	public static BigDecimal getMotesSellPrice(String id) {
		return lookupWithFallback(id, key -> {
			Float price = ItemData.INSTANCE.getMotesSellPrice(key);
			return (price != null && price > 0) ? BigDecimal.valueOf(price) : null;
		});
	}

	public static BigDecimal getUnifiedPrice(String id) {
		return lookupWithFallback(id, key -> {
			long price = Pricing.INSTANCE.getPrice(key);
			return (price > 0) ? BigDecimal.valueOf(price) : null;
		});
	}
}
