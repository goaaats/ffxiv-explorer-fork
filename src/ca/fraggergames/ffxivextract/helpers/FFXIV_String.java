package ca.fraggergames.ffxivextract.helpers;

import java.io.UnsupportedEncodingException;

public class FFXIV_String {

	String original, parsed;
	public FFXIV_String(String string) {
		
		original = string;
		try {
			StringBuilder builder = new StringBuilder(original);
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
			
			parsed = new String(builder.toString().getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public FFXIV_String(byte[] string) {
		this(new String(string));
	}

	private void processToken(int tokenType, byte[] tokenBuffer) {
		
	}

	@Override
	public String toString() {
		return parsed;
	}
}
