package io.redmoon.helper;

import java.lang.reflect.Field;

/**
 * A bunch of Reflection utils...
 * 
 * @author Benjamin
 */
public class FieldUtils {

	public static Field[] getFields(Object obj) {
		try {
			return obj.getClass().getDeclaredFields();
		} catch (Exception ex) {}
		return null;
	}

	public static Object getFieldValue(Field f, Object obj) {
		try {
			return f.get(obj);
		} catch (IllegalAccessException ex) {
			try {
				f.setAccessible(true);
				return f.get(obj);
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void setFieldValue(Object obj, String name, Object value) {
		try {
			Field f = obj.getClass().getDeclaredField(name);
			boolean b = f.isAccessible();
			f.setAccessible(true);
			f.set(obj, value);
			f.setAccessible(b);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static <T> T newInstance(Class<T> type) {
		if (type != null) {
			try {
				return type.newInstance();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}

}
