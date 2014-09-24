package ca.fraggergames.ffxivextract.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.helpers.FFXIV_String;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;

public class Log_File {
	
	private Log_Entry[] entries;
	
	public Log_File(String path) throws IOException, FileNotFoundException {
		LERandomAccessFile file = new LERandomAccessFile(path, "r");
		
		//Read in sizes
		int bodySize = file.readInt();
		int fileSize = file.readInt();
		
		//Read in size table
		int numOffsets = (fileSize - bodySize);
		int offsets[] = new int[numOffsets];
		entries = new Log_Entry[numOffsets];
		
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
			file.read(buffer, 0, offsets[i] - (i == 0 ? 0 : offsets[i-1]));
			String data = new String(buffer, 0, offsets[i] - (i == 0 ? 0 : offsets[i-1]));
			
			String[] splitData = data.split(":");							
			
			String info = splitData[0];
			String sender = splitData[1];
			String message = splitData[2];
			
			long time = Long.parseLong(info.substring(0, 8), 16);			
			int filter = Integer.parseInt(info.substring(8, 10), 16);
			int channel = Integer.parseInt(info.substring(10, 12), 16);			
			
			entries[i] = new Log_Entry(time, filter, channel, FFXIV_String.parseFFXIVString(sender), FFXIV_String.parseFFXIVString(message));
			
			if (Constants.DEBUG)
			{
				if (!entries[i].sender.toString().isEmpty())
					System.out.print(entries[i].sender.toString() + ": ");
				System.out.println(entries[i].message.toString());
			}
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
		final public String formattedTime;
		
		public Log_Entry(long time, int filter, int channel, String string, String message) {
			this.time = time;
			this.filter = filter;
			this.channel = channel;
			this.sender = string;
			this.message = message;
			
			Date date = new Date(time);
	        DateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
	        format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	        String formatted = format.format(date);
	        
			formattedTime = formatted;
		}
		
		@Override
		public String toString() {
			return message.toString();
		}
	}
}
