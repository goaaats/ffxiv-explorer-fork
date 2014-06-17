package ca.fraggergames.ffxivextract.models;

public class SqPack_File {
	public int id;
	public long dataoffset;
	
	protected SqPack_File(int id, long offset)
	{
		this.id = id;
		this.dataoffset = offset;
	}
	
	public int getId()
	{
		return id;		
	}
	
	public long getOffset()
	{
		return dataoffset;
	}
}
