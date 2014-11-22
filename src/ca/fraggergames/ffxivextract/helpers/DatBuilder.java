package ca.fraggergames.ffxivextract.helpers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;


import com.jcraft.jzlib.GZIPOutputStream;

public class DatBuilder {
	
	private LERandomAccessFile fileOut = null;
	private long currentOffset = 0x800;
	private int index;
	
	public DatBuilder(int index, String outPath) throws FileNotFoundException
	{
		this.index = index + 1;
		
		File f = new File(outPath);
		if (f.exists())
			f.delete();
	
		fileOut = new LERandomAccessFile(new File(outPath), "rw");
	}
	
	public long addFile(String path)
	{
		File fileToLoad = new File(path);
		byte[] data = new byte[(int) fileToLoad.length()];
		try {
		    FileInputStream fIn = new FileInputStream(fileToLoad);
		    fIn.read(data);
		    fIn.close();
		} catch (Exception e) {
		    e.printStackTrace();
		    return -1;
		}
		
		ArrayList<DataBlock> blocks = new ArrayList<DatBuilder.DataBlock>();
		int position = 0;
		int largestBlock = 0;
		while (data.length-position > 16000)
		{
			byte uncompressedBlock[] = new byte[16000];
			System.arraycopy(data, position, uncompressedBlock, 0, 16000);
			DataBlock currentBlock = null;
			try {
				currentBlock = new DataBlock(blocks.size() == 0 ? 0 : blocks.get(blocks.size()-1).nextOffset, uncompressedBlock);
				blocks.add(currentBlock);
				position += 16000;					
			} catch (IOException e) {
				e.printStackTrace();					
			}
			
			if (currentBlock.uncompressedSize > largestBlock)
				largestBlock = currentBlock.uncompressedSize;
		}
		
		if (data.length-position > 0)
		{
			byte finalBlock[] = new byte[data.length-position-1];
			System.arraycopy(data, position, finalBlock, 0, finalBlock.length);
			try{
			DataBlock currentBlock = new DataBlock(blocks.size() == 0 ? 0 : blocks.get(blocks.size()-1).nextOffset, finalBlock);
			blocks.add(currentBlock);
			
			if (currentBlock.uncompressedSize > largestBlock)
				largestBlock = currentBlock.uncompressedSize;
			}
			catch (Exception e)
			{
				return -1;
			}
		}
		
		//Generate Header
		byte tempHeader[] = new byte[0x9900];
		ByteBuffer headerBB = ByteBuffer.wrap(tempHeader);
		headerBB.order(ByteOrder.LITTLE_ENDIAN);
		headerBB.putInt(0x100);
		headerBB.putInt(2);
		headerBB.putInt(data.length);
		headerBB.putInt(0);
		headerBB.putInt(largestBlock*2);
		headerBB.putInt(blocks.size());
		
		Iterator<DataBlock> it = blocks.iterator();
		int headerSize = 0;
		while (it.hasNext())
		{				
			DataBlock block = it.next();
			headerBB.putInt(block.offset); //Offset
			headerBB.putShort((short) (block.totalSize));
			headerBB.putShort((short) block.uncompressedSize);			
		}			
		headerSize += (6 * 4) + (3 * 4 * blocks.size());
		while ((headerSize & 0xFF) != 0x00)
			headerSize += 1;
		
		headerBB.rewind();
		headerBB.putInt(headerSize);
		byte header[] = new byte[headerSize];
		System.arraycopy(tempHeader, 0,	 header, 0, header.length);
		
		try {
			fileOut.seek(currentOffset);
			fileOut.write(header);
			
			Iterator<DataBlock> it2 = blocks.iterator();			
			while (it2.hasNext())
			{			
				DataBlock block = it2.next();
				
				fileOut.seek(currentOffset+header.length + block.offset);
				
				byte data2[] = new byte[16 + block.compressedSize];
				ByteBuffer bb = ByteBuffer.wrap(data2);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				
				//Write Header
				bb.putInt(0x10);
				bb.putInt(0x0);
				bb.putInt(block.compressedSize);
				bb.putInt(block.uncompressedSize);
					
				//Write Block
				bb.put(block.compressedData);
				
				bb.rewind();
				
				fileOut.write(data2);				
			}			
			
			long curposition = fileOut.getFilePointer();
			while ((curposition & 0xFF) != 0x00)
				curposition += 1;
			
			long entryOffset = ((currentOffset)/8) + (index * 2);
			
			currentOffset = curposition;
			
			System.out.println(String.format("Added file at path \"%s\", to offset: 0x%X", path, entryOffset));
			
			return entryOffset;
		} catch (IOException e) {
			e.printStackTrace();			
		}
		return -1;
	}	

