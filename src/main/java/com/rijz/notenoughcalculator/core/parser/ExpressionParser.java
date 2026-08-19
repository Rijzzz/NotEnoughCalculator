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

import com.rijz.notenoughcalculator.config.CalculatorConfig;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator;
import com.rijz.notenoughcalculator.core.evaluator.MarketPriceLookup;
import com.rijz.notenoughcalculator.core.evaluator.PlayerStatLookup;
import com.rijz.notenoughcalculator.core.skyblock.SkyblockTaxCalculator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

public class ExpressionParser {

    private final MathContext mc;
    private final Map<String, BigDecimal> variables;
    private final BiConsumer<String, BigDecimal> variableSetter;

    public ExpressionParser(MathContext mc, Map<String, BigDecimal> variables, BiConsumer<String, BigDecimal> variableSetter) {
        this.mc = mc;
        this.variables = variables;
        this.variableSetter = variableSetter;
    }

    public ParseResult parse(List<Token> tokens) throws ExpressionEvaluator.EvalException {
        return parseExpression(tokens, 0);
    }

    public ParseResult parseExpression(List<Token> tokens, int pos) throws ExpressionEvaluator.EvalException {
        if (pos >= tokens.size() || tokens.get(pos).kind == TokenKind.EOF) {
            throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.unexpected_end"), pos);
        }

        if (pos < tokens.size() && tokens.get(pos).kind == TokenKind.VAR) {
            String rawName = tokens.get(pos).value;
            String cleanName = rawName.startsWith("$") ? rawName.substring(1) : rawName;

            if (pos + 1 < tokens.size() && tokens.get(pos + 1).kind == TokenKind.ASSIGN) {
                if (ExpressionEvaluator.isReservedVariable(cleanName)) {
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.reserved_variable", cleanName), tokens.get(pos).pos);
                }
                ParseResult right = parseBitwiseOr(tokens, pos + 2);
                if (variableSetter != null) {
                    variableSetter.accept(cleanName, right.value);
                }
                return right;
            }
        }
        return parseBitwiseOr(tokens, pos);
    }

    // Bitwise OR (|) - Lowest precedence above assignment
    private ParseResult parseBitwiseOr(List<Token> tokens, int pos) throws ExpressionEvaluator.EvalException {
        ParseResult left = parseBitwiseAnd(tokens, pos);
        int currentPos = left.nextPos;
        ExpressionEvaluator.RadixMode mode = left.radixMode;

        while (currentPos < tokens.size() && tokens.get(currentPos).kind == TokenKind.OP && tokens.get(currentPos).value.equals("|")) {
            ParseResult right = parseBitwiseAnd(tokens, currentPos + 1);
            if (right.radixMode != ExpressionEvaluator.RadixMode.NONE) mode = right.radixMode;
            try {
                long a = left.value.longValueExact();
                long b = right.value.longValueExact();
                left = new ParseResult(BigDecimal.valueOf(a | b), right.nextPos, mode);
            } catch (ArithmeticException e) {
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.invalid_number"), currentPos);
            }
            currentPos = right.nextPos;
        }

        return left;
    }

    // Bitwise AND (&)
    private ParseResult parseBitwiseAnd(List<Token> tokens, int pos) throws ExpressionEvaluator.EvalException {
        ParseResult left = parseShift(tokens, pos);
        int currentPos = left.nextPos;
        ExpressionEvaluator.RadixMode mode = left.radixMode;

        while (currentPos < tokens.size() && tokens.get(currentPos).kind == TokenKind.OP && tokens.get(currentPos).value.equals("&")) {
            ParseResult right = parseShift(tokens, currentPos + 1);
            if (right.radixMode != ExpressionEvaluator.RadixMode.NONE) mode = right.radixMode;
            try {
                long a = left.value.longValueExact();
                long b = right.value.longValueExact();
                left = new ParseResult(BigDecimal.valueOf(a & b), right.nextPos, mode);
            } catch (ArithmeticException e) {
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.invalid_number"), currentPos);
            }
            currentPos = right.nextPos;
        }

        return left;
    }

