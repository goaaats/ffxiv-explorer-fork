package com.fragmenterworks.ffxivextract.paths;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Checksum;

/**
 * CRC-64 implementation with ability to combine checksums calculated over
 * different blocks of data.
 *
 * This is a faster version of the original implementation by R. Nikitchenko,
 * incorporating the nested lookup table design by Mark Adler (see <a
 * href="http://stackoverflow.com/a/20579405/58962">Stackoverflow</a>).
 *
 * <a href="https://stackoverflow.com/a/20586626">from here</a>
 *
 * @author Roman Nikitchenko (roman@nikitchenko.dp.ua)
 * @author Michael Böckling
 */
public class Crc64 implements Checksum
{
	private final static long POLY = 0xc96c5795d7870f42L; // ECMA-182

	/* Crc64 calculation table. */
	private final static long[][] table;

	/* Current CRC value. */
	private long value;

	static
	{
		/*
		 * Nested tables as described by Mark Adler:
		 * http://stackoverflow.com/a/20579405/58962
		 */
		table = new long[8][256];

		for (int n = 0; n < 256; n++)
		{
			long crc = n;
			for (int k = 0; k < 8; k++)
			{
				if ((crc & 1) == 1)
				{
					crc = (crc >>> 1) ^ POLY;
				}
				else
				{
					crc = (crc >>> 1);
				}
			}
			table[0][n] = crc;
		}

		/* generate nested CRC table for future slice-by-8 lookup */
		for (int n = 0; n < 256; n++)
		{
			long crc = table[0][n];
			for (int k = 1; k < 8; k++)
			{
				crc = table[0][(int) (crc & 0xff)] ^ (crc >>> 8);
				table[k][n] = crc;
			}
		}
	}

	/**
	 * Initialize with a value of zero.
	 */
	public Crc64()
	{
		this.value = 0;
	}

	/**
	 * Initialize with a custom CRC value.
	 *
	 * @param value
	 */
	public Crc64(long value)
	{
		this.value = value;
	}

	/**
	 * Initialize by calculating the CRC of the given byte blocks.
	 *
	 * @param b
	 *            block of bytes
	 * @param len
	 *            number of bytes to process
	 */
	public Crc64(byte[] b, int len)
	{
		this.value = 0;
		update(b, len);
	}

	/**
	 * Initialize by calculating the CRC of the given byte blocks.
	 *
	 * @param b
	 *            block of bytes
	 * @param off
	 *            starting offset of the byte block
	 * @param len
	 *            number of bytes to process
	 */
	public Crc64(byte[] b, int off, int len)
	{
		this.value = 0;
		update(b, off, len);
	}

	/**
	 * Construct new Crc64 instance from byte array.
	 */
	public static Crc64 fromBytes(byte[] b)
	{
		long l = 0;
		for (int i = 0; i < 4; i++)
		{
			l <<= 8;
			l ^= (long) b[i] & 0xFF;
		}
		return new Crc64(l);
	}

	public static Crc64 fromString(String s)
	{
		return fromBytes(s.getBytes());
	}

	public static long compute(String s) {
		return compute(s.getBytes());
	}

	public static long compute(byte[] b) {
		Crc64 run = new Crc64();
		run.update(b, b.length);
		return run.getValue();
	}

	/**
	 * Calculate the Crc64 of the given file's content.
	 *
	 * @param f
	 * @return new {@link Crc64} instance initialized to the file's CRC value
	 * @throws IOException
	 *             in case the {@link FileInputStream#read(byte[])} method fails
	 */
	public static Crc64 fromFile(File f) throws IOException
	{
		return fromInputStream(new FileInputStream(f));
	}

	/**
	 * Calculate the Crc64 of the given {@link InputStream} until the end of the
	 * stream has been reached.
	 *
	 * @param in
	 *            the stream will be closed automatically
	 * @return new {@link Crc64} instance initialized to the {@link InputStream}'s CRC value
	 * @throws IOException
	 *             in case the {@link InputStream#read(byte[])} method fails
	 */
	public static Crc64 fromInputStream(InputStream in) throws IOException
	{
		try
		{
			Crc64 crc = new Crc64();
			byte[] b = new byte[65536];
			int l = 0;

			while ((l = in.read(b)) != -1)
			{
				crc.update(b, l);
			}

			return crc;

		} finally
		{
			in.close();
		}
	}

