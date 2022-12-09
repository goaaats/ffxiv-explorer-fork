package com.fragmenterworks.ffxivextract.paths.database;

public class FullPathEntry extends HashEntry {
	FolderEntry folder;
	FileEntry file;

	public FullPathEntry(int indexId, int fullHash, FolderEntry folder, FileEntry file) {
		this.indexId = indexId;
		this.hash = fullHash;
		this.folder = folder;
		this.file = file;
	}

	public String getFullPath() {
		return folder.text + '/' + file.text;
	}

	public FolderEntry getFolderEntry() {
		return folder;
	}

	public FileEntry getFileEntry() {
		return file;
	}

	public String getFolderName() {
		return folder.text;
	}

	public String getFileName() {
		return file.text;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FullPathEntry))
			return false;

		FullPathEntry other = (FullPathEntry)obj;
		return other.indexId == indexId && other.hash == hash && other.folder.equals(folder) && other.file.equals(file);
	}
}
