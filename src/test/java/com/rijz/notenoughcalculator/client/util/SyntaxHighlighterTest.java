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
        assertTrue(highlighted.contains("§3100"), "Numbers should be Dark Aqua / Cyan (§3)");
        assertTrue(highlighted.contains("§bm"), "Units should be Vibrant Cyan (§b)");
    }

    @Test
    public void testFunctionHighlighting() {
        String highlighted = SyntaxHighlighter.highlight("sqrt(50m)");
        assertTrue(highlighted.contains("§esqrt"), "Math functions should be Bright Yellow (§e)");
        assertTrue(highlighted.contains("§350"), "Numbers should be Dark Aqua / Cyan (§3)");
        assertTrue(highlighted.contains("§bm"), "Units should be Vibrant Cyan (§b)");

        String marketHighlighted = SyntaxHighlighter.highlight("bzb(SUPERBOOM_TNT)");
        assertTrue(marketHighlighted.contains("§9bzb"), "Market functions should be Royal Blue (§9)");
        assertTrue(marketHighlighted.contains("§dSUPERBOOM_TNT"), "Item IDs should be Light Purple (§d)");

        String progHighlighted = SyntaxHighlighter.highlight("vampirexp(5) + skillxp(50) + perk(mining_speed)");
        assertTrue(progHighlighted.contains("§6vampirexp"), "Progression functions should be Vibrant Gold (§6)");
        assertTrue(progHighlighted.contains("§6skillxp"), "Progression functions should be Vibrant Gold (§6)");
        assertTrue(progHighlighted.contains("§6perk"), "Perk functions should be Vibrant Gold (§6)");

        String radixHighlighted = SyntaxHighlighter.highlight("hex(255) + 0xFF");
        assertTrue(radixHighlighted.contains("§dhex"), "Radix functions should be Light Purple (§d)");
        assertTrue(radixHighlighted.contains("§d0xFF"), "Radix literals should be Light Purple (§d)");
    }

    @Test
    public void testVariableHighlighting() {
        String highlighted = SyntaxHighlighter.highlight("$buy + ans + $purse + $coins + $w");
        assertTrue(highlighted.contains("§6$§3buy"), "Dollar sign should be Gold (§6) and custom var Dark Aqua (§3)");
        assertTrue(highlighted.contains("§bans"), "Builtin variables should be Vibrant Aqua (§b)");
        assertTrue(highlighted.contains("§6$§bpurse"), "Dollar sign Gold (§6) and API variable Vibrant Aqua (§b)");
        assertTrue(highlighted.contains("§6$§bcoins"), "Dollar sign Gold (§6) and alias coins Vibrant Aqua (§b)");
        assertTrue(highlighted.contains("§6$§bw"), "Dollar sign Gold (§6) and alias w Vibrant Aqua (§b)");
    }

    @Test
    public void testOperatorsAndParens() {
        String highlighted = SyntaxHighlighter.highlight("(10 + 5) * 2 << 1");
        assertTrue(highlighted.contains("§7("), "Parens should be Light Gray (§7)");
        assertTrue(highlighted.contains("§c+"), "Operators should be Bright Light Red (§c)");
        assertTrue(highlighted.contains("§c<<"), "Bitwise shift operators should be Bright Light Red (§c)");
    }

    @Test
    public void testStorageUnitsAndQuotedStrings() {
        String unitHighlighted = SyntaxHighlighter.highlight("2dc + 1eb + 3sc");
        assertTrue(unitHighlighted.contains("§bdc"), "Double chest unit should be Vibrant Aqua (§b)");
        assertTrue(unitHighlighted.contains("§beb"), "Ender chest unit should be Vibrant Aqua (§b)");
        assertTrue(unitHighlighted.contains("§bsc"), "Small chest unit should be Vibrant Aqua (§b)");

        String quoteHighlighted = SyntaxHighlighter.highlight("bzb(\"HYPERION\")");
        assertTrue(quoteHighlighted.contains("§d\"HYPERION\""), "Quoted string should be Light Purple (§d)");
    }

    @Test
    public void testResultAndErrorColorCodes() {
        assertEquals("§a", SyntaxHighlighter.getColorResult());
        assertEquals("§c", SyntaxHighlighter.getColorError());
    }
}