	private byte[] buildSqpackDatHeader()
	{
		byte sqpackHeader[] = new byte[0x400];
		
		try{
			ByteBuffer sqpackHeaderBB = ByteBuffer.wrap(sqpackHeader);
			sqpackHeaderBB.order(ByteOrder.LITTLE_ENDIAN);
			
			int signature = 0x61507153;
			int signature2 = 0x00006b63;				
			
			sqpackHeaderBB.putInt(signature);
			sqpackHeaderBB.putInt(signature2);
			sqpackHeaderBB.putInt(0);
			sqpackHeaderBB.putInt(0x400);
			sqpackHeaderBB.putInt(1);
			sqpackHeaderBB.putInt(1);
			sqpackHeaderBB.position(0x20);
			sqpackHeaderBB.putInt(0xFFFFFFFF);
			
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(sqpackHeader, 0, 0x3BF);
			sqpackHeaderBB.position(0x3c0);
			sqpackHeaderBB.put(md.digest());
				
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
		return sqpackHeader;
	}
	
	private byte[] buildSqpackDatDataHeader(int datBodyLength, byte[] datBodySha1, int spannedIndexNum)
	{
		byte dataHeader[] = new byte[0x400];
		
		try{
			ByteBuffer dataHeaderBB = ByteBuffer.wrap(dataHeader);
			dataHeaderBB.order(ByteOrder.LITTLE_ENDIAN);
			
			dataHeaderBB.putInt(dataHeader.length);
			dataHeaderBB.putInt(0);
			dataHeaderBB.putInt(0x10);
			dataHeaderBB.putInt(datBodyLength);
			dataHeaderBB.putInt(spannedIndexNum);
			dataHeaderBB.putInt(0);
			dataHeaderBB.putInt(0x77359400);
			dataHeaderBB.putInt(0);
						
			dataHeaderBB.put(datBodySha1);			
			
			//Sha1 of header
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.update(dataHeader, 0, 0x3BF);
			dataHeaderBB.position(0x3C0);
			dataHeaderBB.put(md.digest());
				
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
		
		return dataHeader;
	}

	private class DataBlock{
		public final short totalSize;
		public final byte compressedData[];
		public final int compressedSize;
		public final int uncompressedSize;
		public final int offset;
		public int nextOffset;
		
		public DataBlock(int offset, byte toBeCompressed[]) throws IOException
		{
			this.offset = offset;
			uncompressedSize = toBeCompressed.length;				
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream(toBeCompressed.length);
			GZIPOutputStream zipOut = new GZIPOutputStream(byteOut);								
			zipOut.write(toBeCompressed);
			zipOut.finish();
			compressedSize = (int) (zipOut.getTotalOut()-10);
			zipOut.close();
			byte tempDecompressed[] = byteOut.toByteArray();
			compressedData = new byte[compressedSize];
			System.arraycopy(tempDecompressed, 10, compressedData, 0, compressedSize);
			
			//Generate offset				
			nextOffset = compressedSize + 0x10 + offset;
			while ((nextOffset & 0xFF) != 0x00)
				nextOffset += 1;
			
			totalSize = (short) (nextOffset - offset);
		}
		
	}

	public void finish() {
		
		try{		
			byte buffer[] = new byte[2048];
			ByteBuffer bb = ByteBuffer.wrap(buffer);		
			fileOut.seek(0x800);
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("SHA1");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			while (true)
			{
				int bytesRead = fileOut.read(buffer);
				bb.rewind();
				if (bytesRead <= 0)
					break;
				md.update(buffer, 0, bytesRead);
				bb.rewind();
			}
			
			byte sha1[] = md.digest();
							
			fileOut.seek(0);
			fileOut.write(buildSqpackDatHeader());
			fileOut.write(buildSqpackDatDataHeader((int) currentOffset, sha1, index));
			fileOut.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();			
		}
		finally{
			try {
				fileOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
