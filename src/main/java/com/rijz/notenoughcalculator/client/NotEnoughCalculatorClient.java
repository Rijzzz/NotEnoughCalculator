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

package com.rijz.notenoughcalculator.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.rijz.notenoughcalculator.client.command.CalcCommands;
import com.rijz.notenoughcalculator.client.util.REIHelper;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import com.rijz.notenoughcalculator.client.integration.CalculatorBounds;
import com.rijz.notenoughcalculator.client.integration.IntegrationManager;
import com.rijz.notenoughcalculator.client.integration.SearchFieldAdapter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.resources.language.I18n;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

// Client setup. Registers listeners, render hooks, and chat commands.
public class NotEnoughCalculatorClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotEnoughCalculatorClient.class);
    private static final CalculatorManager calcManager = new CalculatorManager();

    // Track world state for session-based history resets
    private static boolean wasInWorld = false;
    private static boolean shouldRender = false;
    private static boolean wasREIVisible = false;

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

    @Override
    public void onInitializeClient() {
        LOGGER.info("Not Enough Calculator initializing...");

        CalculatorConfig config = CalculatorConfig.getInstance();
        LOGGER.info("Configuration loaded: precision={}", config.decimalPrecision);

        registerWorldStateTracking();
        registerScreenRendering();
        registerCommands();

        LOGGER.info("Not Enough Calculator initialized successfully!");
    }

    // Wipes session history when leaving a world/server
    private void registerWorldStateTracking() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean isInWorld = client.level != null && client.player != null;

            if (wasInWorld && !isInWorld) {
                LOGGER.info("Player left world - resetting calculator session");
                calcManager.reset();
                calcManager.clearHistory();
                shouldRender = false;
                clearREISearchField();
            }

            wasInWorld = isInWorld;

            boolean isREIVisibleNow = isREIVisible();

            // Commit the current calculation when closing the inventory/REI overlay
            if (wasREIVisible && !isREIVisibleNow) {
                LOGGER.debug("REI overlay closed - committing pending calculation");
                calcManager.commitPendingCalculationPublic();
            }

            wasREIVisible = isREIVisibleNow;
            shouldRender = isInWorld && isREIVisibleNow;
        });
    }

    // Handles overlay rendering hooks, mouse events, and keyboard events
    private void registerScreenRendering() {
        ScreenEvents.BEFORE_INIT.register((client, screen, sw, sh) -> {
            ScreenEvents.afterExtract(screen).register(this::renderCalculatorOverlay);

            ScreenMouseEvents.afterMouseClick(screen).register((scr, click, handled) -> {
                if (!IntegrationManager.isREILoaded()) {
                    IntegrationManager.getStandaloneField().mouseClicked(click.x(), click.y(), click.button());
                }
                return true;
            });

            // Intercept Enter to commit, Ctrl+Z/Y for history undo/redo
            ScreenKeyboardEvents.allowKeyPress(screen).register((scr, keyInput) -> {
                return handleKeyboardShortcutsWithCancel(scr, keyInput.key(), keyInput.scancode(), keyInput.modifiers());
            });
        });
    }

    // Cache scissor methods
    private static Method enableScissorMethod = null;
    private static Method disableScissorMethod = null;
    private static boolean scissorReflectionInitialized = false;

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

    // Safely enable scissor clipping on GuiGraphicsExtractor / GuiGraphics
    private static void enableScissor(GuiGraphicsExtractor context, int minX, int minY, int maxX, int maxY) {
        if (context == null) return;
        try {
            if (!scissorReflectionInitialized) {
                scissorReflectionInitialized = true;
                Class<?> clazz = context.getClass();
                for (Method m : clazz.getMethods()) {
                    String name = m.getName().toLowerCase();
                    if (name.contains("scissor") && m.getParameterCount() == 4) {
                        enableScissorMethod = m;
                        enableScissorMethod.setAccessible(true);
                        break;
                    }
                }
                for (Method m : clazz.getMethods()) {
                    String name = m.getName().toLowerCase();
                    if (name.contains("scissor") && m.getParameterCount() == 0) {
                        disableScissorMethod = m;
                        disableScissorMethod.setAccessible(true);
                        break;
                    }
                }
            }
            if (enableScissorMethod != null) {
                enableScissorMethod.invoke(context, minX, minY, maxX, maxY);
            }
        } catch (Exception ignored) {}
    }

    private static void disableScissor(GuiGraphicsExtractor context) {
        try {
            if (disableScissorMethod != null) {
                disableScissorMethod.invoke(context);
            }
        } catch (Exception ignored) {}
    }

    // Lookup field traversing superclasses
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

    // Main render callback for drawing results in REI or standalone search overlay
    private void renderCalculatorOverlay(Screen screen, GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();

        if (!shouldRenderCalculator(screen, mc)) return;

        try {
            if (IntegrationManager.isREILoaded()) {
                REIRuntime runtime = REIRuntime.getInstance();
                if (runtime == null || !runtime.isOverlayVisible()) return;

                ScreenOverlay overlay = runtime.getOverlay().orElse(null);
                if (overlay == null) return;

                TextField searchField = runtime.getSearchTextField();
                if (searchField == null) return;

                initReflection(searchField);
                clampSearchField(searchField);

                String searchText = searchField.getText();
                calcManager.formatSearchBar(searchText);

                if (!calcManager.looksLikeCalculation(searchText) || !calcManager.hasResult()) {
                    return;
                }

                renderREICalculatorUI(context, overlay, searchField, searchText, mc.font);
            } else {
                SearchFieldAdapter adapter = IntegrationManager.getActiveAdapter();
                if (adapter == null) return;

                String searchText = adapter.getText();
                calcManager.formatSearchBar(searchText);

                renderStandaloneCalculatorUI(context, adapter, searchText, mc.font);
            }
        } catch (Exception e) {
            // Silently swallow errors to avoid crashing Minecraft on render tick
        }
    }

    // Performs the actual overlay component drawing with horizontal scrolling and scissor clipping
    private void renderREICalculatorUI(GuiGraphicsExtractor context, ScreenOverlay overlay, TextField searchField,
                                    String searchText, Font font) {
        Rectangle overlayBounds = overlay.getBounds();
        Rectangle searchBounds = REIHelper.getSearchFieldBounds(searchField);

        if (searchBounds == null) {
            searchBounds = new Rectangle(
                    overlayBounds.x + 2,
                    overlayBounds.getMaxY() - 18,
                    overlayBounds.width - 4,
                    18
            );
        }

        clampSearchField(searchField);

        Matrix3x2fStack pose = context.pose();
        pose.pushMatrix();

        boolean isFocused = isFieldFocused(searchField);

        // Draw search field background (Pure White when focused, Unfocused Gray when unfocused)
        drawREISearchFieldBackground(context, searchBounds, isFocused);

        // Calculate horizontal text scroll offset based on cursor position
        int innerWidth = searchBounds.width - 8;
        int cursorPos = getCursorPosition(searchField);
        String textBeforeCursor = cursorPos > 0 ? searchText.substring(0, Math.min(cursorPos, searchText.length())) : "";
        int cursorXOffset = font.width(textBeforeCursor);

        int scrollOffset = 0;
        if (cursorXOffset > innerWidth - 10) {
            scrollOffset = cursorXOffset - innerWidth + 10;
        }

        int textX = searchBounds.x + 4 - scrollOffset;
        int textY = searchBounds.y + (searchBounds.height - 8) / 2;

        int selectionEnd = getSelectionEnd(searchField);
        int selectionStart = Math.min(cursorPos, selectionEnd);
        int selectionEndPos = Math.max(cursorPos, selectionEnd);
        boolean hasSelection = selectionStart != selectionEndPos;

        // Clip text rendering strictly inside search bar box
        enableScissor(context, searchBounds.x + 2, searchBounds.y + 1, searchBounds.getMaxX() - 2, searchBounds.getMaxY() - 1);

        if (hasSelection) {
            drawTextWithSelection(context, font, searchText, textX, textY, selectionStart, selectionEndPos);
        } else {
            String renderText = (CalculatorConfig.getInstance().enableSyntaxHighlighting && calcManager.looksLikeCalculation(searchText))
                    ? com.rijz.notenoughcalculator.client.util.SyntaxHighlighter.highlight(searchText)
                    : searchText;
            context.text(font, renderText, textX, textY, 0xFFFFFFFF, true);
        }

        boolean resultFitsInline = false;
        if (calcManager.hasResult()) {
            String result = calcManager.getLastFormattedResult();
            String resultDisplay = I18n.get("notenoughcalculator.result.equals") +
                    CalculatorConfig.getInstance().getResultColorCode() + result;
            int queryWidth = font.width(searchText);
            int resultX = textX + queryWidth;
            int resultWidth = font.width(resultDisplay);

            if (resultX + resultWidth <= searchBounds.getMaxX() - 4) {
                // Fits inline inside search bar
                context.text(font, resultDisplay, resultX, textY, 0xFFFFFFFF, true);
                resultFitsInline = true;
            }
        }

        if (!hasSelection && isFocused) {
            drawREICursor(context, searchBounds, searchText, font, textX, textY, cursorPos);
        }

        disableScissor(context);

        // If result overflowed search bar line, draw in clean floating box above search bar
        if (calcManager.hasResult() && !resultFitsInline) {
            String result = calcManager.getLastFormattedResult();
            String resultDisplay = I18n.get("notenoughcalculator.result.equals") +
                    CalculatorConfig.getInstance().getResultColorCode() + result;

            int aboveY = searchBounds.y - 14;
            int resultWidth = font.width(resultDisplay);

            int maxBoxWidth = overlayBounds.width - 12;
            int bgHeight = 12;
            int bgWidth = Math.min(resultWidth + 8, maxBoxWidth);

            int aboveX = searchBounds.x + 4;
            if (aboveX + bgWidth > overlayBounds.getMaxX() - 4) {
                aboveX = Math.max(overlayBounds.x + 4, overlayBounds.getMaxX() - bgWidth - 4);
            }

            int resultScroll = 0;
            int visibleWidth = bgWidth - 8;
            if (resultWidth > visibleWidth) {
                int overflowPixels = resultWidth - visibleWidth;
                long time = System.currentTimeMillis();
                // Smooth 4-second ping-pong marquee scroll back and forth
                double cycle = (time % 4000) / 4000.0;
                double normalized = (Math.sin(cycle * Math.PI * 2.0) + 1.0) / 2.0;
                resultScroll = (int) (overflowPixels * normalized);
            }

            context.fill(aboveX - 2, aboveY - 2, aboveX + bgWidth, aboveY + bgHeight - 2, 0xEE000000);

            // Clip floating box text within floating box bounds
            enableScissor(context, aboveX - 2, aboveY - 2, aboveX + bgWidth, aboveY + bgHeight - 2);
            context.text(font, resultDisplay, aboveX - resultScroll, aboveY, 0xFFFFFFFF, true);
            disableScissor(context);
        }

        pose.popMatrix();
    }

    // Performs the overlay component drawing for standalone mode
    private void renderStandaloneCalculatorUI(GuiGraphicsExtractor context, SearchFieldAdapter adapter,
                                               String searchText, Font font) {
        CalculatorBounds searchBounds = adapter.getBounds();
        if (searchBounds == null) return;

        adapter.clamp();

        Matrix3x2fStack pose = context.pose();
        pose.pushMatrix();

        boolean isFocused = adapter.isFocused();

        // Draw search field background (Pure White when focused, Unfocused Gray when unfocused)
        drawSearchFieldBackground(context, searchBounds, isFocused);

        int innerWidth = searchBounds.width - 8;
        int cursorPos = adapter.getCursorPosition();
        String textBeforeCursor = cursorPos > 0 ? searchText.substring(0, Math.min(cursorPos, searchText.length())) : "";
        int cursorXOffset = font.width(textBeforeCursor);

        int scrollOffset = 0;
        if (cursorXOffset > innerWidth - 10) {
            scrollOffset = cursorXOffset - innerWidth + 10;
        }

        int textX = searchBounds.x + 4 - scrollOffset;
        int textY = searchBounds.y + (searchBounds.height - 8) / 2;

        int selectionEnd = adapter.getSelectionEnd();
        int selectionStart = Math.min(cursorPos, selectionEnd);
        int selectionEndPos = Math.max(cursorPos, selectionEnd);
        boolean hasSelection = selectionStart != selectionEndPos;

        enableScissor(context, searchBounds.x + 2, searchBounds.y + 1, searchBounds.getMaxX() - 2, searchBounds.getMaxY() - 1);

        if (searchText.isEmpty() && !isFocused) {
            context.text(font, I18n.get("notenoughcalculator.standalone.placeholder"), textX, textY, 0x88AAAAAA, true);
        } else if (hasSelection) {
            drawTextWithSelection(context, font, searchText, textX, textY, selectionStart, selectionEndPos);
        } else {
            String renderText = (CalculatorConfig.getInstance().enableSyntaxHighlighting && calcManager.looksLikeCalculation(searchText))
                    ? com.rijz.notenoughcalculator.client.util.SyntaxHighlighter.highlight(searchText)
                    : searchText;
            context.text(font, renderText, textX, textY, 0xFFFFFFFF, true);
        }

        boolean resultFitsInline = false;
        if (calcManager.hasResult()) {
            String result = calcManager.getLastFormattedResult();
            String resultDisplay = I18n.get("notenoughcalculator.result.equals") +
                    CalculatorConfig.getInstance().getResultColorCode() + result;
            int queryWidth = font.width(searchText);
            int resultX = textX + queryWidth;
            int resultWidth = font.width(resultDisplay);

            if (resultX + resultWidth <= searchBounds.getMaxX() - 4) {
                context.text(font, resultDisplay, resultX, textY, 0xFFFFFFFF, true);
                resultFitsInline = true;
            }
        }

        if (!hasSelection && isFocused) {
            drawCursor(context, searchBounds, searchText, font, textX, textY, cursorPos);
        }

        disableScissor(context);

        if (calcManager.hasResult() && !resultFitsInline) {
            String result = calcManager.getLastFormattedResult();
            String resultDisplay = I18n.get("notenoughcalculator.result.equals") +
                    CalculatorConfig.getInstance().getResultColorCode() + result;

            int resultWidth = font.width(resultDisplay);
            int maxBoxWidth = Math.max(searchBounds.width, 160);
            int bgHeight = 12;
            int bgWidth = Math.min(resultWidth + 8, maxBoxWidth);

            int aboveY = searchBounds.y - 14;
            int aboveX = searchBounds.x + (searchBounds.width - bgWidth) / 2;

            int resultScroll = 0;
            int visibleWidth = bgWidth - 8;
            if (resultWidth > visibleWidth) {
                int overflowPixels = resultWidth - visibleWidth;
                long time = System.currentTimeMillis();
                double cycle = (time % 4000) / 4000.0;
                double normalized = (Math.sin(cycle * Math.PI * 2.0) + 1.0) / 2.0;
                resultScroll = (int) (overflowPixels * normalized);
            }

            context.fill(aboveX - 2, aboveY - 2, aboveX + bgWidth + 2, aboveY + bgHeight - 2, 0xEE000000);

            enableScissor(context, aboveX - 1, aboveY - 2, aboveX + bgWidth + 1, aboveY + bgHeight);
            context.text(font, resultDisplay, aboveX + 2 - resultScroll, aboveY, 0xFFFFFFFF, true);
            disableScissor(context);
        }

        pose.popMatrix();
    }

    // Draw text with selection highlight
    private void drawTextWithSelection(GuiGraphicsExtractor context, Font font,
                                       String text, int x, int y, int selStart, int selEnd) {
        if (text == null || text.isEmpty()) return;

        int len = text.length();
        selStart = Math.min(Math.max(0, selStart), len);
        selEnd = Math.min(Math.max(0, selEnd), len);
        if (selStart > selEnd) {
            int temp = selStart;
            selStart = selEnd;
            selEnd = temp;
        }

        String beforeSelection = selStart > 0 ? text.substring(0, selStart) : "";
        String selectedText = selEnd > selStart ? text.substring(selStart, selEnd) : "";
        String afterSelection = selEnd < len ? text.substring(selEnd) : "";

        int currentX = x;

        if (!beforeSelection.isEmpty()) {
            context.text(font, beforeSelection, currentX, y, 0xFFFFFFFF, true);
            currentX += font.width(beforeSelection);
        }

        if (!selectedText.isEmpty()) {
            int selectionWidth = font.width(selectedText);
            context.fill(currentX, y - 1, currentX + selectionWidth, y + 9, 0xFF0066CC);
            context.text(font, selectedText, currentX, y, 0xFFFFFFFF, true);
            currentX += selectionWidth;
        }

        if (!afterSelection.isEmpty()) {
            context.text(font, afterSelection, currentX, y, 0xFFFFFFFF, true);
        }
    }

    // Check if search field is focused
    private boolean isFieldFocused(TextField searchField) {
        if (searchField == null) return false;
        try {
            return searchField.isFocused();
        } catch (Throwable e) {
            try {
                java.lang.reflect.Method m = searchField.getClass().getMethod("isFocused");
                return (boolean) m.invoke(searchField);
            } catch (Throwable ignored) {}
        }
        return true;
    }

    // Draw REI search field background box
    private void drawREISearchFieldBackground(GuiGraphicsExtractor context, Rectangle bounds, boolean isFocused) {
        int borderColor = isFocused ? 0xFFFFFFFF : 0xFF8B8B8B;
        context.fill(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), borderColor);
        context.fill(bounds.x + 1, bounds.y + 1, bounds.getMaxX() - 1, bounds.getMaxY() - 1, 0xFF000000);
    }

    // Draw REI blinking text cursor
    private void drawREICursor(GuiGraphicsExtractor context, Rectangle bounds, String text,
                               Font font, int textX, int textY, int cursorPos) {
        try {
            if (text == null) text = "";
            int len = text.length();
            cursorPos = Math.min(Math.max(0, cursorPos), len);

            long time = System.currentTimeMillis();
            if ((time / 500) % 2 == 0) {
                String textBeforeCursor = cursorPos > 0 ? text.substring(0, cursorPos) : "";
                int cursorX = textX + font.width(textBeforeCursor);
                int cursorY = textY - 1;

                if (cursorX >= bounds.x + 2 && cursorX <= bounds.getMaxX() - 3) {
                    context.fill(cursorX, cursorY, cursorX + 1, cursorY + 9, 0xFFFFFFFF);
                }
            }
        } catch (Exception ignored) {}
    }

    // Draw standalone search field background box
    private void drawSearchFieldBackground(GuiGraphicsExtractor context, CalculatorBounds bounds, boolean isFocused) {
        int borderColor = isFocused ? 0xFFFFFFFF : 0xFF8B8B8B;
        context.fill(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), borderColor);
        context.fill(bounds.x + 1, bounds.y + 1, bounds.getMaxX() - 1, bounds.getMaxY() - 1, 0xFF000000);
    }

    // Draw standalone blinking text cursor
    private void drawCursor(GuiGraphicsExtractor context, CalculatorBounds bounds, String text,
                            Font font, int textX, int textY, int cursorPos) {
        try {
            if (text == null) text = "";
            int len = text.length();
            cursorPos = Math.min(Math.max(0, cursorPos), len);

            long time = System.currentTimeMillis();
            if ((time / 500) % 2 == 0) {
                String textBeforeCursor = cursorPos > 0 ? text.substring(0, cursorPos) : "";
                int cursorX = textX + font.width(textBeforeCursor);
                int cursorY = textY - 1;

                if (cursorX >= bounds.x + 2 && cursorX <= bounds.getMaxX() - 3) {
                    context.fill(cursorX, cursorY, cursorX + 1, cursorY + 9, 0xFFFFFFFF);
                }
            }
        } catch (Exception ignored) {}
    }

    // Listen for key presses and handle Enter, Ctrl+Z, Ctrl+Y, standalone editing
    private boolean handleKeyboardShortcutsWithCancel(Screen screen, int key, int scancode, int modifiers) {
        Minecraft mc = Minecraft.getInstance();

        // Make sure we're actually on the right screen and in-game
        if (getCurrentScreen(mc) != screen || mc.level == null || mc.player == null) {
            return true; // Allow the key press
        }

        if (isNonGameplayScreen(screen)) {
            return true; // Allow the key press
        }

        try {
            if (IntegrationManager.isREILoaded()) {
                REIRuntime runtime = REIRuntime.getInstance();
                if (runtime != null && runtime.isOverlayVisible()) {
                    TextField searchField = runtime.getSearchTextField();
                    if (searchField != null) {
                        String searchText = searchField.getText();
                        calcManager.formatSearchBar(searchText);
                        boolean isCalculation = calcManager.looksLikeCalculation(searchText);
                        boolean hasResult = calcManager.hasResult();

                        // If Enter or Numpad Enter is pressed on a calculation with a result
                        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && isCalculation && hasResult) {
                            calcManager.commitPendingCalculationPublic();

                            // Put the result into the search bar so user can continue calculating
                            String result = calcManager.getLastFormattedResult();
                            if (result != null && !result.isEmpty()) {
                                String cleanResult = result.replace(",", "");
                                searchField.setText(cleanResult);
                                clampSearchField(searchField);
                                LOGGER.debug("Enter pressed - result '{}' inserted into search bar", cleanResult);
                            }

                            return false; // Cancel the Enter key - prevents REI from closing
                        }

                        // Ctrl+C / Cmd+C on a calculation copies full equation to clipboard (e.g. "1+1 = 2") if full or no selection
                        boolean isCtrlOrCmd = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
                        if (key == GLFW.GLFW_KEY_C && isCtrlOrCmd && isCalculation && hasResult && isFullOrNoSelection(searchField)) {
                            String result = calcManager.getLastFormattedResult();
                            if (result != null && !result.isEmpty()) {
                                String fullEquation = searchText + " = " + result;
                                Minecraft.getInstance().keyboardHandler.setClipboard(fullEquation);
                                LOGGER.debug("Copied full equation '{}' to clipboard", fullEquation);
                                return false; // Prevent REI search box from overwriting clipboard with search bar text
                            }
                        }

                        // Ctrl+X / Cmd+X on a calculation cuts full equation to clipboard (e.g. "1+1 = 2") and clears search field if full or no selection
                        if (key == GLFW.GLFW_KEY_X && isCtrlOrCmd && isCalculation && hasResult && isFullOrNoSelection(searchField)) {
                            String result = calcManager.getLastFormattedResult();
                            if (result != null && !result.isEmpty()) {
                                String fullEquation = searchText + " = " + result;
                                Minecraft.getInstance().keyboardHandler.setClipboard(fullEquation);
                                searchField.setText("");
                                clampSearchField(searchField);
                                calcManager.formatSearchBar("");
                                LOGGER.debug("Cut full equation '{}' to clipboard", fullEquation);
                                return false;
                            }
                        }
                    }

                    // Handle Ctrl+Z / Cmd+Z and Ctrl+Y / Cmd+Y (these don't need to be cancelled)
                    boolean isCtrlOrCmd = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
                    if ((key == GLFW.GLFW_KEY_Z || key == GLFW.GLFW_KEY_Y) && isCtrlOrCmd) {
                        calcManager.handleKeyPress(key, modifiers);
                    }
                }
                return true;
            } else {
                SearchFieldAdapter adapter = IntegrationManager.getActiveAdapter();
                if (adapter != null) {
                    String searchText = adapter.getText();
                    calcManager.formatSearchBar(searchText);
                    boolean isCalculation = calcManager.looksLikeCalculation(searchText);
                    boolean hasResult = calcManager.hasResult();

                    if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && isCalculation && hasResult) {
                        calcManager.commitPendingCalculationPublic();

                        String result = calcManager.getLastFormattedResult();
                        if (result != null && !result.isEmpty()) {
                            String cleanResult = result.replace(",", "");
                            adapter.setText(cleanResult);
                            adapter.clamp();
                            LOGGER.debug("Enter pressed - result '{}' inserted into search bar", cleanResult);
                        }

                        return false;
                    }

                    boolean isCtrlOrCmd = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
                    if (key == GLFW.GLFW_KEY_C && isCtrlOrCmd && isCalculation && hasResult && isFullOrNoSelection(adapter)) {
                        String result = calcManager.getLastFormattedResult();
                        if (result != null && !result.isEmpty()) {
                            String fullEquation = searchText + " = " + result;
                            Minecraft.getInstance().keyboardHandler.setClipboard(fullEquation);
                            LOGGER.debug("Copied full equation '{}' to clipboard", fullEquation);
                            return false; // Prevent text field from overwriting clipboard with search bar text
                        }
                    }

                    if (key == GLFW.GLFW_KEY_X && isCtrlOrCmd && isCalculation && hasResult && isFullOrNoSelection(adapter)) {
                        String result = calcManager.getLastFormattedResult();
                        if (result != null && !result.isEmpty()) {
                            String fullEquation = searchText + " = " + result;
                            Minecraft.getInstance().keyboardHandler.setClipboard(fullEquation);
                            adapter.setText("");
                            adapter.clamp();
                            calcManager.formatSearchBar("");
                            LOGGER.debug("Cut full equation '{}' to clipboard", fullEquation);
                            return false;
                        }
                    }

                    if ((key == GLFW.GLFW_KEY_Z || key == GLFW.GLFW_KEY_Y) && isCtrlOrCmd) {
                        calcManager.handleKeyPress(key, modifiers);
                    }

                    boolean handled = IntegrationManager.getStandaloneField().keyPressed(key, scancode, modifiers);
                    if (handled) {
                        String text = IntegrationManager.getStandaloneField().getText();
                        calcManager.formatSearchBar(text);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error handling keyboard shortcut: {}", e.getMessage());
        }

        return true; // Allow the key press by default
    }

    // Register all our chat commands
    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            // Main calculation command
            dispatcher.register(ClientCommands.literal("calc")
                    .then(ClientCommands.argument("expression", StringArgumentType.greedyString())
                            .executes(CalcCommands::executeCalc)));

            // History management
            dispatcher.register(ClientCommands.literal("calchist")
                    .executes(CalcCommands::executeHistory));
            dispatcher.register(ClientCommands.literal("calcclear")
                    .executes(CalcCommands::executeClear));

            // Custom variables
            dispatcher.register(ClientCommands.literal("calcset")
                    .then(ClientCommands.argument("variable", StringArgumentType.word())
                            .then(ClientCommands.argument("value", StringArgumentType.greedyString())
                                    .executes(CalcCommands::executeSet))));

            // Help system
            dispatcher.register(ClientCommands.literal("calchelp")
                    .executes(CalcCommands::executeHelp)
                    .then(ClientCommands.argument("page", StringArgumentType.word())
                            .executes(CalcCommands::executeHelpPage)));

            // Configuration
            dispatcher.register(ClientCommands.literal("calcconfig")
                    .executes(CalcCommands::executeConfig));

            // Clipboard helper for click-to-copy
            dispatcher.register(ClientCommands.literal("calccopy")
                    .then(ClientCommands.argument("text", StringArgumentType.greedyString())
                            .executes(CalcCommands::executeCopy)));
        });
    }

    // Should we render the calculator right now?
    private boolean shouldRenderCalculator(Screen screen, Minecraft mc) {
        return !isNonGameplayScreen(screen)
                && getCurrentScreen(mc) == screen
                && mc.level != null
                && mc.player != null
                && shouldRender
                && CalculatorConfig.getInstance().showInlineResults;
    }

    // Safely retrieve the current screen using reflection for cross-version 26.2+ compatibility.
    // In 26.2, the 'screen' field was moved from Minecraft to Minecraft.gui.
    // We cache the reflection objects to avoid expensive lookups on every frame.
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

    // Only allow calculator in actual gameplay screens (not menus, loading screens, etc)
    // AbstractContainerScreen = inventory, chest, furnace, etc - all the in-game GUIs
    // Also allow REI recipe screens and other REI-related screens
    private static boolean isNonGameplayScreen(Screen screen) {
        if (screen instanceof AbstractContainerScreen) {
            return false; // Allow AbstractContainerScreen (inventories, chests, etc.)
        }

        // Allow REI screens (recipe viewing, etc.)
        String screenClassName = screen.getClass().getName();
        if (screenClassName.contains("rei") || screenClassName.contains("REI")) {
            return false; // Allow REI screens
        }

        // Block everything else (main menu, loading screens, etc.)
        return true;
    }

    // Is REI currently visible? (In standalone mode, returns true for gameplay screens)
    private boolean isREIVisible() {
        if (!IntegrationManager.isREILoaded()) {
            return true;
        }
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            return runtime != null && runtime.isOverlayVisible();
        } catch (Exception e) {
            return false;
        }
    }

    // Wipe the search field
    private void clearREISearchField() {
        try {
            SearchFieldAdapter adapter = IntegrationManager.getActiveAdapter();
            if (adapter != null) {
                adapter.setText("");
            }
        } catch (Exception e) {
            // Silently ignore
        }
    }

    public static CalculatorManager getCalculatorManager() {
        return calcManager;
    }
}