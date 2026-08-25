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

package com.rijz.notenoughcalculator.core.skyblock;

import java.math.BigDecimal;
import java.util.Locale;

public final class SlayerXpTable {

	private SlayerXpTable() {
	}

	private static final long[] ZOMBIE_WOLF_XP_TABLE = {0L, 5L, 15L, 200L, 1000L, 5000L, 20000L, 100000L, 400000L,
			1000000L};

	private static final long[] SPIDER_XP_TABLE = {0L, 5L, 25L, 200L, 1000L, 5000L, 20000L, 100000L, 400000L, 1000000L};

	private static final long[] ENDERMAN_BLAZE_XP_TABLE = {0L, 10L, 30L, 250L, 1500L, 5000L, 20000L, 100000L, 400000L,
			1000000L};

	private static final long[] VAMPIRE_XP_TABLE = {0L, 20L, 75L, 240L, 840L, 2400L};

	public static BigDecimal getSlayerXp(String boss, int toLevel) {
		long[] table = getTableForBoss(boss);
		return getFromTable(table, toLevel);
	}

	public static BigDecimal getSlayerXpBetween(String boss, int fromLevel, int toLevel) {
		long[] table = getTableForBoss(boss);
		return getBetweenFromTable(table, fromLevel, toLevel);
	}

	public static BigDecimal getSlayerXp(int toLevel) {
		return getFromTable(ZOMBIE_WOLF_XP_TABLE, toLevel);
	}

	public static BigDecimal getSlayerXpBetween(int fromLevel, int toLevel) {
		return getBetweenFromTable(ZOMBIE_WOLF_XP_TABLE, fromLevel, toLevel);
	}

	public static BigDecimal getSpiderSlayerXp(int toLevel) {
		return getFromTable(SPIDER_XP_TABLE, toLevel);
	}

	public static BigDecimal getSpiderSlayerXpBetween(int fromLevel, int toLevel) {
		return getBetweenFromTable(SPIDER_XP_TABLE, fromLevel, toLevel);
	}

	public static BigDecimal getEndermanSlayerXp(int toLevel) {
		return getFromTable(ENDERMAN_BLAZE_XP_TABLE, toLevel);
	}

	public static BigDecimal getEndermanSlayerXpBetween(int fromLevel, int toLevel) {
		return getBetweenFromTable(ENDERMAN_BLAZE_XP_TABLE, fromLevel, toLevel);
	}

	public static BigDecimal getBlazeSlayerXp(int toLevel) {
		return getFromTable(ENDERMAN_BLAZE_XP_TABLE, toLevel);
	}

	public static BigDecimal getBlazeSlayerXpBetween(int fromLevel, int toLevel) {
		return getBetweenFromTable(ENDERMAN_BLAZE_XP_TABLE, fromLevel, toLevel);
	}

	public static BigDecimal getVampireSlayerXp(int toLevel) {
		return getFromTable(VAMPIRE_XP_TABLE, toLevel);
	}

	public static BigDecimal getVampireSlayerXpBetween(int fromLevel, int toLevel) {
		return getBetweenFromTable(VAMPIRE_XP_TABLE, fromLevel, toLevel);
	}

	public static long[] getTableForBoss(String boss) {
		if (boss == null || boss.trim().isEmpty()) {
			return ZOMBIE_WOLF_XP_TABLE;
		}
		String clean = boss.trim().toLowerCase(Locale.ROOT);
		return switch (clean) {
			case "spider", "tara", "tarantula", "spiderslayer" -> SPIDER_XP_TABLE;
			case "eman", "enderman", "voidgloom", "blaze", "inferno" -> ENDERMAN_BLAZE_XP_TABLE;
			case "vampire", "vamp", "riftstalker", "bloodfiend", "vampslayer" -> VAMPIRE_XP_TABLE;
			default -> ZOMBIE_WOLF_XP_TABLE;
		};
	}

	private static BigDecimal getFromTable(long[] table, int toLevel) {
		if (toLevel <= 0) {
			return BigDecimal.ZERO;
		}
		if (toLevel >= table.length) {
			toLevel = table.length - 1;
		}
		return BigDecimal.valueOf(table[toLevel]);
	}

	private static BigDecimal getBetweenFromTable(long[] table, int fromLevel, int toLevel) {
		if (fromLevel < 0) {
			fromLevel = 0;
		}
		if (toLevel < 0) {
			toLevel = 0;
		}
		if (fromLevel >= toLevel) {
			return BigDecimal.ZERO;
		}

		BigDecimal startXp = getFromTable(table, fromLevel);
		BigDecimal targetXp = getFromTable(table, toLevel);
		return targetXp.subtract(startXp);
	}
}
