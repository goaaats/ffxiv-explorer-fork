package ca.fraggergames.ffxivextract.storage;

import java.util.Hashtable;

//Stores a master file list to check between updates.

public class FileListDatabase {

	Hashtable<Long, Boolean> files;
	
	public void addToList(long hash)
	{
		files.put(hash, true);
	}
	
	public boolean fileExists(long hash)
	{
		return files.get(hash);
	}
	
}
