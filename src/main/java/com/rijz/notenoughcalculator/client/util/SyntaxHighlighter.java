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

import com.rijz.notenoughcalculator.core.ExpressionEvaluator;
import net.minecraft.client.resources.language.I18n;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {

    public static final String COLOR_NUMBER = "§f"; // Pure White
    public static final String COLOR_RADIX_LITERAL = "§d"; // Light Purple / Magenta
    public static final String COLOR_UNIT = "§b"; // Vibrant Aqua / Cyan
    public static final String COLOR_MATH_FUNC = "§e"; // Bright Yellow
    public static final String COLOR_PROGRESSION_FUNC = "§6"; // Vibrant Gold
    public static final String COLOR_MARKET_FUNC = "§9"; // Royal Blue
    public static final String COLOR_BUILTIN_VAR = "§b"; // Vibrant Aqua
    public static final String COLOR_CUSTOM_VAR = "§a"; // Bright Lime Green
    public static final String COLOR_DOLLAR_SIGN = "§6"; // Vibrant Gold
    public static final String COLOR_STRING_ITEM = "§d"; // Light Purple / Pink
    public static final String COLOR_OP = "§c"; // Bright Light Red
    public static final String COLOR_DELIM = "§7"; // Neutral Light Gray
    public static final String COLOR_ERROR = "§c"; // Bright Light Red
    public static final String COLOR_CHAT_RESULT = "§1"; // Dark Blue

    public static String getColor(String key, String defaultColor) {
        try {
            String val = I18n.get(key);
            if (val != null && !val.equals(key)) {
                return val;
            }
        } catch (Throwable ignored) {}
        return defaultColor;
    }

    public static String getColorNumber() { return getColor("notenoughcalculator.color.number", COLOR_NUMBER); }
    public static String getColorRadixLiteral() { return getColor("notenoughcalculator.color.radix_literal", COLOR_RADIX_LITERAL); }
    public static String getColorUnit() { return getColor("notenoughcalculator.color.unit", COLOR_UNIT); }
    public static String getColorMathFunc() { return getColor("notenoughcalculator.color.math_function", COLOR_MATH_FUNC); }
    public static String getColorProgressionFunc() { return getColor("notenoughcalculator.color.progression_function", COLOR_PROGRESSION_FUNC); }
    public static String getColorMarketFunc() { return getColor("notenoughcalculator.color.market_function", COLOR_MARKET_FUNC); }
    public static String getColorBuiltinVar() { return getColor("notenoughcalculator.color.builtin_variable", COLOR_BUILTIN_VAR); }
    public static String getColorCustomVar() { return getColor("notenoughcalculator.color.custom_variable", COLOR_CUSTOM_VAR); }
    public static String getColorDollarSign() { return getColor("notenoughcalculator.color.dollar_sign", COLOR_DOLLAR_SIGN); }
    public static String getColorStringItem() { return getColor("notenoughcalculator.color.string_item", COLOR_STRING_ITEM); }
    public static String getColorOp() { return getColor("notenoughcalculator.color.operator", COLOR_OP); }
    public static String getColorDelim() { return getColor("notenoughcalculator.color.delimiter", COLOR_DELIM); }
    public static String getColorError() { return getColor("notenoughcalculator.color.error", COLOR_ERROR); }
    public static String getColorResult() { return getColor("notenoughcalculator.color.result", COLOR_CHAT_RESULT); }

    public static final Set<String> MARKET_FUNCTIONS = ExpressionEvaluator.MARKET_FUNCTIONS;
    public static final Set<String> PROGRESSION_FUNCTIONS = ExpressionEvaluator.PROGRESSION_FUNCTIONS;
    public static final Set<String> RADIX_FUNCTIONS = ExpressionEvaluator.RADIX_FUNCTIONS;
    public static final Set<String> MATH_FUNCTIONS = ExpressionEvaluator.MATH_FUNCTIONS;
    public static final Set<String> UNITS = ExpressionEvaluator.UNITS.keySet();
    public static final Set<String> BUILTIN_VARS = ExpressionEvaluator.BUILTIN_VARIABLES;

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(?i)" +
                    "(\\$\\w+)|" + // Variable with $ prefix ($var)
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
        // Separate leading number from trailing unit (e.g. "100m", "2.5dc")
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
