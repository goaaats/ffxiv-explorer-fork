package com.fragmenterworks.ffxivextract.models.sqpack.index;

import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;

public class FolderInfo {
	public int folderHash;
	public int offset;
	public int size;
	public int unknown;

	public static FolderInfo read(EARandomAccessFile file) {
		FolderInfo folder = new FolderInfo();

		try {
			folder.folderHash = file.readInt();
			folder.offset = file.readInt();
			folder.size = file.readInt();
			folder.unknown = file.readInt();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return folder;
	}
}
