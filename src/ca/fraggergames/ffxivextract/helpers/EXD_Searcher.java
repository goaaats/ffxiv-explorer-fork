package ca.fraggergames.ffxivextract.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

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
				
				String model1 = split[10];
				String model2 = split[11];
				
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
}
