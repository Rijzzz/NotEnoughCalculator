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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/**
 * Expression evaluator with support for:
 * - Basic math operators (+, -, *, x, /, ^, %)
 * - Functions (sqrt, abs, floor, ceil, round)
 * - Skyblock units (k, m, b, t, s, e, h, sc, dc, eb)
 * - Variables (ans, $custom)
 */
public class ExpressionEvaluator {

    // Hardcoded: store max 15 calculations in history
    private static final int MAX_HISTORY = 15;

    private final MathContext mc;
    private final Map<String, BigDecimal> variables;
    private final List<String> history;
    private BigDecimal lastAnswer;

    public ExpressionEvaluator() {
        CalculatorConfig config = CalculatorConfig.getInstance();
        // Use high precision internally to avoid rounding errors
        // This is ONLY used for division to prevent infinite decimals
        this.mc = new MathContext(Math.max(config.decimalPrecision, 50), RoundingMode.HALF_UP);
        this.variables = new HashMap<>();
        this.history = new ArrayList<>();
        this.lastAnswer = BigDecimal.ZERO;
    }

    public static class EvalException extends Exception {
        private final int position;

        public EvalException(String msg, int pos) {
            super(msg);
            this.position = pos;
        }

        public int getPosition() { return position; }
    }

    // Helper for translation
    private static String tr(String key, Object... args) {
        return I18n.translate(key, args);
    }

    // Token types recognized by the parser
    private enum TokenKind {
        NUM, OP, LPAREN, RPAREN, FUNC, VAR, UNIT, EOF
    }

    private static class Token {
        TokenKind kind;
        String value;
        BigDecimal number;
        int pos;

        Token(TokenKind k, String v, int p) {
            kind = k;
            value = v;
            pos = p;
        }
    }

    // Skyblock unit multipliers
    private static final Map<String, BigDecimal> UNITS;
    static {
        Map<String, BigDecimal> units = new HashMap<>();
        units.put("k", new BigDecimal("1000"));
        units.put("m", new BigDecimal("1000000"));
        units.put("b", new BigDecimal("1000000000"));
        units.put("t", new BigDecimal("1000000000000"));
        units.put("s", new BigDecimal("64"));       // Stack
        units.put("e", new BigDecimal("160"));      // Enchanted
        units.put("h", new BigDecimal("1728"));     // Shulker (27*64)
        units.put("sc", new BigDecimal("1728"));    // Small chest
        units.put("dc", new BigDecimal("3456"));    // Double chest
        units.put("eb", new BigDecimal("2880"));    // Ender chest (45*64)
        UNITS = Collections.unmodifiableMap(units);
    }

