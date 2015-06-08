/*
 * Copyright (c) 2012 All Right Reserved, Jason E. Lewis [http://obxlabs.net]
 */

package net.obxlabs.white;

import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import net.obxlabs.white.Touch;
import net.obxlabs.white.White;
import net.obxlabs.white.Word;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import controlP5.ControlP5;
import controlP5.Knob;
import TUIO.TuioCursor;
import TUIO.TuioProcessing;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;


public class White extends PApplet {

	private static final long serialVersionUID = -6906664568389195326L;
	
	static Logger logger = Logger.getLogger(White.class);
	
	static final int FPS = 30;
	static final String[] BG_TEXT = {"The", "World", "Was", "White"};
	
	static final String SND_BLOOD = "blood";	//id of blood sound sample

	//properties editable in the config.properties file
	static boolean FULLSCREEN;					//true to open in fullscreen
	static int FRAME_WIDTH;						//frame width in window mode
	static int FRAME_HEIGHT;					//frame height in window mode
	static int BG_COLOR;						//background color
	static int SMOOTH_LEVEL;					//anti-aliasing level

	static float BG_TEXT_HMARGIN = 500;			//horizontal left/right margin of background text
	static float BG_TEXT_VMARGIN = 300;			//vertical top/bottom margin of background text
	static float BG_TEXT_SPEED = 4.0f;			//speed of background text motion
	static float BG_TEXT_TOP = 280;				//top position of background text
	static float BG_TEXT_LEADING = 270;			//leading of background text
	static String BG_TEXT_FONT;					//text font (without the .ttf) of background text
	static float BG_TEXT_FONT_SIZE;				//text font size in pixels of background text
	static int BG_TEXT_FILL_COLOR;				//text fill color of background text
	static int BG_TEXT_STROKE_COLOR;			//text stroke color of background text
	static float BG_TEXT_STROKE_WEIGHT = 1; 	//stroke weight of background text
	static float BG_FLICKER_SPEED = 0.1f;		//flicker speed of background text
	static float BG_FLICKER_PROBABILITY = 0.4f; //flicker probability of background text


	static String[] SCROLL_TEXT_FILES;		//scroll text content files
	static String SCROLL_TEXT_FONT;			//scroll text font (without the .ttf)
	static float SCROLL_TEXT_FONT_SIZE;		//scroll text font size in pixels
	static int SCROLL_TEXT_COLOR;			//scroll text color
		
	static String SCROLL_TOUCH1_FWD_AUDIO = "backwards.aif";	//filename of sound for forward motion of first touch
    static String SCROLL_TOUCH1_BWD_AUDIO = "bwd-01.aif";		//filename of sound for backward motion of first touch
    static String SCROLL_TOUCH2_FWD_AUDIO = "backwards.aif";    //filename of sound for forward motion of second touch
    static String SCROLL_TOUCH2_BWD_AUDIO = "bwd-03.aif";       //filename of sound for backward motion of second touch
    
    static int SCROLL_AUDIO_FADE_IN_DURATION;	//duration of scroll line audio fade in
    static int SCROLL_AUDIO_FADE_OUT_DURATION;	//duration of scroll line audio fade out
    
    static float BLOOD_MASS;				//mass of blood
    static float BLOOD_START;				//blood start threshold (based on touch activity)
	
	static boolean TUIO_ENABLE;				//true to enable tuio input
	static int TUIO_PORT;					//port of the TUIO connection

	static boolean DEBUG;					//true to show debug layer
	static boolean MENU;					//true to show menu layer
	static boolean DEBUG_TOUCHES;			//true to show touches

	TuioProcessing tuioClient;				//TUIO client
	Map<Integer, Touch> touches;			//map of current active touches
	Touch firstTouch;						//pointer to the first touch
	String tuioServerAddr;					//address of the TUIO server
	
	PFont debugFont;						//font used in the debug layer
	boolean debugBlood;						//flag to debug blood
	boolean debugSnow;						//flag to debug snow

	float bgOpacity;						//background text opacity
	PVector bgCenter;						//background text center position
	OutlinedWord[] bgWords;					//background outlined words
	PFont bgTextFont;						//font of the text
	
