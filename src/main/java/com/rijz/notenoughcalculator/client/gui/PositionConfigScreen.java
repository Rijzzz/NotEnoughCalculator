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

import com.rijz.notenoughcalculator.client.integration.CalculatorBounds;
import com.rijz.notenoughcalculator.client.integration.StandaloneSearchField;
import com.rijz.notenoughcalculator.client.util.ReflectionUtils;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import com.rijz.notenoughcalculator.core.ColorConstants;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

public class PositionConfigScreen extends Screen {

	private final Screen parent;
	private int currentX;
	private int currentY;
	private boolean isDragging = false;
	private int dragOffsetX = 0;
	private int dragOffsetY = 0;
	private boolean wasMouseDown = false;

	public PositionConfigScreen(Screen parent) {
		super(Component.translatable("notenoughcalculator.position.screen.title"));
		this.parent = parent;

		if (parent instanceof CalculatorConfigScreen configScreen) {
			this.currentX = configScreen.standaloneX;
			this.currentY = configScreen.standaloneY;
		} else {
			CalculatorConfig config = CalculatorConfig.getInstance();
			this.currentX = config.standaloneX;
			this.currentY = config.standaloneY;
		}
	}

	@Override
	protected void init() {
		clearWidgets();

		int maxTotalWidth = Math.min(this.width - 20, 424);
		int gap = 8;
		int buttonWidth = Math.max(50, (maxTotalWidth - (gap * 3)) / 4);
		int buttonHeight = 18;
		int bottomY = this.height - 28;
		int totalWidth = (buttonWidth * 4) + (gap * 3);
		int startX = (this.width - totalWidth) / 2;

		addRenderableWidget(
				Button.builder(Component.translatable("notenoughcalculator.position.screen.btn_center"), btn -> {
					int[] dims = StandaloneSearchField.getMatchedDimensions();
					this.currentX = (this.width - dims[0]) / 2;
					this.currentY = this.height - 22;
				}).bounds(startX, bottomY, buttonWidth, buttonHeight)
						.tooltip(Tooltip
								.create(Component.translatable("notenoughcalculator.config.tooltip.center_bottom")))
						.build());

		addRenderableWidget(
				Button.builder(Component.translatable("notenoughcalculator.position.screen.btn_reset"), btn -> {
					this.currentX = -1;
					this.currentY = -1;
				}).bounds(startX + buttonWidth + 8, bottomY, buttonWidth, buttonHeight)
						.tooltip(Tooltip
								.create(Component.translatable("notenoughcalculator.config.tooltip.reset_position")))
						.build());

		addRenderableWidget(
				Button.builder(Component.translatable("notenoughcalculator.position.screen.btn_save"), btn -> {
					saveAndClose();
				}).bounds(startX + (buttonWidth + 8) * 2, bottomY, buttonWidth, buttonHeight)
						.tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.save")))
						.build());

		addRenderableWidget(
				Button.builder(Component.translatable("notenoughcalculator.position.screen.btn_cancel"), btn -> {
					closeScreen();
				}).bounds(startX + (buttonWidth + 8) * 3, bottomY, buttonWidth, buttonHeight)
						.tooltip(Tooltip.create(Component.translatable("notenoughcalculator.config.tooltip.cancel")))
						.build());
	}

