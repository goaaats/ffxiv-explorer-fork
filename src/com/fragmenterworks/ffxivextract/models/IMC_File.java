package com.fragmenterworks.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

public class IMC_File {

	private int numVariances;
	private int partMask;	
	private int first;
	
	HashMap<Integer, ImcPart> parts = new HashMap<Integer, ImcPart>();

	public IMC_File(String path) throws IOException{
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		fis.read(data);
		fis.close();
		loadIMC(data);
	}

	public IMC_File(byte[] data) throws IOException {
		loadIMC(data);
	}
	
	private void loadIMC(byte[] data)
	{
		ByteBuffer bb = ByteBuffer.wrap(data);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		numVariances = bb.getShort();
		partMask = bb.getShort();
		boolean gotFirst = false;
		
		//This is weird variants sitting here. SaintCoinach reads it based on the part mask.
		for (int i = 0; i < 8; i++)
		{
			int bit = (byte) (1 << i);
			if ((partMask & bit) == bit)
			{
				if (!gotFirst)
				{
					first = bit;
					gotFirst = true;
				}
				parts.put(i, new ImcPart(bit, new VarianceInfo(bb.getShort(), bb.getShort(), bb.getShort())));
			}
		}
		
		//Get the variances
		int remaining = numVariances;
		while (--remaining >= 0){
			for (ImcPart imcPart : parts.values())			
				imcPart.variants.add(new VarianceInfo(bb.getShort(), bb.getShort(), bb.getShort()));			
		}
	}
	
	public VarianceInfo getVarianceInfo(int i)
	{			
		if (i > numVariances || i == -1)
			return parts.get(0).variants.get(0);
		
		return parts.get(0).variants.get(i);
	}
	
	public ArrayList<VarianceInfo> getVariantsList(int key)
	{
		return parts.get(key).variants;
	}
	
	public int getNumVariances()
	{
		return numVariances;
	}
	
	public static class ImcPart{
		public final int bit;
		public final ArrayList<VarianceInfo> variants = new ArrayList<VarianceInfo>();
		
		public ImcPart(int bit, VarianceInfo variance)
		{
			this.bit = bit;
			this.variants.add(variance);
		}
	}
	
	public static class VarianceInfo{
		public final short materialNumber;
		public final short partVisibiltyMask;
		public final short effectNumber;		
		
		public VarianceInfo(short materialNumber, short partVisiMask, short effectNumber)
		{
			this.materialNumber = (short) (materialNumber == 0 ? 1 : materialNumber);
			this.partVisibiltyMask = partVisiMask;
			this.effectNumber = effectNumber;
		}
		
		@Override
		public String toString() {
			return String.format("Mat#: %d, Parts:0x%x, Eff#: %d", materialNumber, partVisibiltyMask, effectNumber);
		}
	}
}
