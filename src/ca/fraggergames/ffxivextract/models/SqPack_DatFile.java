package ca.fraggergames.ffxivextract.models;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.plaf.metal.MetalIconFactory.FolderIcon16;

import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;

public class SqPack_DatFile {
		
	private LERandomAccessFile currentFilePointer;
	
	public SqPack_DatFile(String path) throws FileNotFoundException
	{
		currentFilePointer = new LERandomAccessFile(path, "r");
	}
	
	public byte[] extractFile(long fileOffset) throws IOException
	{
		currentFilePointer.seek(fileOffset);
		int headerLength = currentFilePointer.readInt();
		int contentType = currentFilePointer.readInt();
		int fileSize = currentFilePointer.readInt();
		currentFilePointer.readInt(); //UNKNOWN
		int blockBufferSize = currentFilePointer.readInt() * 0x80;
		int blockCount = currentFilePointer.readInt();		
		currentFilePointer.readInt(); //UNKNOWN (NULL?)
		
		//If texture, there is an extra 128 bytes of header
			currentFilePointer.skipBytes(0x80);
		
		System.out.println("================================");
		System.out.println(String.format("File @ %08x", fileOffset));
		System.out.println("================================");
		System.out.println("Header Length: " + headerLength);
		System.out.println("Content Type: " + contentType);
		System.out.println("File Size: " + fileSize);
		System.out.println("Block Size: " + blockBufferSize);
		System.out.println("Num Blocks: " + blockCount);
		
		//Read in Block Header
		for (int i = 0; i < blockCount; i++){
			int sizes = currentFilePointer.readInt();
			int compressedBlockSize = sizes & 0xFFFF;
			int uncompressedBlockSize = sizes >> 16 & 0xFFFF;
			currentFilePointer.readInt(); //UNKNOWN
			
			System.out.println("Block #" + i);
			System.out.println("Compressed Size: " + compressedBlockSize);
			System.out.println("Uncompressed Size: " + uncompressedBlockSize);
			
		}
		
		//GZIPED FILE IS AFTER HERE
		
		
		return null;
	}
	
	public void close() throws IOException{
		currentFilePointer.close();
	}
}

