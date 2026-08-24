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

import com.rijz.notenoughcalculator.NotEnoughCalculator;
import com.rijz.notenoughcalculator.client.NotEnoughCalculatorClient;
import com.rijz.notenoughcalculator.client.gui.tab.SettingsTab;
import com.rijz.notenoughcalculator.client.gui.tab.VariablesTab;
import com.rijz.notenoughcalculator.client.integration.IntegrationManager;
import com.rijz.notenoughcalculator.client.integration.SearchFieldAdapter;
import com.rijz.notenoughcalculator.client.util.ReflectionUtils;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public class CalculatorConfigScreen extends Screen {

    private final Screen parent;
    private int activeTab = 0;
    public int varPage = 0;

    public boolean showInlineResults;
    public boolean showUnitSuggestions;
    public boolean enableCommaFormatting;
    public boolean enableHistoryNavigation;
    public boolean enableShorthandResults;
    public boolean enableSyntaxHighlighting;
    public boolean enableFullEquationCopy;
    public boolean enableItemListIntegration;
    public boolean forceStandaloneMode;
    public int standaloneX;
    public int standaloneY;
    public int decimalPrecision;
    public int bazaarFlipperLevel;
    public final Map<String, String> workingCustomVariables = new LinkedHashMap<>();

    private Button settingsTabBtn;
    private Button variablesTabBtn;

    private final SettingsTab settingsTab = new SettingsTab();
    private final VariablesTab variablesTab = new VariablesTab();

    private final int[] PRECISION_OPTIONS = {1, 2, 5, 10, 15, 20, 30, 50};

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
        this.enableItemListIntegration = config.enableItemListIntegration;
        this.forceStandaloneMode = config.forceStandaloneMode;
        this.standaloneX = config.standaloneX;
        this.standaloneY = config.standaloneY;
        this.decimalPrecision = config.decimalPrecision;
        this.bazaarFlipperLevel = config.bazaarFlipperLevel;
        if (config.customVariables != null) {
            this.workingCustomVariables.putAll(config.customVariables);
        }
    }

    public <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidgetPublic(T widget) {
        return addRenderableWidget(widget);
    }

    public void updateButtonLabelsPublic() {
        updateButtonLabels();
    }

    public int getNextPrecisionPublic(int current) {
        return getNextPrecision(current);
    }

    public void selectBazaarLevelPublic(int level) {
        selectBazaarLevel(level);
    }

    public Minecraft getMinecraftInstancePublic() {
        return this.minecraft;
    }

    private boolean showUnsavedWarning = false;

    public int getPanelWidth() {
        return 320;
    }

    public int getContentBottomY(int startY) {
        int contentH = (activeTab == 0) ? settingsTab.getContentHeight(this) : variablesTab.getContentHeight();
        return startY + contentH;
    }

    public int getPanelHeight() {
        int contentH = (activeTab == 0) ? settingsTab.getContentHeight(this) : variablesTab.getContentHeight();
        return 48 + contentH + 53;
    }

    public int getPanelX() {
        return (this.width - getPanelWidth()) / 2;
    }

    public int getPanelY() {
        return Math.max(2, (this.height - getPanelHeight()) / 2);
    }

    @Override
    protected void init() {
        clearWidgets();

        int panelX = getPanelX();
        int panelY = getPanelY();

        int buttonWidth = 280;
        int buttonHeight = 16;
        int tabY = panelY + 26;

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

        int startY = tabY + 22;

        if (activeTab == 0) {
            settingsTab.init(this, panelX, startY, buttonWidth, buttonHeight, this::init);
        } else {
            variablesTab.init(this, this.font, panelX, startY, this::init);
        }

        int contentBottom = getContentBottomY(startY);
        int bottomY = contentBottom + 21;
        int gap = 10;
        int saveWidth = (buttonWidth - gap) / 2;

        addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.screen.save"), btn -> {
            saveConfig();
            forceClose();
        }).bounds(panelX + 20, bottomY, saveWidth, 18)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.save")))
                .build());

        addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.screen.cancel"), btn -> {
            forceClose();
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
            settingsTab.updateLabels(this);
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
                    variablesTab.handleMouseClickOutsideInputs(mouseX, mouseY);
                }
            }
            wasMouseDown = isMouseDown;
        }
        int panelWidth = getPanelWidth();
        int panelHeight = getPanelHeight();
        int panelX = getPanelX();
        int panelY = getPanelY();

        graphics.fill(0, 0, this.width, this.height, 0xD0040711);
        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, 0xFF1E293B);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF50F172A);

        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 22, 0xFF1E293B);
        String modVersion = NotEnoughCalculator.getVersion();
        Component titleComp = Component.translatable("notenoughcalculator.config.screen.header_title", modVersion);
        graphics.text(this.font, titleComp, panelX + (panelWidth - this.font.width(titleComp)) / 2, panelY + 7, 0xFFFFFFFF, true);

        int startY = panelY + 48;

        if (activeTab == 0) {
            settingsTab.renderSectionHeaders(graphics, this.font, panelX, startY);
        } else {
            variablesTab.renderContent(graphics, this.font, this, panelX, startY);
        }

        if (showUnsavedWarning && hasUnsavedChanges()) {
            Component warn = Component.translatable("notenoughcalculator.config.screen.unsaved_warning");
            int contentBottom = getContentBottomY(startY);
            int bannerY = contentBottom + 4;
            int bannerX = panelX + 20;
            int bannerWidth = 280;
            int bannerHeight = 13;

            // Sleek ruby translucent banner with border
            graphics.fill(bannerX, bannerY, bannerX + bannerWidth, bannerY + bannerHeight, 0xD02A0808);
            graphics.fill(bannerX, bannerY, bannerX + bannerWidth, bannerY + 1, 0xFFDC2626);
            graphics.fill(bannerX, bannerY + bannerHeight - 1, bannerX + bannerWidth, bannerY + bannerHeight, 0xFFDC2626);
            graphics.fill(bannerX, bannerY, bannerX + 1, bannerY + bannerHeight, 0xFFDC2626);
            graphics.fill(bannerX + bannerWidth - 1, bannerY, bannerX + bannerWidth, bannerY + bannerHeight, 0xFFDC2626);

            int textX = bannerX + (bannerWidth - this.font.width(warn)) / 2;
            int textY = bannerY + 3;
            graphics.text(this.font, warn, textX, textY, 0xFFFFFFFF, true);
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    public boolean hasUnsavedChanges() {
        CalculatorConfig config = CalculatorConfig.getInstance();
        if (this.showInlineResults != config.showInlineResults) return true;
        if (this.showUnitSuggestions != config.showUnitSuggestions) return true;
        if (this.enableCommaFormatting != config.enableCommaFormatting) return true;
        if (this.enableHistoryNavigation != config.enableHistoryNavigation) return true;
        if (this.enableShorthandResults != config.enableShorthandResults) return true;
        if (this.enableSyntaxHighlighting != config.enableSyntaxHighlighting) return true;
        if (this.enableFullEquationCopy != config.enableFullEquationCopy) return true;
        if (this.enableItemListIntegration != config.enableItemListIntegration) return true;
        if (this.forceStandaloneMode != config.forceStandaloneMode) return true;
        if (this.standaloneX != config.standaloneX) return true;
        if (this.standaloneY != config.standaloneY) return true;
        if (this.decimalPrecision != config.decimalPrecision) return true;
        if (this.bazaarFlipperLevel != config.bazaarFlipperLevel) return true;
        Map<String, String> savedVars = config.customVariables != null ? config.customVariables : Map.of();
        return !this.workingCustomVariables.equals(savedVars);
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
        config.enableItemListIntegration = this.enableItemListIntegration;
        config.forceStandaloneMode = this.forceStandaloneMode;
        config.standaloneX = this.standaloneX;
        config.standaloneY = this.standaloneY;
        config.decimalPrecision = this.decimalPrecision;
        config.bazaarFlipperLevel = this.bazaarFlipperLevel;
        if (config.customVariables == null) {
            config.customVariables = new LinkedHashMap<>();
        }
        config.customVariables.clear();
        config.customVariables.putAll(this.workingCustomVariables);
        config.save();
        NotEnoughCalculatorClient.getCalculatorManager().reloadCustomVariables();
        SearchFieldAdapter adapter = IntegrationManager.getActiveAdapter();
        if (adapter != null) {
            NotEnoughCalculatorClient.getCalculatorManager().formatSearchBar(adapter.getText());
        }
    }

    public static void openScreen(Minecraft minecraft, Screen screen) {
        ReflectionUtils.openScreen(minecraft, screen);
    }

    private void forceClose() {
        Screen target = (this.parent instanceof ChatScreen) ? null : this.parent;
        openScreen(this.minecraft, target);
    }

    @Override
    public void onClose() {
        if (hasUnsavedChanges()) {
            this.showUnsavedWarning = true;
            return;
        }
        forceClose();
    }
}
