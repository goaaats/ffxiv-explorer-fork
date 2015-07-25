package com.fragmenterworks.ffxivextract.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.fragmenterworks.ffxivextract.storage.HashDatabase;
import com.fragmenterworks.ffxivextract.gui.components.Loading_Dialog;
import com.fragmenterworks.ffxivextract.helpers.LERandomAccessFile;

public class SqPack_IndexFile {

	private String path;
	
	private SqPack_DataSegment segments[] = new SqPack_DataSegment[4];
	private SqPack_Folder packFolders[];
	private boolean noFolder = false;	

	private boolean isFastloaded = false;
	
	/**
	  * Constructor. Primarily used by the FileManagerWindow to handle gui stuff. Loads all file info + structure info and names.
	  * 
	  * @param pathToIndex Path to the SqPack index file you wish to open 	   
	  * @param prgLoadingBar The progress bar to increment
	  * @param lblLoadingBarString The progress bar text to increment
	  */
	public SqPack_IndexFile(String pathToIndex, JProgressBar prgLoadingBar, JLabel lblLoadingBarString) throws IOException {

		path = pathToIndex;
		
		LERandomAccessFile ref = new LERandomAccessFile(pathToIndex, "r");

		int sqpackHeaderLength = checkSqPackHeader(ref);			
		
		getSegments(ref, sqpackHeaderLength);

		// Check if we have a folder segment, if not... load files only
		if (segments[3] != null && segments[3].offset != 0) {			
			int offset = segments[3].getOffset();
			int size = segments[3].getSize();
			int numFolders = size / 0x10;

			if (prgLoadingBar != null)
				prgLoadingBar.setMaximum(segments[0].getSize()/0x10);

			if (lblLoadingBarString != null)
				lblLoadingBarString.setText("0%");
			
			packFolders = new SqPack_Folder[numFolders];

			for (int i = 0; i < numFolders; i++) {
				ref.seek(offset + (i * 16)); // Every folder offset header is 16
												// bytes

				int id = ref.readInt();
				int fileIndexOffset = ref.readInt();
				int folderSize = ref.readInt();
				int numFiles = folderSize / 0x10;
				ref.readInt(); // Skip

				packFolders[i] = new SqPack_Folder(id, numFiles,
						fileIndexOffset);

				packFolders[i].readFiles(ref, prgLoadingBar, lblLoadingBarString, false);				
			}
		} else {
			noFolder = true;
			
			if (prgLoadingBar != null)
				prgLoadingBar.setMaximum((pathToIndex.contains("index2") ? 2 : 1 ) * segments[0].getSize()/0x10);

			if (lblLoadingBarString != null)
				lblLoadingBarString.setText("0%");
			
			packFolders = new SqPack_Folder[1];
			packFolders[0] = new SqPack_Folder(0, (pathToIndex.contains("index2") ? 2 : 1 ) * segments[0].getSize()/0x10, segments[0].getOffset());
			packFolders[0].readFiles(ref, prgLoadingBar, lblLoadingBarString, pathToIndex.contains("index2"));
		}

		ref.close();

	}
	
