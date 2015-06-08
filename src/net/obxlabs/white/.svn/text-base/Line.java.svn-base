/*
 * Copyright (c) 2012 All Right Reserved, Jason E. Lewis [http://obxlabs.net]
 */

package net.obxlabs.white;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

/**
 * Scrolling line of text.
 * @author Bruno
 */
public class Line {
	static Logger logger = Logger.getLogger(Line.class);
	
	static final int MAX_SIDE_WORDS = 8;		//max words to buffer on each side of highlighted word
	static final float SPACE_WIDTH = 12;		//width of a space (pixels)
	static final int TEXT_TOP_MARGIN = 10;		//top margin between text and center
	static final int MAX_VXSCROLL = 100;		//maximum scrolling velocity
	
	//scrolling states
	static final int UNKNOWN = 0;
	static final int BACKWARD = 1;
	static final int FORWARD = 2;
	
	PApplet p;			//parrent applet
	Word[] source;		//sources words
	int left, right;	//index of the leftmost buffered word
	int highlight;		//index of the rightmost buffered word
	Touch touch;		//active touch (null if not touched)
	
	LinkedList<Word> words;		//word buffer

	PVector position;	//line position
	PVector offset;		//line offset from position (used from grabbing line again mostly)
	
	float dragScroll;	//drag of the scrolling
	float xScroll;		//x position of the scroll
	float axScroll;		//scroll acceleration
	float vxScroll;		//scroll velocity
	int direction;		//scroll direction
	long start;			//when line was started/created
	float dirLength;	//length of direction underline
	
	String forwardSnd, backwardSnd;			//id of the sound samples
	int fadeInDuration, fadeOutDuration;	//fade durations (millis)
	
	/**
	 * Constructor.
	 * @param parent
	 * @param source source words
	 * @param firstword index of first word
	 * @param touch active touch
	 */
	public Line(PApplet parent, Word[] source, int firstword, Touch touch) {
		this.p = parent;
		this.source = source;
		this.left = this.right = firstword;
		this.touch = touch;
		this.highlight = 0;
		this.start = p.millis();
		
		Word word;
		
		//start word		
		word = new Word(source[firstword]);
		word.position.set(0,0,0);
		
		//fill up the line
		words = new LinkedList<Word>();
		words.add(new Word(source[firstword]));
		
		//to the left
		for(int i = 0; i < MAX_SIDE_WORDS; i++)
			if (!addLeft()) break;
		
		//to the right
		for(int i = 0; i < MAX_SIDE_WORDS; i++)
			if (!addRight()) break;
		
		//setup scroll attributes
		position = new PVector(touch.x(), touch.y(), 0);
		offset = new PVector();
		dragScroll = 1;
		xScroll = 0;
		vxScroll = 0;
		direction = UNKNOWN;
		dirLength = 0;

		//set fading attributes
		fadeInDuration = 2000;
		fadeOutDuration = 500;
	}
	
	/**
	 * Get index of highlighted word.
	 * @return index
	 */
	public int highlight() { return highlight + left; }
	
	/**
	 * Set the sound sample ids.
	 * @param fwd id of forward sound
	 * @param bwd id of backward sound
	 */
	public void setAudio(String fwd, String bwd) {
		forwardSnd = fwd;
		backwardSnd = bwd;
	}
	
	/**
	 * Set fade durations.
	 * @param in fade in duration in millis
	 * @param out fade out duration in millis
	 */
	public void setFadeDurations(int in, int out) {
		fadeInDuration = in;
		fadeOutDuration = out;
	}
	
	/**
	 * Release the line from touch.
	 */
	public void release() {
		//release from touch
		this.touch = null;
		
		//fade out words
		ListIterator<Word> it = words.listIterator();
		while(it.hasNext()) {
			Word w = it.next();
			w.fadeOut(0, 0.01f);
		}
		
		//fade the center word slowly
		words.get(highlight).fadeOut(0, 0.001f);
		
		//start dragging to slow down scroll
		dragScroll = 0.90f;
		
		//fade out audio
		White.soundManager.fadeout(forwardSnd, fadeOutDuration);
		White.soundManager.fadeout(backwardSnd, fadeOutDuration);
	}
	
