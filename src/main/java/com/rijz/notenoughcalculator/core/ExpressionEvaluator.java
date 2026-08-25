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
import com.rijz.notenoughcalculator.core.parser.ExpressionParser;
import com.rijz.notenoughcalculator.core.parser.ExpressionTokenizer;
import com.rijz.notenoughcalculator.core.parser.ParseResult;
import com.rijz.notenoughcalculator.core.parser.Token;
import com.rijz.notenoughcalculator.core.parser.TokenKind;
import com.rijz.notenoughcalculator.core.skyblock.SkyblockTaxCalculator;

import net.minecraft.client.resources.language.I18n;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ExpressionEvaluator {

	public enum RadixMode {
		NONE, DEFAULT, HEX, BIN, OCT, SHORTHAND
	}

	public static class EvalResult {
		public final BigDecimal value;
		public final RadixMode radixMode;

		public EvalResult(BigDecimal value) {
			this(value, RadixMode.NONE);
		}

		public EvalResult(BigDecimal value, RadixMode radixMode) {
			this.value = value;
			this.radixMode = radixMode != null ? radixMode : RadixMode.NONE;
		}
	}

	public static class EvalException extends Exception {
		private final int position;

		public EvalException(String msg, int pos) {
			super(msg);
			this.position = pos;
		}

		public int getPosition() {
			return position;
		}
	}

	public static final Map<String, BigDecimal> UNITS = Map.ofEntries(Map.entry("k", new BigDecimal("1000")),
			Map.entry("m", new BigDecimal("1000000")), Map.entry("b", new BigDecimal("1000000000")),
			Map.entry("t", new BigDecimal("1000000000000")), Map.entry("s", new BigDecimal("64")),
			Map.entry("st", new BigDecimal("64")), Map.entry("stack", new BigDecimal("64")),
			Map.entry("stacks", new BigDecimal("64")), Map.entry("e", new BigDecimal("160")),
			Map.entry("h", new BigDecimal("1728")), Map.entry("sc", new BigDecimal("1728")),
			Map.entry("dc", new BigDecimal("3456")), Map.entry("eb", new BigDecimal("2880")));

	public static final Set<String> MATH_FUNCTIONS = Set.of("sqrt", "abs", "floor", "ceil", "round", "log", "ln", "sin",
			"cos", "tan", "min", "max", "gcd", "lcm", "clamp", "avg", "xor", "fmt", "rad", "deg");

	public static final Set<String> RADIX_FUNCTIONS = Set.of("hex", "bin", "oct", "pct");

	public static final Set<String> MARKET_QUERY_FUNCTIONS = Set.of("bzb", "bzbuy", "bzs", "bzsell", "bzm", "bzmargin",
			"lb", "lowestbin", "lba", "lowestbinavg", "npc", "npcsell", "motes", "motessell", "price", "sack",
			"sackcount");

	public static final Set<String> TAX_FUNCTIONS = Set.of("bz", "ah", "ahbin");

	public static final Set<String> MARKET_FUNCTIONS;
	static {
		Set<String> allMarket = new HashSet<>(MARKET_QUERY_FUNCTIONS);
		allMarket.addAll(TAX_FUNCTIONS);
		MARKET_FUNCTIONS = Set.copyOf(allMarket);
	}

	public static final Set<String> PROGRESSION_FUNCTIONS = Set.of("skillxp", "skill_xp", "skilltable", "huntingxp",
			"hunting_xp", "huntingtable", "runecraftingxp", "runecrafting_xp", "runetable", "socialxp", "social_xp",
			"socialtable", "cataxp", "cata_xp", "catatable", "cxp_table", "slayerxp", "slayer_xp", "slayertable",
			"zombiexp", "wolfxp", "svenxp", "revxp", "revenantxp", "spiderxp", "spider_xp", "tarantulaxp",
			"tarantula_xp", "spidertable", "emanxp", "voidgloomxp", "blazexp", "infernoxp", "endermanxp", "vampirexp",
			"vampire_xp", "vampiretable", "vampslayerxp", "riftstalkerxp", "perk", "hotmperk", "hperk");

	public static final Set<String> FUNCTIONS;
	static {
		Set<String> all = new HashSet<>();
		all.addAll(MATH_FUNCTIONS);
		all.addAll(RADIX_FUNCTIONS);
		all.addAll(MARKET_FUNCTIONS);
		all.addAll(PROGRESSION_FUNCTIONS);
		FUNCTIONS = Set.copyOf(all);
	}

	public static final String UNITS_REGEX = buildUnitsRegex();
	public static final String FUNCTIONS_REGEX = buildFunctionsRegex();

	private static String buildUnitsRegex() {
		List<String> sortedUnits = new ArrayList<>(UNITS.keySet());
		sortedUnits.sort((a, b) -> Integer.compare(b.length(), a.length()));
		StringBuilder sb = new StringBuilder("(?:");
		for (int i = 0; i < sortedUnits.size(); i++) {
			if (i > 0)
				sb.append("|");
			sb.append(Pattern.quote(sortedUnits.get(i)));
		}
		sb.append(")");
		return sb.toString();
	}

	private static String buildFunctionsRegex() {
		List<String> sortedFuncs = new ArrayList<>(FUNCTIONS);
		sortedFuncs.sort((a, b) -> Integer.compare(b.length(), a.length()));
		StringBuilder sb = new StringBuilder("(");
		for (int i = 0; i < sortedFuncs.size(); i++) {
			if (i > 0)
				sb.append("|");
			sb.append(Pattern.quote(sortedFuncs.get(i)));
		}
		sb.append(")");
		return sb.toString();
	}

	public static final Set<String> BUILTIN_VARIABLES = Set.of("ans", "pi", "e", "purse", "p", "coins", "coin", "money",
			"bank", "b", "bankcoins", "personalbank", "pbank", "coopbank", "cbank", "bits", "bt", "bit", "motes", "mt",
			"mote", "copper", "cop", "sowdust", "sdust", "kernels", "kern", "kernel", "northstars", "nstars", "ns",
			"northstar", "star", "stars", "gems", "gem", "soulflow", "sflow", "sf", "sblevel", "sblvl", "sb",
			"skyblocklevel", "sblev", "level", "sblevelprogress", "sbprog", "sblevelprog", "levelprogress", "levelprog",
			"rep", "reputation", "mithrilpowder", "mithril", "mpowder", "mith", "mpowd", "gemstonepowder", "gemstone",
			"gpowder", "gpowd", "glacitepowder", "glacite", "glpowder", "glac", "glpowd", "totalmithrilpowder",
			"totalmithril", "totmithril", "totmpowder", "totmith", "totalgemstonepowder", "totalgemstone",
			"totgemstone", "totgpowder", "totgem", "totalglacitepowder", "totalglacite", "totglacite", "totglpowder",
			"totglac", "hotm", "hotmtier", "hotmtokens", "tokens", "token", "hotf", "hotftier", "htier", "hotftokens",
			"htokens", "htoken", "whispers", "whisper", "whisp", "forestwhispers", "fwhispers", "fwhisper",
			"desertwhispers", "dwhispers", "dwhisper", "cata", "catacombs", "catacombslevel", "catalvl", "classlevel",
			"classlvl", "dclass", "dungeonclass", "partysize", "party", "dungeonparty", "witheressence", "wither",
			"wessence", "w", "undeadessence", "undead", "uessence", "u", "dragonessence", "dragon", "dessence", "d",
			"spideressence", "spider", "spessence", "sp", "iceessence", "ice", "iessence", "i", "diamondessence",
			"diamond", "diessence", "di", "goldessence", "gold", "gessence", "g", "crimsonessence", "crimson",
			"cessence", "c", "forestessence", "forest", "foressence", "fe", "fossilessence", "fossil", "fossessence",
			"foss", "sungeckoessence", "sungecko", "geckoessence", "gecko", "sungeck", "safariessence", "safari",
			"safessence", "saf", "petlvl", "petlevel", "pet", "petxp", "pxp", "petexperience", "bestiary",
			"bestiarylvl", "bestiarylevel", "best", "trophyfish", "trophyfishcount", "tfish", "diamondtrophy",
			"diamondtrophyfish", "dtrophy", "goldtrophy", "goldtrophyfish", "gtrophy", "silvertrophy",
			"silvertrophyfish", "strophy", "bronzetrophy", "bronzetrophyfish", "btrophy", "mp", "magicalpower",
			"accessorypower", "power", "hp", "health", "maxhp", "maxhealth", "mhp", "def", "defense", "defence", "mana",
			"intel", "intelligence", "maxmana", "mmana", "maxintel", "maxintelligence", "overflowmana", "ofmana", "vit",
			"vitality", "maxvitality", "mvit", "spd", "speed", "xplevel", "xplvl", "xp", "farming", "farminglvl",
			"farmlvl", "farm", "farmingxp", "farmxp", "mining", "mininglvl", "minelvl", "mine", "miningxp", "minexp",
			"combat", "combatlvl", "cmbtlvl", "cmbt", "combatxp", "cmbtxp", "foraging", "foraginglvl", "foraglvl",
			"forag", "foragingxp", "foragxp", "fishing", "fishinglvl", "fishlvl", "fish", "fishingxp", "fishxp",
			"enchanting", "enchantinglvl", "enchlvl", "ench", "enchantingxp", "enchxp", "alchemy", "alchemylvl",
			"alchlvl", "alch", "alchemyxp", "alchxp", "taming", "taminglvl", "tamelvl", "tame", "tamingxp", "tamexp",
			"carpentry", "carpentrylvl", "carplvl", "carp", "carpentryxp", "carpxp", "runecrafting", "runecraftinglvl",
			"runelvl", "rune", "runecraftingxp", "runexp", "social", "sociallvl", "soclvl", "soc", "socialxp", "socxp",
			"hunting", "huntinglvl", "huntlvl", "hunt", "huntingxp", "huntxp", "zombieslayer", "zombieslayerxp", "rev",
			"revxp", "revslayer", "zombie", "zombiexp", "spiderslayer", "spiderslayerxp", "tara", "taraxp",
			"taraslayer", "spiderxp", "taralvl", "spiderlvl", "wolfslayer", "wolfslayerxp", "sven", "svenxp",
			"svenslayer", "wolf", "wolfxp", "endermanslayer", "endermanslayerxp", "eman", "emanxp", "emanslayer",
			"enderman", "endermanxp", "voidgloom", "voidgloomxp", "blazeslayer", "blazeslayerxp", "blaze", "blazexp",
			"inferno", "infernoxp", "vampireslayer", "vampireslayerxp", "vamp", "vampxp", "riftstalker",
			"riftstalkerxp");

	private static final int MAX_HISTORY = 15;

	private final MathContext mc;
	private final Map<String, BigDecimal> variables;
	private final List<String> history;
	private BigDecimal lastAnswer;

	public ExpressionEvaluator() {
		CalculatorConfig config = CalculatorConfig.getInstance();
		this.mc = new MathContext(Math.max(config.decimalPrecision, 50), RoundingMode.HALF_UP);
		this.variables = new HashMap<>();
		this.history = new ArrayList<>();
		this.lastAnswer = BigDecimal.ZERO;
	}

	// Constructor for isolated testing outside Minecraft runtime
	public ExpressionEvaluator(int precision) {
		this.mc = new MathContext(Math.max(precision, 50), RoundingMode.HALF_UP);
		this.variables = new HashMap<>();
		this.history = new ArrayList<>();
		this.lastAnswer = BigDecimal.ZERO;
	}

	public static String tr(String key, Object... args) {
		try {
			return I18n.get(key, args);
		} catch (Exception | NoClassDefFoundError e) {
			if (args.length > 0) {
				return String.format(key.replace("%s", "%s").replace("%d", "%s"), args);
			}
			return key;
		}
	}

	public EvalResult evaluateQuietResult(String expr) throws EvalException {
		if (expr == null || expr.trim().isEmpty()) {
			throw new EvalException(tr("notenoughcalculator.error.empty_expression"), 0);
		}

		List<Token> tokens = ExpressionTokenizer.tokenize(expr, lastAnswer);
		ExpressionParser parser = new ExpressionParser(mc, variables, this::setVariableDirect);
		ParseResult parseRes = parser.parse(tokens);

		if (parseRes.nextPos < tokens.size() && tokens.get(parseRes.nextPos).kind != TokenKind.EOF) {
			Token leftover = tokens.get(parseRes.nextPos);
			throw new EvalException(tr("notenoughcalculator.error.unexpected_token", leftover.value), leftover.pos);
		}

		BigDecimal result = parseRes.value;
		lastAnswer = result;

		return new EvalResult(result, parseRes.radixMode);
	}

	public BigDecimal evaluateQuiet(String expr) throws EvalException {
		return evaluateQuietResult(expr).value;
	}

	public EvalResult evaluateResult(String expr) throws EvalException {
		if (expr == null || expr.trim().isEmpty()) {
			throw new EvalException(tr("notenoughcalculator.error.empty_expression"), 0);
		}

		List<Token> tokens = ExpressionTokenizer.tokenize(expr, lastAnswer);
		ExpressionParser parser = new ExpressionParser(mc, variables, this::setVariableDirect);
		ParseResult parseRes = parser.parse(tokens);

		if (parseRes.nextPos < tokens.size() && tokens.get(parseRes.nextPos).kind != TokenKind.EOF) {
			Token leftover = tokens.get(parseRes.nextPos);
			throw new EvalException(tr("notenoughcalculator.error.unexpected_token", leftover.value), leftover.pos);
		}

		BigDecimal result = parseRes.value;
		lastAnswer = result;

		if (history.isEmpty() || !history.get(history.size() - 1).equals(expr)) {
			history.add(expr);
			while (history.size() > MAX_HISTORY) {
				history.remove(0);
			}
		}

		return new EvalResult(result, parseRes.radixMode);
	}

	public BigDecimal evaluate(String expr) throws EvalException {
		return evaluateResult(expr).value;
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

	public static boolean isReservedVariable(String name) {
		if (name == null || name.trim().isEmpty())
			return false;
		String cleanName = name.trim().toLowerCase();
		if (cleanName.startsWith("$")) {
			cleanName = cleanName.substring(1);
		}
		return BUILTIN_VARIABLES.contains(cleanName) || FUNCTIONS.contains(cleanName) || UNITS.containsKey(cleanName);
	}

	public void setVariableDirect(String name, BigDecimal value) {
		if (name != null) {
			String cleanName = name.toLowerCase().startsWith("$")
					? name.substring(1).toLowerCase()
					: name.toLowerCase();
			variables.put(cleanName, value);
		}
	}

	public BigDecimal setVariable(String name, BigDecimal value) throws EvalException {
		String cleanName = name.toLowerCase().startsWith("$") ? name.substring(1).toLowerCase() : name.toLowerCase();
		if (isReservedVariable(cleanName)) {
			throw new EvalException(tr("notenoughcalculator.error.reserved_variable", cleanName), 0);
		}
		variables.put(cleanName, value);
		return value;
	}

	public BigDecimal setVariable(String name, String expr) throws EvalException {
		String cleanName = name.toLowerCase().startsWith("$") ? name.substring(1).toLowerCase() : name.toLowerCase();
		if (isReservedVariable(cleanName)) {
			throw new EvalException(tr("notenoughcalculator.error.reserved_variable", cleanName), 0);
		}
		BigDecimal value = evaluateQuiet(expr);
		variables.put(cleanName, value);
		return value;
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
			sb.append(tr("notenoughcalculator.variable.item_format", key,
					ResultFormatter.formatWithCommas(variables.get(key)))).append("\n");
		}
		return sb.toString().trim();
	}
}
