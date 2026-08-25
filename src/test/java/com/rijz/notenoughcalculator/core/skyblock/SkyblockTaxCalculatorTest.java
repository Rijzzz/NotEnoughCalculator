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

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SkyblockTaxCalculatorTest {

	@Test
	@DisplayName("Calculate Bazaar payout with different tax perk levels")
	void testCalculateBzPayout() {
		assertEquals(0, new BigDecimal("98750000")
				.compareTo(SkyblockTaxCalculator.calculateBzPayout(new BigDecimal("100000000"), 1.25)));
		assertEquals(0, new BigDecimal("98875000")
				.compareTo(SkyblockTaxCalculator.calculateBzPayout(new BigDecimal("100000000"), 1.125)));
		assertEquals(0, new BigDecimal("99000000")
				.compareTo(SkyblockTaxCalculator.calculateBzPayout(new BigDecimal("100000000"), 1.0)));
		assertEquals(BigDecimal.ZERO, SkyblockTaxCalculator.calculateBzPayout(null, 1.25));
		assertEquals(BigDecimal.ZERO, SkyblockTaxCalculator.calculateBzPayout(new BigDecimal("-100"), 1.25));
	}

	@Test
	@DisplayName("Calculate Auction House payout for standard auctions and BIN listings")
	void testCalculateAhPayout() {
		assertEquals(0, new BigDecimal("46999955")
				.compareTo(SkyblockTaxCalculator.calculateAhPayout(new BigDecimal("50000000"), 6.0, false)));
		assertEquals(0, new BigDecimal("48499955")
				.compareTo(SkyblockTaxCalculator.calculateAhPayout(new BigDecimal("50000000"), 6.0, true)));
		assertEquals(0, new BigDecimal("4899955")
				.compareTo(SkyblockTaxCalculator.calculateAhPayout(new BigDecimal("5000000"), 6.0, true)));
		assertEquals(0, new BigDecimal("192999955")
				.compareTo(SkyblockTaxCalculator.calculateAhPayout(new BigDecimal("200000000"), 6.0, true)));
		assertEquals(BigDecimal.ZERO, SkyblockTaxCalculator.calculateAhPayout(null, 6.0, true));
		assertEquals(BigDecimal.ZERO, SkyblockTaxCalculator.calculateAhPayout(new BigDecimal("-100"), 6.0, true));
	}

	@Test
	@DisplayName("Calculate AH listing duration fees across milestones")
	void testCalculateAhDurationFee() {
		assertEquals(0, SkyblockTaxCalculator.calculateAhDurationFee(0));
		assertEquals(50, SkyblockTaxCalculator.calculateAhDurationFee(0.5));
		assertEquals(20, SkyblockTaxCalculator.calculateAhDurationFee(1.0));
		assertEquals(45, SkyblockTaxCalculator.calculateAhDurationFee(6.0));
		assertEquals(100, SkyblockTaxCalculator.calculateAhDurationFee(12.0));
		assertEquals(350, SkyblockTaxCalculator.calculateAhDurationFee(24.0));
		assertEquals(1200, SkyblockTaxCalculator.calculateAhDurationFee(48.0));
		assertEquals(3000, SkyblockTaxCalculator.calculateAhDurationFee(72.0));
		assertEquals(7200, SkyblockTaxCalculator.calculateAhDurationFee(96.0));
		assertEquals(12000, SkyblockTaxCalculator.calculateAhDurationFee(120.0));
		assertEquals(55200, SkyblockTaxCalculator.calculateAhDurationFee(336.0));
		assertEquals(55200, SkyblockTaxCalculator.calculateAhDurationFee(500.0));
	}
}
