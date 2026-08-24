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

import com.rijz.notenoughcalculator.client.gui.PositionConfigScreen;
import com.rijz.notenoughcalculator.client.gui.CalculatorConfigScreen;
import com.rijz.notenoughcalculator.client.integration.IntegrationManager;
import com.rijz.notenoughcalculator.client.util.ReflectionUtils;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
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
    private Button standaloneBtn;
    private Button itemListBtn;

    public int getContentHeight(CalculatorConfigScreen screen) {
        int height = 218;
        boolean hasOtherIntegrations = IntegrationManager.isREILoaded() || IntegrationManager.isItemListLoaded();
        if (hasOtherIntegrations) {
            height += 18;
        }
        if (!hasOtherIntegrations || screen.forceStandaloneMode) {
            height += 18;
        }
        if (IntegrationManager.isItemListLoaded()) {
            height += 18;
        }
        return height;
    }

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

        int currY = startY + 182;
        historyBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
            screen.enableHistoryNavigation = !screen.enableHistoryNavigation;
            screen.updateButtonLabelsPublic();
        }).bounds(panelX + 20, currY, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.history_navigation")))
                .build());
        currY += 18;

        fullCopyBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
            screen.enableFullEquationCopy = !screen.enableFullEquationCopy;
            screen.updateButtonLabelsPublic();
        }).bounds(panelX + 20, currY, buttonWidth, buttonHeight)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.full_equation_copy")))
                .build());
        currY += 18;

        boolean hasOtherIntegrations = IntegrationManager.isREILoaded() || IntegrationManager.isItemListLoaded();
        if (hasOtherIntegrations) {
            standaloneBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
                screen.forceStandaloneMode = !screen.forceStandaloneMode;
                if (reinitAction != null) {
                    reinitAction.run();
                } else {
                    screen.updateButtonLabelsPublic();
                }
            }).bounds(panelX + 20, currY, buttonWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.force_standalone_mode")))
                    .build());
            currY += 18;
        } else {
            standaloneBtn = null;
        }

        if (!hasOtherIntegrations || screen.forceStandaloneMode) {
            int resetWidth = 20;
            int editWidth = buttonWidth - resetWidth - 4;
            screen.addRenderableWidgetPublic(Button.builder(Component.translatable("notenoughcalculator.config.screen.edit_gui"), btn -> {
                ReflectionUtils.openScreen(screen.getMinecraftInstancePublic(), new PositionConfigScreen(screen));
            }).bounds(panelX + 20, currY, editWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.edit_gui")))
                    .build());

            screen.addRenderableWidgetPublic(Button.builder(Component.translatable("notenoughcalculator.config.screen.reset_symbol"), btn -> {
                screen.standaloneX = -1;
                screen.standaloneY = -1;
                CalculatorConfig.getInstance().resetPosition();
                CalculatorConfig.getInstance().save();
                screen.updateButtonLabelsPublic();
            }).bounds(panelX + 20 + editWidth + 4, currY, resetWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.reset_position")))
                    .build());
            currY += 18;
        }

        if (IntegrationManager.isItemListLoaded()) {
            itemListBtn = screen.addRenderableWidgetPublic(Button.builder(Component.empty(), btn -> {
                screen.enableItemListIntegration = !screen.enableItemListIntegration;
                screen.updateButtonLabelsPublic();
            }).bounds(panelX + 20, currY, buttonWidth, buttonHeight)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.itemlist_integration")))
                    .build());
        } else {
            itemListBtn = null;
        }
    }

    public void updateLabels(CalculatorConfigScreen screen) {
        Component onText = Component.translatable("notenoughcalculator.config.screen.on");
        Component offText = Component.translatable("notenoughcalculator.config.screen.off");

        if (inlineBtn != null) {
            inlineBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.toggle_format",
                    Component.translatable("notenoughcalculator.config.screen.inline_results"),
                    screen.showInlineResults ? onText : offText));
        }
        if (unitBtn != null) {
            unitBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.toggle_format",
                    Component.translatable("notenoughcalculator.config.screen.unit_suggestions"),
                    screen.showUnitSuggestions ? onText : offText));
        }
        if (commaBtn != null) {
            commaBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.toggle_format",
                    Component.translatable("notenoughcalculator.config.screen.comma_formatting"),
                    screen.enableCommaFormatting ? onText : offText));
        }
        if (shorthandBtn != null) {
            shorthandBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.toggle_format",
                    Component.translatable("notenoughcalculator.config.screen.shorthand_results"),
                    screen.enableShorthandResults ? onText : offText));
        }
        if (syntaxBtn != null) {
            syntaxBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.toggle_format",
                    Component.translatable("notenoughcalculator.config.screen.syntax_highlighting"),
                    screen.enableSyntaxHighlighting ? onText : offText));
        }
        if (precisionBtn != null) {
            precisionBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.decimal_precision").copy().append(Component.translatable("notenoughcalculator.config.screen.decimal_precision_format", String.valueOf(screen.decimalPrecision))));
        }
        if (historyBtn != null) {
            historyBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.toggle_format",
                    Component.translatable("notenoughcalculator.config.screen.history_navigation"),
                    screen.enableHistoryNavigation ? onText : offText));
        }
        if (fullCopyBtn != null) {
            fullCopyBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.toggle_format",
                    Component.translatable("notenoughcalculator.config.screen.full_equation_copy"),
                    screen.enableFullEquationCopy ? onText : offText));
        }
        if (standaloneBtn != null) {
            standaloneBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.toggle_format",
                    Component.translatable("notenoughcalculator.config.screen.force_standalone_mode"),
                    screen.forceStandaloneMode ? onText : offText));
        }
        if (itemListBtn != null) {
            itemListBtn.setMessage(Component.translatable("notenoughcalculator.config.screen.toggle_format",
                    Component.translatable("notenoughcalculator.config.screen.itemlist_integration"),
                    screen.enableItemListIntegration ? onText : offText));
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