    // Bitwise shifts (<< and >>)
    private ParseResult parseShift(List<Token> tokens, int pos) throws ExpressionEvaluator.EvalException {
        ParseResult left = parseAddSub(tokens, pos);
        int currentPos = left.nextPos;
        ExpressionEvaluator.RadixMode mode = left.radixMode;

        while (currentPos < tokens.size() && tokens.get(currentPos).kind == TokenKind.OP
                && (tokens.get(currentPos).value.equals("<<") || tokens.get(currentPos).value.equals(">>"))) {
            String op = tokens.get(currentPos).value;
            ParseResult right = parseAddSub(tokens, currentPos + 1);
            if (right.radixMode != ExpressionEvaluator.RadixMode.NONE) mode = right.radixMode;
            try {
                long a = left.value.longValueExact();
                int b = right.value.intValueExact();
                long res = op.equals("<<") ? (a << b) : (a >> b);
                left = new ParseResult(BigDecimal.valueOf(res), right.nextPos, mode);
            } catch (ArithmeticException e) {
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.invalid_number"), currentPos);
            }
            currentPos = right.nextPos;
        }

        return left;
    }

    // Addition and subtraction with smart percentage calculation (e.g., 100 + 10% -> 100 + (100 * 0.10) = 110)
    private ParseResult parseAddSub(List<Token> tokens, int pos) throws ExpressionEvaluator.EvalException {
        ParseResult left = parseMulDiv(tokens, pos);
        int currentPos = left.nextPos;
        ExpressionEvaluator.RadixMode mode = left.radixMode;

        while (currentPos < tokens.size() && tokens.get(currentPos).kind == TokenKind.OP
                && (tokens.get(currentPos).value.equals("+") || tokens.get(currentPos).value.equals("-"))) {
            String op = tokens.get(currentPos).value;

            ParseResult right = parseMulDiv(tokens, currentPos + 1);
            if (right.radixMode != ExpressionEvaluator.RadixMode.NONE && right.radixMode != ExpressionEvaluator.RadixMode.DEFAULT) mode = right.radixMode;

            BigDecimal res;
            if (right.isPercentage) {
                BigDecimal pctVal = left.value.multiply(right.value);
                res = op.equals("+") ? left.value.add(pctVal, mc) : left.value.subtract(pctVal, mc);
            } else {
                res = op.equals("+") ? left.value.add(right.value, mc) : left.value.subtract(right.value, mc);
            }

            left = new ParseResult(res, right.nextPos, mode);
            currentPos = right.nextPos;
        }

        return left;
    }

    private ParseResult parseMulDiv(List<Token> tokens, int pos) throws ExpressionEvaluator.EvalException {
        ParseResult left = parsePower(tokens, pos);
        int currentPos = left.nextPos;
        ExpressionEvaluator.RadixMode mode = left.radixMode;

        while (currentPos < tokens.size() && tokens.get(currentPos).kind == TokenKind.OP
                && (tokens.get(currentPos).value.equals("*") || tokens.get(currentPos).value.equalsIgnoreCase("x")
                || tokens.get(currentPos).value.equals("/") || tokens.get(currentPos).value.equals("%"))) {

            String op = tokens.get(currentPos).value.toLowerCase();
            ParseResult right = parsePower(tokens, currentPos + 1);
            if (right.radixMode != ExpressionEvaluator.RadixMode.NONE) mode = right.radixMode;

            BigDecimal res;
            if (op.equals("*") || op.equals("x")) {
                res = left.value.multiply(right.value, mc);
            } else if (op.equals("/")) {
                if (right.value.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.division_by_zero"), currentPos);
                }
                res = left.value.divide(right.value, mc);
            } else {
                if (right.value.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.modulo_by_zero"), currentPos);
                }
                res = left.value.remainder(right.value, mc);
            }

            left = new ParseResult(res, right.nextPos, mode);
            currentPos = right.nextPos;
        }

        return left;
    }

    private ParseResult parsePower(List<Token> tokens, int pos) throws ExpressionEvaluator.EvalException {
        ParseResult left = parseUnary(tokens, pos);
        int currentPos = left.nextPos;

        if (currentPos < tokens.size() && tokens.get(currentPos).kind == TokenKind.OP && tokens.get(currentPos).value.equals("^")) {
            ParseResult right = parsePower(tokens, currentPos + 1);

            try {
                int exp = right.value.intValueExact();
                if (exp < -1000 || exp > 1000) {
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.exponent_too_large"), currentPos);
                }
                BigDecimal res = left.value.pow(exp, mc);
                return new ParseResult(res, right.nextPos, left.radixMode != ExpressionEvaluator.RadixMode.NONE ? left.radixMode : right.radixMode);
            } catch (ArithmeticException e) {
                if (left.value.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.negative_power"), currentPos);
                }
                double baseD = left.value.doubleValue();
                double expD = right.value.doubleValue();
                double resD = Math.pow(baseD, expD);
                return new ParseResult(BigDecimal.valueOf(resD), right.nextPos, left.radixMode != ExpressionEvaluator.RadixMode.NONE ? left.radixMode : right.radixMode);
            }
        }

        return left;
    }

