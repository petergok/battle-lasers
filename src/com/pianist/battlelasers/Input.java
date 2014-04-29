package com.pianist.battlelasers;

import java.util.List;

import android.content.Context;
import android.view.View;

/**
 * The Input class is able to gather and keep track of all user input through
 * touch and key events in the phone
 * 
 * @author Peter Gokhshteyn and "Apress Beginning Android Games"
 * @version Date: June 16, 2013
 */
public class Input
{
	KeyboardHandler keyHandler;

	TouchHandler touchHandler;

	/**
	 * Creates an input object according to the given context and view
	 * 
	 * @param context
	 *            The given context of the phone according to the accelerometer
	 * @param view
	 *            The current view of the phone
	 * @param scaleX
	 *            The scale of the x component of the screen
	 * @param scaleY
	 *            The scale of the y component of the screen
	 */
	public Input(Context context, View view, float scaleX, float scaleY)
	{
		keyHandler = new KeyboardHandler(view);
		touchHandler = new TouchHandler(view, scaleX, scaleY);
	}

	/**
	 * Checks if the key is pressed
	 * 
	 * @param keyCode
	 *            The keyCode corresponding to the pressed key
	 * @return True if the key corresponding to the key code is pressed, false
	 *         if it isn't
	 */
	public boolean isKeyPressed(int keyCode)
	{
		return keyHandler.isKeyPressed(keyCode);
	}

	/**
	 * Checks if the screen is currently being touched by checking if the
	 * pointer is within the touchHandler object
	 * 
	 * @param pointer
	 *            The point being touched on the screen
	 * @return true if the point is on the touchHandler, false otherwise
	 */
	public boolean isTouchDown(int pointer)
	{
		return touchHandler.isTouchDown(pointer);
	}

	/**
	 * Gets the x component of the current touch
	 * 
	 * @param pointer
	 *            The point being touched on the screen
	 * @return the x component of the point currently being touched on the
	 *         screen
	 */
	public int getTouchX(int pointer)
	{
		return touchHandler.getTouchX(pointer);
	}

	/**
	 * Gets the y component of the current touch
	 * 
	 * @param pointer
	 *            The point being touched on the screen
	 * @return the y component of the point currently being touched on the
	 *         screen
	 */
	public int getTouchY(int pointer)
	{
		return touchHandler.getTouchY(pointer);
	}

	/**
	 * Gets all of the touch events that have just been performed
	 * 
	 * @return List of all the touch events that have just occurred
	 */
	public List<TouchEvent> getTouchEvents()
	{
		return touchHandler.getTouchEvents();
	}

	/**
	 * Gets all of the key events that have just been performed
	 * 
	 * @return List of all the key events that have just occurred
	 */
	public List<KeyEvent> getKeyEvents()
	{
		return keyHandler.getKeyEvents();
	}

	/**
	 * The inner class that keeps track of the back key events
	 * 
	 * @author Alex Szoke & Peter Gokhshteyn
	 * @version 16, Jun 2013
	 * 
	 */
	public static class KeyEvent
	{
		public static final int KEY_DOWN = 0;

		public static final int KEY_UP = 1;

		public int type;

		public int keyCode;

		public char keyChar;

		/**
		 * Converts the current setting of the key to a string for debugging
		 */
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			if (type == KEY_DOWN)
				builder.append("key down, ");
			else
				builder.append("key up, ");
			builder.append(keyCode);
			builder.append(",");
			builder.append(keyChar);
			return builder.toString();
		}
	}

	/**
	 * The inner class that keeps track of the touch events
	 * 
	 * @author Alex Szoke & Peter Gokhshteyn
	 * @version 16, Jun 2013
	 * 
	 */
	public static class TouchEvent
	{
		public static final int TOUCH_DOWN = 0;

		public static final int TOUCH_UP = 1;

		public static final int TOUCH_DRAGGED = 2;

		public int type;

		public int x, y;

		public int pointer;

		/**
		 * Converts the current setting of the screen to a string for debugging
		 */
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			if (type == TOUCH_DOWN)
				builder.append("touch down, ");
			else if (type == TOUCH_DRAGGED)
				builder.append("touch dragged, ");
			else
				builder.append("touch up, ");
			builder.append(pointer);
			builder.append(",");
			builder.append(x);
			builder.append(",");
			builder.append(y);
			return builder.toString();
		}
	}
}
