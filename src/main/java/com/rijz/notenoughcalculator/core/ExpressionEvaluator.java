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
import com.rijz.notenoughcalculator.core.skyblock.SkyblockTaxCalculator;
import net.minecraft.client.resources.language.I18n;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Expression evaluator with support for:
 * - Basic math operators (+, -, *, x, /, ^, %)
 * - Functions (sqrt, abs, floor, ceil, round, log, ln, sin, cos, tan, min, max)
 * - Smart percentage (10% = 0.1, 100 + 10% = 110)
 * - Implicit multiplication (2(3+4), (3)(4))
 * - Skyblock units (k, m, b, t, s, e, h, sc, dc, eb)
 * - Variables (ans, $custom)
 * - Literals (0b, 0x, 0o)
 */
public class ExpressionEvaluator {


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

    // Package-private constructor for unit tests (no Minecraft dependencies needed)
    ExpressionEvaluator(int precision) {
        this.mc = new MathContext(Math.max(precision, 50), RoundingMode.HALF_UP);
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

    // Helper for translation (falls back to key + args if I18n is unavailable in tests)
    private static String tr(String key, Object... args) {
        try {
            return I18n.get(key, args);
        } catch (Exception | NoClassDefFoundError e) {
            // Outside Minecraft (unit tests) - return key with formatted args
            if (args.length > 0) {
                return String.format(key.replace("%s", "%s").replace("%d", "%s"), args);
            }
            return key;
        }
    }


    private enum TokenKind {
        NUM, OP, LPAREN, RPAREN, COMMA, PERCENT, FACTORIAL, FUNC, VAR, UNIT, EOF
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
    public static final Map<String, BigDecimal> UNITS = Map.of(
            "k",  new BigDecimal("1000"),
            "m",  new BigDecimal("1000000"),
            "b",  new BigDecimal("1000000000"),
            "t",  new BigDecimal("1000000000000"),
            "s",  new BigDecimal("64"),       // Stack
            "e",  new BigDecimal("160"),      // Enchanted
            "h",  new BigDecimal("1728"),     // Shulker (27*64)
            "sc", new BigDecimal("1728"),    // Small chest
            "dc", new BigDecimal("3456"),    // Double chest
            "eb", new BigDecimal("2880")     // Ender chest (45*64)
    );

    public enum RadixMode {
        DEFAULT, HEX, BIN, OCT, SHORTHAND
    }

    public static class EvalResult {
        public final BigDecimal value;
        public final RadixMode radixMode;

        public EvalResult(BigDecimal value, RadixMode radixMode) {
            this.value = value;
            this.radixMode = radixMode != null ? radixMode : RadixMode.DEFAULT;
        }

        public EvalResult(BigDecimal value) {
            this(value, RadixMode.DEFAULT);
        }
    }

    // Supported math functions
    public static final Set<String> FUNCTIONS = Set.of(
            "sqrt", "abs", "floor", "ceil", "round",
            "log", "ln", "sin", "cos", "tan",
            "min", "max", "hex", "bin", "oct",
            "pct", "gcd", "lcm", "clamp", "avg", "xor",
            "bz", "ah", "ahbin", "fmt", "rad", "deg"
    );


    public static final Set<String> BUILTIN_VARIABLES = Set.of("ans", "pi", "e");

    // Functions that take two comma-separated arguments
    private static final Set<String> MULTI_ARG_FUNCTIONS = Set.of("min", "max", "pct", "gcd", "lcm", "xor");

    /**
     * Evaluate without adding to history (for live display).
     */
    public EvalResult evaluateQuietResult(String expr) throws EvalException {
        if (expr == null || expr.trim().isEmpty()) {
            throw new EvalException(tr("notenoughcalculator.error.empty_expression"), 0);
        }

        List<Token> tokens = tokenize(expr);
        tokens = insertImplicitMultiplication(tokens);
        ParseResult parseRes = parseExpression(tokens, 0);

        if (parseRes.nextPos < tokens.size() && tokens.get(parseRes.nextPos).kind != TokenKind.EOF) {
            Token leftover = tokens.get(parseRes.nextPos);
            throw new EvalException(tr("notenoughcalculator.error.unexpected_token", leftover.value), leftover.pos);
        }

        BigDecimal result = parseRes.value;

        // Update lastAnswer but don't add to history
        lastAnswer = result;

        return new EvalResult(result, parseRes.radixMode);
    }

    public BigDecimal evaluateQuiet(String expr) throws EvalException {
        return evaluateQuietResult(expr).value;
    }

    /**
     * Evaluate and add to history (for /calc command).
     */
    public EvalResult evaluateResult(String expr) throws EvalException {
        if (expr == null || expr.trim().isEmpty()) {
            throw new EvalException(tr("notenoughcalculator.error.empty_expression"), 0);
        }

        List<Token> tokens = tokenize(expr);
        tokens = insertImplicitMultiplication(tokens);
        ParseResult parseRes = parseExpression(tokens, 0);

        if (parseRes.nextPos < tokens.size() && tokens.get(parseRes.nextPos).kind != TokenKind.EOF) {
            Token leftover = tokens.get(parseRes.nextPos);
            throw new EvalException(tr("notenoughcalculator.error.unexpected_token", leftover.value), leftover.pos);
        }

        BigDecimal result = parseRes.value;

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

        return new EvalResult(result, parseRes.radixMode);
    }

    public BigDecimal evaluate(String expr) throws EvalException {
        return evaluateResult(expr).value;
    }

    /**
     * Break expression string into tokens.
     */
    private List<Token> tokenize(String expr) throws EvalException {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < expr.length()) {
            char c = expr.charAt(i);


            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }


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

            // Comma separator (for multi-arg functions like min, max)
            if (c == ',') {
                tokens.add(new Token(TokenKind.COMMA, ",", i));
                i++;
                continue;
            }

            // Percentage vs modulo disambiguation
            // Adjacent to number (no space) = percentage: 10% -> 0.1
            // Spaced from number = modulo operator: 10 % 3 -> 1
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


            if ("+-*/^".indexOf(c) != -1) {
                tokens.add(new Token(TokenKind.OP, String.valueOf(c), i));
                i++;
                continue;
            }

            parseLiteral:
            {
                if (tokens.isEmpty())
                    break parseLiteral;

                Token previous = tokens.get(tokens.size() - 1);

                // Enforce adjacency: previous "0" token must directly touch this character (no space)
                if (previous.kind != TokenKind.NUM || !previous.value.equals("0") || previous.pos + previous.value.length() != i)
                    break parseLiteral;

                char prefix = c;

                int radix;
                switch (prefix) {
                    case 'x', 'X' -> radix = 16;
                    case 'b', 'B' -> radix = 2;
                    case 'o', 'O' -> radix = 8;
                    default -> {
                        break parseLiteral;
                    }
                }

                int start = i;
                i++; // skip 'b', 'x', or 'o'

                StringBuilder num = new StringBuilder();
                boolean lastWasUnderscore = false;

                while (i < expr.length()) {
                    char ch = expr.charAt(i);

                    if (ch == '_') {
                        // Underscore must follow a digit and cannot follow another underscore
                        if (num.isEmpty() || lastWasUnderscore) {
                            break;
                        }
                        lastWasUnderscore = true;
                        i++;
                        continue;
                    }

                    int digit = Character.digit(ch, radix);
                    if (digit == -1) {
                        // If it's an out-of-range digit for this base (e.g. '2' in 0b102, '8' in 0o78), fail immediately
                        if (Character.isDigit(ch)) {
                            throw new EvalException(tr("notenoughcalculator.error.invalid_number"), i);
                        }
                        break;
                    }

                    num.append(ch);
                    lastWasUnderscore = false;
                    i++;
                }

                // If literal ended on an underscore, backtrack that underscore
                if (lastWasUnderscore) {
                    i--;
                }

                if (num.isEmpty()) {
                    i = start;
                    break parseLiteral;
                }

                String literal = num.toString();
                previous.value += prefix + literal;
                previous.number = new BigDecimal(new BigInteger(literal, radix));
                continue;
            }

            // Handle 'x' or 'X' as multiplication
            if (c == 'x' || c == 'X') {
                boolean isMultiplication = false;

                // Check if 'x' should be treated as multiplication
                if (i > 0) {
                    char prevChar = expr.charAt(i - 1);

                    // If preceded by a digit, it's multiplication (e.g., "10x5")
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
                }

                if (isMultiplication) {
                    // Normalize 'x' to '*' internally
                    tokens.add(new Token(TokenKind.OP, "*", i));
                    i++;
                    continue;
                }
                // Otherwise, 'x' will be parsed as a variable/identifier below
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
                        // Only break if what we've read so far is a valid unit (e.g., "10bx50k")
                        // This allows "max(10, 5)" to be parsed correctly as "max"
                        if (UNITS.containsKey(name.toString().toLowerCase())) {
                            break;
                        }
                    }

                    name.append(current);
                    i++;
                }

                String nameStr = name.toString().toLowerCase();

    
                if (FUNCTIONS.contains(nameStr)) {
                    tokens.add(new Token(TokenKind.FUNC, nameStr, start));
                } else if (nameStr.equals("pi")) {

                    Token tok = new Token(TokenKind.NUM, "pi", start);
                    tok.number = new BigDecimal("3.14159265358979323846");
                    tokens.add(tok);
                } else if (nameStr.equals("e")) {
                    // 'e' after a number = Skyblock enchanted unit (2e = 320)
                    // 'e' standalone or after operator = Euler's number (2.718...)
                    if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).kind == TokenKind.NUM) {
                        tokens.add(new Token(TokenKind.UNIT, nameStr, start));
                    } else {
                        Token tok = new Token(TokenKind.NUM, "e", start);
                        tok.number = new BigDecimal("2.71828182845904523536");
                        tokens.add(tok);
                    }
                } else if (UNITS.containsKey(nameStr)) {
                    // Units only make sense after numbers
                    if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).kind == TokenKind.NUM) {
                        tokens.add(new Token(TokenKind.UNIT, nameStr, start));
                    } else {
                        tokens.add(new Token(TokenKind.VAR, nameStr, start));
                    }
                } else if (nameStr.equals("ans")) {
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

    // Insert implicit multiplication tokens where multiplication is implied
    // Examples: 2(3+4) -> 2*(3+4), (3)(4) -> (3)*(4), 2sqrt(4) -> 2*sqrt(4)
    private List<Token> insertImplicitMultiplication(List<Token> tokens) {
        List<Token> result = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            result.add(tokens.get(i));
            if (i + 1 < tokens.size()) {
                Token cur = tokens.get(i);
                Token next = tokens.get(i + 1);
                boolean insert = false;

                // NUM followed by LPAREN: 2(3+4)
                if (cur.kind == TokenKind.NUM && next.kind == TokenKind.LPAREN) insert = true;
                // NUM followed by FUNC: 2sqrt(4)
                if (cur.kind == TokenKind.NUM && next.kind == TokenKind.FUNC) insert = true;
                // RPAREN followed by NUM: (3+4)2
                if (cur.kind == TokenKind.RPAREN && next.kind == TokenKind.NUM) insert = true;
                // RPAREN followed by LPAREN: (3)(4)
                if (cur.kind == TokenKind.RPAREN && next.kind == TokenKind.LPAREN) insert = true;
                // RPAREN followed by FUNC: (3)sqrt(4)
                if (cur.kind == TokenKind.RPAREN && next.kind == TokenKind.FUNC) insert = true;
                // UNIT followed by LPAREN: 10k(5)
                if (cur.kind == TokenKind.UNIT && next.kind == TokenKind.LPAREN) insert = true;
                // PERCENT followed by NUM/LPAREN/FUNC: 10%(5+3) -> 0.1*(5+3)
                if (cur.kind == TokenKind.PERCENT && (next.kind == TokenKind.NUM || next.kind == TokenKind.LPAREN || next.kind == TokenKind.FUNC)) insert = true;

                if (insert) {
                    result.add(new Token(TokenKind.OP, "*", cur.pos));
                }
            }
        }
        return result;
    }


    private static class ParseResult {
        BigDecimal value;
        int nextPos;
        boolean isPercentage;
        RadixMode radixMode;

        ParseResult(BigDecimal v, int p) {
            this(v, p, RadixMode.DEFAULT);
        }

        ParseResult(BigDecimal v, int p, RadixMode radixMode) {
            value = v;
            nextPos = p;
            isPercentage = false;
            this.radixMode = radixMode != null ? radixMode : RadixMode.DEFAULT;
        }
    }

    private ParseResult parseExpression(List<Token> tokens, int pos) throws EvalException {
        return parseBitwiseOr(tokens, pos);
    }

    // Bitwise OR (lowest precedence)
    private ParseResult parseBitwiseOr(List<Token> tokens, int pos) throws EvalException {
        ParseResult left = parseBitwiseAnd(tokens, pos);

        while (left.nextPos < tokens.size()) {
            Token tok = tokens.get(left.nextPos);
            if (tok.kind != TokenKind.OP || !tok.value.equals("|")) {
                break;
            }

            if (left.nextPos + 1 >= tokens.size() || tokens.get(left.nextPos + 1).kind == TokenKind.EOF) {
                throw new EvalException(tr("notenoughcalculator.error.unfinished_expression"), tok.pos);
            }

            ParseResult right = parseBitwiseAnd(tokens, left.nextPos + 1);
            BigInteger b1 = left.value.toBigInteger();
            BigInteger b2 = right.value.toBigInteger();
            RadixMode mode = left.radixMode != RadixMode.DEFAULT ? left.radixMode : right.radixMode;
            left = new ParseResult(new BigDecimal(b1.or(b2)), right.nextPos, mode);
        }

        return left;
    }

    // Bitwise AND
    private ParseResult parseBitwiseAnd(List<Token> tokens, int pos) throws EvalException {
        ParseResult left = parseShift(tokens, pos);

        while (left.nextPos < tokens.size()) {
            Token tok = tokens.get(left.nextPos);
            if (tok.kind != TokenKind.OP || !tok.value.equals("&")) {
                break;
            }

            if (left.nextPos + 1 >= tokens.size() || tokens.get(left.nextPos + 1).kind == TokenKind.EOF) {
                throw new EvalException(tr("notenoughcalculator.error.unfinished_expression"), tok.pos);
            }

            ParseResult right = parseShift(tokens, left.nextPos + 1);
            BigInteger b1 = left.value.toBigInteger();
            BigInteger b2 = right.value.toBigInteger();
            RadixMode mode = left.radixMode != RadixMode.DEFAULT ? left.radixMode : right.radixMode;
            left = new ParseResult(new BigDecimal(b1.and(b2)), right.nextPos, mode);
        }

        return left;
    }

    // Bitwise Shifts (<<, >>)
    private ParseResult parseShift(List<Token> tokens, int pos) throws EvalException {
        ParseResult left = parseAddSub(tokens, pos);

        while (left.nextPos < tokens.size()) {
            Token tok = tokens.get(left.nextPos);
            if (tok.kind != TokenKind.OP || (!tok.value.equals("<<") && !tok.value.equals(">>"))) {
                break;
            }

            String op = tok.value;

            if (left.nextPos + 1 >= tokens.size() || tokens.get(left.nextPos + 1).kind == TokenKind.EOF) {
                throw new EvalException(tr("notenoughcalculator.error.unfinished_expression"), tok.pos);
            }

            ParseResult right = parseAddSub(tokens, left.nextPos + 1);
            BigInteger b1 = left.value.toBigInteger();
            int shiftAmount;
            try {
                shiftAmount = right.value.intValueExact();
            } catch (ArithmeticException e) {
                throw new EvalException(tr("notenoughcalculator.error.exponent_too_large"), tok.pos);
            }

            BigInteger res = op.equals("<<") ? b1.shiftLeft(shiftAmount) : b1.shiftRight(shiftAmount);
            RadixMode mode = left.radixMode != RadixMode.DEFAULT ? left.radixMode : right.radixMode;
            left = new ParseResult(new BigDecimal(res), right.nextPos, mode);
        }

        return left;
    }

    // Addition and subtraction (lowest math precedence)
    // Supports smart percentage: 100 + 10% = 110, 200 - 25% = 150
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
            RadixMode mode = left.radixMode != RadixMode.DEFAULT ? left.radixMode : right.radixMode;

            if (right.isPercentage) {
                // Smart percentage: 100 + 10% means "add 10% of 100"
                BigDecimal percentOfLeft = left.value.multiply(right.value);
                if (op.equals("+")) {
                    left = new ParseResult(left.value.add(percentOfLeft), right.nextPos, mode);
                } else {
                    left = new ParseResult(left.value.subtract(percentOfLeft), right.nextPos, mode);
                }
            } else {
                // Normal add/subtract
                if (op.equals("+")) {
                    left = new ParseResult(left.value.add(right.value), right.nextPos, mode);
                } else {
                    left = new ParseResult(left.value.subtract(right.value), right.nextPos, mode);
                }
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
            RadixMode mode = left.radixMode != RadixMode.DEFAULT ? left.radixMode : right.radixMode;

            if (op.equals("*")) {
                left = new ParseResult(left.value.multiply(right.value), right.nextPos, mode);
            } else if (op.equals("/")) {
                if (right.value.compareTo(BigDecimal.ZERO) == 0) {
                    throw new EvalException(tr("notenoughcalculator.error.division_by_zero"), tok.pos);
                }
                // Only use MathContext for division
                left = new ParseResult(left.value.divide(right.value, mc).stripTrailingZeros(), right.nextPos, mode);
            } else { // modulo
                if (right.value.compareTo(BigDecimal.ZERO) == 0) {
                    throw new EvalException(tr("notenoughcalculator.error.modulo_by_zero"), tok.pos);
                }
                left = new ParseResult(left.value.remainder(right.value), right.nextPos, mode);
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
                RadixMode mode = left.radixMode != RadixMode.DEFAULT ? left.radixMode : right.radixMode;

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
                    left = new ParseResult(result, right.nextPos, mode);
                } catch (ArithmeticException e) {
                    throw new EvalException(tr("notenoughcalculator.error.negative_power"), tok.pos);
                }
            }
        }

        return left;
    }

    // Unary operators (negative signs, bitwise NOT ~)
    private ParseResult parseUnary(List<Token> tokens, int pos) throws EvalException {
        if (pos >= tokens.size()) {
            throw new EvalException(tr("notenoughcalculator.error.unexpected_end"), pos);
        }

        Token tok = tokens.get(pos);

        if (tok.kind == TokenKind.OP && tok.value.equals("-")) {
            ParseResult result = parseUnary(tokens, pos + 1);
            return new ParseResult(result.value.negate(), result.nextPos, result.radixMode);
        }

        if (tok.kind == TokenKind.OP && tok.value.equals("+")) {
            return parseUnary(tokens, pos + 1);
        }

        if (tok.kind == TokenKind.OP && tok.value.equals("~")) {
            ParseResult result = parseUnary(tokens, pos + 1);
            BigInteger bi = result.value.toBigInteger();
            return new ParseResult(new BigDecimal(bi.not()), result.nextPos, result.radixMode);
        }

        return parsePostfix(tokens, pos);
    }

    // Unit suffixes (like "100m") and percentage postfix (like "10%")
    private ParseResult parsePostfix(List<Token> tokens, int pos) throws EvalException {
        ParseResult result = parsePrimary(tokens, pos);

        // Handle unit suffixes
        if (result.nextPos < tokens.size()) {
            Token tok = tokens.get(result.nextPos);
            if (tok.kind == TokenKind.UNIT) {
                BigDecimal multiplier = UNITS.get(tok.value);
                result = new ParseResult(result.value.multiply(multiplier), result.nextPos + 1, result.radixMode);
            }
        }

        // Handle percentage postfix: 10% = 0.1
        if (result.nextPos < tokens.size()) {
            Token tok = tokens.get(result.nextPos);
            if (tok.kind == TokenKind.PERCENT) {
                BigDecimal percentValue = result.value.divide(BigDecimal.valueOf(100), mc);
                result = new ParseResult(percentValue, result.nextPos + 1, result.radixMode);
                result.isPercentage = true;
            }
        }

        // Handle factorial postfix: 5! = 120
        if (result.nextPos < tokens.size()) {
            Token tok = tokens.get(result.nextPos);
            if (tok.kind == TokenKind.FACTORIAL) {
                if (result.value.compareTo(BigDecimal.ZERO) < 0 || !isInteger(result.value)) {
                    throw new EvalException(tr("notenoughcalculator.error.factorial_non_integer"), tok.pos);
                }
                int n;
                try {
                    n = result.value.intValueExact();
                } catch (ArithmeticException e) {
                    throw new EvalException(tr("notenoughcalculator.error.factorial_too_large"), tok.pos);
                }
                if (n > 1000) {
                    throw new EvalException(tr("notenoughcalculator.error.factorial_too_large"), tok.pos);
                }
                BigDecimal factResult = BigDecimal.ONE;
                for (int i = 2; i <= n; i++) {
                    factResult = factResult.multiply(BigDecimal.valueOf(i));
                }
                result = new ParseResult(factResult, result.nextPos + 1, result.radixMode);
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

            // Variadic avg(a, b, c, ...) function
            if (tok.value.equals("avg")) {
                int curPos = pos + 2;
                List<BigDecimal> args = new ArrayList<>();
                RadixMode mode = RadixMode.DEFAULT;

                while (curPos < tokens.size()) {
                    ParseResult arg = parseExpression(tokens, curPos);
                    args.add(arg.value);
                    if (arg.radixMode != RadixMode.DEFAULT) mode = arg.radixMode;
                    curPos = arg.nextPos;

                    if (curPos < tokens.size() && tokens.get(curPos).kind == TokenKind.COMMA) {
                        curPos++;
                    } else {
                        break;
                    }
                }

                if (curPos >= tokens.size() || tokens.get(curPos).kind != TokenKind.RPAREN) {
                    throw new EvalException(tr("notenoughcalculator.error.expected_closing_paren"), tok.pos);
                }

                if (args.isEmpty()) {
                    throw new EvalException(tr("notenoughcalculator.error.empty_expression"), tok.pos);
                }

                BigDecimal sum = BigDecimal.ZERO;
                for (BigDecimal a : args) {
                    sum = sum.add(a);
                }
                BigDecimal avgResult = sum.divide(BigDecimal.valueOf(args.size()), mc).stripTrailingZeros();
                return new ParseResult(avgResult, curPos + 1, mode);
            }

            // 3-argument clamp(val, min, max) function
            if (tok.value.equals("clamp")) {
                ParseResult arg1 = parseExpression(tokens, pos + 2);
                if (arg1.nextPos >= tokens.size() || tokens.get(arg1.nextPos).kind != TokenKind.COMMA) {
                    throw new EvalException(tr("notenoughcalculator.error.expected_comma", tok.value), tok.pos);
                }
                ParseResult arg2 = parseExpression(tokens, arg1.nextPos + 1);
                if (arg2.nextPos >= tokens.size() || tokens.get(arg2.nextPos).kind != TokenKind.COMMA) {
                    throw new EvalException(tr("notenoughcalculator.error.expected_comma", tok.value), tok.pos);
                }
                ParseResult arg3 = parseExpression(tokens, arg2.nextPos + 1);
                if (arg3.nextPos >= tokens.size() || tokens.get(arg3.nextPos).kind != TokenKind.RPAREN) {
                    throw new EvalException(tr("notenoughcalculator.error.expected_closing_paren"), tok.pos);
                }
                BigDecimal val = arg1.value;
                BigDecimal min = arg2.value;
                BigDecimal max = arg3.value;
                BigDecimal result = val.compareTo(min) < 0 ? min : (val.compareTo(max) > 0 ? max : val);
                return new ParseResult(result, arg3.nextPos + 1, arg1.radixMode);
            }

            // Multi-argument functions like min, max, pct, gcd, lcm, xor
            if (MULTI_ARG_FUNCTIONS.contains(tok.value)) {
                ParseResult arg1 = parseExpression(tokens, pos + 2);
                if (arg1.nextPos >= tokens.size() || tokens.get(arg1.nextPos).kind != TokenKind.COMMA) {
                    throw new EvalException(tr("notenoughcalculator.error.expected_comma", tok.value), tok.pos);
                }
                ParseResult arg2 = parseExpression(tokens, arg1.nextPos + 1);
                if (arg2.nextPos >= tokens.size() || tokens.get(arg2.nextPos).kind != TokenKind.RPAREN) {
                    throw new EvalException(tr("notenoughcalculator.error.expected_closing_paren"), tok.pos);
                }
                BigDecimal result = switch (tok.value) {
                    case "min" -> arg1.value.min(arg2.value);
                    case "max" -> arg1.value.max(arg2.value);
                    case "pct" -> {
                        if (arg2.value.compareTo(BigDecimal.ZERO) == 0) {
                            throw new EvalException(tr("notenoughcalculator.error.division_by_zero"), tok.pos);
                        }
                        yield arg1.value.divide(arg2.value, mc).multiply(BigDecimal.valueOf(100)).stripTrailingZeros();
                    }
                    case "gcd" -> new BigDecimal(arg1.value.toBigInteger().gcd(arg2.value.toBigInteger()));
                    case "lcm" -> {
                        BigInteger b1 = arg1.value.toBigInteger();
                        BigInteger b2 = arg2.value.toBigInteger();
                        if (b1.equals(BigInteger.ZERO) || b2.equals(BigInteger.ZERO)) yield BigDecimal.ZERO;
                        BigInteger gcd = b1.gcd(b2);
                        BigInteger lcm = b1.multiply(b2).abs().divide(gcd);
                        yield new BigDecimal(lcm);
                    }
                    case "xor" -> new BigDecimal(arg1.value.toBigInteger().xor(arg2.value.toBigInteger()));
                    default -> throw new EvalException(tr("notenoughcalculator.error.unknown_function", tok.value), tok.pos);
                };
                RadixMode mode = arg1.radixMode != RadixMode.DEFAULT ? arg1.radixMode : arg2.radixMode;
                return new ParseResult(result, arg2.nextPos + 1, mode);
            }

            // Optional 2-argument functions ah(price, [hours]) and ahbin(price, [hours])
            if (tok.value.equals("ah") || tok.value.equals("ahbin")) {
                boolean isBin = tok.value.equals("ahbin");
                ParseResult arg1 = parseExpression(tokens, pos + 2);
                double durationHours = 6.0;
                int endPos = arg1.nextPos;
                if (arg1.nextPos < tokens.size() && tokens.get(arg1.nextPos).kind == TokenKind.COMMA) {
                    ParseResult arg2 = parseExpression(tokens, arg1.nextPos + 1);
                    durationHours = arg2.value.doubleValue();
                    endPos = arg2.nextPos;
                }
                if (endPos >= tokens.size() || tokens.get(endPos).kind != TokenKind.RPAREN) {
                    throw new EvalException(tr("notenoughcalculator.error.expected_closing_paren"), tok.pos);
                }
                BigDecimal result = calculateAhPayout(arg1.value, durationHours, isBin);
                return new ParseResult(result, endPos + 1, arg1.radixMode);
            }

            // Single-argument functions
            ParseResult arg = parseExpression(tokens, pos + 2);

            if (arg.nextPos >= tokens.size() || tokens.get(arg.nextPos).kind != TokenKind.RPAREN) {
                throw new EvalException(tr("notenoughcalculator.error.expected_closing_paren"), tok.pos);
            }

            if (tok.value.equals("hex")) {
                return new ParseResult(arg.value, arg.nextPos + 1, RadixMode.HEX);
            }
            if (tok.value.equals("bin")) {
                return new ParseResult(arg.value, arg.nextPos + 1, RadixMode.BIN);
            }
            if (tok.value.equals("oct")) {
                return new ParseResult(arg.value, arg.nextPos + 1, RadixMode.OCT);
            }
            if (tok.value.equals("bz")) {
                double taxRate = CalculatorConfig.getInstance().getBazaarTaxRate();
                BigDecimal result = calculateBzPayout(arg.value, taxRate);
                return new ParseResult(result, arg.nextPos + 1, arg.radixMode);
            }
            if (tok.value.equals("fmt")) {
                return new ParseResult(arg.value, arg.nextPos + 1, RadixMode.SHORTHAND);
            }

            BigDecimal result = applyFunction(tok.value, arg.value, tok.pos);
            return new ParseResult(result, arg.nextPos + 1, arg.radixMode);
        }

        if (tok.kind == TokenKind.LPAREN) {
            ParseResult inner = parseExpression(tokens, pos + 1);

            if (inner.nextPos >= tokens.size() || tokens.get(inner.nextPos).kind != TokenKind.RPAREN) {
                throw new EvalException(tr("notenoughcalculator.error.unmatched_parenthesis"), tok.pos);
            }

            return new ParseResult(inner.value, inner.nextPos + 1, inner.radixMode);
        }

        throw new EvalException(tr("notenoughcalculator.error.unexpected_token", tok.value), tok.pos);
    }


    private BigDecimal applyFunction(String func, BigDecimal arg, int pos) throws EvalException {
        switch (func) {
            case "sqrt":
                if (arg.compareTo(BigDecimal.ZERO) < 0) {
                    throw new EvalException(tr("notenoughcalculator.error.negative_sqrt"), pos);
                }
                return arg.sqrt(mc);

            case "abs":
                return arg.abs();

            case "floor":
                return arg.setScale(0, RoundingMode.FLOOR);

            case "ceil":
                return arg.setScale(0, RoundingMode.CEILING);

            case "round":
                return arg.setScale(0, RoundingMode.HALF_UP);

            case "log":
                if (arg.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new EvalException(tr("notenoughcalculator.error.log_non_positive"), pos);
                }
                return BigDecimal.valueOf(Math.log10(arg.doubleValue()));

            case "ln":
                if (arg.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new EvalException(tr("notenoughcalculator.error.log_non_positive"), pos);
                }
                return BigDecimal.valueOf(Math.log(arg.doubleValue()));

            case "sin":
                return BigDecimal.valueOf(Math.sin(Math.toRadians(arg.doubleValue())));

            case "cos":
                return BigDecimal.valueOf(Math.cos(Math.toRadians(arg.doubleValue())));

            case "tan":
                return BigDecimal.valueOf(Math.tan(Math.toRadians(arg.doubleValue())));

            case "rad":
                return BigDecimal.valueOf(Math.toRadians(arg.doubleValue()));

            case "deg":
                return BigDecimal.valueOf(Math.toDegrees(arg.doubleValue()));

            default:
                throw new EvalException(tr("notenoughcalculator.error.unknown_function", func), pos);
        }
    }

    public static BigDecimal calculateBzPayout(BigDecimal price, double taxRatePct) {
        return SkyblockTaxCalculator.calculateBzPayout(price, taxRatePct);
    }

    public static BigDecimal calculateAhPayout(BigDecimal price, double durationHours, boolean isBin) {
        return SkyblockTaxCalculator.calculateAhPayout(price, durationHours, isBin);
    }

    public static double calculateAhDurationFee(double hours) {
        return SkyblockTaxCalculator.calculateAhDurationFee(hours);
    }

    private boolean isInteger(BigDecimal value) {
        return value.stripTrailingZeros().scale() <= 0;
    }

    public void setVariable(String name, BigDecimal value) {
        variables.put(name.toLowerCase(), value);
    }

    public void setVariable(String name, String expr) throws EvalException {
        BigDecimal value = evaluateQuiet(expr);
        variables.put(name.toLowerCase(), value);
    }

    public void clearCustomVariables() {
        variables.clear();
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