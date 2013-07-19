package com.pianist.battlelasers;

import android.graphics.Point;

/**
 * The Move class stores all the information needed to describe a single move.
 * 
 * @author Peter Gokhshteyn and Alex Szoke
 * @version June 14, 2013
 */
public class Move
{
	private Point start;

	private Point end;

	private boolean turnRight;

	private int AIRating;

	/**
	 * Creates a new move with the given starting and ending points
	 * 
	 * @param start
	 *            The starting point of the move
	 * @param end
	 *            The ending point of the move
	 */
	public Move(Point start, Point end)
	{
		this.start = start;
		this.end = end;
		turnRight = false;

		// The AIRating is the length of the laser shot (the higher, the better
		// the rating
		AIRating = 0;
	}

	/**
	 * Returns the starting point for the current move
	 * 
	 * @return the move's starting point
	 */
	public Point getStartPoint()
	{
		return start;
	}

	/**
	 * Returns the ending point for the current move
	 * 
	 * @return the move's ending point
	 */
	public Point getEndPoint()
	{
		return end;
	}

	/**
	 * Specify that this in this move the gun needs to turn right
	 * 
	 * @return this same Move object (to use for stringing calls)
	 */
	public Move needToTurnRight()
	{
		turnRight = true;
		return this;
	}

	/**
	 * Checks whether this move says that the gun needs to turn right
	 * 
	 * @return whether to turn the gun right
	 */
	public boolean turnRight()
	{
		return turnRight;
	}

	/**
	 * Sets the rating of this move to the given rating
	 * 
	 * @param ranting
	 *            The rating to set to
	 */
	public void setRating(int ranting)
	{
		AIRating = ranting;
	}

	/**
	 * Returns this moves rating
	 * 
	 * @return this moves rating
	 */
	public int getRating()
	{
		return AIRating;
	}

	/**
	 * Returns the reverse of this move (as if undoing)
	 * 
	 * @return the reverse of this move
	 */
	public Move reverse()
	{
		return new Move(end, start);
	}
}
