package com.fragmenterworks.ffxivextract.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import com.fragmenterworks.ffxivextract.storage.HashDatabase;
import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.gui.components.EXDF_View;
import com.fragmenterworks.ffxivextract.models.EXHF_File;
import com.fragmenterworks.ffxivextract.models.Model;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;

public class EXD_Searcher {

	public static void findExhHashes()
	{
		System.out.println("Opening Root.exl");
		
		byte[] rootData = null;
		SqPack_IndexFile index = null;
		try {
			index = new SqPack_IndexFile(Constants.datPath + "\\game\\sqpack\\ffxiv\\0a0000.win32.index", true);
			rootData = index.extractFile("exd/root.exl");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (rootData == null)
			return;
		
		System.out.println("Root.exl file loaded, starting search.");
		
		try {
			InputStream in  = new ByteArrayInputStream(rootData);
			StringBuilder sBuilder = new StringBuilder();
			
			HashDatabase.beginConnection();
			try {
				HashDatabase.setAutoCommit(false);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			boolean pastHeader = false;
			boolean readingString = false;
			while(true)
			{
				int b = in.read();
				
				//Get Past Header
				if (!pastHeader && b == 0x0D)
				{
					int b2 = in.read();
					if (b2 == 0x0A)
					{
						pastHeader = true;
						readingString = true;
						continue;
					}
					continue;
				}
				else if (!pastHeader)
					continue;
				
				if (b == -1)
					break;
											
				if (readingString && b == ',')
				{
					readingString = false;
					String exhName = String.format("exd/%s.exh", sBuilder.toString());
					HashDatabase.addPathToDB(exhName, "0a0000", HashDatabase.globalConnection);
					System.out.println("Found: " + exhName);
					sBuilder.setLength(0);
					continue;
				}
				else if (!readingString && b == 0x0A)
				{
					readingString = true;
					continue;
				}
				else if (readingString)				
					sBuilder.append((char)b);				
								
			}
			try {
				HashDatabase.commit();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			HashDatabase.closeConnection();
			in.close();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		System.out.println("Done searching for exhs");
	}
	
	public static void findMusicHashes()
	{
		System.out.println("Opening bgm.exh");
		
		byte[] exhData = null;
		SqPack_IndexFile index = null;
		try {
			index = new SqPack_IndexFile(Constants.datPath + "\\game\\sqpack\\ffxiv\\0a0000.win32.index", true);
			exhData = index.extractFile("exd/bgm.exh");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (exhData == null)
			return;
		
		EXDF_View viewer = null;
		try {
			viewer = new EXDF_View(index, "exd/bgm.exh", new EXHF_File(exhData));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		if (viewer == null)
			return;
		
		System.out.println("bgm.exh file loaded, filling db.");
		
		HashDatabase.beginConnection();
		try {
			HashDatabase.setAutoCommit(false);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (int i = 1; i < viewer.getTable().getRowCount(); i++)
		{
			String path = String.format("%s",(String) viewer.getTable().getValueAt(i, 1));
			
			if (path == null || path.isEmpty())
				continue;
			
			String archive = path.contains("ex1") ? "0c0100" : "0c0000";
			
			HashDatabase.addPathToDB(path, archive, HashDatabase.globalConnection);
			System.out.println("Found: " + path);
		}
		
		try {
			HashDatabase.commit();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashDatabase.closeConnection();
	
		
		System.out.println("Done searching for music.");
	}
	
	public static void findMapPaths() throws IOException
	{
		
	}
	
	public static void getModelsFromModelChara(String path)
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			
			while(true){
				String in = reader.readLine();
				if (in == null)
					break;
				String[] split = in.split(",");
				
				int id = Integer.parseInt(split[1]);
				int type = Integer.parseInt(split[3]);
				int model = Integer.parseInt(split[4]);
				int variant = Integer.parseInt(split[5]);
					
				String typePath = "";
				String imcPath = null, modelPath = null, skelPath = null;
				
				if (type != 3)
					type = 20;
				
				switch(type){
				case 3:
					typePath = "chara/monster/m";
					imcPath = String.format("%s%04d/obj/body/b%04d/b%04d.imc", typePath,id,model,model);
					modelPath = String.format("%s%04d/obj/body/b%04d/model/m%04db%04d.mdl", typePath,id,model,id,model);
					System.out.println(imcPath);
					System.out.println(modelPath);								
					HashDatabase.addPathToDB(imcPath, "040000");
					HashDatabase.addPathToDB(modelPath, "040000");
					
					skelPath = String.format("%s%04d/skeleton/base/b%04d/eid_m%04db%04d.eid", typePath,id,model,id,model);
					System.out.println(skelPath);
					HashDatabase.addPathToDB(skelPath, "040000");
					skelPath = String.format("%s%04d/skeleton/base/b%04d/skl_m%04db%04d.sklp", typePath,id,model,id,model);
					System.out.println(skelPath);
					HashDatabase.addPathToDB(skelPath, "040000");
					skelPath = String.format("%s%04d/skeleton/base/b%04d/skl_m%04db%04d.sklb", typePath,id,model,id,model);
					System.out.println(skelPath);
					skelPath = String.format("%s%04d/animation/a%04d/bt_common/resident/monster.pap", typePath,id,0);
					System.out.println(skelPath);
					HashDatabase.addPathToDB(skelPath, "040000");
					skelPath = String.format("%s%04d/animation/a%04d/bt_common/event/event_wandering_action.pap", typePath,id,0);
					System.out.println(skelPath);
					HashDatabase.addPathToDB(skelPath, "040000");
					skelPath = String.format("%s%04d/animation/a%04d/bt_common/mon_sp/m%04d/mon_sp001.pap", typePath,id,0,id);
					System.out.println(skelPath);
					HashDatabase.addPathToDB(skelPath, "040000");
					break;
				case 4:
					typePath = "chara/demihuman/d";
					imcPath = String.format("%s%04d/obj/equipment/e%04d/e%04d.imc", typePath,id,model,model);
					System.out.println(imcPath);
					HashDatabase.addPathToDB(imcPath, "040000");
					
					modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_met.mdl", typePath,id,model,id,model);					
					System.out.println(modelPath);												
					HashDatabase.addPathToDB(modelPath, "040000");
					modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_top.mdl", typePath,id,model,id,model);					
					System.out.println(modelPath);												
					HashDatabase.addPathToDB(modelPath, "040000");
					modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_dwn.mdl", typePath,id,model,id,model);					
					System.out.println(modelPath);												
					HashDatabase.addPathToDB(modelPath, "040000");
					modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_sho.mdl", typePath,id,model,id,model);					
					System.out.println(modelPath);												
					HashDatabase.addPathToDB(modelPath, "040000");
					break;
				}
				
					
			}
			reader.close();			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getModels(String path)
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			BufferedWriter writer = new BufferedWriter(new FileWriter(path+"out.txt"));
			
			while(true){
				String in = reader.readLine();
				if (in == null)
					break;
				String[] split = in.split(":");
				
				String model1 = split[0];
				String model2 = split[1];
				
				//Model1
				if (!model1.equals("0, 0, 0, 0")){
					String[] model1Split = model1.split(",");
					int section1 = Integer.parseInt(model1Split[3]);
					int modelNum1 = Integer.parseInt(model1Split[2]);
					int variant1 = Integer.parseInt(model1Split[1]);				
					
					String type1 = null;
					
					if (section1 < 30)
						type1 = "chara/accessory/";
					else if (section1 < 3)
						type1 = "chara/equipment/";
					else if (section1 < 3)
						type1 = "chara/weapon/";
					
					String imcPath1 = String.format("");
					String modelPath1 = String.format("");
					String materialPath1 = String.format("");
					String texturePath1 = String.format("");
				}
				//Model2
				if (!model2.equals("0, 0, 0, 0")){
					String[] model2Split = model2.split(",");
					int section2 = Integer.parseInt(model2Split[3]);
					int modelNum2 = Integer.parseInt(model2Split[2]);
					int variant2 = Integer.parseInt(model2Split[1]);
					
					String type2 = null;
					
					if (section2 < 30)
						type2 = "chara/accessory/";
					else if (section2 < 3)
						type2 = "chara/equipment/";
					else if (section2 < 3)
						type2 = "chara/weapon/";
					
					String imcPath2 = String.format("%s%04d.imc");
					String modelPath2 = String.format("%s%04d.mdl");
					String materialPath2 = String.format("texture/a");
					String texturePath2 = String.format("texture/a");
				}
			}
			reader.close();
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void openEveryModel()
	{
		try {
			SqPack_IndexFile currentIndex = new SqPack_IndexFile("c:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\040000.win32.index", true);
			
			for (int i = 0; i < currentIndex.getPackFolders().length; i++)
			{
					SqPack_Folder folder = currentIndex.getPackFolders()[i];
					for (int j = 0; j < folder.getFiles().length; j++)
					{
						if (folder.getFiles()[j].getName().contains(".mdl"))
						{
							System.out.println("->Getting model " + folder.getFiles()[j].getName());
							Model m = new Model(folder.getName() + "/" + folder.getFiles()[j].getName(), currentIndex, currentIndex.extractFile(folder.getFiles()[j].dataoffset, null));
							for (int x = 0; x < m.getNumVariants(); x++)
								m.loadVariant(x);
						}
					}
			}
						
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	public static void findStains()
	{
		try {
			SqPack_IndexFile currentIndex = new SqPack_IndexFile("e:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\040000.win32.index", true);
			
			for (int i = 0; i < currentIndex.getPackFolders().length; i++)
			{
					SqPack_Folder folder = currentIndex.getPackFolders()[i];
					String folderName = folder.getName();
					if (folderName.contains("equipment") && folderName.contains("material/v"))
					{											
						String newfolder = folderName + "/staining";
						//Check if exists
						int folderHash = HashDatabase.computeCRC(newfolder.getBytes(), 0, newfolder.getBytes().length);
						for (int j = 0; j < currentIndex.getPackFolders().length; j++)
						{							
							SqPack_Folder folder2 = currentIndex.getPackFolders()[j];
							if (folder2.getId() == folderHash)
							{
								
								for (int y = 0; y < folder2.getFiles().length; y++)
								{
									SqPack_File file = folder2.getFiles()[y];
								
									if (!file.getName().endsWith(".tex") && !file.getName().endsWith(".mtrl"))
										continue;
								
									if (file.getName().contains(".tex")){
										for (int x = 1; x <= 85; x++)
											HashDatabase.addPathToDB(newfolder + "/" + file.getName().replace(".tex", String.format("_s%04d.tex", x)), "040000");
										
										for (int x = 101; x <= 120; x++)
											HashDatabase.addPathToDB(newfolder + "/" + file.getName().replace(".tex", String.format("_s%04d.tex", x)), "040000");
									}
									else if (file.getName().contains(".mtrl")){
										for (int x = 1; x <= 85; x++)
											HashDatabase.addPathToDB(newfolder + "/" + file.getName().replace(".mtrl", String.format("_s%04d.mtrl", x)), "040000");
										
										for (int x = 101; x <= 120; x++)
											HashDatabase.addPathToDB(newfolder + "/" + file.getName().replace(".mtrl", String.format("_s%04d.mtrl", x)), "040000");
									}
								}
								
							}
						}
					}
			}
						
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
	
}
