package com.fragmenterworks.ffxivextract.helpers;

import com.jogamp.opengl.GL3;

public class GLHelper {

	public static int getComponents(int datatype) {
		switch (datatype)
		{
		case 2: //FLOAT3 
			return 3;
		case 5: //UBYTE4
		case 8: //UBYTE4N
			return 4;
		case 13://HALF FLOAT 2
			return 2;
		case 14://HALF FLOAT 3
			return 3;
		default: return -1;
		}
	}

	public static int getDatatype(int datatype) {
		switch (datatype)
		{
		case 2: 
			return GL3.GL_FLOAT;
		case 5: 
		case 8:
			return GL3.GL_UNSIGNED_BYTE;
		case 13:
		case 14:
			return GL3.GL_HALF_FLOAT;
		default: return -1;
		}
	}

	public static boolean isNormalized(int datatype) {		
		return false;
	}

	
	
}
