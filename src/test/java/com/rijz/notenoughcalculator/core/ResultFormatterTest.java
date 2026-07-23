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
}
