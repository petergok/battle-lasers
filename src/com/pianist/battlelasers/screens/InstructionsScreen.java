package com.pianist.battlelasers.screens;

import java.util.List;

import android.graphics.Color;

import com.pianist.battlelasers.Assets;
import com.pianist.battlelasers.activities.BattleLaserGame;
import com.pianist.battlelasers.game_objects.Button;
import com.pianist.battlelasers.game_objects.Match;
import com.pianist.battlelasers.graphics.Graphics;
import com.pianist.battlelasers.graphics.Graphics.PixmapFormat;
import com.pianist.battlelasers.input_handlers.Input;
import com.pianist.battlelasers.input_handlers.Input.KeyEvent;
import com.pianist.battlelasers.input_handlers.Input.TouchEvent;

/**
 * The InstructionScreen class is a collection of images that represent the
 * instructions for the game. They only contain two buttons to move forward and
 * back
 * 
 * @author Alex Szoke & Peter Gokhshteyn
 * @version Date: June 16, 2013
 */
public class InstructionsScreen extends Screen
{
	private int number;

	private boolean loaded;

	private Button rightButton;

	private Button leftButton;
	
	private Button menuButton;

	private Match match;
	
	private boolean mIsGuide;

	/**
	 * Creates the instruction screen according to the current page number and
	 * activity
	 * 
	 * @param game
	 *            The game activity that is currently being run
	 * @param number
	 *            The page number of in instruction screen set
	 * @param match
	 *            The specifications of the current match
	 * @param loadImages
	 *            A boolean of whether or not to load the images
	 */
	public InstructionsScreen(BattleLaserGame game, int number, Match match, boolean isGuide)
	{
		super(game, match);
		this.number = number;
		loaded = false;
		this.match = match;
		mIsGuide = isGuide;
	}

	/**
	 * Update is called perpetually while the screen is being shown. Its main
	 * function is to load imaged if they havn't been already, or to check for
	 * touch events from the user
	 * 
	 * @param deltaTime
	 *            The difference in time between the subsequent calls of update
	 */
	public void update(float deltaTime)
	{
		Graphics g = game.getGraphics();
		// If the graphics haven't been loaded then loads them
		if (!loaded)
		{
			loaded = true;
			rightButton = new Button(326, 733, Assets.rightButtonNor,
					Assets.rightButtonClck);
			leftButton = new Button(70, 733, Assets.leftButtonNor,
					Assets.leftButtonClck);
			menuButton = new Button(181, 734, Assets.instMenuButtonNor,
					Assets.instMenuButtonClck);
		}
		// If the graphics have finished then checks through the touch events
		// for button clicks
		else
		{
			List<KeyEvent> keyEvents = game.getInput().getKeyEvents();

			// Goes back to the main menu if the back key was pressed
			if (keyEvents.size() > 0
					&& keyEvents.get(0).type == KeyEvent.KEY_UP
					&& keyEvents.get(0).keyCode == android.view.KeyEvent.KEYCODE_BACK)
			{
				Screen nextScreen = new MainMenuScreen(game, false, match);
				game.setScreen(nextScreen);
			}

			List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
			int size = touchEvents.size();

			for (int event = 0; event < size; event++)
			{
				TouchEvent nextEvent = touchEvents.get(event);
				if (number != 3 || mIsGuide)
					rightButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				if (number != 1)
					leftButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				menuButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
			}

			// Immediately change screens to the game screen (change later)
			// Goes forward or backward through the instruction screens
			// according to which direction arrow was pressed
			if (menuButton.wasReleased())
			{
				Screen nextScreen;
				nextScreen = new MainMenuScreen (game, false, match);
				game.setScreen(nextScreen);
			}
			else if (rightButton.wasReleased())
			{
				if (mIsGuide && number == 3) {
					Screen screen = new GameModeScreen(game, match);
					game.setScreen(screen);
				} else {
					Screen nextScreen;
					nextScreen = new InstructionsScreen(game, number + 1, match, mIsGuide);
					game.setScreen(nextScreen);
				}
			}
			else if (leftButton.wasReleased())
			{
				Screen nextScreen;
				nextScreen = new InstructionsScreen(game, number - 1, match, mIsGuide);
				game.setScreen(nextScreen);
			}
		}

	}

	/**
	 * Draws the set up screen perpetually until the screen is changed
	 * 
	 * @param deltaTime
	 *            The difference in time between subsequent calls of present
	 */
	public void present(float deltaTime)
	{
		Graphics g = game.getGraphics();
		g.drawPixmap(Assets.background, 0, 0);
		if (number == 1)
			g.drawPixmap(Assets.gameInstructions1, 0, 0);
		else if (number == 2)
			g.drawPixmap(Assets.gameInstructions2, 0, 0);
		else
			g.drawPixmap(Assets.gameInstructions3, 0, 0);

		if (number != 3 || mIsGuide)
			rightButton.draw(g);
		if (number != 1)
			leftButton.draw(g);
		menuButton.draw(g);

	}

	/**
	 * This is called whenever the screen is paused and switched to another
	 * screen on an android device, it has no special feature in BattleLasers
	 */
	public void pause()
	{
	}

	/**
	 * This is called whenever the screen is resumed from another screen on the
	 * android device, it has no special feature in BattleLasers
	 */
	public void resume()
	{
	}

	/**
	 * Dispose is called whenever the screen is destroyed, to reduce the memory
	 * taken up by the assets they are all disposed of
	 */
	public void dispose()
	{
	}

}
