package ca.fraggergames.ffxivextract.helpers;

import java.nio.ByteBuffer;

public class HavokNative {

	static public native void startHavok();
	static public native void endHavok();
	static public native boolean loadSkeleton(String path);
	static public native boolean loadAnimation(String path);
	static public native int setAnimation(int i);
	static public native void stepAnimation(float deltaTime);
	static public native int getNumBones();
	static public native void getBones(ByteBuffer buffer);
	static public native void debugRenderBones();
	
	public static void initHavokNativ()
	{
		try{
			System.loadLibrary("havok");
		}catch (UnsatisfiedLinkError e)
		{
			e.printStackTrace();
		}
	}
	
	
}
