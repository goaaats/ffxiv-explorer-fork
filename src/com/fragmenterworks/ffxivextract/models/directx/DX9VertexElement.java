package com.fragmenterworks.ffxivextract.models.directx;

public class DX9VertexElement {

	final public int stream;
	final public int offset;
	final public int datatype;
	final public int usage;
	
	public DX9VertexElement(int stream, int offset, int datatype, int usage)
	{
		this.stream = stream;
		this.offset = offset;
		this.datatype = datatype;
		this.usage = usage;
	}
	
}
