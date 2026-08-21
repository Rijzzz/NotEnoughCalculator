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
import tech.thatgravyboat.skyblockapi.api.profile.hotf.HotfAPI;
import tech.thatgravyboat.skyblockapi.api.profile.hotf.WhispersAPI;
import tech.thatgravyboat.skyblockapi.api.profile.skilltree.SkillTreePerk;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

public class HotfDataProvider {

    public static BigDecimal getHotfTier() {
        if (!SkyblockApiIntegration.isAvailable()) {
            return null;
        }
        try {
            return BigDecimal.valueOf(HotfAPI.INSTANCE.getTier());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getHotfTokens() {
        if (!SkyblockApiIntegration.isAvailable()) {
            return null;
        }
        try {
            return BigDecimal.valueOf(HotfAPI.INSTANCE.getTokens());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getForestWhispers() {
        if (!SkyblockApiIntegration.isAvailable()) {
            return null;
        }
        try {
            return BigDecimal.valueOf(WhispersAPI.INSTANCE.getForest());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getDesertWhispers() {
        if (!SkyblockApiIntegration.isAvailable()) {
            return null;
        }
        try {
            return BigDecimal.valueOf(WhispersAPI.INSTANCE.getDesert());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getPerkLevel(String perkName) {
        if (!SkyblockApiIntegration.isAvailable() || perkName == null || perkName.isEmpty()) {
            return null;
        }

        String normalizedQuery = normalizePerkName(perkName);

        try {
            Map<String, ? extends SkillTreePerk> hotfPerks = HotfAPI.INSTANCE.getPerks();
            if (hotfPerks != null) {
                for (Map.Entry<String, ? extends SkillTreePerk> entry : hotfPerks.entrySet()) {
                    if (normalizePerkName(entry.getKey()).equals(normalizedQuery)) {
                        SkillTreePerk perk = entry.getValue();
                        if (perk != null && perk.getUnlocked()) {
                            return BigDecimal.valueOf(perk.getLevel());
                        }
                        return BigDecimal.ZERO;
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

    public static String normalizePerkName(String name) {
        if (name == null) {
            return "";
        }
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
