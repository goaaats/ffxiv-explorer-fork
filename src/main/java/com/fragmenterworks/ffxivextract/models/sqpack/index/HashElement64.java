package com.fragmenterworks.ffxivextract.models.sqpack.index;

import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;

public class HashElement64 extends HashElement {
	public long hash;
	private int padding;

	public static HashElement64 read(EARandomAccessFile file) {
		HashElement64 element = new HashElement64();
		element.isBigEndian = file.isBigEndian();

		try {
			element.hash = file.readLong();
			element.data = file.readInt();
			element.padding = file.readInt();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return element;
	}

	public int getFileHash() {
		return (int) (hash);
	}

	public int getFolderHash() {
		return (int) ((hash >> 32));
	}
}
