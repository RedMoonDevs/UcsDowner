package io.redmoon.helper;

import static io.redmoon.helper.FieldUtils.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Arguments helper.
 * 
 * Arguments are presented this way:
 * 
 * <pre>
 * 	[*] --arg=val
 * 	[*] -o
 * </pre>
 * 
 * The different types of objects are:
 * 
 * <pre>
 *  [*] boolean
 *  [*] String
 *  [*] int
 *  [*] {@link#Handler}
 * </pre>
 * 
 * @author Benjamin
 */
public class Arguments {

	/**
	 * Creates an instance of <code>T</code> and fills in the fields according
	 * to the <code>args</code> parameter.
	 * 
	 * @param clazz
	 *            T
	 * @param args
	 *            the arguments
	 * @return
	 * @throws Exception
	 */
	public static <T extends Arguments> T parse(Class<T> clazz, String[] args) throws Exception {
		T ret = newInstance(clazz);
		Map<String, String> argsMap = new HashMap<String, String>();
		List<String> toExecute = new ArrayList<String>();
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("--"))
				argsMap.put(arg.substring(2, arg.indexOf('=')),
						arg.contains("=") ? arg.substring(arg.indexOf('=') + 1) : "");
			else if (arg.startsWith("-"))
				toExecute.add(arg.substring(1));
		}

		for (Field f : clazz.getDeclaredFields()) {
			String fieldName = f.getName();
			Object value = null;
			Class<?> type = f.getType();

			if (!is(type, boolean.class, int.class, String.class, Handler.class))
				continue;

			if (f.isAnnotationPresent(Argument.class))
				fieldName = f.getAnnotation(Argument.class).value();

			if (f.isAnnotationPresent(Default.class))
				value = f.getAnnotation(Default.class).value();

			value = argsMap.containsKey(fieldName) ? argsMap.get(fieldName) : value;

			if (type == int.class)
				value = Integer.valueOf((String) value);
			else if (type == boolean.class)
				value = Boolean.valueOf((String) (value == "" ? value : "false"));

			if (toExecute.contains(fieldName)) {
				if (f.getType() == Handler.class) {
					((Handler) getFieldValue(f, ret)).handle();
				}
			} else
				setFieldValue(ret, f.getName(), value);
		}
		return ret;
	}

	/**
	 * Compare the object <code>compare</code> to all the other objects
	 * (contained in <code>to</code>)
	 * 
	 * @param compare
	 *            the object to compare
	 * @param to
	 *            compare it to
	 * @return whether the object to compare equals one of the elements in
	 *         <code>to</code>
	 */
	private static boolean is(Object compare, Object... to) {
		if (compare == null)
			return false;
		return Arrays.stream(to).anyMatch(c -> compare.equals(c));
	}

	/**
	 * Use this annotation if the field's name is different from the argument.
	 * 
	 * @author Benjamin
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Argument {
		/**
		 * Name of the argument
		 * 
		 * @return name of the argument
		 */
		public String value();
	}

	/**
	 * Use this annotation if the argument has a default value.
	 * 
	 * @author Benjamin
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Default {
		/**
		 * Default value of an argument.
		 * 
		 * @return its default value
		 */
		public String value() default "";
	}

	/**
	 * Use this interface to make an action happen when a trigger is put.
	 * 
	 * <pre>
	 * () -> {
	 * 	// Insert code here.
	 * }
	 * </pre>
	 * 
	 * @author Benjamin
	 */
	public static interface Handler {
		/**
		 * What has to be done when it's triggered.
		 */
		public void handle();
	}
}
