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

import tech.thatgravyboat.skyblockapi.api.profile.currency.CurrencyAPI;

import java.math.BigDecimal;

public class CurrencyDataProvider {

    public static BigDecimal getPurse() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            double value = CurrencyAPI.INSTANCE.getPurse();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getBank() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            long value = CurrencyAPI.INSTANCE.getBank();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getPersonalBank() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            long value = CurrencyAPI.INSTANCE.getPersonalBank();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getCoopBank() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            long value = CurrencyAPI.INSTANCE.getCoopBank();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getMotes() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            long value = CurrencyAPI.INSTANCE.getMotes();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getBits() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            long value = CurrencyAPI.INSTANCE.getBits();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getCopper() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            long value = CurrencyAPI.INSTANCE.getCopper();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getSowdust() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            long value = CurrencyAPI.INSTANCE.getSowdust();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getKernels() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            long value = CurrencyAPI.INSTANCE.getKernels();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getNorthStars() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            long value = CurrencyAPI.INSTANCE.getNorthStars();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getGems() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            long value = CurrencyAPI.INSTANCE.getGems();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getSoulflow() {
        if (!SkyblockApiIntegration.isAvailable()) return null;
        try {
            long value = CurrencyAPI.INSTANCE.getSoulflow();
            return BigDecimal.valueOf(value);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
