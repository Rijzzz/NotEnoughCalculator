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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.rijz.notenoughcalculator.api.SkyblockApiIntegration;

import org.junit.jupiter.api.Test;

class ApiProvidersTest {

	@Test
	void testSkyblockApiIntegrationAvailability() {
		assertFalse(SkyblockApiIntegration.isAvailable());
	}

	@Test
	void testCurrencyDataProviderSafety() {
		assertNull(CurrencyDataProvider.getPurse());
		assertNull(CurrencyDataProvider.getBank());
		assertNull(CurrencyDataProvider.getPersonalBank());
		assertNull(CurrencyDataProvider.getCoopBank());
		assertNull(CurrencyDataProvider.getBits());
		assertNull(CurrencyDataProvider.getMotes());
		assertNull(CurrencyDataProvider.getCopper());
		assertNull(CurrencyDataProvider.getSowdust());
		assertNull(CurrencyDataProvider.getKernels());
		assertNull(CurrencyDataProvider.getNorthStars());
		assertNull(CurrencyDataProvider.getGems());
		assertNull(CurrencyDataProvider.getSoulflow());
	}

	@Test
	void testPowderDataProviderSafety() {
		assertNull(PowderDataProvider.getMithril());
		assertNull(PowderDataProvider.getGemstone());
		assertNull(PowderDataProvider.getGlacite());
		assertNull(PowderDataProvider.getTotalMithril());
		assertNull(PowderDataProvider.getTotalGemstone());
		assertNull(PowderDataProvider.getTotalGlacite());
	}

	@Test
	void testMaxwellDataProviderSafety() {
		assertNull(MaxwellDataProvider.getAccessoryPower());
	}

	@Test
	void testPlayerStatsDataProviderSafety() {
		assertNull(PlayerStatsDataProvider.getHealth());
		assertNull(PlayerStatsDataProvider.getMaxHealth());
		assertNull(PlayerStatsDataProvider.getDefense());
		assertNull(PlayerStatsDataProvider.getMana());
		assertNull(PlayerStatsDataProvider.getMaxMana());
		assertNull(PlayerStatsDataProvider.getOverflowMana());
		assertNull(PlayerStatsDataProvider.getVitality());
		assertNull(PlayerStatsDataProvider.getMaxVitality());
		assertNull(PlayerStatsDataProvider.getSkyBlockLevel());
		assertNull(PlayerStatsDataProvider.getSkyBlockLevelProgress());
		assertNull(PlayerStatsDataProvider.getReputation());
		assertNull(PlayerStatsDataProvider.getSpeed());
		assertNull(PlayerStatsDataProvider.getClassLevel());
		assertNull(PlayerStatsDataProvider.getDungeonPartySize());
		assertNull(PlayerStatsDataProvider.getXpLevel());
	}

	@Test
	void testSkillDataProviderSafety() {
		assertNull(SkillDataProvider.getFarmingLevel());
		assertNull(SkillDataProvider.getFarmingXp());
		assertNull(SkillDataProvider.getMiningLevel());
		assertNull(SkillDataProvider.getMiningXp());
		assertNull(SkillDataProvider.getCombatLevel());
		assertNull(SkillDataProvider.getCombatXp());
		assertNull(SkillDataProvider.getForagingLevel());
		assertNull(SkillDataProvider.getForagingXp());
		assertNull(SkillDataProvider.getFishingLevel());
		assertNull(SkillDataProvider.getFishingXp());
		assertNull(SkillDataProvider.getEnchantingLevel());
		assertNull(SkillDataProvider.getEnchantingXp());
		assertNull(SkillDataProvider.getAlchemyLevel());
		assertNull(SkillDataProvider.getAlchemyXp());
		assertNull(SkillDataProvider.getTamingLevel());
		assertNull(SkillDataProvider.getTamingXp());
		assertNull(SkillDataProvider.getCarpentryLevel());
		assertNull(SkillDataProvider.getCarpentryXp());
		assertNull(SkillDataProvider.getRunecraftingLevel());
		assertNull(SkillDataProvider.getRunecraftingXp());
		assertNull(SkillDataProvider.getSocialLevel());
		assertNull(SkillDataProvider.getSocialXp());
		assertNull(SkillDataProvider.getHuntingLevel());
		assertNull(SkillDataProvider.getHuntingXp());
		assertNull(SkillDataProvider.getSkillLevel(null));
		assertNull(SkillDataProvider.getSkillXpAmount(null));
	}