    private ParseResult parseUnary(List<Token> tokens, int pos) throws ExpressionEvaluator.EvalException {
        if (pos >= tokens.size()) {
            throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.unexpected_end"), pos);
        }

        Token tok = tokens.get(pos);
        if (tok.kind == TokenKind.OP && tok.value.equals("-")) {
            ParseResult res = parseUnary(tokens, pos + 1);
            return new ParseResult(res.value.negate(), res.nextPos, res.radixMode);
        }
        if (tok.kind == TokenKind.OP && tok.value.equals("+")) {
            return parseUnary(tokens, pos + 1);
        }
        if (tok.kind == TokenKind.OP && tok.value.equals("~")) {
            ParseResult res = parseUnary(tokens, pos + 1);
            try {
                long val = res.value.longValueExact();
                return new ParseResult(BigDecimal.valueOf(~val), res.nextPos, res.radixMode);
            } catch (ArithmeticException e) {
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.invalid_number"), pos);
            }
        }

        return parseFactorial(tokens, pos);
    }

    private ParseResult parseFactorial(List<Token> tokens, int pos) throws ExpressionEvaluator.EvalException {
        ParseResult left = parsePrimary(tokens, pos);
        int currentPos = left.nextPos;

        while (currentPos < tokens.size() && tokens.get(currentPos).kind == TokenKind.FACTORIAL) {
            BigDecimal val = left.value;
            try {
                long n = val.longValueExact();
                if (n < 0) {
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.factorial_non_integer"), currentPos);
                }
                if (n > 1000) {
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.factorial_too_large"), currentPos);
                }
                BigDecimal fact = BigDecimal.ONE;
                for (long i = 2; i <= n; i++) {
                    fact = fact.multiply(BigDecimal.valueOf(i));
                }
                left = new ParseResult(fact, currentPos + 1, left.radixMode);
            } catch (ArithmeticException e) {
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.factorial_non_integer"), currentPos);
            }
            currentPos++;
        }

        return left;
    }

