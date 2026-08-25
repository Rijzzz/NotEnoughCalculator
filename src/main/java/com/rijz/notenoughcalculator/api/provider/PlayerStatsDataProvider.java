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

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import tech.thatgravyboat.skyblockapi.api.area.dungeon.DungeonAPI;
import tech.thatgravyboat.skyblockapi.api.profile.StatsAPI;
import tech.thatgravyboat.skyblockapi.api.profile.profile.ProfileAPI;
import tech.thatgravyboat.skyblockapi.api.profile.reputation.ReputationAPI;

import java.math.BigDecimal;

public class PlayerStatsDataProvider {

    public static BigDecimal getHealth() {
        BigDecimal health = SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(StatsAPI.INSTANCE.getHealth()));
        if (health != null) return health;
        try {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null)
                return BigDecimal.valueOf((long) player.getHealth());
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static BigDecimal getMaxHealth() {
        BigDecimal maxHealth = SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(StatsAPI.INSTANCE.getMaxHealth()));
        if (maxHealth != null) return maxHealth;
        try {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null)
                return BigDecimal.valueOf((long) player.getMaxHealth());
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static BigDecimal getDefense() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(StatsAPI.INSTANCE.getDefense()));
    }

    public static BigDecimal getMana() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(StatsAPI.INSTANCE.getMana()));
    }

    public static BigDecimal getMaxMana() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(StatsAPI.INSTANCE.getMaxMana()));
    }

    public static BigDecimal getOverflowMana() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(StatsAPI.INSTANCE.getOverflowMana()));
    }

    public static BigDecimal getVitality() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(StatsAPI.INSTANCE.getVitality()));
    }

    public static BigDecimal getMaxVitality() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(StatsAPI.INSTANCE.getMaxVitality()));
    }

    public static BigDecimal getSkyBlockLevel() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(ProfileAPI.INSTANCE.getSbLevel()));
    }

    public static BigDecimal getSkyBlockLevelProgress() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(ProfileAPI.INSTANCE.getSbLevelProgress()));
    }

    public static BigDecimal getReputation() {
        return SkyblockApiIntegration
                .safeQuery(() -> BigDecimal.valueOf(ReputationAPI.INSTANCE.getCurrentReputation()));
    }

    public static BigDecimal getSpeed() {
        try {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                return BigDecimal.valueOf((long) (player.getAbilities().getWalkingSpeed() * 1000.0f));
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static BigDecimal getClassLevel() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(DungeonAPI.INSTANCE.getClassLevel()));
    }

    public static BigDecimal getDungeonPartySize() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(DungeonAPI.INSTANCE.getPartySize()));
    }

    public static BigDecimal getCatacombsLevel() {
        return getClassLevel();
    }

    public static BigDecimal getXpLevel() {
        try {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null)
                return BigDecimal.valueOf(player.experienceLevel);
        } catch (Throwable ignored) {
        }
        return null;
    }
}
