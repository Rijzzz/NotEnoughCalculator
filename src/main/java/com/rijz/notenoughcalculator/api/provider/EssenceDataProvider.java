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

import java.math.BigDecimal;

public class EssenceDataProvider {

    public static BigDecimal getEssence(String name) {
        if (!SkyblockApiIntegration.isAvailable() || name == null || name.isEmpty()) return null;
        String itemId = name.toUpperCase().startsWith("ESSENCE_") ? name.toUpperCase() : "ESSENCE_" + name.toUpperCase();
        return SackDataProvider.getSackItemCount(itemId);
    }

    public static BigDecimal getWither() { return getEssence("WITHER"); }
    public static BigDecimal getUndead() { return getEssence("UNDEAD"); }
    public static BigDecimal getDragon() { return getEssence("DRAGON"); }
    public static BigDecimal getSpider() { return getEssence("SPIDER"); }
    public static BigDecimal getIce() { return getEssence("ICE"); }
    public static BigDecimal getDiamond() { return getEssence("DIAMOND"); }
    public static BigDecimal getGold() { return getEssence("GOLD"); }
    public static BigDecimal getCrimson() { return getEssence("CRIMSON"); }
}