	/**
	 * Set the line's active touch.
	 * @param t touch
	 */
	public void setTouch(Touch t) {
		this.touch = t;	
		this.offset.set(position.x + offset.x - t.x, position.y + offset.y - t.y, 0);
		this.position.set(t.x, t.y, 0);
		this.dragScroll = 1;
	}
	
	/**
	 * Check if the line is done fading, transparent.
	 * @return true if line is done, false if not
	 */
	public boolean isDone() {
		return words.get(highlight).opacity == 0;
	}
	
	/**
	 * Check if the line bounding box contains a position.
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return true if x,y is in the line bounding box, false if not
	 */
	public boolean contains(int x, int y) {
		Word word = words.get(highlight);
		Rectangle bounds = new Rectangle(word.bounds);
		bounds.translate((int)(position.x + offset.x + xScroll + word.position.x - bounds.width/2), (int)(-bounds.height + position.y + offset.y + word.position.y));		
		return bounds.contains(x, y);
	}
	
	/**
	 * Update.
	 */
	public void update() {
		Word w;
		ListIterator<Word> it;
		int index;
		
		//drag
		if (touch != null)
			position.set(touch.x, touch.y, 0);
		
		//scroll
		if (touch != null)
			axScroll -= touch.dx()/5;
		vxScroll += axScroll;
		vxScroll *= dragScroll;
		if (vxScroll > MAX_VXSCROLL) vxScroll = MAX_VXSCROLL;
		xScroll += vxScroll;
		axScroll = 0;
		
		//adjust audio
		int d = touch == null || touch.dx() == 0 ? UNKNOWN : touch.dx() > 0 ? FORWARD : BACKWARD;
		if (direction != d) {
			if (d == FORWARD)
				White.soundManager.crossfade(forwardSnd, backwardSnd, fadeInDuration);
			else if (d == BACKWARD)
				White.soundManager.crossfade(backwardSnd, forwardSnd, fadeInDuration);
			direction = d;
		}
		
		//limit scroll to first/last words
		w = words.getFirst();
		if (w.position.x + xScroll + offset.x > 0) {
			xScroll = -w.position.x - offset.x;
			vxScroll = 0;
			White.soundManager.fadeout(backwardSnd, fadeOutDuration);
		}		
		w = words.getLast();
		if (w.position.x + xScroll + offset.x < 0) {
			xScroll = -w.position.x - offset.x;
			vxScroll = 0;
			White.soundManager.fadeout(forwardSnd, fadeOutDuration);
		}
		
		//find highlighted middle word
		if (touch != null) {
			it = words.listIterator();
			index = 0;
			while(it.hasNext()) {
				w = it.next();
				if (w.position.x - w.bounds.width/2 + xScroll + offset.x < 0 && w.position.x + w.bounds.width/2 + xScroll + offset.x > 0) {
					highlight = index;
					break;
				}
				index++;
			}
		}
		
		//adjust word list buffer
		if(highlight < MAX_SIDE_WORDS) {
			while(highlight < MAX_SIDE_WORDS) if (!addLeft()) break;
		} else {
			while(highlight > MAX_SIDE_WORDS) removeLeft();
		}
		
		if(words.size()-1-highlight < MAX_SIDE_WORDS) {
			while(words.size()-1-highlight < MAX_SIDE_WORDS) if (!addRight()) break;
		} else {
			while(words.size()-1-highlight > MAX_SIDE_WORDS) removeRight();
		}
		
		//set fading values based on scroll offset and speed
		it = words.listIterator();
		index = 0;
		float maxDist = vxScroll * 50;
		if (maxDist < 0) maxDist *= -1;
		maxDist += 200;
		double maxLog = Math.log(maxDist);
		
		while(it.hasNext()) {
			w = it.next();
			
			if (touch != null) {
				float wx = w.position.x + xScroll + offset.x;
				if (wx < 0) wx *= -1;
				wx += 1;
				
				if (index == highlight) w.fadeTo(1, 0.1f);
				else if (wx > maxDist) w.fadeTo(0, 0.1f);
				else w.fadeTo((float)((maxLog - Math.log(wx)) / maxLog), 0.1f);			
			}
			
			w.update();			
			index++;
		}
	}
	
