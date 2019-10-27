package com.fragmenterworks.ffxivextract.helpers;

import java.nio.ByteBuffer;

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.helpers.Utils;

public class HavokNative {

	static public native void startHavok(); //Will initialize Havok. If Havok was already initialized, it will be shutdown first.
	static public native void endHavok(); //Shuts down Havok and cleans up
	static public native boolean loadSkeleton(ByteBuffer buffer, int size); //Loads a skeleton into Havok
	static public native boolean loadAnimation(ByteBuffer buffer, int size); //Loads a animation into Havok
	static public native int setAnimation(int i); //Sets the current animation
	static public native void setPlaybackSpeed(float speed); //Sets animation speed
	static public native void stepAnimation(float deltaTime); //Steps the animation
	static public native int getNumAnimationFrames(int i);
	static public native int getNumBones(); //Get's the number of bones in the skeleton
	static public native void getBones(ByteBuffer buffer); //Fills a buffer with all the bone transform matrices
	static public native boolean getBonesWithNames(ByteBuffer buffer, String[] boneNames, short[] boneIndices, int numIndices); //Fills a buffer with all the bone transform matrices based on a bone name list
	static public native void debugRenderBones(); //Steps the Visual Debugger
	
	public static void initHavokNativ()
	{
		if (!Constants.HAVOK_ENABLED)
			return;
		
		try{
			System.loadLibrary("havok");
		}catch (UnsatisfiedLinkError e)
		{
			Utils.getGlobalLogger().error(e);
		}
	}
	
}
	