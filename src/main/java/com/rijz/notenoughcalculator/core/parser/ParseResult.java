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

import java.math.BigDecimal;

public class ParseResult {
	public final BigDecimal value;
	public final int nextPos;
	public final RadixMode radixMode;
	public final boolean isPercentage;

	public ParseResult(BigDecimal value, int nextPos) {
		this(value, nextPos, RadixMode.DEFAULT, false);
	}

	public ParseResult(BigDecimal value, int nextPos, RadixMode radixMode) {
		this(value, nextPos, radixMode, false);
	}

	public ParseResult(BigDecimal value, int nextPos, RadixMode radixMode, boolean isPercentage) {
		this.value = value;
		this.nextPos = nextPos;
		this.radixMode = radixMode != null ? radixMode : RadixMode.DEFAULT;
		this.isPercentage = isPercentage;
	}

	public ParseResult withPercentage(boolean isPercentage) {
		return new ParseResult(this.value, this.nextPos, this.radixMode, isPercentage);
	}
}
