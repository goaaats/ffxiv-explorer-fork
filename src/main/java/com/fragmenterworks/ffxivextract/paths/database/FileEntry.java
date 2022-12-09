package com.fragmenterworks.ffxivextract.paths.database;

public class FileEntry extends HashEntry {
	String text;
	FolderEntry parent;

	public FileEntry(int indexId, int fileHash, String text, FolderEntry parent) {
		this.indexId = indexId;
		this.hash = fileHash;
		this.text = text;
		this.parent = parent;
	}

	public String getFullPath() {
		return parent.text + '/' + text;
	}

	public FolderEntry getParent() {
		return parent;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FileEntry))
			return false;

		FileEntry other = (FileEntry) obj;
		return this.indexId == other.indexId && this.hash == other.hash && this.text.equals(other.text);
	}
}
