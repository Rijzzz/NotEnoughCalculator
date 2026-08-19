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
import com.rijz.notenoughcalculator.api.provider.BestiaryDataProvider;
import com.rijz.notenoughcalculator.api.provider.CurrencyDataProvider;
import com.rijz.notenoughcalculator.api.provider.EssenceDataProvider;
import com.rijz.notenoughcalculator.api.provider.MarketDataProvider;
import com.rijz.notenoughcalculator.api.provider.MaxwellDataProvider;
import com.rijz.notenoughcalculator.api.provider.PetDataProvider;
import com.rijz.notenoughcalculator.api.provider.PlayerStatsProvider;
import com.rijz.notenoughcalculator.api.provider.PowderDataProvider;
import com.rijz.notenoughcalculator.api.provider.SackDataProvider;
import com.rijz.notenoughcalculator.api.provider.SkillDataProvider;
import com.rijz.notenoughcalculator.api.provider.SlayerDataProvider;
import com.rijz.notenoughcalculator.api.provider.TrophyFishDataProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        assertNull(PowderDataProvider.getHotmTier());
        assertNull(PowderDataProvider.getHotmTokens());
    }

    @Test
    void testMaxwellDataProviderSafety() {
        assertNull(MaxwellDataProvider.getAccessoryPower());
    }

    @Test
    void testPlayerStatsProviderSafety() {
        assertNull(PlayerStatsProvider.getHealth());
        assertNull(PlayerStatsProvider.getMaxHealth());
        assertNull(PlayerStatsProvider.getDefense());
        assertNull(PlayerStatsProvider.getMana());
        assertNull(PlayerStatsProvider.getMaxMana());
        assertNull(PlayerStatsProvider.getOverflowMana());
        assertNull(PlayerStatsProvider.getVitality());
        assertNull(PlayerStatsProvider.getMaxVitality());
        assertNull(PlayerStatsProvider.getSkyBlockLevel());
        assertNull(PlayerStatsProvider.getSkyBlockLevelProgress());
        assertNull(PlayerStatsProvider.getReputation());
        assertNull(PlayerStatsProvider.getSpeed());
        assertNull(PlayerStatsProvider.getClassLevel());
        assertNull(PlayerStatsProvider.getDungeonPartySize());
        assertNull(PlayerStatsProvider.getXpLevel());
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
    }

    @Test
    void testSlayerDataProviderSafety() {
        assertNull(SlayerDataProvider.getZombieSlayerXp());
        assertNull(SlayerDataProvider.getSpiderSlayerXp());
        assertNull(SlayerDataProvider.getWolfSlayerXp());
        assertNull(SlayerDataProvider.getEndermanSlayerXp());
        assertNull(SlayerDataProvider.getBlazeSlayerXp());
        assertNull(SlayerDataProvider.getVampireSlayerXp());
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

        assertNull(PetDataProvider.getPetLevel());
        assertNull(PetDataProvider.getPetXp());

        assertNull(BestiaryDataProvider.getBestiaryLevel());

        assertNull(TrophyFishDataProvider.getTrophyFishCount());
        assertNull(TrophyFishDataProvider.getDiamondTrophyCount());
        assertNull(TrophyFishDataProvider.getGoldTrophyCount());
        assertNull(TrophyFishDataProvider.getSilverTrophyCount());
        assertNull(TrophyFishDataProvider.getBronzeTrophyCount());

        assertNull(PlayerStatsProvider.getCatacombsLevel());
        assertNull(PlayerStatsProvider.getCatacombsXp());
        assertNull(PlayerStatsProvider.getSecrets());
    }
}
