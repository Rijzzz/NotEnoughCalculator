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

import com.rijz.notenoughcalculator.client.NotEnoughCalculatorClient;
import com.rijz.notenoughcalculator.client.util.REIHelper;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;

// Adapter for REI search field integration
public class REISearchAdapter implements SearchFieldAdapter {

    @Override
    public String getText() {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null) {
                TextField searchField = runtime.getSearchTextField();
                if (searchField != null) {
                    return searchField.getText();
                }
            }
        } catch (Exception ignored) {}
        return "";
    }

    @Override
    public void setText(String text) {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null) {
                TextField searchField = runtime.getSearchTextField();
                if (searchField != null) {
                    searchField.setText(text != null ? text : "");
                    NotEnoughCalculatorClient.clampSearchField(searchField);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public boolean isFocused() {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null) {
                TextField searchField = runtime.getSearchTextField();
                if (searchField != null) {
                    return searchField.isFocused();
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null) {
                TextField searchField = runtime.getSearchTextField();
                if (searchField != null) {
                    searchField.setFocused(focused);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public int getCursorPosition() {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null) {
                TextField searchField = runtime.getSearchTextField();
                if (searchField != null) {
                    return NotEnoughCalculatorClient.getCursorPosition(searchField);
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    @Override
    public int getSelectionEnd() {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null) {
                TextField searchField = runtime.getSearchTextField();
                if (searchField != null) {
                    return NotEnoughCalculatorClient.getSelectionEnd(searchField);
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    @Override
    public void clamp() {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null) {
                TextField searchField = runtime.getSearchTextField();
                if (searchField != null) {
                    NotEnoughCalculatorClient.clampSearchField(searchField);
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    public CalculatorBounds getBounds() {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null) {
                TextField searchField = runtime.getSearchTextField();
                if (searchField != null) {
                    Rectangle rect = REIHelper.getSearchFieldBounds(searchField);
                    if (rect != null) {
                        return new CalculatorBounds(rect.x, rect.y, rect.width, rect.height);
                    }
                }
                ScreenOverlay overlay = runtime.getOverlay().orElse(null);
                if (overlay != null) {
                    Rectangle ob = overlay.getBounds();
                    return new CalculatorBounds(ob.x + 2, ob.getMaxY() - 18, ob.width - 4, 18);
                }
            }
        } catch (Exception ignored) {}

        return new StandaloneSearchField().getBounds();
    }

    @Override
    public CalculatorBounds getOverlayBounds() {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime != null) {
                ScreenOverlay overlay = runtime.getOverlay().orElse(null);
                if (overlay != null) {
                    Rectangle ob = overlay.getBounds();
                    return new CalculatorBounds(ob.x, ob.y, ob.width, ob.height);
                }
            }
        } catch (Exception ignored) {}

        return new StandaloneSearchField().getOverlayBounds();
    }
}
