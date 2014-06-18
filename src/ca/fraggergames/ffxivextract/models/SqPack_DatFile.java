package ca.fraggergames.ffxivextract.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;

import com.jcraft.jzlib.Inflater;
import com.jcraft.jzlib.JZlib;

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
		
		System.out.println("================================");
		System.out.println(String.format("File @ %08x", fileOffset));
		System.out.println("================================");
		System.out.println("Header Length: " + headerLength);
		System.out.println("Content Type: " + contentType);
		System.out.println("File Size: " + fileSize);
		System.out.println("Block Size: " + blockBufferSize);
		System.out.println("Num Blocks: " + blockCount);
		
		//Read in Block Info Header
		for (int i = 0; i < blockCount; i++){
			int sizes = currentFilePointer.readInt();
			int compressedBlockSize = sizes & 0xFFFF;
			int uncompressedBlockSize = sizes >> 16 & 0xFFFF;
			currentFilePointer.readInt(); //UNKNOWN
			
			System.out.println("Block #" + i);
			System.out.println("Compressed Size: " + compressedBlockSize);
			System.out.println("Uncompressed Size: " + uncompressedBlockSize);
			
		}
		
		//Block Header
		currentFilePointer.seek(fileOffset + headerLength);
		currentFilePointer.readInt(); //ID?
		currentFilePointer.readInt(); //NULL
		int compressedSize = currentFilePointer.readInt(); //COMPRESSED SIZE?
		int uncompressedSize = currentFilePointer.readInt(); //UNCOMPRESSED SIZE?
		
		//GZIPED FILE HERE
		byte[] gzipedData = new byte[compressedSize + 6]; //Need to add 6 bytes for missing GZIP header/footer
		
		//GZIP MAGICK NUMBAH
		gzipedData[0] = (byte) 0x58; 
		gzipedData[1] = (byte) 0x85;
		
		//Actual Data
		currentFilePointer.readFully(gzipedData, 2, compressedSize);		
		
		//Checksum
		int checksum = adler32(gzipedData, 2, compressedSize);
		byte[] checksumByte = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(checksum).array();
		System.arraycopy(checksumByte, 0, gzipedData, 2 + compressedSize, 4);
		
		//Decompress
		byte[] decompressedData = decompress(gzipedData, uncompressedSize);
				
		return decompressedData;
	}
	
	@SuppressWarnings("deprecation")
	private byte[] decompress(byte[] gzipedData, int uncompressedSize) {
		try{

			byte[] decompressed = new byte[uncompressedSize];
			Inflater inflater = new Inflater();

		    inflater.setInput(gzipedData);
		    inflater.setOutput(decompressed);

		    int err = inflater.init();
		    CHECK_ERR(inflater, err, "inflateInit");

		    while(inflater.total_out<uncompressedSize &&
		      inflater.total_in<gzipedData.length) {
		      inflater.avail_in=inflater.avail_out=1; /* force small buffers */
		      err=inflater.inflate(JZlib.Z_NO_FLUSH);
		      if(err==JZlib.Z_STREAM_END) break;
		      CHECK_ERR(inflater, err, "inflate");
		    }
		    
		    err=inflater.end();	
		    CHECK_ERR(inflater, err, "inflateEnd");
		    
		    inflater.finished();		    
		    		    
			return decompressed;
		}
		catch (Exception e)
		{
			e.printStackTrace();	
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	static void CHECK_ERR(Inflater z, int err, String msg) {
	    if(err!=JZlib.Z_OK){
	      if(z.msg!=null) System.out.print(z.msg+" ");
	      System.out.println(msg+" error: "+err);
	      System.exit(1);
	    }
	  }
	
	public void close() throws IOException{
		currentFilePointer.close();
	}
	
	private int adler32(byte[] bytes, int offset, int size)
	{
	    final int a32mod = 65521;
	    int s1 = 1, s2 = 0;

	    for(int i = offset; i < size; i++)
	    {
	        int b = bytes[i];

	        s1 = (s1 + b) % a32mod;
	        s2 = (s2 + s1) % a32mod;
	    }
	    return (int)((s2 << 16) + s1);
	}
}

