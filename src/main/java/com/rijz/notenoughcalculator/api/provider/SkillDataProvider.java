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

package com.rijz.notenoughcalculator.api.provider;

import com.rijz.notenoughcalculator.api.SkyblockApiIntegration;

import tech.thatgravyboat.skyblockapi.api.profile.skillxp.SkillExpAPI;
import tech.thatgravyboat.skyblockapi.api.remote.hypixel.HypixelSkillAPI.Skill;

import java.math.BigDecimal;
import java.util.Map;

public class SkillDataProvider {

	private static Float getSkillXp(Skill skill) {
		return SkyblockApiIntegration.safeQuery(() -> {
			Map<Skill, Float> skills = SkillExpAPI.INSTANCE.getSkills();
			return skills != null ? skills.get(skill) : null;
		});
	}

	public static BigDecimal getSkillLevel(Skill skill) {
		return SkyblockApiIntegration.safeQuery(() -> {
			Float xp = getSkillXp(skill);
			if (xp == null)
				return null;
			int lvl = skill.getData().getLevelForExp(xp.longValue());
			return BigDecimal.valueOf(lvl);
		});
	}

	public static BigDecimal getSkillXpAmount(Skill skill) {
		return SkyblockApiIntegration.safeQuery(() -> {
			Float xp = getSkillXp(skill);
			return xp != null ? BigDecimal.valueOf(xp.doubleValue()) : null;
		});
	}

	public static BigDecimal getFarmingLevel() {
		return getSkillLevel(Skill.FARMING);
	}
	public static BigDecimal getFarmingXp() {
		return getSkillXpAmount(Skill.FARMING);
	}
	public static BigDecimal getMiningLevel() {
		return getSkillLevel(Skill.MINING);
	}
	public static BigDecimal getMiningXp() {
		return getSkillXpAmount(Skill.MINING);
	}
	public static BigDecimal getCombatLevel() {
		return getSkillLevel(Skill.COMBAT);
	}
	public static BigDecimal getCombatXp() {
		return getSkillXpAmount(Skill.COMBAT);
	}
	public static BigDecimal getForagingLevel() {
		return getSkillLevel(Skill.FORAGING);
	}
	public static BigDecimal getForagingXp() {
		return getSkillXpAmount(Skill.FORAGING);
	}
	public static BigDecimal getFishingLevel() {
		return getSkillLevel(Skill.FISHING);
	}
	public static BigDecimal getFishingXp() {
		return getSkillXpAmount(Skill.FISHING);
	}
	public static BigDecimal getEnchantingLevel() {
		return getSkillLevel(Skill.ENCHANTING);
	}
	public static BigDecimal getEnchantingXp() {
		return getSkillXpAmount(Skill.ENCHANTING);
	}
	public static BigDecimal getAlchemyLevel() {
		return getSkillLevel(Skill.ALCHEMY);
	}
	public static BigDecimal getAlchemyXp() {
		return getSkillXpAmount(Skill.ALCHEMY);
	}
	public static BigDecimal getTamingLevel() {
		return getSkillLevel(Skill.TAMING);
	}
	public static BigDecimal getTamingXp() {
		return getSkillXpAmount(Skill.TAMING);
	}
	public static BigDecimal getCarpentryLevel() {
		return getSkillLevel(Skill.CARPENTRY);
	}
	public static BigDecimal getCarpentryXp() {
		return getSkillXpAmount(Skill.CARPENTRY);
	}
	public static BigDecimal getRunecraftingLevel() {
		return getSkillLevel(Skill.RUNECRAFTING);
	}
	public static BigDecimal getRunecraftingXp() {
		return getSkillXpAmount(Skill.RUNECRAFTING);
	}
	public static BigDecimal getSocialLevel() {
		return getSkillLevel(Skill.SOCIAL);
	}
	public static BigDecimal getSocialXp() {
		return getSkillXpAmount(Skill.SOCIAL);
	}
	public static BigDecimal getHuntingLevel() {
		return getSkillLevel(Skill.HUNTING);
	}
	public static BigDecimal getHuntingXp() {
		return getSkillXpAmount(Skill.HUNTING);
	}
}
