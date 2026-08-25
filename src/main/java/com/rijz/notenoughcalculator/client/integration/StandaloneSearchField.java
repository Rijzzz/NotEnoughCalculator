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

import com.rijz.notenoughcalculator.client.util.REIHelper;
import com.rijz.notenoughcalculator.config.CalculatorConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import org.lwjgl.glfw.GLFW;

public class StandaloneSearchField implements SearchFieldAdapter {

	public static final int DEFAULT_WIDTH = 160;
	public static final int DEFAULT_HEIGHT = 18;

	private String text = "";
	private int cursorPosition = 0;
	private int selectionEnd = 0;
	private boolean focused = true;

	private boolean isDragging = false;
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;

	public boolean isDragging() {
		return isDragging;
	}

	public boolean startDragging(double mouseX, double mouseY) {
		CalculatorBounds bounds = getBounds();
		if (bounds != null && mouseX >= bounds.x && mouseX <= bounds.getMaxX() && mouseY >= bounds.y
				&& mouseY <= bounds.getMaxY()) {
			this.isDragging = true;
			this.dragOffsetX = (int) mouseX - bounds.x;
			this.dragOffsetY = (int) mouseY - bounds.y;
			return true;
		}
		return false;
	}

	public static int[] getMatchedDimensions() {
		if (IntegrationManager.isREILoaded()) {
			try {
				REIRuntime runtime = REIRuntime.getInstance();
				if (runtime != null) {
					TextField reiField = runtime.getSearchTextField();
					if (reiField != null) {
						Rectangle rect = REIHelper.getSearchFieldBounds(reiField);
						if (rect != null && rect.width > 0 && rect.height > 0) {
							return new int[]{rect.width, rect.height};
						}
					}
				}
			} catch (Throwable ignored) {
			}
		}

		if (IntegrationManager.isItemListLoaded()) {
			try {
				SkyblockItemListAdapter itemAdapter = new SkyblockItemListAdapter();
				CalculatorBounds itemBounds = itemAdapter.getBounds();
				if (itemBounds != null && itemBounds.width > 0 && itemBounds.height > 0) {
					return new int[]{itemBounds.width, itemBounds.height};
				}
			} catch (Throwable ignored) {
			}
		}

		return new int[]{DEFAULT_WIDTH, DEFAULT_HEIGHT};
	}

	public void updateDrag(double mouseX, double mouseY) {
		if (!isDragging)
			return;
		try {
			Minecraft mc = Minecraft.getInstance();
			if (mc != null && mc.getWindow() != null) {
				int screenWidth = mc.getWindow().getGuiScaledWidth();
				int screenHeight = mc.getWindow().getGuiScaledHeight();
				int[] dims = getMatchedDimensions();
				int width = dims[0];
				int height = dims[1];

				int newX = (int) mouseX - dragOffsetX;
				int newY = (int) mouseY - dragOffsetY;
				newX = Math.max(0, Math.min(newX, screenWidth - width));
				newY = Math.max(0, Math.min(newY, screenHeight - height));

				CalculatorConfig.getInstance().setPosition(newX, newY);
			}
		} catch (Exception ignored) {
		}
	}

