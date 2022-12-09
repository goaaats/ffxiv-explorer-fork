package com.fragmenterworks.ffxivextract.models.sqpack.index;

import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;

public class HashElement32 extends HashElement {
	public int hash;

	public static HashElement32 read(EARandomAccessFile file) {
		HashElement32 element = new HashElement32();
		element.isBigEndian = file.isBigEndian();

		try {
			element.hash = file.readInt();
			element.data = file.readInt();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return element;
	}
}
