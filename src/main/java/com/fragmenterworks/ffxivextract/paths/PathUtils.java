package com.fragmenterworks.ffxivextract.paths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

public class PathUtils {

	private static final String COMMON = "com";
	private static final String BGCOMMON = "bgc";
	private static final String BG = "bg/";
	private static final String CUT = "cut";
	private static final String CHARA = "cha";
	private static final String SHADER = "sha";
	private static final String UI = "ui/";
	private static final String SOUND = "sou";
	private static final String VFX = "vfx";
	private static final String UI_SCRIPT = "ui_";
	private static final String EXD = "exd";
	private static final String GAME_SCRIPT = "gam";
	private static final String MUSIC = "mus";
	private static final String SQPACK_TEST = "_sq";
	private static final String DEBUG = "_de";

	public static int getIndexId(String gamePath)
	{
		if (gamePath.startsWith(COMMON)) {
			return 0x000000;
		} else if (gamePath.startsWith(BGCOMMON)) {
			return 0x010000;
		} else if (gamePath.startsWith(BG)) {
			return GetBgSubCategoryId(gamePath) | (0x02 << 16);
		} else if (gamePath.startsWith(CUT)) {
			return GetNonBgSubCategoryId(gamePath, 4) | (0x03 << 16);
		} else if (gamePath.startsWith(CHARA)) {
			return 0x040000;
		} else if (gamePath.startsWith(SHADER)) {
			return 0x050000;
		} else if (gamePath.startsWith(UI)) {
			return 0x060000;
		} else if (gamePath.startsWith(SOUND)) {
			return 0x070000;
		} else if (gamePath.startsWith(VFX)) {
			return 0x080000;
		} else if (gamePath.startsWith(UI_SCRIPT)) {
			return 0x090000;
		} else if (gamePath.startsWith(EXD)) {
			return 0x0A0000;
		} else if (gamePath.startsWith(GAME_SCRIPT)) {
			return 0x0B0000;
		} else if (gamePath.startsWith(MUSIC)) {
			return GetNonBgSubCategoryId(gamePath, 6) | (0x0C << 16);
		} else if (gamePath.startsWith(SQPACK_TEST)) {
			return 0x120000;
		} else if (gamePath.startsWith(DEBUG)) {
			return 0x130000;
		}
		return -1;
	}

	private static int GetBgSubCategoryId(String gamePath)
	{
		int segmentIdIndex = 3;
		int expacId = 0;

		// Check if this is an ex* path
		if (gamePath.charAt(3) != 'e')
			return 0;

		// Check if our expac ID has one or two digits
		if (gamePath.charAt(6) == '/')
		{
			expacId = parseInt(gamePath, 5, 1) << 8;
			segmentIdIndex = 7;
		}
		else if (gamePath.charAt(7) == '/')
		{
			expacId = parseInt(gamePath, 5, 2) << 8;
			segmentIdIndex = 8;
		}
		else
		{
			expacId = 0;
		}

		// Parse the segment id for this bg path
		int segmentId = parseInt(gamePath, segmentIdIndex, 2);

		return expacId + segmentId;
	}

	private static int GetNonBgSubCategoryId(String gamePath, int firstDirLen)
	{
		if (gamePath.charAt(firstDirLen) != 'e')
			return 0;

		if (gamePath.charAt(firstDirLen + 3) == '/')
			return parseInt(gamePath, firstDirLen + 2, 1) << 8;

		if (gamePath.charAt(firstDirLen + 4) == '/')
			return parseInt(gamePath, firstDirLen + 2, 2) << 8;

		return 0;
	}

	public static int parseInt(String chars, int offset, int length) {
		int result = 0;
		for (int i = 0; i < length; i++) {
			result = result * 10 + (chars.charAt(offset + i) - '0');
		}
		return result;
	}

	public static int parseInt(char[] chars, int offset, int length) {
		int r = 0;
		for (int i = offset; i < offset + length; ++i) {
			r *= 10;
			r += chars[i] - '0';
		}
		return r;
	}

	public static CrcResult computeHashesWithLower(String fullPath) {
		String path = fullPath.toLowerCase();
		return computeHashes(path);
	}

	public static CrcResult computeHashes(String fullPath) {
		CrcResult result = new CrcResult();

		int slashIndex = fullPath.lastIndexOf('/');
		if (slashIndex == -1) {
			result.folderHash = -1;
			result.fileHash = -1;
			result.fullHash = -1;
			return result;
		}

		byte[] pathBytes = fullPath.getBytes();

		CRC32 crc = new CRC32();
		crc.update(pathBytes, 0, pathBytes.length);
		result.fullHash = ~((int) crc.getValue());
		crc.reset();

		crc.update(pathBytes, 0, slashIndex);
		result.folderHash = ~((int) crc.getValue());
		crc.reset();

		crc.update(pathBytes, slashIndex + 1, pathBytes.length - slashIndex - 1);
		result.fileHash = ~((int) crc.getValue());

		return result;
	}

	public static String getExpacString(int indexId) {
		switch ((indexId >> 8) & 0xFF) {
			case 1: return "ex1";
			case 2: return "ex2";
			case 3: return "ex3";
			case 4: return "ex4";
		}
		return "ffxiv";
	}
}
