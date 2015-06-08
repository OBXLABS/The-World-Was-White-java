/*
 * Copyright (c) 2012 All Right Reserved, Jason E. Lewis [http://obxlabs.net]
 */

package net.obxlabs.white;

import java.awt.Rectangle;

import org.apache.log4j.Logger;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * A word with some graphical properties that can fade in and out.
 */
public class Word {
	
	static Logger logger = Logger.getLogger(Word.class);
	
	//fading state
	static enum FadeState { FADE_IN, FADE_OUT, STABLE; }

	PApplet p;							//parent applet
	
	String value;						//textual value

	float opacity;						//opacity
	float fadeInSpeed, fadeOutSpeed;	//fading speeds
	FadeState fadeState;				//fading state (in, out, stable)
	float fadeTo;						//opacity to fade to
	
	PVector position;					//position
	PVector velocity;					//velocity
	float drag;							//drag

	Rectangle bounds;					//bounding rectangle
	
	/**
	 * Constructor.
	 * @param parent parent Processing applet
	 * @param value textual value
	 */
	public Word(PApplet parent, String value) {
		this.p = parent;
		this.value = value;
		this.opacity = 1;
		this.fadeInSpeed = 0.05f;
		this.fadeOutSpeed = 0.01f;
		this.position = new PVector();
		this.velocity = new PVector();
		this.drag = 0.98f;
		this.fadeState = FadeState.STABLE;
		this.fadeTo = 1.0f;
		this.bounds = new Rectangle(0, 0, (int)this.p.textWidth(value), (int)(this.p.textAscent()+this.p.textDescent()));
	}
	
	/**
	 * Copy constructor.
	 * @param w original word
	 */
	public Word(Word w) {
		this.p = w.p;
		this.value = w.value;
		this.opacity = w.opacity;
		this.fadeInSpeed = w.fadeInSpeed;
		this.fadeOutSpeed = w.fadeOutSpeed;
		this.position = w.position.get();
		this.velocity = w.velocity.get();
		this.drag = w.drag;
		this.fadeState = w.fadeState;
		this.fadeTo = w.fadeTo;
		this.bounds = w.bounds.getBounds();
	}
	
	/**
	 * Update.
	 */
	public void update() {		
		//fade in or out based on the current state
		switch(fadeState) {
		case FADE_IN:
			opacity += fadeInSpeed;
			if (opacity > fadeTo) { opacity = fadeTo; fadeState = FadeState.STABLE; }
			break;
		case FADE_OUT:
			opacity -= fadeOutSpeed;
			if (opacity < fadeTo) { opacity = fadeTo; fadeState = FadeState.STABLE; }
			break;
		case STABLE:
			break;
		}
		
		//update position
		position.add(PVector.mult(velocity, drag));
	}
	
	/**
	 * Check if the word is fading in.
	 * @return true if the word is fading in, false if not.
	 */
	public boolean isFadingIn() { return fadeState == FadeState.FADE_IN; }
	
	/**
	 * Check if the word is fading out.
	 * @return true if the word is fading out, false if not.
	 */
	public boolean isFadingOut() { return fadeState == FadeState.FADE_OUT; }
	
	/**
	 * Fade to an opacity at a given speed.
	 * @param opacity opacity to fade to
	 * @param speed speed to fade at
	 */
	public void fadeTo(float opacity, float speed) {
		if (opacity > this.opacity) fadeIn(opacity, speed);
		else if (opacity < this.opacity) fadeOut(opacity, speed);
	}
	
	/**
	 * Fade in.
	 * @param opacity opacity to fade to
	 */
	public void fadeIn(float opacity) { fadeState = FadeState.FADE_IN; fadeTo = opacity; }
	
	/**
	 * Fade out.
	 * @param opacity opacity to fade to
	 */
	public void fadeOut(float opacity) { fadeState = FadeState.FADE_OUT; fadeTo = opacity; }
	
	/**
	 * Fade in at a given speed.
	 * @param opacity opacity to fade to
	 * @param speed speed to fade at
	 */
	public void fadeIn(float opacity, float speed) { fadeState = FadeState.FADE_IN; fadeTo = opacity; fadeInSpeed = speed; }
	
	/**
	 * Fade out at a given speed.
	 * @param opacity opacity to fade to
	 * @param speed speed to fade at
	 */
	public void fadeOut(float opacity, float speed) { fadeState = FadeState.FADE_OUT; fadeTo = opacity; fadeOutSpeed = speed; }

	/**
	 * Set the fading speeds
	 * @param in speed of fade in
	 * @param out speed of fade out
	 */
	public void setFadeSpeeds(float in, float out) {
		fadeInSpeed = in;
		fadeOutSpeed = out;
	}
	
	/**
	 * Check if the word contains an x,y position
	 * @param x x position
	 * @param y y position
	 * @return true if the position is within the word, false if outside
	 */
	public boolean contains(int x, int y) {
		return bounds.contains(x, y);
	}
	
	/**
	 * Get the center position of the word.
	 * @return center position
	 */
	public PVector center() {
		return new PVector((float)bounds.getCenterX(), (float)bounds.getCenterY(), position.z);
	}
	
	/**
	 * Draw.
	 */
	public void draw() {
		//if fully transparent, nothing to do
		if (opacity == 0) return;
		
		//draw the word at the right opacity
		int savedFill = p.g.fillColor;
		p.fill(savedFill, p.alpha(savedFill)*opacity);
		p.text(value, position.x, position.y);
		p.fill(savedFill);
	}	
}
