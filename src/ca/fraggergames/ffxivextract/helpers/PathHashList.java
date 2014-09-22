package ca.fraggergames.ffxivextract.helpers;

import java.util.Hashtable;


public class PathHashList {

	Hashtable<Long, String> folders;
	Hashtable<Long, String> filenames;
	
	public void loadHashes(String path)
	{
		
	}
	
	public void saveHashes(String path)
	{
		
	}
	
	public void addPathToDB(String path)
	{
		
	}
	
	public void loadHashesFromSQDB(String path)
	{
		
	}	
	
	public String getFolder(long hash)
	{
		return folders.get(hash);
	}
	
	public String getFileName(long hash)
	{
		return filenames.get(hash);
	}
	
}
