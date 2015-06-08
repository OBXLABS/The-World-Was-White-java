/*
 * Copyright (c) 2012 All Right Reserved, Jason E. Lewis [http://obxlabs.net]
 */

package net.obxlabs.white;

/**
 * A identifiable touch location.
 * @author Bruno
 */
public class Touch {
	
	int id;					//id of the touch
	protected float ox, oy;	//original x and y positions
	protected float lx, ly;	//last x and y positions
	protected float x, y; 	//x and y positions
	long start;				//start time in millis

	/**
	 * Constructor.
	 * @param id id
	 * @param x x position
	 * @param y y position
	 */
	public Touch(int id, float x, float y, long start) 
	{
		this.id = id;
		this.ox = this.lx = this.x = x;
		this.oy = this.ly = this.y = y;
		this.start = start;
	}
	
	/**
	 * Get the touch x coordinate.
	 * @return x coordinate
	 */
	public float x() { return x; }
	
	/**
	 * Get the touch y coordinate.
	 * @return y coordinate
	 */
	public float y() { return y; }
	
	/**
	 * Get the touch original x coordinate.
	 * @return original x coordinate
	 */
	public float ox() { return ox; }
	
	/**
	 * Get the touch original y coordinate.
	 * @return original y coordinate
	 */
	public float oy() { return oy; }

	/**
	 * Get the touch horizontal distance between the current and last position.
	 * @return x distance
	 */
	public float dx() { return x-lx; }
	
	/**
	 * Get the touch vertical distance between the current and last position.
	 * @return y distance
	 */
	public float dy() { return y-ly; }
	
	/**
	 * Get the touch horizontal distance between the current and original position.
	 * @return x distance
	 */
	public float odx() { return x-ox; }
	
	/**
	 * Get the touch vertical distance between the current and original position.
	 * @return y distance
	 */
	public float ody() { return y-oy; }
	
	/**
	 * Set the position.
	 * @param x x position
	 * @param y y position
	 */
	public void set(float x, float y) {
		this.lx = this.x;
		this.ly = this.y;
		this.x = x;
		this.y = y;
	}
}
