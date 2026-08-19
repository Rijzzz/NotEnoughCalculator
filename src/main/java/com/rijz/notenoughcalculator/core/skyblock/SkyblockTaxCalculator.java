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

package com.rijz.notenoughcalculator.core.skyblock;

import java.math.BigDecimal;

public class SkyblockTaxCalculator {

    /**
     * Calculate Bazaar net payout accounting for Bazaar Flipper perk level tax rate.
     */
    public static BigDecimal calculateBzPayout(BigDecimal price, double taxRatePct) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        // Base Bazaar tax rate (1.25% default, reduced by Bazaar Flipper perk)
        double multiplier = 1.0 - (taxRatePct / 100.0);
        return price.multiply(BigDecimal.valueOf(multiplier)).stripTrailingZeros();
    }

    /**
     * Calculate Auction House net payout accounting for listing fees, collection claim tax, and listing duration fees.
     */
    public static BigDecimal calculateAhPayout(BigDecimal price, double durationHours, boolean isBin) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Creation / Listing Fee
        double listingFeePct;
        if (isBin) {
            // BIN listing fee brackets: 1% (<10m), 2% (10m-100m), 2.5% (>100m)
            if (price.compareTo(new BigDecimal("100000000")) > 0) {
                listingFeePct = 0.025;
            } else if (price.compareTo(new BigDecimal("10000000")) >= 0) {
                listingFeePct = 0.020;
            } else {
                listingFeePct = 0.010;
            }
        } else {
            // Standard auction listing fee: 5% of starting price
            listingFeePct = 0.050;
        }
        BigDecimal listingFee = price.multiply(BigDecimal.valueOf(listingFeePct));

        // Collection claim fee (1% above 1m, capped to avoid dropping under 1m)
        BigDecimal collectionTax = BigDecimal.ZERO;
        if (price.compareTo(new BigDecimal("1000000")) > 0) {
            BigDecimal rawTax = price.multiply(new BigDecimal("0.01"));
            BigDecimal afterTax = price.subtract(rawTax);
            if (afterTax.compareTo(new BigDecimal("1000000")) < 0) {
                collectionTax = price.subtract(new BigDecimal("1000000"));
            } else {
                collectionTax = rawTax;
            }
        }

        // AH listing duration fee (6h is standard default = 0 coins)
        BigDecimal durationFee = BigDecimal.valueOf(calculateAhDurationFee(durationHours));

        BigDecimal net = price.subtract(listingFee).subtract(collectionTax).subtract(durationFee);
        return net.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : net.stripTrailingZeros();
    }

    /**
     * Calculate AH listing duration fee in coins based on listing duration in hours.
     */
    public static double calculateAhDurationFee(double hours) {
        if (hours <= 0) return 0;
        // 5 Minutes to 59 Minutes: +50 coins
        if (hours < 1.0) {
            return 50;
        }
        // Hours 1 to 7: Starts at 20 coins (+5 coins/hour)
        if (hours <= 7) {
            return 20 + Math.round((hours - 1) * 5);
        }
        // Hours 7 to 12: Adds +10 coins/hour (12h = 100)
        if (hours <= 12) {
            return 50 + Math.round((hours - 7) * 10);
        }
        // Hours 12 to 24: Adds +20 coins/hour (24h = 350)
        if (hours < 24) {
            return 100 + Math.round((hours - 12) * 20);
        }
        if (hours == 24) {
            return 350;
        }
        // Daily scaling up to Day 5 (120h)
        if (hours <= 48) {
            return 350 + Math.round((hours - 24) * ((1200.0 - 350.0) / 24.0));
        }
        if (hours <= 72) {
            return 1200 + Math.round((hours - 48) * ((3000.0 - 1200.0) / 24.0));
        }
        if (hours <= 96) {
            return 3000 + Math.round((hours - 72) * ((7200.0 - 3000.0) / 24.0));
        }
        if (hours <= 120) {
            return 7200 + Math.round((hours - 96) * ((12000.0 - 7200.0) / 24.0));
        }
        // Day 5 (120h) through Day 14 (336h): linear +4,800 coins/day (+200 coins/hour)
        double extraHours = hours - 120;
        double fee = 12000 + (extraHours * 200);
        return Math.min(55200, Math.round(fee));
    }
}
