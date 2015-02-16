package ca.fraggergames.ffxivextract.helpers;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class FFXIV_String {

	final static int START_BYTE = 0x02;
	final static int END_BYTE = 0x03;

	final static int TYPE_NEWLINE = 0x10;
	final static int TYPE_INFO = 0x29;
	final static int TYPE_REFERENCE = 0x28;
	final static int TYPE_IF = 0x08;
	final static int TYPE_SWITCH = 0x09;
	final static int TYPE_SPLIT = 0x2c;
	final static int TYPE_ITALICS = 0x1a;
	final static int TYPE_COLOR_CHANGE = 0x13;	
	final static int TYPE_SERVER_VALUE = 0x20;
	final static int TYPE_SERVER_VALUE2 = 0x24;
	
	final static int TYPE_ITEM_LOOKUP = 0x31;
	
	final static int INFO_NAME = 235;
	final static int INFO_GENDER = 233;
	
	public static String parseFFXIVString(byte[] stringBytes) {
		
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
		return "ERROR";
	}

	private static void processPacket(ByteBuffer buffIn, ByteBuffer buffOut) throws UnsupportedEncodingException {
		byte type = buffIn.get();
		byte payloadSize = buffIn.get();
		
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
		case TYPE_COLOR_CHANGE:		
			if (payload[0] == -20)
				buffOut.put("</color>".getBytes("UTF-8"));
			else
				buffOut.put(String.format("<color #%02X%02X%02X>", payload[2], payload[3], payload[4]).getBytes("UTF-8"));
			break;
		case TYPE_REFERENCE:
			byte exdName[] = new byte[payload[1]-1];
			System.arraycopy(payload, 2, exdName, 0, exdName.length);
			buffOut.put(String.format("<ref:%s,line:%d>", new String(exdName), payload[payload[1]+1]-1).getBytes("UTF-8"));
			break;
		case TYPE_SWITCH:
			buffOut.put("<switch>".getBytes("UTF-8"));
			break;
		case TYPE_NEWLINE:
			buffOut.put("</br>".getBytes("UTF-8"));
			break;
		case TYPE_ITEM_LOOKUP:
			buffOut.put("<item>".getBytes("UTF-8"));
			break;
		case TYPE_SERVER_VALUE:
		case TYPE_SERVER_VALUE2:
			buffOut.put("<value>".getBytes("UTF-8"));
			break;
		default:
			String unknownMsg = String.format("<Unknown Type 0x%x>", type);
			buffOut.put(unknownMsg.getBytes("UTF-8"));
			break;
		}
		
	}
	
}
			/*
			byte[] chars = string.getBytes();
						
			//Parse through for tokens and replace
			int s = 0, e = 0, deleteOffset = 0;
			boolean readingToken = false;
			int tokenCount = 0;
			int tokenType = -1, tokenSize = -1;
			byte[] tokenBuffer = null;
			for (int i = 0; i < chars.length; i++)
			{
				if (chars[i] == 02 && !readingToken)
				{
					s = i;		
					readingToken = true;
					tokenCount = 1;					
				}
				else if (readingToken)
				{
					switch (tokenCount)
					{
					case 1: //type
						tokenType = chars[i];
						break;
					case 2: //size
						tokenSize = chars[i];
						tokenBuffer = new byte[tokenSize-1];
						break;
					default:
						if (tokenCount - 3 >= tokenSize-1)
						{
							readingToken = false;
							processToken(tokenType, tokenBuffer);
							e = i+1;
							System.out.println("Deleting " + s + " to " + e);
							builder.delete(s-deleteOffset, e-deleteOffset);
							deleteOffset = e-s;
						}
						else
							tokenBuffer[tokenCount-3] = chars[i];
						break;						
					}					
					tokenCount++;			
				}
			}
			
			return new String(builder.toString().getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}	

	private static void processToken(int tokenType, byte[] tokenBuffer) {
		
	}
}
*/	

