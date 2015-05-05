package ca.fraggergames.ffxivextract.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.gui.components.Loading_Dialog;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;

import com.jcraft.jzlib.Inflater;
import com.jcraft.jzlib.JZlib;

public class SqPack_DatFile {

	public final static int TYPE_TEXTURE = 4;
	public final static int TYPE_MODEL = 3;
	public final static int TYPE_BINARY = 2;
	
	private LERandomAccessFile currentFilePointer;

	protected SqPack_DatFile(String path) throws FileNotFoundException {
		currentFilePointer = new LERandomAccessFile(path, "r");
	}
	
	@SuppressWarnings("unused")
	protected byte[] extractFile(long fileOffset, Loading_Dialog loadingDialog) throws IOException {
		currentFilePointer.seek(fileOffset);
		int headerLength = currentFilePointer.readInt();
		int contentType = currentFilePointer.readInt();
		int fileSize = currentFilePointer.readInt();
		currentFilePointer.readInt(); // UNKNOWN
		int blockBufferSize = currentFilePointer.readInt() * 0x80;
		int blockCount = currentFilePointer.readInt();
		
		byte extraHeader[] = null;
		int extraHeaderSize = 0;

		if (Constants.DEBUG){
			System.out.println("================================");
			System.out.println(String.format("File @ %08x", fileOffset));
			System.out.println("================================");
			System.out.println("Header Length: " + headerLength);
			System.out.println("Content Type: " + contentType);
			System.out.println("File Size: " + fileSize);
			System.out.println("Block Buffer Size: " + blockBufferSize);
		}			
		
		Data_Block[][] dataBlocks = null;

		if (Constants.DEBUG){
			System.out.println("================================");
			System.out.println("Block Data for this file");
			System.out.println("================================");			
		}
		
		//How to get the blocks
		switch (contentType)
		{
		case TYPE_TEXTURE:
			
			TextureBlocks blocks[] = new TextureBlocks[blockCount];
			
			dataBlocks = new Data_Block[blockCount][];
			
			for (int i = 0; i < blockCount; i++) {
				int frameStartOffset = currentFilePointer.readInt();
				int frameSize = currentFilePointer.readInt();
				int wut1 = currentFilePointer.readInt();
				int blockTableOffset = currentFilePointer.readInt();
				int numSubBlocks = currentFilePointer.readInt();
				blocks[i] = new TextureBlocks(frameStartOffset, frameSize, blockTableOffset, numSubBlocks);
				dataBlocks[i] = new Data_Block[numSubBlocks];
			}
			
			for (int i = 0; i < blockCount; i++)
			{
				TextureBlocks block = blocks[i];
				dataBlocks[i][0] = new Data_Block(block.offset);
				int runningTotal = block.offset;
				for (int j = 1; j < block.subblockSize; j++)
				{					
					runningTotal += currentFilePointer.readShort();
					dataBlocks[i][j] = new Data_Block(runningTotal);
				}
				currentFilePointer.readShort();
			}
			
			extraHeaderSize = blocks[0].offset;
			extraHeader = new byte[extraHeaderSize];
			currentFilePointer.seek(fileOffset + headerLength);
			currentFilePointer.read(extraHeader, 0, extraHeaderSize);
			
			break;
		case TYPE_MODEL:
			
			ContentType3Container container = new ContentType3Container();								
			
			for (int i = 0; i < 11; i++)
				container.chunkDecompressedSizes[i] = currentFilePointer.readInt();
			for (int i = 0; i < 11; i++)
				container.chunkSizes[i] = currentFilePointer.readInt();
			for (int i = 0; i < 11; i++)
				container.chunkOffsets[i] = currentFilePointer.readInt();
			for (int i = 0; i < 11; i++)
				container.chunkStartBlockIndex[i] = currentFilePointer.readShort();
			int numBlocks = 0;
			for (int i = 0; i < 11; i++)
			{
				container.chunkNumBlocks[i] = currentFilePointer.readShort();
				numBlocks+= container.chunkNumBlocks[i];
			}
			
			container.blockSizes = new short[numBlocks];
			
			//Skip, unknown
			container.numMeshes = currentFilePointer.readShort();
			container.numMaterials = currentFilePointer.readShort();
			short y1 = currentFilePointer.readShort();
			short y2 = currentFilePointer.readShort();
			
			for (int i = 0; i < numBlocks; i++)
				container.blockSizes[i] = currentFilePointer.readShort();
			
			//int CHOSEN_CHUNK = 2; 

			int pos = 0x44;
			byte[] mdlData = new byte[fileSize];
			ByteBuffer bb = ByteBuffer.wrap(mdlData);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putShort(container.numMeshes);
			bb.putShort(container.numMaterials);
			currentFilePointer.seek(fileOffset + headerLength + container.chunkOffsets[0]);			
			for (int i = 0; i < container.blockSizes.length; i++)
			{					
				int lastPos = (int) currentFilePointer.getFilePointer();
				
				// Block Header
				int blockHeaderLength2 = currentFilePointer.readInt();
				currentFilePointer.readInt(); // NULL
				int compressedBlockSize2 = currentFilePointer.readInt(); 
				int decompressedBlockSize2 = currentFilePointer.readInt();
					
				byte[] decompressedBlock2 = null;			
				if (compressedBlockSize2 == 32000 || decompressedBlockSize2 == 1) //Not actually compressed, just read decompressed size
				{
					decompressedBlock2 = new byte[decompressedBlockSize2];
					currentFilePointer.readFully(decompressedBlock2);
				}
				else //Gotta decompress
					decompressedBlock2 = decompressBlock(compressedBlockSize2, decompressedBlockSize2);
				
				System.arraycopy(decompressedBlock2, 0, mdlData, pos, decompressedBlockSize2);
				pos+=decompressedBlockSize2;
				
				currentFilePointer.seek(lastPos + container.blockSizes[i]);
			}						
			/*
			pos = container.chunkDecompressedSizes[0];
			currentFilePointer.seek(fileOffset + headerLength + container.chunkOffsets[1]);			
			for (int i = 0; i < container.chunkNumBlocks[1]; i++)
			{					
				int lastPos = (int) currentFilePointer.getFilePointer();
				
				// Block Header
				int blockHeaderLength2 = currentFilePointer.readInt();
				currentFilePointer.readInt(); // NULL
				int compressedBlockSize2 = currentFilePointer.readInt(); 
				int decompressedBlockSize2 = currentFilePointer.readInt();
					
				byte[] decompressedBlock2 = null;			
				if (compressedBlockSize2 == 32000 || decompressedBlockSize2 == 1) //Not actually compressed, just read decompressed size
				{
					decompressedBlock2 = new byte[decompressedBlockSize2];
					currentFilePointer.readFully(decompressedBlock2);
				}
				else //Gotta decompress
					decompressedBlock2 = decompressBlock(compressedBlockSize2, decompressedBlockSize2);
				
				System.arraycopy(decompressedBlock2, 0, mdlData, pos, decompressedBlockSize2);
				pos+=decompressedBlockSize2;
				
				currentFilePointer.seek(lastPos + container.blockSizes[container.chunkStartBlockIndex[1]+i]);
			}			
			
			pos = container.chunkDecompressedSizes[1]+container.chunkDecompressedSizes[0];
			currentFilePointer.seek(fileOffset + headerLength + container.chunkOffsets[2]);			
			for (int i = 0; i < container.chunkNumBlocks[2]; i++)
			{					
				int lastPos = (int) currentFilePointer.getFilePointer();
				
				// Block Header
				int blockHeaderLength2 = currentFilePointer.readInt();
				currentFilePointer.readInt(); // NULL
				int compressedBlockSize2 = currentFilePointer.readInt(); 
				int decompressedBlockSize2 = currentFilePointer.readInt();
					
				byte[] decompressedBlock2 = null;			
				if (compressedBlockSize2 == 32000 || decompressedBlockSize2 == 1) //Not actually compressed, just read decompressed size
				{
					decompressedBlock2 = new byte[decompressedBlockSize2];
					currentFilePointer.readFully(decompressedBlock2);
				}
				else //Gotta decompress
					decompressedBlock2 = decompressBlock(compressedBlockSize2, decompressedBlockSize2);
				
				System.arraycopy(decompressedBlock2, 0, mdlData, pos, decompressedBlockSize2);
				pos+=decompressedBlockSize2;
				
				currentFilePointer.seek(lastPos + container.blockSizes[container.chunkStartBlockIndex[2]+i]);
			}
			
			
			System.out.println(container.numChunk1Entries);
			/*
			System.out.println(String.format("0x%x",y1));
			System.out.println(String.format("0x%x",y2));
			System.out.println("--------");
			*/
			
			return mdlData;
		case TYPE_BINARY: 
			dataBlocks = new Data_Block[1][blockCount];
			
			// Read in Block Info Header
			for (int i = 0; i < blockCount; i++) {
				int offset = currentFilePointer.readInt();
				int paddingAndSize = currentFilePointer.readInt();
				int padding = paddingAndSize & 0xFFFF;
				int decompressedBlockSize = paddingAndSize >> 16 & 0xFFFF;

				dataBlocks[0][i] = new Data_Block(offset, padding, decompressedBlockSize);
			
				if (Constants.DEBUG){
					System.out.println("Block #" + i);
					System.out.println("Offset: " + String.format("%X", offset));
					System.out.println("Padding: " + padding);
					System.out.println("Uncompressed Size: " + decompressedBlockSize);
				}
			}		
			break;
		}		
		
		byte decompressedFile[] = null;
		int currentFileOffset = -1;
		
		try{
		if (fileSize + extraHeaderSize < 0)
			return null;
				
		decompressedFile = new byte[fileSize];
		}
		catch (Exception e)
		{
			return null;
		}
		
		//If we got a loading dialog
		if (loadingDialog != null)
			loadingDialog.setMaxBlocks(dataBlocks[0].length);
		
		if (dataBlocks == null || dataBlocks[0] == null)
			return null;
		
		int mipmapPosTable[] = null;
		
		//Load in file offsets for mipmaps
		if (extraHeader != null && contentType == TYPE_TEXTURE)
		{
			ByteBuffer bb = ByteBuffer.wrap(extraHeader);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			
			bb.position(0x0E);			
			int numMipmaps = bb.getShort();
			mipmapPosTable = new int[numMipmaps];
			
			if (numMipmaps < blockCount)
				mipmapPosTable = new int[blockCount];
			
			bb.position(0x1C);
			for (int i = 0; i < numMipmaps; i++)
				mipmapPosTable[i] = bb.getInt();
		}
		
		//Extract File
		for (int j = 0; j < (contentType == TYPE_TEXTURE ? blockCount : 1); j++){
			if (mipmapPosTable != null)				
				currentFileOffset = mipmapPosTable[j];
			else
				currentFileOffset = 0;
			
			for (int i = 0; i < dataBlocks[j].length; i++)
			{
				// Block Header
				currentFilePointer.seek(fileOffset + headerLength + dataBlocks[j][i].offset);
				int blockHeaderLength = currentFilePointer.readInt();
				currentFilePointer.readInt(); // NULL
				int compressedBlockSize = currentFilePointer.readInt(); 
				int decompressedBlockSize = currentFilePointer.readInt();
					
				if (Constants.DEBUG)
					System.out.println("Decompressing block " + i + " @ file offset: " + currentFilePointer.getFilePointer() + " @ block offset: " + String.format("%X", dataBlocks[j][i].offset) + ". Compressed Size: " + compressedBlockSize + " and Decompressed Size: " + decompressedBlockSize + ". Block Size: " + dataBlocks[j][i].padding);
				
				byte[] decompressedBlock = null;			
				if (compressedBlockSize == 32000 || decompressedBlockSize == 1) //Not actually compressed, just read decompressed size
				{
					decompressedBlock = new byte[decompressedBlockSize];
					currentFilePointer.readFully(decompressedBlock);
				}
				else //Gotta decompress
					decompressedBlock = decompressBlock(compressedBlockSize, decompressedBlockSize);
				
				System.arraycopy(decompressedBlock, 0, decompressedFile, currentFileOffset, decompressedBlockSize);
				currentFileOffset+=decompressedBlockSize;
				
				if (loadingDialog != null)			
					loadingDialog.nextBlock(i+1);			
			}
		}
		if (extraHeader != null)
			System.arraycopy(extraHeader, 0, decompressedFile, 0, extraHeaderSize);
		
		return decompressedFile;
	}

