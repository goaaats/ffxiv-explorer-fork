package com.fragmenterworks.ffxivextract.models;

public class Vector4 {

	public float x, y, z, w;
	
	public Vector4(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	@Override
	public String toString() {
		return String.format("%f, %f, %f, %f", x, y ,z, w);
	}
	
}
