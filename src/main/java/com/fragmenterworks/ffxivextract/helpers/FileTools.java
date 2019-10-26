package com.fragmenterworks.ffxivextract.helpers;

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.Main;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;
import com.fragmenterworks.ffxivextract.models.Texture_File;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.prefs.Preferences;

/**
 * Created by Roze on 2017-06-17.
 *
 * @author Roze
 */
public class FileTools {

	static{
		if( Constants.datPath == null) {
			Preferences prefs = Preferences.userNodeForPackage(Main.class);
			Constants.datPath = prefs.get(Constants.PREF_DAT_PATH, null);
		}
	}


	public static BufferedImage getIcon(SqPack_IndexFile sqPak, int iconID){
		String file;
		if(sqPak == null){
			file = Constants.datPath;
		}else {
			file = sqPak.getPath();
			file = new File(file).getParentFile().getPath();
		}
		return getIcon(file, iconID);
	}

	public static BufferedImage getIcon(int iconID){
		return getIcon((String)null, iconID);
	}

	public static BufferedImage getIcon(String sqPakPath, int iconID){
		if(sqPakPath == null){
			sqPakPath = Constants.datPath;
		}
		String iconPath = String.format("ui/icon/%06d/%06d.tex", iconID - (iconID % 1000), iconID);
		System.out.println("IconPath="+iconPath+", iconID="+iconID);
		BufferedImage bi = getTexture(sqPakPath, iconPath);
		return bi;
	}


	public static byte[] getRaw(SqPack_IndexFile sqPak, String path){
		String file = sqPak.getPath();
		String sqpakpath = new File(file).getParentFile().getPath();
		return getRaw(sqpakpath, path);
	}

	public static byte[] getRaw(String path){
		return getRaw((String)null, path);
	}


	public static byte[] getRaw(String sqPakPath, String path){
		if(sqPakPath == null){
			sqPakPath = Constants.datPath;
		}
		String lowerpath = path.toLowerCase();
		String dat       = getDatByPath(lowerpath);

		if(!sqPakPath.endsWith(File.separator)){
			sqPakPath+= File.separator;
		}

		if(!sqPakPath.endsWith("\\game\\sqpack\\ffxiv\\")){
			sqPakPath+= "\\game\\sqpack\\ffxiv\\";
		}

		SqPack_IndexFile index = SqPack_IndexFile.createIndexFileForPath(sqPakPath + dat+".index", true);

		if(index != null) {
			try {
				return index.extractFile(lowerpath);
			} catch ( IOException e ) {
				e.printStackTrace();
			}
		}
		return new byte[0];
	}

	private static String getDatByPath(final String lowerpath) {
		String root = lowerpath.substring(0, lowerpath.indexOf("/"));
		String dat;
		switch ( root ) {
			case "exd": {
				dat = "0a0000";
				break;
			}
			case "game_script": {
				dat = "0b0000";
				break;
			}
			case "music": {
				dat = "0c0000";
				break;
			}
			case "common": {
				dat = "000000";
				break;
			}
			case "bgcommon": {
				dat = "010000";
				break;
			}
			case "bg": {
				dat = "020000";
				break;
			}
			case "cut": {
				dat = "030000";
				break;
			}
			case "chara": {
				dat = "040000";
				break;
			}
			case "shader": {
				dat = "050000";
				break;
			}
			case "ui": {
				dat = "060000";
				break;
			}
			case "sound": {
				dat = "070000";
				break;
			}
			case "vfx": {
				dat = "080000";
				break;
			}
			default: throw new RuntimeException("Unknown root: " + root);
		}
		dat+= ".win32";
		return dat;
	}

	public static BufferedImage getTexture(String path) {
		return getTexture(null, path);
	}

	public static BufferedImage getTexture(String sqPakPath, String path) {
		if(sqPakPath == null){
			sqPakPath = Constants.datPath;
		}
		byte[] data = getRaw(sqPakPath, path);

		//TODO: Random ULD stuff can be little-endian, no?
		Texture_File tf = new Texture_File(data, ByteOrder.LITTLE_ENDIAN);
		try {
			BufferedImage bf = tf.decode(0, new HashMap<>());
			return bf;
		} catch ( ImageDecoding.ImageDecodingException e ) {
			e.printStackTrace();
		}
		return null;

	}

}
