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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class REIHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(REIHelper.class);
	private static Field boundsField = null;
	private static Method getBoundsMethod = null;
	private static boolean reflectionAttempted = false;

	private static Field cursorField = null;
	private static Field selectionEndField = null;
	private static Method getCursorMethod = null;
	private static Method getSelectionEndMethod = null;
	private static Method setCursorMethod = null;
	private static Method setSelectionEndMethod = null;
	private static boolean textFieldReflectionInitialized = false;

	private static void init(TextField searchField) {
		if (reflectionAttempted)
			return;
		if (searchField == null)
			return;

		Class<?> implClass = searchField.getClass();

		try {
			getBoundsMethod = implClass.getMethod("getBounds");
			getBoundsMethod.setAccessible(true);
			LOGGER.debug("Found getBounds in {}", implClass.getSimpleName());
		} catch (NoSuchMethodException ignored) {
		}

		if (getBoundsMethod == null) {
			boundsField = findBoundsField(implClass);
			if (boundsField != null) {
				boundsField.setAccessible(true);
				LOGGER.debug("Found bounds field in {}", implClass.getSimpleName());
			}
		}

		if (getBoundsMethod != null || boundsField != null) {
			reflectionAttempted = true;
			LOGGER.info("REIHelper reflection cache initialized successfully");
		}
	}

	private static void initTextFieldReflection(TextField searchField) {
		if (textFieldReflectionInitialized)
			return;
		if (searchField == null)
			return;

		Class<?> fieldClass = searchField.getClass();

		try {
			getCursorMethod = fieldClass.getMethod("getCursor");
			getCursorMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			String[] cursorNames = {"cursor", "cursorPosition", "cursorPos", "caretPosition"};
			for (String name : cursorNames) {
				try {
					cursorField = findFieldInHierarchy(fieldClass, name);
					if (cursorField != null) {
						cursorField.setAccessible(true);
						break;
					}
				} catch (Exception ignored) {
				}
			}
		}

		try {
			getSelectionEndMethod = fieldClass.getMethod("getSelectionEnd");
			getSelectionEndMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			String[] selectionNames = {"selectionEnd", "selectionEndPos", "selectionStart", "highlightPos"};
			for (String name : selectionNames) {
				try {
					selectionEndField = findFieldInHierarchy(fieldClass, name);
					if (selectionEndField != null) {
						selectionEndField.setAccessible(true);
						break;
					}
				} catch (Exception ignored) {
				}
			}
		}

		String[] setCursorNames = {"setCursor", "setCursorPosition", "setCaretPosition"};
		for (String name : setCursorNames) {
			try {
				setCursorMethod = fieldClass.getMethod(name, int.class);
				setCursorMethod.setAccessible(true);
				break;
			} catch (NoSuchMethodException ignored) {
			}
		}

		String[] setSelectionNames = {"setSelectionEnd", "setSelectionStart", "setHighlightPos"};
		for (String name : setSelectionNames) {
			try {
				setSelectionEndMethod = fieldClass.getMethod(name, int.class);
				setSelectionEndMethod.setAccessible(true);
				break;
			} catch (NoSuchMethodException ignored) {
			}
		}

		if ((getCursorMethod != null || cursorField != null)
				&& (getSelectionEndMethod != null || selectionEndField != null)) {
			textFieldReflectionInitialized = true;
		}
	}

	public static Rectangle getSearchFieldBounds(TextField searchField) {
		if (searchField == null) {
			return null;
		}

		init(searchField);

		try {
			if (getBoundsMethod != null) {
				try {
					return (Rectangle) getBoundsMethod.invoke(searchField);
				} catch (Exception e) {
					LOGGER.debug("getBounds invoke failed: {}", e.getMessage());
				}
			}

			if (boundsField != null) {
				Object fieldValue = boundsField.get(searchField);
				if (fieldValue instanceof Rectangle) {
					return (Rectangle) fieldValue;
				}
			}

		} catch (Exception e) {
			LOGGER.debug("Reflection bounds fetch failed: {}", e.getMessage());
		}

		return null;
	}

	public static void clampSearchField(TextField searchField) {
		if (searchField == null)
			return;
		initTextFieldReflection(searchField);
		String text = searchField.getText();
		int len = text != null ? text.length() : 0;

		try {
			int cursor = getCursorPosition(searchField);
			if (cursor > len || cursor < 0) {
				setRawCursor(searchField, len);
			}

			int selection = getSelectionEnd(searchField);
			if (selection > len || selection < 0) {
				setRawSelection(searchField, len);
			}
		} catch (Exception ignored) {
		}
	}

	public static int getCursorPosition(TextField searchField) {
		if (searchField == null)
			return 0;
		initTextFieldReflection(searchField);
		String text = searchField.getText();
		int len = text != null ? text.length() : 0;
		try {
			if (getCursorMethod != null) {
				Object result = getCursorMethod.invoke(searchField);
				if (result instanceof Integer)
					return Math.min(Math.max(0, (Integer) result), len);
			}
			if (cursorField != null) {
				Object result = cursorField.get(searchField);
				if (result instanceof Integer)
					return Math.min(Math.max(0, (Integer) result), len);
			}
		} catch (Exception ignored) {
		}
		return 0;
	}

	public static int getSelectionEnd(TextField searchField) {
		if (searchField == null)
			return 0;
		initTextFieldReflection(searchField);
		String text = searchField.getText();
		int len = text != null ? text.length() : 0;
		try {
			if (getSelectionEndMethod != null) {
				Object result = getSelectionEndMethod.invoke(searchField);
				if (result instanceof Integer)
					return Math.min(Math.max(0, (Integer) result), len);
			}
			if (selectionEndField != null) {
				Object result = selectionEndField.get(searchField);
				if (result instanceof Integer)
					return Math.min(Math.max(0, (Integer) result), len);
			}
		} catch (Exception ignored) {
		}
		return 0;
	}

	public static boolean isNoSelection(TextField searchField) {
		if (searchField == null)
			return true;
		int cursor = getCursorPosition(searchField);
		int selection = getSelectionEnd(searchField);
		return cursor == selection;
	}

	private static void setRawCursor(TextField searchField, int pos) {
		try {
			if (setCursorMethod != null) {
				setCursorMethod.invoke(searchField, pos);
				return;
			}
			if (cursorField != null) {
				cursorField.set(searchField, pos);
			}
		} catch (Exception ignored) {
		}
	}

	private static void setRawSelection(TextField searchField, int pos) {
		try {
			if (setSelectionEndMethod != null) {
				setSelectionEndMethod.invoke(searchField, pos);
				return;
			}
			if (selectionEndField != null) {
				selectionEndField.set(searchField, pos);
			}
		} catch (Exception ignored) {
		}
	}

	private static Field findBoundsField(Class<?> clazz) {
		String[] fieldNames = {"bounds", "bound", "rectangle", "area", "rect"};

		for (String fieldName : fieldNames) {
			try {
				return clazz.getDeclaredField(fieldName);
			} catch (NoSuchFieldException ignored) {
			}
		}

		Class<?> superClass = clazz.getSuperclass();
		while (superClass != null && !superClass.equals(Object.class)) {
			for (String fieldName : fieldNames) {
				try {
					return superClass.getDeclaredField(fieldName);
				} catch (NoSuchFieldException ignored) {
				}
			}
			superClass = superClass.getSuperclass();
		}

		return null;
	}

	private static Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
		try {
			return clazz.getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			Class<?> superClass = clazz.getSuperclass();
			if (superClass != null && !superClass.equals(Object.class)) {
				return findFieldInHierarchy(superClass, fieldName);
			}
		}
		return null;
	}
}
