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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import static org.junit.jupiter.api.Assertions.*;

public class StandaloneSearchFieldTest {

    private StandaloneSearchField field;

    @BeforeEach
    public void setUp() {
        field = new StandaloneSearchField();
        field.setFocused(true);
    }

    @Test
    public void testInitialState() {
        assertEquals("", field.getText());
        assertEquals(0, field.getCursorPosition());
        assertEquals(0, field.getSelectionEnd());
        assertTrue(field.isFocused());
    }

    @Test
    public void testSetTextAndClamp() {
        field.setText("100 + 200");
        assertEquals("100 + 200", field.getText());
        assertEquals(0, field.getCursorPosition());

        field.clamp();
        assertEquals(0, field.getCursorPosition());
        assertEquals(0, field.getSelectionEnd());
    }

    @Test
    public void testCharTyped() {
        field.charTyped('5', 0);
        field.charTyped('0', 0);
        field.charTyped('+', 0);
        field.charTyped('2', 0);
        assertEquals("50+2", field.getText());
        assertEquals(4, field.getCursorPosition());
    }

    @Test
    public void testKeyPressedNavigation() {
        field.setText("hello");
        field.keyPressed(GLFW.GLFW_KEY_END, 0, 0);
        assertEquals(5, field.getCursorPosition());

        field.keyPressed(GLFW.GLFW_KEY_LEFT, 0, 0);
        assertEquals(4, field.getCursorPosition());

        field.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0);
        assertEquals(0, field.getCursorPosition());

        field.keyPressed(GLFW.GLFW_KEY_RIGHT, 0, 0);
        assertEquals(1, field.getCursorPosition());
    }

    @Test
    public void testBackspaceAndDelete() {
        field.setText("123");
        field.keyPressed(GLFW.GLFW_KEY_END, 0, 0);

        field.keyPressed(GLFW.GLFW_KEY_BACKSPACE, 0, 0);
        assertEquals("12", field.getText());
        assertEquals(2, field.getCursorPosition());

        field.keyPressed(GLFW.GLFW_KEY_HOME, 0, 0);
        field.keyPressed(GLFW.GLFW_KEY_DELETE, 0, 0);
        assertEquals("2", field.getText());
        assertEquals(0, field.getCursorPosition());
    }

    @Test
    public void testSelectAllAndReplace() {
        field.setText("100+500");
        field.keyPressed(GLFW.GLFW_KEY_A, 0, GLFW.GLFW_MOD_CONTROL);
        assertTrue(field.hasSelection());
        assertEquals("100+500", field.getSelectedText());

        field.charTyped('9', 0);
        assertEquals("9", field.getText());
        assertFalse(field.hasSelection());
    }

    @Test
    public void testCutCopyPaste() {
        field.setText("abcdef");
        field.setCursorPosition(2);
        field.setSelectionEnd(4);
        assertTrue(field.hasSelection());
        assertFalse(field.isFullSelection());
        assertEquals("cd", field.getSelectedText());

        field.keyPressed(GLFW.GLFW_KEY_C, 0, GLFW.GLFW_MOD_CONTROL);

        field.setText("");
        field.setCursorPosition(0);
        field.setSelectionEnd(0);

        field.keyPressed(GLFW.GLFW_KEY_V, 0, GLFW.GLFW_MOD_CONTROL);
        assertEquals("cd", field.getText());
    }

    @Test
    public void testCopyFullSelectionAfterCtrlA() {
        field.setText("126.855m");
        field.keyPressed(GLFW.GLFW_KEY_A, 0, GLFW.GLFW_MOD_CONTROL);
        assertTrue(field.hasSelection());
        assertTrue(field.isFullSelection());

        boolean handled = field.keyPressed(GLFW.GLFW_KEY_C, 0, GLFW.GLFW_MOD_CONTROL);
        assertTrue(handled);
        assertEquals("126.855m", field.getClipboardText());
    }

    @Test
    public void testWordNavigation() {
        field.setText("100 + 200");
        field.keyPressed(GLFW.GLFW_KEY_END, 0, 0);

        field.keyPressed(GLFW.GLFW_KEY_LEFT, 0, GLFW.GLFW_MOD_CONTROL);
        assertEquals(6, field.getCursorPosition());

        field.keyPressed(GLFW.GLFW_KEY_LEFT, 0, GLFW.GLFW_MOD_CONTROL);
        assertEquals(4, field.getCursorPosition());

        field.keyPressed(GLFW.GLFW_KEY_RIGHT, 0, GLFW.GLFW_MOD_CONTROL);
        assertEquals(6, field.getCursorPosition());
    }

    @Test
    public void testDraggingAndCustomBounds() {
        assertFalse(field.isDragging());

        CalculatorBounds bounds = field.getBounds();
        assertNotNull(bounds);

        boolean started = field.startDragging(bounds.x + 5, bounds.y + 5);
        assertTrue(started);
        assertTrue(field.isDragging());

        field.updateDrag(bounds.x + 50, bounds.y + 60);
        assertTrue(field.isDragging());

        field.mouseReleased(bounds.x + 50, bounds.y + 60, 0);
        assertFalse(field.isDragging());
    }

    @Test
    public void testMatchedDimensionsDefault() {
        int[] dims = StandaloneSearchField.getMatchedDimensions();
        assertNotNull(dims);
        assertEquals(2, dims.length);
        assertEquals(StandaloneSearchField.DEFAULT_WIDTH, dims[0]);
        assertEquals(StandaloneSearchField.DEFAULT_HEIGHT, dims[1]);
    }

    @Test
    public void testDragBoundsClamping() {
        CalculatorBounds bounds = field.getBounds();
        assertNotNull(bounds);
        assertTrue(bounds.width > 0);
        assertTrue(bounds.height > 0);
    }

    @Test
    public void testMouseClickedFocus() {
        CalculatorBounds bounds = field.getBounds();
        assertNotNull(bounds);

        boolean inside = field.mouseClicked(bounds.x + 5, bounds.y + 5, 0);
        assertTrue(inside);
        assertTrue(field.isFocused());

        boolean outside = field.mouseClicked(bounds.x - 50, bounds.y - 50, 0);
        assertFalse(outside);
        assertFalse(field.isFocused());
    }

    @Test
    public void testKeyPressedFocusHandling() {
        field.setFocused(true);
        assertTrue(field.keyPressed(GLFW.GLFW_KEY_1, 0, 0));

        field.setFocused(false);
        assertFalse(field.keyPressed(GLFW.GLFW_KEY_1, 0, 0));
    }

    @Test
    public void testGetDefaultBounds() {
        CalculatorBounds def = StandaloneSearchField.getDefaultBounds(800, 600);
        assertNotNull(def);
        assertEquals(StandaloneSearchField.DEFAULT_WIDTH, def.width);
        assertEquals(StandaloneSearchField.DEFAULT_HEIGHT, def.height);
        assertTrue(def.x >= 0);
        assertTrue(def.y >= 0);
        assertEquals((800 - StandaloneSearchField.DEFAULT_WIDTH) / 2, def.x);
        assertEquals(600 - 22, def.y);
    }
}
