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

import tech.thatgravyboat.skyblockapi.api.profile.items.sacks.SacksAPI;

import java.math.BigDecimal;
import java.util.Map;

public class SackDataProvider {

    public static BigDecimal getSackItemCount(String itemId) {
        if (itemId == null || itemId.isEmpty()) return null;
        return SkyblockApiIntegration.safeQuery(() -> {
            Map<String, Integer> counts = SacksAPI.INSTANCE.getSackItems();
            if (counts != null && counts.containsKey(itemId.toUpperCase())) {
                return BigDecimal.valueOf(counts.get(itemId.toUpperCase()));
            }
            return null;
        });
    }
}
