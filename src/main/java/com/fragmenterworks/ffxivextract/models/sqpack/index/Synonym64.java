package com.fragmenterworks.ffxivextract.models.sqpack.index;

import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;

public class Synonym64 extends SynonymElement {

	public long hash;

	public static Synonym64 read(EARandomAccessFile file) {
		Synonym64 element = new Synonym64();
		element.isBigEndian = file.isBigEndian();

		try {
			element.hash = file.readLong();
			element.unknown = file.readInt();
			element.data = file.readInt();
			element.index = file.readInt();
			byte[] tmp = new byte[240];
			file.read(tmp);
			int len = 0;
			for (; tmp[len] != 0; len++);
			element.path = new String(tmp, 0, len);
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
