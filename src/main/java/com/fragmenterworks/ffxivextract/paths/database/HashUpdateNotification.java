package com.fragmenterworks.ffxivextract.paths.database;

public class HashUpdateNotification {
	public int indexId;
	public int fullHash;
	public int folderHash;
	public int fileHash;
	public String folder;
	public String fileName;

	public HashUpdateNotification(int indexId, int fullHash, int folderHash, int fileHash, String folder, String fileName) {
		this.indexId = indexId;
		this.fullHash = fullHash;
		this.folderHash = folderHash;
		this.fileHash = fileHash;
		this.folder = folder;
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return "HashUpdateNotification{" +
				"indexId=" + indexId +
				", fullHash=" + fullHash +
				", folderHash=" + folderHash +
				", fileHash=" + fileHash +
				", folder='" + folder + '\'' +
				", fileName='" + fileName + '\'' +
				'}';
	}
}
