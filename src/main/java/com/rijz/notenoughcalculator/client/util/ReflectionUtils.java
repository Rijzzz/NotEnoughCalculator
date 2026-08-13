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

import com.rijz.notenoughcalculator.client.integration.SearchFieldAdapter;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Cross-version reflection utilities for Minecraft screen access and REI TextField state inspection.
 */
public class ReflectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    // Cache reflection lookups for TextField cursor position, text selection, and setters
    private static Field cursorField = null;
    private static Field selectionEndField = null;
    private static Method getCursorMethod = null;
    private static Method getSelectionEndMethod = null;
    private static Method setCursorMethod = null;
    private static Method setSelectionEndMethod = null;
    private static boolean reflectionInitialized = false;

    // Cache reflection fields for current screen retrieval (compat helper for 26.2+)
    private static Field mcScreenField = null;
    private static Field mcGuiField = null;
    private static Field guiScreenField = null;
    private static Method guiScreenMethod = null;
    private static boolean screenReflectionInitialized = false;

    // Set up reflection for text field cursor/selection access and modification
    private static void initReflection(TextField searchField) {
        if (reflectionInitialized) return;
        reflectionInitialized = true;

        if (searchField == null) return;
        Class<?> fieldClass = searchField.getClass();

        // Get cursor position method or field
        try {
            getCursorMethod = fieldClass.getMethod("getCursor");
            getCursorMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            String[] cursorNames = {"cursor", "cursorPosition", "cursorPos", "caretPosition"};
            for (String name : cursorNames) {
                try {
                    cursorField = findFieldInHierarchy(fieldClass, name);
                    if (cursorField != null) {
                        cursorField.setAccessible(true);
                        break;
                    }
                } catch (Exception ignored) {}
            }
        }

        // Get selection end method or field
        try {
            getSelectionEndMethod = fieldClass.getMethod("getSelectionEnd");
            getSelectionEndMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            String[] selectionNames = {"selectionEnd", "selectionEndPos", "selectionStart", "highlightPos"};
            for (String name : selectionNames) {
                try {
                    selectionEndField = findFieldInHierarchy(fieldClass, name);
                    if (selectionEndField != null) {
                        selectionEndField.setAccessible(true);
                        break;
                    }
                } catch (Exception ignored) {}
            }
        }

        // Setters for cursor position
        String[] setCursorNames = {"setCursor", "setCursorPosition", "setCaretPosition"};
        for (String name : setCursorNames) {
            try {
                setCursorMethod = fieldClass.getMethod(name, int.class);
                setCursorMethod.setAccessible(true);
                break;
            } catch (NoSuchMethodException ignored) {}
        }

        // Setters for selection
        String[] setSelectionNames = {"setSelectionEnd", "setSelectionStart", "setHighlightPos"};
        for (String name : setSelectionNames) {
            try {
                setSelectionEndMethod = fieldClass.getMethod(name, int.class);
                setSelectionEndMethod.setAccessible(true);
                break;
            } catch (NoSuchMethodException ignored) {}
        }
    }

    // Clamp REI's search field cursor and selection pointers to prevent StringIndexOutOfBoundsException crashes
    public static void clampSearchField(TextField searchField) {
        if (searchField == null) return;
        initReflection(searchField);
        String text = searchField.getText();
        int len = text != null ? text.length() : 0;

        try {
            int cursor = getCursorPosition(searchField);
            if (cursor > len || cursor < 0) {
                setRawCursor(searchField, len);
            }

            int selection = getSelectionEnd(searchField);
            if (selection > len || selection < 0) {
                setRawSelection(searchField, len);
            }
        } catch (Exception ignored) {}
    }

    public static int getCursorPosition(TextField searchField) {
        if (searchField == null) return 0;
        String text = searchField.getText();
        int len = text != null ? text.length() : 0;
        try {
            if (getCursorMethod != null) {
                Object result = getCursorMethod.invoke(searchField);
                if (result instanceof Integer) return Math.min(Math.max(0, (Integer) result), len);
            }
            if (cursorField != null) {
                Object result = cursorField.get(searchField);
                if (result instanceof Integer) return Math.min(Math.max(0, (Integer) result), len);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public static int getSelectionEnd(TextField searchField) {
        if (searchField == null) return 0;
        String text = searchField.getText();
        int len = text != null ? text.length() : 0;
        try {
            if (getSelectionEndMethod != null) {
                Object result = getSelectionEndMethod.invoke(searchField);
                if (result instanceof Integer) return Math.min(Math.max(0, (Integer) result), len);
            }
            if (selectionEndField != null) {
                Object result = selectionEndField.get(searchField);
                if (result instanceof Integer) return Math.min(Math.max(0, (Integer) result), len);
            }
        } catch (Exception ignored) {}
        return 0;
    }

    public static boolean isFullOrNoSelection(SearchFieldAdapter adapter) {
        if (adapter == null) return true;
        int cursor = adapter.getCursorPosition();
        int selection = adapter.getSelectionEnd();
        String text = adapter.getText();
        if (text == null) text = "";
        int len = text.length();

        if (cursor == selection) return true;
        int start = Math.min(cursor, selection);
        int end = Math.max(cursor, selection);
        return start == 0 && end >= len;
    }

    public static boolean isFullOrNoSelection(TextField searchField) {
        if (searchField == null) return true;
        int cursor = getCursorPosition(searchField);
        int selection = getSelectionEnd(searchField);
        String text = searchField.getText();
        if (text == null) text = "";
        int len = text.length();

        if (cursor == selection) return true;
        int start = Math.min(cursor, selection);
        int end = Math.max(cursor, selection);
        return start == 0 && end >= len;
    }

    private static void setRawCursor(TextField searchField, int pos) {
        try {
            if (setCursorMethod != null) {
                setCursorMethod.invoke(searchField, pos);
                return;
            }
            if (cursorField != null) {
                cursorField.set(searchField, pos);
            }
        } catch (Exception ignored) {}
    }

    private static void setRawSelection(TextField searchField, int pos) {
        try {
            if (setSelectionEndMethod != null) {
                setSelectionEndMethod.invoke(searchField, pos);
                return;
            }
            if (selectionEndField != null) {
                selectionEndField.set(searchField, pos);
            }
        } catch (Exception ignored) {}
    }

    // Lookup field traversing superclasses
    public static Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
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

    // Safely retrieve the current screen using reflection for cross-version 26.2+ compatibility.
    private static void initScreenReflection(Minecraft mc) {
        if (screenReflectionInitialized) return;
        screenReflectionInitialized = true;

        try {
            // Try Minecraft.screen (Minecraft 26.1 and older)
            mcScreenField = Minecraft.class.getDeclaredField("screen");
            mcScreenField.setAccessible(true);
            LOGGER.debug("Cached Minecraft.screen field successfully");
        } catch (NoSuchFieldException e1) {
            try {
                // Try mc.gui (Minecraft 26.2+)
                mcGuiField = Minecraft.class.getDeclaredField("gui");
                mcGuiField.setAccessible(true);
                LOGGER.debug("Cached Minecraft.gui field successfully");

                Object gui = mcGuiField.get(mc);
                if (gui != null) {
                    Class<?> guiClass = gui.getClass();
                    try {
                        // Try gui.screen field
                        guiScreenField = guiClass.getDeclaredField("screen");
                        guiScreenField.setAccessible(true);
                        LOGGER.debug("Cached Gui.screen field successfully");
                    } catch (NoSuchFieldException e2) {
                        try {
                            // Try gui.screen() method
                            guiScreenMethod = guiClass.getDeclaredMethod("screen");
                            guiScreenMethod.setAccessible(true);
                            LOGGER.debug("Cached Gui.screen() method successfully");
                        } catch (NoSuchMethodException e3) {
                            LOGGER.error("Failed to find screen field or method in Gui class (Minecraft 26.2+ compatibility lookup failed)");
                        }
                    }
                }
            } catch (Exception e3) {
                LOGGER.error("Failed to initialize Minecraft 26.2+ screen reflection accessors: {}", e3.getMessage(), e3);
            }
        }
    }

    public static Screen getCurrentScreen(Minecraft mc) {
        if (mc == null) return null;
        initScreenReflection(mc);

        if (mcScreenField != null) {
            try {
                return (Screen) mcScreenField.get(mc);
            } catch (Exception ignored) {}
        }

        if (mcGuiField != null) {
            try {
                Object gui = mcGuiField.get(mc);
                if (gui != null) {
                    if (guiScreenField != null) {
                        return (Screen) guiScreenField.get(gui);
                    }
                    if (guiScreenMethod != null) {
                        return (Screen) guiScreenMethod.invoke(gui);
                    }
                }
            } catch (Exception ignored) {}
        }

        return null;
    }
}
