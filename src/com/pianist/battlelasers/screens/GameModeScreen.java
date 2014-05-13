package com.pianist.battlelasers.screens;

import java.util.List;

import com.pianist.battlelasers.Assets;
import com.pianist.battlelasers.activities.BattleLaserActivity;
import com.pianist.battlelasers.game_objects.Button;
import com.pianist.battlelasers.game_objects.Match;
import com.pianist.battlelasers.game_objects.AI.AIDifficulty;
import com.pianist.battlelasers.graphics.Graphics;
import com.pianist.battlelasers.graphics.Graphics.PixmapFormat;
import com.pianist.battlelasers.input_handlers.Input;
import com.pianist.battlelasers.input_handlers.Input.KeyEvent;
import com.pianist.battlelasers.input_handlers.Input.TouchEvent;

import android.graphics.Color;

/**
 * The GameModeScreen class is called when preparing the game, it acts as a
 * menu screen to select the game mode
 * 
 * @author Peter Gokhshteyn
 * @version Date: September 9, 2013
 */
public class GameModeScreen extends Screen
{
	// Whether the images have been loaded yet
	boolean loaded;

	// Store the buttons
	Button leftButton;
	
	Button singleButton;
	
	Button localMultButton;
	
	Button onlineMultButton;

	// Store the match
	Match match;

	/**
	 * Starts up the screen with the current game activity and match parameters
	 * 
	 * @param game
	 *            The game activity that is currently being run
	 * @param loadImages
	 *            A boolean of whether to load the game screen images or not
	 * @param match
	 *            A default match object to assign the parameters of the current
	 *            match in the game to
	 */
	public GameModeScreen(BattleLaserActivity game, Match match)
	{
		super(game, match);

		loaded = false;

		match.reset();
		this.match = match;
	}

	/**
	 * Update is called perpetually while the screen is being shown. Its main 
	 * function is to load imaged if they havn't been already, or to check for touch 
	 * events from the user
	 * 
	 * @param deltaTime 
	 * 		The difference in time between the subsequent calls of update
	 */
	public void update(float deltaTime)
	{
		// If the graphics haven't been loaded then loads them
		if (!loaded)
		{
			loaded = true;
			
			// Load all the buttons
			leftButton = new Button(28, 727, Assets.leftButtonNor,
					Assets.leftButtonClck);
			singleButton = new Button(50, 207, Assets.singleButtonNor,
					Assets.singleButtonClck);
			localMultButton = new Button(160, 382, Assets.localMultButtonNor,
					Assets.localMultButtonClck);
			onlineMultButton = new Button(70, 557, Assets.onlineMultButtonNor,
					Assets.onlineMultButtonClck);
		}
		// If the graphics have been loaded
		else
		{
			// Check if the back button was clicked, and return to the main menu if it was
			List<KeyEvent> keyEvents = game.getInput().getKeyEvents();
			if (keyEvents.size() > 0 && keyEvents.get(0).type == KeyEvent.KEY_UP && keyEvents.get(0).keyCode == android.view.KeyEvent.KEYCODE_BACK)
			{
				Screen nextScreen = new MainMenuScreen (game, false, match);
				game.setScreen(nextScreen);
			}
			
			List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
			int size = touchEvents.size();

			// For every touch event
			for (int event = 0; event < size; event++)
			{
				TouchEvent nextEvent = touchEvents.get(event);
				
				// Update the buttons
				leftButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				singleButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				localMultButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				onlineMultButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
			}

			if (leftButton.wasReleased())
			{
				Screen screen = new MainMenuScreen(game, false, match);
				game.setScreen(screen);
			}
			else if (singleButton.wasReleased())
			{
				match.onePlayer = true;
				match.timerOn = true;
				Screen screen = new GameSetupScreen(game, false, match);
				game.setScreen(screen);
			}
			else if (localMultButton.wasReleased())
			{
				match.onePlayer = false;
				Screen screen = new GameSetupScreen(game, false, match);
				game.setScreen(screen);
			} 
			else if (onlineMultButton.wasReleased()) 
			{
				Screen screen = new MultiSetupScreen(game, false, match);
				game.setScreen(screen);
			}
		}
	}

	/**
	 * Draws the screen based on the current match selections
	 * 
	 * @param deltaTime
	 * 		The time since the last frame was drawn
	 */
	public void present(float deltaTime)
	{
		Graphics g = game.getGraphics();
		
		// Draw the background
		g.drawPixmap(Assets.background, 0, 0);
		g.drawPixmap(Assets.gameModeBackground, 0, 0);

		// Draw all the buttons
		leftButton.draw(g);
		singleButton.draw(g);
		localMultButton.draw(g);
		onlineMultButton.draw(g);
	}

	/**
	 * Called when the game is paused
	 */
	public void pause()
	{
	}

	/**
	 * Called when the game is resumed
	 */
	public void resume()
	{
	}

	/**
	 * Called when the screen is being closed or switched. This method recycles
	 * all the images it doesn't need anymore
	 */
	public void dispose()
	{
	}
}
