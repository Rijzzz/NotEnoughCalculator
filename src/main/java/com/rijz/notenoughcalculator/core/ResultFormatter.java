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

import com.rijz.notenoughcalculator.config.CalculatorConfig;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator.EvalResult;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator.RadixMode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.regex.Pattern;

public class ResultFormatter {

	private static String tr(String key, Object... args) {
		if ("notenoughcalculator.unit.suggestion_suffix".equals(key) && args.length > 0) {
			String translated = ExpressionEvaluator.tr(key, args);
			if (translated.equals(key)) {
				return " (" + args[0] + ")";
			}
			return translated;
		}
		return ExpressionEvaluator.tr(key, args);
	}

	public static String getEqualsSign() {
		CalculatorConfig config = CalculatorConfig.getInstance();
		if (!config.enableSyntaxHighlighting) {
			return ColorConstants.EQUALS_PLAIN;
		}
		String val = ExpressionEvaluator.tr("notenoughcalculator.result.equals");
		if (val != null && !val.equals("notenoughcalculator.result.equals")) {
			return val;
		}
		return ColorConstants.EQUALS_HIGHLIGHTED;
	}

	// Format full equation for copying to clipboard (e.g. "1+1 = 2")
	public static String formatEquationForCopy(String expr, String formattedResult) {
		String cleanEquals = stripMinecraftFormatting(getEqualsSign());
		if (cleanEquals.isEmpty())
			cleanEquals = " = ";
		return expr + cleanEquals + formattedResult;
	}

	// Format with commas (preserves arbitrary precision without double-casting
	// loss).
	public static String formatWithCommas(BigDecimal value) {
		if (value == null)
			return "0";
		CalculatorConfig config = CalculatorConfig.getInstance();

		BigDecimal scaled = value.setScale(config.decimalPrecision, RoundingMode.HALF_UP).stripTrailingZeros();
		String plain = scaled.toPlainString();

		if (!config.enableCommaFormatting) {
			return plain;
		}

		return insertCommas(plain);
	}

	// Pure string comma inserter preserving unlimited digits
	public static String insertCommas(String numberStr) {
		if (numberStr == null || numberStr.isEmpty())
			return "";

		int dotIdx = numberStr.indexOf('.');
		String intPart = dotIdx >= 0 ? numberStr.substring(0, dotIdx) : numberStr;
		String fracPart = dotIdx >= 0 ? numberStr.substring(dotIdx) : "";

		String sign = "";
		if (intPart.startsWith("-") || intPart.startsWith("+")) {
			sign = intPart.substring(0, 1);
			intPart = intPart.substring(1);
		}

		StringBuilder sb = new StringBuilder();
		int len = intPart.length();

		for (int i = 0; i < len; i++) {
			if (i > 0 && (len - i) % 3 == 0) {
				sb.append(',');
			}
			sb.append(intPart.charAt(i));
		}

		return sign + sb.toString() + fracPart;
	}

	public static String formatResult(EvalResult evalResult) {
		if (evalResult == null)
			return "0";
		if (evalResult.radixMode != null && evalResult.radixMode != RadixMode.DEFAULT
				&& evalResult.radixMode != RadixMode.NONE) {
			return formatWithRadix(evalResult.value, evalResult.radixMode);
		}
		CalculatorConfig config = CalculatorConfig.getInstance();
		if (config.enableShorthandResults) {
			return toShorthand(evalResult.value);
		}
		return formatWithCommas(evalResult.value);
	}

	public static String formatWithRadix(BigDecimal value, RadixMode radixMode) {
		if (value == null)
			return "0";
		if (radixMode == null || radixMode == RadixMode.DEFAULT || radixMode == RadixMode.NONE) {
			return formatWithCommas(value);
		}
		try {
			BigInteger bi = value.toBigInteger();
			return switch (radixMode) {
				case HEX -> bi.compareTo(BigInteger.ZERO) < 0
						? "-0x" + bi.abs().toString(16).toUpperCase()
						: "0x" + bi.toString(16).toUpperCase();
				case BIN -> bi.compareTo(BigInteger.ZERO) < 0 ? "-0b" + bi.abs().toString(2) : "0b" + bi.toString(2);
				case OCT -> bi.compareTo(BigInteger.ZERO) < 0 ? "-0o" + bi.abs().toString(8) : "0o" + bi.toString(8);
				case SHORTHAND -> toShorthand(value);
				default -> formatWithCommas(value);
			};
		} catch (Exception e) {
			return formatWithCommas(value);
		}
	}

	public static String formatResultWithUnits(EvalResult evalResult) {
		if (evalResult == null)
			return "0";
		if (evalResult.radixMode != null && evalResult.radixMode != RadixMode.DEFAULT
				&& evalResult.radixMode != RadixMode.NONE) {
			return formatWithRadix(evalResult.value, evalResult.radixMode);
		}
		return formatWithUnits(evalResult.value);
	}

	public static String formatWithUnits(BigDecimal value) {
		CalculatorConfig config = CalculatorConfig.getInstance();

		StringBuilder result = new StringBuilder();
		if (config.enableShorthandResults) {
			result.append(toShorthand(value));
		} else {
			result.append(formatWithCommas(value));
		}

		if (config.showUnitSuggestions && !config.enableShorthandResults) {
			String unitSuggestion = suggestUnit(value);
			if (unitSuggestion != null) {
				result.append(tr("notenoughcalculator.unit.suggestion_suffix", unitSuggestion));
			}
		}

		return result.toString();
	}

