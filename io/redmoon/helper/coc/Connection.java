package io.redmoon.helper.coc;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

import io.redmoon.helper.Log;
import io.redmoon.helper.PacketStream;
import io.redmoon.helper.coc.crypto.RC4;
import io.redmoon.helper.coc.crypto.Scramble7;

/**
 * Simple class to handle a Supercell-style (CoC, BB, CR, HD) connection.
 * <p>
 * The reading is done in the <code>readingThread</code>
 * <p>
 * The writing of a message is done by {@link#send(Message)}.
 * 
 * @author Benjamin
 */
public class Connection {

	public static final int PORT = 9339;
	public static final String KEY = "fhsd6f86f67rt8fw78fw789we78r9789wer6re";
	public static final int ENCRYPTION = 20000;

	private Thread readingThread;

	private Socket sock;
	private RC4 decrypt;
	private RC4 encrypt;

	private int waitingFor;
	private volatile boolean wait;

	/**
	 * Handles the most basic connection possible.
	 * 
	 * @param ip
	 *            the server to connect to.
	 * @throws IOException
	 */
	public Connection(String ip) throws IOException {
		Log.info("Connecting to the server...");
		try {
			sock = new Socket(ip, PORT);
		} catch (ConnectException ce) {
			Log.error("Couldn't connect to the server.");
			Log.error("Aborting...");
			System.exit(0);
		}
		decrypt = new RC4((KEY + "nonce").getBytes());
		encrypt = new RC4((KEY + "nonce").getBytes());

		readingThread = new Thread(() -> {
			try {
				DataInputStream dis = new DataInputStream(sock.getInputStream());
				while (true) {
					Message received = Message.read(dis, decrypt);
					if (received == null)
						break;
					received.log();
					if (received.header().getId() == ENCRYPTION) {
						Scramble7 s = new Scramble7(0);
						PacketStream ps = PacketStream.in(received.payload());
						byte[] random = ps.getStringAsBytes();

						byte[] nonce = s.getScramble(random);
						decrypt = new RC4(concat(KEY.getBytes(), nonce));
						encrypt = new RC4(concat(KEY.getBytes(), nonce));
					}

					if (received.header().getId() == waitingFor)
						wait = false;
				}
			} catch (SocketException se) {
				Log.info("Success! The server is now down!");
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				disconnect();
			}
		});
		readingThread.start();
	}

	/**
	 * Concats multiple byte arrays.
	 * 
	 * @param byteArrays
	 *            the byte arrays to concat
	 * @return the concatenation of the arrays provided.
	 */
	public static byte[] concat(byte[]... byteArrays) {
		int length = 0;
		for (byte[] ba : byteArrays)
			length += ba.length;
		byte[] ret = new byte[length];

		for (int i = 0, j = 0; j < byteArrays.length; i += byteArrays[j++].length)
			System.arraycopy(byteArrays[j], 0, ret, i, byteArrays[j].length);

		return ret;
	}

	/**
	 * Send a message.
	 * 
	 * @param message
	 *            the message to send
	 * @throws IOException
	 */
	public void send(Message message) throws IOException {
		OutputStream os = sock.getOutputStream();
		os.write(message.build(encrypt));
		os.flush();

		message.log();
	}

	/**
	 * Disconnect from the socket.
	 * 
	 * @note It isn't really safe as the exception isn't printed (if there is an
	 *       exception).
	 */
	public void disconnect() {
		try {
			sock.close();
		} catch (Exception ex) {}
	}

	/**
	 * Sketchy method that blocks until a packet is received.
	 * 
	 * @param packetId
	 *            the id of the packet to wait
	 */
	public synchronized void waitFor(int packetId) {
		waitingFor = packetId;
		wait = true;
		while (wait)
			try {
				Thread.sleep(100L);
			} catch (Exception ex) {}
	}

}
