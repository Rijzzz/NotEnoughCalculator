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

import java.math.BigDecimal;

public class HotfDataProvider {

    public static BigDecimal getHotfTier() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(HotfAPI.INSTANCE.getTier()));
    }

    public static BigDecimal getHotfTokens() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(HotfAPI.INSTANCE.getTokens()));
    }

    public static BigDecimal getForestWhispers() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(WhispersAPI.INSTANCE.getForest()));
    }

    public static BigDecimal getDesertWhispers() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(WhispersAPI.INSTANCE.getDesert()));
    }

    public static BigDecimal getPerkLevel(String perkName) {
        if (!SkyblockApiIntegration.isAvailable() || perkName == null || perkName.isEmpty()) {
            return null;
        }
        return HotmDataProvider.lookupPerkLevel(HotfAPI.INSTANCE.getPerks(), perkName);
    }
}
