package com.fragmenterworks.ffxivextract.helpers;

public class LuaDec {

	private LuaDec() {
	}
	
	public static LuaDec initLuaDec(){
		try{
			System.loadLibrary("luadec");
		}catch (UnsatisfiedLinkError e)
		{
			return null;
		}
		return new LuaDec();
	}

	public native String decompile(byte[] luaByteCode);

}
