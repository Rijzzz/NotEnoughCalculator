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

import com.rijz.notenoughcalculator.config.CalculatorConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.lang.reflect.Method;

public class CalculatorConfigScreen extends Screen {

    private final Screen parent;

    // Working state copied from config (only saved when player hits Save)
    private boolean showInlineResults;
    private boolean showUnitSuggestions;
    private boolean enableCommaFormatting;
    private boolean enableHistoryNavigation;
    private boolean enableShorthandResults;
    private int decimalPrecision;
    private int bazaarFlipperLevel;

    private Button inlineBtn;
    private Button unitBtn;
    private Button commaBtn;
    private Button shorthandBtn;
    private Button precisionBtn;
    private Button bazaarLvl0Btn;
    private Button bazaarLvl1Btn;
    private Button bazaarLvl2Btn;
    private Button historyBtn;
    private Button saveBtn;
    private Button cancelBtn;

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
        this.decimalPrecision = config.decimalPrecision;
        this.bazaarFlipperLevel = config.bazaarFlipperLevel;
    }

    @Override
    protected void init() {
        int panelWidth = 310;
        int panelHeight = 260;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        int buttonWidth = 270;
        int buttonHeight = 18;
        int startY = panelY + 28;

        // Display options
        inlineBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
            showInlineResults = !showInlineResults;
            updateButtonLabels();
        }).bounds(panelX + (panelWidth - buttonWidth) / 2, startY + 14, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.inline_results")))
                .build());

        unitBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
            showUnitSuggestions = !showUnitSuggestions;
            updateButtonLabels();
        }).bounds(panelX + (panelWidth - buttonWidth) / 2, startY + 34, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.unit_suggestions")))
                .build());

        commaBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
            enableCommaFormatting = !enableCommaFormatting;
            updateButtonLabels();
        }).bounds(panelX + (panelWidth - buttonWidth) / 2, startY + 54, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.comma_formatting")))
                .build());

        shorthandBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
            enableShorthandResults = !enableShorthandResults;
            updateButtonLabels();
        }).bounds(panelX + (panelWidth - buttonWidth) / 2, startY + 74, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.shorthand_results")))
                .build());

        // Calculation precision
        precisionBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
            decimalPrecision = getNextPrecision(decimalPrecision);
            updateButtonLabels();
        }).bounds(panelX + (panelWidth - buttonWidth) / 2, startY + 110, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.decimal_precision")))
                .build());

        // Bazaar Perk level selector pills (0 = 1.25%, 1 = 1.125%, 2 = 1.0%)
        int pillWidth = 86;
        int pillGap = 6;
        int pillsStartX = panelX + (panelWidth - (pillWidth * 3 + pillGap * 2)) / 2;
        int pillsY = startY + 146;

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

        // History navigation shortcut
        historyBtn = addRenderableWidget(Button.builder(Component.empty(), btn -> {
            enableHistoryNavigation = !enableHistoryNavigation;
            updateButtonLabels();
        }).bounds(panelX + (panelWidth - buttonWidth) / 2, startY + 182, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.history_navigation")))
                .build());

        // Save & Cancel
        int bottomY = panelY + panelHeight - 24;
        int gap = 12;
        int saveWidth = (buttonWidth - gap) / 2;

        saveBtn = addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.screen.save"),
                btn -> {
                    saveConfig();
                    closeScreen();
                }).bounds(panelX + (panelWidth - buttonWidth) / 2, bottomY, saveWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.save")))
                .build());

        cancelBtn = addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.screen.cancel"),
                btn -> closeScreen()).bounds(panelX + (panelWidth - buttonWidth) / 2 + saveWidth + gap, bottomY, saveWidth, buttonHeight)
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

        if (inlineBtn != null) {
            MutableComponent label = Component.translatable("notenoughcalculator.config.screen.inline_results").copy().append(": ").append(showInlineResults ? onText : offText);
            inlineBtn.setMessage(label);
        }
        if (unitBtn != null) {
            MutableComponent label = Component.translatable("notenoughcalculator.config.screen.unit_suggestions").copy().append(": ").append(showUnitSuggestions ? onText : offText);
            unitBtn.setMessage(label);
        }
        if (commaBtn != null) {
            MutableComponent label = Component.translatable("notenoughcalculator.config.screen.comma_formatting").copy().append(": ").append(enableCommaFormatting ? onText : offText);
            commaBtn.setMessage(label);
        }
        if (shorthandBtn != null) {
            MutableComponent label = Component.translatable("notenoughcalculator.config.screen.shorthand_results").copy().append(": ").append(enableShorthandResults ? onText : offText);
            shorthandBtn.setMessage(label);
        }
        if (precisionBtn != null) {
            MutableComponent label = Component.translatable("notenoughcalculator.config.screen.decimal_precision").copy().append(": §e§l").append(String.valueOf(decimalPrecision));
            precisionBtn.setMessage(label);
        }
        if (historyBtn != null) {
            MutableComponent label = Component.translatable("notenoughcalculator.config.screen.history_navigation").copy().append(": ").append(enableHistoryNavigation ? onText : offText);
            historyBtn.setMessage(label);
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

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int panelWidth = 310;
        int panelHeight = 260;
        int panelX = (this.width - panelWidth) / 2;
        int panelY = (this.height - panelHeight) / 2;

        // Dark background and glass card frame
        graphics.fill(0, 0, this.width, this.height, 0xD0040711);
        graphics.fill(panelX - 2, panelY - 2, panelX + panelWidth + 2, panelY + panelHeight + 2, 0x4006B6D4);
        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, 0xFF1E293B);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF50F172A);

        // Header title bar
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 22, 0xFF1E293B);
        graphics.fill(panelX, panelY + 21, panelX + panelWidth, panelY + 22, 0xFF06B6D4);

        Component titleComp = Component.translatable("notenoughcalculator.config.screen.header_title");
        graphics.drawString(this.font, titleComp, panelX + (panelWidth - this.font.width(titleComp)) / 2, panelY + 7, 0xFFFFFFFF, true);

        // Category headers
        int startY = panelY + 28;
        graphics.drawString(this.font, Component.translatable("notenoughcalculator.config.screen.display_header"), panelX + 20, startY + 2, 0xFF34D399, true);
        graphics.drawString(this.font, Component.translatable("notenoughcalculator.config.screen.calculation_header"), panelX + 20, startY + 98, 0xFF38BDF8, true);
        graphics.drawString(this.font, Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_level_label"), panelX + 20, startY + 134, 0xFFCBD5E1, true);
        graphics.drawString(this.font, Component.translatable("notenoughcalculator.config.screen.features_header"), panelX + 20, startY + 170, 0xFFFBBF24, true);

        // Custom widget backgrounds & borders
        renderGlassButtonBounds(graphics, inlineBtn, mouseX, mouseY, showInlineResults);
        renderGlassButtonBounds(graphics, unitBtn, mouseX, mouseY, showUnitSuggestions);
        renderGlassButtonBounds(graphics, commaBtn, mouseX, mouseY, enableCommaFormatting);
        renderGlassButtonBounds(graphics, shorthandBtn, mouseX, mouseY, enableShorthandResults);
        renderGlassButtonBounds(graphics, precisionBtn, mouseX, mouseY, true);

        renderPillBounds(graphics, bazaarLvl0Btn, mouseX, mouseY, bazaarFlipperLevel == 0);
        renderPillBounds(graphics, bazaarLvl1Btn, mouseX, mouseY, bazaarFlipperLevel == 1);
        renderPillBounds(graphics, bazaarLvl2Btn, mouseX, mouseY, bazaarFlipperLevel == 2);

        renderGlassButtonBounds(graphics, historyBtn, mouseX, mouseY, enableHistoryNavigation);

        renderActionButtonBounds(graphics, saveBtn, mouseX, mouseY, 0xFF065F46, 0xFF10B981);
        renderActionButtonBounds(graphics, cancelBtn, mouseX, mouseY, 0xFF881337, 0xFFF43F5E);

        // Render widget text
        super.render(graphics, mouseX, mouseY, delta);
    }

    private void renderGlassButtonBounds(GuiGraphics graphics, Button btn, int mouseX, int mouseY, boolean active) {
        if (btn == null) return;
        int x = btn.getX();
        int y = btn.getY();
        int w = btn.getWidth();
        int h = btn.getHeight();
        boolean hovered = isHovered(btn, mouseX, mouseY);

        int bgColor = active ? (hovered ? 0xFF1E3A8A : 0xFF172554) : (hovered ? 0xFF334155 : 0xFF1E293B);
        int borderColor = hovered ? 0xFF38BDF8 : (active ? 0xFF3B82F6 : 0xFF475569);

        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, borderColor);
        graphics.fill(x, y, x + w, y + h, bgColor);

        if (active) {
            graphics.fill(x + w - 4, y + 3, x + w - 2, y + h - 3, 0xFF34D399);
        }
    }

    private void renderPillBounds(GuiGraphics graphics, Button btn, int mouseX, int mouseY, boolean selected) {
        if (btn == null) return;
        int x = btn.getX();
        int y = btn.getY();
        int w = btn.getWidth();
        int h = btn.getHeight();
        boolean hovered = isHovered(btn, mouseX, mouseY);

        int bgColor = selected ? (hovered ? 0xFF92400E : 0xFF78350F) : (hovered ? 0xFF334155 : 0xFF1E293B);
        int borderColor = selected ? 0xFFF59E0B : (hovered ? 0xFF38BDF8 : 0xFF475569);

        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, borderColor);
        graphics.fill(x, y, x + w, y + h, bgColor);
    }

    private void renderActionButtonBounds(GuiGraphics graphics, Button btn, int mouseX, int mouseY, int bgBase, int borderBase) {
        if (btn == null) return;
        int x = btn.getX();
        int y = btn.getY();
        int w = btn.getWidth();
        int h = btn.getHeight();
        boolean hovered = isHovered(btn, mouseX, mouseY);

        int borderColor = hovered ? 0xFFFFFFFF : borderBase;

        graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, borderColor);
        graphics.fill(x, y, x + w, y + h, bgBase);
    }

    private boolean isHovered(Button btn, int mouseX, int mouseY) {
        return btn != null && mouseX >= btn.getX() && mouseX <= btn.getX() + btn.getWidth() &&
               mouseY >= btn.getY() && mouseY <= btn.getY() + btn.getHeight();
    }

    private void saveConfig() {
        CalculatorConfig config = CalculatorConfig.getInstance();
        config.showInlineResults = this.showInlineResults;
        config.showUnitSuggestions = this.showUnitSuggestions;
        config.enableCommaFormatting = this.enableCommaFormatting;
        config.enableHistoryNavigation = this.enableHistoryNavigation;
        config.enableShorthandResults = this.enableShorthandResults;
        config.decimalPrecision = this.decimalPrecision;
        config.bazaarFlipperLevel = this.bazaarFlipperLevel;
        config.save();
    }

    public static void openScreen(net.minecraft.client.Minecraft minecraft, Screen screen) {
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
        openScreen(this.minecraft, this.parent);
    }

    @Override
    public void onClose() {
        closeScreen();
    }
}
