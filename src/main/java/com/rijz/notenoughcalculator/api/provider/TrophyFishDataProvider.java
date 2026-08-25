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
import tech.thatgravyboat.skyblockapi.api.area.isle.trophyfish.TrophyFishTier;
import tech.thatgravyboat.skyblockapi.api.area.isle.trophyfish.TrophyFishType;
import tech.thatgravyboat.skyblockapi.api.area.isle.trophyfish.TrophyFishingAPI;

import java.math.BigDecimal;
import java.util.Map;

public class TrophyFishDataProvider {

    private static long countByTier(TrophyFishTier targetTier) {
        long total = 0;
        for (TrophyFishType type : TrophyFishType.values()) {
            Map<TrophyFishTier, Integer> caught = TrophyFishingAPI.INSTANCE.getCaught(type);
            if (caught == null) continue;
            if (targetTier == null) {
                for (Integer count : caught.values()) {
                    if (count != null) total += count;
                }
            } else {
                Integer count = caught.get(targetTier);
                if (count != null) total += count;
            }
        }
        return total;
    }

    public static BigDecimal getTrophyFishCount() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(countByTier(null)));
    }

    public static BigDecimal getDiamondTrophyCount() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(countByTier(TrophyFishTier.DIAMOND)));
    }

    public static BigDecimal getGoldTrophyCount() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(countByTier(TrophyFishTier.GOLD)));
    }

    public static BigDecimal getSilverTrophyCount() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(countByTier(TrophyFishTier.SILVER)));
    }

    public static BigDecimal getBronzeTrophyCount() {
        return SkyblockApiIntegration.safeQuery(() -> BigDecimal.valueOf(countByTier(TrophyFishTier.BRONZE)));
    }
}
