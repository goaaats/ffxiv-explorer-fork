package com.fragmenterworks.ffxivextract.helpers;

import javax.media.opengl.GL3;
import javax.media.opengl.GL3bc;

public class GLHelper2 {

	public static final int D3DDECLTYPE_FLOAT1     = 0;
	public static final int D3DDECLTYPE_FLOAT2     = 1;
	public static final int D3DDECLTYPE_FLOAT3     = 2;
	public static final int D3DDECLTYPE_FLOAT4     = 3;
	public static final int D3DDECLTYPE_D3DCOLOR   = 4;
	public static final int D3DDECLTYPE_UBYTE4     = 5;
	public static final int D3DDECLTYPE_SHORT2     = 6;
	public static final int D3DDECLTYPE_SHORT4     = 7;
	public static final int D3DDECLTYPE_UBYTE4N    = 8;
	public static final int D3DDECLTYPE_SHORT2N    = 9;
	public static final int D3DDECLTYPE_SHORT4N    = 10;
	public static final int D3DDECLTYPE_USHORT2N   = 11;
	public static final int D3DDECLTYPE_USHORT4N   = 12;
	public static final int D3DDECLTYPE_UDEC3      = 13;
	public static final int D3DDECLTYPE_DEC3N      = 14;
	public static final int D3DDECLTYPE_FLOAT16_2  = 15;
	public static final int D3DDECLTYPE_FLOAT16_4  = 16;
	public static final int D3DDECLTYPE_UNUSED     = 17;
	
	public static int getComponents(int datatype) {
		switch (datatype)
		{
		case D3DDECLTYPE_FLOAT1:
			return 1;
		case D3DDECLTYPE_FLOAT2:
		case D3DDECLTYPE_SHORT2:
		case D3DDECLTYPE_SHORT2N:
		case D3DDECLTYPE_USHORT2N:
		case D3DDECLTYPE_FLOAT16_2:
			return 2;
		case D3DDECLTYPE_FLOAT3:
			return 3;
		case D3DDECLTYPE_FLOAT4:
		case D3DDECLTYPE_SHORT4:
		case D3DDECLTYPE_SHORT4N:
		case D3DDECLTYPE_USHORT4N:
		case D3DDECLTYPE_UBYTE4:
		case D3DDECLTYPE_UBYTE4N:
		case D3DDECLTYPE_FLOAT16_4:
		case D3DDECLTYPE_D3DCOLOR:
		case D3DDECLTYPE_UDEC3:
		case D3DDECLTYPE_DEC3N:
			return 4;
		
		default: return -1;
		}
	}

	public static int getDatatype(int datatype) {
		switch (datatype)
		{
		case D3DDECLTYPE_FLOAT1:
		case D3DDECLTYPE_FLOAT2:
		case D3DDECLTYPE_FLOAT3:
		case D3DDECLTYPE_FLOAT4:			
			return GL3.GL_FLOAT;		
		case D3DDECLTYPE_D3DCOLOR:
		case D3DDECLTYPE_UBYTE4:
		case D3DDECLTYPE_UBYTE4N:
			return GL3.GL_UNSIGNED_BYTE;
		case D3DDECLTYPE_USHORT2N:
		case D3DDECLTYPE_USHORT4N:
			return GL3.GL_UNSIGNED_SHORT;
		case D3DDECLTYPE_SHORT2:
		case D3DDECLTYPE_SHORT4:
		case D3DDECLTYPE_SHORT2N:
		case D3DDECLTYPE_SHORT4N:
			return GL3.GL_SHORT;
		case D3DDECLTYPE_FLOAT16_2:
		case D3DDECLTYPE_FLOAT16_4:
			return GL3.GL_HALF_FLOAT;
		case D3DDECLTYPE_UDEC3:
			return GL3.GL_UNSIGNED_INT_2_10_10_10_REV;
		case D3DDECLTYPE_DEC3N:
			return GL3.GL_INT_2_10_10_10_REV;
		default: return -1;
		}
	}

	public static boolean isNormalized(int datatype)
	{
		switch (datatype)
		{
		case D3DDECLTYPE_UBYTE4N:
		case D3DDECLTYPE_USHORT2N:
		case D3DDECLTYPE_USHORT4N:
		case D3DDECLTYPE_SHORT2N:
		case D3DDECLTYPE_SHORT4N:
		case D3DDECLTYPE_DEC3N:
			return true;
		default:
			return false;
		}
	}
	
}
