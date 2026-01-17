package com.rijz.notenoughcalculator.core;

import com.rijz.notenoughcalculator.config.CalculatorConfig;
import net.minecraft.client.resource.language.I18n;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

// Custom calculator engine with Hypixel Skyblock units and variables
// Built from scratch by Rijz & Laze
public class ExpressionEvaluator {

    private final MathContext mc;
    private final Map<String, BigDecimal> variables;
    private final List<String> history;
    private BigDecimal lastAnswer;

    public ExpressionEvaluator() {
        CalculatorConfig config = CalculatorConfig.getInstance();
        // Use much higher precision to avoid rounding errors with large numbers
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

    // Helper method for creating translated error messages
    private static String tr(String key, Object... args) {
        return I18n.translate(key, args);
    }

    // Different types of tokens our parser recognizes
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

    // Skyblock unit multipliers (k=thousand, m=million, s=stack, etc.)
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

    // NEW: Evaluate without adding to history (for inline display)
    public BigDecimal evaluateQuiet(String expr) throws EvalException {
        if (expr == null || expr.trim().isEmpty()) {
            throw new EvalException(tr("notenoughcalculator.error.empty_expression"), 0);
        }

        // Break expression into tokens and parse them
        List<Token> tokens = tokenize(expr);
        BigDecimal result = parseExpression(tokens, 0).value;

        // Update lastAnswer but DON'T add to history
        lastAnswer = result;

        return result;
    }

    // Main entry point: evaluate a math expression (adds to history)
    public BigDecimal evaluate(String expr) throws EvalException {
        if (expr == null || expr.trim().isEmpty()) {
            throw new EvalException(tr("notenoughcalculator.error.empty_expression"), 0);
        }

        // Break expression into tokens and parse them
        List<Token> tokens = tokenize(expr);
        BigDecimal result = parseExpression(tokens, 0).value;

        // Remember this result for 'ans' variable AND add to history
        lastAnswer = result;
        CalculatorConfig config = CalculatorConfig.getInstance();

        // Only add to history if it's different from the last entry
        if (history.isEmpty() || !history.get(history.size() - 1).equals(expr)) {
            history.add(expr);
            if (history.size() > config.maxHistorySize) {
                history.remove(0);
            }
        }

        return result;
    }

    // Break the expression string into individual tokens
    private List<Token> tokenize(String expr) throws EvalException {
        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < expr.length()) {
            char c = expr.charAt(i);

            // Skip spaces
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Parse numbers (including decimals like 3.14)
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

                // Make sure we got a valid number
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

            // Math operators
            if ("+-*/^%".indexOf(c) != -1) {
                tokens.add(new Token(TokenKind.OP, String.valueOf(c), i));
                i++;
                continue;
            }

            // Parentheses for grouping
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

            // Variables ($profit), functions (sqrt), or units (m, s, k)
            if (c == '$' || Character.isLetter(c)) {
                int start = i;
                StringBuilder name = new StringBuilder();

                if (c == '$') {
                    i++; // Skip the $ symbol
                }

                while (i < expr.length() && (Character.isLetterOrDigit(expr.charAt(i)) || expr.charAt(i) == '_')) {
                    name.append(expr.charAt(i));
                    i++;
                }

                String nameStr = name.toString().toLowerCase();

                // Figure out what this text means: function, unit, or variable?
                if (FUNCTIONS.contains(nameStr)) {
                    tokens.add(new Token(TokenKind.FUNC, nameStr, start));
                } else if (UNITS.containsKey(nameStr)) {
                    // Units only make sense after numbers (like "100m")
                    if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).kind == TokenKind.NUM) {
                        tokens.add(new Token(TokenKind.UNIT, nameStr, start));
                    } else {
                        // Otherwise treat it as a variable
                        tokens.add(new Token(TokenKind.VAR, nameStr, start));
                    }
                } else if (nameStr.equals("ans")) {
                    // 'ans' is a special variable for the last result
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

    // Helper class to pass around results during parsing
    private static class ParseResult {
        BigDecimal value;
        int nextPos;

        ParseResult(BigDecimal v, int p) {
            value = v;
            nextPos = p;
        }
    }

    // Start parsing the expression (entry point for parser)
    private ParseResult parseExpression(List<Token> tokens, int pos) throws EvalException {
        return parseAddSub(tokens, pos);
    }

    // Handle addition and subtraction (lowest precedence)
    // CRITICAL: NO MathContext here - use unlimited precision!
    private ParseResult parseAddSub(List<Token> tokens, int pos) throws EvalException {
        ParseResult left = parseMulDiv(tokens, pos);

        while (left.nextPos < tokens.size()) {
            Token tok = tokens.get(left.nextPos);
            if (tok.kind != TokenKind.OP || (!tok.value.equals("+") && !tok.value.equals("-"))) {
                break;
            }

            String op = tok.value;

            // Check if there's a right operand - if not, expression is incomplete
            if (left.nextPos + 1 >= tokens.size() || tokens.get(left.nextPos + 1).kind == TokenKind.EOF) {
                throw new EvalException(tr("notenoughcalculator.error.unfinished_expression"), tok.pos);
            }

            ParseResult right = parseMulDiv(tokens, left.nextPos + 1);

            // Use unlimited precision for addition/subtraction
            // This prevents losing small numbers when adding to large numbers
            if (op.equals("+")) {
                left = new ParseResult(left.value.add(right.value), right.nextPos);
            } else {
                left = new ParseResult(left.value.subtract(right.value), right.nextPos);
            }
        }

        return left;
    }

    // Handle multiplication, division, and modulo (higher precedence than +/-)
    // CRITICAL: NO MathContext for multiplication - use unlimited precision!
    private ParseResult parseMulDiv(List<Token> tokens, int pos) throws EvalException {
        ParseResult left = parsePower(tokens, pos);

        while (left.nextPos < tokens.size()) {
            Token tok = tokens.get(left.nextPos);
            if (tok.kind != TokenKind.OP || (!tok.value.equals("*") && !tok.value.equals("/") && !tok.value.equals("%"))) {
                break;
            }

            String op = tok.value;

            // Check if there's a right operand - if not, expression is incomplete
            if (left.nextPos + 1 >= tokens.size() || tokens.get(left.nextPos + 1).kind == TokenKind.EOF) {
                throw new EvalException(tr("notenoughcalculator.error.unfinished_expression"), tok.pos);
            }

            ParseResult right = parsePower(tokens, left.nextPos + 1);

            if (op.equals("*")) {
                // Use unlimited precision for multiplication
                left = new ParseResult(left.value.multiply(right.value), right.nextPos);
            } else if (op.equals("/")) {
                if (right.value.compareTo(BigDecimal.ZERO) == 0) {
                    throw new EvalException(tr("notenoughcalculator.error.division_by_zero"), tok.pos);
                }
                // Only use MathContext for division to prevent infinite decimals (1/3 = 0.333...)
                left = new ParseResult(left.value.divide(right.value, mc).stripTrailingZeros(), right.nextPos);
            } else { // modulo
                if (right.value.compareTo(BigDecimal.ZERO) == 0) {
                    throw new EvalException(tr("notenoughcalculator.error.modulo_by_zero"), tok.pos);
                }
                // Use unlimited precision for modulo
                left = new ParseResult(left.value.remainder(right.value), right.nextPos);
            }
        }

        return left;
    }

    // Handle exponentiation (highest precedence operator)
    private ParseResult parsePower(List<Token> tokens, int pos) throws EvalException {
        ParseResult left = parseUnary(tokens, pos);

        if (left.nextPos < tokens.size()) {
            Token tok = tokens.get(left.nextPos);
            if (tok.kind == TokenKind.OP && tok.value.equals("^")) {
                // Check if there's a right operand - if not, expression is incomplete
                if (left.nextPos + 1 >= tokens.size() || tokens.get(left.nextPos + 1).kind == TokenKind.EOF) {
                    throw new EvalException(tr("notenoughcalculator.error.unfinished_expression"), tok.pos);
                }

                ParseResult right = parsePower(tokens, left.nextPos + 1); // Right-associative: 2^3^2 = 2^(3^2)

                // Sanity check: don't allow crazy huge exponents
                if (right.value.abs().compareTo(new BigDecimal("1000")) > 0) {
                    throw new EvalException(tr("notenoughcalculator.error.exponent_too_large"), tok.pos);
                }

                // Can't do negative^decimal (like (-2)^0.5)
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

    // Handle unary operators (like negative signs: -5)
    private ParseResult parseUnary(List<Token> tokens, int pos) throws EvalException {
        if (pos >= tokens.size()) {
            throw new EvalException(tr("notenoughcalculator.error.unexpected_end"), pos);
        }

        Token tok = tokens.get(pos);

        // Negative number
        if (tok.kind == TokenKind.OP && tok.value.equals("-")) {
            ParseResult result = parseUnary(tokens, pos + 1);
            return new ParseResult(result.value.negate(), result.nextPos);
        }

        // Positive sign (just ignore it)
        if (tok.kind == TokenKind.OP && tok.value.equals("+")) {
            return parseUnary(tokens, pos + 1);
        }

        return parsePostfix(tokens, pos);
    }

    // Handle unit suffixes (like "100m" where m multiplies by 1,000,000)
    private ParseResult parsePostfix(List<Token> tokens, int pos) throws EvalException {
        ParseResult result = parsePrimary(tokens, pos);

        // Check if there's a unit suffix after the number
        if (result.nextPos < tokens.size()) {
            Token tok = tokens.get(result.nextPos);
            if (tok.kind == TokenKind.UNIT) {
                BigDecimal multiplier = UNITS.get(tok.value);
                // Use unlimited precision for unit multiplication
                result = new ParseResult(result.value.multiply(multiplier), result.nextPos + 1);
            }
        }

        return result;
    }

    // Parse primary expressions (numbers, variables, functions, parentheses)
    private ParseResult parsePrimary(List<Token> tokens, int pos) throws EvalException {
        if (pos >= tokens.size()) {
            throw new EvalException(tr("notenoughcalculator.error.unexpected_end"), pos);
        }

        Token tok = tokens.get(pos);

        // Just a number
        if (tok.kind == TokenKind.NUM) {
            return new ParseResult(tok.number, pos + 1);
        }

        // A variable like $profit
        if (tok.kind == TokenKind.VAR) {
            if (!variables.containsKey(tok.value)) {
                throw new EvalException(tr("notenoughcalculator.error.undefined_variable", tok.value), tok.pos);
            }
            return new ParseResult(variables.get(tok.value), pos + 1);
        }

        // A function like sqrt(144)
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

        // Expression in parentheses like (5 + 3)
        if (tok.kind == TokenKind.LPAREN) {
            ParseResult inner = parseExpression(tokens, pos + 1);

            if (inner.nextPos >= tokens.size() || tokens.get(inner.nextPos).kind != TokenKind.RPAREN) {
                throw new EvalException(tr("notenoughcalculator.error.unmatched_parenthesis"), tok.pos);
            }

            return new ParseResult(inner.value, inner.nextPos + 1);
        }

        throw new EvalException(tr("notenoughcalculator.error.unexpected_token", tok.value), tok.pos);
    }

    // Execute math functions like sqrt, abs, floor, etc.
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

    // Check if a number is a whole number (no decimal part)
    private boolean isInteger(BigDecimal value) {
        return value.stripTrailingZeros().scale() <= 0;
    }

    // Set a custom variable to a specific value
    public void setVariable(String name, BigDecimal value) {
        variables.put(name.toLowerCase(), value);
    }

    // Set a custom variable by calculating an expression
    public void setVariable(String name, String expr) throws EvalException {
        BigDecimal value = evaluate(expr);
        variables.put(name.toLowerCase(), value);
    }

    // Get the result from the last calculation (for 'ans' variable)
    public BigDecimal getLastAnswer() {
        return lastAnswer;
    }

    // Get list of all previous calculations
    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    // Wipe all calculation history
    public void clearHistory() {
        history.clear();
    }

    // Get a nice formatted list of all saved variables
    public String getVariablesInfo() {
        if (variables.isEmpty()) {
            return tr("notenoughcalculator.variable.none");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(tr("notenoughcalculator.variable.list_title", variables.size())).append("\n");

        // Sort alphabetically for easier reading
        List<String> sortedKeys = new ArrayList<>(variables.keySet());
        Collections.sort(sortedKeys);

        for (String key : sortedKeys) {
            sb.append("  $").append(key).append(" = ")
                    .append(ResultFormatter.formatWithCommas(variables.get(key))).append("\n");
        }
        return sb.toString().trim();
    }
}