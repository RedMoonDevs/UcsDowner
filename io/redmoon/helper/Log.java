package io.redmoon.helper;

import java.io.PrintStream;

/**
 * Basic log class.
 * 
 * @author Benjamin
 */
public class Log {
	private static boolean debug = false;
	private final static PrintStream OUT_PRINT = System.out;
	private static String tag = "Info";

	public static void init(String string) {
		tag = string;
	}

	public static void debug() {
		debug = !debug;
	}

	public static void debug(String message, Object... objects) {
		if (debug)
			OUT_PRINT.printf("[Debug] " + message + "\n", objects);
	}

	public static void info(String message, Object... objects) {
		tag("[" + tag + "]", message, objects);
	}

	public static void warn(String message, Object... objects) {
		tag("[Warn]", message, objects);
	}

	public static void error(String message, Object... objects) {
		tag("[Error]", message, objects);
	}

	private static void tag(String tag, String message, Object... objects) {
		OUT_PRINT.printf(tag + " " + message + "\n", objects);
	}

}
