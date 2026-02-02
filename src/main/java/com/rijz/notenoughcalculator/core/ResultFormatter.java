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
import net.minecraft.client.resource.language.I18n;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

// Makes numbers look nice with commas and unit suggestions
// Example: 1000000 -> "1,000,000 (1m)"
public class ResultFormatter {

    private static final DecimalFormat COMMA_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        COMMA_FORMAT = new DecimalFormat("#,##0.##########", symbols);
        COMMA_FORMAT.setMaximumFractionDigits(10);
    }

    // Helper method for translations
    private static String tr(String key, Object... args) {
        return I18n.translate(key, args);
    }

    // Format with commas only (used for inline display in REI)
    // This matches how NEU calculator shows results
    public static String formatWithCommas(BigDecimal value) {
        return COMMA_FORMAT.format(value.stripTrailingZeros());
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
            BigDecimal t = value.divide(new BigDecimal("1000000000000"), 2, BigDecimal.ROUND_HALF_UP);
            if (t.stripTrailingZeros().scale() <= 2) {
                return t.stripTrailingZeros().toPlainString() + "t";
            }
        }

        if (abs.compareTo(new BigDecimal("1000000000")) >= 0) {
            BigDecimal b = value.divide(new BigDecimal("1000000000"), 2, BigDecimal.ROUND_HALF_UP);
            if (b.stripTrailingZeros().scale() <= 2) {
                return b.stripTrailingZeros().toPlainString() + "b";
            }
        }

        if (abs.compareTo(new BigDecimal("1000000")) >= 0) {
            BigDecimal m = value.divide(new BigDecimal("1000000"), 2, BigDecimal.ROUND_HALF_UP);
            if (m.stripTrailingZeros().scale() <= 2) {
                return m.stripTrailingZeros().toPlainString() + "m";
            }
        }

        if (abs.compareTo(new BigDecimal("1000")) >= 0) {
            BigDecimal k = value.divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_UP);
            if (k.stripTrailingZeros().scale() <= 2) {
                return k.stripTrailingZeros().toPlainString() + "k";
            }
        }

        // Suggest stacks for smaller numbers that are multiples of 64
        if (abs.compareTo(new BigDecimal("64")) >= 0 && abs.compareTo(new BigDecimal("10000")) < 0) {
            BigDecimal stacks = value.divide(new BigDecimal("64"), 10, BigDecimal.ROUND_HALF_UP);
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