package ca.fraggergames.ffxivextract.gui.modelviewer;

public class ModelFurnitureEntry {
	
	final public int id;
	final public String name;
	final public int model;
	final public String type;	
	
	public ModelFurnitureEntry(int id, String name, int model, String furnitureTypeName)
	{
		this.id = id;
		this.name = name;
		this.model = model;
		this.type = furnitureTypeName;
	}
}
