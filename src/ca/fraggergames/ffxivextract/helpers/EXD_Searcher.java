package ca.fraggergames.ffxivextract.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import ca.fraggergames.ffxivextract.models.Model;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

public class EXD_Searcher {

	public static void saveEXL()
	{
		InputStream in;
		BufferedWriter writer = null;
		
		boolean readingName = true;
		
		try {
			in  = new FileInputStream("C:\\Users\\Filip\\Desktop\\exd\\root.exl");
			writer = new BufferedWriter(new FileWriter("./exddump2.txt"));
			
			while(true)
			{
				int b = in.read();
				
				if (b == -1)
					break;
				
				if (b == ',' || b == 0x0D)
				{
					if (b == 0x0D)
						in.read();
					if (readingName)
						writer.append("");
					readingName = !readingName;
				}
	
				System.out.println((char)b);
				
				if (readingName)
					writer.append((char)b);
			}
			in.close();
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	public static void saveEXDNames(SqPack_IndexFile currentIndexFile)
	{
		BufferedWriter writer = null;
		try {writer = new BufferedWriter(new FileWriter("./exddump.txt"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int numSaved = 0;
		String string = "TEXT_";
			for (int i = 0; i < currentIndexFile.getPackFolders().length; i++) {			
				SqPack_Folder f = currentIndexFile.getPackFolders()[i];
				for (int j = 0; j < f.getFiles().length; j++) {
					SqPack_File fi = f.getFiles()[j];
					byte[] data;
					try {
						data = currentIndexFile.extractFile(fi.dataoffset, null);
						if (data == null)
							continue;
						
						for (int i2 = 0; i2 < data.length - string.length(); i2++) {
							boolean exitFile = false;
							for (int j2 = 0; j2 < string.length(); j2++) {
								if (data[i2 + j2] == string.charAt(j2)) {
									if (j2 == string.length() - 1) {																								
										
										//Look for end
										int endString = 0;
										int underScoreCount = 0;
										for (int endSearch = i2; endSearch < data.length - string.length(); endSearch++)
										{																					
											if (data[endSearch] == '_')											
												underScoreCount++;
											
											if (underScoreCount >= 3)
											{
												endString = endSearch;
												break;
											}
										}										
	
										//Hack for last file
										if (endString == 0)
											endString = data.length-1;
										
										//Get full path
										String fullpath = new String(data, i2, endString-i2);
										fullpath = HashDatabase.getFolder(f.getId()) + "/" + fullpath.toLowerCase();
																				
										writer.write(fullpath + ".exh\r\n");
										writer.write(fullpath + "_0_en.exd\r\n");
										writer.write(fullpath + "_0_ja.exd\r\n");
										writer.write(fullpath + "_0_de.exd\r\n");
										writer.write(fullpath + "_0_fr.exd\r\n");
										System.out.println("=> "+ fullpath);
										exitFile = true;							
										numSaved++;
																										
									} else
										continue;
								} else
									break;
							}	
							if (exitFile)
								break;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
	
				}								
			}
			System.out.println("Saved " + numSaved +"names.");
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	public static void createEXDFiles(String path)
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			BufferedWriter writer = new BufferedWriter(new FileWriter(path+"out.txt"));
			
			while(true){
				String in = reader.readLine();
				if (in == null)
					break;
				in.replace("\n", "");
				in.replace("\r", "");
				writer.write("exd/"+in + ".exh\r\n");
				writer.write("exd/"+in + "_0_en.exd\r\n");
				writer.write("exd/"+in + "_0_ja.exd\r\n");
				writer.write("exd/"+in + "_0_de.exd\r\n");
				writer.write("exd/"+in + "_0_fr.exd\r\n");
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
					HashDatabase.addPathToDB(imcPath);
					HashDatabase.addPathToDB(modelPath);
					
					skelPath = String.format("%s%04d/skeleton/base/b%04d/eid_m%04db%04d.eid", typePath,id,model,id,model);
					System.out.println(skelPath);
					HashDatabase.addPathToDB(skelPath);
					skelPath = String.format("%s%04d/skeleton/base/b%04d/skl_m%04db%04d.sklp", typePath,id,model,id,model);
					System.out.println(skelPath);
					HashDatabase.addPathToDB(skelPath);
					skelPath = String.format("%s%04d/skeleton/base/b%04d/skl_m%04db%04d.sklb", typePath,id,model,id,model);
					System.out.println(skelPath);
					skelPath = String.format("%s%04d/animation/a%04d/bt_common/resident/monster.pap", typePath,id,0);
					System.out.println(skelPath);
					HashDatabase.addPathToDB(skelPath);
					skelPath = String.format("%s%04d/animation/a%04d/bt_common/event/event_wandering_action.pap", typePath,id,0);
					System.out.println(skelPath);
					HashDatabase.addPathToDB(skelPath);
					skelPath = String.format("%s%04d/animation/a%04d/bt_common/mon_sp/m%04d/mon_sp001.pap", typePath,id,0,id);
					System.out.println(skelPath);
					HashDatabase.addPathToDB(skelPath);
					break;
				case 4:
					typePath = "chara/demihuman/d";
					imcPath = String.format("%s%04d/obj/equipment/e%04d/e%04d.imc", typePath,id,model,model);
					System.out.println(imcPath);
					HashDatabase.addPathToDB(imcPath);
					
					modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_met.mdl", typePath,id,model,id,model);					
					System.out.println(modelPath);												
					HashDatabase.addPathToDB(modelPath);
					modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_top.mdl", typePath,id,model,id,model);					
					System.out.println(modelPath);												
					HashDatabase.addPathToDB(modelPath);
					modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_dwn.mdl", typePath,id,model,id,model);					
					System.out.println(modelPath);												
					HashDatabase.addPathToDB(modelPath);
					modelPath = String.format("%s%04d/obj/equipment/e%04d/model/d%04de%04d_sho.mdl", typePath,id,model,id,model);					
					System.out.println(modelPath);												
					HashDatabase.addPathToDB(modelPath);
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
	
	public static void generateMaps(String path) throws IOException
	{
		//String regions[] = {"f", "s", "w", "r", "l"};
		//String types[] = {"f", "t", "h", "r", "d"};		
		BufferedWriter writer = new BufferedWriter(new FileWriter(path+"out.txt"));
		/*
		for (int i = 0; i < regions.length; i++)
		{
			for (int i2 = 0; i2 < 5; i2++)
			{
				for (int i3 = 0; i3 < types.length; i3++)
				{
					for (int i4 = 0; i4 < 10; i4++)
					{
						for (int floor = 0; floor < 8; floor++)
						{
							String spath = String.format("ui/map/%s%d%s%d/%02d/", regions[i], i2, types[i3], i4, floor);
							writer.write(spath + String.format("%s%d%s%d%02dd.tex\r\n", regions[i], i2, types[i3], i4, floor));
							writer.write(spath + String.format("%s%d%s%d%02d_s.tex\r\n", regions[i], i2, types[i3], i4, floor));
							writer.write(spath + String.format("%s%d%s%d%02ds_s.tex\r\n", regions[i], i2, types[i3], i4, floor));
							writer.write(spath + String.format("%s%d%s%d%02d_m.tex\r\n", regions[i], i2, types[i3], i4, floor));
							writer.write(spath + String.format("%s%d%s%d%02dm_m.tex\r\n", regions[i], i2, types[i3], i4, floor));
							System.out.println("Creating: " + spath + String.format("%s%d%s%d%02dd.tex\r\n", regions[i], i2, types[i3], i4, floor));
						}
					}
				}
			}
		}*/
		
		writer.write("ui/map/default/00/default00m_m.tex\r\n");
		writer.write("ui/map/default/00/default00s_s.tex\r\n");
		writer.write("ui/map/default/00/default00_m.tex\r\n");
		
		writer.close();
	}
	
	public static void openEveryModel()
	{
		try {
			SqPack_IndexFile currentIndex = new SqPack_IndexFile("c:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\040000.win32.index");
			
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
								m.loadMaterials(x);
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
			SqPack_IndexFile currentIndex = new SqPack_IndexFile("e:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\040000.win32.index");
			
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
											HashDatabase.addPathToDB(newfolder + "/" + file.getName().replace(".tex", String.format("_s%04d.tex", x)));
										
										for (int x = 101; x <= 120; x++)
											HashDatabase.addPathToDB(newfolder + "/" + file.getName().replace(".tex", String.format("_s%04d.tex", x)));
									}
									else if (file.getName().contains(".mtrl")){
										for (int x = 1; x <= 85; x++)
											HashDatabase.addPathToDB(newfolder + "/" + file.getName().replace(".mtrl", String.format("_s%04d.mtrl", x)));
										
										for (int x = 101; x <= 120; x++)
											HashDatabase.addPathToDB(newfolder + "/" + file.getName().replace(".mtrl", String.format("_s%04d.mtrl", x)));
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
