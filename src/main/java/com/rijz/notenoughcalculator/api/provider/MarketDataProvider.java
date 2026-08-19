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

public class MarketDataProvider {

    private static String cleanId(String id) {
        if (id == null) return null;
        String s = id.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            if (s.length() >= 2) {
                s = s.substring(1, s.length() - 1).trim();
            }
        }
        return s.toUpperCase();
    }

    public static BigDecimal getBazaarBuyPrice(String id) {
        if (!SkyblockApiIntegration.isAvailable() || id == null || id.isEmpty()) return null;
        String formattedId = cleanId(id);
        try {
            BazaarAPI.BazaarProduct product = BazaarAPI.INSTANCE.getProduct(formattedId);
            if (product != null && product.getBuyPrice() > 0) {
                return BigDecimal.valueOf(product.getBuyPrice());
            }
        } catch (Throwable ignored) {}
        try {
            BazaarAPI.BazaarProduct product = BazaarAPI.INSTANCE.getProduct(id);
            if (product != null && product.getBuyPrice() > 0) {
                return BigDecimal.valueOf(product.getBuyPrice());
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static BigDecimal getBazaarSellPrice(String id) {
        if (!SkyblockApiIntegration.isAvailable() || id == null || id.isEmpty()) return null;
        String formattedId = cleanId(id);
        try {
            BazaarAPI.BazaarProduct product = BazaarAPI.INSTANCE.getProduct(formattedId);
            if (product != null && product.getSellPrice() > 0) {
                return BigDecimal.valueOf(product.getSellPrice());
            }
        } catch (Throwable ignored) {}
        try {
            BazaarAPI.BazaarProduct product = BazaarAPI.INSTANCE.getProduct(id);
            if (product != null && product.getSellPrice() > 0) {
                return BigDecimal.valueOf(product.getSellPrice());
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static BigDecimal getBazaarMargin(String id) {
        if (!SkyblockApiIntegration.isAvailable() || id == null || id.isEmpty()) return null;
        String formattedId = cleanId(id);
        try {
            BazaarAPI.BazaarProduct product = BazaarAPI.INSTANCE.getProduct(formattedId);
            if (product != null && product.getBuyPrice() > 0 && product.getSellPrice() > 0) {
                return BigDecimal.valueOf(product.getBuyPrice() - product.getSellPrice());
            }
        } catch (Throwable ignored) {}
        try {
            BazaarAPI.BazaarProduct product = BazaarAPI.INSTANCE.getProduct(id);
            if (product != null && product.getBuyPrice() > 0 && product.getSellPrice() > 0) {
                return BigDecimal.valueOf(product.getBuyPrice() - product.getSellPrice());
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static BigDecimal getLowestBinPrice(String id) {
        if (!SkyblockApiIntegration.isAvailable() || id == null || id.isEmpty()) return null;
        String formattedId = cleanId(id);
        try {
            Long price = LowestBinAPI.INSTANCE.getLowestPrice(formattedId);
            if (price != null && price > 0) {
                return BigDecimal.valueOf(price);
            }
        } catch (Throwable ignored) {}
        try {
            Long price = LowestBinAPI.INSTANCE.getLowestPrice(id);
            if (price != null && price > 0) {
                return BigDecimal.valueOf(price);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static BigDecimal getLowestBinAvgPrice(String id) {
        if (!SkyblockApiIntegration.isAvailable() || id == null || id.isEmpty()) return null;
        String formattedId = cleanId(id);
        try {
            LowestBinAPI.AuctionItem item = LowestBinAPI.INSTANCE.getPrice(formattedId);
            if (item != null && item.getMean() > 0) {
                return BigDecimal.valueOf(item.getMean());
            }
        } catch (Throwable ignored) {}
        try {
            LowestBinAPI.AuctionItem item = LowestBinAPI.INSTANCE.getPrice(id);
            if (item != null && item.getMean() > 0) {
                return BigDecimal.valueOf(item.getMean());
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static BigDecimal getNpcSellPrice(String id) {
        if (!SkyblockApiIntegration.isAvailable() || id == null || id.isEmpty()) return null;
        String formattedId = cleanId(id);
        try {
            Float price = ItemData.INSTANCE.getNpcSellPrice(formattedId);
            if (price != null && price > 0) {
                return BigDecimal.valueOf(price);
            }
        } catch (Throwable ignored) {}
        try {
            Float price = ItemData.INSTANCE.getNpcSellPrice(id);
            if (price != null && price > 0) {
                return BigDecimal.valueOf(price);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static BigDecimal getMotesSellPrice(String id) {
        if (!SkyblockApiIntegration.isAvailable() || id == null || id.isEmpty()) return null;
        String formattedId = cleanId(id);
        try {
            Float price = ItemData.INSTANCE.getMotesSellPrice(formattedId);
            if (price != null && price > 0) {
                return BigDecimal.valueOf(price);
            }
        } catch (Throwable ignored) {}
        try {
            Float price = ItemData.INSTANCE.getMotesSellPrice(id);
            if (price != null && price > 0) {
                return BigDecimal.valueOf(price);
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static BigDecimal getUnifiedPrice(String id) {
        if (!SkyblockApiIntegration.isAvailable() || id == null || id.isEmpty()) return null;
        String formattedId = cleanId(id);
        try {
            long price = Pricing.INSTANCE.getPrice(formattedId);
            if (price > 0) {
                return BigDecimal.valueOf(price);
            }
        } catch (Throwable ignored) {}
        try {
            long price = Pricing.INSTANCE.getPrice(id);
            if (price > 0) {
                return BigDecimal.valueOf(price);
            }
        } catch (Throwable ignored) {}
        return null;
    }
}
