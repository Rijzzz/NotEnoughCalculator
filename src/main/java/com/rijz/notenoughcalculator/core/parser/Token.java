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

import java.math.BigDecimal;

public class Token {
	public final TokenKind kind;
	public final String value;
	public final int pos;
	public final BigDecimal number;

	public Token(TokenKind kind, String value, int pos) {
		this(kind, value, pos, null);
	}

	public Token(TokenKind kind, String value, int pos, BigDecimal number) {
		this.kind = kind;
		this.value = value;
		this.pos = pos;
		this.number = number;
	}
}
