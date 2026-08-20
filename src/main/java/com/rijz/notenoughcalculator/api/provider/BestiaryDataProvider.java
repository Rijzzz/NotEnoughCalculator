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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BestiaryDataProvider {

    private static final Pattern BESTIARY_LEVEL_PATTERN = Pattern.compile("(?i)(?:overall|tier|level|bestiary|milestone)\\s*[:\\-]?\\s*([\\d,]+)");

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static BigDecimal getBestiaryLevel() {
        if (!SkyblockApiIntegration.isAvailable()) return null;

        try {
            Class<?> tabWidgetClass = Class.forName("tech.thatgravyboat.skyblockapi.api.events.info.TabWidget");
            Object bestiaryEnum = Enum.valueOf((Class<Enum>) tabWidgetClass, "BESTIARY");
            Method getCurrentLinesMethod = tabWidgetClass.getMethod("getCurrentLines");
            List<String> lines = (List<String>) getCurrentLinesMethod.invoke(bestiaryEnum);

            if (lines != null) {
                for (String line : lines) {
                    if (line == null) continue;
                    String clean = line.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
                    Matcher matcher = BESTIARY_LEVEL_PATTERN.matcher(clean);
                    if (matcher.find()) {
                        String numStr = matcher.group(1).replace(",", "");
                        return new BigDecimal(numStr);
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return null;
    }
}
