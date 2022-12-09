package com.fragmenterworks.ffxivextract.paths;

import java.util.zip.CRC32;

public class Crc32 {
	public static int compute(String s) {
		byte[] b = s.getBytes();
		return compute(b, 0, b.length);
	}

	public static int compute(byte[] bytes, int start, int end) {
		CRC32 crc = new CRC32();
		crc.update(bytes, start, end);
		return ~((int) crc.getValue());
	}
}