	 /**
	  * Constructor.
	  * 
	  * @param pathToIndex Path to the SqPack index file you wish to open 	   
	  * @param fastLoad Setting this to true will load only file info of the archive, and omit it's structure and file/folder names.
	  */
	public SqPack_IndexFile(String pathToIndex, boolean fastLoad) throws IOException {

		path = pathToIndex;
		
		LERandomAccessFile ref = new LERandomAccessFile(pathToIndex, "r");

		int sqpackHeaderLength = checkSqPackHeader(ref);	
		getSegments(ref, sqpackHeaderLength);

		//Fast load will blindly load all files regardless of folder
		if (fastLoad)
		{
			isFastloaded = true;
			
			noFolder = true;
			packFolders = new SqPack_Folder[1];
			packFolders[0] = new SqPack_Folder(0, (pathToIndex.contains("index2") ? 2 : 1 ) * segments[0].getSize()/0x10, segments[0].getOffset());
			
			ref.seek(segments[0].getOffset());
			
			for (int i = 0; i < packFolders[0].files.length; i++)
			{			
				if (!pathToIndex.contains("index2"))
				{
					int id = ref.readInt();
					int id2 = ref.readInt();
					long dataoffset = ref.readInt();
					ref.readInt();
				
					packFolders[0].getFiles()[i] = new SqPack_File(id, id2, dataoffset, false);
				}
				else
				{
					int id = ref.readInt();
					long dataoffset = ref.readInt();
					packFolders[0].getFiles()[i] = new SqPack_File(id, -1, dataoffset, false);										
				}					
			}
		}
		else
		{
			// Check if we have a folder segment, if not... load files only
			if (segments[3] != null) {			
				int offset = segments[3].getOffset();
				int size = segments[3].getSize();
				int numFolders = size / 0x10;
	
				packFolders = new SqPack_Folder[numFolders];
	
				for (int i = 0; i < numFolders; i++) {
					ref.seek(offset + (i * 16)); // Every folder offset header is 16
													// bytes
	
					int id = ref.readInt();
					int fileIndexOffset = ref.readInt();
					int folderSize = ref.readInt();
					int numFiles = folderSize / 0x10;
					ref.readInt(); // Skip
	
					packFolders[i] = new SqPack_Folder(id, numFiles,
							fileIndexOffset);
	
					packFolders[i].readFiles(ref, false);
				}
			} else {
				noFolder = true;
				packFolders = new SqPack_Folder[1];
				packFolders[0] = new SqPack_Folder(0, (pathToIndex.contains("index2") ? 2 : 1 ) * segments[0].getSize()/0x10, segments[0].getOffset());
				packFolders[0].readFiles(ref, pathToIndex.contains("index2"));
			}
		}

		ref.close();

	}
	
	/** Checks the sqpack header, will also advance the file pointer by it's size.  */
	public static int checkSqPackHeader(LERandomAccessFile ref) throws IOException{
		// Check SqPack Header
		byte[] buffer = new byte[6];
		ref.readFully(buffer, 0, 6);
		if (buffer[0] != 'S' || buffer[1] != 'q' || buffer[2] != 'P'
				|| buffer[3] != 'a' || buffer[4] != 'c' || buffer[5] != 'k') {
			ref.close();
			throw new IOException("Not a SqPack file");
		}

		// Get Header Length
		ref.seek(0x0c);
		int headerLength = ref.readInt();

		ref.readInt(); // Unknown

		// Get Header Type, has to be 2 for index
		int type = ref.readInt();
		if (type != 2)
			throw new IOException("Not a index");
		
		return headerLength;
	}

	private int getSegments(LERandomAccessFile ref, int segmentHeaderStart)
			throws IOException {

		ref.seek(segmentHeaderStart);
		
		int headerLength = ref.readInt();				

		for (int i = 0; i < segments.length; i++) {
			int firstVal = ref.readInt();			
			int offset = ref.readInt();
			int size = ref.readInt();
			byte[] sha1 = new byte[20];
			ref.readFully(sha1, 0, 20);
			segments[i] = new SqPack_DataSegment(offset, size, sha1);			

			if (i == 0)
				ref.skipBytes(0x4);
			ref.skipBytes(0x28);
		}
		
		return headerLength;
	}

	/** Returns the folders in this archive.  */
	public SqPack_Folder[] getPackFolders() {
		return packFolders;
	}
	
	/** Returns if this archive has no folders (usually the case with index2 and will be for fastloads)  */
	public boolean hasNoFolders()
	{
		return noFolder;
	}
	
	/** Debug  */
	public void displayIndexInfo() {
		for (int i = 0; i < getPackFolders().length; i++) {
			System.out.println("Folder: "
					+ String.format("%X",
							getPackFolders()[i].getId() & 0xFFFFFFFF));
			System.out.println("Num files: "
					+ getPackFolders()[i].getFiles().length);
			System.out.println("Files: ");
			for (int j = 0; j < getPackFolders()[i].getFiles().length; j++) {
				System.out.println("\t"
						+ String.format("%X",
								getPackFolders()[i].getFiles()[j].id)
						+ " @offset "
						+ String.format("%X",
								getPackFolders()[i].getFiles()[j].dataoffset));
			}
		}
	}

	public class SqPack_DataSegment {

		private int offset;
		private int size;
		private byte[] sha1 = new byte[20];
		
		public SqPack_DataSegment(int offset, int size, byte[] sha1)
		{
			this.offset = offset;
			this.size = size;
			this.sha1 = sha1;
		}
		
