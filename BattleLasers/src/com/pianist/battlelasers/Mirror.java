package com.pianist.battlelasers;

import android.graphics.Point;

/**
 * The Mirror class keeps track of its own position as well as its orientation.
 * The mirror is also able to be double clicked by keeping track of the time
 * since its last tap
 * 
 * @author Alex Szoke & Peter Gokhshteyn
 * @version Date: June 16, 2013
 */
public class Mirror
{

	// These rows and columns correspond to the mirrors position on the grid
	public int row;

	public int col;

	private boolean horizontal;

	private boolean selected;

	private float lastTapTime;

	/**
	 * Constructs a mirror object at the given row and column
	 * 
	 * @param row
	 *            The given row of the mirror
	 * @param col
	 *            The given column of the mirror
	 */
	public Mirror(int row, int col)
	{
		this.row = row;
		this.col = col;
		horizontal = true;
		selected = false;
		lastTapTime = 0;
	}

	/**
	 * Clears the time since the last tap of the mirror, this occurs when the
	 * mirror was not tapped a second time
	 */
	public void clearClicks()
	{
		lastTapTime = 0;
	}

	/**
	 * Checks if the mirror was double tapped according to the difference
	 * between the first tap and this tap
	 * 
	 * @param tapTime
	 *            The current time that the mirror was tapped
	 * @return true if the time interval was small enough that the mirror would
	 *         turn, false otherwise
	 */
	public boolean doubleTap(float tapTime)
	{
		// Checks if the difference in tap times is less then half a second, if
		// it is then the event counts as a double tap
		if (tapTime - lastTapTime < 0.5)
		{
			lastTapTime = tapTime;
			return true;
		}
		else
		{
			lastTapTime = tapTime;
			return false;
		}
	}

	/**
	 * Checks if the mirror is horizontal or not
	 * 
	 * @return true if the mirror is horizontal, false if it is vertical
	 */
	public boolean isHorizontal()
	{
		return horizontal;
	}

	/**
	 * Rotates the mirror 90 degrees
	 */
	public void rotate()
	{
		horizontal = !horizontal;
		selected = false;
	}

	/**
	 * Checks if the mirror is selected or not
	 * 
	 * @return true if the mirror is currently selected, false if it is not
	 */
	public boolean isSelected()
	{
		return selected;
	}

	/**
	 * Deselects the mirror, setting it to not selected even if it was
	 * originally not selected
	 */
	public void deSelect()
	{
		selected = false;
	}

	/**
	 * Changes the state of the mirror, selecting it if it wasn't selected, and
	 * deselecting it if it was selected
	 */
	public void changeState()
	{
		selected = !selected;
	}

	/**
	 * Checks the coordinates of the point to see if they match up with the
	 * mirrors row and column
	 * 
	 * @param point
	 *            The point to check the mirror against
	 * @return True if the point is the same square as the mirror, false if it
	 *         isn't
	 */
	public boolean checkCoordinates(Point point)
	{
		return (point.x == col && point.y == row);
	}

	/**
	 * Checks if the mirror can move to the given point
	 * 
	 * @param point
	 *            The given point to check if the mirror can move to
	 * @return true if the mirror can move to the point, false if it can't
	 */
	public boolean canMove(Point point)
	{
		// First checks against the edges of the board
		if (point.x == 0 || point.x == 7 || point.y == 0 || point.y == 11)
			return false;

		// Then checks against the area around the turrets
		if ((point.y < 2 || point.y > 9) && (point.x == 3 || point.x == 4))
			return false;

		// Then checks if the point is adjacent to the square of this mirrors
		return ((point.x == col && point.y == row - 1)
				|| (point.x == col + 1 && point.y == row)
				|| (point.x == col && point.y == row + 1) || (point.x == col - 1 && point.y == row));
	}

	/**
	 * Moves the mirror from its current position to the given point
	 * 
	 * @param point
	 *            The point to move the mirror to
	 */
	public void move(Point point)
	{
		col = point.x;
		row = point.y;
		selected = false;
	}

	/**
	 * Draws the mirror at its current position according to its set parameters
	 * 
	 * @param g
	 *            The graphics content of the game activity
	 */
	public void draw(Graphics g)
	{
		int x = 25 + col * 62;
		int y = 97 + row * 62;

		// Draws the horizontal or vertical image of the mirror
		if (horizontal)
			g.drawPixmap(Assets.mirrorHorizonal,
					x - Assets.mirrorHorizonal.getWidth() / 2, y
							- Assets.mirrorVertical.getHeight() / 2);
		else
			g.drawPixmap(Assets.mirrorVertical,
					x - Assets.mirrorHorizonal.getWidth() / 2, y
							- Assets.mirrorVertical.getHeight() / 2);

		// Draws the select circle around the mirror if it is selected
		if (selected)
			g.drawPixmap(Assets.mirrorSelect,
					x - Assets.mirrorSelect.getWidth() / 2, y
							- Assets.mirrorSelect.getHeight() / 2);
	}

	/**
	 * Checks if the mirror is equal to the given object
	 * 
	 * @param object
	 *            The object to check the mirror against
	 * @return true if the mirror is equal to the object, false if it isn't
	 */
	public boolean equals(Object object)
	{
		if (object == null)
			return false;
		Mirror otherMirror = (Mirror) object;
		return (this.row == otherMirror.row && this.col == otherMirror.col);
	}
}
