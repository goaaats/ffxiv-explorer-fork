package ca.fraggergames.ffxivextract.models;

import java.io.FileNotFoundException;
import java.io.IOException;

import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;

public class Log_File {
	
	private Log_Entry[] entries;
	
	public Log_File(String path) throws IOException, FileNotFoundException {
		LERandomAccessFile file = new LERandomAccessFile(path, "r");
		
		//Read in sizes
		int bodySize = file.readInt();
		int fileSize = file.readInt();
		
		//Read in size table
		int numOffsets = fileSize - bodySize / 4;
		int offsets[] = new int[numOffsets];
		
		int maxBufferSize = 0;
		
		for (int i = 0; i < numOffsets; i++)
		{
			offsets[i] = file.readInt();
			if (i == 0)
				maxBufferSize = offsets[i];			
			else if (i != 0 && offsets[i]-offsets[i-1] > maxBufferSize)
				maxBufferSize = offsets[i]-offsets[i-1];
		}		
		
		//Read in log entries
		byte buffer[] = new byte[maxBufferSize];
		for (int i = 0; i < offsets.length; i++)
		{			
			file.read(buffer, 0, offsets[i]);
			String data = new String(buffer, 0, offsets[i]);
			
			long time = Long.parseLong(data.substring(0, 8), 16);
			int eventId = Integer.parseInt(data.substring(8, 12), 16);
			String message = new String(data.substring(14).getBytes(), "UTF-8");
			
			entries[i] = new Log_Entry(time, eventId, message);
		}
		
		file.close();
	}
	
	public Log_Entry[] getEntries()
	{
		return entries;
	}
	
	public static class Log_Entry{
		final public long time;
		final public int eventId;
		final public String message;
		
		public Log_Entry(long time, int eventId, String message) {
			this.time = time;
			this.eventId = eventId;
			this.message = message;
		}
	}
}
