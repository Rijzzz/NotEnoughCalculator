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

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenTest {

	@Test
	@DisplayName("Token creation holds correct kind, value, and character position")
	void testTokenFields() {
		Token tok = new Token(TokenKind.NUM, "100", 0, new BigDecimal("100"));

		assertEquals(TokenKind.NUM, tok.kind);
		assertEquals("100", tok.value);
		assertEquals(0, tok.pos);
		assertEquals(0, new BigDecimal("100").compareTo(tok.number));
	}
}
