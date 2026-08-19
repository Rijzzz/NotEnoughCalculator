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

package com.rijz.notenoughcalculator.client.gui.tab;

import com.rijz.notenoughcalculator.client.gui.CalculatorConfigScreen;
import com.rijz.notenoughcalculator.client.integration.IntegrationManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

public class SettingsTab {

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
    private Button itemListBtn;

    public void init(CalculatorConfigScreen screen, int panelX, int startY, int buttonWidth, int buttonHeight, Runnable reinitAction) {
        inlineBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
            screen.showInlineResults = !screen.showInlineResults;
            screen.updateButtonLabelsPublic();
        }).bounds(panelX + 20, startY + 14, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.inline_results")))
                .build());

        unitBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
            screen.showUnitSuggestions = !screen.showUnitSuggestions;
            screen.updateButtonLabelsPublic();
        }).bounds(panelX + 20, startY + 32, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.unit_suggestions")))
                .build());

        commaBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
            screen.enableCommaFormatting = !screen.enableCommaFormatting;
            screen.updateButtonLabelsPublic();
        }).bounds(panelX + 20, startY + 50, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.comma_formatting")))
                .build());

        shorthandBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
            screen.enableShorthandResults = !screen.enableShorthandResults;
            screen.updateButtonLabelsPublic();
        }).bounds(panelX + 20, startY + 68, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.shorthand_results")))
                .build());

        syntaxBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
            screen.enableSyntaxHighlighting = !screen.enableSyntaxHighlighting;
            screen.updateButtonLabelsPublic();
        }).bounds(panelX + 20, startY + 86, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.syntax_highlighting")))
                .build());

        precisionBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
            screen.decimalPrecision = screen.getNextPrecisionPublic(screen.decimalPrecision);
            screen.updateButtonLabelsPublic();
        }).bounds(panelX + 20, startY + 118, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.decimal_precision")))
                .build());

        int pillWidth = 88;
        int pillGap = 8;
        int pillsStartX = panelX + 20;
        int pillsY = startY + 150;
        Tooltip bazaarTooltip = Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.bazaar_flipper_perk"));

        bazaarLvl0Btn = screen.addRenderableWidgetPublic(Button.builder(Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_0"),
                btn -> screen.selectBazaarLevelPublic(0)).bounds(pillsStartX, pillsY, pillWidth, buttonHeight)
                .tooltip(bazaarTooltip)
                .build());

        bazaarLvl1Btn = screen.addRenderableWidgetPublic(Button.builder(Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_1"),
                btn -> screen.selectBazaarLevelPublic(1)).bounds(pillsStartX + pillWidth + pillGap, pillsY, pillWidth, buttonHeight)
                .tooltip(bazaarTooltip)
                .build());

        bazaarLvl2Btn = screen.addRenderableWidgetPublic(Button.builder(Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_2"),
                btn -> screen.selectBazaarLevelPublic(2)).bounds(pillsStartX + (pillWidth + pillGap) * 2, pillsY, pillWidth, buttonHeight)
                .tooltip(bazaarTooltip)
                .build());

        historyBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
            screen.enableHistoryNavigation = !screen.enableHistoryNavigation;
            screen.updateButtonLabelsPublic();
        }).bounds(panelX + 20, startY + 182, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.history_navigation")))
                .build());

        fullCopyBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
            screen.enableFullEquationCopy = !screen.enableFullEquationCopy;
            screen.updateButtonLabelsPublic();
        }).bounds(panelX + 20, startY + 200, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.full_equation_copy")))
                .build());

        if (IntegrationManager.isItemListLoaded()) {
            itemListBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
                screen.enableItemListIntegration = !screen.enableItemListIntegration;
                screen.updateButtonLabelsPublic();
            }).bounds(panelX + 20, startY + 218, buttonWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.itemlist_integration")))
                    .build());
        }
    }

    public void updateLabels(CalculatorConfigScreen screen) {
        Component onText = Component.translatable("notenoughcalculator.config.screen.on");
        Component offText = Component.translatable("notenoughcalculator.config.screen.off");

        if (inlineBtn != null) {
            inlineBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.inline_results").copy().append(": ").append(screen.showInlineResults ? onText : offText));
        }
        if (unitBtn != null) {
            unitBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.unit_suggestions").copy().append(": ").append(screen.showUnitSuggestions ? onText : offText));
        }
        if (commaBtn != null) {
            commaBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.comma_formatting").copy().append(": ").append(screen.enableCommaFormatting ? onText : offText));
        }
        if (shorthandBtn != null) {
            shorthandBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.shorthand_results").copy().append(": ").append(screen.enableShorthandResults ? onText : offText));
        }
        if (syntaxBtn != null) {
            syntaxBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.syntax_highlighting").copy().append(": ").append(screen.enableSyntaxHighlighting ? onText : offText));
        }
        if (precisionBtn != null) {
            precisionBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.decimal_precision").copy().append(": §e§l").append(String.valueOf(screen.decimalPrecision)));
        }
        if (historyBtn != null) {
            historyBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.history_navigation").copy().append(": ").append(screen.enableHistoryNavigation ? onText : offText));
        }
        if (fullCopyBtn != null) {
            fullCopyBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.full_equation_copy").copy().append(": ").append(screen.enableFullEquationCopy ? onText : offText));
        }
        if (itemListBtn != null) {
            itemListBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.itemlist_integration").copy().append(": ").append(screen.enableItemListIntegration ? onText : offText));
        }

        if (bazaarLvl0Btn != null) {
            bazaarLvl0Btn.setMessage(screen.bazaarFlipperLevel == 0 ?
                    Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_0_selected") :
                    Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_0"));
        }
        if (bazaarLvl1Btn != null) {
            bazaarLvl1Btn.setMessage(screen.bazaarFlipperLevel == 1 ?
                    Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_1_selected") :
                    Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_1"));
        }
        if (bazaarLvl2Btn != null) {
            bazaarLvl2Btn.setMessage(screen.bazaarFlipperLevel == 2 ?
                    Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_2_selected") :
                    Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_lvl_2"));
        }
    }

    public void renderSectionHeaders(GuiGraphicsExtractor graphics, Font font, int panelX, int startY) {
        graphics.text(font, Component.translatable("notenoughcalculator.config.screen.display_header"), panelX + 20, startY + 2, 0xFF34D399, true);
        graphics.text(font, Component.translatable("notenoughcalculator.config.screen.calculation_header"), panelX + 20, startY + 106, 0xFF38BDF8, true);
        graphics.text(font, Component.translatable("notenoughcalculator.config.screen.bazaar_flipper_level_label"), panelX + 20, startY + 138, 0xFFCBD5E1, true);
        graphics.text(font, Component.translatable("notenoughcalculator.config.screen.features_header"), panelX + 20, startY + 170, 0xFFFBBF24, true);
    }
}
