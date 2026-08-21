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
import tech.thatgravyboat.skyblockapi.api.profile.hotm.HotmAPI;
import tech.thatgravyboat.skyblockapi.api.profile.skilltree.SkillTreePerk;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

public class HotmDataProvider {

    public static BigDecimal getHotmTier() {
        if (!SkyblockApiIntegration.isAvailable()) {
            return null;
        }
        try {
            return BigDecimal.valueOf(HotmAPI.INSTANCE.getTier());
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static BigDecimal getHotmTokens() {
        if (!SkyblockApiIntegration.isAvailable()) {
            return null;
        }
        try {
            return BigDecimal.valueOf(HotmAPI.INSTANCE.getTokens());
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
            Map<String, ? extends SkillTreePerk> hotmPerks = HotmAPI.INSTANCE.getPerks();
            if (hotmPerks != null) {
                for (Map.Entry<String, ? extends SkillTreePerk> entry : hotmPerks.entrySet()) {
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
