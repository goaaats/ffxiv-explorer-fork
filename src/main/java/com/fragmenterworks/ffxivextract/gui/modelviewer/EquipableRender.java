package com.fragmenterworks.ffxivextract.gui.modelviewer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.fragmenterworks.ffxivextract.storage.HashDatabase;

import com.fragmenterworks.ffxivextract.models.Model;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;

public class EquipableRender {

	public static final int MET = 0;
	public static final int TOP = 1;
	public static final int GLV = 2;
	public static final int DWN = 3;
	public static final int SHO = 4;
	public static final int WEAPON1 = 5;
	public static final int WEAPON2 = 6;
	public static final int NEK = 7;
	public static final int WRS = 8;
	public static final int RIR = 9;
	public static final int RIL = 10;
	
	public Model met;
	public Model top;
	public Model glv;
	public Model dwn;
	public Model sho;
	public Model weapon1;
	public Model weapon2;
	public Model nek;
	public Model wrs;
	public Model rir;
	public Model ril;

	public void setModel(int type, SqPack_IndexFile modelIndexFile, String modelPath, int varient) throws FileNotFoundException, IOException {
		byte[] modelData = modelIndexFile.extractFile(modelPath);
		
		if (modelData == null)
			return;
		
		System.out.println("Adding Entry: " + modelPath);
		HashDatabase.addPathToDB(modelPath, "040000");
		
		Model model = new Model(modelPath,modelIndexFile,modelData, modelIndexFile.getEndian());
		model.loadVariant(varient);
		
		switch (type)
		{
		case 0:
			met = model;
			break;
		case 1:
			top = model;
			break;
		case 2:
			glv = model;
			break;
		case 3:
			dwn = model;
			break;
		case 4:
			sho = model;
			break;
		case 5:
			weapon1 = model;
			break;
		case 6:
			weapon2 = model;
			break;
		case 7:
			nek = model;
			break;
		case 8:
			wrs = model;
			break;
		case 9:
			rir = model;
			break;
		case 10:
			ril = model;
			break;
		}
	}

	public ArrayList<Model> getModels() {
		ArrayList<Model> models = new ArrayList<Model>();

		if (met != null)
			models.add(met);
		if (top != null)
			models.add(top);
		if (glv != null)
			models.add(glv);
		if (dwn != null)
			models.add(dwn);
		if (sho != null)
			models.add(sho);
		if (weapon1 != null)
			models.add(weapon1);
		if (weapon2 != null)
			models.add(weapon2);
		if (nek != null)
			models.add(nek);
		if (wrs != null)
			models.add(wrs);
		if (rir != null)
			models.add(rir);
		if (ril != null)
			models.add(ril);

		return models;
	}
}
