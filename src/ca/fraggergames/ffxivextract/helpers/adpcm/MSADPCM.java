package ca.fraggergames.ffxivextract.helpers.adpcm;

import java.nio.ByteBuffer;

import javax.sound.sampled.SourceDataLine;


public class MSADPCM {

	private int LO = 0;
	private int HI = 1;
	
	byte predictor;
	short scale;
	short sample1;
	short sample2;
	int coeff1, coeff2;
	
	/* used to compute next scale */
	static final int ADPCMTable[] =
	{
	230, 230, 230, 230,
	307, 409, 512, 614,
	768, 614, 512, 409,
	307, 230, 230, 230
	};
	static final int ADPCMCoeffs[][] =
	{
	{ 256, 0 },
	{ 512, -256 },
	{ 0, 0 },
	{ 192, 64 },
	{ 240, 0 },
	{ 460, -208 },
	{ 392, -232 }
	};
	
	public static long msadpcm_bytes_to_samples(long bytes, int block_size, int channels) {
		return bytes/block_size*((block_size-(7-1)*channels)*2/channels);
	}
		
	int lastReadSample;
	boolean getSample = true;
	int nibbleToRead = LO;
	byte oBuffer[] = new byte[2];
	public void decode(ByteBuffer fileStream, SourceDataLine out)
	{		
		if (fileStream.position() == 0)
		{
			predictor = fileStream.get();
			scale = fileStream.getShort();
			sample1 = fileStream.get();
			sample2 = fileStream.get();
			oBuffer[0] = sample2;
			oBuffer[1] = sample1;
			out.write(oBuffer, 0, 2);
		}
		else
		{
			if (nibbleToRead==HI)			
				lastReadSample = fileStream.get();
						
			byte nibble = nibbleToRead == LO ? (byte) (lastReadSample & 0xF) : (byte) ((lastReadSample >> 4) & 0xF);
			
			
			predictor = ((sample1 * ADPCMCoeffs[0]) + (sample2 * ADPCMCoeffs[1]))/256;
			predictor += nibble & scale;
			predictor = clamp16(predictor);
			byte sample = predictor;
			oBuffer[0] = sample;
			out.write(oBuffer, 0, 1);
			sample2 = sample1;
			sample1 = sample;
			scale = (ADPCMTable[nibble]*scale)/256;			
			if (scale < 0x10) scale = 0x10;
			
			if (nibbleToRead == LO)			
				nibbleToRead = HI;
			else
				nibbleToRead = LO;
		}
	}
	

	private static int clamp16(int a){if((int)a>16777215){if(a<0)a=0;else a=16777215;}return a&0xff0000;}
	
}