	PFont scrollTextFont;					//font of the text
	Word[][] scrollTextWords;				//all the words from the text
	ArrayList<Line> scrollTextLines;		//the lines that appeared under touches
	int[] scrollNextWords; 					//index of the next words to display for each stream
	int[] scrollNextTouches;				//id of the last touch associate to each stream
	
	Blood blood;							//blood layer
	float activity;							//level of interaction activity
	Snow snow;								//noisy snow layer that covers the blood
	
	long lastUpdate = 0;        			//last time the sketch was updated
	long now = 0;               			//current time
	long dt;                    			//time difference between draw calls
	
	static SoundManager soundManager;		//sound manager for the application
	
	ControlP5 cp5;	//ui controls
	
	/**
	 * Setup sketch.
	 */
	public void setup() {
		//if set, open in fullscreen
		if (FULLSCREEN) {
			//fill up the screen
			size(displayWidth, displayHeight, OPENGL);
			if (!DEBUG) noCursor(); //hide the cursor
		}
		//if not, open in standard window mode
		else {
			size(FRAME_WIDTH, FRAME_HEIGHT, OPENGL);
		}
		
		smooth(SMOOTH_LEVEL);	//set anti-aliasing
		background(BG_COLOR);	//clear background
		frameRate(FPS);			//limit framerate
		
		//create the debug font
		debugFont = createFont("Arial", 16);

		//keep track of time
		now = millis();
		lastUpdate = now;
		  
		//setup touches
		touches = Collections.synchronizedMap(new HashMap<Integer, Touch>());
		firstTouch = null;

		//setup audio
		setupAudio();

		//setup snow
		setupSnow();
		
		//setup blood
		blood = new Blood(this);
		blood.setAudio(SND_BLOOD);
		debugBlood = false;
		blood.setMass(BLOOD_MASS);
		
		//setup TUIO
		setupTUIO();
		
		//setup the text
		setupText();
		
		//setup controls
		setupControls();
	}
	
