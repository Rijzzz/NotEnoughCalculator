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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    // Cache reflection fields for current screen retrieval
    private static Field mcScreenField = null;
    private static Field mcGuiField = null;
    private static Field guiScreenField = null;
    private static Method guiScreenMethod = null;
    private static boolean screenReflectionInitialized = false;

    public static int getCursorPosition(Object searchField) {
        if (searchField == null) return 0;
        try {
            Method m = searchField.getClass().getMethod("getCursorPosition");
            Object res = m.invoke(searchField);
            if (res instanceof Integer) return (Integer) res;
        } catch (Throwable ignored) {}
        try {
            Method m = searchField.getClass().getMethod("getCursor");
            Object res = m.invoke(searchField);
            if (res instanceof Integer) return (Integer) res;
        } catch (Throwable ignored) {}
        try {
            Field f = findFieldInHierarchy(searchField.getClass(), "cursor");
            if (f != null) {
                f.setAccessible(true);
                Object res = f.get(searchField);
                if (res instanceof Integer) return (Integer) res;
            }
        } catch (Throwable ignored) {}
        try {
            Field f = findFieldInHierarchy(searchField.getClass(), "cursorPosition");
            if (f != null) {
                f.setAccessible(true);
                Object res = f.get(searchField);
                if (res instanceof Integer) return (Integer) res;
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    public static int getSelectionEnd(Object searchField) {
        if (searchField == null) return 0;
        try {
            Method m = searchField.getClass().getMethod("getSelectionEnd");
            Object res = m.invoke(searchField);
            if (res instanceof Integer) return (Integer) res;
        } catch (Throwable ignored) {}
        try {
            Field f = findFieldInHierarchy(searchField.getClass(), "selectionEnd");
            if (f != null) {
                f.setAccessible(true);
                Object res = f.get(searchField);
                if (res instanceof Integer) return (Integer) res;
            }
        } catch (Throwable ignored) {}
        try {
            Field f = findFieldInHierarchy(searchField.getClass(), "highlightPos");
            if (f != null) {
                f.setAccessible(true);
                Object res = f.get(searchField);
                if (res instanceof Integer) return (Integer) res;
            }
        } catch (Throwable ignored) {}
        return getCursorPosition(searchField);
    }

    public static void clampSearchField(Object searchField) {
        if (searchField == null) return;
        try {
            String text = "";
            try {
                Method getTextMethod = searchField.getClass().getMethod("getText");
                Object t = getTextMethod.invoke(searchField);
                if (t != null) text = t.toString();
            } catch (Throwable e) {
                try {
                    Method getValueMethod = searchField.getClass().getMethod("getValue");
                    Object t = getValueMethod.invoke(searchField);
                    if (t != null) text = t.toString();
                } catch (Throwable ignored) {}
            }

            int len = text.length();
            int cursor = getCursorPosition(searchField);
            if (cursor > len || cursor < 0) {
                try {
                    Method setCursorMethod = searchField.getClass().getMethod("setCursorPosition", int.class);
                    setCursorMethod.invoke(searchField, len);
                } catch (Throwable e) {
                    try {
                        Method setCursorMethod = searchField.getClass().getMethod("setCursor", int.class);
                        setCursorMethod.invoke(searchField, len);
                    } catch (Throwable ignored) {}
                }
            }

            int selection = getSelectionEnd(searchField);
            if (selection > len || selection < 0) {
                try {
                    Method setSelectionMethod = searchField.getClass().getMethod("setSelectionEnd", int.class);
                    setSelectionMethod.invoke(searchField, len);
                } catch (Throwable e) {
                    try {
                        Method setSelectionMethod = searchField.getClass().getMethod("setHighlightPos", int.class);
                        setSelectionMethod.invoke(searchField, len);
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
    }

    public static boolean isNoSelection(SearchFieldAdapter adapter) {
        if (adapter == null) return true;
        int cursor = adapter.getCursorPosition();
        int selection = adapter.getSelectionEnd();
        return cursor == selection;
    }

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

    // Retrieve the current screen using reflection
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

    public static void openScreen(Minecraft mc, Screen screen) {
        if (mc == null) return;
        mc.execute(() -> {
            try {
                Method m = Minecraft.class.getMethod("setScreen", Screen.class);
                m.invoke(mc, screen);
                return;
            } catch (Exception ignored) {}

            try {
                Field guiField = Minecraft.class.getDeclaredField("gui");
                guiField.setAccessible(true);
                Object gui = guiField.get(mc);
                if (gui != null) {
                    Method m = gui.getClass().getMethod("setScreen", Screen.class);
                    m.invoke(gui, screen);
                    return;
                }
            } catch (Exception ignored) {}

            try {
                Method m = Minecraft.class.getMethod("setScreenAndShow", Screen.class);
                m.invoke(mc, screen);
            } catch (Exception ignored) {}
        });
    }
}
