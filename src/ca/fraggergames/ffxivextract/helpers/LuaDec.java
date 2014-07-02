package ca.fraggergames.ffxivextract.helpers;

public class LuaDec {

	static {
		System.loadLibrary("luadec");
	}

	public native static String decompile(byte[] luaByteCode);

}
