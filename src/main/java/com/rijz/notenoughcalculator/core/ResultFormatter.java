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

        BigDecimal stripped = value.stripTrailingZeros();
        String plain = stripped.toPlainString();

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

    // Format with commas AND unit suggestions (used for chat commands)
    // Example: "50,000,000 (50m)"
    public static String formatWithUnits(BigDecimal value) {
        CalculatorConfig config = CalculatorConfig.getInstance();

        StringBuilder result = new StringBuilder();
        result.append(formatWithCommas(value));

        // Add helpful unit suggestions if enabled
        if (config.showUnitSuggestions) {
            String unitSuggestion = suggestUnit(value);
            if (unitSuggestion != null) {
                result.append(" (").append(unitSuggestion).append(")");
            }
        }

        return result.toString();
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