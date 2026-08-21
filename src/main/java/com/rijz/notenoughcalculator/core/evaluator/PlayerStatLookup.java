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

package com.rijz.notenoughcalculator.core.evaluator;

import com.rijz.notenoughcalculator.api.provider.BestiaryDataProvider;
import com.rijz.notenoughcalculator.api.provider.CurrencyDataProvider;
import com.rijz.notenoughcalculator.api.provider.EssenceDataProvider;
import com.rijz.notenoughcalculator.api.provider.HotfDataProvider;
import com.rijz.notenoughcalculator.api.provider.HotmDataProvider;
import com.rijz.notenoughcalculator.api.provider.MaxwellDataProvider;
import com.rijz.notenoughcalculator.api.provider.PetDataProvider;
import com.rijz.notenoughcalculator.api.provider.PlayerStatsDataProvider;
import com.rijz.notenoughcalculator.api.provider.PowderDataProvider;
import com.rijz.notenoughcalculator.api.provider.SkillDataProvider;
import com.rijz.notenoughcalculator.api.provider.SlayerDataProvider;
import com.rijz.notenoughcalculator.api.provider.TrophyFishDataProvider;

import java.math.BigDecimal;

public class PlayerStatLookup {

    public static BigDecimal lookupPlayerStat(String name) {
        switch (name) {
            case "purse":
            case "p":
                return CurrencyDataProvider.getPurse();
            case "bank":
            case "b":
                return CurrencyDataProvider.getBank();
            case "personalbank":
            case "pbank":
                return CurrencyDataProvider.getPersonalBank();
            case "coopbank":
            case "cbank":
                return CurrencyDataProvider.getCoopBank();
            case "bits":
            case "bt":
                return CurrencyDataProvider.getBits();
            case "motes":
            case "mt":
                return CurrencyDataProvider.getMotes();
            case "copper":
            case "cop":
                return CurrencyDataProvider.getCopper();
            case "sowdust":
            case "sdust":
                return CurrencyDataProvider.getSowdust();
            case "kernels":
            case "kern":
                return CurrencyDataProvider.getKernels();
            case "northstars":
            case "nstars":
            case "ns":
                return CurrencyDataProvider.getNorthStars();
            case "gems":
            case "gem":
                return CurrencyDataProvider.getGems();
            case "soulflow":
            case "sflow":
            case "sf":
                return CurrencyDataProvider.getSoulflow();

            case "sblevel":
            case "sblvl":
            case "sb":
            case "skyblocklevel":
                return PlayerStatsDataProvider.getSkyBlockLevel();
            case "sblevelprogress":
            case "sbprog":
            case "sblevelprog":
                return PlayerStatsDataProvider.getSkyBlockLevelProgress();
            case "rep":
            case "reputation":
                return PlayerStatsDataProvider.getReputation();

            case "mithrilpowder":
            case "mithril":
            case "mpowder":
                return PowderDataProvider.getMithril();
            case "gemstonepowder":
            case "gemstone":
            case "gpowder":
                return PowderDataProvider.getGemstone();
            case "glacitepowder":
            case "glacite":
            case "glpowder":
                return PowderDataProvider.getGlacite();
            case "totalmithrilpowder":
            case "totalmithril":
            case "totmithril":
            case "totmpowder":
                return PowderDataProvider.getTotalMithril();
            case "totalgemstonepowder":
            case "totalgemstone":
            case "totgemstone":
            case "totgpowder":
                return PowderDataProvider.getTotalGemstone();
            case "totalglacitepowder":
            case "totalglacite":
            case "totglacite":
            case "totglpowder":
                return PowderDataProvider.getTotalGlacite();
            case "hotm":
            case "hotmtier":
                return HotmDataProvider.getHotmTier();
            case "hotmtokens":
            case "tokens":
                return HotmDataProvider.getHotmTokens();

            case "hotf":
            case "hotftier":
            case "htier":
                return HotfDataProvider.getHotfTier();
            case "hotftokens":
            case "htokens":
                return HotfDataProvider.getHotfTokens();
            case "whispers":
            case "whisper":
            case "whisp":
            case "forestwhispers":
                return HotfDataProvider.getForestWhispers();
            case "desertwhispers":
            case "dwhispers":
                return HotfDataProvider.getDesertWhispers();

            case "cata":
            case "catacombs":
            case "catacombslevel":
            case "catalvl":
                return PlayerStatsDataProvider.getCatacombsLevel();
            case "cataxp":
            case "cxp":
            case "catacombsxp":
                return PlayerStatsDataProvider.getCatacombsXp();
            case "secrets":
            case "sec":
            case "secretcount":
                return PlayerStatsDataProvider.getSecrets();
            case "classlevel":
            case "classlvl":
            case "dclass":
            case "dungeonclass":
                return PlayerStatsDataProvider.getClassLevel();
            case "partysize":
            case "party":
            case "dungeonparty":
                return PlayerStatsDataProvider.getDungeonPartySize();

            case "witheressence":
            case "wither":
            case "wessence":
            case "w":
                return EssenceDataProvider.getWither();
            case "undeadessence":
            case "undead":
            case "uessence":
            case "u":
                return EssenceDataProvider.getUndead();
            case "dragonessence":
            case "dragon":
            case "dessence":
            case "d":
                return EssenceDataProvider.getDragon();
            case "spideressence":
            case "spider":
            case "spessence":
            case "sp":
                return EssenceDataProvider.getSpider();
            case "iceessence":
            case "ice":
            case "iessence":
            case "i":
                return EssenceDataProvider.getIce();
            case "diamondessence":
            case "diamond":
            case "diessence":
            case "di":
                return EssenceDataProvider.getDiamond();
            case "goldessence":
            case "gold":
            case "gessence":
            case "g":
                return EssenceDataProvider.getGold();
            case "crimsonessence":
            case "crimson":
            case "cessence":
            case "c":
                return EssenceDataProvider.getCrimson();

            case "petlvl":
            case "petlevel":
            case "pet":
                return PetDataProvider.getPetLevel();
            case "petxp":
            case "pxp":
            case "petexperience":
                return PetDataProvider.getPetXp();

            case "bestiary":
            case "bestiarylvl":
            case "bestiarylevel":
            case "best":
                return BestiaryDataProvider.getBestiaryLevel();

            case "trophyfish":
            case "trophyfishcount":
            case "tfish":
                return TrophyFishDataProvider.getTrophyFishCount();
            case "diamondtrophy":
            case "diamondtrophyfish":
            case "dtrophy":
                return TrophyFishDataProvider.getDiamondTrophyCount();
            case "goldtrophy":
            case "goldtrophyfish":
            case "gtrophy":
                return TrophyFishDataProvider.getGoldTrophyCount();
            case "silvertrophy":
            case "silvertrophyfish":
            case "strophy":
                return TrophyFishDataProvider.getSilverTrophyCount();
            case "bronzetrophy":
            case "bronzetrophyfish":
            case "btrophy":
                return TrophyFishDataProvider.getBronzeTrophyCount();

            case "mp":
            case "magicalpower":
            case "accessorypower":
                return MaxwellDataProvider.getAccessoryPower();

            case "hp":
            case "health":
                return PlayerStatsDataProvider.getHealth();
            case "maxhp":
            case "maxhealth":
                return PlayerStatsDataProvider.getMaxHealth();
            case "def":
            case "defense":
                return PlayerStatsDataProvider.getDefense();
            case "mana":
            case "intel":
            case "intelligence":
                return PlayerStatsDataProvider.getMana();
            case "maxmana":
            case "mmana":
            case "maxintel":
                return PlayerStatsDataProvider.getMaxMana();
            case "overflowmana":
            case "ofmana":
                return PlayerStatsDataProvider.getOverflowMana();
            case "vit":
            case "vitality":
                return PlayerStatsDataProvider.getVitality();
            case "maxvitality":
            case "mvit":
                return PlayerStatsDataProvider.getMaxVitality();
            case "spd":
            case "speed":
                return PlayerStatsDataProvider.getSpeed();
            case "xplevel":
            case "xplvl":
            case "xp":
                return PlayerStatsDataProvider.getXpLevel();

            case "farming":
            case "farminglvl":
            case "farm":
                return SkillDataProvider.getFarmingLevel();
            case "farmingxp":
            case "farmxp":
                return SkillDataProvider.getFarmingXp();
            case "mining":
            case "mininglvl":
            case "mine":
                return SkillDataProvider.getMiningLevel();
            case "miningxp":
            case "minexp":
                return SkillDataProvider.getMiningXp();
            case "combat":
            case "combatlvl":
            case "cmbt":
                return SkillDataProvider.getCombatLevel();
            case "combatxp":
            case "cmbtxp":
                return SkillDataProvider.getCombatXp();
            case "foraging":
            case "foraginglvl":
            case "forag":
                return SkillDataProvider.getForagingLevel();
            case "foragingxp":
            case "foragxp":
                return SkillDataProvider.getForagingXp();
            case "fishing":
            case "fishinglvl":
            case "fish":
                return SkillDataProvider.getFishingLevel();
            case "fishingxp":
            case "fishxp":
                return SkillDataProvider.getFishingXp();
            case "enchanting":
            case "enchantinglvl":
            case "ench":
                return SkillDataProvider.getEnchantingLevel();
            case "enchantingxp":
            case "enchxp":
                return SkillDataProvider.getEnchantingXp();
            case "alchemy":
            case "alchemylvl":
            case "alch":
                return SkillDataProvider.getAlchemyLevel();
            case "alchemyxp":
            case "alchxp":
                return SkillDataProvider.getAlchemyXp();
            case "taming":
            case "taminglvl":
            case "tame":
                return SkillDataProvider.getTamingLevel();
            case "tamingxp":
            case "tamexp":
                return SkillDataProvider.getTamingXp();
            case "carpentry":
            case "carpentrylvl":
            case "carp":
                return SkillDataProvider.getCarpentryLevel();
            case "carpentryxp":
            case "carpxp":
                return SkillDataProvider.getCarpentryXp();
            case "runecrafting":
            case "runecraftinglvl":
            case "rune":
                return SkillDataProvider.getRunecraftingLevel();
            case "runecraftingxp":
            case "runexp":
                return SkillDataProvider.getRunecraftingXp();
            case "social":
            case "sociallvl":
            case "soc":
                return SkillDataProvider.getSocialLevel();
            case "socialxp":
            case "socxp":
                return SkillDataProvider.getSocialXp();
            case "hunting":
            case "huntinglvl":
            case "hunt":
                return SkillDataProvider.getHuntingLevel();
            case "huntingxp":
            case "huntxp":
                return SkillDataProvider.getHuntingXp();

            case "zombieslayer":
            case "zombieslayerxp":
            case "rev":
            case "revxp":
            case "revslayer":
                return SlayerDataProvider.getZombieSlayerXp();
            case "spiderslayer":
            case "spiderslayerxp":
            case "tara":
            case "taraxp":
            case "taraslayer":
                return SlayerDataProvider.getSpiderSlayerXp();
            case "wolfslayer":
            case "wolfslayerxp":
            case "sven":
            case "svenxp":
            case "svenslayer":
                return SlayerDataProvider.getWolfSlayerXp();
            case "endermanslayer":
            case "endermanslayerxp":
            case "eman":
            case "emanxp":
            case "emanslayer":
                return SlayerDataProvider.getEndermanSlayerXp();
            case "blazeslayer":
            case "blazeslayerxp":
            case "blaze":
            case "blazexp":
                return SlayerDataProvider.getBlazeSlayerXp();
            case "vampireslayer":
            case "vampireslayerxp":
            case "vamp":
            case "vampxp":
                return SlayerDataProvider.getVampireSlayerXp();

            default:
                return null;
        }
    }
}
