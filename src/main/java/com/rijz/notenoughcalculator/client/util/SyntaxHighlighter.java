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
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Live syntax highlighter for REI search bar expressions.
 * Color codes:
 * - Numbers / Values (123, 45.6, 0xFF, 0b1010): Pure Bright White (§f)
 * - Units (k, m, b, t, s, e, h, sc, dc, eb): Vibrant Cyan (§b)
 * - Functions (sqrt, bz, ah, ahbin, fmt, rad, deg, etc.): Bright Yellow (§e)
 * - Variables (ans, pi, e, $var): Bright Green (§a)
 * - Math Operators (+, -, *, x, /, ^, %, !, &, |, ~, <<, >>): Bright Gold (§6)
 * - Delimiters & Parens ((, ), ,): Light Gray (§7)
 * - Errors: Red (§c)
 * - Chat output: Green (§a)
 */
public class SyntaxHighlighter {

    public static final String COLOR_NUMBER = "§f";    
    public static final String COLOR_UNIT = "§b";     
    public static final String COLOR_FUNC = "§e";      
    public static final String COLOR_VAR = "§a";       
    public static final String COLOR_OP = "§6";        
    public static final String COLOR_DELIM = "§7";     
    public static final String COLOR_ERROR = "§c";     
    public static final String COLOR_CHAT_RESULT = "§a";

    private static final Set<String> FUNCTIONS = ExpressionEvaluator.FUNCTIONS;
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
            "(\\$\\w+)|" +                                      // Custom variable ($var)
            "(0[bxo][0-9a-fA-F_]+)|" +                          // Number literal (0b, 0x, 0o)
            "(\\d+\\.?\\d*\\s*" + buildUnitsRegex() + ")|" +    // Number with unit (ExpressionEvaluator.UNITS)
            "(\\d+\\.?\\d*)|" +                                 // Plain number
            "([a-zA-Z]+)|" +                                    // Word (function / builtin var)
            "(<<|>>|[+\\-*/^%xX!&|~(),])|" +                    // Operators / Delimiters
            "(\\s+)|" +                                         // Whitespace
            "(.)"                                               // Anything else
    );

    public static String highlight(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Matcher matcher = TOKEN_PATTERN.matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Custom variable ($var)
                sb.append(COLOR_VAR).append(matcher.group(1));
            } else if (matcher.group(2) != null) {
                // Hex / Bin / Oct literal
                sb.append(COLOR_NUMBER).append(matcher.group(2));
            } else if (matcher.group(3) != null) {
                // Number attached to unit (e.g. 100m, 2dc)
                splitNumberAndUnit(matcher.group(3), sb);
            } else if (matcher.group(4) != null) {
                // Plain number
                sb.append(COLOR_NUMBER).append(matcher.group(4));
            } else if (matcher.group(5) != null) {
                // Word
                String word = matcher.group(5);
                String lower = word.toLowerCase();
                if (FUNCTIONS.contains(lower)) {
                    sb.append(COLOR_FUNC).append(word);
                } else if (BUILTIN_VARS.contains(lower)) {
                    sb.append(COLOR_VAR).append(word);
                } else if (UNITS.contains(lower)) {
                    sb.append(COLOR_UNIT).append(word);
                } else {
                    sb.append(COLOR_NUMBER).append(word);
                }
            } else if (matcher.group(6) != null) {
                // Operator / Delimiter
                String opToken = matcher.group(6);
                if (opToken.equals("(") || opToken.equals(")") || opToken.equals(",")) {
                    sb.append(COLOR_DELIM).append(opToken);
                } else {
                    sb.append(COLOR_OP).append(opToken);
                }
            } else if (matcher.group(7) != null) {
                // Whitespace
                sb.append(matcher.group(7));
            } else if (matcher.group(8) != null) {
                // Other fallback
                sb.append(COLOR_NUMBER).append(matcher.group(8));
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
