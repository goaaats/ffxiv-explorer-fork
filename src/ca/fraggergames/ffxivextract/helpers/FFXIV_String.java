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
	final static int TYPE_SERVER_VALUE0 = 0x20;
	final static int TYPE_SERVER_VALUE1 = 0x21;
	final static int TYPE_SERVER_VALUE2 = 0x22;
	final static int TYPE_SERVER_VALUE3 = 0x24;
	final static int TYPE_SERVER_VALUE4 = 0x25;
	final static int TYPE_ICON1 = 0x12;
	final static int TYPE_ICON2 = 0x1E;
	final static int TYPE_DASH = 0x1F;		
	
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
			else if (payload[0] == -2)
				buffOut.put(String.format("<color #%02X%02X%02X>", payload[2], payload[3], payload[4]).getBytes("UTF-8"));
			else buffOut.put("<color?>".getBytes("UTF-8"));
			break;
		case TYPE_REFERENCE:
			byte exdName[] = new byte[payload[1]-1];
			System.arraycopy(payload, 2, exdName, 0, exdName.length);
			buffOut.put(String.format("<ref:%s>", new String(exdName)).getBytes("UTF-8"));
			break;
		case TYPE_IF:
			int pos1 = 2;
			String ifString = "<if:";
			
			if ((((int)payload[0])&0xFF) == 0xE9)
			{
				while (true)
				{
					if (payload[pos1] == -1)
						pos1++;
					int stringSize = payload[pos1];
					pos1++;
					byte switchBuffer[] = new byte[stringSize-1];
					System.arraycopy(payload, pos1, switchBuffer, 0, stringSize-1);
					if (switchBuffer[0] == 0x02)
					{
						ByteBuffer switchBB = ByteBuffer.wrap(switchBuffer);
						switchBB.position(1);
						byte[] outProcessBuffer = new byte[512];
						ByteBuffer outProcessBB = ByteBuffer.wrap(outProcessBuffer);
						processPacket(switchBB, outProcessBB);
						ifString += new String(outProcessBuffer, 0, outProcessBB.position(), "UTF-8");
					}
					else
						ifString += new String(switchBuffer, "UTF-8");
					pos1+=stringSize-1;
					if (payload[pos1] == 3 && ((((int)payload[pos1-1])&0xFF) != 0xFF) || (pos1+1 <= payload.length-1 &&payload[pos1+1] == 3))
						break;				
					ifString += "/";				
				}
				buffOut.put((ifString+">").getBytes("UTF-8"));
			}
			else
				buffOut.put("<if?>".getBytes("UTF-8"));
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

