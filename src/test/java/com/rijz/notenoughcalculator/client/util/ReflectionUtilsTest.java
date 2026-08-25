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

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ReflectionUtilsTest {

	@SuppressWarnings("unused")
	static class DummyParent {
		private String parentField = "parent";
	}

	@SuppressWarnings("unused")
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

	@Test
	@DisplayName("Null safety for reflection methods")
	void testNullSafety() {
		assertEquals(0, ReflectionUtils.getCursorPosition(null));
		assertEquals(0, ReflectionUtils.getSelectionEnd(null));
		assertDoesNotThrow(() -> ReflectionUtils.clampSearchField(null));
		assertTrue(ReflectionUtils.isNoSelection(null));
	}

	static class DummyBox {
		int cursor = 5;
		int selectionEnd = 10;
		String text = "hello";

		public String getText() {
			return text;
		}

		public int getCursorPosition() {
			return cursor;
		}

		public int getSelectionEnd() {
			return selectionEnd;
		}

		public void setCursorPosition(int pos) {
			this.cursor = pos;
		}

		public void setSelectionEnd(int pos) {
			this.selectionEnd = pos;
		}
	}

	@Test
	@DisplayName("Test cursor, selection, and clamping on mock search box")
	void testMockSearchBox() {
		DummyBox box = new DummyBox();
		assertEquals(5, ReflectionUtils.getCursorPosition(box));
		assertEquals(10, ReflectionUtils.getSelectionEnd(box));

		// text length is 5, selectionEnd is 10 (out of bounds) -> clamp should fix it
		// to 5
		ReflectionUtils.clampSearchField(box);
		assertEquals(5, box.selectionEnd);
	}
}
