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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;

// Custom settings screen built with Minecraft's native widgets.
// Opens from ModMenu's "Configure" button in the mod list.
public class CalculatorConfigScreen extends Screen {

    private final Screen parent;

    // Mutable copies of config values (applied on Save)
    private boolean showInlineResults;
    private boolean showUnitSuggestions;
    private boolean enableCommaFormatting;
    private boolean enableHistoryNavigation;
    private int decimalPrecision;

    public CalculatorConfigScreen(Screen parent) {
        super(Component.translatable("notenoughcalculator.config.screen.title"));
        this.parent = parent;

        // Snapshot current config so Cancel discards changes
        CalculatorConfig config = CalculatorConfig.getInstance();
        this.showInlineResults = config.showInlineResults;
        this.showUnitSuggestions = config.showUnitSuggestions;
        this.enableCommaFormatting = config.enableCommaFormatting;
        this.enableHistoryNavigation = config.enableHistoryNavigation;
        this.decimalPrecision = config.decimalPrecision;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;
        int startY = 40;
        int spacing = 26;

        // -- Display Settings --

        addRenderableWidget(CycleButton.onOffBuilder(showInlineResults)
                .create(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight,
                        Component.translatable("notenoughcalculator.config.screen.inline_results"),
                        (btn, val) -> showInlineResults = val));

        addRenderableWidget(CycleButton.onOffBuilder(showUnitSuggestions)
                .create(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight,
                        Component.translatable("notenoughcalculator.config.screen.unit_suggestions"),
                        (btn, val) -> showUnitSuggestions = val));

        addRenderableWidget(CycleButton.onOffBuilder(enableCommaFormatting)
                .create(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight,
                        Component.translatable("notenoughcalculator.config.screen.comma_formatting"),
                        (btn, val) -> enableCommaFormatting = val));

        // -- Calculation Settings --

        addRenderableWidget(CycleButton.<Integer>builder(val -> Component.literal(String.valueOf(val)), decimalPrecision)
                .withValues(1, 2, 5, 10, 15, 20, 30, 50)
                .create(centerX - buttonWidth / 2, startY + spacing * 4, buttonWidth, buttonHeight,
                        Component.translatable("notenoughcalculator.config.screen.decimal_precision"),
                        (btn, val) -> decimalPrecision = val));

        // -- Features --

        addRenderableWidget(CycleButton.onOffBuilder(enableHistoryNavigation)
                .create(centerX - buttonWidth / 2, startY + spacing * 6, buttonWidth, buttonHeight,
                        Component.translatable("notenoughcalculator.config.screen.history_navigation"),
                        (btn, val) -> enableHistoryNavigation = val));

        // -- Save / Cancel buttons --

        int bottomY = this.height - 30;
        int gap = 10;
        int halfWidth = (buttonWidth - gap) / 2;

        addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.screen.save"),
                btn -> {
                    saveConfig();
                    closeScreen();
                }).bounds(centerX - buttonWidth / 2, bottomY, halfWidth, buttonHeight).build());

        addRenderableWidget(Button.builder(Component.translatable("notenoughcalculator.config.screen.cancel"),
                btn -> closeScreen())
                .bounds(centerX - buttonWidth / 2 + halfWidth + gap, bottomY, halfWidth, buttonHeight).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int spacing = 26;

        // Title
        graphics.text(this.font, this.title, centerX - this.font.width(this.title) / 2, 15, 0xFFFFFF, true);

        // Category headers
        int startY = 40;
        graphics.text(this.font,
                Component.translatable("notenoughcalculator.config.screen.display_header"),
                centerX - 100, startY - 12, 0xFFAA00, true);

        graphics.text(this.font,
                Component.translatable("notenoughcalculator.config.screen.calculation_header"),
                centerX - 100, startY + spacing * 3 + 2, 0xFFAA00, true);

        graphics.text(this.font,
                Component.translatable("notenoughcalculator.config.screen.features_header"),
                centerX - 100, startY + spacing * 5 + 2, 0xFFAA00, true);
    }

    private void saveConfig() {
        CalculatorConfig config = CalculatorConfig.getInstance();
        config.showInlineResults = this.showInlineResults;
        config.showUnitSuggestions = this.showUnitSuggestions;
        config.enableCommaFormatting = this.enableCommaFormatting;
        config.enableHistoryNavigation = this.enableHistoryNavigation;
        config.decimalPrecision = this.decimalPrecision;
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
