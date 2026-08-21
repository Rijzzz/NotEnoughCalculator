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

package com.rijz.notenoughcalculator.core.parser;

import com.rijz.notenoughcalculator.core.ExpressionEvaluator.RadixMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ParseResultTest {

    @Test
    @DisplayName("Default constructor sets RadixMode.DEFAULT and isPercentage false")
    void testDefaultConstructor() {
        ParseResult result = new ParseResult(new BigDecimal("100"), 5);
        assertEquals(0, new BigDecimal("100").compareTo(result.value));
        assertEquals(5, result.nextPos);
        assertEquals(RadixMode.DEFAULT, result.radixMode);
        assertFalse(result.isPercentage);
    }

    @Test
    @DisplayName("Custom RadixMode constructor preserves radix mode")
    void testCustomRadixMode() {
        ParseResult result = new ParseResult(new BigDecimal("255"), 8, RadixMode.HEX);
        assertEquals(RadixMode.HEX, result.radixMode);
        assertFalse(result.isPercentage);
    }

    @Test
    @DisplayName("Null RadixMode parameter falls back to DEFAULT")
    void testNullRadixModeFallback() {
        ParseResult result = new ParseResult(new BigDecimal("10"), 2, null);
        assertEquals(RadixMode.DEFAULT, result.radixMode);
    }
}