    // Supported math functions
    private static final Set<String> FUNCTIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("sqrt", "abs", "floor", "ceil", "round"))
    );

    /**
     * Evaluate without adding to history (for live display).
     */
    public BigDecimal evaluateQuiet(String expr) throws EvalException {
        if (expr == null || expr.trim().isEmpty()) {
            throw new EvalException(tr("notenoughcalculator.error.empty_expression"), 0);
        }

        List<Token> tokens = tokenize(expr);
        BigDecimal result = parseExpression(tokens, 0).value;

        // Update lastAnswer but don't add to history
        lastAnswer = result;

        return result;
    }

    /**
     * Evaluate and add to history (for /calc command).
     */
    public BigDecimal evaluate(String expr) throws EvalException {
        if (expr == null || expr.trim().isEmpty()) {
            throw new EvalException(tr("notenoughcalculator.error.empty_expression"), 0);
        }

        List<Token> tokens = tokenize(expr);
        BigDecimal result = parseExpression(tokens, 0).value;

        // Update lastAnswer and add to history
        lastAnswer = result;

        // Only add if different from last entry
        if (history.isEmpty() || !history.get(history.size() - 1).equals(expr)) {
            history.add(expr);
            // Keep only last 15 calculations (hardcoded)
            while (history.size() > MAX_HISTORY) {
                history.remove(0);
            }
        }

        return result;
    }

    /**
     * Break expression string into tokens.
     */
    private List<Token> tokenize(String expr) throws EvalException {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < expr.length()) {
            char c = expr.charAt(i);

            // Skip whitespace
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Parse numbers (including decimals)
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
                    throw new EvalException(tr("notenoughcalculator.error.invalid_number"), start);
                }

                Token tok = new Token(TokenKind.NUM, numStr, start);
                try {
                    tok.number = new BigDecimal(numStr);
                } catch (NumberFormatException e) {
                    throw new EvalException(tr("notenoughcalculator.error.invalid_number"), start);
                }
                tokens.add(tok);
                continue;
            }

            // Operators
            if ("+-*/^%".indexOf(c) != -1) {
                tokens.add(new Token(TokenKind.OP, String.valueOf(c), i));
                i++;
                continue;
            }

            // Handle 'x' or 'X' as multiplication
            if (c == 'x' || c == 'X') {
                boolean isMultiplication = false;

                // Check if 'x' should be treated as multiplication
                if (i > 0) {
                    // If preceded by a digit, it's multiplication (e.g., "10x5")
                    char prevChar = expr.charAt(i - 1);
                    if (Character.isDigit(prevChar)) {
                        isMultiplication = true;
                    }
                    // If preceded by ')', it's multiplication (e.g., "(5+3)x2")
                    else if (prevChar == ')') {
                        isMultiplication = true;
                    }
                    // If we just parsed a unit token, it's multiplication (e.g., "10kx50k")
                    else if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).kind == TokenKind.UNIT) {
                        isMultiplication = true;
                    }
                    // Don't treat as multiplication if preceded by '0' (hex like 0x123)
                    else if (prevChar == '0') {
                        isMultiplication = false;
                    }
                }

                if (isMultiplication) {
                    tokens.add(new Token(TokenKind.OP, "*", i));
                    i++;
                    continue;
                }
                // Otherwise, 'x' will be parsed as a variable/identifier below
            }

            // Parentheses
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

            // Variables, functions, or units
            if (c == '$' || Character.isLetter(c)) {
                int start = i;
                StringBuilder name = new StringBuilder();

                if (c == '$') {
                    i++; // Skip $
                }

                while (i < expr.length() && (Character.isLetterOrDigit(expr.charAt(i)) || expr.charAt(i) == '_')) {
                    char current = expr.charAt(i);

                    // Special handling: if we hit 'x' or 'X' after reading at least one character,
                    // check if it should be multiplication instead of part of the variable name
                    // This makes "10bx50k" work the same as "10b*50k"
                    if ((current == 'x' || current == 'X') && name.length() > 0) {
                        // If we started with '$', this is definitely a variable, so include the 'x'
                        // Example: "$myxvar" should include the x
                        if (c == '$') {
                            name.append(current);
                            i++;
                            continue;
                        }
                        // Otherwise, we've read a unit/function name and hit 'x' - treat as multiplication
                        break;
                    }

                    name.append(current);
                    i++;
                }

                String nameStr = name.toString().toLowerCase();

                // Determine token type
                if (FUNCTIONS.contains(nameStr)) {
                    tokens.add(new Token(TokenKind.FUNC, nameStr, start));
                } else if (UNITS.containsKey(nameStr)) {
                    // Units only make sense after numbers
                    if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).kind == TokenKind.NUM) {
                        tokens.add(new Token(TokenKind.UNIT, nameStr, start));
                    } else {
                        tokens.add(new Token(TokenKind.VAR, nameStr, start));
                    }
                } else if (nameStr.equals("ans")) {
                    // Special variable for last answer
                    Token tok = new Token(TokenKind.NUM, "ans", start);
                    tok.number = lastAnswer;
                    tokens.add(tok);
                } else {
                    tokens.add(new Token(TokenKind.VAR, nameStr, start));
                }
                continue;
            }

            throw new EvalException(tr("notenoughcalculator.error.unexpected_character", c), i);
        }

        tokens.add(new Token(TokenKind.EOF, "", expr.length()));
        return tokens;
    }

    // Helper class for parser results
    private static class ParseResult {
        BigDecimal value;
        int nextPos;

        ParseResult(BigDecimal v, int p) {
            value = v;
            nextPos = p;
        }
    }

    private ParseResult parseExpression(List<Token> tokens, int pos) throws EvalException {
        return parseAddSub(tokens, pos);
    }

    // Addition and subtraction (lowest precedence)
    private ParseResult parseAddSub(List<Token> tokens, int pos) throws EvalException {
        ParseResult left = parseMulDiv(tokens, pos);

        while (left.nextPos < tokens.size()) {
            Token tok = tokens.get(left.nextPos);
            if (tok.kind != TokenKind.OP || (!tok.value.equals("+") && !tok.value.equals("-"))) {
                break;
            }

            String op = tok.value;

            if (left.nextPos + 1 >= tokens.size() || tokens.get(left.nextPos + 1).kind == TokenKind.EOF) {
                throw new EvalException(tr("notenoughcalculator.error.unfinished_expression"), tok.pos);
            }

            ParseResult right = parseMulDiv(tokens, left.nextPos + 1);

            // Use unlimited precision for add/subtract
            if (op.equals("+")) {
                left = new ParseResult(left.value.add(right.value), right.nextPos);
            } else {
                left = new ParseResult(left.value.subtract(right.value), right.nextPos);
            }
        }

        return left;
    }

    // Multiplication, division, modulo
    private ParseResult parseMulDiv(List<Token> tokens, int pos) throws EvalException {
        ParseResult left = parsePower(tokens, pos);

        while (left.nextPos < tokens.size()) {
            Token tok = tokens.get(left.nextPos);
            if (tok.kind != TokenKind.OP || (!tok.value.equals("*") && !tok.value.equals("/") && !tok.value.equals("%"))) {
                break;
            }

            String op = tok.value;

            if (left.nextPos + 1 >= tokens.size() || tokens.get(left.nextPos + 1).kind == TokenKind.EOF) {
                throw new EvalException(tr("notenoughcalculator.error.unfinished_expression"), tok.pos);
            }

            ParseResult right = parsePower(tokens, left.nextPos + 1);

            if (op.equals("*")) {
                left = new ParseResult(left.value.multiply(right.value), right.nextPos);
            } else if (op.equals("/")) {
                if (right.value.compareTo(BigDecimal.ZERO) == 0) {
                    throw new EvalException(tr("notenoughcalculator.error.division_by_zero"), tok.pos);
                }
                // Only use MathContext for division
                left = new ParseResult(left.value.divide(right.value, mc).stripTrailingZeros(), right.nextPos);
            } else { // modulo
                if (right.value.compareTo(BigDecimal.ZERO) == 0) {
                    throw new EvalException(tr("notenoughcalculator.error.modulo_by_zero"), tok.pos);
                }
                left = new ParseResult(left.value.remainder(right.value), right.nextPos);
            }
        }

        return left;
    }

    // Exponentiation (highest precedence)
    private ParseResult parsePower(List<Token> tokens, int pos) throws EvalException {
        ParseResult left = parseUnary(tokens, pos);

        if (left.nextPos < tokens.size()) {
            Token tok = tokens.get(left.nextPos);
            if (tok.kind == TokenKind.OP && tok.value.equals("^")) {
                if (left.nextPos + 1 >= tokens.size() || tokens.get(left.nextPos + 1).kind == TokenKind.EOF) {
                    throw new EvalException(tr("notenoughcalculator.error.unfinished_expression"), tok.pos);
                }

                ParseResult right = parsePower(tokens, left.nextPos + 1);

                // Don't allow crazy huge exponents
                if (right.value.abs().compareTo(new BigDecimal("1000")) > 0) {
                    throw new EvalException(tr("notenoughcalculator.error.exponent_too_large"), tok.pos);
                }

                // Can't do negative^decimal
                if (left.value.compareTo(BigDecimal.ZERO) < 0 && !isInteger(right.value)) {
                    throw new EvalException(tr("notenoughcalculator.error.negative_power"), tok.pos);
                }

                try {
                    int exp = right.value.intValueExact();
                    BigDecimal result = left.value.pow(exp, mc);
                    left = new ParseResult(result, right.nextPos);
                } catch (ArithmeticException e) {
                    throw new EvalException(tr("notenoughcalculator.error.negative_power"), tok.pos);
                }
            }
        }

        return left;
    }

    // Unary operators (negative signs)
    private ParseResult parseUnary(List<Token> tokens, int pos) throws EvalException {
        if (pos >= tokens.size()) {
            throw new EvalException(tr("notenoughcalculator.error.unexpected_end"), pos);
        }

        Token tok = tokens.get(pos);

        if (tok.kind == TokenKind.OP && tok.value.equals("-")) {
            ParseResult result = parseUnary(tokens, pos + 1);
            return new ParseResult(result.value.negate(), result.nextPos);
        }

        if (tok.kind == TokenKind.OP && tok.value.equals("+")) {
            return parseUnary(tokens, pos + 1);
        }

        return parsePostfix(tokens, pos);
    }

    // Unit suffixes (like "100m")
    private ParseResult parsePostfix(List<Token> tokens, int pos) throws EvalException {
        ParseResult result = parsePrimary(tokens, pos);

        if (result.nextPos < tokens.size()) {
            Token tok = tokens.get(result.nextPos);
            if (tok.kind == TokenKind.UNIT) {
                BigDecimal multiplier = UNITS.get(tok.value);
                result = new ParseResult(result.value.multiply(multiplier), result.nextPos + 1);
            }
        }

        return result;
    }

    // Primary expressions (numbers, variables, functions, parentheses)
    private ParseResult parsePrimary(List<Token> tokens, int pos) throws EvalException {
        if (pos >= tokens.size()) {
            throw new EvalException(tr("notenoughcalculator.error.unexpected_end"), pos);
        }

        Token tok = tokens.get(pos);

        if (tok.kind == TokenKind.NUM) {
            return new ParseResult(tok.number, pos + 1);
        }

        if (tok.kind == TokenKind.VAR) {
            if (!variables.containsKey(tok.value)) {
                throw new EvalException(tr("notenoughcalculator.error.undefined_variable", tok.value), tok.pos);
            }
            return new ParseResult(variables.get(tok.value), pos + 1);
        }

        if (tok.kind == TokenKind.FUNC) {
            if (pos + 1 >= tokens.size() || tokens.get(pos + 1).kind != TokenKind.LPAREN) {
                throw new EvalException(tr("notenoughcalculator.error.expected_parenthesis", tok.value), tok.pos);
            }

            ParseResult arg = parseExpression(tokens, pos + 2);

            if (arg.nextPos >= tokens.size() || tokens.get(arg.nextPos).kind != TokenKind.RPAREN) {
                throw new EvalException(tr("notenoughcalculator.error.expected_closing_paren"), tok.pos);
            }

            BigDecimal result = applyFunction(tok.value, arg.value, tok.pos);
            return new ParseResult(result, arg.nextPos + 1);
        }

        if (tok.kind == TokenKind.LPAREN) {
            ParseResult inner = parseExpression(tokens, pos + 1);

            if (inner.nextPos >= tokens.size() || tokens.get(inner.nextPos).kind != TokenKind.RPAREN) {
                throw new EvalException(tr("notenoughcalculator.error.unmatched_parenthesis"), tok.pos);
            }

            return new ParseResult(inner.value, inner.nextPos + 1);
        }

        throw new EvalException(tr("notenoughcalculator.error.unexpected_token", tok.value), tok.pos);
    }

    // Apply math functions
    private BigDecimal applyFunction(String func, BigDecimal arg, int pos) throws EvalException {
        switch (func) {
            case "sqrt":
                if (arg.compareTo(BigDecimal.ZERO) < 0) {
                    throw new EvalException(tr("notenoughcalculator.error.negative_sqrt"), pos);
                }
                return new BigDecimal(Math.sqrt(arg.doubleValue()), mc);

            case "abs":
                return arg.abs();

            case "floor":
                return arg.setScale(0, RoundingMode.FLOOR);

            case "ceil":
                return arg.setScale(0, RoundingMode.CEILING);

            case "round":
                return arg.setScale(0, RoundingMode.HALF_UP);

            default:
                throw new EvalException(tr("notenoughcalculator.error.unknown_function", func), pos);
        }
    }

    private boolean isInteger(BigDecimal value) {
        return value.stripTrailingZeros().scale() <= 0;
    }

    public void setVariable(String name, BigDecimal value) {
        variables.put(name.toLowerCase(), value);
    }

    public void setVariable(String name, String expr) throws EvalException {
        BigDecimal value = evaluate(expr);
        variables.put(name.toLowerCase(), value);
    }

    public BigDecimal getLastAnswer() {
        return lastAnswer;
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    public void clearHistory() {
        history.clear();
    }

    public String getVariablesInfo() {
        if (variables.isEmpty()) {
            return tr("notenoughcalculator.variable.none");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(tr("notenoughcalculator.variable.list_title", variables.size())).append("\n");

        List<String> sortedKeys = new ArrayList<>(variables.keySet());
        Collections.sort(sortedKeys);

        for (String key : sortedKeys) {
            sb.append("  $").append(key).append(" = ")
                    .append(ResultFormatter.formatWithCommas(variables.get(key))).append("\n");
        }
        return sb.toString().trim();
    }
}