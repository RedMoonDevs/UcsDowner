package io.redmoon.helper.coc;

import java.io.DataInputStream;
import java.io.IOException;

import javax.xml.bind.DatatypeConverter;

import io.redmoon.helper.Log;
import io.redmoon.helper.PacketStream;
import io.redmoon.helper.coc.crypto.RC4;

/**
 * Simple Message class.
 * <p>
 * To read a packet, see {@link#read}.
 * To write a packet, see {@link#Message} and {@link#MessageMaker}
 * 
 * @author Benjamin
 */
public class Message {
	private Header header;
	private PacketStream payload;

	private byte[] _payload;

	public Message() {}

	public Message(int id, MessageMaker mm) {
		this(id, mm, 0);
	}

	/**
	 * Write a packet using {@link#MessageMaker} as a lambda.
	 * 
	 * @param id
	 *            the id of the packet
	 * @param mm
	 *            the lambda/anonymous/declared class that writes the packet
	 * @param version
	 *            the version of the packet
	 */
	public Message(int id, MessageMaker mm, int version) {
		header = new Header();
		header.id = id;
		header.version = version;
		payload = PacketStream.out();
		mm.build(payload);
	}

	/**
	 * Build the packet and encrypt it.
	 * 
	 * @param rc4
	 *            the Arc4 stream
	 * @return the built packet
	 */
	public byte[] build(RC4 rc4) {
		PacketStream out = PacketStream.out();
		_payload = payload.toArray();
		byte[] newPayload = rc4.crypt(_payload);
		header.length = newPayload.length;
		header.encode(out);
		out.put(newPayload);
		return out.toArray();
	}

	/**
	 * Read a packet
	 * 
	 * @param in
	 *            the in stream
	 * @param rc4
	 *            the Arc4 stream
	 * @return the read packet
	 * @throws IOException
	 */
	public static Message read(DataInputStream in, RC4 rc4) throws IOException {
		Message ret = new Message();

		byte[] header = new byte[Header.LENGTH];
		in.readFully(header);
		Header h = new Header();
		h.decode(PacketStream.in(header));

		byte[] payload = new byte[h.length];
		in.readFully(payload);
		payload = rc4.crypt(payload);
		ret._payload = payload;

		ret.header = h;
		ret.payload = PacketStream.in(payload);
		return ret;
	}

	/**
	 * @return the header
	 */
	public Header header() {
		return header;
	}

	/**
	 * @return the raw payload
	 */
	public byte[] payload() {
		return _payload;
	}

	/**
	 * Logs the packet
	 */
	public void log() {
		String from = header.id >= 20000 ? "received" : "sent";
		Log.info("Message[%d,%d,%d] %s -> %s", header.id, header.length, header.version, from,
				header.length > 2048 ? "#TOO_LONG" : DatatypeConverter.printHexBinary(_payload));
	}

	/**
	 * Header consisting of:
	 * 
	 * <pre>
	 * struct Header { short id; int_24 length; short version; }
	 * <pre>
	 * 
	 * @author Benjamin
	 */
	public static class Header {
		public static final int LENGTH = 7;

		private int id, length, version;

		/**
		 * Encode the header into <code>out</code>
		 * 
		 * @param out
		 */
		public void encode(PacketStream out) {
			out.putShort((short) id);
			out.putMedium(length);
			out.putShort((short) version);
		}

		/**
		 * Decode the header out of <code>in</code>
		 * 
		 * @param in
		 */
		public void decode(PacketStream in) {
			id = in.getShort();
			length = in.getMedium();
			version = in.getShort();
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public int getLength() {
			return length;
		}

		public void setLength(int length) {
			this.length = length;
		}

		public int getVersion() {
			return version;
		}

		public void setVersion(int version) {
			this.version = version;
		}
	}

	/**
	 * Easy to use as a lambda, but can also be implemented in a
	 * declarated/anonymous class.
	 * 
	 * <pre>
	 * out -> {
	 * 	// Write the packet
	 * }
	 * </pre>
	 * 
	 * @author Benjamin
	 */
	public static interface MessageMaker {
		public void build(PacketStream out);
	}
}
