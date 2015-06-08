/*
 * Copyright (c) 2012 All Right Reserved, Jason E. Lewis [http://obxlabs.net]
 */

package net.obxlabs.white;


import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.opengl.PGraphicsOpenGL;

/**
 * Blood layer.
 * A blood-like textured generated using Perlin noise that evolves over time.
 * @author Bruno
 */
public class Blood {
  PApplet p;						//parent applet
  long age;                         //age of the texture (millis)
  PGraphics tex;                    //blood texture
  int texSize;                      //size of texture

  PGraphicsOpenGL pgl;				//opengl
  GL gl;
  GLU glu;
  
  int minR, minG, minB;				//minimum color values
  int rangeR, rangeG, rangeB;		//range of color values
  float minNoise;					//minimum noise
  float maxNoise;					//maximum noise
  
  float mass;						//general blood mass
  
  String snd;						//id of sound sample
  
  boolean NO_SCALE = false;         //true to render the texture 1:1, false to fill window

  /**
   * Constructor
   * @param parent
   */
  public Blood(PApplet parent) {
	p = parent;
    age = 0;
    texSize = 256;
    tex = p.createGraphics(texSize, texSize, PGraphics.P2D);
    minR = 150;
    minG = 0;
    minB = 23;
    rangeR = 255 - minR;
    rangeG = 255 - minG;
    rangeB = 255 - minB;
    minNoise = 1.0f;
    maxNoise = 1.0f;
    mass = 0.666f;
  }
  
  /**
   * Set mass.
   * @param m mass
   */
  public void setMass(float m) {
	  mass = m;
  }
  
  /**
   * Set audio sample id.
   * @param s sample id
   */
  public void setAudio(String s) {
	snd = s;
	White.soundManager.repeat(snd);
  }

  /** Draw fullscreen. */
  public void draw() { draw(0, 0, p.width, p.height); }

  /**
   * Draw blood.
   * @param x x position
   * @param y y position
   * @param w width
   * @param h height
   */
  public void draw(int x, int y, int w, int h) {
    pgl = (PGraphicsOpenGL) p.g;
    gl = pgl.beginPGL().gl;
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
    gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
    pgl.endPGL();

    p.pushMatrix();
    p.translate(x, y);
    if (NO_SCALE) p.scale(texSize, texSize);
    else p.scale(w, w);
    p.beginShape(PApplet.QUADS);
    p.textureMode(PApplet.NORMAL);
    p.texture(tex);
    p.vertex(-1,-1,0,0,0);
    p.vertex(1,-1,0,1,0);
    p.vertex(1,1,0,1,1);
    p.vertex(-1,1,0,0,1);
    p.endShape(PApplet.CLOSE);
    p.popMatrix();
  }
  
  /**
   * Update.
   * @param dt millis since last update
   * @param activity activity level
   */
  public void update(long dt, float activity) {
    age += dt;
  
    //adjust noise based on activity
    minNoise = 1 - activity;
    maxNoise = 1 - activity*mass;
    
    //fade audio based on activity
    White.soundManager.fade(snd, PApplet.constrain(PApplet.map(activity, 0.5f, 1.0f, 0, 1), 0, 1), 0);
    
    //update texture
    setNoise(tex);
  }
  
  /**
   * Fill texture with noise.
   * @param pg texture
   */
  public void setNoise(PGraphics pg) {
    p.noiseDetail(2, 0.5f);
    float fAge = age/10000f;
    float density = 0.05f;
    float value = 0;
    
    pg.loadPixels();    
    for(int y = 0; y < pg.height; y++) {
      for(int x = 0; x < pg.width; x++) {
    	value = 1 - PApplet.constrain(
    					PApplet.map(p.noise(x*density, y*density, fAge), minNoise, maxNoise, 0, 1)
    					, 0, 1);        
    	pg.pixels[y*pg.width+x] = p.color(minR + value*rangeR, minG + value*rangeG, minB + value*rangeB);
      }
    }
    pg.updatePixels();
  }
}
