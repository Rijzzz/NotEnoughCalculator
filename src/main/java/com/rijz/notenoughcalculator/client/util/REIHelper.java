package com.rijz.notenoughcalculator.client.util;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

// Helper for accessing REI's internal components without needing mixins
// This prevents the annoying red highlighting when you type calculations
public class REIHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(REIHelper.class);
    private static Field boundsField = null;
    private static Method getBoundsMethod = null;
    private static boolean initialized = false;

    // Pre-compiled pattern so we don't recompile on every keystroke
    private static final Pattern CALCULATION_PATTERN = Pattern.compile(
            ".*([+\\-*/^%xX()]|sqrt|abs|floor|ceil|round|ans|\\$|\\d+\\s*[kmbtseh]|\\d+\\s*(?:sc|dc|eb)).*",
            Pattern.CASE_INSENSITIVE
    );

    // Set up reflection once instead of every time we need it
    private static void init() {
        if (initialized) return;
        initialized = true;
        LOGGER.info("REIHelper initialized - will use runtime reflection");
    }

    // Get the position and size of REI's search field using reflection
    // We cache the method/field for performance since it's called frequently
    public static Rectangle getSearchFieldBounds(TextField searchField) {
        if (searchField == null) {
            return null;
        }

        init();

        try {
            Class<?> implClass = searchField.getClass();

            // Try calling a getBounds method first (this is the cleanest way)
            if (getBoundsMethod == null) {
                try {
                    getBoundsMethod = implClass.getMethod("getBounds");
                    getBoundsMethod.setAccessible(true);
                    LOGGER.debug("Found getBounds method in {}", implClass.getSimpleName());
                } catch (NoSuchMethodException ignored) {}
            }

            if (getBoundsMethod != null) {
                try {
                    return (Rectangle) getBoundsMethod.invoke(searchField);
                } catch (Exception e) {
                    LOGGER.debug("getBounds method failed: {}", e.getMessage());
                }
            }

            // Fallback: try to access a bounds field directly
            if (boundsField == null) {
                boundsField = findBoundsField(implClass);
                if (boundsField != null) {
                    boundsField.setAccessible(true);
                    LOGGER.debug("Found bounds field in {}", implClass.getSimpleName());
                }
            }

            if (boundsField != null) {
                Object fieldValue = boundsField.get(searchField);
                if (fieldValue instanceof Rectangle) {
                    return (Rectangle) fieldValue;
                }
            }

        } catch (Exception e) {
            LOGGER.debug("Failed to get bounds via reflection: {}", e.getMessage());
        }

        return null;
    }

    // Search through the class hierarchy to find a bounds-related field
    private static Field findBoundsField(Class<?> clazz) {
        String[] fieldNames = {"bounds", "bound", "rectangle", "area", "rect"};

        // Check the current class first
        for (String fieldName : fieldNames) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {}
        }

        // Walk up the inheritance chain looking for the field
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

    // Override REI's default text colors to prevent red highlighting on calculations
    // REI thinks invalid item searches should be red, but our calculations are valid!
    public static void fixTextColor(TextField searchField) {
        if (searchField == null) {
            return;
        }

        init();

        try {
            String text = searchField.getText();

            // Only override colors if it looks like a calculation
            if (text == null || text.isEmpty() || !looksLikeCalculation(text)) {
                return;
            }

            Class<?> implClass = searchField.getClass();

            // Set text color to white
            fixFieldColor(implClass, searchField, "textColor", 0xFFFFFF);
            fixFieldColor(implClass, searchField, "textColour", 0xFFFFFF);
            fixFieldColor(implClass, searchField, "editableColor", 0xFFFFFF);
            fixFieldColor(implClass, searchField, "editableColour", 0xFFFFFF);

            // Set border to gray instead of red
            fixFieldColor(implClass, searchField, "borderColor", 0xFF8B8B8B);
            fixFieldColor(implClass, searchField, "borderColour", 0xFF8B8B8B);
            fixFieldColor(implClass, searchField, "focusedBorderColor", 0xFFFFFFFF);
            fixFieldColor(implClass, searchField, "focusedBorderColour", 0xFFFFFFFF);

            // Try setter methods too in case the class uses those
            trySetColor(implClass, searchField, "setTextColor", 0xFFFFFF);
            trySetColor(implClass, searchField, "setBorderColor", 0xFF8B8B8B);
            trySetColor(implClass, searchField, "setEditableColor", 0xFFFFFF);

        } catch (Exception e) {
            LOGGER.debug("Failed to fix colors: {}", e.getMessage());
        }
    }

    // Update a color field using reflection
    private static void fixFieldColor(Class<?> implClass, TextField searchField, String fieldName, int color) {
        try {
            Field field = findFieldInHierarchy(implClass, fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(searchField, color);
            }
        } catch (Exception ignored) {}
    }

    // Try calling a setter method for the color
    private static void trySetColor(Class<?> implClass, TextField searchField, String methodName, int color) {
        try {
            Method method = implClass.getMethod(methodName, int.class);
            method.setAccessible(true);
            method.invoke(searchField, color);
        } catch (Exception e1) {
            try {
                Method method = implClass.getMethod(methodName, Integer.class);
                method.setAccessible(true);
                method.invoke(searchField, color);
            } catch (Exception ignored) {}
        }
    }

    // Look for a field in the class or any of its parent classes
    private static Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null && !superClass.equals(Object.class)) {
                return findFieldInHierarchy(superClass, fieldName);
            }
        }
        return null;
    }

    // Quick check: does this text look like a calculation?
    public static boolean looksLikeCalculation(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        return CALCULATION_PATTERN.matcher(input.toLowerCase()).matches();
    }
}