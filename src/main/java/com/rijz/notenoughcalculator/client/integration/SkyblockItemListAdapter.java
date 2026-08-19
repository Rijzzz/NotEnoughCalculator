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

package com.rijz.notenoughcalculator.client.integration;

import com.rijz.notenoughcalculator.client.util.ReflectionUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class SkyblockItemListAdapter implements SearchFieldAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkyblockItemListAdapter.class);
    private static final String TARGET_CLASS = "com.operationpotato.itemlist.SkyBlockItemList";

    private Object getSearchBox() {
        try {
            Class<?> clazz = Class.forName(TARGET_CLASS);
            Object companion = clazz.getField("Companion").get(null);
            Method getInstanceMethod = companion.getClass().getMethod("getInstance");
            Object instance = getInstanceMethod.invoke(companion);
            if (instance != null) {
                Method getSearchBoxMethod = instance.getClass().getMethod("getSearchBox");
                return getSearchBoxMethod.invoke(instance);
            }
        } catch (Throwable e) {
            LOGGER.debug("Failed to obtain ItemList searchBox via reflection: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String getText() {
        Object box = getSearchBox();
        if (box != null) {
            try {
                Method getValueMethod = box.getClass().getMethod("getValue");
                Object val = getValueMethod.invoke(box);
                return val != null ? val.toString() : "";
            } catch (Throwable ignored) {}
        }
        return "";
    }

    @Override
    public void setText(String text) {
        Object box = getSearchBox();
        if (box != null) {
            try {
                Method setValueMethod = box.getClass().getMethod("setValue", String.class);
                setValueMethod.invoke(box, text != null ? text : "");
                clamp();
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public boolean isFocused() {
        Object box = getSearchBox();
        if (box != null) {
            try {
                Method isFocusedMethod = box.getClass().getMethod("isFocused");
                Object val = isFocusedMethod.invoke(box);
                return Boolean.TRUE.equals(val);
            } catch (Throwable ignored) {}
        }
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        Object box = getSearchBox();
        if (box != null) {
            try {
                Method setFocusedMethod = box.getClass().getMethod("setFocused", boolean.class);
                setFocusedMethod.invoke(box, focused);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public int getCursorPosition() {
        Object box = getSearchBox();
        if (box != null) {
            try {
                return ReflectionUtils.getCursorPosition(box);
            } catch (Throwable ignored) {}
        }
        return getText().length();
    }

    @Override
    public int getSelectionEnd() {
        Object box = getSearchBox();
        if (box != null) {
            try {
                return ReflectionUtils.getSelectionEnd(box);
            } catch (Throwable ignored) {}
        }
        return getCursorPosition();
    }

    @Override
    public void clamp() {
        Object box = getSearchBox();
        if (box != null) {
            ReflectionUtils.clampSearchField(box);
        }
    }

    @Override
    public CalculatorBounds getBounds() {
        Object box = getSearchBox();
        if (box != null) {
            try {
                int x = (int) box.getClass().getMethod("getX").invoke(box);
                int y = (int) box.getClass().getMethod("getY").invoke(box);
                int w = (int) box.getClass().getMethod("getWidth").invoke(box);
                int h = (int) box.getClass().getMethod("getHeight").invoke(box);
                return new CalculatorBounds(x, y, w, h);
            } catch (Throwable ignored) {}
        }
        return new CalculatorBounds(0, 0, 150, 16);
    }

    @Override
    public CalculatorBounds getOverlayBounds() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getWindow() != null) {
                int width = mc.getWindow().getGuiScaledWidth();
                int height = mc.getWindow().getGuiScaledHeight();
                return new CalculatorBounds(0, 0, width, height);
            }
        } catch (Throwable ignored) {}
        return getBounds();
    }
}
