package io.redmoon.helper.coc.crypto;

/**
 * Arc4
 */
public class RC4 {
	private byte[] S = new byte[256];
	private int i = 0, j = 0;

	public RC4(final byte[] key) {
		for (int i = 0; i < 256; i++) {
			S[i] = (byte) i;
		}

		int j = 0;
		byte tmp;
		for (int i = 0; i < 256; i++) {
			j = (j + S[i] + key[i % key.length]) & 0xFF;
			tmp = S[j];
			S[j] = S[i];
			S[i] = tmp;
		}

		skipFirstBytes(key.length);
	}

	public byte[] crypt(final byte[] plaintext) {
		if (plaintext == null)
			return new byte[0];
		byte[] ciphertext = new byte[plaintext.length];
		byte temp;
		int k;

		for (int counter = 0; counter < plaintext.length; counter++) {
			i = (i + 1) & 0xFF;
			j = (j + S[i]) & 0xFF;

			temp = S[j];
			S[j] = S[i];
			S[i] = temp;

			k = S[(S[i] + S[j]) & 0xFF];
			ciphertext[counter] = (byte) (plaintext[counter] ^ k);
		}
		return ciphertext;
	}

	public byte[] skipFirstBytes(int size) {
		byte[] data = new byte[size];
		return crypt(data);
	}
}