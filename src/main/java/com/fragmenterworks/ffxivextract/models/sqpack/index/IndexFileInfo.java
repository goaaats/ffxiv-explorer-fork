package com.fragmenterworks.ffxivextract.models.sqpack.index;

import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;

public class IndexFileInfo {
	public int size;
	public int version;
	public int indexDataOffset;
	public int indexDataSize;
	public byte[] indexDataHash;
	public int dataFileCount;
	public int collisionOffset;
	public int collisionSize;
	public byte[] collisionDataHash;
	public int emptyBlockOffset;
	public int emptyBlockSize;
	public byte[] emptyBlockHash;
	public int directoryOffset;
	public int directorySize;
	public byte[] directoryHash;
	public int indexType;
	public byte[] unknownData;
	public byte[] fileInfoHash;

	public static IndexFileInfo read(EARandomAccessFile file) {
		IndexFileInfo info = new IndexFileInfo();

		try {
			info.size = file.readInt();
			info.version = file.readInt();
			info.indexDataOffset = file.readInt();
			info.indexDataSize = file.readInt();
			info.indexDataHash = new byte[20];
			file.read(info.indexDataHash);
			file.skipBytes(44);
			info.dataFileCount = file.readInt();
			info.collisionOffset = file.readInt();
			info.collisionSize = file.readInt();
			info.collisionDataHash = new byte[20];
			file.read(info.collisionDataHash);
			file.skipBytes(44);
			info.emptyBlockOffset = file.readInt();
			info.emptyBlockSize = file.readInt();
			info.emptyBlockHash = new byte[20];
			file.read(info.emptyBlockHash);
			file.skipBytes(44);
			info.directoryOffset = file.readInt();
			info.directorySize = file.readInt();
			info.directoryHash = new byte[20];
			file.read(info.directoryHash);
			file.skipBytes(44);
			info.indexType = file.readInt();
			info.unknownData = new byte[656];
			file.read(info.unknownData);
			info.fileInfoHash = new byte[20];
			file.read(info.fileInfoHash);
			file.skipBytes(44);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return info;
	}
}
