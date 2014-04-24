package com.pianist.battlelasers;

import android.os.Bundle;
import android.os.Build.VERSION;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

/**
 * BattleLaserGame is the main game activity of the project. It handles the
 * creation of the screen as well as the transitioning between them. It also
 * deals stores all the objects that deal with input and output to the user and
 * files.
 * 
 * @author Alex Szoke & Peter Gokhshteyn
 * @version Date: June 16, 2013
 */
public class BattleLaserGame extends Activity
{
	// Deals with drawing the framebuffer to the screen and updating all the
	// game screens
	RenderGraphics renderView;

	// Deals with all the graphics implementation in the game
	Graphics graphics;

	// Deals with all the user input
	Input input;

	// Deals with file input and output
	FileIO fileIO;

	// The current screen that is showing
	Screen screen;

	// The framebuffer width and height
	int frameBufferWidth;

	int frameBufferHeight;

	/**
	 * The main method that gets called on creation of the activity. It
	 * initializes all the variables and starts the running of the program.
	 * 
	 * @param savedInstanceState
	 *            Passed to the super constructor to create the activity
	 */
	public void onCreate(Bundle savedInstanceState)
	{
		// Call the super method to create the activity
		super.onCreate(savedInstanceState);

		// Ask for no window title and fullscreen, as well as to keep the screen
		// from dimming
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Create the framebuffer that is draw to and its dimensions
		boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		frameBufferWidth = isLandscape ? 800 : 480;
		frameBufferHeight = isLandscape ? 480 : 800;

		Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,
				frameBufferHeight, Config.ARGB_8888);

		// Gets the scale factor between the screen and buffer to handle user
		// input
		float scaleX = (float) frameBufferWidth
				/ getWindowManager().getDefaultDisplay().getWidth();
		float scaleY = (float) frameBufferHeight
				/ getWindowManager().getDefaultDisplay().getHeight();

		// Create all the objects that run the game
		renderView = new RenderGraphics(frameBuffer, this);
		graphics = new Graphics(getAssets(), frameBuffer);
		fileIO = new FileIO(this);
		input = new Input(this, renderView, scaleX, scaleY);
		screen = new MainMenuScreen(this, true, new Match());
		setContentView(renderView);
		
		BLSocketIOWrapper.connectTo("http://peaceful-plateau-5934.herokuapp.com:8080");
	}

	/**
	 * When the game is resumed, tell the graphics renderer to start drawing again
	 */
	public void onResume()
	{
		super.onResume();
		screen.resume();
		renderView.resume();
	}

	/**
	 * When the game is paused, tell the graphics to stop rendering, and if the game is closing tell the screen it is being closed
	 */
	public void onPause()
	{
		super.onPause();
		renderView.pause();
		screen.pause();
		if (isFinishing())
			screen.dispose();
	}

	/**
	 * Returns the objects that deals with user input
	 * 
	 * @return the input object
	 */
	public Input getInput()
	{
		return input;
	}

	/**
	 * Returns the object that deals with file input and output
	 * 
	 * @return the input output object
	 */ 
	public FileIO getFileIO()
	{
		return fileIO;
	}

	/**
	 * Returns the object that deals with graphics
	 * 
	 * @return the graphics object
	 */
	public Graphics getGraphics()
	{
		return graphics;
	}

	/**
	 * Switches to a new given screen
	 * 
	 * @param screen
	 * 		The screen to switch to
	 */
	public void setScreen(Screen screen)
	{
		if (screen == null)
			throw new IllegalArgumentException("Screen is null");
		
		// Pause and dispose the current screen
		this.screen.pause();
		this.screen.dispose();
		
		// Resume the new screen and update it
		screen.resume();
		screen.update(0);
		this.screen = screen;
	}

	/**
	 * Returns the current screen
	 * 
	 * @return the current screen
	 */
	public Screen getCurrentScreen()
	{
		return screen;
	}

	/**
	 * Disposes of a given image
	 * 
	 * @param image the image to dispose
	 */
	public void disposeImage(Pixmap image)
	{
		if (image != null)
		{
			image.dispose();
		}
	}
}
