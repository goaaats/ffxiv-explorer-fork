package com.fragmenterworks.ffxivextract.models.sqpack.model;

import com.fragmenterworks.ffxivextract.models.sqpack.index.HashElement;
import com.fragmenterworks.ffxivextract.models.sqpack.index.SqPackIndexFile;

import java.util.ArrayList;
import java.util.List;

public class SqPackFolder {
	HashElement element;
	String name;
	int folderHash;
	List<SqPackFile> files;
	SqPackIndexFile parentIndexFile;

	public SqPackFolder(HashElement element, int folderHash, String name, SqPackIndexFile parentIndexFile) {
		this.element = element;
		this.folderHash = folderHash;
		this.name = name;
		this.files = new ArrayList<>();
		this.parentIndexFile = parentIndexFile;
	}

	public int getHash() {
		return folderHash;
	}

	public void setFiles(List<SqPackFile> files) {
		this.files = files;
	}

	public List<SqPackFile> getFiles() {
		return files;
	}

	public String getName() {
		return name;
	}

	public void setName(String s) {
		name = s;
	}

	public HashElement getElement() {
		return element;
	}

	public String toString() {
		return name;
	}
}
