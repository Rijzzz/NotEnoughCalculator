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

class SkillXpTableTest {

    @Test
    @DisplayName("Verify Skill XP milestones and deltas")
    void testSkillXpMilestones() {
        assertEquals(0, new BigDecimal("0").compareTo(SkillXpTable.getSkillXp(0)));
        assertEquals(0, new BigDecimal("50").compareTo(SkillXpTable.getSkillXp(1)));
        assertEquals(0, new BigDecimal("55172425").compareTo(SkillXpTable.getSkillXp(50)));
        assertEquals(0, new BigDecimal("111672425").compareTo(SkillXpTable.getSkillXp(60)));
        assertEquals(0, new BigDecimal("111672425").compareTo(SkillXpTable.getSkillXp(100)));
        assertEquals(0, new BigDecimal("29650000").compareTo(SkillXpTable.getSkillXpBetween(40, 50)));
        assertEquals(0, BigDecimal.ZERO.compareTo(SkillXpTable.getSkillXpBetween(50, 40)));
    }

    @Test
    @DisplayName("Verify Hunting XP milestones and max 50 level cap")
    void testHuntingXpMilestones() {
        assertEquals(0, new BigDecimal("0").compareTo(SkillXpTable.getHuntingXp(0)));
        assertEquals(0, new BigDecimal("50").compareTo(SkillXpTable.getHuntingXp(1)));
        assertEquals(0, new BigDecimal("55172425").compareTo(SkillXpTable.getHuntingXp(50)));
        assertEquals(0, new BigDecimal("55172425").compareTo(SkillXpTable.getHuntingXp(60)));
        assertEquals(0, new BigDecimal("29650000").compareTo(SkillXpTable.getHuntingXpBetween(40, 50)));
        assertEquals(0, new BigDecimal("29650000").compareTo(SkillXpTable.getHuntingXpBetween(40, 60)));
    }

    @Test
    @DisplayName("Verify Runecrafting XP milestones and deltas")
    void testRunecraftingXpMilestones() {
        assertEquals(0, new BigDecimal("0").compareTo(SkillXpTable.getRunecraftingXp(0)));
        assertEquals(0, new BigDecimal("50").compareTo(SkillXpTable.getRunecraftingXp(1)));
        assertEquals(0, new BigDecimal("3510").compareTo(SkillXpTable.getRunecraftingXp(11)));
        assertEquals(0, new BigDecimal("94450").compareTo(SkillXpTable.getRunecraftingXp(25)));
        assertEquals(0, new BigDecimal("27475").compareTo(SkillXpTable.getRunecraftingXpBetween(10, 20)));
    }

    @Test
    @DisplayName("Verify Social XP milestones and deltas")
    void testSocialXpMilestones() {
        assertEquals(0, new BigDecimal("0").compareTo(SkillXpTable.getSocialXp(0)));
        assertEquals(0, new BigDecimal("50").compareTo(SkillXpTable.getSocialXp(1)));
        assertEquals(0, new BigDecimal("7550").compareTo(SkillXpTable.getSocialXp(10)));
        assertEquals(0, new BigDecimal("272800").compareTo(SkillXpTable.getSocialXp(25)));
        assertEquals(0, new BigDecimal("65500").compareTo(SkillXpTable.getSocialXpBetween(15, 20)));
    }

    @Test
    @DisplayName("Verify Catacombs XP milestones, deltas, and post-50 overflow")
    void testCataXpMilestones() {
        assertEquals(0, new BigDecimal("0").compareTo(SkillXpTable.getCataXp(0)));
        assertEquals(0, new BigDecimal("50").compareTo(SkillXpTable.getCataXp(1)));
        assertEquals(0, new BigDecimal("569809640").compareTo(SkillXpTable.getCataXp(50)));
        assertEquals(0, new BigDecimal("556550000").compareTo(SkillXpTable.getCataXpBetween(35, 50)));
        assertEquals(0, new BigDecimal("769809640").compareTo(SkillXpTable.getCataXp(51)));
        assertEquals(0, new BigDecimal("2569809640").compareTo(SkillXpTable.getCataXp(60)));
        assertEquals(0, new BigDecimal("2000000000").compareTo(SkillXpTable.getCataXpBetween(50, 60)));
    }
}
