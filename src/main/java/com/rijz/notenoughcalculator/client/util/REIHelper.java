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

package com.rijz.notenoughcalculator.client.util;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

// Helper for accessing REI's internal search field bounds via reflection.
// Used by the overlay renderer to position the calculator result display.
public class REIHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(REIHelper.class);
    private static Field boundsField = null;
    private static Method getBoundsMethod = null;
    private static boolean reflectionAttempted = false;

    private static void init(TextField searchField) {
        if (reflectionAttempted) return;
        if (searchField == null) return;
        reflectionAttempted = true;

        Class<?> implClass = searchField.getClass();

        try {
            getBoundsMethod = implClass.getMethod("getBounds");
            getBoundsMethod.setAccessible(true);
            LOGGER.debug("Found getBounds in {}", implClass.getSimpleName());
        } catch (NoSuchMethodException ignored) {}

        if (getBoundsMethod == null) {
            boundsField = findBoundsField(implClass);
            if (boundsField != null) {
                boundsField.setAccessible(true);
                LOGGER.debug("Found bounds field in {}", implClass.getSimpleName());
            }
        }
        LOGGER.info("REIHelper reflection cache initialized");
    }

    // Cached per-frame for performance.
    public static Rectangle getSearchFieldBounds(TextField searchField) {
        if (searchField == null) {
            return null;
        }

        init(searchField);

        try {
            if (getBoundsMethod != null) {
                try {
                    return (Rectangle) getBoundsMethod.invoke(searchField);
                } catch (Exception e) {
                    LOGGER.debug("getBounds invoke failed: {}", e.getMessage());
                }
            }

            if (boundsField != null) {
                Object fieldValue = boundsField.get(searchField);
                if (fieldValue instanceof Rectangle) {
                    return (Rectangle) fieldValue;
                }
            }

        } catch (Exception e) {
            LOGGER.debug("Reflection bounds fetch failed: {}", e.getMessage());
        }

        return null;
    }


    private static Field findBoundsField(Class<?> clazz) {
        String[] fieldNames = {"bounds", "bound", "rectangle", "area", "rect"};


        for (String fieldName : fieldNames) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {}
        }


        Class<?> superClass = clazz.getSuperclass();
        while (superClass != null && !superClass.equals(Object.class)) {
            for (String fieldName : fieldNames) {
                try {
                    return superClass.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignored) {}
            }
            superClass = superClass.getSuperclass();
        }

        return null;
    }
}
