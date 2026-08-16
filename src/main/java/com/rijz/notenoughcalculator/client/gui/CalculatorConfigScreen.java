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

package com.rijz.notenoughcalculator.client.gui;

import com.rijz.notenoughcalculator.client.NotEnoughCalculatorClient;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CalculatorConfigScreen extends Screen {

    private final Screen parent;
    private int activeTab = 0; // 0 = Settings Tab, 1 = Variables Tab
    private int varPage = 0;   // Pagination index for custom variables

    // Working state copied from config
    private boolean showInlineResults;
    private boolean showUnitSuggestions;
    private boolean enableCommaFormatting;
    private boolean enableHistoryNavigation;
    private boolean enableShorthandResults;
    private boolean enableSyntaxHighlighting;
    private boolean enableFullEquationCopy;
    private int decimalPrecision;
    private int bazaarFlipperLevel;
    private final Map<String, String> workingCustomVariables = new LinkedHashMap<>();

    // Settings Tab Widgets
    private Button settingsTabBtn;
    private Button variablesTabBtn;

    private Button inlineBtn;
    private Button unitBtn;
    private Button commaBtn;
    private Button shorthandBtn;
    private Button syntaxBtn;
    private Button precisionBtn;
    private Button bazaarLvl0Btn;
    private Button bazaarLvl1Btn;
    private Button bazaarLvl2Btn;
    private Button historyBtn;
    private Button fullCopyBtn;

    // Variables Tab Widgets
    private EditBox varNameInput;
    private EditBox varValueInput;
    private Button addVarBtn;
    private Button prevPageBtn;
    private Button nextPageBtn;
    private final List<Button> removeVarBtns = new ArrayList<>();

    // Common Action Buttons
    private Button saveBtn;
    private Button cancelBtn;

    private final int[] PRECISION_OPTIONS = {1, 2, 5, 10, 15, 20, 30, 50};
    private static final int VARS_PER_PAGE = 6;

    public CalculatorConfigScreen(Screen parent) {
        super(Component.translatable("notenoughcalculator.config.screen.title"));
        this.parent = parent;

        CalculatorConfig config = CalculatorConfig.getInstance();
        this.showInlineResults = config.showInlineResults;
        this.showUnitSuggestions = config.showUnitSuggestions;
        this.enableCommaFormatting = config.enableCommaFormatting;
        this.enableHistoryNavigation = config.enableHistoryNavigation;
        this.enableShorthandResults = config.enableShorthandResults;
        this.enableSyntaxHighlighting = config.enableSyntaxHighlighting;
        this.enableFullEquationCopy = config.enableFullEquationCopy;
        this.decimalPrecision = config.decimalPrecision;
        this.bazaarFlipperLevel = config.bazaarFlipperLevel;
        if (config.customVariables != null) {
            this.workingCustomVariables.putAll(config.customVariables);
        }
    }

    @Override
    protected void init() {
        clearWidgets();
        removeVarBtns.clear();

        int panelWidth = 320;
        int panelHeight = 295;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        int buttonWidth = 280;
        int buttonHeight = 16;
        int tabY = panelY + 26;

        // Top Navigation Tabs
        int tabWidth = (buttonWidth - 10) / 2;
        settingsTabBtn = addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.tab.settings"), btn -> {
            activeTab = 0;
            init();
        }).bounds(panelX + 20, tabY, tabWidth, 18)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.tab_settings")))
                .build());

        variablesTabBtn = addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.tab.variables"), btn -> {
            activeTab = 1;
            init();
        }).bounds(panelX + 20 + tabWidth + 10, tabY, tabWidth, 18)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.tab_variables")))
                .build());

        int startY = tabY + 22; // panelY + 48

        if (activeTab == 0) {
            // Settings Tab
            inlineBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                showInlineResults = !showInlineResults;
                updateButtonLabels();
            }).bounds(panelX + 20, startY + 14, buttonWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.inline_results")))
                    .build());

            unitBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                showUnitSuggestions = !showUnitSuggestions;
                updateButtonLabels();
            }).bounds(panelX + 20, startY + 32, buttonWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.unit_suggestions")))
                    .build());

            commaBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                enableCommaFormatting = !enableCommaFormatting;
                updateButtonLabels();
            }).bounds(panelX + 20, startY + 50, buttonWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.comma_formatting")))
                    .build());

            shorthandBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                enableShorthandResults = !enableShorthandResults;
                updateButtonLabels();
            }).bounds(panelX + 20, startY + 68, buttonWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.shorthand_results")))
                    .build());

            syntaxBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                enableSyntaxHighlighting = !enableSyntaxHighlighting;
                updateButtonLabels();
            }).bounds(panelX + 20, startY + 86, buttonWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.syntax_highlighting")))
                    .build());

            precisionBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                decimalPrecision = getNextPrecision(decimalPrecision);
                updateButtonLabels();
            }).bounds(panelX + 20, startY + 118, buttonWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.decimal_precision")))
                    .build());

            // Bazaar Perk Pills
            int pillWidth = 88;
            int pillGap = 8;
            int pillsStartX = panelX + 20;
            int pillsY = startY + 150;
            Tooltip bazaarTooltip = Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.bazaar_flipper_perk"));

            bazaarLvl0Btn = addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_0"),
                    btn -> selectBazaarLevel(0)).bounds(pillsStartX, pillsY, pillWidth, buttonHeight)
                    .tooltip(bazaarTooltip)
                    .build());

            bazaarLvl1Btn = addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_1"),
                    btn -> selectBazaarLevel(1)).bounds(pillsStartX + pillWidth + pillGap, pillsY, pillWidth, buttonHeight)
                    .tooltip(bazaarTooltip)
                    .build());

            bazaarLvl2Btn = addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_2"),
                    btn -> selectBazaarLevel(2)).bounds(pillsStartX + (pillWidth + pillGap) * 2, pillsY, pillWidth, buttonHeight)
                    .tooltip(bazaarTooltip)
                    .build());

            historyBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                enableHistoryNavigation = !enableHistoryNavigation;
                updateButtonLabels();
            }).bounds(panelX + 20, startY + 182, buttonWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.history_navigation")))
                    .build());

            fullCopyBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                enableFullEquationCopy = !enableFullEquationCopy;
                updateButtonLabels();
            }).bounds(panelX + 20, startY + 200, buttonWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.full_equation_copy")))
                    .build());

        } else {
            // Variables Tab
            int inputY = startY + 16;
            varNameInput = new EditBox(this.font, panelX + 20, inputY, 110, 18, Component.translatable("notenoughcalculator.config.var.name_placeholder"));
            varNameInput.setHint(Component.translatable("notenoughcalculator.config.var.name_placeholder"));
            varNameInput.setMaxLength(24);
            addRenderableWidget(varNameInput);

            varValueInput = new EditBox(this.font, panelX + 136, inputY, 118, 18, Component.translatable("notenoughcalculator.config.var.value_placeholder"));
            varValueInput.setHint(Component.translatable("notenoughcalculator.config.var.value_placeholder"));
            varValueInput.setMaxLength(64);
            addRenderableWidget(varValueInput);

            addVarBtn = addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.var.add_button"), btn -> {
                String name = varNameInput.getValue().trim();
                String value = varValueInput.getValue().trim();
                if (name.startsWith("$")) name = name.substring(1);
                if (!name.isEmpty() && !value.isEmpty()) {
                    workingCustomVariables.put(name.toLowerCase(), value);
                    varNameInput.setValue("");
                    varValueInput.setValue("");
                    init();
                }
            }).bounds(panelX + 260, inputY, 40, 18)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.add_var")))
                    .build());

            // Render existing variables list with pagination
            List<Map.Entry<String, String>> varEntries = new ArrayList<>(workingCustomVariables.entrySet());
            int totalPages = Math.max(1, (int) Math.ceil((double) varEntries.size() / VARS_PER_PAGE));
            if (varPage >= totalPages) varPage = totalPages - 1;
            if (varPage < 0) varPage = 0;

            int startIndex = varPage * VARS_PER_PAGE;
            int endIndex = Math.min(startIndex + VARS_PER_PAGE, varEntries.size());

            int rowY = inputY + 24;
            for (int i = startIndex; i < endIndex; i++) {
                String varKey = varEntries.get(i).getKey();

                Button removeBtn = addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.var.remove_button"), btn -> {
                    workingCustomVariables.remove(varKey);
                    init();
                }).bounds(panelX + 265, rowY, 35, 16)
                        .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.remove_var")))
                        .build());
                removeVarBtns.add(removeBtn);

                rowY += 18;
            }

            // Page navigation buttons
            if (totalPages > 1) {
                prevPageBtn = addRenderableWidget(Button.builder(Component.literal("<"), btn -> {
                    if (varPage > 0) {
                        varPage--;
                        init();
                    }
                }).bounds(panelX + 225, inputY + 134, 20, 14).build());
                prevPageBtn.active = (varPage > 0);

                nextPageBtn = addRenderableWidget(Button.builder(Component.literal(">"), btn -> {
                    if (varPage < totalPages - 1) {
                        varPage++;
                        init();
                    }
                }).bounds(panelX + 280, inputY + 134, 20, 14).build());
                nextPageBtn.active = (varPage < totalPages - 1);
            }
        }


        int bottomY = panelY + panelHeight - 24;
        int gap = 10;
        int saveWidth = (buttonWidth - gap) / 2;

        saveBtn = addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.screen.save"), btn -> {
            saveConfig();
            closeScreen();
        }).bounds(panelX + 20, bottomY, saveWidth, 18)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.save")))
                .build());

        cancelBtn = addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.screen.cancel"), btn -> {
            closeScreen();
        }).bounds(panelX + 20 + saveWidth + gap, bottomY, saveWidth, 18)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.cancel")))
                .build());

        updateButtonLabels();
    }

    private int getNextPrecision(int current) {
        for (int i = 0; i < PRECISION_OPTIONS.length; i++) {
            if (PRECISION_OPTIONS[i] == current) {
                return PRECISION_OPTIONS[(i + 1) % PRECISION_OPTIONS.length];
            }
        }
        return 10;
    }

    private void selectBazaarLevel(int level) {
        this.bazaarFlipperLevel = level;
        updateButtonLabels();
    }

    private void updateButtonLabels() {
        Component onText = Component.translatable("notenoughcalculator.config.screen.on");
        Component offText = Component.translatable("notenoughcalculator.config.screen.off");

        if (settingsTabBtn != null) {
            settingsTabBtn.setMessage(activeTab == 0 ?
                    Component.translatable("notenoughcalculator.config.tab.settings_selected") :
                    Component.translatable("notenoughcalculator.config.tab.settings"));
        }
        if (variablesTabBtn != null) {
            variablesTabBtn.setMessage(activeTab == 1 ?
                    Component.translatable("notenoughcalculator.config.tab.variables_selected") :
                    Component.translatable("notenoughcalculator.config.tab.variables"));
        }

        if (activeTab == 0) {
            if (inlineBtn != null) {
                inlineBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.inline_results").copy().append(": ").append(showInlineResults ? onText : offText));
            }
            if (unitBtn != null) {
                unitBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.unit_suggestions").copy().append(": ").append(showUnitSuggestions ? onText : offText));
            }
            if (commaBtn != null) {
                commaBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.comma_formatting").copy().append(": ").append(enableCommaFormatting ? onText : offText));
            }
            if (shorthandBtn != null) {
                shorthandBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.shorthand_results").copy().append(": ").append(enableShorthandResults ? onText : offText));
            }
            if (syntaxBtn != null) {
                syntaxBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.syntax_highlighting").copy().append(": ").append(enableSyntaxHighlighting ? onText : offText));
            }
            if (precisionBtn != null) {
                precisionBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.decimal_precision").copy().append(": §e§l").append(String.valueOf(decimalPrecision)));
            }
            if (historyBtn != null) {
                historyBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.history_navigation").copy().append(": ").append(enableHistoryNavigation ? onText : offText));
            }
            if (fullCopyBtn != null) {
                fullCopyBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.full_equation_copy").copy().append(": ").append(enableFullEquationCopy ? onText : offText));
            }

            if (bazaarLvl0Btn != null) {
                bazaarLvl0Btn.setMessage(bazaarFlipperLevel == 0 ?
                        Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_0_selected") :
                        Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_0"));
            }
            if (bazaarLvl1Btn != null) {
                bazaarLvl1Btn.setMessage(bazaarFlipperLevel == 1 ?
                        Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_1_selected") :
                        Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_1"));
            }
            if (bazaarLvl2Btn != null) {
                bazaarLvl2Btn.setMessage(bazaarFlipperLevel == 2 ?
                        Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_2_selected") :
                        Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_2"));
            }
        }
    }

    private boolean wasMouseDown = false;

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {

        if (this.minecraft != null) {
            long window = this.minecraft.getWindow().handle();
            boolean isMouseDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            if (isMouseDown && !wasMouseDown) {
                if (activeTab == 1) {
                    if (varNameInput != null && !varNameInput.isMouseOver(mouseX, mouseY)) {
                        varNameInput.setFocused(false);
                    }
                    if (varValueInput != null && !varValueInput.isMouseOver(mouseX, mouseY)) {
                        varValueInput.setFocused(false);
                    }
                }
            }
            wasMouseDown = isMouseDown;
        }
        int panelWidth = 320;
        int panelHeight = 295;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        // Clean dark panel background
        graphics.fill(0, 0, this.width, this.height, 0xD0040711);
        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, 0xFF1E293B);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF50F172A);

        // Header title bar
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 22, 0xFF1E293B);
        Component titleComp = Component.translatable("notenoughcalculator.config.screen.header_title");
        graphics.text(this.font, titleComp, panelX + (panelWidth - this.font.width(titleComp)) / 2, panelY + 7, 0xFFFFFFFF, true);

        int startY = panelY + 48;

        if (activeTab == 0) {
            // Category Headers
            graphics.text(this.font, Component.translatable("notenoughcalculator.config.screen.display_header"), panelX + 20, startY + 2, 0xFF34D399, true);
            graphics.text(this.font, Component.translatable("notenoughcalculator.config.screen.calculation_header"), panelX + 20, startY + 106, 0xFF38BDF8, true);
            graphics.text(this.font, Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_level_label"), panelX + 20, startY + 138, 0xFFCBD5E1, true);
            graphics.text(this.font, Component.translatable("notenoughcalculator.config.screen.features_header"), panelX + 20, startY + 170, 0xFFFBBF24, true);
        } else {
            // Custom Variables List Table Header
            graphics.text(this.font, Component.translatable("notenoughcalculator.config.var.section_title"), panelX + 20, startY + 2, 0xFF38BDF8, true);

            List<Map.Entry<String, String>> varEntries = new ArrayList<>(workingCustomVariables.entrySet());
            int totalPages = Math.max(1, (int) Math.ceil((double) varEntries.size() / VARS_PER_PAGE));
            int startIndex = varPage * VARS_PER_PAGE;
            int endIndex = Math.min(startIndex + VARS_PER_PAGE, varEntries.size());

            int rowY = startY + 40;
            for (int i = startIndex; i < endIndex; i++) {
                String keyStr = "$" + varEntries.get(i).getKey();
                String valStr = "= " + varEntries.get(i).getValue();

                // Truncate long variable names and formula values so text never overflows row columns
                if (this.font.width(keyStr) > 78) {
                    keyStr = this.font.plainSubstrByWidth(keyStr, 68) + "...";
                }
                if (this.font.width(valStr) > 138) {
                    valStr = this.font.plainSubstrByWidth(valStr, 128) + "...";
                }

                graphics.fill(panelX + 20, rowY, panelX + 260, rowY + 16, 0xFF1E293B);
                graphics.text(this.font, keyStr, panelX + 25, rowY + 4, 0xFF34D399, true);
                graphics.text(this.font, valStr, panelX + 110, rowY + 4, 0xFFFFFFFF, true);

                rowY += 18;
            }

            if (workingCustomVariables.isEmpty()) {
                graphics.text(this.font, Component.translatable("notenoughcalculator.config.var.empty_notice"), panelX + 20, startY + 50, 0xFF94A3B8, true);
            } else if (totalPages > 1) {
                String pageStr = (varPage + 1) + "/" + totalPages;
                graphics.text(this.font, pageStr, panelX + 250, startY + 152, 0xFFCBD5E1, true);
            }
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    private void saveConfig() {
        CalculatorConfig config = CalculatorConfig.getInstance();
        config.showInlineResults = this.showInlineResults;
        config.showUnitSuggestions = this.showUnitSuggestions;
        config.enableCommaFormatting = this.enableCommaFormatting;
        config.enableHistoryNavigation = this.enableHistoryNavigation;
        config.enableShorthandResults = this.enableShorthandResults;
        config.enableSyntaxHighlighting = this.enableSyntaxHighlighting;
        config.enableFullEquationCopy = this.enableFullEquationCopy;
        config.decimalPrecision = this.decimalPrecision;
        config.bazaarFlipperLevel = this.bazaarFlipperLevel;
        if (config.customVariables == null) {
            config.customVariables = new LinkedHashMap<>();
        }
        config.customVariables.clear();
        config.customVariables.putAll(this.workingCustomVariables);
        config.save();
        NotEnoughCalculatorClient.getCalculatorManager().reloadCustomVariables();
    }

    public static void openScreen(Minecraft minecraft, Screen screen) {
        if (minecraft != null) {
            minecraft.execute(() -> {
                try {
                    Method m = minecraft.getClass().getMethod("setScreenAndShow", Screen.class);
                    m.invoke(minecraft, screen);
                } catch (Exception e1) {
                    try {
                        Method m = minecraft.getClass().getMethod("setScreen", Screen.class);
                        m.invoke(minecraft, screen);
                    } catch (Exception ignored) {}
                }
            });
        }
    }

    private void closeScreen() {
        Screen target = (this.parent instanceof ChatScreen) ? null : this.parent;
        openScreen(this.minecraft, target);
    }

    @Override
    public void onClose() {
        closeScreen();
    }
}
