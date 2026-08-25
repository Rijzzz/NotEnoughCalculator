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

package com.rijz.notenoughcalculator.client.util;

import com.rijz.notenoughcalculator.core.ColorConstants;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {

	public static String getColorNumber() {
		return ColorConstants.NUMBER;
	}

	public static String getColorRadixLiteral() {
		return ColorConstants.RADIX_LITERAL;
	}

	public static String getColorUnit() {
		return ColorConstants.UNIT;
	}

	public static String getColorMathFunc() {
		return ColorConstants.MATH_FUNCTION;
	}

	public static String getColorProgressionFunc() {
		return ColorConstants.PROGRESSION_FUNCTION;
	}

	public static String getColorMarketFunc() {
		return ColorConstants.MARKET_FUNCTION;
	}

	public static String getColorBuiltinVar() {
		return ColorConstants.BUILTIN_VARIABLE;
	}

	public static String getColorCustomVar() {
		return ColorConstants.CUSTOM_VARIABLE;
	}

	public static String getColorDollarSign() {
		return ColorConstants.DOLLAR_SIGN;
	}

	public static String getColorStringItem() {
		return ColorConstants.STRING_ITEM;
	}

	public static String getColorOp() {
		return ColorConstants.OPERATOR;
	}

	public static String getColorDelim() {
		return ColorConstants.DELIMITER;
	}

	public static String getColorError() {
		return ColorConstants.ERROR;
	}

	public static String getColorResult() {
		return ColorConstants.RESULT;
	}

	public static final Set<String> MARKET_FUNCTIONS = ExpressionEvaluator.MARKET_FUNCTIONS;
	public static final Set<String> PROGRESSION_FUNCTIONS = ExpressionEvaluator.PROGRESSION_FUNCTIONS;
	public static final Set<String> RADIX_FUNCTIONS = ExpressionEvaluator.RADIX_FUNCTIONS;
	public static final Set<String> MATH_FUNCTIONS = ExpressionEvaluator.MATH_FUNCTIONS;
	public static final Set<String> UNITS = ExpressionEvaluator.UNITS.keySet();
	public static final Set<String> BUILTIN_VARS = ExpressionEvaluator.BUILTIN_VARIABLES;

	private static final Pattern TOKEN_PATTERN = Pattern.compile("(?i)" + "(\\$\\w+)|" + // Variable with $ prefix
																							// ($var)
			"(\"[^\"]*\"|'[^']*')|" + // Quoted string literal ("ITEM" or 'ITEM')
			"(0[bxo][0-9a-fA-F_]+)|" + // Number literal (0b, 0x, 0o)
			"(\\d+\\.?\\d*\\s*" + ExpressionEvaluator.UNITS_REGEX + "(?![a-zA-Z0-9_]))|" + // Number with unit
			"(\\d+\\.?\\d*)|" + // Plain number
			"([a-zA-Z0-9_]+)|" + // Word (function / builtin var / item id)
			"(<<|>>|[+\\-*/^%xX!&|~(),=])|" + // Operators / Delimiters
			"(\\s+)|" + // Whitespace
			"(.)" // Anything else
	);

	public static String highlight(String input) {
		if (input == null || input.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		Matcher matcher = TOKEN_PATTERN.matcher(input);

		while (matcher.find()) {
			if (matcher.group(1) != null) {
				String varName = matcher.group(1);
				String cleanName = varName.substring(1).toLowerCase();
				sb.append(getColorDollarSign()).append("$");
				if (BUILTIN_VARS.contains(cleanName)) {
					sb.append(getColorBuiltinVar()).append(varName.substring(1));
				} else {
					sb.append(getColorCustomVar()).append(varName.substring(1));
				}
			} else if (matcher.group(2) != null) {
				sb.append(getColorStringItem()).append(matcher.group(2));
			} else if (matcher.group(3) != null) {
				sb.append(getColorRadixLiteral()).append(matcher.group(3));
			} else if (matcher.group(4) != null) {
				splitNumberAndUnit(matcher.group(4), sb);
			} else if (matcher.group(5) != null) {
				sb.append(getColorNumber()).append(matcher.group(5));
			} else if (matcher.group(6) != null) {
				String word = matcher.group(6);
				String lower = word.toLowerCase();
				if (MARKET_FUNCTIONS.contains(lower)) {
					sb.append(getColorMarketFunc()).append(word);
				} else if (PROGRESSION_FUNCTIONS.contains(lower)) {
					sb.append(getColorProgressionFunc()).append(word);
				} else if (RADIX_FUNCTIONS.contains(lower)) {
					sb.append(getColorRadixLiteral()).append(word);
				} else if (MATH_FUNCTIONS.contains(lower)) {
					sb.append(getColorMathFunc()).append(word);
				} else if (BUILTIN_VARS.contains(lower)) {
					sb.append(getColorBuiltinVar()).append(word);
				} else if (UNITS.contains(lower)) {
					sb.append(getColorUnit()).append(word);
				} else {
					sb.append(getColorStringItem()).append(word);
				}
			} else if (matcher.group(7) != null) {
				String opToken = matcher.group(7);
				if (opToken.equals("(") || opToken.equals(")") || opToken.equals(",")) {
					sb.append(getColorDelim()).append(opToken);
				} else {
					sb.append(getColorOp()).append(opToken);
				}
			} else if (matcher.group(8) != null) {
				sb.append(matcher.group(8));
			} else if (matcher.group(9) != null) {
				sb.append(getColorNumber()).append(matcher.group(9));
			}
		}

		return sb.toString();
	}

	private static void splitNumberAndUnit(String numUnitStr, StringBuilder sb) {
		int idx = 0;
		int len = numUnitStr.length();
		while (idx < len && (Character.isDigit(numUnitStr.charAt(idx)) || numUnitStr.charAt(idx) == '.'
				|| Character.isWhitespace(numUnitStr.charAt(idx)))) {
			idx++;
		}
		String numPart = numUnitStr.substring(0, idx);
		String unitPart = numUnitStr.substring(idx);

		sb.append(getColorNumber()).append(numPart);
		if (!unitPart.isEmpty()) {
			sb.append(getColorUnit()).append(unitPart);
		}
	}
}
