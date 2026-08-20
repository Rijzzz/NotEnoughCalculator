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
import com.rijz.notenoughcalculator.client.integration.IntegrationManager;
import com.rijz.notenoughcalculator.client.integration.SearchFieldAdapter;
import com.rijz.notenoughcalculator.client.util.ReflectionUtils;
import com.rijz.notenoughcalculator.config.CalculatorConfig;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.lang.reflect.Method;

public class CalculatorOverlayRenderer {

    private static Method enableScissorMethod = null;
    private static Method disableScissorMethod = null;
    private static boolean scissorReflectionInitialized = false;

    public static void renderOverlay(Screen screen, GuiGraphicsExtractor context, CalculatorManager calcManager, boolean shouldRender) {
        Minecraft mc = Minecraft.getInstance();

        if (!shouldRenderCalculator(screen, mc, shouldRender)) return;

        try {
            if (IntegrationManager.isREILoaded()) {
                REIRuntime runtime = REIRuntime.getInstance();
                if (runtime == null || !runtime.isOverlayVisible()) return;

                ScreenOverlay overlay = runtime.getOverlay().orElse(null);
                if (overlay == null) return;

                TextField searchField = runtime.getSearchTextField();
                if (searchField == null) return;

                ReflectionUtils.clampSearchField(searchField);

                String searchText = searchField.getText();
                calcManager.formatSearchBar(searchText);

                if (!CalculatorManager.looksLikeCalculation(searchText) || !calcManager.hasResult()) {
                    return;
                }

                REIOverlayRenderer.render(context, overlay, searchField, searchText, mc.font, calcManager);
            } else if (IntegrationManager.isItemListLoaded()) {
                return;
            } else if (IntegrationManager.isStandaloneActive()) {
                SearchFieldAdapter adapter = IntegrationManager.getActiveAdapter();
                if (adapter == null) return;

                String searchText = adapter.getText();
                calcManager.formatSearchBar(searchText);

                StandaloneOverlayRenderer.render(context, adapter, searchText, mc.font, calcManager);
            }
        } catch (Exception ignored) {}
    }

    private static boolean shouldRenderCalculator(Screen screen, Minecraft mc, boolean shouldRender) {
        return mc != null
                && screen != null
                && !isNonGameplayScreen(screen)
                && ReflectionUtils.getCurrentScreen(mc) == screen
                && mc.level != null
                && mc.player != null
                && shouldRender
                && CalculatorConfig.getInstance().showInlineResults;
    }

    public static boolean isNonGameplayScreen(Screen screen) {
        if (screen instanceof AbstractContainerScreen) {
            return false;
        }
        String screenClassName = screen.getClass().getName();
        return !screenClassName.contains("rei") && !screenClassName.contains("REI");
    }

    public static void enableScissor(GuiGraphicsExtractor context, int minX, int minY, int maxX, int maxY) {
        if (context == null) return;
        try {
            if (!scissorReflectionInitialized) {
                scissorReflectionInitialized = true;
                Class<?> clazz = context.getClass();
                for (Method m : clazz.getMethods()) {
                    String name = m.getName().toLowerCase();
                    if (name.contains("scissor") && m.getParameterCount() == 4) {
                        enableScissorMethod = m;
                        enableScissorMethod.setAccessible(true);
                        break;
                    }
                }
                for (Method m : clazz.getMethods()) {
                    String name = m.getName().toLowerCase();
                    if (name.contains("scissor") && m.getParameterCount() == 0) {
                        disableScissorMethod = m;
                        disableScissorMethod.setAccessible(true);
                        break;
                    }
                }
            }
            if (enableScissorMethod != null) {
                enableScissorMethod.invoke(context, minX, minY, maxX, maxY);
            }
        } catch (Exception ignored) {}
    }

    public static void disableScissor(GuiGraphicsExtractor context) {
        try {
            if (disableScissorMethod != null) {
                disableScissorMethod.invoke(context);
            }
        } catch (Exception ignored) {}
    }
}