		public int getOffset()
		{
			return offset;
		}
		
		public int getSize()
		{
			return size;
		}

		public byte[] getSha1()
		{
			return sha1;
		}
	}
	
	public static class SqPack_Folder {
		
		private int id;
		private SqPack_File files[];
		private long fileIndexOffset;
		private String name;
		
		public SqPack_Folder(int id, int numFiles, long fileIndexOffset) {
			this.id = id;
			this.files = new SqPack_File[numFiles];
			this.fileIndexOffset = fileIndexOffset;
			this.name = HashDatabase.getFolder(id);
			if (this.name == null)
				this.name = String.format("~%x", id);
			//else
				//HashDatabase.flagFolderNameAsUsed(id);
		}
		
		protected void readFiles(LERandomAccessFile ref, JProgressBar prgLoadingBar, JLabel lblLoadingBarString, boolean isIndex2) throws IOException{
			ref.seek(fileIndexOffset);
			
			for (int i = 0; i < files.length; i++)
			{			
				if (!isIndex2){
					int id = ref.readInt();
					int id2 = ref.readInt();
					long dataoffset = ref.readInt();
					ref.readInt();
				
					files[i] = new SqPack_File(id, id2, dataoffset, true);
				
					if (prgLoadingBar != null)
						prgLoadingBar.setValue(prgLoadingBar.getValue()+1);
					
					if (lblLoadingBarString != null)
						lblLoadingBarString.setText((int)(prgLoadingBar.getPercentComplete() * 100) + "%");
				}
				else
				{
					int id = ref.readInt();
					long dataoffset = ref.readInt();
					files[i] = new SqPack_File(id, -1, dataoffset, true);
					
					if (prgLoadingBar != null)
						prgLoadingBar.setValue(prgLoadingBar.getValue()+1);
					
					if (lblLoadingBarString != null)
						lblLoadingBarString.setText((int)(prgLoadingBar.getPercentComplete() * 100) + "%");
					
				}
			}
		}
		
		protected void readFiles(LERandomAccessFile ref, boolean isIndex2) throws IOException{
			ref.seek(fileIndexOffset);
			
			for (int i = 0; i < files.length; i++)
			{			
				if (!isIndex2)
				{
					int id = ref.readInt();
					int id2 = ref.readInt();
					long dataoffset = ref.readInt();
					ref.readInt();
				
					files[i] = new SqPack_File(id, id2, dataoffset, true);
				}
				else
				{
					int id = ref.readInt();
					long dataoffset = ref.readInt();
					files[i] = new SqPack_File(id, -1, dataoffset, true);										
				}					
			}
		}
		
		public int getId()
		{
			return id;
		}
		
		public SqPack_File[] getFiles()
		{
			return files;
		}
		
