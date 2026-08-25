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

import static org.junit.jupiter.api.Assertions.*;

import com.rijz.notenoughcalculator.core.ExpressionEvaluator.EvalException;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator.EvalResult;
import com.rijz.notenoughcalculator.core.ExpressionEvaluator.RadixMode;
import com.rijz.notenoughcalculator.core.evaluator.PlayerStatLookup;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorTest {

	private ExpressionEvaluator eval;

	@BeforeEach
	void setUp() {
		eval = new ExpressionEvaluator(10);
	}

	private BigDecimal calc(String expr) throws EvalException {
		return eval.evaluate(expr);
	}

	private void assertCalc(String expr, String expected) throws EvalException {
		BigDecimal result = calc(expr);
		assertEquals(0, new BigDecimal(expected).compareTo(result),
				expr + " should equal " + expected + " but got " + result.toPlainString());
	}

	private void assertCalcApprox(String expr, double expected, double tolerance) throws EvalException {
		BigDecimal result = calc(expr);
		assertEquals(expected, result.doubleValue(), tolerance, expr + " should be approximately " + expected);
	}

	@Nested
	@DisplayName("Basic Arithmetic")
	class BasicArithmetic {
		@Test
		void addition() throws Exception {
			assertCalc("2+3", "5");
		}

		@Test
		void subtraction() throws Exception {
			assertCalc("10-4", "6");
		}

		@Test
		void multiplication() throws Exception {
			assertCalc("6*7", "42");
		}

		@Test
		void division() throws Exception {
			assertCalcApprox("10/3", 3.3333333333, 0.0001);
		}

		@Test
		void multipleOperations() throws Exception {
			assertCalc("1+2+3+4+5", "15");
		}

		@Test
		void mixedOperations() throws Exception {
			assertCalc("10+5*2", "20");
		}

		@Test
		void decimals() throws Exception {
			assertCalc("1.5+2.5", "4");
		}

		@Test
		void largeNumbers() throws Exception {
			assertCalc("999999999+1", "1000000000");
		}

		@Test
		void decimalMultiplication() throws Exception {
			assertCalc("0.5 * 0.2", "0.1");
		}

		@Test
		void decimalDivision() throws Exception {
			assertCalc("1.5 / 0.5", "3");
		}

		@Test
		void massiveExpression() throws Exception {
			BigDecimal res = calc(
					"1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+121232131231231231231231231231+23123123123123123123123123123+123+13123131232322+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1+1");
			assertEquals("144355254354354367477485586851", res.toPlainString());
		}
	}

	@Nested
	@DisplayName("Operator Precedence")
	class Precedence {
		@Test
		void mulBeforeAdd() throws Exception {
			assertCalc("2+3*4", "14");
		}

		@Test
		void parensOverride() throws Exception {
			assertCalc("(2+3)*4", "20");
		}

		@Test
		void powerBeforeMul() throws Exception {
			assertCalc("2*3^2", "18");
		}

		@Test
		void nestedParens() throws Exception {
			assertCalc("((2+3)*4)+1", "21");
		}

		@Test
		void complexExpression() throws Exception {
			assertCalc("2^3+1", "9");
		}

		@Test
		void deepNestedParens() throws Exception {
			assertCalc("(((10 + 5) * 2) - 10) / 2", "10");
		}
	}

	@Nested
	@DisplayName("Unary Operators")
	class UnaryOperators {
		@Test
		void negation() throws Exception {
			assertCalc("-5+3", "-2");
		}

		@Test
		void positiveUnary() throws Exception {
			assertCalc("+5", "5");
		}

		@Test
		void doubleNegation() throws Exception {
			assertCalc("--5", "5");
		}

		@Test
		void tripleNegation() throws Exception {
			assertCalc("---5", "-5");
		}

		@Test
		void negationWithParens() throws Exception {
			assertCalc("-(3+2)", "-5");
		}

		@Test
		void unaryInMultiply() throws Exception {
			assertCalc("5 * -2", "-10");
		}
	}

	@Nested
	@DisplayName("Exponentiation")
	class Exponentiation {
		@Test
		void basicPower() throws Exception {
			assertCalc("2^10", "1024");
		}

		@Test
		void negativePower() throws Exception {
			assertCalc("(-2)^3", "-8");
		}

		@Test
		void zeroPower() throws Exception {
			assertCalc("5^0", "1");
		}

		@Test
		void onePower() throws Exception {
			assertCalc("5^1", "5");
		}

		@Test
		void fractionalResultPower() throws Exception {
			assertCalc("2^-2", "0.25");
		}

		@Test
		void exponentTooLarge() {
			assertThrows(EvalException.class, () -> calc("2^1001"));
		}
	}

	@Nested
	@DisplayName("Modulo")
	class Modulo {
		@Test
		void basicModulo() throws Exception {
			assertCalc("10 % 3", "1");
		}

		@Test
		void exactDivisor() throws Exception {
			assertCalc("9 % 3", "0");
		}

		@Test
		void moduloSelf() throws Exception {
			assertCalc("7 % 7", "0");
		}

		@Test
		void moduloDecimal() throws Exception {
			assertCalc("10.5 % 3", "1.5");
		}

		@Test
		void moduloByZero() {
			assertThrows(EvalException.class, () -> calc("10 % 0"));
		}
	}

	@Nested
	@DisplayName("Skyblock Units")
	class Units {
		@Test
		void thousand() throws Exception {
			assertCalc("10k", "10000");
		}

		@Test
		void million() throws Exception {
			assertCalc("5m", "5000000");
		}

		@Test
		void billion() throws Exception {
			assertCalc("2b", "2000000000");
		}

		@Test
		void trillion() throws Exception {
			assertCalc("1t", "1000000000000");
		}

		@Test
		void stack() throws Exception {
			assertCalc("3s", "192");
		}

		@Test
		void enchanted() throws Exception {
			assertCalc("2e", "320");
		}

		@Test
		void shulker() throws Exception {
			assertCalc("1h", "1728");
		}

		@Test
		void smallChest() throws Exception {
			assertCalc("1sc", "1728");
		}

		@Test
		void doubleChest() throws Exception {
			assertCalc("2dc", "6912");
		}

		@Test
		void enderChest() throws Exception {
			assertCalc("1eb", "2880");
		}

		@Test
		void unitArithmetic() throws Exception {
			assertCalc("10k+5k", "15000");
		}

		@Test
		void mixedUnits() throws Exception {
			assertCalc("1m+500k", "1500000");
		}

		@Test
		void storageMath() throws Exception {
			assertCalc("2h + 3dc", "13824");
		}

		@Test
		void unitInFunction() throws Exception {
			assertCalc("log(10k)", "4");
		}

		@Test
		void unitInSqrt() throws Exception {
			assertCalcApprox("sqrt(1h)", 41.569219, 0.001);
		}
	}

	@Nested
	@DisplayName("Existing Functions")
	class ExistingFunctions {
		@Test
		void sqrt() throws Exception {
			assertCalc("sqrt(144)", "12");
		}

		@Test
		void sqrtDecimal() throws Exception {
			assertCalcApprox("sqrt(2)", 1.41421356, 0.0001);
		}

		@Test
		void abs() throws Exception {
			assertCalc("abs(-50)", "50");
		}

		@Test
		void absPositive() throws Exception {
			assertCalc("abs(25)", "25");
		}

		@Test
		void floor() throws Exception {
			assertCalc("floor(3.9)", "3");
		}

		@Test
		void ceil() throws Exception {
			assertCalc("ceil(3.1)", "4");
		}

		@Test
		void round() throws Exception {
			assertCalc("round(3.5)", "4");
		}

		@Test
		void roundDown() throws Exception {
			assertCalc("round(3.4)", "3");
		}

		@Test
		void nested() throws Exception {
			assertCalc("sqrt(abs(-144))", "12");
		}

		@Test
		void negativeSqrt() {
			assertThrows(EvalException.class, () -> calc("sqrt(-1)"));
		}
	}

	@Nested
	@DisplayName("New Functions - Logarithms")
	class Logarithms {
		@Test
		void log10() throws Exception {
			assertCalc("log(100)", "2");
		}

		@Test
		void log1000() throws Exception {
			assertCalc("log(1000)", "3");
		}

		@Test
		void log1() throws Exception {
			assertCalc("log(1)", "0");
		}

		@Test
		void ln1() throws Exception {
			assertCalc("ln(1)", "0");
		}

		@Test
		void lnE() throws Exception {
			assertCalcApprox("ln(2.718281828)", 1.0, 0.0001);
		}

		@Test
		void logNonPositive() {
			assertThrows(EvalException.class, () -> calc("log(0)"));
			assertThrows(EvalException.class, () -> calc("log(-1)"));
			assertThrows(EvalException.class, () -> calc("ln(0)"));
		}
	}

	@Nested
	@DisplayName("New Functions - Trigonometry (Degrees)")
	class Trigonometry {
		@Test
		void sin90() throws Exception {
			assertCalcApprox("sin(90)", 1.0, 0.0001);
		}

		@Test
		void sin0() throws Exception {
			assertCalcApprox("sin(0)", 0.0, 0.0001);
		}

		@Test
		void sin30() throws Exception {
			assertCalcApprox("sin(30)", 0.5, 0.0001);
		}

		@Test
		void cos0() throws Exception {
			assertCalcApprox("cos(0)", 1.0, 0.0001);
		}

		@Test
		void cos90() throws Exception {
			assertCalcApprox("cos(90)", 0.0, 0.0001);
		}

		@Test
		void cos60() throws Exception {
			assertCalcApprox("cos(60)", 0.5, 0.0001);
		}

		@Test
		void tan45() throws Exception {
			assertCalcApprox("tan(45)", 1.0, 0.0001);
		}

		@Test
		void tan0() throws Exception {
			assertCalcApprox("tan(0)", 0.0, 0.0001);
		}

		@Test
		void trigCombination() throws Exception {
			assertCalcApprox("sin(90) + cos(0)", 2.0, 0.0001);
		}
	}

	@Nested
	@DisplayName("New Functions - Min/Max")
	class MinMax {
		@Test
		void minBasic() throws Exception {
			assertCalc("min(3, 5)", "3");
		}

		@Test
		void maxBasic() throws Exception {
			assertCalc("max(3, 5)", "5");
		}

		@Test
		void minNegative() throws Exception {
			assertCalc("min(-1, 1)", "-1");
		}

		@Test
		void maxNegative() throws Exception {
			assertCalc("max(-10, -5)", "-5");
		}

		@Test
		void minEqual() throws Exception {
			assertCalc("min(7, 7)", "7");
		}

		@Test
		void minExpression() throws Exception {
			assertCalc("min(2+3, 10-4)", "5");
		}

		@Test
		void maxExpression() throws Exception {
			assertCalc("max(2+3, 10-4)", "6");
		}

		@Test
		void minNested() throws Exception {
			assertCalc("min(min(1, 2), 3)", "1");
		}

		@Test
		void maxNested() throws Exception {
			assertCalc("max(max(10, 20), sqrt(100))", "20");
		}

		@Test
		void minWithUnits() throws Exception {
			assertCalc("min(5m, 10m)", "5000000");
		}

		@Test
		void maxWithUnits() throws Exception {
			assertCalc("max(1k, 500)", "1000");
		}
	}

	@Nested
	@DisplayName("Implicit Multiplication")
	class ImplicitMultiplication {
		@Test
		void numParen() throws Exception {
			assertCalc("2(3+4)", "14");
		}

		@Test
		void parenParen() throws Exception {
			assertCalc("(3)(4)", "12");
		}

		@Test
		void parenNum() throws Exception {
			assertCalc("(2+3)4", "20");
		}

		@Test
		void numFunc() throws Exception {
			assertCalc("2sqrt(4)", "4");
		}

		@Test
		void numLog() throws Exception {
			assertCalc("3log(100)", "6");
		}

		@Test
		void numMin() throws Exception {
			assertCalc("5min(10, 20)", "50");
		}

		@Test
		void unitParen() throws Exception {
			assertCalc("10k(2+3)", "50000");
		}

		@Test
		void complexImplicit() throws Exception {
			assertCalc("(2+3)(4+5)", "45");
		}

		@Test
		void tripleImplicit() throws Exception {
			assertCalc("(2+3)(4+5)(6+7)", "585");
		}
	}

	@Nested
	@DisplayName("Percentage")
	class Percentage {
		@Test
		void basicPercentage() throws Exception {
			assertCalcApprox("10%", 0.1, 0.0001);
		}

		@Test
		void halfPercent() throws Exception {
			assertCalcApprox("50%", 0.5, 0.0001);
		}

		@Test
		void fullPercent() throws Exception {
			assertCalcApprox("100%", 1.0, 0.0001);
		}

		@Test
		@DisplayName("Smart: 100 + 10% = 110")
		void smartAddPercent() throws Exception {
			assertCalc("100 + 10%", "110");
		}

		@Test
		@DisplayName("Smart: 200 - 25% = 150")
		void smartSubPercent() throws Exception {
			assertCalc("200 - 25%", "150");
		}

		@Test
		@DisplayName("Smart compound: 100 + 10% + 5% = 115.5")
		void smartCompound() throws Exception {
			assertCalcApprox("100 + 10% + 5%", 115.5, 0.0001);
		}

		@Test
		@DisplayName("Multiply with percentage: 100 * 10% = 10")
		void multiplyPercent() throws Exception {
			assertCalc("100 * 10%", "10");
		}

		@Test
		@DisplayName("Divide by percentage: 200 / 50% = 400")
		void dividePercent() throws Exception {
			assertCalc("200 / 50%", "400");
		}

		@Test
		@DisplayName("Paren with percentage: (100 + 20%) * 2 = 240")
		void parenPercent() throws Exception {
			assertCalc("((100 + 20%)) * 2", "240");
		}
	}

	@Nested
	@DisplayName("Percentage vs Modulo Disambiguation")
	class PercentVsModulo {
		@Test
		@DisplayName("Spaced: 10 % 3 = 1 (modulo)")
		void spacedIsModulo() throws Exception {
			assertCalc("10 % 3", "1");
		}

		@Test
		@DisplayName("Adjacent: 10% = 0.1 (percentage)")
		void adjacentIsPercentage() throws Exception {
			assertCalcApprox("10%", 0.1, 0.0001);
		}

		@Test
		@DisplayName("Modulo in context: 10 % 3 + 5 = 6")
		void moduloInContext() throws Exception {
			assertCalc("10 % 3 + 5", "6");
		}
	}

	@Nested
	@DisplayName("Variables")
	class Variables {
		@Test
		void ansVariable() throws Exception {
			calc("100+50");
			assertCalc("ans*2", "300");
		}

		@Test
		void customVariable() throws Exception {
			eval.setVariable("profit", new BigDecimal("50000000"));
			assertCalc("$profit * 2", "100000000");
		}

		@Test
		void chainedVariables() throws Exception {
			eval.setVariable("val1", "10k");
			eval.setVariable("val2", "$val1 + 5k");
			assertCalc("$val2 / $val1", "1.5");
		}

		@Test
		void undefinedVariable() {
			assertThrows(EvalException.class, () -> calc("$undefined + 1"));
		}

		@Test
		void clearCustomVariables() throws Exception {
			eval.setVariable("temp", "100");
			assertCalc("$temp", "100");
			eval.clearCustomVariables();
			assertThrows(EvalException.class, () -> calc("$temp"));
		}

		@Test
		void setVariableReturnValue() throws Exception {
			BigDecimal res1 = eval.setVariable("val1", new BigDecimal("50"));
			assertEquals(0, new BigDecimal("50").compareTo(res1));

			BigDecimal res2 = eval.setVariable("val2", "10k + 5k");
			assertEquals(0, new BigDecimal("15000").compareTo(res2));
		}

		@Test
		void reservedVariables() {
			assertTrue(ExpressionEvaluator.isReservedVariable("purse"));
			assertTrue(ExpressionEvaluator.isReservedVariable("bz"));
			assertTrue(ExpressionEvaluator.isReservedVariable("bzb"));
			assertTrue(ExpressionEvaluator.isReservedVariable("sqrt"));
			assertTrue(ExpressionEvaluator.isReservedVariable("k"));
			assertTrue(ExpressionEvaluator.isReservedVariable("m"));
			assertFalse(ExpressionEvaluator.isReservedVariable("buy"));
			assertFalse(ExpressionEvaluator.isReservedVariable("sell"));
			assertThrows(EvalException.class, () -> eval.setVariable("bz", new BigDecimal("10")));
		}
	}

	@Nested
	@DisplayName("x as Multiplication")
	class XMultiplication {
		@Test
		void basicX() throws Exception {
			assertCalc("10x5", "50");
		}

		@Test
		void upperX() throws Exception {
			assertCalc("10X5", "50");
		}

		@Test
		void unitX() throws Exception {
			assertCalc("10kx50k", "500000000");
		}

		@Test
		void parenX() throws Exception {
			assertCalc("(2+3)x4", "20");
		}
	}

	@Nested
	@DisplayName("Edge Cases & Errors")
	class EdgeCases {
		@Test
		void emptyExpression() {
			assertThrows(EvalException.class, () -> calc(""));
		}

		@Test
		void nullExpression() {
			assertThrows(EvalException.class, () -> calc(null));
		}

		@Test
		void divisionByZero() {
			assertThrows(EvalException.class, () -> calc("1/0"));
		}

		@Test
		void unmatchedParen() {
			assertThrows(EvalException.class, () -> calc("(2+3"));
		}

		@Test
		void trailingOperator() {
			assertThrows(EvalException.class, () -> calc("5+"));
		}

		@Test
		void unexpectedCharacter() {
			assertThrows(EvalException.class, () -> calc("5 @ 3"));
		}

		@Test
		void unconsumedTrailingTokens() {
			assertThrows(EvalException.class, () -> calc("10 20"));
			assertThrows(EvalException.class, () -> calc("10 + 5 apples"));
			assertThrows(EvalException.class, () -> calc("100 foo"));
		}
	}

	@Nested
	@DisplayName("History")
	class History {
		@Test
		void historyTracked() throws Exception {
			calc("1+1");
			calc("2+2");
			assertEquals(2, eval.getHistory().size());
		}

		@Test
		void clearHistory() throws Exception {
			calc("1+1");
			eval.clearHistory();
			assertEquals(0, eval.getHistory().size());
		}

		@Test
		void duplicateNotAdded() throws Exception {
			calc("1+1");
			calc("1+1");
			assertEquals(1, eval.getHistory().size());
		}
	}

	@Nested
	@DisplayName("Constants")
	class Constants {
		@Test
		void pi() throws Exception {
			assertCalcApprox("pi", 3.14159265, 0.0001);
		}

		@Test
		void piArithmetic() throws Exception {
			assertCalcApprox("2*pi", 6.28318530, 0.0001);
		}

		@Test
		void piInFunction() throws Exception {
			assertCalcApprox("sin(pi * 90 / pi)", 1.0, 0.0001);
		}

		@Test
		void eulerStandalone() throws Exception {
			assertCalcApprox("e", 2.71828182, 0.0001);
		}

		@Test
		void eulerArithmetic() throws Exception {
			assertCalcApprox("e*2", 5.43656365, 0.001);
		}

		@Test
		void eulerInFunction() throws Exception {
			assertCalcApprox("ln(e)", 1.0, 0.001);
		}

		@Test
		@DisplayName("2e = 320 (enchanted unit, not Euler)")
		void eAsUnitAfterNumber() throws Exception {
			assertCalc("2e", "320");
		}

		@Test
		@DisplayName("5e = 800 (enchanted unit)")
		void eAsUnitNotEuler() throws Exception {
			assertCalc("5e", "800");
		}

		@Test
		@DisplayName("e + 1 = 3.718... (Euler's number)")
		void eStandaloneInExpression() throws Exception {
			assertCalcApprox("e + 1", 3.71828182, 0.0001);
		}

		@Test
		@DisplayName("pi * e (both constants)")
		void piTimesE() throws Exception {
			assertCalcApprox("pi * e", 8.53973422, 0.001);
		}
	}

	@Nested
	@DisplayName("Factorial")
	class Factorial {
		@Test
		void factorial0() throws Exception {
			assertCalc("0!", "1");
		}

		@Test
		void factorial1() throws Exception {
			assertCalc("1!", "1");
		}

		@Test
		void factorial5() throws Exception {
			assertCalc("5!", "120");
		}

		@Test
		void factorial10() throws Exception {
			assertCalc("10!", "3628800");
		}

		@Test
		void factorial20() throws Exception {
			assertCalc("20!", "2432902008176640000");
		}

		@Test
		void factorialInExpression() throws Exception {
			assertCalc("5! + 1", "121");
		}

		@Test
		void factorialMultiply() throws Exception {
			assertCalc("3! * 2", "12");
		}

		@Test
		void factorialWithParens() throws Exception {
			assertCalc("(3+2)!", "120");
		}

		@Test
		void factorialNegative() {
			assertThrows(EvalException.class, () -> calc("(-5)!"));
		}

		@Test
		void factorialDecimal() {
			assertThrows(EvalException.class, () -> calc("3.5!"));
		}

		@Test
		void factorialTooLarge() {
			assertThrows(EvalException.class, () -> calc("1001!"));
		}
	}

	@Nested
	@DisplayName("Literal")
	class Literal {
		@Test
		void binary() throws Exception {
			assertCalc("0b010", "2");
		}

		@Test
		void binaryUppercase() throws Exception {
			assertCalc("0B1101", "13");
		}

		@Test
		void hex() throws Exception {
			assertCalc("0xa1b", "2587");
		}

		@Test
		void hexUppercase() throws Exception {
			assertCalc("0XFF", "255");
		}

		@Test
		void octal() throws Exception {
			assertCalc("0o511", "329");
		}

		@Test
		void octalUppercase() throws Exception {
			assertCalc("0O77", "63");
		}

		@Test
		void underscoreInLiteral() throws Exception {
			assertCalc("0b1010_0001", "161");
		}

		@Test
		void arithmetic() throws Exception {
			assertCalc("(0b1101! - 0o6_6^0x5) % 0XAaA0B", "607244");
		}

		@Test
		void literalAddition() throws Exception {
			assertCalc("0b10 + 0xA + 0o10", "20");
		}

		@Test
		void zeroBillion() throws Exception {
			assertCalc("0b", "0");
		}

		@Test
		void literalImplicitMultiplication() throws Exception {
			assertCalc("2(0b10)", "4");
		}

		@Test
		void literalInFunction() throws Exception {
			assertCalc("sqrt(0x64)", "10");
		}

		@Test
		void literalMaxFunction() throws Exception {
			assertCalc("max(0b10, 0xFF)", "255");
		}

		@Test
		void literalWithUnit() throws Exception {
			assertCalc("0b10k", "2000");
		}

		@Test
		void literalWithStack() throws Exception {
			assertCalc("0xA * 1s", "640");
		}

		@Test
		void invalidBinaryDigit() {
			assertThrows(EvalException.class, () -> calc("0b102"));
		}

		@Test
		void invalidOctalDigit() {
			assertThrows(EvalException.class, () -> calc("0o78"));
		}
	}

	private void assertCalcResult(String expr, String expectedVal, RadixMode expectedMode) throws EvalException {
		EvalResult res = eval.evaluateResult(expr);
		assertEquals(0, new BigDecimal(expectedVal).compareTo(res.value), expr + " value error");
		assertEquals(expectedMode, res.radixMode, expr + " radix mode error");
	}

	@Nested
	@DisplayName("Bitwise Operators")
	class BitwiseOperators {
		@Test
		void bitwiseAnd() throws Exception {
			assertCalc("0b1010 & 0b1100", "8");
		}

		@Test
		void bitwiseOr() throws Exception {
			assertCalc("0b1010 | 0b0101", "15");
		}

		@Test
		void bitwiseNot() throws Exception {
			assertCalc("~0", "-1");
		}

		@Test
		void bitwiseNotValue() throws Exception {
			assertCalc("~5", "-6");
		}

		@Test
		void bitwiseShiftLeft() throws Exception {
			assertCalc("1 << 4", "16");
		}

		@Test
		void bitwiseShiftRight() throws Exception {
			assertCalc("16 >> 2", "4");
		}

		@Test
		void bitwiseXorFunc() throws Exception {
			assertCalc("xor(0b1010, 0b1100)", "6");
		}

		@Test
		void bitwiseShiftWithHex() throws Exception {
			assertCalc("0x0F << 4", "240");
		}

		@Test
		void chainedBitwiseOr() throws Exception {
			assertCalc("1 | 2 | 4 | 8", "15");
		}

		@Test
		void chainedBitwiseAnd() throws Exception {
			assertCalc("15 & 7 & 3", "3");
		}

		@Test
		void bitwiseWithUnits() throws Exception {
			assertCalc("(1s) & 64", "64");
		}
	}

	@Nested
	@DisplayName("Base Conversions")
	class BaseConversions {
		@Test
		void hexFunc() throws Exception {
			assertCalcResult("hex(255)", "255", RadixMode.HEX);
		}

		@Test
		void binFunc() throws Exception {
			assertCalcResult("bin(10)", "10", RadixMode.BIN);
		}

		@Test
		void octFunc() throws Exception {
			assertCalcResult("oct(63)", "63", RadixMode.OCT);
		}

		@Test
		void hexExpression() throws Exception {
			assertCalcResult("hex(0b1010 + 0o10)", "18", RadixMode.HEX);
		}

		@Test
		void binExpression() throws Exception {
			assertCalcResult("bin(0xFF)", "255", RadixMode.BIN);
		}

		@Test
		void octExpression() throws Exception {
			assertCalcResult("oct(0b111111)", "63", RadixMode.OCT);
		}

		@Test
		void hexZero() throws Exception {
			assertCalcResult("hex(0)", "0", RadixMode.HEX);
		}

		@Test
		void binZero() throws Exception {
			assertCalcResult("bin(0)", "0", RadixMode.BIN);
		}
	}

	@Nested
	@DisplayName("Math Helper Functions")
	class MathHelperFunctions {
		@Test
		void avgTwo() throws Exception {
			assertCalc("avg(10, 20)", "15");
		}

		@Test
		void avgThree() throws Exception {
			assertCalc("avg(10, 20, 30)", "20");
		}

		@Test
		void avgFour() throws Exception {
			assertCalc("avg(100, 200, 300, 400)", "250");
		}

		@Test
		void avgDecimal() throws Exception {
			assertCalc("avg(1, 2)", "1.5");
		}

		@Test
		void pctRatio() throws Exception {
			assertCalc("pct(50, 200)", "25");
		}

		@Test
		void pctQuarter() throws Exception {
			assertCalc("pct(1, 4)", "25");
		}

		@Test
		void pctFull() throws Exception {
			assertCalc("pct(100, 100)", "100");
		}

		@Test
		void gcdBasic() throws Exception {
			assertCalc("gcd(12, 18)", "6");
		}

		@Test
		void gcdCoPrime() throws Exception {
			assertCalc("gcd(17, 13)", "1");
		}

		@Test
		void gcdSame() throws Exception {
			assertCalc("gcd(42, 42)", "42");
		}

		@Test
		void lcmBasic() throws Exception {
			assertCalc("lcm(12, 18)", "36");
		}

		@Test
		void lcmCoPrime() throws Exception {
			assertCalc("lcm(5, 7)", "35");
		}

		@Test
		void clampMiddle() throws Exception {
			assertCalc("clamp(5, 0, 10)", "5");
		}

		@Test
		void clampLower() throws Exception {
			assertCalc("clamp(-5, 0, 10)", "0");
		}

		@Test
		void clampUpper() throws Exception {
			assertCalc("clamp(15, 0, 10)", "10");
		}

		@Test
		void clampExactMin() throws Exception {
			assertCalc("clamp(0, 0, 10)", "0");
		}

		@Test
		void clampExactMax() throws Exception {
			assertCalc("clamp(10, 0, 10)", "10");
		}
	}

	@Nested
	@DisplayName("SkyBlock Tax Functions & Angle Helpers")
	class TaxAndAngleFunctions {
		@Test
		void bzTaxDefault() throws Exception {
			assertCalc("bz(100m)", "98750000");
		}

		@Test
		void bzTaxZero() throws Exception {
			assertCalc("bz(0)", "0");
		}

		@Test
		void ahStandardAuction() throws Exception {
			assertCalc("ah(50m)", "46999955");
		}

		@Test
		void ahBinUnder10m() throws Exception {
			assertCalc("ahbin(5m)", "4899955");
		}

		@Test
		void ahBin10mTo100m() throws Exception {
			assertCalc("ahbin(50m)", "48499955");
		}

		@Test
		void ahBinOver100m() throws Exception {
			assertCalc("ahbin(200m)", "192999955");
		}

		@Test
		void ahBinWith30mDuration() throws Exception {
			assertCalc("ahbin(50m, 0.5)", "48499950");
		}

		@Test
		void ahBinWith59mFraction() throws Exception {
			assertCalc("ahbin(50m, 59/60)", "48499950");
		}

		@Test
		void ahBinWith09hDuration() throws Exception {
			assertCalc("ahbin(50m, 0.9)", "48499950");
		}

		@Test
		void ahBinWith1hDuration() throws Exception {
			assertCalc("ahbin(50m, 1)", "48499980");
		}

		@Test
		void ahBinWith24hDuration() throws Exception {
			assertCalc("ahbin(50m, 24)", "48499650");
		}

		@Test
		void ahBinWith7dDuration() throws Exception {
			assertCalc("ahbin(50m, 168)", "48478400");
		}

		@Test
		void bzFlipperPerkLevels() {
			assertEquals(0, new BigDecimal("98750000")
					.compareTo(ExpressionEvaluator.calculateBzPayout(new BigDecimal("100000000"), 1.25)));
			assertEquals(0, new BigDecimal("98875000")
					.compareTo(ExpressionEvaluator.calculateBzPayout(new BigDecimal("100000000"), 1.125)));
			assertEquals(0, new BigDecimal("99000000")
					.compareTo(ExpressionEvaluator.calculateBzPayout(new BigDecimal("100000000"), 1.0)));
		}

		@Test
		void ahCollectionClaimTaxCapping() {
			BigDecimal net = ExpressionEvaluator.calculateAhPayout(new BigDecimal("1005000"), 6.0, true);
			assertEquals(0, new BigDecimal("989905").compareTo(net));
		}

		@Test
		void rad180() throws Exception {
			assertCalcApprox("rad(180)", Math.PI, 0.0001);
		}

		@Test
		void degPi() throws Exception {
			assertCalcApprox("deg(pi)", 180.0, 0.0001);
		}

		@Test
		void fmtPassThrough() throws Exception {
			assertCalcResult("fmt(1500000)", "1500000", RadixMode.SHORTHAND);
		}

		@Test
		void verifyAllAhDurationFeeMilestones() {
			int[] hours = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 48,
					72, 96, 120, 144, 168, 192, 216, 240, 264, 288, 312, 336};
			double[] expectedFees = {20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 120, 140, 160, 180, 200, 220, 240,
					260, 280, 300, 320, 350, 1200, 3000, 7200, 12000, 16800, 21600, 26400, 31200, 36000, 40800, 45600,
					50400, 55200};

			for (int i = 0; i < hours.length; i++) {
				assertEquals(expectedFees[i], ExpressionEvaluator.calculateAhDurationFee(hours[i]),
						"Failed for hour " + hours[i]);
			}
		}
	}

	@Nested
	@DisplayName("Reserved Variables Exception Safety")
	class ReservedVariables {
		@Test
		void settingReservedVariableThrowsException() {
			String[] reserved = {"purse", "bank", "hp", "maxhealth", "def", "mana", "vitality", "speed", "mp",
					"farming", "mining", "zombieslayer", "hotf", "htier", "hotftokens", "whispers"};
			for (String var : reserved) {
				assertThrows(EvalException.class, () -> eval.setVariable(var, new BigDecimal("100")));
				assertThrows(EvalException.class, () -> eval.setVariable("$" + var, "100"));
			}
		}
	}

	@Nested
	@DisplayName("SkyBlock XP Tables and Perk Functions")
	class XpAndPerkFunctions {
		@Test
		void testSkillXpEvaluations() throws Exception {
			assertCalc("skillxp(50)", "55172425");
			assertCalc("skillxp(40, 50)", "29650000");
			assertCalc("skilltable(60)", "111672425");
			assertCalc("skill_xp(1)", "50");
			assertCalc("huntingxp(50)", "55172425");
			assertCalc("huntingxp(60)", "55172425");
			assertCalc("huntingtable(40, 50)", "29650000");
		}

		@Test
		void testCataXpEvaluations() throws Exception {
			assertCalc("cataxp(50)", "569809640");
			assertCalc("cataxp(35, 50)", "556550000");
			assertCalc("cata_xp(1)", "50");
			assertCalc("catatable(5)", "625");
			assertCalc("cataxp(51)", "769809640");
			assertCalc("cataxp(60)", "2569809640");
		}

		@Test
		void testRunecraftingAndSocialXpEvaluations() throws Exception {
			assertCalc("runecraftingxp(25)", "94450");
			assertCalc("runecraftingxp(10, 20)", "27475");
			assertCalc("runetable(11)", "3510");
			assertCalc("socialxp(25)", "272800");
			assertCalc("social_xp(10)", "7550");
			assertCalc("socialtable(15, 20)", "65500");
		}

		@Test
		void testSlayerXpEvaluations() throws Exception {
			assertCalc("slayerxp(9)", "1000000");
			assertCalc("slayerxp(7, 9)", "900000");
			assertCalc("slayer_xp(1)", "5");
			assertCalc("slayerxp(2)", "15");
			assertCalc("slayerxp(3)", "200");
			assertCalc("slayerxp(4)", "1000");
			assertCalc("slayertable(3)", "200");
			assertCalc("zombiexp(2)", "15");
			assertCalc("wolfxp(4)", "1000");
			assertCalc("spiderxp(2)", "25");
			assertCalc("spiderxp(3)", "200");
			assertCalc("tarantulaxp(4)", "1000");
			assertCalc("emanxp(9)", "1000000");
			assertCalc("emanxp(4)", "1500");
			assertCalc("voidgloomxp(3)", "250");
			assertCalc("blazexp(3, 4)", "1250");
			assertCalc("infernoxp(2)", "30");

			// Unified slayerxp with boss name argument
			assertCalc("slayerxp(\"spider\", 2)", "25");
			assertCalc("slayerxp(spider, 2)", "25");
			assertCalc("slayerxp(\"spider\", 2, 3)", "175");
			assertCalc("slayerxp(tara, 9)", "1000000");
			assertCalc("slayerxp(\"eman\", 4)", "1500");
			assertCalc("slayerxp(eman, 3, 4)", "1250");
			assertCalc("slayerxp(\"blaze\", 4)", "1500");
			assertCalc("slayerxp(\"vampire\", 5)", "2400");
			assertCalc("slayerxp(vamp, 3, 5)", "2160");
		}

		@Test
		void testVampireSlayerXpEvaluations() throws Exception {
			assertCalc("vampirexp(5)", "2400");
			assertCalc("vampirexp(9)", "2400");
			assertCalc("vampire_xp(3, 5)", "2160");
			assertCalc("vampiretable(1)", "20");
			assertCalc("vampslayerxp(4)", "840");
		}

		@Test
		void testPerkEvaluationsFallback() throws Exception {
			assertCalc("perk(mining_speed)", "0");
			assertCalc("hotmperk(mining_speed)", "0");
			assertCalc("hperk(mining_fortune)", "0");
		}

		@Test
		void testAllStorageUnits() throws Exception {
			assertCalc("2s", "128");
			assertCalc("2st", "128");
			assertCalc("3stack", "192");
			assertCalc("5stacks", "320");
			assertCalc("1h", "1728");
			assertCalc("2sc", "3456");
			assertCalc("3dc", "10368");
			assertCalc("2eb", "5760");
		}

		@Test
		void testAllStatVariableLookupsSafelyResolveNullWhenDisconnected() {
			assertNull(PlayerStatLookup.lookupPlayerStat("hunting"));
			assertNull(PlayerStatLookup.lookupPlayerStat("hunt"));
			assertNull(PlayerStatLookup.lookupPlayerStat("huntlvl"));
			assertNull(PlayerStatLookup.lookupPlayerStat("huntingxp"));
			assertNull(PlayerStatLookup.lookupPlayerStat("huntxp"));
			assertNull(PlayerStatLookup.lookupPlayerStat("whispers"));
			assertNull(PlayerStatLookup.lookupPlayerStat("forestwhispers"));
			assertNull(PlayerStatLookup.lookupPlayerStat("fwhispers"));
			assertNull(PlayerStatLookup.lookupPlayerStat("desertwhispers"));
			assertNull(PlayerStatLookup.lookupPlayerStat("dwhisper"));
			assertNull(PlayerStatLookup.lookupPlayerStat("hotm"));
			assertNull(PlayerStatLookup.lookupPlayerStat("hotmtier"));
			assertNull(PlayerStatLookup.lookupPlayerStat("hotmtokens"));
			assertNull(PlayerStatLookup.lookupPlayerStat("hotf"));
			assertNull(PlayerStatLookup.lookupPlayerStat("htier"));
			assertNull(PlayerStatLookup.lookupPlayerStat("hotftokens"));
			assertNull(PlayerStatLookup.lookupPlayerStat("vitality"));
			assertNull(PlayerStatLookup.lookupPlayerStat("maxvitality"));
			assertNull(PlayerStatLookup.lookupPlayerStat("coins"));
			assertNull(PlayerStatLookup.lookupPlayerStat("coin"));
			assertNull(PlayerStatLookup.lookupPlayerStat("money"));
			assertNull(PlayerStatLookup.lookupPlayerStat("defence"));
			assertNull(PlayerStatLookup.lookupPlayerStat("power"));
			assertNull(PlayerStatLookup.lookupPlayerStat("farmlvl"));
			assertNull(PlayerStatLookup.lookupPlayerStat("minelvl"));
			assertNull(PlayerStatLookup.lookupPlayerStat("cmbtlvl"));
			assertNull(PlayerStatLookup.lookupPlayerStat("voidgloomxp"));
		}
	}
}
