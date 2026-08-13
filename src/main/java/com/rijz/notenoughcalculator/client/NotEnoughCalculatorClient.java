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
import com.rijz.notenoughcalculator.client.gui.overlay.CalculatorOverlayRenderer;
import com.rijz.notenoughcalculator.client.integration.IntegrationManager;
import com.rijz.notenoughcalculator.client.integration.SearchFieldAdapter;
import com.rijz.notenoughcalculator.client.util.ReflectionUtils;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client mod initializer. Registers lifecycle event listeners, screen render hooks, and Brigadier chat commands.
 */
public class NotEnoughCalculatorClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotEnoughCalculatorClient.class);
    private static final CalculatorManager calcManager = new CalculatorManager();

    // Track world state for session-based history resets
    private static boolean wasInWorld = false;
    private static boolean shouldRender = false;
    private static boolean wasREIVisible = false;

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
            ScreenEvents.afterExtract(screen).register((scr, context, mouseX, mouseY, delta) -> {
                CalculatorOverlayRenderer.renderOverlay(scr, context, calcManager, shouldRender);
            });

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

    // Delegation wrappers for backwards compatibility
    public static void clampSearchField(TextField searchField) {
        ReflectionUtils.clampSearchField(searchField);
    }

    public static int getCursorPosition(TextField searchField) {
        return ReflectionUtils.getCursorPosition(searchField);
    }

    public static int getSelectionEnd(TextField searchField) {
        return ReflectionUtils.getSelectionEnd(searchField);
    }

    public static boolean isFullOrNoSelection(SearchFieldAdapter adapter) {
        return ReflectionUtils.isFullOrNoSelection(adapter);
    }

    public static boolean isFullOrNoSelection(TextField searchField) {
        return ReflectionUtils.isFullOrNoSelection(searchField);
    }

    public static Screen getCurrentScreen(Minecraft mc) {
        return ReflectionUtils.getCurrentScreen(mc);
    }

    // Listen for key presses and handle Enter, Ctrl+Z, Ctrl+Y, standalone editing
    private boolean handleKeyboardShortcutsWithCancel(Screen screen, int key, int scancode, int modifiers) {
        Minecraft mc = Minecraft.getInstance();

        // Make sure we're actually on the right screen and in-game
        if (ReflectionUtils.getCurrentScreen(mc) != screen || mc.level == null || mc.player == null) {
            return true; // Allow the key press
        }

        if (CalculatorOverlayRenderer.isNonGameplayScreen(screen)) {
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
                        boolean isCalculation = CalculatorManager.looksLikeCalculation(searchText);
                        boolean hasResult = calcManager.hasResult();

                        // If Enter or Numpad Enter is pressed on a calculation with a result
                        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && isCalculation && hasResult) {
                            calcManager.commitPendingCalculationPublic();

                            // Put the result into the search bar so user can continue calculating
                            String result = calcManager.getLastFormattedResult();
                            if (result != null && !result.isEmpty()) {
                                String cleanResult = result.replace(",", "");
                                searchField.setText(cleanResult);
                                ReflectionUtils.clampSearchField(searchField);
                                LOGGER.debug("Enter pressed - result '{}' inserted into search bar", cleanResult);
                            }

                            return false; // Cancel the Enter key - prevents REI from closing
                        }

                        // Ctrl+C / Cmd+C on a calculation copies full equation to clipboard (e.g. "1+1 = 2") if full or no selection
                        boolean isCtrlOrCmd = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
                        if (key == GLFW.GLFW_KEY_C && isCtrlOrCmd && isCalculation && hasResult && ReflectionUtils.isFullOrNoSelection(searchField)) {
                            String result = calcManager.getLastFormattedResult();
                            if (result != null && !result.isEmpty()) {
                                String fullEquation = searchText + " = " + result;
                                Minecraft.getInstance().keyboardHandler.setClipboard(fullEquation);
                                LOGGER.debug("Copied full equation '{}' to clipboard", fullEquation);
                                return false; // Prevent REI search box from overwriting clipboard with search bar text
                            }
                        }

                        // Ctrl+X / Cmd+X on a calculation cuts full equation to clipboard (e.g. "1+1 = 2") and clears search field if full or no selection
                        if (key == GLFW.GLFW_KEY_X && isCtrlOrCmd && isCalculation && hasResult && ReflectionUtils.isFullOrNoSelection(searchField)) {
                            String result = calcManager.getLastFormattedResult();
                            if (result != null && !result.isEmpty()) {
                                String fullEquation = searchText + " = " + result;
                                Minecraft.getInstance().keyboardHandler.setClipboard(fullEquation);
                                searchField.setText("");
                                ReflectionUtils.clampSearchField(searchField);
                                calcManager.formatSearchBar("");
                                LOGGER.debug("Cut full equation '{}' to clipboard", fullEquation);
                                return false;
                            }
                        }

                        // Handle Ctrl+Z / Cmd+Z and Ctrl+Y / Cmd+Y
                        if ((key == GLFW.GLFW_KEY_Z || key == GLFW.GLFW_KEY_Y) && isCtrlOrCmd) {
                            calcManager.handleKeyPress(key, modifiers);
                        }
                    }
                }
                return true;
            } else {
                SearchFieldAdapter adapter = IntegrationManager.getActiveAdapter();
                if (adapter != null) {
                    String searchText = adapter.getText();
                    calcManager.formatSearchBar(searchText);
                    boolean isCalculation = CalculatorManager.looksLikeCalculation(searchText);
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
                    if (key == GLFW.GLFW_KEY_C && isCtrlOrCmd && isCalculation && hasResult && ReflectionUtils.isFullOrNoSelection(adapter)) {
                        String result = calcManager.getLastFormattedResult();
                        if (result != null && !result.isEmpty()) {
                            String fullEquation = searchText + " = " + result;
                            Minecraft.getInstance().keyboardHandler.setClipboard(fullEquation);
                            LOGGER.debug("Copied full equation '{}' to clipboard", fullEquation);
                            return false;
                        }
                    }

                    if (key == GLFW.GLFW_KEY_X && isCtrlOrCmd && isCalculation && hasResult && ReflectionUtils.isFullOrNoSelection(adapter)) {
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