package ca.fraggergames.ffxivextract.helpers;

import java.io.FileNotFoundException;
import java.io.IOException;

import ca.fraggergames.ffxivextract.models.Material;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

public class Utils {

	/**
     * Converts a single precision (32 bit) floating point value
     * into half precision (16 bit).
     *
     * Source: http://www.fox-toolkit.org/ftp/fasthalffloatconversion.pdf
     *
     * @param half The half floating point value as a short.
     * @return floating point value of the half.
     */
    public static float convertHalfToFloat(short half){
        switch ((int)half){
            case 0x0000:
                return 0f;
            case 0x8000:
                return -0f;
            case 0x7c00:
                return Float.POSITIVE_INFINITY;
            case 0xfc00:
                return Float.NEGATIVE_INFINITY;
            // TODO: Support for NaN?
            default:
                return  Float.intBitsToFloat(((half & 0x8000) << 16)
                                          | (((half & 0x7c00) + 0x1C000) << 13)
                                          |  ((half & 0x03FF) << 13));
        }
    }

    public static short convertFloatToHalf(float flt){
        if (Float.isNaN(flt)){
            throw new UnsupportedOperationException("NaN to half conversion not supported!");
        }else if (flt == Float.POSITIVE_INFINITY){
            return (short) 0x7c00;
        }else if (flt == Float.NEGATIVE_INFINITY){
            return (short) 0xfc00;
        }else if (flt == 0f){
            return (short) 0x0000;
        }else if (flt == -0f){
            return (short) 0x8000;
        }else if (flt > 65504f){
            // max value supported by half float
            return 0x7bff;
        }else if (flt < -65504f){
            return (short)(0x7bff | 0x8000);
        }else if (flt > 0f && flt < 5.96046E-8f){
            return 0x0001;
        }else if (flt < 0f && flt > -5.96046E-8f){
            return (short) 0x8001;
        }

        int f = Float.floatToIntBits(flt);
        return (short)(( (f >> 16) & 0x8000)
             | ( ( ( (f & 0x7f800000) - 0x38000000) >> 13) & 0x7c00)
             | ( (f >> 13) & 0x03ff));
    }
	
    public static String getRegexpFromFormatString(String format)
    {
        String toReturn = format;

        // escape some special regexp chars
        toReturn = toReturn.replaceAll("\\.", "\\\\.");
        toReturn = toReturn.replaceAll("\\!", "\\\\!");

        if (toReturn.indexOf("%") >= 0)
        {
            toReturn = toReturn.replaceAll("%s", "[\\\\w]+"); //accepts 0-9 A-Z a-z _

            while (toReturn.matches(".*%([0-9]+)[d]{1}.*"))
            {
                String digitStr = toReturn.replaceFirst(".*%([0-9]+)[d]{1}.*", "$1");
                int numDigits = Integer.parseInt(digitStr);
                toReturn = toReturn.replaceFirst("(.*)(%[0-9]+[d]{1})(.*)", "$1[0-9]{" + numDigits + "}$3");
            }
        }

        return "^" + toReturn + "$";
    }
    
    
    
}