    private ParseResult parsePrimary(List<Token> tokens, int pos) throws ExpressionEvaluator.EvalException {
        if (pos >= tokens.size()) {
            throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.unexpected_end"), pos);
        }

        Token tok = tokens.get(pos);

        if (tok.kind == TokenKind.NUM) {
            BigDecimal val = tok.number;
            ExpressionEvaluator.RadixMode mode = ExpressionEvaluator.RadixMode.NONE;
            if (tok.value.startsWith("0b") || tok.value.startsWith("0B")) mode = ExpressionEvaluator.RadixMode.BIN;
            else if (tok.value.startsWith("0x") || tok.value.startsWith("0X")) mode = ExpressionEvaluator.RadixMode.HEX;
            else if (tok.value.startsWith("0o") || tok.value.startsWith("0O")) mode = ExpressionEvaluator.RadixMode.OCT;

            int nextPos = pos + 1;
            boolean isPct = false;
            if (nextPos < tokens.size() && tokens.get(nextPos).kind == TokenKind.UNIT) {
                Token unitTok = tokens.get(nextPos);
                BigDecimal mult = ExpressionEvaluator.UNITS.get(unitTok.value.toLowerCase());
                if (mult != null) {
                    val = val.multiply(mult);
                    nextPos++;
                }
            }
            if (nextPos < tokens.size() && tokens.get(nextPos).kind == TokenKind.PERCENT) {
                val = val.divide(new BigDecimal("100"), mc);
                isPct = true;
                nextPos++;
            }

            ParseResult res = new ParseResult(val, nextPos, mode);
            res.isPercentage = isPct;
            return res;
        }

        if (tok.kind == TokenKind.VAR) {
            String rawName = tok.value.toLowerCase(Locale.ROOT);
            String cleanName = rawName.startsWith("$") ? rawName.substring(1) : rawName;

            if (variables.containsKey(cleanName)) {
                return new ParseResult(variables.get(cleanName), pos + 1);
            }
            if (variables.containsKey(rawName)) {
                return new ParseResult(variables.get(rawName), pos + 1);
            }

            BigDecimal apiVal = PlayerStatLookup.lookupPlayerStat(cleanName);
            if (apiVal != null) {
                return new ParseResult(apiVal, pos + 1);
            }

            if (ExpressionEvaluator.BUILTIN_VARIABLES.contains(cleanName) || ExpressionEvaluator.BUILTIN_VARIABLES.contains(rawName)) {
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.api_data_unavailable", cleanName), tok.pos);
            }

            throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.undefined_variable", tok.value), tok.pos);
        }

        if (tok.kind == TokenKind.FUNC) {
            if (pos + 1 >= tokens.size() || tokens.get(pos + 1).kind != TokenKind.LPAREN) {
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.expected_parenthesis", tok.value), tok.pos);
            }

            String fnName = tok.value.toLowerCase();

            if (isMarketFunction(fnName)) {
                int nextPos = pos + 2;
                if (nextPos < tokens.size() && tokens.get(nextPos).kind == TokenKind.VAR) {
                    String itemId = tokens.get(nextPos).value;
                    int endPos = nextPos + 1;
                    if (endPos < tokens.size() && tokens.get(endPos).kind == TokenKind.RPAREN) {
                        BigDecimal marketPrice = MarketPriceLookup.lookupMarketPrice(fnName, itemId);
                        if (marketPrice != null) {
                            return new ParseResult(marketPrice, endPos + 1);
                        }
                        throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.item_not_found", itemId), tok.pos);
                    }
                }
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.invalid_market_syntax", fnName), tok.pos);
            }

            if (fnName.equals("avg")) {
                int curPos = pos + 2;
                List<BigDecimal> args = new ArrayList<>();
                ExpressionEvaluator.RadixMode mode = ExpressionEvaluator.RadixMode.NONE;

                while (curPos < tokens.size() && tokens.get(curPos).kind != TokenKind.RPAREN) {
                    ParseResult argRes = parseExpression(tokens, curPos);
                    args.add(argRes.value);
                    if (argRes.radixMode != ExpressionEvaluator.RadixMode.NONE) mode = argRes.radixMode;
                    curPos = argRes.nextPos;
                    if (curPos < tokens.size() && tokens.get(curPos).kind == TokenKind.COMMA) {
                        curPos++;
                    }
                }
                if (curPos >= tokens.size() || tokens.get(curPos).kind != TokenKind.RPAREN) {
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.expected_closing_paren"), curPos);
                }
                if (args.isEmpty()) {
                    return new ParseResult(BigDecimal.ZERO, curPos + 1, mode);
                }
                BigDecimal sum = BigDecimal.ZERO;
                for (BigDecimal arg : args) sum = sum.add(arg);
                BigDecimal avgVal = sum.divide(BigDecimal.valueOf(args.size()), mc);
                return new ParseResult(avgVal, curPos + 1, mode);
            }

            if (fnName.equals("min") || fnName.equals("max") || fnName.equals("pct") || fnName.equals("gcd") || fnName.equals("lcm") || fnName.equals("xor") || fnName.equals("ahbin")) {
                ParseResult arg1 = parseExpression(tokens, pos + 2);
                int nextPos = arg1.nextPos;

                if (nextPos >= tokens.size() || tokens.get(nextPos).kind != TokenKind.COMMA) {
                    if (fnName.equals("ahbin")) {
                        if (nextPos < tokens.size() && tokens.get(nextPos).kind == TokenKind.RPAREN) {
                            BigDecimal payout = SkyblockTaxCalculator.calculateAhPayout(arg1.value, 6.0, true);
                            return new ParseResult(payout, nextPos + 1, arg1.radixMode);
                        }
                    }
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.expected_comma", tok.value), nextPos);
                }

                ParseResult arg2 = parseExpression(tokens, nextPos + 1);
                nextPos = arg2.nextPos;

                if (nextPos >= tokens.size() || tokens.get(nextPos).kind != TokenKind.RPAREN) {
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.expected_closing_paren"), nextPos);
                }

                BigDecimal resultVal = applyBinaryFunction(fnName, arg1.value, arg2.value, pos);
                return new ParseResult(resultVal, nextPos + 1, arg1.radixMode != ExpressionEvaluator.RadixMode.NONE ? arg1.radixMode : arg2.radixMode);
            }

            if (fnName.equals("clamp")) {
                ParseResult vRes = parseExpression(tokens, pos + 2);
                int np = vRes.nextPos;
                if (np >= tokens.size() || tokens.get(np).kind != TokenKind.COMMA) throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.expected_comma", tok.value), np);

                ParseResult minRes = parseExpression(tokens, np + 1);
                np = minRes.nextPos;
                if (np >= tokens.size() || tokens.get(np).kind != TokenKind.COMMA) throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.expected_comma", tok.value), np);

                ParseResult maxRes = parseExpression(tokens, np + 1);
                np = maxRes.nextPos;
                if (np >= tokens.size() || tokens.get(np).kind != TokenKind.RPAREN) throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.expected_closing_paren"), np);

                BigDecimal clamped = vRes.value.min(maxRes.value).max(minRes.value);
                return new ParseResult(clamped, np + 1, vRes.radixMode);
            }

            ParseResult argResult = parseExpression(tokens, pos + 2);
            int nextPos = argResult.nextPos;

            if (nextPos >= tokens.size() || tokens.get(nextPos).kind != TokenKind.RPAREN) {
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.expected_closing_paren"), nextPos);
            }

            ExpressionEvaluator.RadixMode mode = argResult.radixMode;
            if (fnName.equals("hex")) mode = ExpressionEvaluator.RadixMode.HEX;
            else if (fnName.equals("bin")) mode = ExpressionEvaluator.RadixMode.BIN;
            else if (fnName.equals("oct")) mode = ExpressionEvaluator.RadixMode.OCT;
            else if (fnName.equals("fmt")) mode = ExpressionEvaluator.RadixMode.SHORTHAND;

            BigDecimal resultValue = applyFunction(fnName, argResult.value, pos);
            return new ParseResult(resultValue, nextPos + 1, mode);
        }

        if (tok.kind == TokenKind.LPAREN) {
            ParseResult inner = parseExpression(tokens, pos + 1);
            int nextPos = inner.nextPos;
            if (nextPos >= tokens.size() || tokens.get(nextPos).kind != TokenKind.RPAREN) {
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.unmatched_parenthesis"), pos);
            }
            nextPos++;
            boolean isPct = false;
            BigDecimal val = inner.value;
            if (nextPos < tokens.size() && tokens.get(nextPos).kind == TokenKind.PERCENT) {
                val = val.divide(new BigDecimal("100"), mc);
                isPct = true;
                nextPos++;
            }
            ParseResult res = new ParseResult(val, nextPos, inner.radixMode);
            res.isPercentage = isPct;
            return res;
        }

        throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.unexpected_token", tok.value), tok.pos);
    }

