package ca.fraggergames.ffxivextract.gui.modelviewer;

public class ModelItemEntry {
	
	final public String name;
	final public int id;
	final public int model;
	final public int varient;
	final public int type;
	
	public ModelItemEntry(String name, int id, int model, int varient, int type)
	{
		this.name = name;
		this.id = id;
		this.model = model;
		this.varient = varient;
		this.type = type;
	}
}
