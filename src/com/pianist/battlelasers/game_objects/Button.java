package com.pianist.battlelasers.game_objects;

import com.pianist.battlelasers.graphics.Graphics;
import com.pianist.battlelasers.graphics.Pixmap;
import com.pianist.battlelasers.input_handlers.Input.TouchEvent;

import android.graphics.Rect;

/**
 * Button class is a created button that reacts to user input, it can be
 * clicked, released and drawn
 * 
 * @author Alex Szoke & Peter Gokhshteyn
 * @version Date: June 16, 2013
 */
public class Button
{

	private Pixmap imageNormal;

	private Pixmap imageClick;

	private boolean normal;

	private boolean released;

	private Rect outline;

	/**
	 * Creates the button object at the given x and y coordinates
	 * 
	 * @param x
	 *            The x coordinate of the button
	 * @param y
	 *            The y coordinate of the button
	 * @param imageNormal
	 *            The normal image of the button when it isn't being interacted
	 *            with
	 * @param imageClick
	 *            The image of the button while its being clicked
	 */
	public Button(int x, int y, Pixmap imageNormal, Pixmap imageClick)
	{
		// Creates an outline of the button slightly larger then the size of the
		// actual image, this is so that it is easier to click on
		this.outline = new Rect(x - 10, y - 10,
				x + imageNormal.getWidth() + 20, y + imageNormal.getHeight()
						+ 20);
		this.imageNormal = imageNormal;
		this.imageClick = imageClick;
		normal = true;
		released = false;
	}

	/**
	 * Checks if the given point intersects the button and handles what type of
	 * action occurred
	 * 
	 * @param x
	 *            The x coordinate of the action
	 * @param y
	 *            The y coordinate of the action
	 * @param type
	 *            The type of event occurring to the button
	 */
	public void click(int x, int y, int type)
	{	
		// Handles if the button was released
		if (type == TouchEvent.TOUCH_UP)
		{
			if (outline.contains(x, y) && !normal)
			{
				normal = true;
				released = true;
				return;
			}
			// If the point was not on the button the the buttons parameters are
			// set to their default values
			else
			{
				normal = true;
				released = false;
			}
		}
		// Handles if the button was pressed
		if (type == TouchEvent.TOUCH_DOWN)
		{
			if (outline.contains(x, y))
			{
				normal = false;
			}
		}
	}

	/**
	 * Checks if the button was released after the last event
	 * 
	 * @return true if the button was released, false if it was not
	 */
	public boolean wasReleased()
	{
		if (released)
		{
			released = false;
			return true;
		}
		return false;
	}

	/**
	 * Draws the button based on if it is in its normal stage, or if it is being
	 * pressed
	 * 
	 * @param g
	 *            The graphics component for the button
	 */
	public void draw(Graphics g)
	{
		if (normal)
			g.drawPixmap(imageNormal, outline.left + 10, outline.top + 10);
		else
			g.drawPixmap(imageClick, outline.left + 10, outline.top + 10);
	}
}