package com.fragmenterworks.ffxivextract.models.sqpack.model;

import com.fragmenterworks.ffxivextract.models.sqpack.index.HashElement;

import java.util.ArrayList;
import java.util.List;

public class SqPackFolder {
	HashElement element;
	String name;
	int folderHash;
	List<SqPackFile> files;

	public SqPackFolder(HashElement element, int folderHash, String name) {
		this.element = element;
		this.folderHash = folderHash;
		this.name = name;
		files = new ArrayList<>();
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
