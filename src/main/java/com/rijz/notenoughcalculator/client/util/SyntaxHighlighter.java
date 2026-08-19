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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {

    public static final String COLOR_NUMBER = "§f";          // Pure White (Maximum Legibility)
    public static final String COLOR_RADIX_LITERAL = "§d";   // Light Purple / Magenta (Ultra Bright)
    public static final String COLOR_UNIT = "§b";            // Vibrant Aqua / Cyan (Ultra Bright)
    public static final String COLOR_MATH_FUNC = "§e";       // Bright Yellow (Ultra Bright)
    public static final String COLOR_MARKET_FUNC = "§9";     // Royal Blue (Ultra Clear)
    public static final String COLOR_BUILTIN_VAR = "§b";     // Vibrant Aqua (Ultra Clear & High Contrast)
    public static final String COLOR_CUSTOM_VAR = "§a";      // Bright Lime Green (Ultra Bright)
    public static final String COLOR_DOLLAR_SIGN = "§6";     // Vibrant Gold (Ultra Bright)
    public static final String COLOR_STRING_ITEM = "§d";     // Light Purple / Pink (Item IDs & String Arguments)
    public static final String COLOR_OP = "§c";              // Bright Light Red (High Contrast & Clear)
    public static final String COLOR_DELIM = "§7";           // Neutral Light Gray
    public static final String COLOR_ERROR = "§c";           // Bright Light Red
    public static final String COLOR_CHAT_RESULT = "§a";     // Bright Lime Green (High Contrast & Clear)

    private static final Set<String> MARKET_FUNCTIONS = Set.of(
            "bz", "bzb", "bzbuy", "bzs", "bzsell", "bzm", "bzmargin",
            "lb", "lowestbin", "lba", "lowestbinavg", "npc", "npcsell",
            "motes", "motessell", "price", "sack", "sackcount"
    );

    private static final Set<String> MATH_FUNCTIONS;
    static {
        Set<String> funcs = new HashSet<>(ExpressionEvaluator.FUNCTIONS);
        funcs.removeAll(MARKET_FUNCTIONS);
        MATH_FUNCTIONS = Set.copyOf(funcs);
    }

    private static final Set<String> UNITS = ExpressionEvaluator.UNITS.keySet();
    private static final Set<String> BUILTIN_VARS = ExpressionEvaluator.BUILTIN_VARIABLES;

    private static String buildUnitsRegex() {
        List<String> sortedUnits = new ArrayList<>(ExpressionEvaluator.UNITS.keySet());
        sortedUnits.sort((a, b) -> Integer.compare(b.length(), a.length()));
        StringBuilder sb = new StringBuilder("(?:");
        for (int i = 0; i < sortedUnits.size(); i++) {
            if (i > 0) sb.append("|");
            sb.append(Pattern.quote(sortedUnits.get(i)));
        }
        sb.append(")");
        return sb.toString();
    }

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(?i)" +
            "(\\$\\w+)|" +                                                            // Variable with $ prefix ($var)
            "(\"[^\"]*\"|'[^']*')|" +                                                 // Quoted string literal ("ITEM" or 'ITEM')
            "(0[bxo][0-9a-fA-F_]+)|" +                                                // Number literal (0b, 0x, 0o)
            "(\\d+\\.?\\d*\\s*" + buildUnitsRegex() + "(?![a-zA-Z0-9_]))|" +          // Number with unit
            "(\\d+\\.?\\d*)|" +                                                       // Plain number
            "([a-zA-Z0-9_]+)|" +                                                      // Word (function / builtin var / item id)
            "(<<|>>|[+\\-*/^%xX!&|~(),=])|" +                                         // Operators / Delimiters
            "(\\s+)|" +                                                               // Whitespace
            "(.)"                                                                     // Anything else
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
                sb.append(COLOR_DOLLAR_SIGN).append("$");
                if (BUILTIN_VARS.contains(cleanName)) {
                    sb.append(COLOR_BUILTIN_VAR).append(varName.substring(1));
                } else {
                    sb.append(COLOR_CUSTOM_VAR).append(varName.substring(1));
                }
            } else if (matcher.group(2) != null) {
                sb.append(COLOR_STRING_ITEM).append(matcher.group(2));
            } else if (matcher.group(3) != null) {
                sb.append(COLOR_RADIX_LITERAL).append(matcher.group(3));
            } else if (matcher.group(4) != null) {
                splitNumberAndUnit(matcher.group(4), sb);
            } else if (matcher.group(5) != null) {
                sb.append(COLOR_NUMBER).append(matcher.group(5));
            } else if (matcher.group(6) != null) {
                String word = matcher.group(6);
                String lower = word.toLowerCase();
                if (MARKET_FUNCTIONS.contains(lower)) {
                    sb.append(COLOR_MARKET_FUNC).append(word);
                } else if (MATH_FUNCTIONS.contains(lower)) {
                    sb.append(COLOR_MATH_FUNC).append(word);
                } else if (BUILTIN_VARS.contains(lower)) {
                    sb.append(COLOR_BUILTIN_VAR).append(word);
                } else if (UNITS.contains(lower)) {
                    sb.append(COLOR_UNIT).append(word);
                } else {
                    sb.append(COLOR_STRING_ITEM).append(word);
                }
            } else if (matcher.group(7) != null) {
                String opToken = matcher.group(7);
                if (opToken.equals("(") || opToken.equals(")") || opToken.equals(",")) {
                    sb.append(COLOR_DELIM).append(opToken);
                } else {
                    sb.append(COLOR_OP).append(opToken);
                }
            } else if (matcher.group(8) != null) {
                sb.append(matcher.group(8));
            } else if (matcher.group(9) != null) {
                sb.append(COLOR_NUMBER).append(matcher.group(9));
            }
        }

        return sb.toString();
    }

    private static void splitNumberAndUnit(String numUnitStr, StringBuilder sb) {
        // Separate leading number from trailing unit (e.g. "100m", "2.5dc")
        int idx = 0;
        int len = numUnitStr.length();
        while (idx < len && (Character.isDigit(numUnitStr.charAt(idx)) || numUnitStr.charAt(idx) == '.' || Character.isWhitespace(numUnitStr.charAt(idx)))) {
            idx++;
        }
        String numPart = numUnitStr.substring(0, idx);
        String unitPart = numUnitStr.substring(idx);

        sb.append(COLOR_NUMBER).append(numPart);
        if (!unitPart.isEmpty()) {
            sb.append(COLOR_UNIT).append(unitPart);
        }
    }
}
