package com.fragmenterworks.ffxivextract.helpers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MSADPCM_Decode {

	/* These are for MS-ADPCM */
	/*
	 * AdaptationTable[], AdaptCoeff1[], and AdaptCoeff2[] are from libsndfile
	 */
	private static final int[] AdaptationTable = { 230, 230, 230, 230, 307,
			409, 512, 614, 768, 614, 512, 409, 307, 230, 230, 230 };
	private static final int[] AdaptCoeff1 = { 256, 512, 0, 192, 240, 460, 392 };
	private static final int[] AdaptCoeff2 = { 0, -256, 0, 64, 0, -208, -232 };

	public static int getBufferSize(long inputBufferLength, int channels, int blockAlign, int bitsPerSample) {
		if (inputBufferLength > Integer.MAX_VALUE)
			throw new NumberFormatException("Number too large: "
					+ inputBufferLength);
		
			int blockHeaderOverhead = (7 * channels);
			int numOfBlocks = ((int) inputBufferLength / blockAlign);
			int rawSize = (channels * 16 / bitsPerSample)
					* (int) inputBufferLength;
			return rawSize - (numOfBlocks * blockHeaderOverhead);
		
	}

	public static byte[] decode(byte[] inputBuffer, byte[] outputBuffer,
			int len, int channels, int blockAlign) {		
			decodeADPCM(inputBuffer, outputBuffer, len, channels, blockAlign);
			return outputBuffer;
	}	

	/*
	 * MS ADPCM part starts here. It (mostly) works on the WAV file (some
	 * hisses, but normal sound otherwise), but I was unable to test over the
	 * network, becuse the "RDP Clip monitor" crashes on the server, if ADPCM is
	 * forced. Go figure :(
	 */
	private static void decodeADPCM(byte inputBuffer[], byte outputBuffer[],
			int len, int channels, int blockAlign) {
		int blocksInSample = len / blockAlign;
		int dstIndex = 0;
		for (int i = 0; i < blocksInSample; i++) {
			dstIndex = adpcmDecodeFrame(inputBuffer,
					i * blockAlign, blockAlign,
					outputBuffer, dstIndex, channels,  blockAlign);
		}
	}

	private static int storeShort(byte[] dst, short data, int offset) {
		dst[offset] = (byte) (data & 0xFF);
		dst[offset + 1] = (byte) ((data >> 8) & 0xFF);
		return offset + 2;
	}

	private static short adpcmMsExpandNibble(ADPCMChannelStatus c, byte nibble) {
		int prePredictor;
		prePredictor = ((c.sample1 * c.coeff1) + (c.sample2 * c.coeff2)) / 256;
		prePredictor += ((nibble & 0x08) != 0 ? (nibble - 0x10) : (nibble))
				* c.idelta;
		short predictor = (short) (prePredictor & 0xFFFF);
		c.sample2 = c.sample1;
		c.sample1 = predictor;
		c.idelta = (AdaptationTable[(int) nibble] * c.idelta) >> 8;
		if (c.idelta < 16) {
			c.idelta = 16;
		}
		return predictor;
	}

	/*
	 * Based on: ADPCM codecs Copyright (c) 2001-2003 The ffmpeg Project
	 * 
	 * This library is free software; you can redistribute it and/or modify it
	 * under the terms of the GNU Lesser General Public License as published by
	 * the Free Software Foundation; either version 2 of the License, or (at
	 * your option) any later version.
	 * 
	 * This library is distributed in the hope that it will be useful, but
	 * WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
	 * General Public License for more details.
	 * 
	 * You should have received a copy of the GNU Lesser General Public License
	 * along with this library; if not, write to the Free Software Foundation,
	 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
	 * 
	 * --- end license ---
	 * 
	 * Adopted for Java by Miha Vitorovic
	 */
	public static int adpcmDecodeFrame(byte[] srcBuffer,
			int srcIndex, int inputBufferSize, byte[] dstBuffer, int dstIndex, int channels, int blockAlign) {
		int blockPredictor[] = new int[2];
		boolean st; /* stereo */
		ADPCMChannelStatus status0 = new ADPCMChannelStatus();
		ADPCMChannelStatus status1 = new ADPCMChannelStatus();
		if (inputBufferSize == 0)
			return dstIndex;
		st = channels == 2;
		if (blockAlign != 0 && inputBufferSize > blockAlign)
			inputBufferSize = blockAlign;
		int n = inputBufferSize - 7 * channels;
		if (n < 0)
			return dstIndex;
		blockPredictor[0] = clip(srcBuffer[srcIndex++], 0, 7);
		blockPredictor[1] = 0;
		if (st)
			blockPredictor[1] = clip(srcBuffer[srcIndex++], 0, 7);
		status0.idelta = (((int) srcBuffer[srcIndex] & 0xFF) | (((int) srcBuffer[srcIndex + 1] << 8) & 0xFF00));
		srcIndex += 2;
		if (st) {
			status1.idelta = (((int) srcBuffer[srcIndex] & 0xFF) | (((int) srcBuffer[srcIndex + 1] << 8) & 0xFF00));
			srcIndex += 2;
		}
		status0.coeff1 = AdaptCoeff1[blockPredictor[0]];
		status0.coeff2 = AdaptCoeff2[blockPredictor[0]];
		status1.coeff1 = AdaptCoeff1[blockPredictor[1]];
		status1.coeff2 = AdaptCoeff2[blockPredictor[1]];
		status0.sample1 = (short) (((int) srcBuffer[srcIndex] & 0xFF) | (((int) srcBuffer[srcIndex + 1] << 8) & 0xFF00));
		srcIndex += 2;
		if (st)
			status1.sample1 = (short) (((int) srcBuffer[srcIndex] & 0xFF) | (((int) srcBuffer[srcIndex + 1] << 8) & 0xFF00));
		if (st)
			srcIndex += 2;
		status0.sample2 = (short) (((int) srcBuffer[srcIndex] & 0xFF) | (((int) srcBuffer[srcIndex + 1] << 8) & 0xFF00));
		srcIndex += 2;
		if (st)
			status1.sample2 = (short) ((srcBuffer[(int) srcIndex] & 0xFF) | (((int) srcBuffer[srcIndex + 1] << 8) & 0xFF00));
		if (st)
			srcIndex += 2;
		dstIndex = storeShort(dstBuffer, status0.sample1, dstIndex);
		if (st)
			dstIndex = storeShort(dstBuffer, status1.sample1, dstIndex);
		dstIndex = storeShort(dstBuffer, status0.sample2, dstIndex);
		if (st)
			dstIndex = storeShort(dstBuffer, status1.sample2, dstIndex);
		while (n > 0) {
			dstIndex = storeShort(
					dstBuffer,
					adpcmMsExpandNibble(status0,
							(byte) (((int) srcBuffer[srcIndex] >> 4) & 0x0F)),
					dstIndex);
			dstIndex = storeShort(
					dstBuffer,
					adpcmMsExpandNibble((st ? status1 : status0),
							(byte) ((int) srcBuffer[srcIndex] & 0x0F)),
					dstIndex);
			srcIndex++;
			n--;
		}
		return dstIndex;
	}

	private static int clip(int a, int amin, int amax) {
		if (a < amin)
			return amin;
		else if (a > amax)
			return amax;
		else
			return a;
	}
	
    public static class ADPCMChannelStatus {
		public int predictor = 0;
		public short step_index = 0;
		public int step = 0;
		/* for encoding */
		public int prev_sample;
		/* MS version */
		public short sample1;
		public short sample2;
		public int coeff1;
		public int coeff2;
		public int idelta;
		}
	
  

}

