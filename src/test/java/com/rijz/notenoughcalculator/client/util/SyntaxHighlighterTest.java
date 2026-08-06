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

package com.rijz.notenoughcalculator.client.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SyntaxHighlighterTest {

    @Test
    public void testEmptyAndNullInput() {
        assertEquals("", SyntaxHighlighter.highlight(null));
        assertEquals("", SyntaxHighlighter.highlight(""));
    }

    @Test
    public void testNumberAndUnitHighlighting() {
        String highlighted = SyntaxHighlighter.highlight("100m");
        assertTrue(highlighted.contains("§f100"), "Numbers should be Pure White (§f)");
        assertTrue(highlighted.contains("§bm"), "Units should be Vibrant Cyan (§b)");
    }

    @Test
    public void testFunctionHighlighting() {
        String highlighted = SyntaxHighlighter.highlight("bz(50m)");
        assertTrue(highlighted.contains("§ebz"), "Functions should be Bright Yellow (§e)");
        assertTrue(highlighted.contains("§f50"), "Numbers should be Pure White (§f)");
        assertTrue(highlighted.contains("§bm"), "Units should be Vibrant Cyan (§b)");
    }

    @Test
    public void testVariableHighlighting() {
        String highlighted = SyntaxHighlighter.highlight("$buy + ans");
        assertTrue(highlighted.contains("§a$buy"), "Custom variables should be Bright Green (§a)");
        assertTrue(highlighted.contains("§aans"), "Builtin variables should be Bright Green (§a)");
    }

    @Test
    public void testOperatorsAndParens() {
        String highlighted = SyntaxHighlighter.highlight("(10 + 5) * 2");
        assertTrue(highlighted.contains("§7("), "Parens should be Light Gray (§7)");
        assertTrue(highlighted.contains("§6+"), "Operators should be Bright Gold (§6)");
    }
}
