package com.fragmenterworks.ffxivextract.paths.database;

import java.util.ArrayList;
import java.util.List;

public class FolderEntry extends HashEntry {
	String text;
	List<FileEntry> files;

	public FolderEntry(int indexId, int folderHash, String text) {
		this.indexId = indexId;
		this.hash = folderHash;
		this.text = text;
		files = new ArrayList<>();
	}

	public void addFile(FileEntry file) {
		files.add(file);
	}

	public List<FileEntry> getFiles() {
		return files;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FolderEntry))
			return false;

		FolderEntry other = (FolderEntry)obj;
		return other.indexId == indexId && other.hash == hash && other.text.equals(text);
	}
}
