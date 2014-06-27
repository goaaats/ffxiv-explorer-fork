package ca.fraggergames.ffxivextract.helpers;

public class LuaDec {

	static {
		System.loadLibrary("luadec");
	}

	public native static void decompile(byte[] luaByteCode, int isDissassemble);

}
