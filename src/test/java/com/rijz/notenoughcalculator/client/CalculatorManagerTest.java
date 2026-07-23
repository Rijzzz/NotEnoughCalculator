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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Tests the static looksLikeCalculation() heuristic used to distinguish
// math expressions from item searches in the REI search bar.
class CalculatorManagerTest {

    @Nested
    @DisplayName("looksLikeCalculation - Should trigger calculator")
    class ShouldTrigger {
        @Test void addition() { assertTrue(CalculatorManager.looksLikeCalculation("10+5")); }
        @Test void subtraction() { assertTrue(CalculatorManager.looksLikeCalculation("10-5")); }
        @Test void multiplication() { assertTrue(CalculatorManager.looksLikeCalculation("10*5")); }
        @Test void division() { assertTrue(CalculatorManager.looksLikeCalculation("10/5")); }
        @Test void exponent() { assertTrue(CalculatorManager.looksLikeCalculation("2^10")); }
        @Test void modulo() { assertTrue(CalculatorManager.looksLikeCalculation("10 % 3")); }
        @Test void factorial() { assertTrue(CalculatorManager.looksLikeCalculation("5!")); }
        @Test void xMultiply() { assertTrue(CalculatorManager.looksLikeCalculation("10x5")); }
        @Test void parentheses() { assertTrue(CalculatorManager.looksLikeCalculation("(5+3)")); }
        @Test void sqrtFunction() { assertTrue(CalculatorManager.looksLikeCalculation("sqrt(4)")); }
        @Test void absFunction() { assertTrue(CalculatorManager.looksLikeCalculation("abs(-5)")); }
        @Test void logFunction() { assertTrue(CalculatorManager.looksLikeCalculation("log(100)")); }
        @Test void lnFunction() { assertTrue(CalculatorManager.looksLikeCalculation("ln(10)")); }
        @Test void sinFunction() { assertTrue(CalculatorManager.looksLikeCalculation("sin(90)")); }
        @Test void cosFunction() { assertTrue(CalculatorManager.looksLikeCalculation("cos(0)")); }
        @Test void tanFunction() { assertTrue(CalculatorManager.looksLikeCalculation("tan(45)")); }
        @Test void minFunction() { assertTrue(CalculatorManager.looksLikeCalculation("min(3, 5)")); }
        @Test void maxFunction() { assertTrue(CalculatorManager.looksLikeCalculation("max(3, 5)")); }
        @Test void floorFunction() { assertTrue(CalculatorManager.looksLikeCalculation("floor(3.5)")); }
        @Test void ceilFunction() { assertTrue(CalculatorManager.looksLikeCalculation("ceil(3.5)")); }
        @Test void roundFunction() { assertTrue(CalculatorManager.looksLikeCalculation("round(3.5)")); }
        @Test void ansVariable() { assertTrue(CalculatorManager.looksLikeCalculation("ans")); }
        @Test void customVariable() { assertTrue(CalculatorManager.looksLikeCalculation("$profit")); }
        @Test void unitK() { assertTrue(CalculatorManager.looksLikeCalculation("10k + 5k")); }
        @Test void unitM() { assertTrue(CalculatorManager.looksLikeCalculation("5m + 1m")); }
        @Test void unitB() { assertTrue(CalculatorManager.looksLikeCalculation("2b + 500m")); }
        @Test void storageUnit() { assertTrue(CalculatorManager.looksLikeCalculation("2dc + 1sc")); }
        @Test void complexExpression() { assertTrue(CalculatorManager.looksLikeCalculation("100m - 50m * 1.1")); }
        @Test void mixedUnitsAndOps() { assertTrue(CalculatorManager.looksLikeCalculation("10kx5k")); }
    }

    @Nested
    @DisplayName("looksLikeCalculation - Should NOT trigger calculator")
    class ShouldNotTrigger {
        @Test void emptyString() { assertFalse(CalculatorManager.looksLikeCalculation("")); }
        @Test void nullInput() { assertFalse(CalculatorManager.looksLikeCalculation(null)); }
        @Test void whitespace() { assertFalse(CalculatorManager.looksLikeCalculation("   ")); }
        @Test void singleNumber() { assertFalse(CalculatorManager.looksLikeCalculation("64")); }
        @Test void singleDecimal() { assertFalse(CalculatorManager.looksLikeCalculation("3.14")); }
        @Test void diamondSword() { assertFalse(CalculatorManager.looksLikeCalculation("diamond sword")); }
        @Test void ironIngot() { assertFalse(CalculatorManager.looksLikeCalculation("iron ingot")); }
        @Test void netheriteHelmet() { assertFalse(CalculatorManager.looksLikeCalculation("netherite helmet")); }
        @Test void enchantedBook() { assertFalse(CalculatorManager.looksLikeCalculation("enchanted book")); }
        @Test void bow() { assertFalse(CalculatorManager.looksLikeCalculation("bow")); }
        @Test void coal() { assertFalse(CalculatorManager.looksLikeCalculation("coal")); }
        @Test void redstone() { assertFalse(CalculatorManager.looksLikeCalculation("redstone")); }
    }
}