	// Convert value to Skyblock shorthand (1,500,000 -> 1.5m)
	public static String toShorthand(BigDecimal value) {
		if (value == null)
			return "0";
		BigDecimal abs = value.abs();

		BigDecimal unitT = ExpressionEvaluator.UNITS.get("t");
		BigDecimal unitB = ExpressionEvaluator.UNITS.get("b");
		BigDecimal unitM = ExpressionEvaluator.UNITS.get("m");
		BigDecimal unitK = ExpressionEvaluator.UNITS.get("k");

		if (abs.compareTo(unitT) >= 0) {
			BigDecimal t = value.divide(unitT, 4, RoundingMode.HALF_UP).stripTrailingZeros();
			return t.toPlainString() + "t";
		}
		if (abs.compareTo(unitB) >= 0) {
			BigDecimal b = value.divide(unitB, 4, RoundingMode.HALF_UP).stripTrailingZeros();
			return b.toPlainString() + "b";
		}
		if (abs.compareTo(unitM) >= 0) {
			BigDecimal m = value.divide(unitM, 4, RoundingMode.HALF_UP).stripTrailingZeros();
			return m.toPlainString() + "m";
		}
		if (abs.compareTo(unitK) >= 0) {
			BigDecimal k = value.divide(unitK, 4, RoundingMode.HALF_UP).stripTrailingZeros();
			return k.toPlainString() + "k";
		}
		return formatWithCommas(value);
	}

	// Suggest a Skyblock unit that matches this number
	private static String suggestUnit(BigDecimal value) {
		BigDecimal abs = value.abs();

		BigDecimal unitEB = ExpressionEvaluator.UNITS.get("eb");
		BigDecimal unitDC = ExpressionEvaluator.UNITS.get("dc");
		BigDecimal unitH = ExpressionEvaluator.UNITS.get("h");
		BigDecimal unitT = ExpressionEvaluator.UNITS.get("t");
		BigDecimal unitB = ExpressionEvaluator.UNITS.get("b");
		BigDecimal unitM = ExpressionEvaluator.UNITS.get("m");
		BigDecimal unitK = ExpressionEvaluator.UNITS.get("k");
		BigDecimal unitS = ExpressionEvaluator.UNITS.get("s");

		// Check for exact storage container sizes first
		if (abs.compareTo(unitEB) == 0) {
			return tr("notenoughcalculator.unit.suggestion.ender_chest");
		}
		if (abs.compareTo(unitDC) == 0) {
			return tr("notenoughcalculator.unit.suggestion.double_chest");
		}
		if (abs.compareTo(unitH) == 0) {
			return tr("notenoughcalculator.unit.suggestion.shulker");
		}

		if (abs.compareTo(unitT) >= 0) {
			BigDecimal t = value.divide(unitT, 2, RoundingMode.HALF_UP);
			if (t.stripTrailingZeros().scale() <= 2) {
				return t.stripTrailingZeros().toPlainString() + "t";
			}
		}

		if (abs.compareTo(unitB) >= 0) {
			BigDecimal b = value.divide(unitB, 2, RoundingMode.HALF_UP);
			if (b.stripTrailingZeros().scale() <= 2) {
				return b.stripTrailingZeros().toPlainString() + "b";
			}
		}

		if (abs.compareTo(unitM) >= 0) {
			BigDecimal m = value.divide(unitM, 2, RoundingMode.HALF_UP);
			if (m.stripTrailingZeros().scale() <= 2) {
				return m.stripTrailingZeros().toPlainString() + "m";
			}
		}

		if (abs.compareTo(unitK) >= 0) {
			BigDecimal k = value.divide(unitK, 2, RoundingMode.HALF_UP);
			if (k.stripTrailingZeros().scale() <= 2) {
				return k.stripTrailingZeros().toPlainString() + "k";
			}
		}

		// Suggest stacks for smaller numbers that are multiples of 64
		if (abs.compareTo(unitS) >= 0 && abs.compareTo(new BigDecimal("10000")) < 0) {
			BigDecimal stacks = value.divide(unitS, 10, RoundingMode.HALF_UP);
			if (stacks.stripTrailingZeros().scale() <= 0) {
				long stackCount = stacks.longValue();
				if (stackCount == 1) {
					return tr("notenoughcalculator.unit.suggestion.stack_singular");
				} else {
					return tr("notenoughcalculator.unit.suggestion.stack_plural", stackCount);
				}
			} else if (stacks.stripTrailingZeros().scale() <= 2) {
				return tr("notenoughcalculator.unit.suggestion.stacks_decimal",
						stacks.stripTrailingZeros().toPlainString());
			}
		}

		return null;
	}

	private static final Pattern MINECRAFT_COLOR_PATTERN = Pattern.compile("§[0-9a-fk-orA-FK-OR]");

	// Strip Minecraft color and formatting codes (§a, §l, etc.)
	public static String stripMinecraftFormatting(String input) {
		return input == null ? "" : MINECRAFT_COLOR_PATTERN.matcher(input).replaceAll("");
	}

	public static String cleanInput(String input) {
		return input == null ? "" : input.replaceAll("\\p{Cf}", "").trim();
	}
}
