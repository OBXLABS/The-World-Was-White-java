/*
 * Copyright (c) 2012 All Right Reserved, Jason E. Lewis [http://obxlabs.net]
 */

package net.obxlabs.white;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * A layer of noisy snow.
 * @author Bruno
 */
public class Snow {
	PApplet p;			//parent applet
	PImage[] tiles;		//the tiles that form the whole layer
	int tileSize;		//size of the tiles
	int rows, cols;		//number of rows and columns
	
	/**
	 * Constructr.
	 * @param p parent applet
	 * @param w width
	 * @param h height
	 */
	public Snow(PApplet p, int w, int h) {
		this.p = p;
		p.noiseDetail(4, 0.25f);

		this.tileSize = 128;
		
		cols = PApplet.ceil(w/(float)tileSize);
		rows = PApplet.ceil(h/(float)tileSize);
		tiles = new PImage[cols*rows];
		
		int count = 0;
		
		//set the noise values
		int bloodMaxOpacity = 255;
		float bloodSize = 0.008f;
		float bloodSnowRatio = 0.35f;
		
		//generate the tiles
		for(int c = 0; c < cols; c++) {
			for(int r = 0; r < rows; r++) {
				tiles[c + r*cols] = p.createImage(tileSize, tileSize, PApplet.ARGB);
				tiles[c + r*cols].loadPixels();
				for(int y = 0; y < tileSize; y++) {
					for(int x = 0; x < tileSize; x++) {
						tiles[c + r*cols].pixels[x + y*tiles[c + r*cols].width] = 
								p.color(255, PApplet.map(PApplet.constrain(255*(
										p.noise((c*tileSize+x)*0.8f, (r*tileSize+y)*0.8f, p.noise(count++)*10000) * 1.0f *
										PApplet.constrain(PApplet.map(p.noise((c*tileSize+x)*bloodSize, (r*tileSize+y)*bloodSize), bloodSnowRatio, 1, 0, 1), 0, 1) +
										p.noise((c*tileSize+x)*0.001f, (r*tileSize+y)*0.001f)*0.1f
										), 0, 255), 0, 255, 0, bloodMaxOpacity)
								);
					}
				}
				tiles[c + r*cols].updatePixels();
			}
		}
	}
	
	/**
	 * Draw.
	 */
	public void draw() {
		for(int c = 0; c < cols; c++) {
			for(int r = 0; r < rows; r++) {
				p.image(tiles[c + r*cols], c*tileSize, r*tileSize);
			}
		}
	}
}
