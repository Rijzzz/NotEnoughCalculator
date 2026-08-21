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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpressionTokenizerTest {

    @Test
    @DisplayName("Tokenize basic numbers and math operators")
    void testBasicTokenization() throws Exception {
        List<Token> tokens = ExpressionTokenizer.tokenize("100 + 200 * 3", BigDecimal.ZERO);
        assertNotNull(tokens);
        assertTrue(tokens.size() >= 5);
        assertEquals(TokenKind.NUM, tokens.get(0).kind);
        assertEquals("100", tokens.get(0).value);
        assertEquals(TokenKind.OP, tokens.get(1).kind);
        assertEquals("+", tokens.get(1).value);
    }

    @Test
    @DisplayName("Tokenize SkyBlock units")
    void testUnitTokenization() throws Exception {
        List<Token> tokens = ExpressionTokenizer.tokenize("10k + 5m", BigDecimal.ZERO);
        assertNotNull(tokens);
        assertEquals(TokenKind.NUM, tokens.get(0).kind);
        assertEquals(TokenKind.UNIT, tokens.get(1).kind);
        assertEquals("k", tokens.get(1).value);
    }

    @Test
    @DisplayName("Tokenize radix literals (0b, 0x, 0o)")
    void testRadixLiteralTokenization() throws Exception {
        List<Token> hexTokens = ExpressionTokenizer.tokenize("0xFF", BigDecimal.ZERO);
        assertEquals(TokenKind.NUM, hexTokens.get(0).kind);
        assertEquals(0, new BigDecimal("255").compareTo(hexTokens.get(0).number));

        List<Token> binTokens = ExpressionTokenizer.tokenize("0b1010", BigDecimal.ZERO);
        assertEquals(TokenKind.NUM, binTokens.get(0).kind);
        assertEquals(0, new BigDecimal("10").compareTo(binTokens.get(0).number));
    }

    @Test
    @DisplayName("Tokenize 'x' as multiplication operator")
    void testXMultiplicationTokenization() throws Exception {
        List<Token> tokens = ExpressionTokenizer.tokenize("10x5", BigDecimal.ZERO);
        assertEquals(TokenKind.NUM, tokens.get(0).kind);
        assertEquals(TokenKind.OP, tokens.get(1).kind);
        assertEquals("*", tokens.get(1).value);
    }
}