	/**
	 * Setup the audio using Minim. 
	 */
	public void setupAudio() {	
		//init sound manager
		soundManager = new SoundManager(this);

		//load sounds
		soundManager.loadSample("touch1-fwd", "data"+File.separator+"audio"+File.separator+SCROLL_TOUCH1_FWD_AUDIO);
		soundManager.loadSample("touch1-bwd", "data"+File.separator+"audio"+File.separator+SCROLL_TOUCH1_BWD_AUDIO);
		soundManager.loadSample("touch2-fwd", "data"+File.separator+"audio"+File.separator+SCROLL_TOUCH2_FWD_AUDIO);
		soundManager.loadSample("touch2-bwd", "data"+File.separator+"audio"+File.separator+SCROLL_TOUCH2_BWD_AUDIO);
		soundManager.loadSample(SND_BLOOD, "data"+File.separator+"audio"+File.separator+"blood.aif");
		
		//stop sound manager on exit
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run () {
				soundManager.stop();
			}
		}));
	}
	
	/**
	 * Setup the snow.
	 */
	public void setupSnow() {
		snow = new Snow(this, width, height);
		debugSnow = false;
	}
	
	/**
	 * Setup TUIO.
	 */
	public void setupTUIO() {
		//init TUIO client
		if (TUIO_ENABLE) {
			logger.info("Init TUIO");
			tuioClient  = new TuioProcessing(this);
			try {
			    InetAddress addr = InetAddress.getLocalHost();
	
			    //convert to string
			    tuioServerAddr = addr.getHostAddress() + ":" + TUIO_PORT;
			} catch (UnknownHostException e) {
				logger.warn(e.getMessage());
			}
		}
	}
	
	/**
	 * Setup the text.
	 */
	public void setupText() {
		//start the words in the center (zero offset)
		bgCenter = new PVector(width/2, height/2);
		
		//create the outlined words
		bgWords = new OutlinedWord[BG_TEXT.length];
		for(int i = 0; i < BG_TEXT.length; i++)
			bgWords[i] = new OutlinedWord(this, BG_TEXT[i]);
		
		//load the scroll text font
		bgTextFont = createFont(BG_TEXT_FONT, BG_TEXT_FONT_SIZE);
		textFont(bgTextFont);

		//load the scroll text font
		scrollTextFont = createFont(SCROLL_TEXT_FONT, SCROLL_TEXT_FONT_SIZE);
		textFont(scrollTextFont);
		
		//read the lines from the text file
		String[][] strings = new String[2][];
		scrollTextWords = new Word[2][];
		
		for(int i = 0; i < strings.length; i++) {
			strings[i] = loadStrings(SCROLL_TEXT_FILES[i]);
		
			//count all the words in the text
			int wordCount = 0;
			for(String textLine : strings[i]) {
				if (textLine.isEmpty()) continue;
				wordCount += textLine.split(" ").length;
			}
			
			//create one large array that holds all the words
			scrollTextWords[i] = new Word[wordCount];
			wordCount = 0;
			for(String textLine : strings[i]) {
				if (textLine.isEmpty()) continue;
				
				String[] lineWords = textLine.split(" ");
				for(int j = 0; j < lineWords.length; j++) {
					String lineWord = lineWords[j] + (j == lineWords.length-1 ? "  " : "");
					scrollTextWords[i][wordCount] = new Word(this, lineWord);
					scrollTextWords[i][wordCount].opacity = 0;
					wordCount++;
				}
			}			
		}
		
		//set the current index (next word shown on touch) to the first word
		scrollNextWords = new int[2];
		for(int i = 0; i < scrollNextWords.length; i++)
			scrollNextWords[i] = 0;
		
		scrollNextTouches = new int[2];
		for(int i = 0; i < scrollNextTouches.length; i++)
			scrollNextTouches[i] = -1;
		
		//create the empty lines array
		scrollTextLines = new ArrayList<Line>();
	}

	/**
	 * Set the blood mass.
	 * @param value mass
	 */
	public void setBloodMass(float value) {
		blood.setMass(value);
	}
	
	/**
	 * Setup UI controls.
	 */
	public void setupControls() {
		cp5 = new ControlP5(this);
		
		cp5.addKnob("setBloodMass")
               .setRange(0.1f, 1f)
               .setValue(BLOOD_MASS)
               .setPosition(width-120,height-130)
               .setRadius(50)
               .setDragDirection(Knob.VERTICAL)
               .setCaptionLabel("blood.mass")
               ;	

		cp5.addKnob("BLOOD_START")
	        .setRange(0f, 1f)
	        .setValue(BLOOD_START)
	        .setPosition(width-240,height-130)
	        .setRadius(50)
	        .setDragDirection(Knob.VERTICAL)
	        .setCaptionLabel("blood.start")
	        ;			
		
		if (MENU) cp5.show();
		else cp5.hide();		
	}
	
	/**
	 * Draw.
	 */
	public void draw() {
		//millis since last draw
		dt = now-lastUpdate;
		  	
		blood.update(dt, debugBlood?1:constrain(map(activity*activity, 0, 1, 1-BLOOD_START, 1), 0, 1));
		
		soundManager.update();
		updateActivity();
		updateBgText();
		updateScrollingLines();
		
		//draw blood
		blood.draw();
		if (!debugSnow) snow.draw();
				
		//draw the text
		drawText();
		
	    //keep track of time
	    lastUpdate = now;
	    now = millis();
		  
		//draw debug and menu layer
		if (DEBUG) drawDebug();
		if (MENU) drawMenu();
	}
	
	/**
	 * Update the touch activity meter.
	 */
	public void updateActivity() {
		int found = 0;
		float speed = 0;
		
		//check if any lines are touched, use velocity to compute activity
		synchronized(scrollTextLines) {
			for(Line l : scrollTextLines) {
				if (l.touch != null) {
					speed += abs(l.vxScroll)/2;
					found++;
				}
			}
		}
		
		//adjust the current level of activity towards target
		if (speed > activity) {
			activity += 0.001f;
			if (activity > found*(TUIO_ENABLE?0.5f:1.0f))				
				activity -= 0.001f;
		}
		else {
			activity -= 0.001f;
			if (activity < 0)
				activity = 0;
		}
	}
	
	/**
	 * Update the background text.
	 */
	public void updateBgText() {
		float p = noise((width/2 + bgCenter.x)*0.005f, (height/2 + bgCenter.y)*0.005f, frameCount*0.01f);
		float r = p * TWO_PI * 4;
		
		//move the background text center
		bgCenter.x += Math.cos(r) * BG_TEXT_SPEED * p;
		if (bgCenter.x < BG_TEXT_HMARGIN) bgCenter.x = BG_TEXT_HMARGIN;
		else if (bgCenter.x > width-BG_TEXT_HMARGIN) bgCenter.x = width-BG_TEXT_HMARGIN;
		
		bgCenter.y += Math.sin(r) * BG_TEXT_SPEED * p;
		if (bgCenter.y < BG_TEXT_VMARGIN) bgCenter.y = BG_TEXT_VMARGIN;
		else if (bgCenter.y > height-BG_TEXT_VMARGIN) bgCenter.y = height-BG_TEXT_VMARGIN;
		
		//hack to render the background text at least once at the beginning.
		if (frameCount == 1) {
			bgOpacity = 255;
			return;
		}
		
		//adjust the background text opacity (flicker)
		p = noise((width/2 + bgCenter.x)*BG_FLICKER_SPEED, (height/2 + bgCenter.y)*BG_FLICKER_SPEED, frameCount*BG_FLICKER_SPEED);
		bgOpacity = p > 1-BG_FLICKER_PROBABILITY ? 255 : 0;
	}
	
	/**
	 * Update the scrolling lines.
	 */
	public void updateScrollingLines() {
		//update the scrolling lines
		synchronized(scrollTextLines) {
			Iterator<Line> it = scrollTextLines.iterator();
			while(it.hasNext()) {
				Line l = it.next();
				l.update();
				if (l.isDone()) {
					it.remove();
				}
			}
		}		
	}
	
	/**
	 * Draw the text.
	 */
	public void drawText() {
		//draw the background text, if not transparent
		if (bgOpacity != 0) {
			fill(BG_TEXT_FILL_COLOR);			
			stroke(BG_TEXT_STROKE_COLOR, bgOpacity);
			strokeWeight(BG_TEXT_STROKE_WEIGHT);
			textFont(bgTextFont);
			textAlign(LEFT, BASELINE);
		
			pushMatrix();
			translate(bgCenter.x - width/2, bgCenter.y - height/2);
			float y = BG_TEXT_TOP;
			for(OutlinedWord w : bgWords) {
				pushMatrix();
				translate(width/2 - (float)w.bounds().getWidth()/2, y);
				w.draw();
				y += BG_TEXT_LEADING;
				popMatrix();
			}
			popMatrix();
		}
		
		//set text color and alignment
		fill(SCROLL_TEXT_COLOR);
		noStroke();
		textFont(scrollTextFont);
		textAlign(CENTER, CENTER);

		//draw all words
		synchronized(scrollTextLines) {
			for(Line l : scrollTextLines)
				l.draw();
		}
	}
	
	/**
	 * Draw debug layer.
	 */
	public void drawDebug() {
		//draw debug info
		fill(0);
		noStroke();
		textAlign(LEFT, BASELINE);
		textFont(debugFont);
		text("fps: " + frameRate, 10, 24);		
		text("heap: " + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1048576) +
				" / " + (Runtime.getRuntime().totalMemory()/1048576) + "mb", 10, 24*2);
		text("TUIO server: " + tuioServerAddr, 10, 24*3);
		text("activity: " + activity, 10, 24*4);
		
		//draw touches
		if (DEBUG_TOUCHES) {
			synchronized (touches) {
				Iterator<Integer> it = touches.keySet().iterator();
			    while (it.hasNext()) {
			    	Touch touch = touches.get(it.next());
	
			    	// draw the Touch
	   				fill(0, 128);
	       			stroke(255, 0, 0);
	       			ellipse(touch.x, touch.y, 50, 50);
			    }
			}
		}
	}
	
	/**
	 * Draw the menu options.
	 */
	public void drawMenu() {
		//draw debug info
		fill(0);
		noStroke();
		textAlign(LEFT, BASELINE);
		textFont(debugFont);
		text("m: toggle this menu", 10, height-12);	
		text("d: toggle debug layer", 10, height-12-24*1);			
		text("t: toggle debug touches layer", 10, height-12-24*2);
		text("b: toggle blood", 10, height-12-24*3);
		text("s: toggle snow", 10, height-12-24*4);
	}
	
	/**
	 * Handle mouse press events.
	 */
	public void mousePressed() { 
		if (TUIO_ENABLE) return;
		addTouch(mouseEvent.isAltDown()?1:0, mouseX, mouseY);	//simulate second touch with alt
	}
	
	/**
	 * Handle mouse release events.
	 */
	public void mouseReleased() {
		if (TUIO_ENABLE) return;
		removeTouch(mouseEvent.isAltDown()?1:0);	//simulate second touch with alt
	}
	
	/**
	 * Handle mouse drag events.
	 */
	public void mouseDragged() {
		if (TUIO_ENABLE) return;
		updateTouch(mouseEvent.isAltDown()?1:0, mouseX, mouseY);	//simulate second touch with alt
	}
	
	/**
	 * Handle key press events.
	 */
	public void keyPressed() {
		switch(key) {
		//show/hide debug layer
		case 'd':
		case 'D':
			DEBUG = !DEBUG;
			if (DEBUG) cursor();
			else noCursor();
			break;
		//show/hide menu
		case 'm':
		case 'M':
			MENU = !MENU;
			if (MENU) cp5.show();
			else cp5.hide();			
			break;
		//debug touches
		case 't':
		case 'T':
			DEBUG_TOUCHES = !DEBUG_TOUCHES;
			break;
		//debug blood
		case 'b':
		case 'B':
			debugBlood = !debugBlood;
			break;
		//debug snow
		case 's':
		case 'S':
			debugSnow = !debugSnow;
			break;
		}
	}
	
	/**
	 * Handle TUIO add cursor event.
	 * @param c the cursor
	 */
	public void addTuioCursor(TuioCursor c) 
	{
		addTouch(c.getCursorID(), c.getScreenX(width), c.getScreenY(height));
	}

	/**
	 * Handle TUIO update cursor event.
	 * @param c the cursor
	 */
	public void updateTuioCursor(TuioCursor c) 
	{
		updateTouch(c.getCursorID(), c.getScreenX(width), c.getScreenY(height));
	}

	/**
	 * Handle TUIO remove cursor event.
	 * @param c the cursor
	 */
	public void removeTuioCursor(TuioCursor c) 
	{
		removeTouch(c.getCursorID());
	}	
	
	/**
	 * Add a touch object to the active list.
	 * @param id id
	 * @param x x position
	 * @param y y position
	 */
	public void addTouch(int id, int x, int y) {
		//add to touches
		Touch t = new Touch(id, x, y, millis());
		
		synchronized(touches) {
			touches.put(new Integer(id), t);
			if(touches.size() == 1) firstTouch = t;
		}
		
		synchronized(scrollTextLines) {
			//check if touch is over an existing line
			boolean found = false;
			Iterator<Line> it = scrollTextLines.iterator();
			while(it.hasNext() && !found) {
				Line l = it.next();
				if (l.contains(x, y)) {
					//l.touch = t;
					l.setTouch(t);
					found = true;
				}
			}		
			//if not highlight the next line
			if (!found) {
				//Line l = new Line(this, scrollTextWords, nextWord(t.id), t);
				int stream = stream(t.id);
				Line l = new Line(this, scrollTextWords[stream], scrollNextWords[stream], t);
				if (firstTouch == t)
					l.setAudio("touch1-fwd", "touch1-bwd");					
				else
					l.setAudio("touch2-fwd", "touch2-bwd");
				l.setFadeDurations(SCROLL_AUDIO_FADE_IN_DURATION, SCROLL_AUDIO_FADE_OUT_DURATION);
				scrollTextLines.add(l);
			}
		}
	}
	
	/**
	 * Get the index of the next word for the stream that matches a touch id.
	 * @param touchId touch id
	 * @return index of the next word
	 */
	public int stream(int touchId) {
		for(int i = 0; i < scrollNextTouches.length; i++) {
			if (scrollNextTouches[i] == touchId)
				return i;
		}
		
		for(int i = 0; i < scrollNextTouches.length; i++) {
			if (scrollNextTouches[i] == -1) {
				scrollNextTouches[i] = touchId;
				return i;
			}
		}	
		
		return 0;
	}
	
	/**
	 * Set the next word index for the stream with a given touch id
	 * @param touchId touch id
	 * @param index index of the next word
	 */
	public void setNextWord(int touchId, int index) {
		for(int i = 0; i < scrollNextTouches.length; i++) {
			if (scrollNextTouches[i] == touchId) {
				scrollNextTouches[i] = -1;
				scrollNextWords[i] = index;
			}
		}		
	}
	
	/**
	 * Update a touch objct in the active list.
	 * @param id id 
	 * @param x x position
	 * @param y y position
	 */
	public void updateTouch(int id, int x, int y) {
		//update the touch
		synchronized (touches) {
			Touch touch = touches.get(new Integer(id));
			if (touch != null) {
				touch.set(x, y);
			}
		}
	}
	
	/**
	 * Remove a touch object from the active list.
	 * @param id id
	 */
	public void removeTouch(int id) {
		//remove the touch
		Touch t = touches.remove(new Integer(id));
		if (t == null) return;
		
		//make all lines fade out quickly,
		//and make the first word that was shown fade out slowly
		synchronized(scrollTextLines) {
			for(Line l : scrollTextLines) {
				if (l.touch == t) {
					setNextWord(t.id, l.highlight());
					l.release();
				}
			}
		}
	}
	
	public static void main(String _args[]) {
		//configure logger
		PropertyConfigurator.configure("data"+File.separator+"logging.properties");
		
		//load properties
		Properties props = new Properties();
		try {
	        //load a properties file
			props.load(new FileInputStream("data"+File.separator+"config.properties"));
	 
	    	//get the standard properties
			FULLSCREEN = (Boolean.valueOf(props.getProperty("fullscreen", "true")));
			FRAME_WIDTH = (Integer.valueOf(props.getProperty("frame.width", "1280")));
			FRAME_HEIGHT = (Integer.valueOf(props.getProperty("frame.height", "720")));
			BG_COLOR = unhex(props.getProperty("background.color", "FF000000"));
			SMOOTH_LEVEL = (Integer.valueOf(props.getProperty("smooth.level", "6")));
			
			BG_TEXT_FONT = "fonts"+java.io.File.separator+props.getProperty("bg.text.font", "Arial")+".ttf";
			BG_TEXT_FONT_SIZE = (Float.valueOf(props.getProperty("bg.text.font.size", "300")));
			BG_TEXT_FILL_COLOR = unhex(props.getProperty("bg.text.fill.color", "FFFFFFFF"));
			BG_TEXT_STROKE_COLOR = unhex(props.getProperty("bg.text.stroke.color", "FF000000"));
			BG_FLICKER_SPEED = (Float.valueOf(props.getProperty("bg.flicker.speed", "0.1")));
			BG_FLICKER_PROBABILITY = (Float.valueOf(props.getProperty("bg.flicker.probability", "0.3")));
			
			SCROLL_TEXT_FILES = props.getProperty("scroll.text.files", "wewerethree.txt,wewerethree.txt").split(",");
			SCROLL_TEXT_FONT = "fonts"+java.io.File.separator+props.getProperty("scroll.text.font", "Arial")+".ttf";
			SCROLL_TEXT_FONT_SIZE = (Float.valueOf(props.getProperty("scroll.text.font.size", "72")));
			SCROLL_TEXT_COLOR = unhex(props.getProperty("scroll.text.color", "FF000000"));
			
			SCROLL_AUDIO_FADE_IN_DURATION = (Integer.valueOf(props.getProperty("scroll.audio.fade.in.duration", "2000")));
			SCROLL_AUDIO_FADE_OUT_DURATION = (Integer.valueOf(props.getProperty("scroll.audio.fade.out.duration", "500")));
			
			BLOOD_MASS = (Float.valueOf(props.getProperty("blood.mass", "0.666")));
			BLOOD_START = (Float.valueOf(props.getProperty("blood.start", "0.6")));
			
			TUIO_ENABLE = (Boolean.valueOf(props.getProperty("tuio.enable", "true")));
			TUIO_PORT = (Integer.valueOf(props.getProperty("tuio.port", "3333")));
						
			DEBUG = (Boolean.valueOf(props.getProperty("debug", "false")));
			DEBUG_TOUCHES = (Boolean.valueOf(props.getProperty("debug.touches", "false")));
			MENU = (Boolean.valueOf(props.getProperty("menu", "false")));
			
	        logger.info("Configuration properties loaded.");
		} catch (IOException ex) {
			logger.error("Exception occurred when trying to load config file.");
			ex.printStackTrace();
	    }
				
		//launch
		if (FULLSCREEN)
			//use present mode if fullscreen
			PApplet.main(new String[] { "--present", net.obxlabs.white.White.class.getName() });
		else
			//standard mode for window
			PApplet.main(new String[] { net.obxlabs.white.White.class.getName() });
	}
}
