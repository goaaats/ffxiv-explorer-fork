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
			
			String[] splitData = data.split(":");
						
			String info = splitData[0];
			String sender = splitData[1];
			String message = splitData[2];
			
			long time = Long.parseLong(info.substring(0, 8), 16);			
			int filter = Integer.parseInt(info.substring(8, 10), 16);
			int channel = Integer.parseInt(info.substring(10, 12), 16);			
			
			entries[i] = new Log_Entry(time, filter, channel, new String(sender.getBytes(), "UTF-8"), new String(message.getBytes(), "UTF-8"));
		}
		
		file.close();
	}
	
	public Log_Entry[] getEntries()
	{
		return entries;
	}
	
	public static class Log_Entry{
		final public long time;
		final public int channel;
		final public int filter;
		final public String sender;
		final public String message;
		
		public Log_Entry(long time, int filter, int channel, String sender, String message) {
			this.time = time;
			this.filter = filter;
			this.channel = channel;
			this.sender = sender;
			this.message = message;
		}
	}
}
