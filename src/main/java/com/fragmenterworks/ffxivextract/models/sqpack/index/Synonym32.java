package com.fragmenterworks.ffxivextract.models.sqpack.index;

import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;

public class Synonym32 extends SynonymElement {

	public int hash;

	public static Synonym32 read(EARandomAccessFile file) {
		Synonym32 element = new Synonym32();
		element.isBigEndian = file.isBigEndian();

		try {
			element.hash = file.readInt();
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
}
