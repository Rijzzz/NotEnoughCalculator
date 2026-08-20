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
        if (SkyblockApiIntegration.isAvailable()) {
            try {
                return BigDecimal.valueOf(StatsAPI.INSTANCE.getHealth());
            } catch (Throwable ignored) {
            }
        }
        try {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null)
                return BigDecimal.valueOf((long) player.getHealth());
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static BigDecimal getMaxHealth() {
        if (SkyblockApiIntegration.isAvailable()) {
            try {
                return BigDecimal.valueOf(StatsAPI.INSTANCE.getMaxHealth());
            } catch (Throwable ignored) {
            }
        }
        try {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null)
                return BigDecimal.valueOf((long) player.getMaxHealth());
        } catch (Throwable ignored) {
        }
        return null;
    }

    public static BigDecimal getDefense() {
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return BigDecimal.valueOf(StatsAPI.INSTANCE.getDefense());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getMana() {
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return BigDecimal.valueOf(StatsAPI.INSTANCE.getMana());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getMaxMana() {
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return BigDecimal.valueOf(StatsAPI.INSTANCE.getMaxMana());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getOverflowMana() {
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return BigDecimal.valueOf(StatsAPI.INSTANCE.getOverflowMana());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getVitality() {
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return BigDecimal.valueOf(StatsAPI.INSTANCE.getVitality());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getMaxVitality() {
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return BigDecimal.valueOf(StatsAPI.INSTANCE.getMaxVitality());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getSkyBlockLevel() {
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return BigDecimal.valueOf(ProfileAPI.INSTANCE.getSbLevel());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getSkyBlockLevelProgress() {
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return BigDecimal.valueOf(ProfileAPI.INSTANCE.getSbLevelProgress());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getReputation() {
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return BigDecimal.valueOf(ReputationAPI.INSTANCE.getCurrentReputation());
        } catch (Throwable ignored) {
            return null;
        }
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
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return BigDecimal.valueOf(DungeonAPI.INSTANCE.getClassLevel());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getDungeonPartySize() {
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return BigDecimal.valueOf(DungeonAPI.INSTANCE.getPartySize());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getCatacombsLevel() {
        if (!SkyblockApiIntegration.isAvailable())
            return null;
        try {
            return getClassLevel();
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getCatacombsXp() {
        return null;
    }

    public static BigDecimal getSecrets() {
        return null;
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
