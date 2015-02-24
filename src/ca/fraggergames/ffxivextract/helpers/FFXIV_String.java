package ca.fraggergames.ffxivextract.helpers;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class FFXIV_String {

	final static int START_BYTE = 0x02;
	final static int END_BYTE = 0x03;

	final static int TYPE_NEWLINE = 0x10;
	final static int TYPE_INFO = 0x29;
	final static int TYPE_REFERENCE = 0x28;
	final static int TYPE_REFERENCE2 = 0x40;
	final static int TYPE_IF = 0x08;
	final static int TYPE_SWITCH = 0x09;
	final static int TYPE_SPLIT = 0x2c;
	final static int TYPE_ITALICS = 0x1a;
	final static int TYPE_COLOR_CHANGE = 0x13;	
	final static int TYPE_SERVER_VALUE0 = 0x20;
	final static int TYPE_SERVER_VALUE1 = 0x21;
	final static int TYPE_SERVER_VALUE2 = 0x22;
	final static int TYPE_SERVER_VALUE3 = 0x24;
	final static int TYPE_SERVER_VALUE4 = 0x25;
	final static int TYPE_ICON1 = 0x12;
	final static int TYPE_ICON2 = 0x1E;
	final static int TYPE_DASH = 0x1F;	
	
	final static int TYPE_UNKNOWN2 = 0x2b;
	
	final static int TYPE_UNKNOWN = 0x07;
	
	final static int TYPE_ITEM_LOOKUP = 0x31;
	
	final static int INFO_NAME = 235;
	final static int INFO_GENDER = 233;
	
	//Hard Coded Payloads that are known
	final static byte forenamePayload[] = {-1, 7, 2, 41, 3, -21, 2, 3, -1, 2, 32, 2, 3};
	final static byte surnamePayload[] = {-1, 7, 2, 41, 3, -21, 2, 3, -1, 2, 32, 3, 3};
	
	public static String parseFFXIVString(byte[] stringBytes) {
		
		try{
		byte[] newStringBytes = new byte[stringBytes.length*4];
		
		ByteBuffer buffIn = ByteBuffer.wrap(stringBytes);
		buffIn.order(ByteOrder.LITTLE_ENDIAN);
		ByteBuffer buffOut = ByteBuffer.wrap(newStringBytes);
		buffIn.order(ByteOrder.LITTLE_ENDIAN);
		
		while (buffIn.hasRemaining())
		{
			byte b = buffIn.get();
			
			if (b == START_BYTE)
				try {
					processPacket(buffIn, buffOut);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else
				buffOut.put(b);
		}
				
		try {
			return new String(newStringBytes, 0, buffOut.position(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		}
		catch (Exception e)
		{try {
			return "<ERROR Parsing: "+ new String(stringBytes, "UTF-8")+">";
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}}
		return "ERROR";
	}

	private static void processPacket(ByteBuffer buffIn, ByteBuffer buffOut) throws UnsupportedEncodingException {
		int type = buffIn.get()&0xFF;
		int payloadSize = buffIn.get()&0xFF;		
		
		if (payloadSize <= 1)
		{
			switch (type)
			{
			case TYPE_NEWLINE:
				buffOut.put("\\n".getBytes("UTF-8"));
				break;
			}
			return;
		}
		
		if (payloadSize > buffIn.remaining())
			payloadSize = buffIn.remaining();
				
		byte[] payload = new byte[payloadSize];
		
		buffIn.get(payload);
		
		switch (type)
		{		
		case (byte) 223:	
			for (int i = 0; i < payload.length; i++)
				System.out.print(String.format("0x%x ", payload[i]));
			System.out.print("\n");

			break;
		case TYPE_INFO:
			if ((((int)payload[0])&0xFF) == 0xEB && (((int)payload[1])&0xFF) == 0x02)
				buffOut.put("<forename surname>".getBytes("UTF-8"));
			else
				buffOut.put("<value>".getBytes("UTF-8"));
			break;
		case TYPE_SPLIT:
			/*
			if (Arrays.equals(payload, forenamePayload))
			{
				buffOut.put("<forename>".getBytes("UTF-8"));
				break;
			}
			else if (Arrays.equals(payload, surnamePayload))
			{
				buffOut.put("<surname>".getBytes("UTF-8"));
				break;
			}				
			*/
			buffOut.put("<split:".getBytes("UTF-8"));
			
			int contentsSize = payload[1];						
			
			buffOut.put(String.format("[%d]", payload[1 + contentsSize + 1]).getBytes("UTF-8"));
			
			byte splitBuffer[] = new byte[contentsSize];
			System.arraycopy(payload, 2, splitBuffer, 0, splitBuffer.length-2);
			ByteBuffer splitBB = ByteBuffer.wrap(splitBuffer);
			splitBB.position(1);
			byte[] outSplitProcessBuffer = new byte[512];
			ByteBuffer outSplitProcessBB = ByteBuffer.wrap(outSplitProcessBuffer);
			outSplitProcessBuffer = parseFFXIVString(splitBuffer).getBytes();
			buffOut.put((new String(outSplitProcessBuffer, 0, outSplitProcessBuffer.length, "UTF-8").getBytes("UTF-8")));			
			
			buffOut.put(">".getBytes("UTF-8"));
			break;
		case (byte) 222:
			byte opt1[] = new byte[payload[4]-1];
			byte opt2[] = new byte[payload[4+payload[4]]-1];				
			
			System.arraycopy(payload, 4, opt1, 0, opt1.length);
			System.arraycopy(payload, 5+payload[3], opt2, 0, opt2.length);
			
			buffOut.put("<".getBytes("UTF-8"));
			if (opt1[0] == 0x02)
			{
				ByteBuffer optionPayload = ByteBuffer.wrap(opt1);
				optionPayload.get(); //Skip start flag
				processPacket(optionPayload, buffOut);
			}
			else
				buffOut.put(opt1);
			buffOut.put("/".getBytes("UTF-8"));
			if (opt2[0] == 0x02)
			{
				ByteBuffer optionPayload = ByteBuffer.wrap(opt1);
				optionPayload.get(); //Skip start flag
				processPacket(optionPayload, buffOut);
			}
			else
				buffOut.put(opt2);
			buffOut.put(">".getBytes("UTF-8"));						
			break;
		case TYPE_ITALICS:
			if (payload[0] == 2)
				buffOut.put("<i>".getBytes("UTF-8"));
			else
				buffOut.put("</i>".getBytes("UTF-8"));
			break;		
		case TYPE_UNKNOWN:
			break;
		case TYPE_COLOR_CHANGE:		
			if (payload[0] == -20)
				buffOut.put("</color>".getBytes("UTF-8"));
			else if (payload[0] == -2)
				buffOut.put(String.format("<color #%02X%02X%02X>", payload[2], payload[3], payload[4]).getBytes("UTF-8"));
			else buffOut.put("<color?>".getBytes("UTF-8"));
			break;
		case TYPE_UNKNOWN2:			
						
			byte unknownBuffer[] = new byte[payload[1]];
			System.arraycopy(payload, 2, unknownBuffer, 0, unknownBuffer.length);
			ByteBuffer unknownBB = ByteBuffer.wrap(unknownBuffer);
			unknownBB.position(1);
			byte[] outUnknownProcessBuffer = new byte[512];
			ByteBuffer outUnknownProcessBB = ByteBuffer.wrap(outUnknownProcessBuffer);
			processPacket(unknownBB, outUnknownProcessBB);
			buffOut.put((new String(outUnknownProcessBuffer, 0, outUnknownProcessBB.position(), "UTF-8").getBytes("UTF-8")));			
			
			break;
		case TYPE_REFERENCE:
			byte exdName[] = new byte[payload[1]-1];
			System.arraycopy(payload, 2, exdName, 0, exdName.length);
			buffOut.put(String.format("<ref:%s>", new String(exdName)).getBytes("UTF-8"));
			break;
		case TYPE_REFERENCE2:
			exdName = new byte[payload[6]-1];
			System.arraycopy(payload, 7, exdName, 0, exdName.length);
			buffOut.put(String.format("<ref:%s>", new String(exdName)).getBytes("UTF-8"));
			break;
		case TYPE_IF:
			int pos1 = 2;
			String switchString1 = "<if:";
			
			if((((int)payload[0])&0xFF) == 0xDB){
				buffOut.put("<if:#s>".getBytes("UTF-8"));
				break;
			}
			//if ((((int)payload[0])&0xFF) == 0xE9 || (((int)payload[0])&0xFF) == 0xE3)
			//{
				while (true)
				{
					//Skip till hit marker
						for (int i = 0; i <= 4; i++)
					{
						
						if (payload[pos1] == -1)
							break;
						else
							pos1++;
					}
					pos1++;
					int stringSize = (((int)payload[pos1])&0xFF);											
					pos1++;
					
					if(stringSize == 1)
					{
						switchString1 += "none";
						if (payload[pos1] == 3 || payload[pos1] != -1 || (pos1+1 < payload.length && payload[pos1+1] == 3))
							break;
						else
						{
							switchString1 +="/";
							continue;
						}
					}
					
					byte switchBuffer[] = new byte[stringSize-1];
					System.arraycopy(payload, pos1, switchBuffer, 0, stringSize-1);
					if (switchBuffer[0] == 0x02)
					{
						ByteBuffer switchBB = ByteBuffer.wrap(switchBuffer);
						switchBB.position(1);
						byte[] outProcessBuffer = new byte[512];
						ByteBuffer outProcessBB = ByteBuffer.wrap(outProcessBuffer);
						outProcessBuffer = parseFFXIVString(switchBuffer).getBytes();
						switchString1 += new String(outProcessBuffer, 0, outProcessBuffer.length, "UTF-8");
					}
					else
						switchString1 += new String(switchBuffer, "UTF-8");
					pos1+=stringSize-1;
					if (payload[pos1] != -1 && (payload[pos1] == 3 || (pos1+1 < payload.length && payload[pos1+1] == 3)))
						break;				
					switchString1 += "/";				
				}
				buffOut.put((switchString1+">").getBytes("UTF-8"));
			/*}
			/*
			else
				buffOut.put("<if?>".getBytes("UTF-8"));*/
			break;
		case TYPE_SWITCH:
			int pos2 = 1;
			String switchString2 = "<switch:";
		
			if (payload[0] == -35 || payload[0] == -24){
				if (payload[0] == -24)
					pos2++;
				while (true)
				{					
					pos2++;
					int stringSize = payload[pos2];
					pos2++;
					if (stringSize-1 != 0){
						byte switchBuffer[] = new byte[stringSize-1];
						System.arraycopy(payload, pos2, switchBuffer, 0, stringSize-1);
						if (switchBuffer[0] == 0x02)
						{
							ByteBuffer switchBB = ByteBuffer.wrap(switchBuffer);
							switchBB.position(1);
							byte[] outProcessBuffer = new byte[512];
							ByteBuffer outProcessBB = ByteBuffer.wrap(outProcessBuffer);
							processPacket(switchBB, outProcessBB);
							switchString2 += new String(outProcessBuffer, 0, outProcessBB.position(), "UTF-8");
						}
						else
							switchString2 += new String(switchBuffer, "UTF-8");
					}
					pos2+=stringSize-1;
					if (payload[pos2] == 0x03)
						break;				
					switchString2 += "/";
				}
			}
			else if (payload[0] == -37)
			{				
				switchString2 += "?";
			}
		
			buffOut.put((switchString2+">").getBytes("UTF-8"));
			break;
		case TYPE_NEWLINE:
			buffOut.put("\\n".getBytes("UTF-8"));
			break;
		case TYPE_ITEM_LOOKUP:
			buffOut.put("<item>".getBytes("UTF-8"));
			break;
		case TYPE_ICON1:
		case TYPE_ICON2:
			buffOut.put(String.format("<icon:%d>", payload[0]).getBytes("UTF-8"));
			break;
		case TYPE_DASH:
			buffOut.put("-".getBytes("UTF-8"));
			break;
		case TYPE_SERVER_VALUE0:		
		case TYPE_SERVER_VALUE2:
		//case TYPE_SERVER_VALUE3:
		case TYPE_SERVER_VALUE3:
		//case TYPE_SERVER_VALUE5:
			buffOut.put("<value>".getBytes("UTF-8"));
			break;
		default:
			String unknownMsg = String.format("<?0x%x>", type);
			buffOut.put(unknownMsg.getBytes("UTF-8"));
			break;
		}
		
	}
	
}		

