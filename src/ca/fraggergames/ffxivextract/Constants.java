package ca.fraggergames.ffxivextract;

import javax.swing.UIManager;

public class Constants {
	
	public static final String APPNAME = "FFXIV 2.0 Data Explorer";
	public static final String VERSION = "v0.11";
	public static final String COMMIT = "07735cbbae203569a0cefe006bd6104c71653377";
	public static boolean DEBUG = false;
	public static boolean EASTER_EGG = false;
	
	public static final String[] iconList = {
		"000000", "000ED81", "0010235", "0010236" , "0010237",
		"0010249", "001024A", "001024B", "001025D", "001025E", 
		"001025F", "00101E5", "00101E6", "00101E7", "00101F9",
		"00101FA", "00101FB", "001020D", "001020E", "001020F",
		"00007DD", "0000841", "00008A5", "0000908", "000096D",
		"00009D0", "0000A35", "0000A98", "0010221", "0010222",
		"0010271", "0010272", "0010273", "0010274", "0010275",
		"0010276", "0010277", "0010278", "0010279", "001027A",
		"001027B", "0010285", "0010278", "0010286", "0010287",
		"0010288", "0010289", "001028A", "001028B", "001028C",
		"001028D", "001028E", "00102FD"
	};
	
	public static void setUIFont (javax.swing.plaf.FontUIResource f){
	    java.util.Enumeration keys = UIManager.getDefaults().keys();
	    while (keys.hasMoreElements()) {
	      Object key = keys.nextElement();
	      Object value = UIManager.get (key);
	      if (value != null && value instanceof javax.swing.plaf.FontUIResource)
	        UIManager.put (key, f);
	      }
	    } 
}
	