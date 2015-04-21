package ca.fraggergames.ffxivextract.helpers;

public class HavokNative {

	static public native void startHavok();
	static public native void endHavok();
	static public native boolean loadSkeleton(String path);
	static public native boolean loadAnimation(String path);
	static public native int setAnimation(int i);
	static public native void stepAnimation(float deltaTime);
	static public native void getBones();
	
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
