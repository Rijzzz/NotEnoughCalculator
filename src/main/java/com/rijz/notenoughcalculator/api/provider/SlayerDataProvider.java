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

import tech.thatgravyboat.skyblockapi.api.area.slayer.SlayerType;
import tech.thatgravyboat.skyblockapi.api.profile.slayer.SlayerEntry;
import tech.thatgravyboat.skyblockapi.api.profile.slayer.SlayerProgressAPI;

import java.math.BigDecimal;
import java.util.Map;

public class SlayerDataProvider {

    public static BigDecimal getSlayerXp(SlayerType type) {
        return SkyblockApiIntegration.safeQuery(() -> {
            Map<SlayerType, SlayerEntry> data = SlayerProgressAPI.INSTANCE.getSlayerData();
            if (data == null) return null;
            SlayerEntry entry = data.get(type);
            return entry != null ? BigDecimal.valueOf(entry.getXp()) : null;
        });
    }

    public static BigDecimal getZombieSlayerXp()   { return getSlayerXp(SlayerType.REVENANT_HORROR); }
    public static BigDecimal getSpiderSlayerXp()   { return getSlayerXp(SlayerType.TARANTULA_BROODFATHER); }
    public static BigDecimal getWolfSlayerXp()     { return getSlayerXp(SlayerType.SVEN_PACKMASTER); }
    public static BigDecimal getEndermanSlayerXp() { return getSlayerXp(SlayerType.VOIDGLOOM_SERAPH); }
    public static BigDecimal getBlazeSlayerXp()    { return getSlayerXp(SlayerType.INFERNO_DEMONLORD); }
    public static BigDecimal getVampireSlayerXp()  { return getSlayerXp(SlayerType.RIFTSTALKER_BLOODFIEND); }
}
