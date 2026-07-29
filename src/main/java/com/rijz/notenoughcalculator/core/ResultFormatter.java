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
import net.minecraft.client.resources.language.I18n;

import java.math.BigDecimal;
import java.math.RoundingMode;

// Makes numbers look nice with commas and unit suggestions
// Example: 1000000 -> "1,000,000 (1m)"
public class ResultFormatter {

    // Helper method for translations
    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }

    // Format with commas only (used for inline display in REI)
    // Uses pure String manipulation to preserve 100% arbitrary precision without double-casting loss
    public static String formatWithCommas(BigDecimal value) {
        if (value == null) return "0";
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
        if (numberStr == null || numberStr.isEmpty()) return "";

        int dotIdx = numberStr.indexOf('.');
        String intPart = dotIdx >= 0 ? numberStr.substring(0, dotIdx) : numberStr;
        String fracPart = dotIdx >= 0 ? numberStr.substring(dotIdx) : "";

        // Handle leading sign (- or +)
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

    // Format result considering radix mode (hex, bin, oct) or decimal commas
    public static String formatResult(ExpressionEvaluator.EvalResult evalResult) {
        if (evalResult == null) return "0";
        if (evalResult.radixMode != null && evalResult.radixMode != ExpressionEvaluator.RadixMode.DEFAULT) {
            return formatWithRadix(evalResult.value, evalResult.radixMode);
        }
        CalculatorConfig config = CalculatorConfig.getInstance();
        if (config.enableShorthandResults) {
            return toShorthand(evalResult.value);
        }
        return formatWithCommas(evalResult.value);
    }

    // Convert value to base 16 (0xFF), base 2 (0b1010), or base 8 (0o77)
    public static String formatWithRadix(BigDecimal value, ExpressionEvaluator.RadixMode radixMode) {
        if (value == null) return "0";
        if (radixMode == null || radixMode == ExpressionEvaluator.RadixMode.DEFAULT) {
            return formatWithCommas(value);
        }
        try {
            java.math.BigInteger bi = value.toBigInteger();
            return switch (radixMode) {
                case HEX -> bi.compareTo(java.math.BigInteger.ZERO) < 0
                        ? "-0x" + bi.abs().toString(16).toUpperCase()
                        : "0x" + bi.toString(16).toUpperCase();
                case BIN -> bi.compareTo(java.math.BigInteger.ZERO) < 0
                        ? "-0b" + bi.abs().toString(2)
                        : "0b" + bi.toString(2);
                case OCT -> bi.compareTo(java.math.BigInteger.ZERO) < 0
                        ? "-0o" + bi.abs().toString(8)
                        : "0o" + bi.toString(8);
                case SHORTHAND -> toShorthand(value);
                default -> formatWithCommas(value);
            };
        } catch (Exception e) {
            return formatWithCommas(value);
        }
    }

    // Format with units OR radix representation
    public static String formatResultWithUnits(ExpressionEvaluator.EvalResult evalResult) {
        if (evalResult == null) return "0";
        if (evalResult.radixMode != null && evalResult.radixMode != ExpressionEvaluator.RadixMode.DEFAULT) {
            return formatWithRadix(evalResult.value, evalResult.radixMode);
        }
        return formatWithUnits(evalResult.value);
    }

    // Format with commas AND unit suggestions (used for chat commands)
    // Example: "50,000,000 (50m)"
    public static String formatWithUnits(BigDecimal value) {
        CalculatorConfig config = CalculatorConfig.getInstance();

        StringBuilder result = new StringBuilder();
        if (config.enableShorthandResults) {
            result.append(toShorthand(value));
        } else {
            result.append(formatWithCommas(value));
        }

        // Add helpful unit suggestions if enabled
        if (config.showUnitSuggestions && !config.enableShorthandResults) {
            String unitSuggestion = suggestUnit(value);
            if (unitSuggestion != null) {
                result.append(" (").append(unitSuggestion).append(")");
            }
        }

        return result.toString();
    }

    // Convert value to Skyblock shorthand (1,500,000 -> 1.5m)
    public static String toShorthand(BigDecimal value) {
        if (value == null) return "0";
        BigDecimal abs = value.abs();

        if (abs.compareTo(new BigDecimal("1000000000000")) >= 0) {
            BigDecimal t = value.divide(new BigDecimal("1000000000000"), 4, RoundingMode.HALF_UP).stripTrailingZeros();
            return t.toPlainString() + "t";
        }
        if (abs.compareTo(new BigDecimal("1000000000")) >= 0) {
            BigDecimal b = value.divide(new BigDecimal("1000000000"), 4, RoundingMode.HALF_UP).stripTrailingZeros();
            return b.toPlainString() + "b";
        }
        if (abs.compareTo(new BigDecimal("1000000")) >= 0) {
            BigDecimal m = value.divide(new BigDecimal("1000000"), 4, RoundingMode.HALF_UP).stripTrailingZeros();
            return m.toPlainString() + "m";
        }
        if (abs.compareTo(new BigDecimal("1000")) >= 0) {
            BigDecimal k = value.divide(new BigDecimal("1000"), 4, RoundingMode.HALF_UP).stripTrailingZeros();
            return k.toPlainString() + "k";
        }
        return formatWithCommas(value);
    }

    // Suggest a Skyblock unit that matches this number
    private static String suggestUnit(BigDecimal value) {
        BigDecimal abs = value.abs();

        // Check for exact storage container sizes first
        if (abs.compareTo(new BigDecimal("2880")) == 0) {
            return tr("notenoughcalculator.unit.suggestion.ender_chest");
        }
        if (abs.compareTo(new BigDecimal("3456")) == 0) {
            return tr("notenoughcalculator.unit.suggestion.double_chest");
        }
        if (abs.compareTo(new BigDecimal("1728")) == 0) {
            return tr("notenoughcalculator.unit.suggestion.shulker");
        }

        // Suggest currency units for large numbers
        if (abs.compareTo(new BigDecimal("1000000000000")) >= 0) {
            BigDecimal t = value.divide(new BigDecimal("1000000000000"), 2, RoundingMode.HALF_UP);
            if (t.stripTrailingZeros().scale() <= 2) {
                return t.stripTrailingZeros().toPlainString() + "t";
            }
        }

        if (abs.compareTo(new BigDecimal("1000000000")) >= 0) {
            BigDecimal b = value.divide(new BigDecimal("1000000000"), 2, RoundingMode.HALF_UP);
            if (b.stripTrailingZeros().scale() <= 2) {
                return b.stripTrailingZeros().toPlainString() + "b";
            }
        }

        if (abs.compareTo(new BigDecimal("1000000")) >= 0) {
            BigDecimal m = value.divide(new BigDecimal("1000000"), 2, RoundingMode.HALF_UP);
            if (m.stripTrailingZeros().scale() <= 2) {
                return m.stripTrailingZeros().toPlainString() + "m";
            }
        }

        if (abs.compareTo(new BigDecimal("1000")) >= 0) {
            BigDecimal k = value.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
            if (k.stripTrailingZeros().scale() <= 2) {
                return k.stripTrailingZeros().toPlainString() + "k";
            }
        }

        // Suggest stacks for smaller numbers that are multiples of 64
        if (abs.compareTo(new BigDecimal("64")) >= 0 && abs.compareTo(new BigDecimal("10000")) < 0) {
            BigDecimal stacks = value.divide(new BigDecimal("64"), 10, RoundingMode.HALF_UP);
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

    // Remove any weird formatting characters from user input
    public static String cleanInput(String input) {
        return input == null ? "" : input.replaceAll("\\p{Cf}", "").trim();
    }
}