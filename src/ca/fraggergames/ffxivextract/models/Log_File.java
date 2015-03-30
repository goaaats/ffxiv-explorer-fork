package ca.fraggergames.ffxivextract.models;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.mysql.jdbc.log.Log;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.helpers.FFXIV_String;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;

public class Log_File {
	
	public static final int CHANNEL_SAY = 0xA;
	public static final int CHANNEL_SHOUT = 0xB;
	public static final int CHANNEL_TELLOUT = 0xC;
	public static final int CHANNEL_TELLIN = 0xD;
	public static final int CHANNEL_PARTY = 0xE;
	public static final int CHANNEL_ALLIANCE = 0xF;
	public static final int CHANNEL_LS1 = 0x10;
	public static final int CHANNEL_LS2 = 0x11;
	public static final int CHANNEL_LS3 = 0x12;
	public static final int CHANNEL_LS4 = 0x13;
	public static final int CHANNEL_LS5 = 0x14;
	public static final int CHANNEL_LS6 = 0x15;
	public static final int CHANNEL_LS7 = 0x16;
	public static final int CHANNEL_LS8 = 0x17;
	public static final int CHANNEL_FC = 0x18;
	public static final int CHANNEL_EMOTE_STANDARD = 0x1C;
	public static final int CHANNEL_EMOTE_CUSTOM = 0x1D;
	public static final int CHANNEL_YELL = 0x1E;		
	public static final int CHANNEL_SYSTEM = 0x39;
	
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
			String sender = splitData.length >= 2 ? FFXIV_String.parseFFXIVString(splitData[1].getBytes()) : "";			
			String message = splitData.length == 3 ? FFXIV_String.parseFFXIVString(splitData[2].getBytes()) : "";
			
			long time = Long.parseLong(info.substring(0, 8), 16);			
			int filter = Integer.parseInt(info.substring(8, 10), 16);
			int channel = Integer.parseInt(info.substring(10, 12), 16);			
			
			entries[i] = new Log_Entry(time, filter, channel, sender, message);
			
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
			
			Date date = new Date(time*1000);			
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
	
	public static String getChannelName(int channel){
		switch (channel)
		{
		case Log_File.CHANNEL_SAY:
			return "Say";
		case Log_File.CHANNEL_SHOUT:
			return "Shout";
		case Log_File.CHANNEL_TELLIN:
			return "Tell (in)";
		case Log_File.CHANNEL_TELLOUT:
			return "Tell (out)";
		case Log_File.CHANNEL_PARTY:
			return "Party";
		case Log_File.CHANNEL_ALLIANCE:
			return "Alliance";
		case Log_File.CHANNEL_LS1:
			return "Linkshell 1";
		case Log_File.CHANNEL_LS2:
			return "Linkshell 2";
		case Log_File.CHANNEL_LS3:
			return "Linkshell 3";
		case Log_File.CHANNEL_LS4:
			return "Linkshell 4";
		case Log_File.CHANNEL_LS5:
			return "Linkshell 5";
		case Log_File.CHANNEL_LS6:
			return "Linkshell 6";
		case Log_File.CHANNEL_LS7:
			return "Linkshell 7";
		case Log_File.CHANNEL_LS8:
			return "Linkshell 8";
		case Log_File.CHANNEL_FC:
			return "Free Company";
		case Log_File.CHANNEL_EMOTE_STANDARD:
			return "Emote";
		case Log_File.CHANNEL_EMOTE_CUSTOM:
			return "Emote (Custom)";
		case Log_File.CHANNEL_YELL:
			return "Yell";
		case Log_File.CHANNEL_SYSTEM:
			return "System";
		}
		return "Undefined";
	}
	
}
