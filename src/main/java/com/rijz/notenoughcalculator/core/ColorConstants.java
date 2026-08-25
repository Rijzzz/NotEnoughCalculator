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

package com.rijz.notenoughcalculator.core;

public final class ColorConstants {

    private ColorConstants() {
    }

    public static final String NUMBER = "§3";
    public static final String RADIX_LITERAL = "§d";
    public static final String UNIT = "§b";
    public static final String MATH_FUNCTION = "§e";
    public static final String PROGRESSION_FUNCTION = "§6";
    public static final String MARKET_FUNCTION = "§9";
    public static final String BUILTIN_VARIABLE = "§b";
    public static final String CUSTOM_VARIABLE = "§3";
    public static final String DOLLAR_SIGN = "§6";
    public static final String STRING_ITEM = "§d";
    public static final String OPERATOR = "§c";
    public static final String DELIMITER = "§7";
    public static final String ERROR = "§c";
    public static final String RESULT = "§a";

    public static final String PLAIN_WHITE = "§f";
    public static final String EQUALS_HIGHLIGHTED = " §6= ";
    public static final String EQUALS_PLAIN = " §f= ";

    public static final int TEXT_WHITE = 0xFFFFFFFF;
    public static final int TEXT_PLACEHOLDER = 0x88AAAAAA;
    public static final int TEXT_LIGHT_GRAY = 0xFFE0E0E0;
    public static final int TEXT_MUTED = 0xFF94A3B8;
    public static final int TEXT_SLATE = 0xFFCBD5E1;
    public static final int TEXT_CYAN = 0xFF55FFFF;
    public static final int TEXT_EMERALD = 0xFF34D399;
    public static final int TEXT_SKY = 0xFF38BDF8;
    public static final int TEXT_AMBER = 0xFFFBBF24;

    public static final int BG_BLACK = 0xFF000000;
    public static final int BG_TOOLTIP = 0xEE000000;
    public static final int BG_SCREEN_OVERLAY = 0xD0040711;
    public static final int BG_PANEL = 0xF50F172A;
    public static final int BG_PANEL_HEADER = 0xFF1E293B;
    public static final int BG_VARIABLE_ROW = 0xFF1E293B;
    public static final int BG_POSITION_BAR = 0xEE1E293B;
    public static final int BG_POSITION_OVERLAY = 0xB0000000;
    public static final int BG_WARNING_BANNER = 0xD02A0808;

    public static final int BORDER_FOCUSED = 0xFFFFFFFF;
    public static final int BORDER_UNFOCUSED = 0xFF8B8B8B;
    public static final int BORDER_WARNING = 0xFFDC2626;
    public static final int BORDER_DRAG_ACTIVE = 0xFFFFD700;
    public static final int BORDER_DRAG_IDLE = 0xFF55FFFF;
    public static final int DRAG_ICON_ACTIVE = 0xFFFFD700;
    public static final int DRAG_ICON_IDLE = 0xFF88AAAA;

    public static final int SELECTION_HIGHLIGHT = 0xFF0066CC;
}
