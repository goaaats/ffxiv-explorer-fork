package com.fragmenterworks.ffxivextract.models;

import java.nio.ByteBuffer;

public class BoundingBox {

	public final Vector4 pointA, pointB;
	
	public BoundingBox(ByteBuffer bb)
	{
		pointA = new Vector4(bb.getFloat(),bb.getFloat(),bb.getFloat(),bb.getFloat());
		pointB = new Vector4(bb.getFloat(),bb.getFloat(),bb.getFloat(),bb.getFloat());
	}
	
	@Override
	public String toString() {		
		return String.format("Point A: [%s] and Point B: [%s]", pointA.toString(), pointB.toString());
	}
}
