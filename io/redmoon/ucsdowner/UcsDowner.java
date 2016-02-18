package io.redmoon.ucsdowner;

import java.io.IOException;

import io.redmoon.helper.Arguments;
import io.redmoon.helper.Log;
import io.redmoon.ucsdowner.minion.DownMinion;

/**
 * UCS is badly coded but no one wants to hear that conclusion. It is now the
 * reason why RedMoon decided to open-source this project.
 * <p>
 * <b>Take this, UCS ^^</b>
 * 
 * @author Benjamin
 */
public class UcsDowner {

	/**
	 * Starts the program.
	 * 
	 * @param args
	 *            the arguments
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		UcsDownArguments config = Arguments.parse(UcsDownArguments.class, args);

		if (config.getIp() == null)
			config.h.handle();

		Log.init("UCSDowner");
		Log.info("Starting the UCS Downing process...");
		DownMinion.start(config);
	}

}
