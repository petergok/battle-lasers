package com.pianist.battlelasers;

import java.util.List;

import android.graphics.Color;

import com.pianist.battlelasers.Graphics.PixmapFormat;
import com.pianist.battlelasers.Input.KeyEvent;
import com.pianist.battlelasers.Input.TouchEvent;

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

	private Match match;

	private boolean leavingInstructions;

	private boolean loadImages;

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
	public InstructionsScreen(BattleLaserGame game, int number, Match match,
			boolean loadImages)
	{
		super(game);
		this.number = number;
		this.loadImages = loadImages;
		loaded = false;
		this.match = match;
		leavingInstructions = false;
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

			if (loadImages)
			{
				Assets.gameInstructions1 = g.newPixmap("Instructions1.png",
						PixmapFormat.ARGB4444);
				Assets.gameInstructions2 = g.newPixmap("Instructions2.png",
						PixmapFormat.ARGB4444);
				Assets.gameInstructions3 = g.newPixmap("Instructions3.png",
						PixmapFormat.ARGB4444);
				Assets.gameInstructions4 = g.newPixmap("Instructions4.png",
						PixmapFormat.ARGB4444);
				Assets.gameInstructions5 = g.newPixmap("Instructions5.png",
						PixmapFormat.ARGB4444);
				Assets.rightButtonNor = g.newPixmap("RightButtonNormal.png",
						PixmapFormat.ARGB4444);
				Assets.rightButtonClck = g.newPixmap("RightButtonClicked.png",
						PixmapFormat.ARGB4444);
				Assets.leftButtonNor = g.newPixmap("LeftButtonNormal.png",
						PixmapFormat.ARGB4444);
				Assets.leftButtonClck = g.newPixmap("LeftButtonClicked.png",
						PixmapFormat.ARGB4444);
			}
			rightButton = new Button(288, 733, Assets.rightButtonNor,
					Assets.rightButtonClck);
			leftButton = new Button(108, 733, Assets.leftButtonNor,
					Assets.leftButtonClck);
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
				rightButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				leftButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
			}

			// Immediately change screens to the game screen (change later)
			// Goes forward or backward through the instruction screens
			// according to which direction arrow was pressed
			if (rightButton.wasReleased())
			{
				Screen nextScreen;
				// If the final screen was reached, then the next arrow leads to
				// the main menu
				if (number == 5)
				{
					leavingInstructions = true;
					nextScreen = new MainMenuScreen(game, false, match);
				}
				else
					nextScreen = new InstructionsScreen(game, number + 1,
							match, false);
				game.setScreen(nextScreen);
			}
			else if (leftButton.wasReleased())
			{
				Screen nextScreen;
				// If the instructions are still on their first screen, then the
				// back arrow leads to the main menu
				if (number == 1)
				{
					leavingInstructions = true;
					nextScreen = new MainMenuScreen(game, false, match);
				}
				else
					nextScreen = new InstructionsScreen(game, number - 1,
							match, false);
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
		if (number == 1)
			g.drawPixmap(Assets.gameInstructions1, 0, 0);
		else if (number == 2)
			g.drawPixmap(Assets.gameInstructions2, 0, 0);
		else if (number == 3)
			g.drawPixmap(Assets.gameInstructions3, 0, 0);
		else if (number == 4)
			g.drawPixmap(Assets.gameInstructions4, 0, 0);
		else
			g.drawPixmap(Assets.gameInstructions5, 0, 0);

		rightButton.draw(g);
		leftButton.draw(g);

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
		if (leavingInstructions)
		{
			game.disposeImage(Assets.rightButtonNor);
			game.disposeImage(Assets.rightButtonClck);
			game.disposeImage(Assets.leftButtonNor);
			game.disposeImage(Assets.leftButtonClck);

			game.disposeImage(Assets.gameInstructions1);
			game.disposeImage(Assets.gameInstructions2);
			game.disposeImage(Assets.gameInstructions3);
			game.disposeImage(Assets.gameInstructions4);
			game.disposeImage(Assets.gameInstructions5);
		}
	}

}
