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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlayerXpTableTest {

    @Test
    @DisplayName("Verify Zombie (Revenant) and Wolf (Sven) Slayer XP milestones")
    void testZombieWolfSlayerXpMilestones() {
        assertEquals(0, new BigDecimal("0").compareTo(SlayerXpTable.getSlayerXp(0)));
        assertEquals(0, new BigDecimal("5").compareTo(SlayerXpTable.getSlayerXp(1)));
        assertEquals(0, new BigDecimal("15").compareTo(SlayerXpTable.getSlayerXp(2)));
        assertEquals(0, new BigDecimal("200").compareTo(SlayerXpTable.getSlayerXp(3)));
        assertEquals(0, new BigDecimal("1000").compareTo(SlayerXpTable.getSlayerXp(4)));
        assertEquals(0, new BigDecimal("5000").compareTo(SlayerXpTable.getSlayerXp(5)));
        assertEquals(0, new BigDecimal("20000").compareTo(SlayerXpTable.getSlayerXp(6)));
        assertEquals(0, new BigDecimal("100000").compareTo(SlayerXpTable.getSlayerXp(7)));
        assertEquals(0, new BigDecimal("400000").compareTo(SlayerXpTable.getSlayerXp(8)));
        assertEquals(0, new BigDecimal("1000000").compareTo(SlayerXpTable.getSlayerXp(9)));
        assertEquals(0, new BigDecimal("1000000").compareTo(SlayerXpTable.getSlayerXp(10)));
        assertEquals(0, new BigDecimal("900000").compareTo(SlayerXpTable.getSlayerXpBetween(7, 9)));
        assertEquals(0, new BigDecimal("800").compareTo(SlayerXpTable.getSlayerXpBetween(3, 4)));
    }

    @Test
    @DisplayName("Verify Spider (Tarantula) Slayer XP milestones (Lvl 2 = 25 XP)")
    void testSpiderSlayerXpMilestones() {
        assertEquals(0, new BigDecimal("0").compareTo(SlayerXpTable.getSpiderSlayerXp(0)));
        assertEquals(0, new BigDecimal("5").compareTo(SlayerXpTable.getSpiderSlayerXp(1)));
        assertEquals(0, new BigDecimal("25").compareTo(SlayerXpTable.getSpiderSlayerXp(2)));
        assertEquals(0, new BigDecimal("200").compareTo(SlayerXpTable.getSpiderSlayerXp(3)));
        assertEquals(0, new BigDecimal("1000").compareTo(SlayerXpTable.getSpiderSlayerXp(4)));
        assertEquals(0, new BigDecimal("5000").compareTo(SlayerXpTable.getSpiderSlayerXp(5)));
        assertEquals(0, new BigDecimal("20000").compareTo(SlayerXpTable.getSpiderSlayerXp(6)));
        assertEquals(0, new BigDecimal("100000").compareTo(SlayerXpTable.getSpiderSlayerXp(7)));
        assertEquals(0, new BigDecimal("400000").compareTo(SlayerXpTable.getSpiderSlayerXp(8)));
        assertEquals(0, new BigDecimal("1000000").compareTo(SlayerXpTable.getSpiderSlayerXp(9)));
        assertEquals(0, new BigDecimal("175").compareTo(SlayerXpTable.getSpiderSlayerXpBetween(2, 3)));
    }

    @Test
    @DisplayName("Verify Enderman (Voidgloom) and Blaze (Inferno) Slayer XP milestones")
    void testEndermanBlazeSlayerXpMilestones() {
        assertEquals(0, new BigDecimal("0").compareTo(SlayerXpTable.getEndermanSlayerXp(0)));
        assertEquals(0, new BigDecimal("10").compareTo(SlayerXpTable.getEndermanSlayerXp(1)));
        assertEquals(0, new BigDecimal("30").compareTo(SlayerXpTable.getEndermanSlayerXp(2)));
        assertEquals(0, new BigDecimal("250").compareTo(SlayerXpTable.getEndermanSlayerXp(3)));
        assertEquals(0, new BigDecimal("1500").compareTo(SlayerXpTable.getEndermanSlayerXp(4)));
        assertEquals(0, new BigDecimal("5000").compareTo(SlayerXpTable.getEndermanSlayerXp(5)));
        assertEquals(0, new BigDecimal("20000").compareTo(SlayerXpTable.getEndermanSlayerXp(6)));
        assertEquals(0, new BigDecimal("100000").compareTo(SlayerXpTable.getEndermanSlayerXp(7)));
        assertEquals(0, new BigDecimal("400000").compareTo(SlayerXpTable.getEndermanSlayerXp(8)));
        assertEquals(0, new BigDecimal("1000000").compareTo(SlayerXpTable.getEndermanSlayerXp(9)));
        assertEquals(0, new BigDecimal("1250").compareTo(SlayerXpTable.getEndermanSlayerXpBetween(3, 4)));
        assertEquals(0, new BigDecimal("1500").compareTo(SlayerXpTable.getBlazeSlayerXp(4)));
        assertEquals(0, new BigDecimal("1250").compareTo(SlayerXpTable.getBlazeSlayerXpBetween(3, 4)));
    }

    @Test
    @DisplayName("Verify Vampire (Riftstalker Bloodfiend) Slayer XP milestones")
    void testVampireSlayerXpMilestones() {
        assertEquals(0, new BigDecimal("0").compareTo(SlayerXpTable.getVampireSlayerXp(0)));
        assertEquals(0, new BigDecimal("20").compareTo(SlayerXpTable.getVampireSlayerXp(1)));
        assertEquals(0, new BigDecimal("75").compareTo(SlayerXpTable.getVampireSlayerXp(2)));
        assertEquals(0, new BigDecimal("240").compareTo(SlayerXpTable.getVampireSlayerXp(3)));
        assertEquals(0, new BigDecimal("840").compareTo(SlayerXpTable.getVampireSlayerXp(4)));
        assertEquals(0, new BigDecimal("2400").compareTo(SlayerXpTable.getVampireSlayerXp(5)));
        assertEquals(0, new BigDecimal("2400").compareTo(SlayerXpTable.getVampireSlayerXp(9)));
        assertEquals(0, new BigDecimal("2160").compareTo(SlayerXpTable.getVampireSlayerXpBetween(3, 5)));
    }
}
