package ca.fraggergames.ffxivextract.models;

public class SqPack_DataSegment {

	private int offset;
	private int size;
	private byte[] sha1 = new byte[20];
	
	public SqPack_DataSegment(int offset, int size, byte[] sha1)
	{
		this.offset = offset;
		this.size = size;
		this.sha1 = sha1;
	}
	
	public int getOffset()
	{
		return offset;
	}
	
	public int getSize()
	{
		return size;
	}

	public byte[] getSha1()
	{
		return sha1;
	}
}
