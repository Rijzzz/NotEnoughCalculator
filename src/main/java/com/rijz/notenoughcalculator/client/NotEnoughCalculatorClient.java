package com.rijz.notenoughcalculator.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.rijz.notenoughcalculator.client.command.CalcCommands;
import com.rijz.notenoughcalculator.client.util.REIHelper;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

// Main client-side initialization for the calculator mod
// Sets up rendering, commands, and world state tracking
public class NotEnoughCalculatorClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotEnoughCalculatorClient.class);
    private static final CalculatorManager calcManager = new CalculatorManager();

    // Track whether player is in a world for session-based history management
    private static boolean wasInWorld = false;
    private static boolean shouldRender = false;
    private static boolean wasREIVisible = false; // Track REI visibility state

    // Cached reflection fields/methods for TextField cursor and selection
    private static Field cursorField = null;
    private static Field selectionEndField = null;
    private static Method getCursorMethod = null;
    private static Method getSelectionEndMethod = null;
    private static boolean reflectionInitialized = false;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Not Enough Calculator initializing...");

        // Load user settings from config file
        CalculatorConfig config = CalculatorConfig.getInstance();
        LOGGER.info("Configuration loaded: precision={}", config.decimalPrecision);

        registerWorldStateTracking();
        registerScreenRendering();
        registerCommands();

        LOGGER.info("Not Enough Calculator initialized successfully!");
    }

    // Monitor when player joins/leaves worlds to reset history each session
    private void registerWorldStateTracking() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            boolean isInWorld = client.world != null && client.player != null;

            // Player just left a world or server - clear everything for next session
            if (wasInWorld && !isInWorld) {
                LOGGER.info("Player left world - resetting calculator session");
                calcManager.reset();
                calcManager.clearHistory();
                shouldRender = false;

                // Wipe the REI search bar too
                clearREISearchField();
            }

            // Update tracking state
            wasInWorld = isInWorld;

            // Check if REI overlay state changed
            boolean isREIVisibleNow = isREIVisible();

            // If REI just closed (was visible, now not visible), commit any pending calculation
            if (wasREIVisible && !isREIVisibleNow) {
                LOGGER.debug("REI overlay closed - committing pending calculation");
                calcManager.commitPendingCalculationPublic();
            }

            wasREIVisible = isREIVisibleNow;

            // Only render calculator when in-game with REI open
            shouldRender = isInWorld && isREIVisibleNow;
        });
    }

    // Hook into screen rendering to draw our calculator overlay
    private void registerScreenRendering() {
        ScreenEvents.BEFORE_INIT.register((client, screen, sw, sh) -> {
            // Draw calculator results over the REI search bar
            ScreenEvents.afterRender(screen).register(this::renderCalculatorOverlay);

            // Listen for Ctrl+Z and Ctrl+Y to navigate history (and intercept Enter)
            // Use allowKeyPress to be able to cancel the event
            ScreenKeyboardEvents.allowKeyPress(screen).register((scr, keyInput) -> {
                return handleKeyboardShortcutsWithCancel(scr, keyInput.key(), keyInput.scancode(), keyInput.modifiers());
            });
        });
    }

    // Initialize reflection once for TextField cursor/selection access
    private static void initReflection(TextField searchField) {
        if (reflectionInitialized) return;
        reflectionInitialized = true;

        if (searchField == null) return;

        Class<?> fieldClass = searchField.getClass();

        // Try to find cursor position field/method
        try {
            getCursorMethod = fieldClass.getMethod("getCursor");
            getCursorMethod.setAccessible(true);
            LOGGER.debug("Found getCursor() method");
        } catch (NoSuchMethodException e) {
            // Try field access
            String[] cursorNames = {"cursor", "cursorPosition", "cursorPos", "caretPosition"};
            for (String name : cursorNames) {
                try {
                    cursorField = findFieldInHierarchy(fieldClass, name);
                    if (cursorField != null) {
                        cursorField.setAccessible(true);
                        LOGGER.debug("Found cursor field: {}", name);
                        break;
                    }
                } catch (Exception ignored) {}
            }
        }

        // Try to find selection end field/method
        try {
            getSelectionEndMethod = fieldClass.getMethod("getSelectionEnd");
            getSelectionEndMethod.setAccessible(true);
            LOGGER.debug("Found getSelectionEnd() method");
        } catch (NoSuchMethodException e) {
            // Try field access
            String[] selectionNames = {"selectionEnd", "selectionEndPos", "selectionStart", "highlightPos"};
            for (String name : selectionNames) {
                try {
                    selectionEndField = findFieldInHierarchy(fieldClass, name);
                    if (selectionEndField != null) {
                        selectionEndField.setAccessible(true);
                        LOGGER.debug("Found selection field: {}", name);
                        break;
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    // Get cursor position from TextField using reflection
    private static int getCursorPosition(TextField searchField) {
        if (searchField == null) return 0;

        try {
            if (getCursorMethod != null) {
                Object result = getCursorMethod.invoke(searchField);
                if (result instanceof Integer) {
                    return (Integer) result;
                }
            }

            if (cursorField != null) {
                Object result = cursorField.get(searchField);
                if (result instanceof Integer) {
                    return (Integer) result;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to get cursor position: {}", e.getMessage());
        }

        // Fallback: cursor at end of text
        return searchField.getText().length();
    }

    // Get selection end position from TextField using reflection
    private static int getSelectionEnd(TextField searchField) {
        if (searchField == null) return 0;

        try {
            if (getSelectionEndMethod != null) {
                Object result = getSelectionEndMethod.invoke(searchField);
                if (result instanceof Integer) {
                    return (Integer) result;
                }
            }

            if (selectionEndField != null) {
                Object result = selectionEndField.get(searchField);
                if (result instanceof Integer) {
                    return (Integer) result;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to get selection end: {}", e.getMessage());
        }

        // Fallback: no selection
        return getCursorPosition(searchField);
    }

    // Find field in class hierarchy
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

    // Draw calculation results next to the user's input in REI search
    private void renderCalculatorOverlay(Screen screen, DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // Bunch of safety checks before we try to render anything
        if (!shouldRenderCalculator(screen, mc)) {
            return;
        }

        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime == null || !runtime.isOverlayVisible()) {
                return;
            }

            ScreenOverlay overlay = runtime.getOverlay().orElse(null);
            if (overlay == null) {
                return;
            }

            TextField searchField = runtime.getSearchTextField();
            if (searchField == null) {
                return;
            }

            // Initialize reflection once
            initReflection(searchField);

            String searchText = searchField.getText();
            calcManager.formatSearchBar(searchText);

            // Only show results if this is actually a calculation with a valid answer
            if (!calcManager.looksLikeCalculation(searchText) || !calcManager.hasResult()) {
                return;
            }

            // All checks passed - draw the calculator UI
            renderCalculatorUI(context, overlay, searchField, searchText, mc.textRenderer);

        } catch (Exception e) {
            // If anything goes wrong, just silently skip this frame
        }
    }

    // Actually draw all the calculator UI elements
    private void renderCalculatorUI(DrawContext context, ScreenOverlay overlay, TextField searchField,
                                    String searchText, TextRenderer textRenderer) {
        Rectangle overlayBounds = overlay.getBounds();
        Rectangle searchBounds = REIHelper.getSearchFieldBounds(searchField);

        // If we couldn't get the real bounds, estimate where the search bar should be
        if (searchBounds == null) {
            searchBounds = new Rectangle(
                    overlayBounds.x + 2,
                    overlayBounds.getMaxY() - 18,
                    overlayBounds.width - 4,
                    18
            );
        }

        // Get the matrix stack (compatible with both 1.21.5 and 1.21.6)
        MatrixStack matrices = getMatrixStack(context);

        // Move to top layer (z-index 1000) so we draw over everything else
        matrices.push();
        matrices.translate(0, 0, 1000);

        // Draw the search field background
        drawSearchFieldBackground(context, searchBounds);

        // Figure out where to draw text
        int textX = searchBounds.x + 4;
        int textY = searchBounds.y + (searchBounds.height - 8) / 2;

        // Get cursor and selection info
        int cursorPos = getCursorPosition(searchField);
        int selectionEnd = getSelectionEnd(searchField);
        int selectionStart = Math.min(cursorPos, selectionEnd);
        int selectionEndPos = Math.max(cursorPos, selectionEnd);
        boolean hasSelection = selectionStart != selectionEndPos;

        // Draw text with selection highlight
        if (hasSelection) {
            drawTextWithSelection(context, textRenderer, searchText, textX, textY,
                    selectionStart, selectionEndPos);
        } else {
            drawText(context, textRenderer, searchText, textX, textY, 0xFFFFFFFF, true);
        }

        // Show the calculation result (moves to next line if overflow)
        if (calcManager.hasResult()) {
            drawCalculationResult(context, textRenderer, searchText, searchBounds, textX, textY);
        }

        // Draw the blinking text cursor (only if no selection)
        if (!hasSelection) {
            drawCursor(context, searchBounds, searchText, textRenderer, textX, textY, cursorPos);
        }

        // Done - restore the matrix state
        matrices.pop();
    }

    // Draw text with selection highlight
    private void drawTextWithSelection(DrawContext context, TextRenderer textRenderer,
                                       String text, int x, int y, int selStart, int selEnd) {
        if (text.isEmpty()) return;

        // Split text into three parts: before selection, selected, after selection
        String beforeSelection = selStart > 0 ? text.substring(0, selStart) : "";
        String selectedText = selEnd > selStart ? text.substring(selStart, selEnd) : "";
        String afterSelection = selEnd < text.length() ? text.substring(selEnd) : "";

        int currentX = x;

        // Draw text before selection
        if (!beforeSelection.isEmpty()) {
            drawText(context, textRenderer, beforeSelection, currentX, y, 0xFFFFFFFF, true);
            currentX += textRenderer.getWidth(beforeSelection);
        }

        // Draw selection highlight and selected text
        if (!selectedText.isEmpty()) {
            int selectionWidth = textRenderer.getWidth(selectedText);

            // Draw blue highlight background
            context.fill(currentX, y - 1, currentX + selectionWidth, y + 9, 0xFF0066CC);

            // Draw selected text in white
            drawText(context, textRenderer, selectedText, currentX, y, 0xFFFFFFFF, true);
            currentX += selectionWidth;
        }

        // Draw text after selection
        if (!afterSelection.isEmpty()) {
            drawText(context, textRenderer, afterSelection, currentX, y, 0xFFFFFFFF, true);
        }
    }

    // Get MatrixStack in a way that works for both 1.21.5 and 1.21.6
    private MatrixStack getMatrixStack(DrawContext context) {
        try {
            // Try 1.21.6 method first
            return (MatrixStack) context.getClass().getMethod("getMatrices").invoke(context);
        } catch (Exception e) {
            try {
                // Fall back to 1.21.5 method
                return (MatrixStack) context.getClass().getMethod("method_51448").invoke(context);
            } catch (Exception ex) {
                // If both fail, create a new one (fallback)
                LOGGER.warn("Could not get MatrixStack, using new instance");
                return new MatrixStack();
            }
        }
    }

    // Draw text with version compatibility
    private void drawText(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color, boolean shadow) {
        try {
            // Try 1.21.6 method signature (returns int)
            context.getClass()
                    .getMethod("drawText", TextRenderer.class, String.class, int.class, int.class, int.class, boolean.class)
                    .invoke(context, textRenderer, text, x, y, color, shadow);
        } catch (Exception e) {
            try {
                // Try 1.21.5 method signature (returns int, different method name)
                context.getClass()
                        .getMethod("method_51433", TextRenderer.class, String.class, int.class, int.class, int.class, boolean.class)
                        .invoke(context, textRenderer, text, x, y, color, shadow);
            } catch (Exception ex) {
                // Ultimate fallback - just skip drawing this text
                LOGGER.warn("Could not draw text, incompatible Minecraft version");
            }
        }
    }

    // Draw the gray border and black background for the search field
    private void drawSearchFieldBackground(DrawContext context, Rectangle bounds) {
        // Gray border (matches REI's normal style)
        context.fill(bounds.x, bounds.y, bounds.getMaxX(), bounds.getMaxY(), 0xFF8B8B8B);
        // Black inside
        context.fill(bounds.x + 1, bounds.y + 1, bounds.getMaxX() - 1, bounds.getMaxY() - 1, 0xFF000000);
    }

    // Show the calculation result
    // NEW APPROACH: If result doesn't fit on same line, show it ABOVE the search bar
    private void drawCalculationResult(DrawContext context, TextRenderer textRenderer, String searchText,
                                       Rectangle searchBounds, int textX, int textY) {
        String result = calcManager.getLastFormattedResult();
        int queryWidth = textRenderer.getWidth(searchText);
        String resultDisplay = " = " + result;

        int resultX = textX + queryWidth;
        int maxX = searchBounds.getMaxX() - 4;
        int displayWidth = textRenderer.getWidth(resultDisplay);

        // Check if result fits on the same line
        if (resultX + displayWidth <= maxX) {
            // Result fits - draw on same line
            drawText(context, textRenderer, resultDisplay, resultX, textY, 0xFFFFFFFF, true);
        } else {
            // Result doesn't fit - draw ABOVE the search bar
            int aboveY = searchBounds.y - 12; // 12 pixels above search bar
            int aboveX = searchBounds.x + 4;

            // Draw a background for the result above
            int bgHeight = 12;
            int bgWidth = Math.min(displayWidth + 8, searchBounds.width - 4);
            context.fill(aboveX - 2, aboveY - 2, aboveX + bgWidth, aboveY + bgHeight - 2, 0xCC000000);

            // Draw the result text
            drawText(context, textRenderer, resultDisplay, aboveX, aboveY, 0xFFFFFFFF, true);
        }
    }

    // Draw a blinking cursor at the correct position
    private void drawCursor(DrawContext context, Rectangle bounds, String text,
                            TextRenderer tr, int textX, int textY, int cursorPos) {
        try {
            long time = System.currentTimeMillis();
            if ((time / 500) % 2 == 0) { // Blink every half second
                // Calculate cursor X position based on cursor position in text
                String textBeforeCursor = cursorPos > 0 && cursorPos <= text.length()
                        ? text.substring(0, cursorPos)
                        : "";

                int cursorX = textX + tr.getWidth(textBeforeCursor);
                int cursorY = textY - 1;

                // Make sure cursor stays within bounds
                if (cursorX >= bounds.x + 4 && cursorX < bounds.getMaxX() - 4) {
                    context.fill(cursorX, cursorY, cursorX + 1, cursorY + 9, 0xFFFFFFFF);
                }
            }
        } catch (Exception ignored) {
            // Don't crash if cursor rendering fails
        }
    }

    // Listen for key presses and handle Enter, Ctrl+Z, Ctrl+Y
    private boolean handleKeyboardShortcutsWithCancel(Screen screen, int key, int scancode, int modifiers) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // Make sure we're actually on the right screen and in-game
        if (mc.currentScreen != screen || mc.world == null || mc.player == null) {
            return true; // Allow the key press
        }

        if (isNonGameplayScreen(screen)) {
            return true; // Allow the key press
        }

        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null && runtime.isOverlayVisible()) {
                TextField searchField = runtime.getSearchTextField();
                if (searchField != null) {
                    String searchText = searchField.getText();
                    boolean isCalculation = calcManager.looksLikeCalculation(searchText);
                    boolean hasResult = calcManager.hasResult();

                    // If Enter is pressed on a calculation with a result
                    if (key == GLFW.GLFW_KEY_ENTER && isCalculation && hasResult) {
                        calcManager.commitPendingCalculationPublic();

                        // Put the result into the search bar so user can continue calculating
                        String result = calcManager.getLastFormattedResult();
                        if (result != null && !result.isEmpty()) {
                            // Remove commas from result before inserting (e.g., "1,000" -> "1000")
                            String cleanResult = result.replace(",", "");
                            searchField.setText(cleanResult);
                            LOGGER.debug("Enter pressed - result '{}' inserted into search bar", cleanResult);
                        }

                        return false; // Cancel the Enter key - prevents REI from closing
                    }
                }

                // Handle Ctrl+Z and Ctrl+Y (these don't need to be cancelled)
                boolean isCtrlPressed = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
                if ((key == GLFW.GLFW_KEY_Z || key == GLFW.GLFW_KEY_Y) && isCtrlPressed) {
                    calcManager.handleKeyPress(key, modifiers);
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
            dispatcher.register(ClientCommandManager.literal("calc")
                    .then(ClientCommandManager.argument("expression", StringArgumentType.greedyString())
                            .executes(CalcCommands::executeCalc)));

            // History management
            dispatcher.register(ClientCommandManager.literal("calchist")
                    .executes(CalcCommands::executeHistory));
            dispatcher.register(ClientCommandManager.literal("calcclear")
                    .executes(CalcCommands::executeClear));

            // Custom variables
            dispatcher.register(ClientCommandManager.literal("calcset")
                    .then(ClientCommandManager.argument("variable", StringArgumentType.word())
                            .then(ClientCommandManager.argument("value", StringArgumentType.greedyString())
                                    .executes(CalcCommands::executeSet))));

            // Help system
            dispatcher.register(ClientCommandManager.literal("calchelp")
                    .executes(CalcCommands::executeHelp)
                    .then(ClientCommandManager.argument("page", StringArgumentType.word())
                            .executes(CalcCommands::executeHelpPage)));

            // Configuration
            dispatcher.register(ClientCommandManager.literal("calcconfig")
                    .executes(CalcCommands::executeConfig));
        });
    }

    // Should we render the calculator right now?
    private boolean shouldRenderCalculator(Screen screen, MinecraftClient mc) {
        return !isNonGameplayScreen(screen)
                && mc.currentScreen == screen
                && mc.world != null
                && mc.player != null
                && shouldRender
                && CalculatorConfig.getInstance().showInlineResults;
    }

    // Only allow calculator in actual gameplay screens (not menus, loading screens, etc)
    // HandledScreen = inventory, chest, furnace, etc - all the in-game GUIs
    // Also allow REI recipe screens and other REI-related screens
    private static boolean isNonGameplayScreen(Screen screen) {
        if (screen instanceof HandledScreen) {
            return false; // Allow HandledScreen (inventories, chests, etc.)
        }

        // Allow REI screens (recipe viewing, etc.)
        String screenClassName = screen.getClass().getName();
        if (screenClassName.contains("rei") || screenClassName.contains("REI")) {
            return false; // Allow REI screens
        }

        // Block everything else (main menu, loading screens, etc.)
        return true;
    }

    // Is REI currently visible?
    private boolean isREIVisible() {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            return runtime != null && runtime.isOverlayVisible();
        } catch (Exception e) {
            return false;
        }
    }

    // Wipe the REI search field
    private void clearREISearchField() {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null) {
                TextField searchField = runtime.getSearchTextField();
                if (searchField != null) {
                    searchField.setText("");
                }
            }
        } catch (Exception e) {
            // Silently ignore
        }
    }

    public static CalculatorManager getCalculatorManager() {
        return calcManager;
    }
}