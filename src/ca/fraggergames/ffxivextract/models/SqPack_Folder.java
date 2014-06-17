package ca.fraggergames.ffxivextract.models;

import java.io.IOException;

import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;

public class SqPack_Folder {
	
	private int id;
	private SqPack_File files[];
	private long fileIndexOffset;
	
	protected SqPack_Folder(int id, int numFiles, long fileIndexOffset) {
		this.id = id;
		this.files = new SqPack_File[numFiles];
		this.fileIndexOffset = fileIndexOffset;
	}

	protected void readFiles(LERandomAccessFile ref) throws IOException{
		ref.seek(fileIndexOffset);
		for (int i = 0; i < files.length; i++)
		{			
			int id = ref.readInt();
			ref.readInt();
			long dataoffset = ref.readInt() * 8;
			ref.readInt();
		
			files[i] = new SqPack_File(id, dataoffset);			
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
	
}
