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

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.rijz.notenoughcalculator.client.command.CalculatorCommands;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.SharedSuggestionProvider;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            // Commit current calculation when closing inventory/REI overlay
            if (wasREIVisible && !isREIVisibleNow) {
                LOGGER.debug("REI overlay closed - committing pending calculation");
                calcManager.commitPendingCalculationPublic();
            }

            wasREIVisible = isREIVisibleNow;
            shouldRender = isInWorld && isREIVisibleNow;
        });
    }


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


    private boolean handleKeyboardShortcutsWithCancel(Screen screen, int key, int scancode, int modifiers) {
        Minecraft mc = Minecraft.getInstance();

        if (ReflectionUtils.getCurrentScreen(mc) != screen || mc.level == null || mc.player == null) {
            return true;
        }

        if (CalculatorOverlayRenderer.isNonGameplayScreen(screen)) {
            return true;
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

                        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && isCalculation && hasResult) {
                            calcManager.commitPendingCalculationPublic();

                            String result = calcManager.getLastFormattedResult();
                            if (result != null && !result.isEmpty()) {
                                String cleanResult = result.replace(",", "");
                                searchField.setText(cleanResult);
                                ReflectionUtils.clampSearchField(searchField);
                                LOGGER.debug("Enter pressed - result '{}' inserted into search bar", cleanResult);
                            }

                            return false;
                        }

                        boolean isCtrlOrCmd = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
                        boolean enableFullCopy = CalculatorConfig.getInstance().enableFullEquationCopy;

                        if (key == GLFW.GLFW_KEY_C && isCtrlOrCmd && isCalculation && hasResult && enableFullCopy && ReflectionUtils.isNoSelection(searchField)) {
                            String result = calcManager.getLastFormattedResult();
                            if (result != null && !result.isEmpty()) {
                                String fullEquation = searchText + " = " + result;
                                Minecraft.getInstance().keyboardHandler.setClipboard(fullEquation);
                                LOGGER.debug("Copied full equation '{}' to clipboard", fullEquation);
                                return false;
                            }
                        }

                        if (key == GLFW.GLFW_KEY_X && isCtrlOrCmd && isCalculation && hasResult && enableFullCopy && ReflectionUtils.isNoSelection(searchField)) {
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
                    boolean enableFullCopy = CalculatorConfig.getInstance().enableFullEquationCopy;

                    if (key == GLFW.GLFW_KEY_C && isCtrlOrCmd && isCalculation && hasResult && enableFullCopy && ReflectionUtils.isNoSelection(adapter)) {
                        String result = calcManager.getLastFormattedResult();
                        if (result != null && !result.isEmpty()) {
                            String fullEquation = searchText + " = " + result;
                            Minecraft.getInstance().keyboardHandler.setClipboard(fullEquation);
                            LOGGER.debug("Copied full equation '{}' to clipboard", fullEquation);
                            return false;
                        }
                    }

                    if (key == GLFW.GLFW_KEY_X && isCtrlOrCmd && isCalculation && hasResult && enableFullCopy && ReflectionUtils.isNoSelection(adapter)) {
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

                    if (IntegrationManager.isStandaloneActive() && IntegrationManager.getStandaloneField().isFocused()) {
                        if (mc.options.keyInventory.matches(InputConstants.Type.KEYSYM.getOrCreate(key))) {
                            IntegrationManager.getStandaloneField().keyPressed(key, scancode, modifiers);
                            String text = IntegrationManager.getStandaloneField().getText();
                            calcManager.formatSearchBar(text);
                            return false;
                        }
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

        return true;
    }


    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("calc")
                    .then(ClientCommands.argument("expression", StringArgumentType.greedyString())
                            .executes(CalculatorCommands::executeCalc)));

            dispatcher.register(ClientCommands.literal("calchist")
                    .executes(CalculatorCommands::executeHistory));
            dispatcher.register(ClientCommands.literal("calcclear")
                    .executes(CalculatorCommands::executeClear));

            dispatcher.register(ClientCommands.literal("calcset")
                    .then(ClientCommands.argument("variable", StringArgumentType.word())
                            .then(ClientCommands.argument("value", StringArgumentType.greedyString())
                                    .executes(CalculatorCommands::executeSet))));

            dispatcher.register(ClientCommands.literal("calchelp")
                    .executes(CalculatorCommands::executeHelp)
                    .then(ClientCommands.argument("page", StringArgumentType.word())
                            .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                    new String[]{"main", "operators", "functions", "units", "variables", "stats", "market", "tax", "examples", "config"}, builder))
                            .executes(CalculatorCommands::executeHelpPage)));

            dispatcher.register(ClientCommands.literal("calcconfig")
                    .executes(CalculatorCommands::executeConfig));

            dispatcher.register(ClientCommands.literal("calccopy")
                    .then(ClientCommands.argument("text", StringArgumentType.greedyString())
                            .executes(CalculatorCommands::executeCopy)));
        });
    }

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

    private void clearREISearchField() {
        try {
            SearchFieldAdapter adapter = IntegrationManager.getActiveAdapter();
            if (adapter != null) {
                adapter.setText("");
            }
        } catch (Exception ignored) {}
    }

    public static CalculatorManager getCalculatorManager() {
        return calcManager;
    }
}