package ca.fraggergames.ffxivextract;

import java.sql.Connection;
import java.util.Enumeration;

import javax.swing.UIManager;

public class Constants {

	public static final String APPNAME = "FFXIV Data Explorer !EARLY COPY FOR VEKIEN!";
	public static final String VERSION = "v1.5.2B";
	public static final int APP_VERSION_CODE = 7;
	public static final int DB_VERSION_CODE = 8;	
	
	public static final String COMMIT = "32161031d61ec0efa02ada880db61af0826c79c0";
	public static boolean HAVOK_ENABLED = false;
	public static boolean DEBUG = false;
	public static boolean EASTER_EGG = false;

	public static Connection GLOBAL_CONN;

	public static final String URL_WEBSITE = "http://ffxivexplorer.fragmenterworks.com";
	public static final String URL_VERSION_CHECK = URL_WEBSITE
			+ "/version_check.json";
	public static final String URL_HASHLIST_FILE = URL_WEBSITE
			+ "/downloads/hashlist.db";

	public static final String DBFILE_NAME = "hashlist.db";

	public static final String PREF_FIRSTRUN = "pref_firstrun";
	public static final String PREF_LASTOPENED = "pref_lastopened";
	public static final String PREF_DO_DB_UPDATE = "pref_dbupdate";
	public static final String PREF_DAT_PATH = "pref_datpath";
	
	public static String datPath = null;

	// ///DEFAULT COLORS//////
	public static float defaultHairColor[] = { 0.2941176f, 0.2117647f,
			0.105882f, 1.0f };
	public static float defaultHighlightColor[] = { 0.650f, 0.502f, 0.392f,
			1.0f };
	public static float defaultEyeColor[] = { 0.0f, 0.302f, 0.0f, 1.0f };
	// ///DEFAULT COLORS//////

	public static final int[] macroIconList = {0, 66001, 66101, 66102, 66103, 66121,
			66122, 66123, 66141, 66142, 66143, 66021, 66022, 66023, 66041,
			66042, 66043, 66061, 66062, 66063, 2013, 2113, 2213, 2312, 2413,
			2512, 2613, 2712, 66081, 66082, 66083, 66161, 66162, 66163, 66164,
			66165, 66166, 66167, 66168, 66169, 66170, 66171, 66181, 66182,
			66183, 66184, 66185, 66186, 66187, 66188, 66189, 66190, 66191,
			66301, 66302, 66303, 66304, 66305, 66306, 66307, 66308, 66309,
			66310, 66311, 66312, 66313, 66314, 66315, 66316, 66317, 66318,
			66319, 66320, 66321, 66322, 66323, 66324, 66325, 66326, 20007,
			20009, 20010, 20012, 20011, 20008 };

	public static void setUIFont(javax.swing.plaf.FontUIResource f) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value != null
					&& value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, f);
		}
	}
}
