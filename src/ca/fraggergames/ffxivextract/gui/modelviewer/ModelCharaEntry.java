package ca.fraggergames.ffxivextract.gui.modelviewer;

public class ModelCharaEntry {
	
	final public int id;
	final public int model;
	final public int varient;
	final public int type;
	
	public ModelCharaEntry(int id, int model, int varient, int type)
	{
		this.id = id;
		this.model = model;
		this.varient = varient;
		this.type = type;
	}
}