	@SuppressWarnings("deprecation")
	private byte[] decompressBlock(int compressedSize, int decompressedSize) throws IOException{
		
		// Build the zlib header stuff
		byte[] decompressedData = new byte[decompressedSize];
		byte[] gzipedData = new byte[compressedSize + 6]; // Need to add 6 bytes
															// for missing zlib
															// header/footer
		// Zlib Magic Number
		gzipedData[0] = (byte) 0x78;
		gzipedData[1] = (byte) 0x9C;

		// Actual Data
		currentFilePointer.readFully(gzipedData, 2, compressedSize);

		// Checksum
		int checksum = adler32(gzipedData, 2, compressedSize);
		byte[] checksumByte = ByteBuffer.allocate(4)
				.order(ByteOrder.BIG_ENDIAN).putInt(checksum).array();
		System.arraycopy(checksumByte, 0, gzipedData, 2 + compressedSize, 4);
		
		//Decompress this block
		try {
			
			Inflater inflater = new Inflater();

			inflater.setInput(gzipedData);
			inflater.setOutput(decompressedData);

			int err = inflater.init();
			CHECK_ERR(inflater, err, "inflateInit");

			while (inflater.total_out < decompressedSize
					&& inflater.total_in < gzipedData.length) {
				inflater.avail_in = inflater.avail_out = 1; /*
															 * force small
															 * buffers
															 */
				err = inflater.inflate(JZlib.Z_NO_FLUSH);
				if (err == JZlib.Z_STREAM_END)
					break;
				CHECK_ERR(inflater, err, "inflate");
			}

			err = inflater.end();
			CHECK_ERR(inflater, err, "inflateEnd");

			inflater.finished();

			return decompressedData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	private static void CHECK_ERR(Inflater z, int err, String msg) {
		if (err != JZlib.Z_OK) {
			if (z.msg != null)
				System.out.print(z.msg + " ");
			System.out.println(msg + " error: " + err);
			System.exit(1);
		}
	}

	protected void close() throws IOException {
		currentFilePointer.close();
	}

	private int adler32(byte[] bytes, int offset, int size) {
		final int a32mod = 65521;
		int s1 = 1, s2 = 0;

		for (int i = offset; i < size; i++) {
			int b = bytes[i];

			s1 = (s1 + b) % a32mod;
			s2 = (s2 + s1) % a32mod;
		}
		return (int) ((s2 << 16) + s1);
	}
	
	protected class Data_Block{
		public final int offset;
		public final int padding;
		public final int decompressedSize;
		
		public Data_Block(int offset, int padding, int decompressedSize){
			this.offset = offset;
			this.padding = padding;
			this.decompressedSize = decompressedSize;
		}
		
		public Data_Block(int offset)
		{
			this.offset = offset;
			this.padding = -1;
			this.decompressedSize = -1;
		}
	}
	
	protected class TextureBlocks{
		public final int offset;
		public final int padding;		
		public final int tableOffset;
		public final int subblockSize;
		
		public TextureBlocks(int offset, int padding, int tableOffset, int subblocksize){
			this.offset = offset;
			this.padding = padding;
			this.tableOffset = tableOffset;
			this.subblockSize = subblocksize;
		}
		
	}

	protected int getContentType(long offset) throws IOException {
		currentFilePointer.seek(offset);
		currentFilePointer.readInt(); //Header Length
		return currentFilePointer.readInt();
	}
	
	protected Calendar getTimeStamp() throws IOException
	{
		// Timestamp
		currentFilePointer.seek(0x18);
		int date = currentFilePointer.readInt();
		int time = currentFilePointer.readInt();
		if (date != 0)
		{
			//Copied code from Cassiope example, too lazy to write my own lol.
			int yyyy = (int) (date   /   10000) % 10000;
            int mm   = (int) (date   /     100) %   100;
            int dd   = (int) (date            ) %   100;
            int hh24 = (int) (time / 1000000) %   100;
            int mi   = (int) (time /   10000) %   100;
            int ss   = (int) (time /     100) %   100;
            int ms   = (int) (time          ) %   100;
            
            Calendar timestamp = Calendar.getInstance();
            timestamp.set(yyyy, mm, dd, hh24, mi, ss);
            return timestamp;
		}		
		return null;
	}
}

