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
import com.rijz.notenoughcalculator.core.ExpressionEvaluator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VariablesTab {

    private static final int VARS_PER_PAGE = 6;

    private EditBox varNameInput;
    private EditBox varValueInput;
    private Button addVarBtn;
    private Button prevPageBtn;
    private Button nextPageBtn;
    private String varStatusMessage = "";
    private final List<Button> removeVarBtns = new ArrayList<>();

    public void init(CalculatorConfigScreen screen, Font font, int panelX, int startY, Runnable reinitAction) {
        removeVarBtns.clear();
        int inputY = startY + 16;

        varNameInput = new EditBox(font, panelX + 20, inputY, 110, 18, Component.translatable("notenoughcalculator.config.var.name_placeholder"));
        varNameInput.setHint(Component.translatable("notenoughcalculator.config.var.name_placeholder"));
        varNameInput.setMaxLength(24);
        screen.addRenderableWidgetPublic(varNameInput);

        varValueInput = new EditBox(font, panelX + 136, inputY, 118, 18, Component.translatable("notenoughcalculator.config.var.value_placeholder"));
        varValueInput.setHint(Component.translatable("notenoughcalculator.config.var.value_placeholder"));
        varValueInput.setMaxLength(64);
        screen.addRenderableWidgetPublic(varValueInput);

        addVarBtn = screen.addRenderableWidgetPublic(Button.builder(Component.translatable("notenoughcalculator.config.var.add_button"), btn -> {
            String name = varNameInput.getValue().trim();
            String value = varValueInput.getValue().trim();
            if (name.startsWith("$")) name = name.substring(1);
            String cleanName = name.toLowerCase();

            if (cleanName.isEmpty() || value.isEmpty()) {
                varStatusMessage = Component.translatable("notenoughcalculator.config.var.error.enter_name_val").getString();
            } else if (!cleanName.matches("^[a-zA-Z0-9_]+$")) {
                varStatusMessage = Component.translatable("notenoughcalculator.config.var.error.invalid_name", cleanName).getString();
            } else if (ExpressionEvaluator.isReservedVariable(cleanName)) {
                varStatusMessage = Component.translatable("notenoughcalculator.config.var.error.reserved", cleanName).getString();
            } else {
                screen.workingCustomVariables.put(cleanName, value);
                varNameInput.setValue("");
                varValueInput.setValue("");
                varStatusMessage = Component.translatable("notenoughcalculator.config.var.success_set", cleanName, value).getString();
                reinitAction.run();
            }
        }).bounds(panelX + 260, inputY, 40, 18)
                .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.add_var")))
                .build());

        List<Map.Entry<String, String>> varEntries = new ArrayList<>(screen.workingCustomVariables.entrySet());
        int totalPages = Math.max(1, (int) Math.ceil((double) varEntries.size() / VARS_PER_PAGE));
        if (screen.varPage >= totalPages) screen.varPage = totalPages - 1;
        if (screen.varPage < 0) screen.varPage = 0;

        int startIndex = screen.varPage * VARS_PER_PAGE;
        int endIndex = Math.min(startIndex + VARS_PER_PAGE, varEntries.size());

        int rowY = inputY + 36;
        for (int i = startIndex; i < endIndex; i++) {
            String varKey = varEntries.get(i).getKey();

            Button removeBtn = screen.addRenderableWidgetPublic(Button.builder(Component.translatable("notenoughcalculator.config.var.remove_button"), btn -> {
                screen.workingCustomVariables.remove(varKey);
                reinitAction.run();
            }).bounds(panelX + 265, rowY, 35, 16)
                    .tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.remove_var")))
                    .build());
            removeVarBtns.add(removeBtn);

            rowY += 18;
        }

        if (totalPages > 1) {
            prevPageBtn = screen.addRenderableWidgetPublic(Button.builder(Component.translatable("notenoughcalculator.config.var.prev_page"), btn -> {
                if (screen.varPage > 0) {
                    screen.varPage--;
                    reinitAction.run();
                }
            }).bounds(panelX + 225, inputY + 146, 20, 14).build());
            prevPageBtn.active = (screen.varPage > 0);

            nextPageBtn = screen.addRenderableWidgetPublic(Button.builder(Component.translatable("notenoughcalculator.config.var.next_page"), btn -> {
                if (screen.varPage < totalPages - 1) {
                    screen.varPage++;
                    reinitAction.run();
                }
            }).bounds(panelX + 280, inputY + 146, 20, 14).build());
            nextPageBtn.active = (screen.varPage < totalPages - 1);
        }
    }

    public void handleMouseClickOutsideInputs(int mouseX, int mouseY) {
        if (varNameInput != null && !varNameInput.isMouseOver(mouseX, mouseY)) {
            varNameInput.setFocused(false);
        }
        if (varValueInput != null && !varValueInput.isMouseOver(mouseX, mouseY)) {
            varValueInput.setFocused(false);
        }
    }

    public void renderContent(GuiGraphicsExtractor graphics, Font font, CalculatorConfigScreen screen, int panelX, int startY) {
        graphics.text(font, Component.translatable("notenoughcalculator.config.var.section_title"), panelX + 20, startY + 2, 0xFF38BDF8, true);
        if (varStatusMessage != null && !varStatusMessage.isEmpty()) {
            graphics.text(font, Component.literal(varStatusMessage), panelX + 20, startY + 37, 0xFFFFFFFF, false);
        }

        List<Map.Entry<String, String>> varEntries = new ArrayList<>(screen.workingCustomVariables.entrySet());
        int totalPages = Math.max(1, (int) Math.ceil((double) varEntries.size() / VARS_PER_PAGE));
        int startIndex = screen.varPage * VARS_PER_PAGE;
        int endIndex = Math.min(startIndex + VARS_PER_PAGE, varEntries.size());

        int rowY = startY + 52;
        String ellipsis = Component.translatable("notenoughcalculator.config.ellipsis").getString();
        for (int i = startIndex; i < endIndex; i++) {
            String keyStr = Component.translatable("notenoughcalculator.config.var.key_format", varEntries.get(i).getKey()).getString();
            String valStr = Component.translatable("notenoughcalculator.config.var.val_format", varEntries.get(i).getValue()).getString();

            if (font.width(keyStr) > 78) {
                keyStr = font.plainSubstrByWidth(keyStr, 68) + ellipsis;
            }
            if (font.width(valStr) > 138) {
                valStr = font.plainSubstrByWidth(valStr, 128) + ellipsis;
            }

            graphics.fill(panelX + 20, rowY, panelX + 260, rowY + 16, 0xFF1E293B);
            graphics.text(font, keyStr, panelX + 25, rowY + 4, 0xFF34D399, true);
            graphics.text(font, valStr, panelX + 110, rowY + 4, 0xFFFFFFFF, true);

            rowY += 18;
        }

        if (screen.workingCustomVariables.isEmpty()) {
            graphics.text(font, Component.translatable("notenoughcalculator.config.var.empty_notice"), panelX + 20, startY + 52, 0xFF94A3B8, true);
        } else if (totalPages > 1) {
            Component pageComp = Component.translatable("notenoughcalculator.config.var.page_format", (screen.varPage + 1), totalPages);
            graphics.text(font, pageComp, panelX + 250, startY + 164, 0xFFCBD5E1, true);
        }
    }
}
