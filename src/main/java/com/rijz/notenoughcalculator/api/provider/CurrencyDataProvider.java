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
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getPurse()));
	}

	public static BigDecimal getBank() {
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getBank()));
	}

	public static BigDecimal getPersonalBank() {
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getPersonalBank()));
	}

	public static BigDecimal getCoopBank() {
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getCoopBank()));
	}

	public static BigDecimal getMotes() {
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getMotes()));
	}

	public static BigDecimal getBits() {
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getBits()));
	}

	public static BigDecimal getCopper() {
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getCopper()));
	}

	public static BigDecimal getSowdust() {
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getSowdust()));
	}

	public static BigDecimal getKernels() {
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getKernels()));
	}

	public static BigDecimal getNorthStars() {
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getNorthStars()));
	}

	public static BigDecimal getGems() {
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getGems()));
	}

	public static BigDecimal getSoulflow() {
		return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(CurrencyAPI.INSTANCE.getSoulflow()));
	}
}
