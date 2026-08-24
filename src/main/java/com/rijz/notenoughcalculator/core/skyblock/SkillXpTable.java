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
import java.util.function.IntFunction;

public final class SkillXpTable {

    private SkillXpTable() {
    }

    private static final long[] SKILL_XP_TABLE = {
            0L,
            50L, 175L, 375L, 675L, 1175L, 1925L, 2925L, 4425L, 6425L, 9925L,
            14925L, 22425L, 32425L, 47425L, 67425L, 97425L, 147425L, 222425L, 322425L, 522425L,
            822425L, 1222425L, 1722425L, 2322425L, 3022425L, 3822425L, 4722425L, 5722425L, 6822425L, 8022425L,
            9322425L, 10722425L, 12222425L, 13822425L, 15522425L, 17322425L, 19222425L, 21222425L, 23322425L, 25522425L,
            27822425L, 30222425L, 32722425L, 35322425L, 38072425L, 40972425L, 44072425L, 47472425L, 51172425L,
            55172425L,
            59472425L, 64072425L, 68972425L, 74172425L, 79672425L, 85472425L, 91572425L, 97972425L, 104672425L,
            111672425L
    };

    private static final long[] RUNECRAFTING_XP_TABLE = {
            0L,
            50L, 150L, 275L, 435L, 635L, 885L, 1200L, 1600L, 2100L, 2725L,
            3510L, 4510L, 5760L, 7325L, 9325L, 11825L, 14950L, 18950L, 23950L, 30200L,
            38050L, 47850L, 60100L, 75400L, 94450L
    };

    private static final long[] SOCIAL_XP_TABLE = {
            0L,
            50L, 150L, 300L, 550L, 1050L, 1800L, 2800L, 4050L, 5550L, 7550L,
            10050L, 13050L, 16800L, 21300L, 27300L, 35300L, 45300L, 57800L, 72800L, 92800L,
            117800L, 147800L, 182800L, 222800L, 272800L
    };

    private static final long[] CATACOMBS_XP_TABLE = {
            0L,
            50L, 125L, 235L, 395L, 625L, 955L, 1425L, 2095L, 3045L, 4385L,
            6275L, 8940L, 12700L, 17960L, 25340L, 35640L, 50040L, 70040L, 97640L, 135640L,
            188140L, 259640L, 356640L, 488640L, 668640L, 911640L, 1239640L, 1684640L, 2284640L, 3084640L,
            4149640L, 5559640L, 7459640L, 9959640L, 13259640L, 17559640L, 23159640L, 30359640L, 39559640L, 51559640L,
            66559640L, 85559640L, 109559640L, 139559640L, 177559640L, 225559640L, 285559640L, 360559640L, 453559640L,
            569809640L
    };

    private static final BigDecimal CATA_OVERFLOW_STEP = new BigDecimal("200000000");

    public static BigDecimal getSkillXp(int toLevel) {
        if (toLevel <= 0) {
            return BigDecimal.ZERO;
        }
        if (toLevel >= SKILL_XP_TABLE.length) {
            toLevel = SKILL_XP_TABLE.length - 1;
        }
        return BigDecimal.valueOf(SKILL_XP_TABLE[toLevel]);
    }

    public static BigDecimal getSkillXpBetween(int fromLevel, int toLevel) {
        return calculateBetween(SkillXpTable::getSkillXp, fromLevel, toLevel);
    }

    public static BigDecimal getRunecraftingXp(int toLevel) {
        if (toLevel <= 0) {
            return BigDecimal.ZERO;
        }
        if (toLevel >= RUNECRAFTING_XP_TABLE.length) {
            toLevel = RUNECRAFTING_XP_TABLE.length - 1;
        }
        return BigDecimal.valueOf(RUNECRAFTING_XP_TABLE[toLevel]);
    }

    public static BigDecimal getRunecraftingXpBetween(int fromLevel, int toLevel) {
        return calculateBetween(SkillXpTable::getRunecraftingXp, fromLevel, toLevel);
    }

    public static BigDecimal getSocialXp(int toLevel) {
        if (toLevel <= 0) {
            return BigDecimal.ZERO;
        }
        if (toLevel >= SOCIAL_XP_TABLE.length) {
            toLevel = SOCIAL_XP_TABLE.length - 1;
        }
        return BigDecimal.valueOf(SOCIAL_XP_TABLE[toLevel]);
    }

    public static BigDecimal getSocialXpBetween(int fromLevel, int toLevel) {
        return calculateBetween(SkillXpTable::getSocialXp, fromLevel, toLevel);
    }

    public static BigDecimal getCataXp(int toLevel) {
        if (toLevel <= 0) {
            return BigDecimal.ZERO;
        }
        if (toLevel <= 50) {
            return BigDecimal.valueOf(CATACOMBS_XP_TABLE[toLevel]);
        }
        BigDecimal base = BigDecimal.valueOf(CATACOMBS_XP_TABLE[50]);
        BigDecimal overflow = CATA_OVERFLOW_STEP.multiply(BigDecimal.valueOf((long) toLevel - 50));
        return base.add(overflow);
    }

    public static BigDecimal getCataXpBetween(int fromLevel, int toLevel) {
        return calculateBetween(SkillXpTable::getCataXp, fromLevel, toLevel);
    }

    public static BigDecimal getHuntingXp(int toLevel) {
        if (toLevel <= 0) {
            return BigDecimal.ZERO;
        }
        if (toLevel > 50) {
            toLevel = 50;
        }
        return BigDecimal.valueOf(SKILL_XP_TABLE[toLevel]);
    }

    public static BigDecimal getHuntingXpBetween(int fromLevel, int toLevel) {
        return calculateBetween(SkillXpTable::getHuntingXp, fromLevel, toLevel);
    }

    private static BigDecimal calculateBetween(IntFunction<BigDecimal> xpFunction, int fromLevel, int toLevel) {
        if (fromLevel < 0) {
            fromLevel = 0;
        }
        if (toLevel < 0) {
            toLevel = 0;
        }
        if (fromLevel >= toLevel) {
            return BigDecimal.ZERO;
        }
        return xpFunction.apply(toLevel).subtract(xpFunction.apply(fromLevel));
    }
}
