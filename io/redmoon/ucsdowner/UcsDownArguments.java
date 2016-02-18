package io.redmoon.ucsdowner;

import io.redmoon.helper.Arguments;

/**
 * The different arguments needed for UcsDowner to work.
 * 
 * @author Benjamin
 */
public class UcsDownArguments extends Arguments {

	protected String ip;
	protected @Default("40000") int recursion;
	protected Handler h = () -> {
		System.out.println("Usage: java -jar UcsDowner.jar -h[for help] [--opt=val]");
		System.out.println();
		System.out.println("--ip            - IP of the server to down.");
		System.out.println("--recursion     - Level of recursivity (default: 40000).");
		System.out.println("-h              - Show help.");

		System.exit(0);
	};

	public String getIp() {
		return ip;
	}

	public int getRecursion() {
		return recursion;
	}

}
