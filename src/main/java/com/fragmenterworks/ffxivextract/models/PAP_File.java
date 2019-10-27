package com.fragmenterworks.ffxivextract.models;

import com.fragmenterworks.ffxivextract.helpers.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PAP_File extends Game_File {

	private int numAnimations;
	private byte havokData[];
	private String[] animationNames;
	private int[] animationIndex;
	
	public PAP_File(String path, ByteOrder endian) throws IOException{
		super(endian);
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		loadPAP(data);
	}

	public PAP_File(byte[] data, ByteOrder endian) throws IOException {
		super(endian);
		loadPAP(data);
	}

	private void loadPAP(byte[] data) throws IOException {
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(endian);

		int magic = bb.getInt();

		if (magic != 0x20706170) {
			Utils.getGlobalLogger().error("PAP magic was incorrect.");
			Utils.getGlobalLogger().debug("Magic was {}", String.format("0x%08X", magic));
			return;
		}

		bb.getShort();
		bb.getShort();
		numAnimations = bb.getShort();
		bb.getShort();
		bb.getShort();		
		bb.getInt();
		int havokPosition = bb.getInt();
		int footerPosition = bb.getInt();
		
		//ANIM NAME TABLE
		animationNames = new String[numAnimations];
		animationIndex = new int[numAnimations];
		int nameTableOffset = bb.position();
		for (int i = 0; i < numAnimations; i++)
		{
			bb.position(nameTableOffset + (0x28 * i));
			byte[] name = new byte[34];
			bb.get(name);
			animationNames[i] = new String(name).trim();
			animationIndex[i] = bb.getShort();
		}		
		
		//HAVOK FILE
		bb.position(nameTableOffset + (0x28 * numAnimations));
		havokData = new byte[footerPosition-havokPosition];
		bb.get(havokData);
		
		//FOOTER
		//TODO: Unknown
	}
	
	public int getNumAnimations()
	{
		return numAnimations;
	}
	
	public String[] getAnimationNames()
	{
		return animationNames;
	}
	
	public String getAnimationName(int index)
	{
		if (index >= numAnimations)
			return null;					
		return animationNames[index];
	}
	
	public int getAnimationIndex(int index)
	{
		if (index >= numAnimations)
			return -1;					
		return animationIndex[index];
	}
	
	public byte[] getHavokData()
	{
		return havokData;
	}
	
}