		public String getName()
		{
			return name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public static class SqPack_File {
		public int id, id2;
		public long dataoffset;
		private String name;
		
		public SqPack_File(int id, int id2, long offset, boolean loadNames)
		{
			this.id = id;
			this.id2 = id2;
			this.dataoffset = offset;
			
			//For Index2
			//if (id2 == -1)			
				//this.name = HashDatabase.getFullpath(id);			
			//else
			
			if (loadNames){
			
				if (id2 != -1)
					this.name = HashDatabase.getFileName(id);
				if (this.name == null)
					this.name = String.format("~%x", id);
				
			}
			//else
				//HashDatabase.flagFileNameAsUsed(id);
		}
		
		public int getId()
		{
			return id;		
		}
		
		public long getOffset()
		{
			return dataoffset;
		}

		public int getId2() {
			return id2;
		}
		
		public String getName(){
			return name;
		}
		
		@Override
		public String toString() {
			return name;
		}

		public String getName2() {
			String name = null;
			if (id2 != -1)
				name = HashDatabase.getFileName(id);
			if (name == null)
				name = String.format("~%x", id);
			return name;
		}
	}

	/** Gets the content type of the file at the given offset.  */
	public int getContentType(long dataoffset) throws IOException, FileNotFoundException {
		String pathToOpen = path;
		
		//Get the correct data number
		int datNum = (int) ((dataoffset & 0x000F) / 2);
		dataoffset -= dataoffset & 0x000F;		
		pathToOpen = pathToOpen.replace("index2", "dat" + datNum);
		pathToOpen = pathToOpen.replace("index", "dat" + datNum);
		
		SqPack_DatFile datFile = new SqPack_DatFile(pathToOpen);
		int contentType = datFile.getContentType(dataoffset * 0x8);
		datFile.close();
		return contentType;
	}
	
	/** Extracts the file at the specified path.  */
	public byte[] extractFile(String path) throws IOException, FileNotFoundException 
    {
    	String folder = path.substring(0, path.lastIndexOf("/"));
    	String file = path.substring(path.lastIndexOf("/")+1);
    	
    	return extractFile(folder, file);
    }
    
	/** Extracts the file at the specified folder with the given filename.  */
    public byte[] extractFile(String foldername, String filename) throws IOException, FileNotFoundException 
    {
    	if (getPath().contains("index2"))
    	{
    		String fullPath = foldername + "/" + filename;
    		int hash = HashDatabase.computeCRC(fullPath.getBytes(), 0, fullPath.getBytes().length);
    		for (SqPack_File f : getPackFolders()[0].getFiles())
    		{
    			if (f.getId() == hash)
    				return extractFile(f.getOffset());
    		}
    	}
    	else
    	{
	    	int hash1 = HashDatabase.computeCRC(foldername.getBytes(), 0, foldername.getBytes().length);
			
	    	if (!isFastloaded)
	    	{
				for (SqPack_Folder f : getPackFolders())
				{			
					if (f.getId() == hash1)
					{
									
						int hash2 = HashDatabase.computeCRC(filename.getBytes(), 0, filename.getBytes().length);
						for (SqPack_File file : f.getFiles())
						{
							if (file.id == hash2)
							{
								return extractFile(file.getOffset());
							}
						}
					
						break;
					}
				}	
	    	}
	    	else
	    	{
	    		for (int i = 0; i < packFolders[0].getFiles().length; i++)
	    		{
	    			SqPack_File file = packFolders[0].getFiles()[i];
	    			if (file.getId2() == hash1)
	    			{
	    				int hash2 = HashDatabase.computeCRC(filename.getBytes(), 0, filename.getBytes().length);
	    				if (file.getId() == hash2)
	    					return extractFile(file.getOffset()); 
	    			}
	    		}
	    	}
    	}
		return null;
    }

    /** Extracts the file at the specified offset.  */
    public byte[] extractFile(long dataoffset) throws IOException, FileNotFoundException {
    	return extractFile(dataoffset, null);
    }
    
    /** Extracts the file at the specified offset... loading bar info also given.  */
	public byte[] extractFile(long dataoffset, Loading_Dialog loadingDialog) throws IOException, FileNotFoundException {
		
		String pathToOpen = path;
		
		//Get the correct data number
		int datNum = (int) ((dataoffset & 0x000F) / 2);

		dataoffset -= dataoffset & 0x000F;		
		pathToOpen = pathToOpen.replace("index2", "dat" + datNum);
		pathToOpen = pathToOpen.replace("index", "dat" + datNum);
		
		SqPack_DatFile datFile = new SqPack_DatFile(pathToOpen);
		byte[] data = datFile.extractFile(dataoffset * 0x8, loadingDialog);
		datFile.close();
		return data;
	}

	/** Returns the index file's name.  */
	public String getName() {
		
		if (path.lastIndexOf("/") == -1)
		{
			String frontStripped = path.substring(path.lastIndexOf("\\")+1, path.length());
			return frontStripped.substring(0, frontStripped.indexOf("."));
		}
		
		String frontStripped = path.substring(path.lastIndexOf("/")+1, path.length());
		return frontStripped.substring(0, frontStripped.indexOf("."));
		
	}

	public String getPath() {
		return path;
	}
	public Calendar getDatTimestmap(int datNum) throws IOException
	{
		String pathToOpen = path;
		
		//Get the correct data number		
		pathToOpen = pathToOpen.replace("index2", "dat" + datNum);
		pathToOpen = pathToOpen.replace("index", "dat" + datNum);
		
		SqPack_DatFile datFile = new SqPack_DatFile(pathToOpen);
		Calendar timestamp = datFile.getTimeStamp();
		datFile.close();
		return timestamp;
	}
	
}
