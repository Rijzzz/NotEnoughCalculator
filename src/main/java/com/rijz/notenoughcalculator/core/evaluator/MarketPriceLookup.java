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

package com.rijz.notenoughcalculator.core.evaluator;

import com.rijz.notenoughcalculator.api.provider.MarketDataProvider;
import com.rijz.notenoughcalculator.api.provider.SackDataProvider;

import java.math.BigDecimal;

public class MarketPriceLookup {

	public static BigDecimal lookupMarketPrice(String func, String itemId) {
		switch (func) {
			case "bzb" :
			case "bzbuy" :
				return MarketDataProvider.getBazaarBuyPrice(itemId);
			case "bzs" :
			case "bzsell" :
				return MarketDataProvider.getBazaarSellPrice(itemId);
			case "bzm" :
			case "bzmargin" :
				return MarketDataProvider.getBazaarMargin(itemId);
			case "lb" :
			case "lowestbin" :
				return MarketDataProvider.getLowestBinPrice(itemId);
			case "lba" :
			case "lowestbinavg" :
				return MarketDataProvider.getLowestBinAvgPrice(itemId);
			case "npc" :
			case "npcsell" :
				return MarketDataProvider.getNpcSellPrice(itemId);
			case "motes" :
			case "motessell" :
				return MarketDataProvider.getMotesSellPrice(itemId);
			case "price" :
				return MarketDataProvider.getUnifiedPrice(itemId);
			case "sack" :
			case "sackcount" :
				return SackDataProvider.getSackItemCount(itemId);
			default :
				return null;
		}
	}
}
