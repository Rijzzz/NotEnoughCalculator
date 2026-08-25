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

    private static final BigDecimal BIN_THRESHOLD_HIGH = new BigDecimal("100000000");
    private static final BigDecimal BIN_THRESHOLD_MID = new BigDecimal("10000000");
    private static final double BIN_FEE_HIGH = 0.025;
    private static final double BIN_FEE_MID = 0.020;
    private static final double BIN_FEE_LOW = 0.010;
    private static final double AUCTION_FEE = 0.050;

    private static final BigDecimal CLAIM_TAX_THRESHOLD = new BigDecimal("1000000");
    private static final BigDecimal CLAIM_TAX_RATE = new BigDecimal("0.01");

    public static BigDecimal calculateBzPayout(BigDecimal price, double taxRatePct) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        double multiplier = 1.0 - (taxRatePct / 100.0);
        return price.multiply(BigDecimal.valueOf(multiplier)).stripTrailingZeros();
    }

    public static BigDecimal calculateAhPayout(BigDecimal price, double durationHours, boolean isBin) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        double listingFeePct;
        if (isBin) {
            if (price.compareTo(BIN_THRESHOLD_HIGH) > 0) {
                listingFeePct = BIN_FEE_HIGH;
            } else if (price.compareTo(BIN_THRESHOLD_MID) >= 0) {
                listingFeePct = BIN_FEE_MID;
            } else {
                listingFeePct = BIN_FEE_LOW;
            }
        } else {
            listingFeePct = AUCTION_FEE;
        }
        BigDecimal listingFee = price.multiply(BigDecimal.valueOf(listingFeePct));

        BigDecimal collectionTax = BigDecimal.ZERO;
        if (price.compareTo(CLAIM_TAX_THRESHOLD) > 0) {
            BigDecimal rawTax = price.multiply(CLAIM_TAX_RATE);
            BigDecimal afterTax = price.subtract(rawTax);
            if (afterTax.compareTo(CLAIM_TAX_THRESHOLD) < 0) {
                collectionTax = price.subtract(CLAIM_TAX_THRESHOLD);
            } else {
                collectionTax = rawTax;
            }
        }

        BigDecimal durationFee = BigDecimal.valueOf(calculateAhDurationFee(durationHours));

        BigDecimal net = price.subtract(listingFee).subtract(collectionTax).subtract(durationFee);
        return net.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : net.stripTrailingZeros();
    }

    public static double calculateAhDurationFee(double hours) {
        if (hours <= 0)
            return 0;
        if (hours < 1.0) {
            return 50;
        }
        if (hours <= 7) {
            return 20 + Math.round((hours - 1) * 5);
        }
        if (hours <= 12) {
            return 50 + Math.round((hours - 7) * 10);
        }
        if (hours < 24) {
            return 100 + Math.round((hours - 12) * 20);
        }
        if (hours == 24) {
            return 350;
        }
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
        double extraHours = hours - 120;
        double fee = 12000 + (extraHours * 200);
        return Math.min(55200, Math.round(fee));
    }
}
