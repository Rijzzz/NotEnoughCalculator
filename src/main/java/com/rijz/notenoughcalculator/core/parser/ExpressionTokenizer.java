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

import com.rijz.notenoughcalculator.core.ExpressionEvaluator;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator.EvalException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class ExpressionTokenizer {

    public static List<Token> tokenize(String expr, BigDecimal lastAnswer) throws EvalException {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < expr.length()) {
            char c = expr.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // String literals in double or single quotes: "SUPERBOOM_TNT" or 'COBBLESTONE'
            if (c == '"' || c == '\'') {
                char quoteChar = c;
                int start = i;
                i++;
                StringBuilder str = new StringBuilder();
                while (i < expr.length() && expr.charAt(i) != quoteChar) {
                    str.append(expr.charAt(i));
                    i++;
                }
                if (i < expr.length() && expr.charAt(i) == quoteChar) {
                    i++;
                }
                tokens.add(new Token(TokenKind.VAR, str.toString(), start));
                continue;
            }

            // Radix literals: 0b (binary), 0x (hexadecimal), 0o (octal)
            if (c == '0' && i + 1 < expr.length()) {
                char nextChar = expr.charAt(i + 1);
                int radix = 0;
                if (nextChar == 'b' || nextChar == 'B') radix = 2;
                else if (nextChar == 'x' || nextChar == 'X') radix = 16;
                else if (nextChar == 'o' || nextChar == 'O') radix = 8;

                if (radix > 0) {
                    int start = i;
                    i += 2;
                    StringBuilder rawNum = new StringBuilder();
                    while (i < expr.length()) {
                        char rc = expr.charAt(i);
                        if (rc == '_') {
                            i++;
                            continue;
                        }
                        boolean validDigit = (radix == 2 && (rc == '0' || rc == '1'))
                                || (radix == 8 && rc >= '0' && rc <= '7')
                                || (radix == 16 && Character.digit(rc, 16) != -1);
                        if (validDigit) {
                            rawNum.append(rc);
                            i++;
                        } else {
                            break;
                        }
                    }

                    String rawVal = rawNum.toString();
                    BigDecimal parsedVal = BigDecimal.ZERO;
                    if (!rawVal.isEmpty()) {
                        try {
                            BigInteger bigInt = new BigInteger(rawVal, radix);
                            parsedVal = new BigDecimal(bigInt);
                        } catch (NumberFormatException e) {
                            throw new EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.invalid_number"), start);
                        }
                    }

                    tokens.add(new Token(TokenKind.NUM, "0" + nextChar + rawVal, start, parsedVal));
                    continue;
                }
            }

            // Standard numbers & decimals
            if (Character.isDigit(c) || c == '.') {
                int start = i;
                StringBuilder num = new StringBuilder();
                boolean hasDot = false;

                while (i < expr.length()) {
                    c = expr.charAt(i);
                    if (Character.isDigit(c)) {
                        num.append(c);
                        i++;
                    } else if (c == '.' && !hasDot) {
                        num.append(c);
                        hasDot = true;
                        i++;
                    } else {
                        break;
                    }
                }

                String numStr = num.toString();
                if (numStr.equals(".") || numStr.isEmpty()) {
                    throw new EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.invalid_number"), start);
                }

                Token tok;
                try {
                    tok = new Token(TokenKind.NUM, numStr, start, new BigDecimal(numStr));
                } catch (NumberFormatException e) {
                    throw new EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.invalid_number"), start);
                }
                tokens.add(tok);
                continue;
            }

            // Comma separator
            if (c == ',') {
                tokens.add(new Token(TokenKind.COMMA, ",", i));
                i++;
                continue;
            }

            // Percentage vs modulo disambiguation
            if (c == '%') {
                boolean isPercentage = false;
                if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).kind == TokenKind.NUM) {
                    Token prevNum = tokens.get(tokens.size() - 1);
                    int numEndPos = prevNum.pos + prevNum.value.length();
                    if (numEndPos == i) {
                        isPercentage = true;
                    }
                }
                tokens.add(new Token(isPercentage ? TokenKind.PERCENT : TokenKind.OP, "%", i));
                i++;
                continue;
            }

            if (c == '=') {
                tokens.add(new Token(TokenKind.ASSIGN, "=", i));
                i++;
                continue;
            }

            if (c == '!') {
                tokens.add(new Token(TokenKind.FACTORIAL, "!", i));
                i++;
                continue;
            }

            if (c == '&' || c == '|' || c == '~') {
                tokens.add(new Token(TokenKind.OP, String.valueOf(c), i));
                i++;
                continue;
            }

            if (c == '<' && i + 1 < expr.length() && expr.charAt(i + 1) == '<') {
                tokens.add(new Token(TokenKind.OP, "<<", i));
                i += 2;
                continue;
            }

            if (c == '>' && i + 1 < expr.length() && expr.charAt(i + 1) == '>') {
                tokens.add(new Token(TokenKind.OP, ">>", i));
                i += 2;
                continue;
            }

            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^') {
                tokens.add(new Token(TokenKind.OP, String.valueOf(c), i));
                i++;
                continue;
            }

            if (c == '(') {
                tokens.add(new Token(TokenKind.LPAREN, "(", i));
                i++;
                continue;
            }

            if (c == ')') {
                tokens.add(new Token(TokenKind.RPAREN, ")", i));
                i++;
                continue;
            }

            // Handle 'x' or 'X' as multiplication
            if (c == 'x' || c == 'X') {
                boolean isMultiplication = false;

                if (i > 0) {
                    char prevChar = expr.charAt(i - 1);
                    if (Character.isDigit(prevChar) || prevChar == ')') {
                        isMultiplication = true;
                    } else if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).kind == TokenKind.UNIT) {
                        isMultiplication = true;
                    }
                }

                if (isMultiplication) {
                    tokens.add(new Token(TokenKind.OP, "*", i));
                    i++;
                    continue;
                }
            }

            if (c == '$' || Character.isLetter(c) || c == '_') {
                int start = i;
                StringBuilder name = new StringBuilder();
                name.append(c);
                i++;

                while (i < expr.length()) {
                    char current = expr.charAt(i);
                    if (!Character.isLetterOrDigit(current) && current != '_') {
                        break;
                    }

                    if ((current == 'x' || current == 'X') && name.length() > 0) {
                        if (c == '$') {
                            name.append(current);
                            i++;
                            continue;
                        }
                        if (ExpressionEvaluator.UNITS.containsKey(name.toString().toLowerCase())) {
                            break;
                        }
                    }

                    name.append(current);
                    i++;
                }

                String nameStr = name.toString().toLowerCase();

                if (ExpressionEvaluator.FUNCTIONS.contains(nameStr)) {
                    tokens.add(new Token(TokenKind.FUNC, nameStr, start));
                } else if (nameStr.equals("pi") || nameStr.equals("$pi")) {
                    tokens.add(new Token(TokenKind.NUM, nameStr, start, new BigDecimal("3.14159265358979323846")));
                } else if (nameStr.equals("e") || nameStr.equals("$e")) {
                    if (!nameStr.startsWith("$") && !tokens.isEmpty() && tokens.get(tokens.size() - 1).kind == TokenKind.NUM) {
                        tokens.add(new Token(TokenKind.UNIT, nameStr, start));
                    } else {
                        tokens.add(new Token(TokenKind.NUM, nameStr, start, new BigDecimal("2.71828182845904523536")));
                    }
                } else if (ExpressionEvaluator.UNITS.containsKey(nameStr)) {
                    if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).kind == TokenKind.NUM) {
                        tokens.add(new Token(TokenKind.UNIT, nameStr, start));
                    } else {
                        tokens.add(new Token(TokenKind.VAR, nameStr, start));
                    }
                } else if (nameStr.equals("ans") || nameStr.equals("$ans")) {
                    tokens.add(new Token(TokenKind.NUM, nameStr, start, lastAnswer != null ? lastAnswer : BigDecimal.ZERO));
                } else {
                    tokens.add(new Token(TokenKind.VAR, nameStr, start));
                }
                continue;
            }

            throw new EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.unexpected_character", c), i);
        }

        tokens.add(new Token(TokenKind.EOF, "", expr.length()));
        return insertImplicitMultiplication(tokens);
    }

    private static List<Token> insertImplicitMultiplication(List<Token> tokens) {
        List<Token> result = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            result.add(tokens.get(i));
            if (i + 1 < tokens.size()) {
                Token cur = tokens.get(i);
                Token next = tokens.get(i + 1);
                boolean insert = false;

                if (cur.kind == TokenKind.NUM && next.kind == TokenKind.LPAREN) insert = true;
                if (cur.kind == TokenKind.NUM && next.kind == TokenKind.FUNC) insert = true;
                if (cur.kind == TokenKind.RPAREN && next.kind == TokenKind.NUM) insert = true;
                if (cur.kind == TokenKind.RPAREN && next.kind == TokenKind.LPAREN) insert = true;
                if (cur.kind == TokenKind.RPAREN && next.kind == TokenKind.FUNC) insert = true;
                if (cur.kind == TokenKind.UNIT && next.kind == TokenKind.LPAREN) insert = true;
                if (cur.kind == TokenKind.PERCENT && (next.kind == TokenKind.NUM || next.kind == TokenKind.LPAREN || next.kind == TokenKind.FUNC)) insert = true;

                if (insert) {
                    result.add(new Token(TokenKind.OP, "*", cur.pos));
                }
            }
        }
        return result;
    }
}
