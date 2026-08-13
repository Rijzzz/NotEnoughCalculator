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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class ReflectionUtilsTest {

    static class DummyParent {
        private String parentField = "parent";
    }

    static class DummyChild extends DummyParent {
        private int childField = 42;
    }

    @Test
    @DisplayName("Find field in inheritance hierarchy")
    void testFindFieldInHierarchy() {
        Field fChild = ReflectionUtils.findFieldInHierarchy(DummyChild.class, "childField");
        assertNotNull(fChild);
        assertEquals("childField", fChild.getName());

        Field fParent = ReflectionUtils.findFieldInHierarchy(DummyChild.class, "parentField");
        assertNotNull(fParent);
        assertEquals("parentField", fParent.getName());

        Field fNotFound = ReflectionUtils.findFieldInHierarchy(DummyChild.class, "nonExistent");
        assertNull(fNotFound);
    }
}
