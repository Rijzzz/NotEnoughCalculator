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

import com.rijz.notenoughcalculator.client.integration.SearchFieldAdapter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {

	public static int getCursorPosition(Object searchField) {
		if (searchField == null)
			return 0;
		try {
			Method m = searchField.getClass().getMethod("getCursorPosition");
			Object res = m.invoke(searchField);
			if (res instanceof Integer)
				return (Integer) res;
		} catch (Throwable ignored) {
		}
		try {
			Method m = searchField.getClass().getMethod("getCursor");
			Object res = m.invoke(searchField);
			if (res instanceof Integer)
				return (Integer) res;
		} catch (Throwable ignored) {
		}
		try {
			Field f = findFieldInHierarchy(searchField.getClass(), "cursor");
			if (f != null) {
				f.setAccessible(true);
				Object res = f.get(searchField);
				if (res instanceof Integer)
					return (Integer) res;
			}
		} catch (Throwable ignored) {
		}
		try {
			Field f = findFieldInHierarchy(searchField.getClass(), "cursorPosition");
			if (f != null) {
				f.setAccessible(true);
				Object res = f.get(searchField);
				if (res instanceof Integer)
					return (Integer) res;
			}
		} catch (Throwable ignored) {
		}
		return 0;
	}

	public static int getSelectionEnd(Object searchField) {
		if (searchField == null)
			return 0;
		try {
			Method m = searchField.getClass().getMethod("getSelectionEnd");
			Object res = m.invoke(searchField);
			if (res instanceof Integer)
				return (Integer) res;
		} catch (Throwable ignored) {
		}
		try {
			Field f = findFieldInHierarchy(searchField.getClass(), "selectionEnd");
			if (f != null) {
				f.setAccessible(true);
				Object res = f.get(searchField);
				if (res instanceof Integer)
					return (Integer) res;
			}
		} catch (Throwable ignored) {
		}
		try {
			Field f = findFieldInHierarchy(searchField.getClass(), "highlightPos");
			if (f != null) {
				f.setAccessible(true);
				Object res = f.get(searchField);
				if (res instanceof Integer)
					return (Integer) res;
			}
		} catch (Throwable ignored) {
		}
		return getCursorPosition(searchField);
	}

	public static void clampSearchField(Object searchField) {
		if (searchField == null)
			return;
		try {
			String text = "";
			try {
				Method getTextMethod = searchField.getClass().getMethod("getText");
				Object t = getTextMethod.invoke(searchField);
				if (t != null)
					text = t.toString();
			} catch (Throwable e) {
				try {
					Method getValueMethod = searchField.getClass().getMethod("getValue");
					Object t = getValueMethod.invoke(searchField);
					if (t != null)
						text = t.toString();
				} catch (Throwable ignored) {
				}
			}

			int len = text.length();
			int cursor = getCursorPosition(searchField);
			if (cursor > len || cursor < 0) {
				try {
					Method setCursorMethod = searchField.getClass().getMethod("setCursorPosition", int.class);
					setCursorMethod.invoke(searchField, len);
				} catch (Throwable e) {
					try {
						Method setCursorMethod = searchField.getClass().getMethod("setCursor", int.class);
						setCursorMethod.invoke(searchField, len);
					} catch (Throwable ignored) {
					}
				}
			}

			int selection = getSelectionEnd(searchField);
			if (selection > len || selection < 0) {
				try {
					Method setSelectionMethod = searchField.getClass().getMethod("setSelectionEnd", int.class);
					setSelectionMethod.invoke(searchField, len);
				} catch (Throwable e) {
					try {
						Method setSelectionMethod = searchField.getClass().getMethod("setHighlightPos", int.class);
						setSelectionMethod.invoke(searchField, len);
					} catch (Throwable ignored) {
					}
				}
			}
		} catch (Throwable ignored) {
		}
	}

	public static boolean isNoSelection(SearchFieldAdapter adapter) {
		if (adapter == null)
			return true;
		int cursor = adapter.getCursorPosition();
		int selection = adapter.getSelectionEnd();
		return cursor == selection;
	}

	public static Field findFieldInHierarchy(Class<?> clazz, String fieldName) {
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

	private static Field mcGuiField = null;
	private static Field guiScreenField = null;
	private static Method guiScreenMethod = null;
	private static Method setScreenMethod = null;
	private static boolean screenInitialized = false;

	private static void initScreenAccessors(Minecraft mc) {
		if (screenInitialized || mc == null)
			return;
		screenInitialized = true;
		try {
			mcGuiField = Minecraft.class.getDeclaredField("gui");
			mcGuiField.setAccessible(true);
			Object gui = mcGuiField.get(mc);
			if (gui != null) {
				Class<?> guiClass = gui.getClass();
				try {
					guiScreenField = guiClass.getDeclaredField("screen");
					guiScreenField.setAccessible(true);
				} catch (NoSuchFieldException e) {
					try {
						guiScreenMethod = guiClass.getDeclaredMethod("screen");
						guiScreenMethod.setAccessible(true);
					} catch (NoSuchMethodException ignored) {
					}
				}
				try {
					setScreenMethod = guiClass.getMethod("setScreen", Screen.class);
				} catch (NoSuchMethodException ignored) {
				}
			}
		} catch (Throwable ignored) {
		}

		if (setScreenMethod == null) {
			try {
				setScreenMethod = Minecraft.class.getMethod("setScreen", Screen.class);
			} catch (Throwable ignored) {
			}
		}
	}

	public static Screen getCurrentScreen(Minecraft mc) {
		if (mc == null)
			return null;
		initScreenAccessors(mc);
		try {
			if (mcGuiField != null) {
				Object gui = mcGuiField.get(mc);
				if (gui != null) {
					if (guiScreenField != null)
						return (Screen) guiScreenField.get(gui);
					if (guiScreenMethod != null)
						return (Screen) guiScreenMethod.invoke(gui);
				}
			}
		} catch (Throwable ignored) {
		}
		return null;
	}

	public static void openScreen(Minecraft mc, Screen screen) {
		if (mc == null)
			return;
		mc.execute(() -> {
			initScreenAccessors(mc);
			try {
				if (setScreenMethod != null) {
					if (setScreenMethod.getDeclaringClass().equals(Minecraft.class)) {
						setScreenMethod.invoke(mc, screen);
					} else if (mcGuiField != null) {
						Object gui = mcGuiField.get(mc);
						if (gui != null) {
							setScreenMethod.invoke(gui, screen);
						}
					}
				}
			} catch (Throwable ignored) {
			}
		});
	}
}