    private static boolean isMarketFunction(String name) {
        return name.equals("bzb") || name.equals("bzbuy") || name.equals("bzs") || name.equals("bzsell")
                || name.equals("bzm") || name.equals("bzmargin") || name.equals("lb") || name.equals("lowestbin")
                || name.equals("lba") || name.equals("lowestbinavg") || name.equals("npc") || name.equals("npcsell")
                || name.equals("motes") || name.equals("motessell") || name.equals("price")
                || name.equals("sack") || name.equals("sackcount");
    }

    private BigDecimal applyBinaryFunction(String func, BigDecimal a, BigDecimal b, int pos) throws ExpressionEvaluator.EvalException {
        switch (func) {
            case "min": return a.min(b);
            case "max": return a.max(b);
            case "pct": return a.multiply(new BigDecimal("100")).divide(b, mc);
            case "gcd": return new BigDecimal(a.toBigInteger().gcd(b.toBigInteger()));
            case "lcm":
                BigInteger biA = a.toBigInteger();
                BigInteger biB = b.toBigInteger();
                if (biA.equals(BigInteger.ZERO) || biB.equals(BigInteger.ZERO)) return BigDecimal.ZERO;
                BigInteger gcd = biA.gcd(biB);
                return new BigDecimal(biA.multiply(biB).abs().divide(gcd));
            case "xor":
                long lA = a.longValueExact();
                long lB = b.longValueExact();
                return BigDecimal.valueOf(lA ^ lB);
            case "ahbin":
                return SkyblockTaxCalculator.calculateAhPayout(a, b.doubleValue(), true);
            default:
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.unknown_function", func), pos);
        }
    }

    private BigDecimal applyFunction(String func, BigDecimal arg, int pos) throws ExpressionEvaluator.EvalException {
        switch (func) {
            case "sqrt":
                if (arg.compareTo(BigDecimal.ZERO) < 0) {
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.negative_sqrt"), pos);
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
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.log_non_positive"), pos);
                }
                return BigDecimal.valueOf(Math.log10(arg.doubleValue()));
            case "ln":
                if (arg.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.log_non_positive"), pos);
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
            case "bz":
                return SkyblockTaxCalculator.calculateBzPayout(arg, CalculatorConfig.getInstance().getBazaarTaxRate());
            case "ah":
                return SkyblockTaxCalculator.calculateAhPayout(arg, 6.0, false);
            case "ahbin":
                return SkyblockTaxCalculator.calculateAhPayout(arg, 6.0, true);
            case "hex":
            case "bin":
            case "oct":
            case "fmt":
                return arg;
            default:
                throw new ExpressionEvaluator.EvalException(ExpressionEvaluator.tr("notenoughcalculator.error.unknown_function", func), pos);
        }
    }
}