	/**
	 * Get 8 byte representation of current Crc64 value.
	 */
	public byte[] getBytes()
	{
		byte[] b = new byte[8];
		for (int i = 0; i < 8; i++)
		{
			b[7 - i] = (byte) (this.value >>> (i * 8));
		}
		return b;
	}

	/**
	 * Get long representation of current Crc64 value.
	 */
	public long getValue()
	{
		return this.value;
	}

	/**
	 * Update Crc64 with new byte block.
	 */
	public void update(byte[] b, int len) {
		this.update(b, 0, len);
	}

	/**
	 * Update Crc64 with new byte block.
	 */
	public void update(byte[] b, int off, int len)
	{
		this.value = ~this.value;

		/* fast middle processing, 8 bytes (aligned!) per loop */

		int idx = off;
		while (len >= 8)
		{
			value = table[7][(int) (value & 0xff ^ (b[idx] & 0xff))]
					^ table[6][(int) ((value >>> 8) & 0xff ^ (b[idx + 1] & 0xff))]
					^ table[5][(int) ((value >>> 16) & 0xff ^ (b[idx + 2] & 0xff))]
					^ table[4][(int) ((value >>> 24) & 0xff ^ (b[idx + 3] & 0xff))]
					^ table[3][(int) ((value >>> 32) & 0xff ^ (b[idx + 4] & 0xff))]
					^ table[2][(int) ((value >>> 40) & 0xff ^ (b[idx + 5] & 0xff))]
					^ table[1][(int) ((value >>> 48) & 0xff ^ (b[idx + 6] & 0xff))]
					^ table[0][(int) ((value >>> 56) ^ b[idx + 7] & 0xff)];
			idx += 8;
			len -= 8;
		}

		/* process remaining bytes (can't be larger than 8) */
		while (len > 0)
		{
			value = table[0][(int) ((this.value ^ b[idx]) & 0xff)] ^ (this.value >>> 8);
			idx++;
			len--;
		}

		this.value = ~this.value;
	}

	public void update(int b) {
		this.update(new byte[]{(byte)b}, 0, 1);
	}

	public void reset() {
		this.value = 0;
	}

	// dimension of GF(2) vectors (length of CRC)
	private static final int GF2_DIM = 64;

	private static long gf2MatrixTimes(long[] mat, long vec)
	{
		long sum = 0;
		int idx = 0;
		while (vec != 0)
		{
			if ((vec & 1) == 1)
				sum ^= mat[idx];
			vec >>>= 1;
			idx++;
		}
		return sum;
	}

	private static void gf2MatrixSquare(long[] square, long[] mat)
	{
		for (int n = 0; n < GF2_DIM; n++)
			square[n] = gf2MatrixTimes(mat, mat[n]);
	}

	/*
	 * Return the CRC-64 of two sequential blocks, where summ1 is the CRC-64 of
	 * the first block, summ2 is the CRC-64 of the second block, and len2 is the
	 * length of the second block.
	 */
	static public Crc64 combine(Crc64 summ1, Crc64 summ2, long len2)
	{
		// degenerate case.
		if (len2 == 0)
			return new Crc64(summ1.getValue());

		int n;
		long row;
		long[] even = new long[GF2_DIM]; // even-power-of-two zeros operator
		long[] odd = new long[GF2_DIM]; // odd-power-of-two zeros operator

		// put operator for one zero bit in odd
		odd[0] = POLY; // CRC-64 polynomial

		row = 1;
		for (n = 1; n < GF2_DIM; n++)
		{
			odd[n] = row;
			row <<= 1;
		}

		// put operator for two zero bits in even
		gf2MatrixSquare(even, odd);

		// put operator for four zero bits in odd
		gf2MatrixSquare(odd, even);

		// apply len2 zeros to crc1 (first square will put the operator for one
		// zero byte, eight zero bits, in even)
		long crc1 = summ1.getValue();
		long crc2 = summ2.getValue();
		do
		{
			// apply zeros operator for this bit of len2
			gf2MatrixSquare(even, odd);
			if ((len2 & 1) == 1)
				crc1 = gf2MatrixTimes(even, crc1);
			len2 >>>= 1;

			// if no more bits set, then done
			if (len2 == 0)
				break;

			// another iteration of the loop with odd and even swapped
			gf2MatrixSquare(odd, even);
			if ((len2 & 1) == 1)
				crc1 = gf2MatrixTimes(odd, crc1);
			len2 >>>= 1;

			// if no more bits set, then done
		} while (len2 != 0);

		// return combined crc.
		crc1 ^= crc2;
		return new Crc64(crc1);
	}

}