	private CalculatorBounds getEffectiveBounds() {
		int[] dims = StandaloneSearchField.getMatchedDimensions();
		int width = dims[0];
		int height = dims[1];

		if (currentX >= 0 && currentY >= 0) {
			int cx = Math.max(0, Math.min(currentX, this.width - width));
			int cy = Math.max(0, Math.min(currentY, this.height - height));
			return new CalculatorBounds(cx, cy, width, height);
		}

		return StandaloneSearchField.getDefaultBounds(this.width, this.height);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (this.minecraft != null) {
			long window = this.minecraft.getWindow().handle();
			boolean isMouseDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
			CalculatorBounds bounds = getEffectiveBounds();

			if (isMouseDown && !wasMouseDown) {
				if (mouseX >= bounds.x && mouseX <= bounds.getMaxX() && mouseY >= bounds.y
						&& mouseY <= bounds.getMaxY()) {
					this.isDragging = true;
					this.dragOffsetX = mouseX - bounds.x;
					this.dragOffsetY = mouseY - bounds.y;
					this.currentX = bounds.x;
					this.currentY = bounds.y;
				}
			} else if (isMouseDown && this.isDragging) {
				int[] dims = StandaloneSearchField.getMatchedDimensions();
				int newX = mouseX - dragOffsetX;
				int newY = mouseY - dragOffsetY;
				this.currentX = Math.max(0, Math.min(newX, this.width - dims[0]));
				this.currentY = Math.max(0, Math.min(newY, this.height - dims[1]));
			} else if (!isMouseDown && wasMouseDown) {
				this.isDragging = false;
			}
			this.wasMouseDown = isMouseDown;
		}

		graphics.fill(0, 0, this.width, this.height, ColorConstants.BG_POSITION_OVERLAY);

		Component title = Component.translatable("notenoughcalculator.position.screen.title");
		graphics.text(this.font, title, (this.width - this.font.width(title)) / 2, 12, ColorConstants.TEXT_WHITE, true);

		Component instructions = Component.translatable("notenoughcalculator.position.screen.instructions");
		graphics.text(this.font, instructions, (this.width - this.font.width(instructions)) / 2, 26,
				ColorConstants.TEXT_PLACEHOLDER, true);

		CalculatorBounds bounds = getEffectiveBounds();
		String coordText = (currentX >= 0 && currentY >= 0)
				? I18n.get("notenoughcalculator.position.screen.coord_custom", bounds.x, bounds.y)
				: I18n.get("notenoughcalculator.position.screen.coord_default", bounds.x, bounds.y);
		graphics.text(this.font, coordText, (this.width - this.font.width(coordText)) / 2, 40, ColorConstants.TEXT_CYAN,
				true);

		int bx = bounds.x;
		int by = bounds.y;
		int bMaxX = bounds.getMaxX();
		int bMaxY = bounds.getMaxY();

		graphics.fill(bx - 2, by - 2, bMaxX + 2, bMaxY + 2,
				isDragging ? ColorConstants.BORDER_DRAG_ACTIVE : ColorConstants.BORDER_DRAG_IDLE);
		graphics.fill(bx - 1, by - 1, bMaxX + 1, bMaxY + 1, ColorConstants.BG_BLACK);
		graphics.fill(bx, by, bMaxX, bMaxY, ColorConstants.BG_POSITION_BAR);

		String placeholder = I18n.get("notenoughcalculator.standalone.placeholder");
		graphics.text(this.font, placeholder, bx + 4, by + (bounds.height - 8) / 2, ColorConstants.TEXT_LIGHT_GRAY,
				true);

		String dragIcon = I18n.get("notenoughcalculator.position.screen.drag_icon");
		graphics.text(this.font, dragIcon, bMaxX - this.font.width(dragIcon) - 4, by + (bounds.height - 8) / 2,
				isDragging ? ColorConstants.DRAG_ICON_ACTIVE : ColorConstants.DRAG_ICON_IDLE, true);

		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	private void saveAndClose() {
		if (this.parent instanceof CalculatorConfigScreen configScreen) {
			configScreen.standaloneX = this.currentX;
			configScreen.standaloneY = this.currentY;
		}
		CalculatorConfig config = CalculatorConfig.getInstance();
		config.standaloneX = this.currentX;
		config.standaloneY = this.currentY;
		config.save();
		closeScreen();
	}

	private void closeScreen() {
		ReflectionUtils.openScreen(this.minecraft, this.parent);
	}

	@Override
	public void onClose() {
		closeScreen();
	}
}
