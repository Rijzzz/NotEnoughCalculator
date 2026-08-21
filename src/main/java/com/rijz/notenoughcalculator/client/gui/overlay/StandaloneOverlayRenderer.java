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

package com.rijz.notenoughcalculator.client.gui.overlay;

import com.rijz.notenoughcalculator.client.CalculatorManager;
import com.rijz.notenoughcalculator.client.integration.CalculatorBounds;
import com.rijz.notenoughcalculator.client.integration.SearchFieldAdapter;
import com.rijz.notenoughcalculator.client.util.SyntaxHighlighter;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import com.rijz.notenoughcalculator.core.ResultFormatter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import org.joml.Matrix3x2fStack;

public class StandaloneOverlayRenderer {

    public static void render(GuiGraphicsExtractor context, SearchFieldAdapter adapter,
                              String searchText, Font font, CalculatorManager calcManager) {
        CalculatorBounds searchBounds = adapter.getBounds();
        if (searchBounds == null) return;

        adapter.clamp();

        Matrix3x2fStack pose = context.pose();
        pose.pushMatrix();

        boolean isFocused = adapter.isFocused();
        drawSearchBoxBackground(context, searchBounds.x, searchBounds.y, searchBounds.getMaxX(), searchBounds.getMaxY(), isFocused);

        int innerWidth = searchBounds.width - 8;
        int cursorPos = adapter.getCursorPosition();
        String textBeforeCursor = cursorPos > 0 ? searchText.substring(0, Math.min(cursorPos, searchText.length())) : "";
        int cursorXOffset = font.width(textBeforeCursor);

        int scrollOffset = cursorXOffset > innerWidth - 10 ? cursorXOffset - innerWidth + 10 : 0;
        int textX = searchBounds.x + 4 - scrollOffset;
        int textY = searchBounds.y + (searchBounds.height - 8) / 2;

        int selectionEnd = adapter.getSelectionEnd();
        int selectionStart = Math.min(cursorPos, selectionEnd);
        int selectionEndPos = Math.max(cursorPos, selectionEnd);
        boolean hasSelection = selectionStart != selectionEndPos;

        CalculatorOverlayRenderer.enableScissor(context, searchBounds.x + 2, searchBounds.y + 1, searchBounds.getMaxX() - 2, searchBounds.getMaxY() - 1);

        if (searchText.isEmpty() && !isFocused) {
            context.text(font, I18n.get("notenoughcalculator.standalone.placeholder"), textX, textY, 0x88AAAAAA, true);
        } else if (hasSelection) {
            drawTextWithSelection(context, font, searchText, textX, textY, selectionStart, selectionEndPos);
        } else {
            String renderText = (CalculatorConfig.getInstance().enableSyntaxHighlighting && CalculatorManager.looksLikeCalculation(searchText))
                    ? SyntaxHighlighter.highlight(searchText)
                    : searchText;
            context.text(font, renderText, textX, textY, 0xFFFFFFFF, true);
        }

        boolean resultFitsInline = false;
        boolean showInline = CalculatorConfig.getInstance().showInlineResults;
        if (calcManager.hasResult() && showInline) {
            String result = calcManager.getLastFormattedResult();
            String resultDisplay = ResultFormatter.getEqualsSign() +
                    CalculatorConfig.getInstance().getResultColorCode() + result;
            int queryWidth = font.width(searchText);
            int resultX = textX + queryWidth;
            int resultWidth = font.width(resultDisplay);

            if (resultX + resultWidth <= searchBounds.getMaxX() - 4) {
                context.text(font, resultDisplay, resultX, textY, 0xFFFFFFFF, true);
                resultFitsInline = true;
            }
        }

        if (!hasSelection && isFocused) {
            drawCursor(context, searchText, font, textX, textY, cursorPos, searchBounds.x + 2, searchBounds.getMaxX() - 3);
        }

        CalculatorOverlayRenderer.disableScissor(context);

        if (calcManager.hasResult() && !resultFitsInline && showInline) {
            String result = calcManager.getLastFormattedResult();
            String resultDisplay = ResultFormatter.getEqualsSign() +
                    CalculatorConfig.getInstance().getResultColorCode() + result;

            int resultWidth = font.width(resultDisplay);
            int maxBoxWidth = Math.max(searchBounds.width, 160);
            int bgHeight = 12;
            int bgWidth = Math.min(resultWidth + 8, maxBoxWidth);

            int aboveY = searchBounds.y - 14;
            int aboveX = searchBounds.x + (searchBounds.width - bgWidth) / 2;

            int resultScroll = 0;
            int visibleWidth = bgWidth - 8;
            if (resultWidth > visibleWidth) {
                int overflowPixels = resultWidth - visibleWidth;
                long time = System.currentTimeMillis();
                double cycle = (time % 4000) / 4000.0;
                double normalized = (Math.sin(cycle * Math.PI * 2.0) + 1.0) / 2.0;
                resultScroll = (int) (overflowPixels * normalized);
            }

            context.fill(aboveX - 2, aboveY - 2, aboveX + bgWidth + 2, aboveY + bgHeight - 2, 0xEE000000);

            CalculatorOverlayRenderer.enableScissor(context, aboveX - 1, aboveY - 2, aboveX + bgWidth + 1, aboveY + bgHeight);
            context.text(font, resultDisplay, aboveX + 2 - resultScroll, aboveY, 0xFFFFFFFF, true);
            CalculatorOverlayRenderer.disableScissor(context);
        }

        pose.popMatrix();
    }

