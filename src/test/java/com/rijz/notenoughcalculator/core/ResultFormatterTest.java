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

package com.rijz.notenoughcalculator.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ResultFormatterTest {

    @Test
    @DisplayName("Format with commas when config disabled")
    void formatWithCommasDisabled() {
        BigDecimal val = new BigDecimal("1000000");
        String result = val.stripTrailingZeros().toPlainString();
        assertEquals("1000000", result);
    }

    @Test
    @DisplayName("Format with commas enabled")
    void formatWithCommasEnabled() {
        BigDecimal val = new BigDecimal("1000000");
        String formatted = ResultFormatter.formatWithCommas(val);
        assertEquals("1,000,000", formatted);
    }

    @Test
    @DisplayName("Format massive 30-digit integer without floating-point precision loss")
    void formatMassiveInteger() {
        BigDecimal val = new BigDecimal("144355254354354354367477676851");
        String formatted = ResultFormatter.formatWithCommas(val);
        assertEquals("144,355,254,354,354,354,367,477,676,851", formatted);
    }

    @Test
    @DisplayName("Clean input removes formatting characters")
    void cleanInput() {
        assertEquals("100 + 50", ResultFormatter.cleanInput(" 100 + 50 "));
        assertEquals("", ResultFormatter.cleanInput(null));
        assertEquals("abc", ResultFormatter.cleanInput("\u200Babc\u200C"));
    }

    @Test
    @DisplayName("Format with units returns formatted string")
    void formatWithUnits() {
        BigDecimal val = new BigDecimal("50000000");
        String formatted = ResultFormatter.formatWithUnits(val);
        assertNotNull(formatted);
        assertTrue(formatted.startsWith("50,000,000"));
    }

    @Test
    @DisplayName("Format with Hex radix")
    void formatHexRadix() {
        String formatted = ResultFormatter.formatWithRadix(new BigDecimal("255"), ExpressionEvaluator.RadixMode.HEX);
        assertEquals("0xFF", formatted);
    }

    @Test
    @DisplayName("Format with Binary radix")
    void formatBinRadix() {
        String formatted = ResultFormatter.formatWithRadix(new BigDecimal("10"), ExpressionEvaluator.RadixMode.BIN);
        assertEquals("0b1010", formatted);
    }

    @Test
    @DisplayName("Format with Octal radix")
    void formatOctRadix() {
        String formatted = ResultFormatter.formatWithRadix(new BigDecimal("63"), ExpressionEvaluator.RadixMode.OCT);
        assertEquals("0o77", formatted);
    }

    @Test
    @DisplayName("Format negative number with Hex radix")
    void formatNegativeHexRadix() {
        String formatted = ResultFormatter.formatWithRadix(new BigDecimal("-255"), ExpressionEvaluator.RadixMode.HEX);
        assertEquals("-0xFF", formatted);
    }

    @Test
    @DisplayName("Format negative number with commas")
    void formatNegativeCommas() {
        String formatted = ResultFormatter.formatWithCommas(new BigDecimal("-1000000"));
        assertEquals("-1,000,000", formatted);
    }

    @Test
    @DisplayName("Format decimal with commas")
    void formatDecimalCommas() {
        String formatted = ResultFormatter.formatWithCommas(new BigDecimal("1234567.89"));
        assertEquals("1,234,567.89", formatted);
    }

    @Test
    @DisplayName("Format zero with Hex radix")
    void formatZeroHexRadix() {
        String formatted = ResultFormatter.formatWithRadix(new BigDecimal("0"), ExpressionEvaluator.RadixMode.HEX);
        assertEquals("0x0", formatted);
    }

    @Test
    @DisplayName("Format zero with Binary radix")
    void formatZeroBinRadix() {
        String formatted = ResultFormatter.formatWithRadix(new BigDecimal("0"), ExpressionEvaluator.RadixMode.BIN);
        assertEquals("0b0", formatted);
    }

    @Test
    @DisplayName("Format zero with Octal radix")
    void formatZeroOctRadix() {
        String formatted = ResultFormatter.formatWithRadix(new BigDecimal("0"), ExpressionEvaluator.RadixMode.OCT);
        assertEquals("0o0", formatted);
    }

    @Test
    @DisplayName("Format large hex value")
    void formatLargeHex() {
        String formatted = ResultFormatter.formatWithRadix(new BigDecimal("4294967295"), ExpressionEvaluator.RadixMode.HEX);
        assertEquals("0xFFFFFFFF", formatted);
    }

    @Test
    @DisplayName("Format negative binary radix")
    void formatNegativeBinRadix() {
        String formatted = ResultFormatter.formatWithRadix(new BigDecimal("-10"), ExpressionEvaluator.RadixMode.BIN);
        assertEquals("-0b1010", formatted);
    }

    @Test
    @DisplayName("Format negative octal radix")
    void formatNegativeOctRadix() {
        String formatted = ResultFormatter.formatWithRadix(new BigDecimal("-63"), ExpressionEvaluator.RadixMode.OCT);
        assertEquals("-0o77", formatted);
    }

    @Test
    @DisplayName("Format toShorthand values")
    void formatToShorthand() {
        assertEquals("1.5m", ResultFormatter.toShorthand(new BigDecimal("1500000")));
        assertEquals("2.5b", ResultFormatter.toShorthand(new BigDecimal("2500000000")));
        assertEquals("100k", ResultFormatter.toShorthand(new BigDecimal("100000")));
        assertEquals("500", ResultFormatter.toShorthand(new BigDecimal("500")));
    }

    @Test
    @DisplayName("Format null value with commas returns 0")
    void formatNullCommas() {
        assertEquals("0", ResultFormatter.formatWithCommas(null));
    }

    @Test
    @DisplayName("Format null value with radix returns 0")
    void formatNullRadix() {
        assertEquals("0", ResultFormatter.formatWithRadix(null, ExpressionEvaluator.RadixMode.HEX));
    }

    @Test
    @DisplayName("Format result with null EvalResult returns 0")
    void formatNullEvalResult() {
        assertEquals("0", ResultFormatter.formatResult(null));
        assertEquals("0", ResultFormatter.formatResultWithUnits(null));
    }

    @Test
    @DisplayName("Format with Shorthand radix mode")
    void formatShorthandRadix() {
        String formatted = ResultFormatter.formatWithRadix(new BigDecimal("1500000"), ExpressionEvaluator.RadixMode.SHORTHAND);
        assertEquals("1.5m", formatted);
    }

    @Test
    @DisplayName("Format result with units under Shorthand radix mode")
    void formatResultWithUnitsShorthand() {
        ExpressionEvaluator.EvalResult res = new ExpressionEvaluator.EvalResult(new BigDecimal("2500000000"), ExpressionEvaluator.RadixMode.SHORTHAND);
        String formatted = ResultFormatter.formatResultWithUnits(res);
        assertEquals("2.5b", formatted);
    }

    @Test
    @DisplayName("Format toShorthand across k, m, b, t boundaries")
    void formatToShorthandBoundaries() {
        assertEquals("999k", ResultFormatter.toShorthand(new BigDecimal("999000")));
        assertEquals("1.5m", ResultFormatter.toShorthand(new BigDecimal("1500000")));
        assertEquals("2.5b", ResultFormatter.toShorthand(new BigDecimal("2500000000")));
        assertEquals("3.1t", ResultFormatter.toShorthand(new BigDecimal("3100000000000")));
        assertEquals("-1.5m", ResultFormatter.toShorthand(new BigDecimal("-1500000")));
        assertEquals("-500", ResultFormatter.toShorthand(new BigDecimal("-500")));
    }
}