	@Test
	void testSlayerDataProviderSafety() {
		assertNull(SlayerDataProvider.getZombieSlayerXp());
		assertNull(SlayerDataProvider.getSpiderSlayerXp());
		assertNull(SlayerDataProvider.getWolfSlayerXp());
		assertNull(SlayerDataProvider.getEndermanSlayerXp());
		assertNull(SlayerDataProvider.getBlazeSlayerXp());
		assertNull(SlayerDataProvider.getVampireSlayerXp());
		assertNull(SlayerDataProvider.getSlayerXp(null));
	}

	@Test
	void testMarketDataProviderSafety() {
		assertNull(MarketDataProvider.getBazaarBuyPrice("SUPERBOOM_TNT"));
		assertNull(MarketDataProvider.getBazaarSellPrice("SUPERBOOM_TNT"));
		assertNull(MarketDataProvider.getBazaarMargin("SUPERBOOM_TNT"));
		assertNull(MarketDataProvider.getLowestBinPrice("HYPERION"));
		assertNull(MarketDataProvider.getLowestBinAvgPrice("HYPERION"));
		assertNull(MarketDataProvider.getNpcSellPrice("COBBLESTONE"));
		assertNull(MarketDataProvider.getMotesSellPrice("RIFT_ITEM"));
		assertNull(MarketDataProvider.getUnifiedPrice("SUPERBOOM_TNT"));

		assertNull(MarketDataProvider.getBazaarBuyPrice(null));
		assertNull(MarketDataProvider.getBazaarBuyPrice(""));
	}

	@Test
	void testSackDataProviderSafety() {
		assertNull(SackDataProvider.getSackItemCount("COBBLESTONE"));
		assertNull(SackDataProvider.getSackItemCount(null));
		assertNull(SackDataProvider.getSackItemCount(""));
	}

	@Test
	void testNewApiProvidersSafety() {
		assertNull(EssenceDataProvider.getWither());
		assertNull(EssenceDataProvider.getUndead());
		assertNull(EssenceDataProvider.getDragon());
		assertNull(EssenceDataProvider.getSpider());
		assertNull(EssenceDataProvider.getIce());
		assertNull(EssenceDataProvider.getDiamond());
		assertNull(EssenceDataProvider.getGold());
		assertNull(EssenceDataProvider.getCrimson());
		assertNull(EssenceDataProvider.getForest());
		assertNull(EssenceDataProvider.getFossil());
		assertNull(EssenceDataProvider.getSunGecko());
		assertNull(EssenceDataProvider.getSafari());
		assertNull(EssenceDataProvider.getEssence("wither"));
		assertNull(EssenceDataProvider.getEssence(null));

		assertNull(PetDataProvider.getPetLevel());
		assertNull(PetDataProvider.getPetXp());

		assertNull(BestiaryDataProvider.getBestiaryLevel());

		assertNull(TrophyFishDataProvider.getTrophyFishCount());
		assertNull(TrophyFishDataProvider.getDiamondTrophyCount());
		assertNull(TrophyFishDataProvider.getGoldTrophyCount());
		assertNull(TrophyFishDataProvider.getSilverTrophyCount());
		assertNull(TrophyFishDataProvider.getBronzeTrophyCount());

		assertNull(PlayerStatsDataProvider.getCatacombsLevel());
	}

	@Test
	void testHotmDataProviderSafety() {
		assertNull(HotmDataProvider.getHotmTier());
		assertNull(HotmDataProvider.getHotmTokens());
		assertNull(HotmDataProvider.getPerkLevel("mining_speed"));
		assertNull(HotmDataProvider.getPerkLevel(null));
		assertEquals("miningspeed", HotmDataProvider.normalizePerkName("Mining Speed"));
		assertEquals("", HotmDataProvider.normalizePerkName(null));
	}

	@Test
	void testHotfDataProviderSafety() {
		assertNull(HotfDataProvider.getHotfTier());
		assertNull(HotfDataProvider.getHotfTokens());
		assertNull(HotfDataProvider.getForestWhispers());
		assertNull(HotfDataProvider.getDesertWhispers());
		assertNull(HotfDataProvider.getPerkLevel("foraging_speed"));
		assertNull(HotfDataProvider.getPerkLevel(null));
		assertEquals("foragingspeed", HotmDataProvider.normalizePerkName("Foraging Speed"));
		assertEquals("", HotmDataProvider.normalizePerkName(null));
	}
}
