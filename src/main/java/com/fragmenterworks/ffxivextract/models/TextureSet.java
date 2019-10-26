package com.fragmenterworks.ffxivextract.models;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Roze on 2017-06-23.
 *
 * @author Roze
 */
public class TextureSet {

	private int index;

	private final List<TextureRegion> regions = new ArrayList<>();


	public TextureSet(final int index) {

		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public TextureRegion getRegion(int index){
		return regions.get(index);
	}


	public void clear(){
		regions.clear();
	}

	public void addRegion(final TextureRegion textureRegion) {
		synchronized ( regions ){
			regions.add(textureRegion);
		}
	}
}
