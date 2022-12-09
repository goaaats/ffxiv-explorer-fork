package com.fragmenterworks.ffxivextract.models.sqpack.index;

public class HashElement {
	protected boolean isBigEndian;
	public int data;

	public boolean isSynonym() {
		if (isBigEndian) {
			return (data & (0b1 << 31)) == (0b1 << 31);
		}
		return (data & 0b1) == 0b1;
	}

	public int getDatafileId() {
		if (isBigEndian) {
			return 0;
		}
		return (data & 0b1110) >> 1;
	}

	public long getOffset() {
		if (isBigEndian) {
			return (data & ~0x80_00_00_00) * 128L;
		}
		return (data & ~0xF) * 8L;
	}

	// TODO may or may not be used, this is for index1/2 correlation
	public long getFileIdentifier() {
		if (isBigEndian) {
			return (data & ~0x80000000);
		}
		return (data & ~0b1);
	}
}
