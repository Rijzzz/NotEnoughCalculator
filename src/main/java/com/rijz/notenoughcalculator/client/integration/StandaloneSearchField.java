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

package com.rijz.notenoughcalculator.client.integration;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.lwjgl.glfw.GLFW;

// Standalone search field component with full selection, clipboard, mouse cursor positioning, and word navigation
public class StandaloneSearchField implements SearchFieldAdapter {

    private String text = "";
    private int cursorPosition = 0;
    private int selectionEnd = 0;
    private boolean focused = true;

    @Override
    public String getText() {
        return text != null ? text : "";
    }

    @Override
    public void setText(String newText) {
        this.text = newText != null ? newText : "";
        clamp();
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public int getCursorPosition() {
        return Math.min(Math.max(0, cursorPosition), getText().length());
    }

    @Override
    public int getSelectionEnd() {
        return Math.min(Math.max(0, selectionEnd), getText().length());
    }

    public void setCursorPosition(int pos) {
        this.cursorPosition = Math.min(Math.max(0, pos), getText().length());
    }

    public void setSelectionEnd(int pos) {
        this.selectionEnd = Math.min(Math.max(0, pos), getText().length());
    }

    @Override
    public void clamp() {
        int len = getText().length();
        this.cursorPosition = Math.min(Math.max(0, cursorPosition), len);
        this.selectionEnd = Math.min(Math.max(0, selectionEnd), len);
    }

    private String testClipboard = "";

    void setClipboardText(String str) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.keyboardHandler != null) {
                mc.keyboardHandler.setClipboard(str);
                return;
            }
        } catch (Exception ignored) {}
        this.testClipboard = str != null ? str : "";
    }

    String getClipboardText() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.keyboardHandler != null) {
                String val = mc.keyboardHandler.getClipboard();
                if (val != null) return val;
            }
        } catch (Exception ignored) {}
        return testClipboard != null ? testClipboard : "";
    }

    @Override
    public CalculatorBounds getBounds() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getWindow() != null) {
                int screenWidth = mc.getWindow().getGuiScaledWidth();
                int screenHeight = mc.getWindow().getGuiScaledHeight();

                int width = 160;
                int height = 18;
                int x = (screenWidth - width) / 2;
                int y = screenHeight - 22;

                return new CalculatorBounds(x, y, width, height);
            }
        } catch (Exception ignored) {}
        return new CalculatorBounds(320, 500, 160, 18);
    }

    @Override
    public CalculatorBounds getOverlayBounds() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getWindow() != null) {
                int screenWidth = mc.getWindow().getGuiScaledWidth();
                int screenHeight = mc.getWindow().getGuiScaledHeight();

                return new CalculatorBounds(0, 0, screenWidth, screenHeight);
            }
        } catch (Exception ignored) {}
        return new CalculatorBounds(0, 0, 800, 600);
    }

    public boolean isFullSelection() {
        if (!hasSelection()) return false;
        int start = Math.min(cursorPosition, selectionEnd);
        int end = Math.max(cursorPosition, selectionEnd);
        return start == 0 && end >= getText().length();
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (!focused) return false;
        if (codePoint < 32 || codePoint == 127) return false;

        deleteSelection();
        int pos = getCursorPosition();
        String currentStr = getText();
        String newStr = currentStr.substring(0, pos) + codePoint + currentStr.substring(pos);
        setText(newStr);
        cursorPosition = pos + 1;
        selectionEnd = cursorPosition;
        return true;
    }

    public boolean keyPressed(int key, int scancode, int modifiers) {
        if (!focused) return false;

        boolean isCtrlOrCmd = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
        boolean isShift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        String currentStr = getText();
        int len = currentStr.length();
        int pos = getCursorPosition();

        // Ctrl+A / Cmd+A: Select All
        if (key == GLFW.GLFW_KEY_A && isCtrlOrCmd) {
            cursorPosition = 0;
            selectionEnd = len;
            return true;
        }

        // Ctrl+C / Cmd+C: Copy selected text
        if (key == GLFW.GLFW_KEY_C && isCtrlOrCmd) {
            if (hasSelection()) {
                String selected = getSelectedText();
                setClipboardText(selected);
                return true;
            }
        }

        // Ctrl+X / Cmd+X: Cut selected text
        if (key == GLFW.GLFW_KEY_X && isCtrlOrCmd) {
            if (hasSelection()) {
                String selected = getSelectedText();
                setClipboardText(selected);
                deleteSelection();
                return true;
            }
        }

        // Ctrl+V / Cmd+V: Paste clipboard text
        if (key == GLFW.GLFW_KEY_V && isCtrlOrCmd) {
            String clipboard = getClipboardText();
            if (clipboard != null && !clipboard.isEmpty()) {
                deleteSelection();
                int currentPos = getCursorPosition();
                String cur = getText();
                String updated = cur.substring(0, currentPos) + clipboard + cur.substring(currentPos);
                setText(updated);
                cursorPosition = currentPos + clipboard.length();
                selectionEnd = cursorPosition;
                return true;
            }
        }

        if (key == GLFW.GLFW_KEY_BACKSPACE) {
            if (hasSelection()) {
                deleteSelection();
            } else if (isCtrlOrCmd) {
                int wordStart = findWordStart(currentStr, pos);
                String newStr = currentStr.substring(0, wordStart) + currentStr.substring(pos);
                setText(newStr);
                cursorPosition = wordStart;
                selectionEnd = cursorPosition;
            } else if (pos > 0) {
                String newStr = currentStr.substring(0, pos - 1) + currentStr.substring(pos);
                setText(newStr);
                cursorPosition = pos - 1;
                selectionEnd = cursorPosition;
            }
            return true;
        }

        if (key == GLFW.GLFW_KEY_DELETE) {
            if (hasSelection()) {
                deleteSelection();
            } else if (isCtrlOrCmd) {
                int wordEnd = findWordEnd(currentStr, pos);
                String newStr = currentStr.substring(0, pos) + currentStr.substring(wordEnd);
                setText(newStr);
            } else if (pos < len) {
                String newStr = currentStr.substring(0, pos) + currentStr.substring(pos + 1);
                setText(newStr);
            }
            return true;
        }

        if (key == GLFW.GLFW_KEY_LEFT) {
            int newPos = isCtrlOrCmd ? findWordStart(currentStr, pos) : Math.max(0, pos - 1);
            cursorPosition = newPos;
            if (!isShift) {
                selectionEnd = cursorPosition;
            }
            return true;
        }

        if (key == GLFW.GLFW_KEY_RIGHT) {
            int newPos = isCtrlOrCmd ? findWordEnd(currentStr, pos) : Math.min(len, pos + 1);
            cursorPosition = newPos;
            if (!isShift) {
                selectionEnd = cursorPosition;
            }
            return true;
        }

        if (key == GLFW.GLFW_KEY_HOME) {
            cursorPosition = 0;
            if (!isShift) {
                selectionEnd = 0;
            }
            return true;
        }

        if (key == GLFW.GLFW_KEY_END) {
            cursorPosition = len;
            if (!isShift) {
                selectionEnd = len;
            }
            return true;
        }

        char ch = getCharFromKey(key, isShift);
        if (ch != 0 && !isCtrlOrCmd) {
            return charTyped(ch, modifiers);
        }

        return false;
    }

    private int findWordStart(String str, int fromIndex) {
        if (fromIndex <= 0) return 0;
        int idx = fromIndex - 1;
        while (idx > 0 && Character.isWhitespace(str.charAt(idx))) {
            idx--;
        }
        while (idx > 0 && !Character.isWhitespace(str.charAt(idx - 1))) {
            idx--;
        }
        return Math.max(0, idx);
    }

    private int findWordEnd(String str, int fromIndex) {
        int len = str.length();
        if (fromIndex >= len) return len;
        int idx = fromIndex;
        while (idx < len && !Character.isWhitespace(str.charAt(idx))) {
            idx++;
        }
        while (idx < len && Character.isWhitespace(str.charAt(idx))) {
            idx++;
        }
        return Math.min(len, idx);
    }

    private char getCharFromKey(int key, boolean shift) {
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            char base = (char) ('a' + (key - GLFW.GLFW_KEY_A));
            return shift ? Character.toUpperCase(base) : base;
        }
        if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            if (!shift) {
                return (char) ('0' + (key - GLFW.GLFW_KEY_0));
            } else {
                switch (key) {
                    case GLFW.GLFW_KEY_1: return '!';
                    case GLFW.GLFW_KEY_4: return '$';
                    case GLFW.GLFW_KEY_5: return '%';
                    case GLFW.GLFW_KEY_6: return '^';
                    case GLFW.GLFW_KEY_7: return '&';
                    case GLFW.GLFW_KEY_8: return '*';
                    case GLFW.GLFW_KEY_9: return '(';
                    case GLFW.GLFW_KEY_0: return ')';
                    default: return (char) ('0' + (key - GLFW.GLFW_KEY_0));
                }
            }
        }
        if (key >= GLFW.GLFW_KEY_KP_0 && key <= GLFW.GLFW_KEY_KP_9) {
            return (char) ('0' + (key - GLFW.GLFW_KEY_KP_0));
        }
        switch (key) {
            case GLFW.GLFW_KEY_SPACE: return ' ';
            case GLFW.GLFW_KEY_EQUAL: return shift ? '+' : '=';
            case GLFW.GLFW_KEY_MINUS: return shift ? '_' : '-';
            case GLFW.GLFW_KEY_SLASH: return shift ? '?' : '/';
            case GLFW.GLFW_KEY_PERIOD: return shift ? '>' : '.';
            case GLFW.GLFW_KEY_COMMA: return shift ? '<' : ',';
            case GLFW.GLFW_KEY_GRAVE_ACCENT: return shift ? '~' : '`';
            case GLFW.GLFW_KEY_KP_ADD: return '+';
            case GLFW.GLFW_KEY_KP_SUBTRACT: return '-';
            case GLFW.GLFW_KEY_KP_MULTIPLY: return '*';
            case GLFW.GLFW_KEY_KP_DIVIDE: return '/';
            case GLFW.GLFW_KEY_KP_DECIMAL: return '.';
            default: return 0;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        CalculatorBounds bounds = getBounds();
        if (mouseX >= bounds.x && mouseX <= bounds.getMaxX() && mouseY >= bounds.y && mouseY <= bounds.getMaxY()) {
            focused = true;

            // Character index calculation based on click position
            Font font = Minecraft.getInstance().font;
            String currentStr = getText();
            int clickX = (int) mouseX - bounds.x - 4;

            int bestIndex = 0;
            int bestDist = Integer.MAX_VALUE;

            for (int i = 0; i <= currentStr.length(); i++) {
                int w = font.width(currentStr.substring(0, i));
                int dist = Math.abs(w - clickX);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestIndex = i;
                }
            }

            cursorPosition = bestIndex;
            selectionEnd = bestIndex;
            return true;
        } else {
            focused = false;
            return false;
        }
    }

    public boolean hasSelection() {
        return getCursorPosition() != getSelectionEnd();
    }

    public String getSelectedText() {
        if (!hasSelection()) return "";
        int start = Math.min(getCursorPosition(), getSelectionEnd());
        int end = Math.max(getCursorPosition(), getSelectionEnd());
        String currentStr = getText();
        return currentStr.substring(Math.min(start, currentStr.length()), Math.min(end, currentStr.length()));
    }

    private void deleteSelection() {
        if (!hasSelection()) return;
        int start = Math.min(getCursorPosition(), getSelectionEnd());
        int end = Math.max(getCursorPosition(), getSelectionEnd());
        String currentStr = getText();
        String newStr = currentStr.substring(0, start) + currentStr.substring(end);
        setText(newStr);
        cursorPosition = start;
        selectionEnd = start;
    }
}
