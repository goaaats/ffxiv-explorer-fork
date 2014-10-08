package ca.fraggergames.ffxivextract.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

	public SqPack_DatFile(String path) throws FileNotFoundException {
		currentFilePointer = new LERandomAccessFile(path, "r");
	}

	public byte[] extractFile(long fileOffset, Loading_Dialog loadingDialog) throws IOException {
		currentFilePointer.seek(fileOffset);
		int headerLength = currentFilePointer.readInt();
		int contentType = currentFilePointer.readInt();
		int fileSize = currentFilePointer.readInt();
		currentFilePointer.readInt(); // UNKNOWN
		int blockBufferSize = currentFilePointer.readInt() * 0x80;
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
		
		Data_Block[] dataBlocks = null;

		if (Constants.DEBUG){
			System.out.println("================================");
			System.out.println("Block Data for this file");
			System.out.println("================================");			
		}
		
		//How to get the blocks
		switch (contentType)
		{
		case TYPE_TEXTURE:
			int numIndexes = currentFilePointer.readInt();
			extraHeaderSize = currentFilePointer.readInt();
			int blockSize = currentFilePointer.readInt();
			currentFilePointer.readInt(); // UNKNOWN
			currentFilePointer.readInt(); // NULL
			int numBlocks = currentFilePointer.readInt();
			
			dataBlocks = new Data_Block[numBlocks];
			int runningTotal = 0;
			for (int i = 0; i < numBlocks; i++) {		
				dataBlocks[i] = new Data_Block(runningTotal);
				runningTotal+= currentFilePointer.readShort();
				if (Constants.DEBUG){
					System.out.println("Block #" + i);
					System.out.println("Offset: " + String.format("%X", dataBlocks[i].offset));					
				}
			}
			
			break;
		case TYPE_MODEL:
			return null;
		case TYPE_BINARY: 
			int blockCount = currentFilePointer.readInt();
			dataBlocks = new Data_Block[blockCount];
			
			// Read in Block Info Header
			for (int i = 0; i < blockCount; i++) {
				int offset = currentFilePointer.readInt();
				int paddingAndSize = currentFilePointer.readInt();
				int padding = paddingAndSize & 0xFFFF;
				int decompressedBlockSize = paddingAndSize >> 16 & 0xFFFF;

				dataBlocks[i] = new Data_Block(offset, padding, decompressedBlockSize);
			
				if (Constants.DEBUG){
					System.out.println("Block #" + i);
					System.out.println("Offset: " + String.format("%X", offset));
					System.out.println("Padding: " + padding);
					System.out.println("Uncompressed Size: " + decompressedBlockSize);
				}
			}		
			break;
		}
		byte decompressedFile[] = new byte[fileSize];
		int currentFileOffset = 0;
		
		//If we got a loading dialog
		if (loadingDialog != null)
			loadingDialog.setMaxBlocks(dataBlocks.length);
		
		//Extract File
		for (int i = 0; i < dataBlocks.length; i++)
		{
			// Block Header
			currentFilePointer.seek(fileOffset + headerLength + extraHeaderSize + dataBlocks[i].offset);
			int blockHeaderLength = currentFilePointer.readInt();
			currentFilePointer.readInt(); // NULL
			int compressedBlockSize = currentFilePointer.readInt(); 
			int decompressedBlockSize = currentFilePointer.readInt();
			
			if (Constants.DEBUG)
				System.out.println("Decompressing block " + i + " @ file offset: " + currentFilePointer.getFilePointer() + " @ block offset: " + String.format("%X", dataBlocks[i].offset) + ". Compressed Size: " + compressedBlockSize + " and Decompressed Size: " + decompressedBlockSize + ". Block Size: " + dataBlocks[i].padding);
			
			byte[] decompressedBlock = null;			
			if (compressedBlockSize == 32000) //Not actually compressed, just read decompressed size
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
	static void CHECK_ERR(Inflater z, int err, String msg) {
		if (err != JZlib.Z_OK) {
			if (z.msg != null)
				System.out.print(z.msg + " ");
			System.out.println(msg + " error: " + err);
			System.exit(1);
		}
	}

	public void close() throws IOException {
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
	
	public class Data_Block{
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

	public int getContentType(long offset) throws IOException {
		currentFilePointer.seek(offset);
		currentFilePointer.readInt(); //Header Length
		return currentFilePointer.readInt();
	}
}
