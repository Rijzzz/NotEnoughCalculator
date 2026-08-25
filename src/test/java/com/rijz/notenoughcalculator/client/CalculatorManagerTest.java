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

package com.rijz.notenoughcalculator.client;

import static org.junit.jupiter.api.Assertions.*;

import com.rijz.notenoughcalculator.config.CalculatorConfig;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator.EvalResult;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator.RadixMode;
import com.rijz.notenoughcalculator.core.ResultFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CalculatorManagerTest {

	@BeforeEach
	void resetConfigDefaults() {
		CalculatorConfig config = CalculatorConfig.getInstance();
		config.showInlineResults = true;
		config.showUnitSuggestions = true;
		config.enableCommaFormatting = true;
		config.enableHistoryNavigation = true;
		config.enableShorthandResults = false;
		config.decimalPrecision = 10;
		config.bazaarFlipperLevel = 0;
		config.save();
	}

	@Nested
	@DisplayName("looksLikeCalculation - Should trigger calculator")
	class ShouldTrigger {
		@Test
		void addition() {
			assertTrue(CalculatorManager.looksLikeCalculation("10+5"));
		}
		@Test
		void subtraction() {
			assertTrue(CalculatorManager.looksLikeCalculation("10-5"));
		}
		@Test
		void multiplication() {
			assertTrue(CalculatorManager.looksLikeCalculation("10*5"));
		}
		@Test
		void division() {
			assertTrue(CalculatorManager.looksLikeCalculation("10/5"));
		}
		@Test
		void exponent() {
			assertTrue(CalculatorManager.looksLikeCalculation("2^10"));
		}
		@Test
		void modulo() {
			assertTrue(CalculatorManager.looksLikeCalculation("10 % 3"));
		}
		@Test
		void factorial() {
			assertTrue(CalculatorManager.looksLikeCalculation("5!"));
		}
		@Test
		void xMultiply() {
			assertTrue(CalculatorManager.looksLikeCalculation("10x5"));
		}
		@Test
		void parentheses() {
			assertTrue(CalculatorManager.looksLikeCalculation("(5+3)"));
		}
		@Test
		void sqrtFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("sqrt(4)"));
		}
		@Test
		void absFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("abs(-5)"));
		}
		@Test
		void logFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("log(100)"));
		}
		@Test
		void lnFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("ln(10)"));
		}
		@Test
		void sinFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("sin(90)"));
		}
		@Test
		void cosFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("cos(0)"));
		}
		@Test
		void tanFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("tan(45)"));
		}
		@Test
		void minFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("min(3, 5)"));
		}
		@Test
		void maxFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("max(3, 5)"));
		}
		@Test
		void floorFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("floor(3.5)"));
		}
		@Test
		void ceilFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("ceil(3.5)"));
		}
		@Test
		void roundFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("round(3.5)"));
		}
		@Test
		void ansVariable() {
			assertTrue(CalculatorManager.looksLikeCalculation("ans"));
		}
		@Test
		void piConstant() {
			assertTrue(CalculatorManager.looksLikeCalculation("pi"));
		}
		@Test
		void eConstant() {
			assertTrue(CalculatorManager.looksLikeCalculation("e"));
		}
		@Test
		void customVariable() {
			assertTrue(CalculatorManager.looksLikeCalculation("$profit"));
		}
		@Test
		void unitK() {
			assertTrue(CalculatorManager.looksLikeCalculation("10k + 5k"));
		}
		@Test
		void unitM() {
			assertTrue(CalculatorManager.looksLikeCalculation("5m + 1m"));
		}
		@Test
		void unitB() {
			assertTrue(CalculatorManager.looksLikeCalculation("2b + 500m"));
		}
		@Test
		void storageUnit() {
			assertTrue(CalculatorManager.looksLikeCalculation("2dc + 1sc"));
		}
		@Test
		void complexExpression() {
			assertTrue(CalculatorManager.looksLikeCalculation("100m - 50m * 1.1"));
		}
		@Test
		void mixedUnitsAndOps() {
			assertTrue(CalculatorManager.looksLikeCalculation("10kx5k"));
		}
		@Test
		void binaryLiteral() {
			assertTrue(CalculatorManager.looksLikeCalculation("0b0_10"));
		}
		@Test
		void binaryLiteralUppercase() {
			assertTrue(CalculatorManager.looksLikeCalculation("0B1101"));
		}
		@Test
		void hexLiteral() {
			assertTrue(CalculatorManager.looksLikeCalculation("0xa1b"));
		}
		@Test
		void hexLiteralUppercase() {
			assertTrue(CalculatorManager.looksLikeCalculation("0XFF"));
		}
		@Test
		void octalLiteral() {
			assertTrue(CalculatorManager.looksLikeCalculation("0o511"));
		}
		@Test
		void octalLiteralUppercase() {
			assertTrue(CalculatorManager.looksLikeCalculation("0O77"));
		}
		@Test
		void literalArithmetic() {
			assertTrue(CalculatorManager.looksLikeCalculation("0b10 + 0xA"));
		}
		@Test
		void literalParentheses() {
			assertTrue(CalculatorManager.looksLikeCalculation("(0xFA)"));
		}
		@Test
		void literalFactorial() {
			assertTrue(CalculatorManager.looksLikeCalculation("0b100!"));
		}
		@Test
		void bitwiseAnd() {
			assertTrue(CalculatorManager.looksLikeCalculation("0b1010 & 0b1100"));
		}
		@Test
		void bitwiseOr() {
			assertTrue(CalculatorManager.looksLikeCalculation("0b1010 | 0b0101"));
		}
		@Test
		void bitwiseNot() {
			assertTrue(CalculatorManager.looksLikeCalculation("~0"));
		}
		@Test
		void bitwiseShiftLeft() {
			assertTrue(CalculatorManager.looksLikeCalculation("1 << 4"));
		}
		@Test
		void bitwiseShiftRight() {
			assertTrue(CalculatorManager.looksLikeCalculation("16 >> 2"));
		}
		@Test
		void hexFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("hex(255)"));
		}
		@Test
		void binFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("bin(10)"));
		}
		@Test
		void octFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("oct(63)"));
		}
		@Test
		void avgFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("avg(10, 20, 30)"));
		}
		@Test
		void pctFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("pct(50, 200)"));
		}
		@Test
		void gcdFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("gcd(12, 18)"));
		}
		@Test
		void lcmFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("lcm(12, 18)"));
		}
		@Test
		void clampFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("clamp(5, 0, 10)"));
		}
		@Test
		void xorFunction() {
			assertTrue(CalculatorManager.looksLikeCalculation("xor(10, 12)"));
		}
	}

	@Nested
	@DisplayName("looksLikeCalculation - Should NOT trigger calculator")
	class ShouldNotTrigger {
		@Test
		void emptyString() {
			assertFalse(CalculatorManager.looksLikeCalculation(""));
		}
		@Test
		void nullInput() {
			assertFalse(CalculatorManager.looksLikeCalculation(null));
		}
		@Test
		void whitespace() {
			assertFalse(CalculatorManager.looksLikeCalculation("   "));
		}
		@Test
		void singleNumber() {
			assertFalse(CalculatorManager.looksLikeCalculation("64"));
		}
		@Test
		void singleDecimal() {
			assertFalse(CalculatorManager.looksLikeCalculation("3.14"));
		}
		@Test
		void diamondSword() {
			assertFalse(CalculatorManager.looksLikeCalculation("diamond sword"));
		}
		@Test
		void ironIngot() {
			assertFalse(CalculatorManager.looksLikeCalculation("iron ingot"));
		}
		@Test
		void netheriteHelmet() {
			assertFalse(CalculatorManager.looksLikeCalculation("netherite helmet"));
		}
		@Test
		void enchantedBook() {
			assertFalse(CalculatorManager.looksLikeCalculation("enchanted book"));
		}
		@Test
		void bow() {
			assertFalse(CalculatorManager.looksLikeCalculation("bow"));
		}
		@Test
		void coal() {
			assertFalse(CalculatorManager.looksLikeCalculation("coal"));
		}
		@Test
		void redstone() {
			assertFalse(CalculatorManager.looksLikeCalculation("redstone"));
		}
		@Test
		void lapisLazuli() {
			assertFalse(CalculatorManager.looksLikeCalculation("lapis lazuli"));
		}
		@Test
		void goldenApple() {
			assertFalse(CalculatorManager.looksLikeCalculation("golden apple"));
		}
		@Test
		void cobblestone() {
			assertFalse(CalculatorManager.looksLikeCalculation("cobblestone"));
		}
	}

	@Nested
	@DisplayName("Config Options Compliance Test")
	class ConfigOptionsCompliance {
		private CalculatorManager manager;

		@BeforeEach
		void setUp() {
			manager = new CalculatorManager();
		}

		@Test
		void respectsBazaarFlipperLevelPerk() throws Exception {
			CalculatorConfig config = CalculatorConfig.getInstance();

			config.bazaarFlipperLevel = 0; // 1.25%
			config.save();
			assertEquals(0, new BigDecimal("98750000").compareTo(manager.calculate("bz(100m)")));

			config.bazaarFlipperLevel = 1; // 1.125%
			config.save();
			assertEquals(0, new BigDecimal("98875000").compareTo(manager.calculate("bz(100m)")));

			config.bazaarFlipperLevel = 2; // 1.0%
			config.save();
			assertEquals(0, new BigDecimal("99000000").compareTo(manager.calculate("bz(100m)")));
		}

		@Test
		void respectsEnableShorthandResults() throws Exception {
			CalculatorConfig config = CalculatorConfig.getInstance();

			config.enableShorthandResults = false;
			config.save();
			EvalResult eval1 = manager.calculateResult("1000000 + 500000");
			assertEquals("1,500,000", ResultFormatter.formatResult(eval1));

			config.enableShorthandResults = true;
			config.save();
			EvalResult eval2 = manager.calculateResult("1000000 + 500000");
			assertEquals("1.5m", ResultFormatter.formatResult(eval2));
		}

		@Test
		void respectsEnableCommaFormatting() {
			CalculatorConfig config = CalculatorConfig.getInstance();

			config.enableCommaFormatting = true;
			config.enableShorthandResults = false;
			config.save();
			assertEquals("1,000,000", ResultFormatter.formatWithCommas(new BigDecimal("1000000")));

			config.enableCommaFormatting = false;
			config.save();
			assertEquals("1000000", ResultFormatter.formatWithCommas(new BigDecimal("1000000")));
		}

		@Test
		void respectsDecimalPrecision() {
			CalculatorConfig config = CalculatorConfig.getInstance();

			config.decimalPrecision = 2;
			config.save();
			BigDecimal divVal = new BigDecimal("10").divide(new BigDecimal("3"), 50, RoundingMode.HALF_UP);
			assertEquals("3.33", ResultFormatter.formatWithCommas(divVal));

			config.decimalPrecision = 5;
			config.save();
			assertEquals("3.33333", ResultFormatter.formatWithCommas(divVal));
		}

		@Test
		void respectsShowUnitSuggestions() {
			CalculatorConfig config = CalculatorConfig.getInstance();

			config.showUnitSuggestions = true;
			config.save();
			String withUnits = ResultFormatter.formatResultWithUnits(new EvalResult(new BigDecimal("1728")));
			assertTrue(withUnits.contains("("));

			config.showUnitSuggestions = false;
			config.save();
			String withoutUnits = ResultFormatter.formatResultWithUnits(new EvalResult(new BigDecimal("1728")));
			assertFalse(withoutUnits.contains("("));
		}
	}

	@Nested
	@DisplayName("CalculatorManager Instance Operations")
	class InstanceOperations {
		private CalculatorManager manager;

		@BeforeEach
		void setUp() {
			manager = new CalculatorManager();
		}

		@Test
		void calculateDirect() throws Exception {
			BigDecimal res = manager.calculate("10 + 20");
			assertEquals(0, new BigDecimal("30").compareTo(res));
		}

		@Test
		void calculateResultWithRadix() throws Exception {
			EvalResult res = manager.calculateResult("hex(255)");
			assertEquals(0, new BigDecimal("255").compareTo(res.value));
			assertEquals(RadixMode.HEX, res.radixMode);
		}

		@Test
		void setVariableAndUse() throws Exception {
			manager.setVariableDirect("tax", new BigDecimal("0.10"));
			BigDecimal res = manager.calculate("100 * $tax");
			assertEquals(0, new BigDecimal("10").compareTo(res));
		}

		@Test
		void setVariableStringAndReload() throws Exception {
			BigDecimal val = manager.setVariable("discount", "20m - 5m");
			assertEquals(0, new BigDecimal("15000000").compareTo(val));
			assertEquals(0, new BigDecimal("15000000").compareTo(manager.calculate("$discount")));

			manager.reloadCustomVariables();
			assertEquals(0, new BigDecimal("15000000").compareTo(manager.calculate("$discount")));
		}

		@Test
		void historyTracking() throws Exception {
			manager.calculate("5 + 5");
			manager.calculate("10 * 10");
			List<String> hist = manager.getHistory();
			assertFalse(hist.isEmpty());
			assertTrue(hist.stream().anyMatch(e -> e.contains("5 + 5 = 10")));
		}

		@Test
		void clearHistoryWipesData() throws Exception {
			manager.calculate("5 + 5");
			manager.clearHistory();
			assertTrue(manager.getHistory().isEmpty());
			assertNull(manager.getLastFormattedResult());
		}

		@Test
		void resetSessionWipesData() throws Exception {
			manager.calculate("100 + 100");
			manager.reset();
			assertTrue(manager.getHistory().isEmpty());
			assertNull(manager.getLastFormattedResult());
		}

		@Test
		void formatSearchBarLiveEvaluation() {
			String clean = manager.formatSearchBar("10 + 20");
			assertEquals("10 + 20", clean);
			assertTrue(manager.hasResult());
			assertEquals("30", manager.getLastFormattedResult());
		}

		@Test
		void taxAndAngleHeuristics() {
			assertTrue(CalculatorManager.looksLikeCalculation("bz(100m)"));
			assertTrue(CalculatorManager.looksLikeCalculation("ah(50m)"));
			assertTrue(CalculatorManager.looksLikeCalculation("ahbin(50m, 24)"));
			assertTrue(CalculatorManager.looksLikeCalculation("rad(180)"));
			assertTrue(CalculatorManager.looksLikeCalculation("deg(pi)"));
			assertTrue(CalculatorManager.looksLikeCalculation("fmt(1500000)"));
			assertTrue(CalculatorManager.looksLikeCalculation("bz(10m) + ahbin(5m)"));
		}

		@Test
		void formatSearchBarLiveEvaluationSkyBlockFeatures() {
			assertEquals("bz(100m)", manager.formatSearchBar("bz(100m)"));
			assertTrue(manager.hasResult());
			assertEquals("98,750,000", manager.getLastFormattedResult());

			assertEquals("fmt(1500000)", manager.formatSearchBar("fmt(1500000)"));
			assertTrue(manager.hasResult());
			assertEquals("1.5m", manager.getLastFormattedResult());
		}
	}
}
