package ca.fraggergames.ffxivextract.models;

import java.nio.ByteBuffer;

public class ParameterInfo
{
	final public int id;
	final public int stringOffset;
	final public int stringSize;
	final public int registerIndex;
	final public int registerCount;
	
	public String parameterName;
	
	public ParameterInfo(ByteBuffer bb){

		id = bb.getInt();
		stringOffset = bb.getInt();
		stringSize = bb.getInt();
		registerIndex = bb.getShort();
		registerCount = bb.getShort();
		
	}
}
