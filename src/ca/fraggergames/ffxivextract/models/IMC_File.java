package ca.fraggergames.ffxivextract.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IMC_File {

	private int numVariances;
	private int numParts = 0;
	private int partMask;	
	
	VarianceInfo[] varianceInfoList;

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
		
		varianceInfoList = new VarianceInfo[numVariances];
		
		//This is weird variants sitting here. SaintCoinach reads it based on the part mask.
		for (int i = 0; i < 8; i++)
		{
			int bit = (byte) (1 << i);
			if ((partMask & bit) == bit)
			{
				bb.getShort();
				bb.getShort();
				bb.getShort();
				numParts++;
			}
		}
		
		//Get the variances
		for (int p = 0; p < numParts; p++)
		{
			for (int i = 0; i < numVariances; i++)
			{
				short materialNumber = bb.getShort();
				short partMask = bb.getShort();
				short effectNumber = bb.getShort();
				if (i == p)
					varianceInfoList[i] = new VarianceInfo(materialNumber, partMask, effectNumber);
			}
		}
	}
	
	public VarianceInfo getVarianceInfo(int i)
	{			
		if (i >= numVariances || i == -1)
			return varianceInfoList[0];
		
		return varianceInfoList[i];
	}
	
	public int getNumVariances()
	{
		return numVariances;
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
	}
}