    private static void drawTextWithSelection(GuiGraphicsExtractor context, Font font,
                                               String text, int x, int y, int selStart, int selEnd) {
        if (text == null || text.isEmpty()) return;

        int len = text.length();
        selStart = Math.min(Math.max(0, selStart), len);
        selEnd = Math.min(Math.max(0, selEnd), len);
        if (selStart > selEnd) {
            int temp = selStart;
            selStart = selEnd;
            selEnd = temp;
        }

        String beforeSelection = selStart > 0 ? text.substring(0, selStart) : "";
        String selectedText = selEnd > selStart ? text.substring(selStart, selEnd) : "";
        String afterSelection = selEnd < len ? text.substring(selEnd) : "";

        int currentX = x;

        if (!beforeSelection.isEmpty()) {
            context.text(font, beforeSelection, currentX, y, 0xFFFFFFFF, true);
            currentX += font.width(beforeSelection);
        }

        if (!selectedText.isEmpty()) {
            int selectionWidth = font.width(selectedText);
            context.fill(currentX, y - 1, currentX + selectionWidth, y + 9, 0xFF0066CC);
            context.text(font, selectedText, currentX, y, 0xFFFFFFFF, true);
            currentX += selectionWidth;
        }

        if (!afterSelection.isEmpty()) {
            context.text(font, afterSelection, currentX, y, 0xFFFFFFFF, true);
        }
    }

    private static void drawSearchBoxBackground(GuiGraphicsExtractor context, int minX, int minY, int maxX, int maxY, boolean isFocused) {
        int borderColor = isFocused ? 0xFFFFFFFF : 0xFF8B8B8B;
        context.fill(minX, minY, maxX, maxY, borderColor);
        context.fill(minX + 1, minY + 1, maxX - 1, maxY - 1, 0xFF000000);
    }

    private static void drawCursor(GuiGraphicsExtractor context, String text, Font font, int textX, int textY,
                                   int cursorPos, int minX, int maxX) {
        try {
            if (text == null) text = "";
            int len = text.length();
            cursorPos = Math.min(Math.max(0, cursorPos), len);

            long time = System.currentTimeMillis();
            if ((time / 500) % 2 == 0) {
                String textBeforeCursor = cursorPos > 0 ? text.substring(0, cursorPos) : "";
                int cursorX = textX + font.width(textBeforeCursor);
                int cursorY = textY - 1;

                if (cursorX >= minX && cursorX <= maxX) {
                    context.fill(cursorX, cursorY, cursorX + 1, cursorY + 9, 0xFFFFFFFF);
                }
            }
        } catch (Exception ignored) {}
    }
}
