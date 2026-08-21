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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionParserTest {

    private MathContext mc;
    private Map<String, BigDecimal> variables;
    private ExpressionParser parser;

    @BeforeEach
    void setUp() {
        mc = new MathContext(50, RoundingMode.HALF_UP);
        variables = new HashMap<>();
        parser = new ExpressionParser(mc, variables, variables::put);
    }

    @Test
    @DisplayName("Parse basic addition and multiplication expression")
    void testParseBasicExpression() throws Exception {
        List<Token> tokens = ExpressionTokenizer.tokenize("10 + 20 * 2", BigDecimal.ZERO);
        ParseResult result = parser.parse(tokens);
        assertNotNull(result);
        assertEquals(0, new BigDecimal("50").compareTo(result.value));
    }

    @Test
    @DisplayName("Parse hex conversion function")
    void testParseHexFunction() throws Exception {
        List<Token> tokens = ExpressionTokenizer.tokenize("hex(255)", BigDecimal.ZERO);
        ParseResult result = parser.parse(tokens);
        assertNotNull(result);
        assertEquals(0, new BigDecimal("255").compareTo(result.value));
        assertEquals(RadixMode.HEX, result.radixMode);
    }

    @Test
    @DisplayName("Parse custom variable evaluation")
    void testParseCustomVariable() throws Exception {
        variables.put("buy", new BigDecimal("50000000"));
        List<Token> tokens = ExpressionTokenizer.tokenize("$buy * 2", BigDecimal.ZERO);
        ParseResult result = parser.parse(tokens);
        assertNotNull(result);
        assertEquals(0, new BigDecimal("100000000").compareTo(result.value));
    }
}
