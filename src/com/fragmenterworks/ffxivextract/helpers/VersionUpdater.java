package com.fragmenterworks.ffxivextract.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import com.fragmenterworks.ffxivextract.Constants;
import com.google.gson.Gson;

public class VersionUpdater {


	public static VersionCheckObject checkForUpdates()
	{
		VersionCheckObject verCheck = null;		
	    try {
	    	InputStream is = new URL(Constants.URL_VERSION_CHECK).openStream();
	    	Gson gson = new Gson();	    	
	    	BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	    	verCheck = gson.fromJson(rd, VersionCheckObject.class);	      
	    } 
	    catch (IOException e) {
			e.printStackTrace();
		}
		return verCheck;	    	    
	}
	
	public static class VersionCheckObject{
		public Integer currentAppVer;
		public Integer currentDbVer;
		public String appUpdateDate;
		public String dbUpdateDate;
		public String patchDesc;
	}
}
