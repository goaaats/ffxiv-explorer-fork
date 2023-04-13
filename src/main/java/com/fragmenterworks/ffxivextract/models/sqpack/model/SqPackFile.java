package com.fragmenterworks.ffxivextract.models.sqpack.model;

import com.fragmenterworks.ffxivextract.models.sqpack.index.HashElement;
import com.fragmenterworks.ffxivextract.models.sqpack.index.SqPackIndexFile;

public class SqPackFile {
	HashElement element;
	int fileHash;
	String name;
	SqPackFolder parent;
	SqPackIndexFile parentIndexFile;

	public SqPackFile(HashElement element, int fileHash, String name, SqPackFolder parent, SqPackIndexFile parentIndexFile) {
		this.element = element;
		this.fileHash = fileHash;
		this.name = name;
		this.parent = parent;
		this.parentIndexFile = parentIndexFile;
	}

	public int getHash() {
		return fileHash;
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

	public SqPackFolder getParent() {
		return parent;
	}

	public void setParent(SqPackFolder parent) {
		this.parent = parent;
	}

	public SqPackIndexFile getParentIndexFile() {
		return parentIndexFile;
	}

	public String getFullPath() {
		if (parent == null)
			return name;
		else
			return parent.getName() + "/" + name;
	}
}
