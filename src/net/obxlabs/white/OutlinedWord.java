/*
 * Copyright (c) 2012 All Right Reserved, Jason E. Lewis [http://obxlabs.net]
 */

package net.obxlabs.white;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import processing.core.PApplet;
import processing.core.PVector;

/**
 * An outlined word.
 * @author Bruno
 */
public class OutlinedWord {
	static Logger logger = Logger.getLogger(OutlinedWord.class);
	
	PApplet p;		//parent applet
	String value;	//textual value

	protected static FontRenderContext frc = new FontRenderContext(null, false, false);	 //font render context
	ArrayList<PVector> vertices;	//verticies of the contours
	ArrayList<int[]> contours;		//start indices of contours
	GeneralPath outline;			//word outline
	Rectangle bounds;				//word bounds

	/**
	 * Constructor.
	 * @param parent parent applet
	 * @param value textual value
	 */
	public OutlinedWord(PApplet parent, String value) {
		this.p = parent;
		this.value = value;
		
		this.vertices = null;
		this.contours = null;
		this.outline = null;
	}
	
	/**
	 * Draw.
	 */
	public void draw() {
		//draw the word outline
		PathIterator pi = outline().getPathIterator(null);
	    float[] pts = new float[4];
	    while (!pi.isDone()) {
			int type = pi.currentSegment(pts);
			if (type == PathIterator.SEG_MOVETO) {
			  p.beginShape();
			  p.vertex(pts[0],pts[1]);
			}
			else if (type == PathIterator.SEG_LINETO) {
			  p.vertex(pts[0],pts[1]);
			}
			else if (type == PathIterator.SEG_QUADTO) {
			  p.quadraticVertex(pts[0],pts[1],pts[2],pts[3]);
			}
			else if (type == PathIterator.SEG_CLOSE) {
			  p.endShape();
			}
			pi.next();
	    }
	}
	
	/**
	 * Get the word outline, generate it if it does not exist yet.
	 * @return outline
	 */
	public GeneralPath outline() {
		if (outline == null) {
			initControlPoints(value);
			initOutline();
		}
		return outline;
	}
	
	/**
	 * Get the word bounds.
	 * @return bounds
	 */
	public Rectangle bounds() {
		if (bounds == null)
			bounds = outline().getBounds();
		return bounds;
	}

