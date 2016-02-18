package io.redmoon.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;

/**
 * Pretty straight-forward
 * 
 * @note The methods are kinda like {@link java.nio.ByteBuffer}
 * @author Aoi
 */
public class PacketStream {

	/**
	 * Allocates a new PacketStream in order to write in it.
	 * 
	 * @return a new PacketStream
	 */
	public static PacketStream out() {
		return new PacketStream();
	}

	/**
	 * Creates a new PacketStream in order to read its content.
	 * 
	 * @param array
	 *            the byte array containing the payload
	 * @return a new PacketStream
	 */
	public static PacketStream in(byte[] array) {
		return new PacketStream(array);
	}

	private ByteArrayOutputStream baos;
	private ByteArrayInputStream bais;

	private PacketInputStream in;
	private PacketOutputStream out;

	public PacketStream(byte[] array) {
		bais = new ByteArrayInputStream(array);
		in = new PacketInputStream(bais);
	}

	public PacketStream() {
		baos = new ByteArrayOutputStream();
		out = new PacketOutputStream(baos);
	}

	public byte[] toArray() {
		byte[] ret = baos == null ? null : baos.toByteArray();
		clear();
		return ret;
	}

	public void clear() {
		try {
			if (in != null) {
				in.close();
				bais.close();
			}
			if (out != null) {
				out.close();
				baos.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String getString() {
		try {
			return in.readString();
		} catch (Exception ex) {}
		return null;
	}

	public byte[] getStringAsBytes() {
		try {
			return in.readStringAsBytes();
		} catch (Exception ex) {}
		return null;
	}

	public void putString(String arg0) {
		try {
			out.writeString(arg0);
		} catch (Exception ex) {}
	}

	public long getLong() {
		try {
			return in.readLong();
		} catch (Exception ex) {}
		return 0;
	}

	public void putLong(long arg0) {
		try {
			out.writeLong(arg0);
		} catch (Exception ex) {}
	}

	public int getInt() {
		try {
			return in.readInt();
		} catch (Exception ex) {}
		return 0;
	}

	public void putInt(int arg0) {
		try {
			out.writeInt(arg0);
		} catch (Exception ex) {}
	}

	public void putMedium(int arg0) {
		try {
			out.writeMedium(arg0);
		} catch (Exception ex) {}
	}

	public short getShort() {
		try {
			return in.readShort();
		} catch (Exception ex) {}
		return 0;
	}

	public void putShort(short arg0) {
		try {
			out.writeShort(arg0);
		} catch (Exception ex) {}
	}

	public byte get() {
		try {
			return in.readByte();
		} catch (Exception ex) {}
		return 0;
	}

	public boolean getBoolean() {
		return get() == 0x0 ? false : true;
	}

	public void putBoolean(boolean arg0) {
		this.put((byte) (arg0 ? 0x1 : 0x0));
	}

	public void put(byte arg0) {
		try {
			out.write(arg0);
		} catch (Exception ex) {}
	}

	public void put(byte[] arg0) {
		try {
			out.write(arg0);
		} catch (Exception ex) {}
	}

	public void putBytes(byte[] arg0) {
		try {
			out.writeBytesAsString(arg0);
		} catch (Exception ex) {}
	}

	public void putNullString() {
		putString(null);
	}

	public void putLEInt(int arg0) {
		try {
			out.writeLEInt(arg0);
		} catch (Exception ex) {}
	}

	public int getMedium() {
		return get() << 24 | getShort();
	}

	public void putCompressedData(String data) {
		putBoolean(true);

		Deflater deflater = new Deflater();
		deflater.setInput(data.getBytes());
		deflater.finish();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.getBytes().length);
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		byte[] output = outputStream.toByteArray();

		putInt(output.length + 4);
		putLEInt(data.getBytes().length);
		put(output);
	}

	private static class PacketInputStream extends DataInputStream {
		public PacketInputStream(InputStream is) {
			super(is);
		}

		public String readString() throws IOException {
			return new String(readStringAsBytes());
		}

		public byte[] readStringAsBytes() throws IOException {
			int length = readInt();
			if (length == -1)
				return new byte[0];
			byte[] array = new byte[length];
			readFully(array);
			return array;
		}
	}

	private static class PacketOutputStream extends DataOutputStream {
		public PacketOutputStream(OutputStream os) {
			super(os);
		}

		public void writeString(String toWrite) throws IOException {
			this.writeBytesAsString(toWrite == null ? null : toWrite.getBytes(StandardCharsets.UTF_8));
		}

		public void writeBytesAsString(byte[] toWrite) throws IOException {
			if (toWrite == null) {
				writeInt(-1);
			} else {
				writeInt(toWrite.length);
				write(toWrite);
			}
		}

		public void writeMedium(int a) throws IOException {
			byte[] ret = new byte[3];
			ret[2] = (byte) (a & 0xff);
			ret[1] = (byte) (a >> 8 & 0xff);
			ret[0] = (byte) (a >> 16 & 0xff);
			this.write(ret);
		}

		private void writeLEInt(int value) throws IOException {
			this.writeByte(value & 0xFF);
			this.writeByte((value >> 8) & 0xFF);
			this.writeByte((value >> 16) & 0xFF);
			this.writeByte((value >> 24) & 0xFF);
		}
	}

	@Deprecated
	public static PacketStream wrap(ByteBuffer allocate) {
		return new PacketStream(allocate.array());
	}

}
