package com.fragmenterworks.ffxivextract.models.sqpack;

import com.fragmenterworks.ffxivextract.helpers.EARandomAccessFile;
import com.fragmenterworks.ffxivextract.helpers.Utils;

public class SqPackVersionInfo {
	public byte platformId;
	public int fileSize;
	public int version;
	public int type;
	public int date;
	public int time;
	public int regionId;
	public int languageId;
	public byte[] unknownData;
	public byte[] versionInfoHash;

	public static SqPackVersionInfo read(EARandomAccessFile file) {
		SqPackVersionInfo info = new SqPackVersionInfo();

		try {
			byte[] tmp = new byte[8];
			file.read(tmp);
			if (tmp[0] != 'S' || tmp[1] != 'q' || tmp[2] != 'P' || tmp[3] != 'a' || tmp[4] != 'c' || tmp[5] != 'k')
				throw new Exception("Invalid SqPack header");
			info.platformId = file.readByte();
			file.skipBytes(3);
			info.fileSize = file.readInt();
			info.version = file.readInt();
			info.type = file.readInt();
			info.date = file.readInt();
			info.time = file.readInt();
			info.regionId = file.readInt();
			info.languageId = file.readInt();
			info.unknownData = new byte[920];
			file.read(info.unknownData);
			info.versionInfoHash = new byte[20];
			file.read(info.versionInfoHash);
			file.skipBytes(44);
		} catch (Exception e) {
			Utils.getGlobalLogger().error("", e);
		}

		return info;
	}
}