	/**
	 * This method uses the Java AWT Font methods to create a vector outline of 
     * the letters based on the current positions.
	 * @param str string
	 */
    protected void initControlPoints(String str) {
    	//make space for the control points
    	vertices = new ArrayList<PVector>();
            
    	// create a list to store the contours
    	contours = new ArrayList<int[]>();
            
    	// vertex array index (used to associate more than one contour point
    	// with the same vertex)
    	int vertexIndex = 0;
    
    	// a temporary list to store vertex indices for each contour (once 
    	// the contour is closed, this list will be converted to an array
    	// and stored into the Contour list.
    	ArrayList<Integer> tmpContour = new ArrayList<Integer>();
                            
    	// used to receive the list of points from PathIterator.currentSegment()
    	float points[] = new float[6];  
    	
    	// used to receive the segment type from PathIterator.currentSegment()
    	// segmentType can be SEG_MOVETO, SEG_LINETO, SEG_QUADTO, SEG_CLOSE
    	int segmentType = 0; 
    	
    	// used to remember the previously calculated Anchor and ControlPoint.
    	// for a more detailed description of what an anchor and control point are,
    	// see the architecture document.
    	PVector lastAnchor = new PVector();
            
    	// get the Shape for this glyph
    	GlyphVector gv = p.g.textFont.getFont().createGlyphVector( frc, str );
    	Shape outline = gv.getOutline();
            
    	// store the glyph's logical bounds information
    	//Rectangle2D logicalBounds = gv.getLogicalBounds();
            
    	// no flattening done at the moment, just iterate through all the 
    	// segments of the outline.  For more details see Javadoc for
    	// java.awt.geom.PathIterator
    	PathIterator pit = outline.getPathIterator(null);
    
    	while ( !pit.isDone() ) {         
    		segmentType = pit.currentSegment( points ); 
                            
    		switch( segmentType ) {
    			case PathIterator.SEG_MOVETO:
					// start a new tmpContour vector
					tmpContour = new ArrayList<Integer>();
					// get the starting point for this contour      
					PVector startingPoint = new PVector( (float)points[0], (float)points[1] );
					// store the point in the list of vertices
					vertices.add( new PVector( startingPoint.x, startingPoint.y ) );
					// store this point in the current tmpContour and increment
					// the vertices index
					tmpContour.add( vertexIndex );
					vertexIndex++;
					// update temporary variables used for backtracking
					lastAnchor = startingPoint;
					break;
                                    
    			case PathIterator.SEG_LINETO:
					// then, we must find the middle of the line and use it as 
					// control point in order to allow smooth deformations
					PVector endPoint = new PVector( (float)points[0], (float)points[1] );
					PVector midPoint = new PVector( (lastAnchor.x + endPoint.x)/2, 
					                                (lastAnchor.y + endPoint.y)/2  );
					vertices.add( new PVector( midPoint.x, midPoint.y ) );
					tmpContour.add( vertexIndex );
					vertexIndex++;
					
					// finally, we must add the endPoint twice to the contour
					// to preserve sharp corners
					vertices.add( new PVector( endPoint.x, endPoint.y ) );
					tmpContour.add( vertexIndex );
					vertexIndex++;
					
					// update variables used for backtracking
					lastAnchor = endPoint;
					break;
                                    
    			case PathIterator.SEG_QUADTO:
					PVector controlPoint = new PVector( (float)points[0], (float)points[1] );
					PVector anchorPoint = new PVector( (float)points[2], (float)points[3] );
					
					// Store control point.
					vertices.add( new PVector( controlPoint.x, controlPoint.y ) );
					tmpContour.add( vertexIndex );
					vertexIndex++;
					
					// Store anchor point.
					vertices.add( new PVector( anchorPoint.x, anchorPoint.y ) );
					tmpContour.add( vertexIndex );
					vertexIndex++;
					
					// update temporary variables used for backtracking                                     
					lastAnchor = anchorPoint;
					break;  
                             
    			case PathIterator.SEG_CLOSE:
					// A SEG_CLOSE signifies the end of a contour, therefore
					// convert tmpContour into a new array of correct size
					int contour[] = new int[tmpContour.size()];
					Iterator<Integer> it = tmpContour.iterator();
					int i = 0;
					while( it.hasNext() ) {
					        contour[i] = it.next();
					        i++;    
					}
					
					// add the newly created contour array to the contour list
					contours.add(contour);
					break;
                                    
    			case PathIterator.SEG_CUBICTO:        
					break;
    		} // end switch 
    
    		pit.next();
    	} // end while  
	}
    
    /**
     * Initalize the outline.
     */
    public void initOutline() {        
        // create a new GeneralPath to hold the vector outline
        GeneralPath gp = new GeneralPath();
        // get an iterator for the list of contours
        Iterator<int[]> it = contours.iterator();

        // process each contour
        while (it.hasNext()) {

            // get the list of vertices for this contour
            int contour[] = it.next();

            PVector firstPoint = vertices.get(contour[0]);
            // move the pen to the beginning of the contour
            gp.moveTo((float) firstPoint.x, (float) firstPoint.y);
            
            // generate all the quads forming the line
            for (int i = 1; i < contour.length-1; i+=2) {

            	PVector controlPoint = vertices.get(contour[i]);
                PVector anchorPoint = vertices.get(contour[i + 1]);

                gp.quadTo((float) controlPoint.x, (float) controlPoint.y,
                          (float) anchorPoint.x, (float) anchorPoint.y);
            }
            // close the path
            gp.closePath();

        } // end while 

        // cache it
        outline = gp;	
    }	
	    
    /**
     * Get string value.
     * @return string
     */
    public String toString() { return value; }
}
