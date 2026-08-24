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
import com.rijz.notenoughcalculator.client.command.CalculatorCommands;
import com.rijz.notenoughcalculator.client.gui.overlay.CalculatorOverlayRenderer;
import com.rijz.notenoughcalculator.client.integration.CalculatorBounds;
import com.rijz.notenoughcalculator.client.integration.IntegrationManager;
import com.rijz.notenoughcalculator.client.integration.SearchFieldAdapter;
import com.rijz.notenoughcalculator.client.util.REIHelper;
import com.rijz.notenoughcalculator.client.util.ReflectionUtils;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import com.rijz.notenoughcalculator.core.ResultFormatter;
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
    private static boolean wasInScreen = false;
    private static boolean shouldRender = false;

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
            boolean hasScreen = ReflectionUtils.getCurrentScreen(client) != null;

            if (wasInWorld && !isInWorld) {
                LOGGER.info("Player left world - resetting calculator session");
                calcManager.reset();
                calcManager.clearHistory();
                shouldRender = false;
                clearSearchField();
            }

            // Commit current calculation when closing inventory screen
            if (wasInWorld && wasInScreen && !hasScreen) {
                LOGGER.debug("Screen closed - committing pending calculation");
                calcManager.commitPendingCalculationPublic();
            }

            wasInWorld = isInWorld;
            wasInScreen = hasScreen;
            shouldRender = isInWorld;
        });
    }

    private void registerScreenRendering() {
        ScreenEvents.BEFORE_INIT.register((client, screen, sw, sh) -> {
            ScreenEvents.afterExtract(screen).register((scr, context, mouseX, mouseY, delta) -> {
                if (IntegrationManager.isStandaloneActive()) {
                    var sf = IntegrationManager.getStandaloneField();
                    if (sf.isDragging()) {
                        sf.updateDrag(mouseX, mouseY);
                    }
                }
                CalculatorOverlayRenderer.renderOverlay(scr, context, calcManager, shouldRender);
            });

            ScreenMouseEvents.allowMouseClick(screen).register((scr, click) -> {
                if (IntegrationManager.isStandaloneActive()) {
                    var sf = IntegrationManager.getStandaloneField();
                    CalculatorBounds bounds = sf.getBounds();
                    if (click.x() >= bounds.x && click.x() <= bounds.getMaxX() && click.y() >= bounds.y && click.y() <= bounds.getMaxY()) {
                        sf.mouseClicked(click.x(), click.y(), click.button());
                        long window = Minecraft.getInstance().getWindow().handle();
                        boolean shiftDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
                        if (shiftDown || click.button() == 1) {
                            sf.startDragging(click.x(), click.y());
                        }
                        return false;
                    } else {
                        sf.setFocused(false);
                    }
                }
                return true;
            });

            ScreenMouseEvents.afterMouseRelease(screen).register((scr, release, handled) -> {
                if (IntegrationManager.isStandaloneActive()) {
                    IntegrationManager.getStandaloneField().mouseReleased(release.x(), release.y(), release.button());
                }
                return true;
            });

            // Intercept Enter to commit, Ctrl+Z/Y for history undo/redo
            ScreenKeyboardEvents.allowKeyPress(screen).register((scr, keyInput) -> {
                return handleKeyboardShortcutsWithCancel(scr, keyInput.key(), keyInput.scancode(), keyInput.modifiers());
            });

            // Intercept character typing when standalone field is focused so it doesn't leak to screen widgets
            ScreenKeyboardEvents.allowCharType(screen).register((scr, charInput) -> {
                if (IntegrationManager.isStandaloneActive() && IntegrationManager.getStandaloneField().isFocused()) {
                    char ch = (char) charInput.codepoint();
                    boolean handled = IntegrationManager.getStandaloneField().charTyped(ch, 0);
                    if (handled) {
                        calcManager.formatSearchBar(IntegrationManager.getStandaloneField().getText());
                    }
                    return false;
                }
                return true;
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
            if (!IntegrationManager.isStandaloneActive() && IntegrationManager.isREILoaded()) {
                REIRuntime runtime = REIRuntime.getInstance();
                if (runtime != null) {
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
                                REIHelper.clampSearchField(searchField);
                                LOGGER.debug("Enter pressed - result '{}' inserted into search bar", cleanResult);
                            }

                            return false;
                        }

                        boolean isCtrlOrCmd = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
                        boolean enableFullCopy = CalculatorConfig.getInstance().enableFullEquationCopy;

                        if (key == GLFW.GLFW_KEY_C && isCtrlOrCmd && isCalculation && hasResult && REIHelper.isNoSelection(searchField)) {
                            String result = calcManager.getLastFormattedResult();
                            if (result != null && !result.isEmpty()) {
                                String copyText = enableFullCopy ? ResultFormatter.formatEquationForCopy(searchText, result) : result;
                                Minecraft.getInstance().keyboardHandler.setClipboard(copyText);
                                LOGGER.debug("Copied '{}' to clipboard", copyText);
                                return false;
                            }
                        }

                        if (key == GLFW.GLFW_KEY_X && isCtrlOrCmd && isCalculation && hasResult && REIHelper.isNoSelection(searchField)) {
                            String result = calcManager.getLastFormattedResult();
                            if (result != null && !result.isEmpty()) {
                                String cutText = enableFullCopy ? ResultFormatter.formatEquationForCopy(searchText, result) : result;
                                Minecraft.getInstance().keyboardHandler.setClipboard(cutText);
                                searchField.setText("");
                                REIHelper.clampSearchField(searchField);
                                calcManager.formatSearchBar("");
                                LOGGER.debug("Cut '{}' to clipboard", cutText);
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

                    if (key == GLFW.GLFW_KEY_C && isCtrlOrCmd && isCalculation && hasResult && ReflectionUtils.isNoSelection(adapter)) {
                        String result = calcManager.getLastFormattedResult();
                        if (result != null && !result.isEmpty()) {
                            String copyText = enableFullCopy ? ResultFormatter.formatEquationForCopy(searchText, result) : result;
                            Minecraft.getInstance().keyboardHandler.setClipboard(copyText);
                            LOGGER.debug("Copied '{}' to clipboard", copyText);
                            return false;
                        }
                    }

                    if (key == GLFW.GLFW_KEY_X && isCtrlOrCmd && isCalculation && hasResult && ReflectionUtils.isNoSelection(adapter)) {
                        String result = calcManager.getLastFormattedResult();
                        if (result != null && !result.isEmpty()) {
                            String cutText = enableFullCopy ? ResultFormatter.formatEquationForCopy(searchText, result) : result;
                            Minecraft.getInstance().keyboardHandler.setClipboard(cutText);
                            adapter.setText("");
                            adapter.clamp();
                            calcManager.formatSearchBar("");
                            LOGGER.debug("Cut '{}' to clipboard", cutText);
                            return false;
                        }
                    }

                    if ((key == GLFW.GLFW_KEY_Z || key == GLFW.GLFW_KEY_Y) && isCtrlOrCmd) {
                        calcManager.handleKeyPress(key, modifiers);
                    }

                    if (IntegrationManager.isStandaloneActive() && IntegrationManager.getStandaloneField().isFocused()) {
                        if (key == GLFW.GLFW_KEY_ESCAPE) {
                            IntegrationManager.getStandaloneField().setFocused(false);
                            return true;
                        }
                        IntegrationManager.getStandaloneField().keyPressed(key, scancode, modifiers);
                        String text = IntegrationManager.getStandaloneField().getText();
                        calcManager.formatSearchBar(text);
                        return false;
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
                    .executes(CalculatorCommands::executeConfig)
                    .then(ClientCommands.literal("position")
                            .executes(CalculatorCommands::executePosition)));

            dispatcher.register(ClientCommands.literal("calcpos")
                    .executes(CalculatorCommands::executePosition));

            dispatcher.register(ClientCommands.literal("calccopy")
                    .then(ClientCommands.argument("text", StringArgumentType.greedyString())
                            .executes(CalculatorCommands::executeCopy)));
        });
    }

    private void clearSearchField() {
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