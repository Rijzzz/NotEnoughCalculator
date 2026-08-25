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

package com.rijz.notenoughcalculator.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalculatorConfigTest {

	private CalculatorConfig config;

	@BeforeEach
	void setUp() {
		config = new CalculatorConfig();
	}

	@Test
	void testDefaultValues() {
		assertTrue(config.showInlineResults);
		assertTrue(config.showUnitSuggestions);
		assertTrue(config.enableCommaFormatting);
		assertTrue(config.enableHistoryNavigation);
		assertFalse(config.enableShorthandResults);
		assertTrue(config.enableSyntaxHighlighting);
		assertTrue(config.enableFullEquationCopy);
		assertTrue(config.enableItemListIntegration);
		assertEquals(10, config.decimalPrecision);
		assertEquals(0, config.bazaarFlipperLevel);
		assertNotNull(config.customVariables);
	}

	@Test
	void testBazaarTaxRates() {
		config.bazaarFlipperLevel = 0;
		assertEquals(1.25, config.getBazaarTaxRate(), 0.001);

		config.bazaarFlipperLevel = 1;
		assertEquals(1.125, config.getBazaarTaxRate(), 0.001);

		config.bazaarFlipperLevel = 2;
		assertEquals(1.0, config.getBazaarTaxRate(), 0.001);

		config.bazaarFlipperLevel = 99;
		assertEquals(1.0, config.getBazaarTaxRate(), 0.001);

		config.bazaarFlipperLevel = -5;
		assertEquals(1.25, config.getBazaarTaxRate(), 0.001);
	}

	@Test
	void testResultColorCodes() {
		config.enableSyntaxHighlighting = true;
		assertEquals("§a", config.getResultColorCode());
		assertEquals("§a", config.getChatResultColorCode());

		config.enableSyntaxHighlighting = false;
		assertEquals("§f", config.getResultColorCode());
		assertEquals("§f", config.getChatResultColorCode());
	}

	@Test
	void testStandaloneModeAndPositioning() {
		assertFalse(config.forceStandaloneMode);
		assertEquals(-1, config.standaloneX);
		assertEquals(-1, config.standaloneY);
		assertFalse(config.isCustomPositionSet());

		config.setPosition(150, 250);
		assertTrue(config.isCustomPositionSet());
		assertEquals(150, config.standaloneX);
		assertEquals(250, config.standaloneY);

		config.resetPosition();
		assertFalse(config.isCustomPositionSet());
		assertEquals(-1, config.standaloneX);
		assertEquals(-1, config.standaloneY);

		config.forceStandaloneMode = true;
		assertTrue(config.forceStandaloneMode);
	}

	@Test
	void testCustomVariablesMap() {
		config.customVariables.put("profit", "100m");
		assertEquals("100m", config.customVariables.get("profit"));
		assertEquals(1, config.customVariables.size());
	}
}
