package com.fragmenterworks.ffxivextract.gui.modelviewer;

public class ModelCharaEntry {
	
	final public int id;
	final public int model;
	final public int varient;
	final public int type;
	final public int index;
	
	public ModelCharaEntry(int index, int id, int model, int varient, int type)
	{
		this.index = index;
		this.id = id;
		this.model = model;
		this.varient = varient;
		this.type = type;
	}
}