	/**
	 * Remove leftmost buffered word.
	 */
	public void removeLeft() {
		words.removeFirst();
		left++;
		highlight--;
	}
	
	/**
	 * Add a word to the left of the buffer.
	 * @return
	 */
	public boolean addLeft() {
		if (left <= 0) return false;
		
		//get the position of the current leftmost buffered word
		Word word;
		word = words.getFirst();
		PVector pos = word.position.get();
		pos.x -= (float)word.bounds.getWidth()/2 + SPACE_WIDTH;

		//add the next word from the source to the left
		word = new Word(source[left-1]);
		pos.x -= (float)word.bounds.getWidth()/2;
		word.position.set(pos);
		words.addFirst(word);
		
		//adjust indexes
		left--;
		highlight++;
		
		//success!
		return true;
	}
	
	/**
	 * Remove rightmost buffered word.
	 */
	public void removeRight() {
		words.removeLast();
		right--;
	}
	
	/**
	 * Add a word to the right of the buffer.
	 * @return
	 */
	public boolean addRight() {
		if (right >= source.length-1) return false;
		
		//get the position of the current rightmost buffered word
		Word word;
		word = words.getLast();
		PVector pos = word.position.get();
		pos.x += (float)word.bounds.getWidth()/2 + SPACE_WIDTH;

		//add the next word from the source to the right
		word = new Word(source[right+1]);
		pos.x += (float)word.bounds.getWidth()/2;
		word.position.set(pos);
		words.addLast(word);
		
		//adjust index
		right++;
		
		//success!
		return true;
	}
	
	/**
	 * Draw.
	 */
	public void draw() {
		p.textAlign(PConstants.CENTER, PConstants.BASELINE);
		p.pushMatrix();
		p.translate(position.x + xScroll + offset.x, position.y - TEXT_TOP_MARGIN + offset.y);
		
		//draw words
		ListIterator<Word> it = words.listIterator();
		while(it.hasNext())
			it.next().draw();
	
		p.translate(-xScroll - offset.x, TEXT_TOP_MARGIN);
		p.noFill();
			
		//calculate length of direction underline
		int dir = vxScroll<0?-1:1;			
		float length = vxScroll*10*dir;
		
		if (length*dir > dirLength) {
			dirLength += touch==null?20:5;
			if (dirLength > length*dir) dirLength = length*dir;
		}
		else if (length*dir < dirLength) {
			dirLength -= touch==null?20:5;
			if (dirLength < length*dir) dirLength = length*dir;				
		}
		dir = vxScroll<0?-1:1;
			
		//draw direction underline
		if (dirLength*dir > 1) {
			for(float vx = 0, vy = 0; vx <= dirLength*dir; vx += 0.2f) {
				vy = (p.noise(vx/10f, p.millis()/1000f)-0.5f)*2*(1-vx/(dirLength*dir))*10;
				
				p.stroke(0, 200 - vx/(dirLength*dir)*200);
				p.point(vx*dir, vy + 3);
			}
		}
		
		p.popMatrix();
	}
	
	/**
	 * Draw line debug layer.
	 */
	public void drawDebug() {
		Word word = words.get(highlight);
		Rectangle bounds = new Rectangle(word.bounds);
		bounds.translate((int)(position.x + xScroll + word.position.x - bounds.width/2), (int)(-bounds.height + position.y + word.position.y));
		p.noFill();
		p.stroke(0);
		p.rect(bounds.x, bounds.y, bounds.width, bounds.height);
	}
}