	public void stopDragging() {
		if (isDragging) {
			this.isDragging = false;
			CalculatorConfig.getInstance().save();
		}
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (isDragging) {
			stopDragging();
			return true;
		}
		return false;
	}

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
		} catch (Exception ignored) {
		}
		this.testClipboard = str != null ? str : "";
	}

	String getClipboardText() {
		try {
			Minecraft mc = Minecraft.getInstance();
			if (mc != null && mc.keyboardHandler != null) {
				String val = mc.keyboardHandler.getClipboard();
				if (val != null)
					return val;
			}
		} catch (Exception ignored) {
		}
		return testClipboard != null ? testClipboard : "";
	}

	@Override
	public CalculatorBounds getBounds() {
		try {
			Minecraft mc = Minecraft.getInstance();
			if (mc != null && mc.getWindow() != null) {
				int screenWidth = mc.getWindow().getGuiScaledWidth();
				int screenHeight = mc.getWindow().getGuiScaledHeight();

				int[] dims = getMatchedDimensions();
				int width = dims[0];
				int height = dims[1];

				CalculatorConfig config = CalculatorConfig.getInstance();
				if (config.isCustomPositionSet()) {
					int customX = Math.max(0, Math.min(config.standaloneX, screenWidth - width));
					int customY = Math.max(0, Math.min(config.standaloneY, screenHeight - height));
					return new CalculatorBounds(customX, customY, width, height);
				}

				return getDefaultBounds(screenWidth, screenHeight);
			}
		} catch (Exception ignored) {
		}
		int[] dims = getMatchedDimensions();
		return new CalculatorBounds(320, 500, dims[0], dims[1]);
	}

	public static CalculatorBounds getDefaultBounds(int screenWidth, int screenHeight) {
		int[] dims = getMatchedDimensions();
		int width = dims[0];
		int height = dims[1];

		// Case A: If REI is loaded, position just beside REI search bar
		if (IntegrationManager.isREILoaded()) {
			try {
				REIRuntime runtime = REIRuntime.getInstance();
				if (runtime != null) {
					TextField reiField = runtime.getSearchTextField();
					if (reiField != null) {
						Rectangle rect = REIHelper.getSearchFieldBounds(reiField);
						if (rect != null && rect.width > 0 && rect.height > 0) {
							int candX = rect.x - width - 4;
							int candY = rect.y;
							if (candX < 4) {
								candX = rect.getMaxX() + 4;
							}
							if (candX + width > screenWidth) {
								candX = rect.x;
								candY = Math.max(0, rect.y - height - 4);
							}
							return new CalculatorBounds(candX, candY, width, height);
						}
					}
					ScreenOverlay overlay = runtime.getOverlay().orElse(null);
					if (overlay != null) {
						Rectangle ob = overlay.getBounds();
						int reiSearchX = ob.x + 2;
						int reiSearchY = ob.getMaxY() - 18;
						int candX = reiSearchX - width - 4;
						int candY = reiSearchY;
						if (candX < 4) {
							candX = ob.getMaxX() + 4;
						}
						if (candX + width > screenWidth) {
							candX = reiSearchX;
							candY = Math.max(0, reiSearchY - height - 4);
						}
						return new CalculatorBounds(candX, candY, width, height);
					}
				}
			} catch (Throwable ignored) {
			}
			int fallbackX = Math.max(0, screenWidth - (width * 2) - 10);
			int fallbackY = screenHeight - 22;
			return new CalculatorBounds(fallbackX, fallbackY, width, height);
		}

		// Case B: If Skyblock Item List is loaded, position just beside Item List
		// search bar
		if (IntegrationManager.isItemListLoaded()) {
			try {
				SkyblockItemListAdapter itemAdapter = new SkyblockItemListAdapter();
				CalculatorBounds itemBounds = itemAdapter.getBounds();
				if (itemBounds != null && itemBounds.width > 0 && itemBounds.height > 0) {
					int candX = itemBounds.x - width - 4;
					int candY = itemBounds.y;
					if (candX < 4) {
						candX = itemBounds.getMaxX() + 4;
					}
					if (candX + width > screenWidth) {
						candX = itemBounds.x;
						candY = Math.max(0, itemBounds.y - height - 4);
					}
					return new CalculatorBounds(candX, candY, width, height);
				}
			} catch (Throwable ignored) {
			}
			int fallbackX = Math.max(0, screenWidth - (width * 2) - 10);
			int fallbackY = screenHeight - 22;
			return new CalculatorBounds(fallbackX, fallbackY, width, height);
		}

		// Case C: Standard standalone centered at bottom of inventory screens
		int x = (screenWidth - width) / 2;
		int y = screenHeight - 22;
		return new CalculatorBounds(x, y, width, height);
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
		} catch (Exception ignored) {
		}
		return new CalculatorBounds(0, 0, 800, 600);
	}

	public boolean isFullSelection() {
		if (!hasSelection())
			return false;
		int start = Math.min(cursorPosition, selectionEnd);
		int end = Math.max(cursorPosition, selectionEnd);
		return start == 0 && end >= getText().length();
	}

	public boolean charTyped(char codePoint, int modifiers) {
		if (!focused)
			return false;
		if (codePoint < 32 || codePoint == 127)
			return false;

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
		if (!focused)
			return false;

		boolean isCtrlOrCmd = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0 || (modifiers & GLFW.GLFW_MOD_SUPER) != 0;
		boolean isShift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

		String currentStr = getText();
		int len = currentStr.length();
		int pos = getCursorPosition();

		if (key == GLFW.GLFW_KEY_A && isCtrlOrCmd) {
			cursorPosition = 0;
			selectionEnd = len;
			return true;
		}

		if (key == GLFW.GLFW_KEY_C && isCtrlOrCmd) {
			if (hasSelection()) {
				String selected = getSelectedText();
				setClipboardText(selected);
				return true;
			}
		}

		if (key == GLFW.GLFW_KEY_X && isCtrlOrCmd) {
			if (hasSelection()) {
				String selected = getSelectedText();
				setClipboardText(selected);
				deleteSelection();
				return true;
			}
		}

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

		return focused;
	}

	private int findWordStart(String str, int fromIndex) {
		if (fromIndex <= 0)
			return 0;
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
		if (fromIndex >= len)
			return len;
		int idx = fromIndex;
		while (idx < len && !Character.isWhitespace(str.charAt(idx))) {
			idx++;
		}
		while (idx < len && Character.isWhitespace(str.charAt(idx))) {
			idx++;
		}
		return Math.min(len, idx);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		CalculatorBounds bounds = getBounds();
		if (mouseX >= bounds.x && mouseX <= bounds.getMaxX() && mouseY >= bounds.y && mouseY <= bounds.getMaxY()) {
			focused = true;

			Minecraft mc = Minecraft.getInstance();
			if (mc != null && mc.font != null) {
				Font font = mc.font;
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
			} else {
				cursorPosition = getText().length();
				selectionEnd = cursorPosition;
			}
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
		if (!hasSelection())
			return "";
		int start = Math.min(getCursorPosition(), getSelectionEnd());
		int end = Math.max(getCursorPosition(), getSelectionEnd());
		String currentStr = getText();
		return currentStr.substring(Math.min(start, currentStr.length()), Math.min(end, currentStr.length()));
	}

	private void deleteSelection() {
		if (!hasSelection())
			return;
		int start = Math.min(getCursorPosition(), getSelectionEnd());
		int end = Math.max(getCursorPosition(), getSelectionEnd());
		String currentStr = getText();
		String newStr = currentStr.substring(0, start) + currentStr.substring(end);
		setText(newStr);
		cursorPosition = start;
		selectionEnd = start;
	}
}
