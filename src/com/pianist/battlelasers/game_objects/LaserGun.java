package com.pianist.battlelasers.game_objects;

import com.pianist.battlelasers.Assets;
import com.pianist.battlelasers.graphics.Graphics;

import android.graphics.Point;

/**
 * The LaserGun class keeps track of the laser gun's position and direction on
 * the game grid
 * 
 * @author Alex Szoke & Peter Gokhshteyn
 */
public class LaserGun
{
	// These rows and columns correspond to the mirrors position on the grid
	private int row;

	private int col;

	private boolean isTop;

	private float holdStartTime;

	// 1 - TR, 2 - TL, 3 - BL, 4 - BR
	private int direction;

	/**
	 * Constructs a LaserGun object with the given position
	 * 
	 * @param row
	 *            The row of the laser gun
	 * @param col
	 *            The column of the laser gun
	 * @param isTop
	 *            Whether or not the gun is the top one on the board
	 */
	public LaserGun(int row, int col, boolean isTop)
	{
		this.row = row;
		this.col = col;
		this.isTop = isTop;
		if (isTop)
			direction = 3;
		else
			direction = 1;
		holdStartTime = -1;
	}

	/**
	 * Sets the time for how long the laser gun has been held down for
	 * 
	 * @param time
	 *            The current time of the move
	 */
	public void startHold(float time)
	{
		holdStartTime = time;
	}

	/**
	 * Checks whether or not the laser gun can shoot according to how long it
	 * has been held down for
	 * 
	 * @param time
	 *            The time when the method was called
	 * @return Whether or not the laser was ultimately shot
	 */
	public boolean shoot(float time)
	{
		// If the laser gun was not held down it can't fire
		if (holdStartTime == -1)
		{
			return false;
		}
		// If the gun was held down for a sufficiently long enough time then the
		// initial time is resent and the shot can be fired
		if (time - holdStartTime >= 0.2)
		{
			holdStartTime = -1;
			return true;
		}
		return false;
	}

	/**
	 * Returns the direction the laser gun is currently facing
	 * 
	 * @return the direction the laser gun is currently facing
	 */
	public int getDirection()
	{
		return direction;
	}

	/**
	 * Turns the laser gun to a different direction based on whether it is the
	 * top or bottom gun, and which way it was facing initially
	 */
	public void turn()
	{
		holdStartTime = -1;
		if (isTop)
			direction = (direction == 3) ? 4 : 3;
		else
			direction = (direction == 1) ? 2 : 1;
	}

	/**
	 * Checks whether or not the given point is contained in the laser gun area
	 * 
	 * @param point
	 *            The given point to check against the laser gun
	 * @return true if the point is within the bounds of the laser gun, false
	 *         otherwise
	 */
	public boolean contains(Point point)
	{
		return ((point.x == col && point.y == row)
				|| (point.x == col + 1 && point.y == row)
				|| (point.x == col + 1 && point.y == row + 1) || (point.x == col && point.y == row + 1));
	}

	/**
	 * Draws the laser gun at its position, if it is the turn of the current
	 * laser gun then it is drawn with a selection image around it
	 * 
	 * @param g
	 *            The graphics content for the game activity
	 * @param isTurn
	 *            Whether or not it's this guns turn to move in the game
	 */
	public void draw(Graphics g, boolean isTurn)
	{
		int x = 86 + (col - 1) * 62;
		int y = 158 + (row - 1) * 62;

		// Draws the normal gun
		if (!isTurn)
		{
			if (direction == 1)
				g.drawPixmap(Assets.gunTR, x, y);
			else if (direction == 2)
				g.drawPixmap(Assets.gunTL, x, y);
			else if (direction == 3)
				g.drawPixmap(Assets.gunBL, x, y);
			else
				g.drawPixmap(Assets.gunBR, x, y);
		}

		// Draws the selected gun
		else
		{
			if (direction == 1)
				g.drawPixmap(Assets.gunTRSel, x, y);

			else if (direction == 2)
				g.drawPixmap(Assets.gunTLSel, x, y);

			else if (direction == 3)
				g.drawPixmap(Assets.gunBLSel, x, y);

			else
				g.drawPixmap(Assets.gunBRSel, x, y);
		}
	}

	/**
	 * Draws the highlight around the laser gun if and only if it is its turn at
	 * the moment
	 * 
	 * @param g
	 *            The graphics content for the game activity
	 * @param isTurn
	 *            Whether or not it's this gun's turn to move
	 */
	public void drawHighlight(Graphics g, boolean isTurn)
	{
		int x = 80 + (col - 1) * 62;
		int y = 152 + (row - 1) * 62;

		if (isTurn)
		{
			if (direction == 1)
				g.drawPixmap(Assets.gunTRHighlight, x, y);
			else if (direction == 2)
				g.drawPixmap(Assets.gunTLHighlight, x, y);
			else if (direction == 3)
				g.drawPixmap(Assets.gunBLHighlight, x, y);
			else
				g.drawPixmap(Assets.gunBRHighlight, x, y);
		}
	}
}