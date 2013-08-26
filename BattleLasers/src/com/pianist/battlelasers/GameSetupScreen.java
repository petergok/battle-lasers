package com.pianist.battlelasers;

import java.util.List;

import com.pianist.battlelasers.Graphics.PixmapFormat;
import com.pianist.battlelasers.Input.KeyEvent;
import com.pianist.battlelasers.Input.TouchEvent;

import android.graphics.Color;

/**
 * The GameSetupScreen class is called when preparing the game, it acts as a
 * menu screen to create the parameters for a match
 * 
 * @author Alex Szoke & Peter Gokhshteyn
 * @version Date: June 16, 2013
 */
public class GameSetupScreen extends Screen
{
	// Whether the images have been loaded yet
	boolean loaded;

	// Whether to load the game scren images
	boolean loadImages;
	
	boolean startingGame;

	// Store the buttons
	Button leftButton;

	Button rightButton;

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
	public GameSetupScreen(BattleLaserGame game, boolean loadImages, Match match)
	{
		super(game);

		this.loadImages = loadImages;
		loaded = false;

		match.reset();
		this.match = match;
		
		startingGame = false;
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
		Graphics g = game.getGraphics();
		
		// If the graphics haven't been loaded then loads them
		if (!loaded)
		{
			loaded = true;
			
			// If the game screen graphics haven't been loaded yet, load them
			if (loadImages)
			{
				Assets.gameMenuButtonNor = g.newPixmap("GameMenuButtonNormal.png",
						PixmapFormat.ARGB4444);
				Assets.gameMenuButtonClck = g.newPixmap(
						"GameMenuButtonClicked.png", PixmapFormat.ARGB4444);
				Assets.undoButtonNor = g.newPixmap("UndoButtonNormal.png",
						PixmapFormat.ARGB4444);
				Assets.undoButtonClck = g.newPixmap("UndoButtonClicked.png",
						PixmapFormat.ARGB4444);

				Assets.gameBackground = g.newPixmap("GameBackground.png",
						PixmapFormat.ARGB4444);
				Assets.gridHighlight = g.newPixmap("GridHighlight.png",
						PixmapFormat.ARGB4444);
				Assets.shadedTile = g.newPixmap("ShadedTile.png",
						PixmapFormat.ARGB4444);
				Assets.target = g.newPixmap("Target.png", PixmapFormat.ARGB4444);
				Assets.mirrorBorder = g.newPixmap("MirrorBorder.png",
						PixmapFormat.ARGB4444);
				Assets.gameTitleBar = g.newPixmap("GameTitleBar.png",
						PixmapFormat.ARGB4444);

				Assets.horAnimation = new Pixmap[5];
				Assets.verAnimation = new Pixmap[5];
				Assets.tileAnimation = new Pixmap[5];
				for (int index = 0; index < 5; index++)
				{
					Assets.horAnimation[index] = g.newPixmap("HorizonalAnimation"
							+ (index + 1) + ".png", PixmapFormat.ARGB4444);
					Assets.verAnimation[index] = g.newPixmap("VerticalAnimation"
							+ (index + 1) + ".png", PixmapFormat.ARGB4444);
					Assets.tileAnimation[index] = g.newPixmap("TileAnimation"
							+ (index + 1) + ".png", PixmapFormat.ARGB4444);
				}
				Assets.horLine = g.newPixmap("HorizonalLine.png",
						PixmapFormat.ARGB4444);
				Assets.verLine = g.newPixmap("VerticalLine.png",
						PixmapFormat.ARGB4444);

				Assets.mirrorHorizonal = g.newPixmap("MirrorHorizonal.png",
						PixmapFormat.ARGB4444);
				Assets.mirrorVertical = g.newPixmap("MirrorVertical.png",
						PixmapFormat.ARGB4444);

				Assets.gunBL = g.newPixmap("CannonBottomLeft.png",
						PixmapFormat.ARGB4444);
				Assets.gunBR = g.newPixmap("CannonBottomRight.png",
						PixmapFormat.ARGB4444);
				Assets.gunTL = g.newPixmap("CannonTopLeft.png",
						PixmapFormat.ARGB4444);
				Assets.gunTR = g.newPixmap("CannonTopRight.png",
						PixmapFormat.ARGB4444);

				Assets.gunBLSel = g.newPixmap("CannonBottomLeftSelected.png",
						PixmapFormat.ARGB4444);
				Assets.gunBRSel = g.newPixmap("CannonBottomRightSelected.png",
						PixmapFormat.ARGB4444);
				Assets.gunTLSel = g.newPixmap("CannonTopLeftSelected.png",
						PixmapFormat.ARGB4444);
				Assets.gunTRSel = g.newPixmap("CannonTopRightSelected.png",
						PixmapFormat.ARGB4444);

				Assets.timerBar = g
						.newPixmap("TimerBar.png", PixmapFormat.ARGB4444);

				Assets.gunBLHighlight = g.newPixmap(
						"CannonBottomLeftHighlight.png", PixmapFormat.ARGB4444);
				Assets.gunBRHighlight = g.newPixmap(
						"CannonBottomRightHighlight.png", PixmapFormat.ARGB4444);
				Assets.gunTLHighlight = g.newPixmap("CannonTopLeftHighlight.png",
						PixmapFormat.ARGB4444);
				Assets.gunTRHighlight = g.newPixmap("CannonTopRightHighlight.png",
						PixmapFormat.ARGB4444);

				Assets.mainMenuBackground = g.newPixmap("MainMenuBackground.png",
						PixmapFormat.ARGB4444);

				Assets.playGameButtonNor = g.newPixmap("PlayGameButtonNormal.png",
						PixmapFormat.ARGB4444);
				Assets.playGameButtonClck = g.newPixmap(
						"PlayGameButtonClicked.png", PixmapFormat.ARGB4444);

				Assets.instructionsButtonNor = g.newPixmap(
						"InstructionsButtonNormal.png", PixmapFormat.ARGB4444);
				Assets.instructionsButtonClck = g.newPixmap(
						"InstructionsButtonClicked.png", PixmapFormat.ARGB4444);

				Assets.aboutButtonNor = g.newPixmap("AboutButtonNormal.png",
						PixmapFormat.ARGB4444);
				Assets.aboutButtonClck = g.newPixmap("AboutButtonClicked.png",
						PixmapFormat.ARGB4444);

				Assets.exitButtonNor = g.newPixmap("ExitButtonNormal.png",
						PixmapFormat.ARGB4444);
				Assets.exitButtonClck = g.newPixmap("ExitButtonClicked.png",
						PixmapFormat.ARGB4444);

				Assets.aboutBackground = g.newPixmap("AboutBackground.png",
						PixmapFormat.ARGB4444);
				Assets.aboutBackButtonNor = g.newPixmap(
						"AboutBackButtonNormal.png", PixmapFormat.ARGB4444);
				Assets.aboutBackButtonClck = g.newPixmap(
						"AboutBackButtonClicked.png", PixmapFormat.ARGB4444);
				
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
				
				Assets.background = g.newPixmap("Background.png",
						PixmapFormat.ARGB4444);
				
				Assets.gameSetupBackground = g.newPixmap("GameSetupScreen.png",
						PixmapFormat.ARGB4444);
				Assets.mode2PlayerSel = g.newPixmap("DoubleDigitSelect.png",
						PixmapFormat.ARGB4444);
				Assets.mode1PlayerSel = g.newPixmap("1PlayerSelect.png",
						PixmapFormat.ARGB4444);
				Assets.singleDigitSelect = g.newPixmap("SingleDigitSelect.png",
						PixmapFormat.ARGB4444);
				Assets.doubleDigitSelect = g.newPixmap("DoubleDigitSelect.png",
						PixmapFormat.ARGB4444);
				Assets.mixedSelect = g.newPixmap("MixedSelect.png",
						PixmapFormat.ARGB4444);
			}
			
			rightButton = new Button(367, 727, Assets.rightButtonNor,
					Assets.rightButtonClck);
			leftButton = new Button(28, 727, Assets.leftButtonNor,
					Assets.leftButtonClck);
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
				rightButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				leftButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				
				// Check if any of the options in the game setup were selected and changes them
				if (nextEvent.type == TouchEvent.TOUCH_DOWN)
				{
					if (contained(nextEvent.x, nextEvent.y, 108, 217,
							Assets.mode1PlayerSel.getWidth(),
							Assets.mode1PlayerSel.getHeight()))
					{
						match.isComputerDifficult = false;
						match.onePlayer = true;
					}
					else if (contained(nextEvent.x, nextEvent.y, 283, 217,
							Assets.mode1PlayerSel.getWidth(),
							Assets.mode1PlayerSel.getHeight()))
					{
						match.isComputerDifficult = true;
						match.onePlayer = true;
					}
					else if (contained(nextEvent.x, nextEvent.y, 411, 217,
							Assets.mode2PlayerSel.getWidth(),
							Assets.mode2PlayerSel.getHeight()))
						match.onePlayer = false;

					else if (contained(nextEvent.x, nextEvent.y, 67, 368,
							Assets.singleDigitSelect.getWidth(),
							Assets.singleDigitSelect.getHeight()))
						match.numMirrors = 1;
					else if (contained(nextEvent.x, nextEvent.y, 153, 368,
							Assets.singleDigitSelect.getWidth(),
							Assets.singleDigitSelect.getHeight()))
						match.numMirrors = 2;
					else if (contained(nextEvent.x, nextEvent.y, 251, 365,
							Assets.doubleDigitSelect.getWidth(),
							Assets.doubleDigitSelect.getHeight()))
						match.numMirrors = 3;
					else if (contained(nextEvent.x, nextEvent.y, 385, 365,
							Assets.mixedSelect.getWidth(),
							Assets.mixedSelect.getHeight()))
						match.numMirrors = 4;

					else if (contained(nextEvent.x, nextEvent.y, 87, 516,
							Assets.doubleDigitSelect.getWidth(),
							Assets.doubleDigitSelect.getHeight()))
						match.turnLength = 15;
					else if (contained(nextEvent.x, nextEvent.y, 238, 516,
							Assets.doubleDigitSelect.getWidth(),
							Assets.doubleDigitSelect.getHeight()))
						match.turnLength = 30;
					else if (contained(nextEvent.x, nextEvent.y, 392, 516,
							Assets.doubleDigitSelect.getWidth(),
							Assets.doubleDigitSelect.getHeight()))
						match.turnLength = 60;

					else if (contained(nextEvent.x, nextEvent.y, 93, 669,
							Assets.singleDigitSelect.getWidth(),
							Assets.singleDigitSelect.getHeight()))
						match.numGames = 1;
					else if (contained(nextEvent.x, nextEvent.y, 237, 669,
							Assets.singleDigitSelect.getWidth(),
							Assets.singleDigitSelect.getHeight()))
						match.numGames = 3;
					else if (contained(nextEvent.x, nextEvent.y, 383, 669,
							Assets.singleDigitSelect.getWidth(),
							Assets.singleDigitSelect.getHeight()))
						match.numGames = 5;

				}
			}

			// If the left or right buttons were released, change screens
			if (rightButton.wasReleased())
			{
				startingGame = true;
				Screen screen = new GameScreen(game, match);
				game.setScreen(screen);
			}
			else if (leftButton.wasReleased())
			{
				Screen screen = new MainMenuScreen(game, false, match);
				game.setScreen(screen);
			}
		}
	}

	/**
	 * Checks if the given point is contained within the given box
	 * 
	 * @param x
	 *            The x coordinate of the given point
	 * @param y
	 *            The y coordinate of the given point
	 * @param boxX
	 *            The x coordinate of the given box
	 * @param boxY
	 *            The y coordinate of the given box
	 * @param width
	 *            The width of the given box
	 * @param height
	 *            The height of the given box
	 * @return True if the point is contained in the box, false otherwise
	 */
	public boolean contained(int x, int y, int boxX, int boxY, int width,
			int height)
	{
		return (x > boxX - width / 2 && x < boxX + width / 2
				&& y > boxY - height / 2 && y < boxY + height / 2);
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
		g.drawPixmap(Assets.gameSetupBackground, 0, 0);

		// Draw the currently selected game mode
		if (match.onePlayer)
			if (match.isComputerDifficult)
				g.drawPixmap(Assets.mode1PlayerSel,
						283 - Assets.mode1PlayerSel.getWidth() / 2,
						217 - Assets.mode1PlayerSel.getHeight() / 2);
			else
				g.drawPixmap(Assets.mode1PlayerSel,
						108 - Assets.mode1PlayerSel.getWidth() / 2,
						217 - Assets.mode1PlayerSel.getHeight() / 2);
		else
			g.drawPixmap(Assets.mode2PlayerSel,
					411 - Assets.mode2PlayerSel.getWidth() / 2,
					217 - Assets.mode2PlayerSel.getHeight() / 2);

		// Draws the currently selected number of mirrors
		if (match.numMirrors == 1)
			g.drawPixmap(Assets.singleDigitSelect,
					67 - Assets.singleDigitSelect.getWidth() / 2,
					366 - Assets.singleDigitSelect.getHeight() / 2);
		else if (match.numMirrors == 2)
			g.drawPixmap(Assets.singleDigitSelect,
					153 - Assets.singleDigitSelect.getWidth() / 2,
					366 - Assets.singleDigitSelect.getHeight() / 2);
		else if (match.numMirrors == 3)
			g.drawPixmap(Assets.doubleDigitSelect,
					251 - Assets.doubleDigitSelect.getWidth() / 2,
					366 - Assets.doubleDigitSelect.getHeight() / 2);
		else
			g.drawPixmap(Assets.mixedSelect,
					385 - Assets.mixedSelect.getWidth() / 2,
					366 - Assets.mixedSelect.getHeight() / 2);

		// Draws the currently selected turn length
		if (match.turnLength == 15)
			g.drawPixmap(Assets.doubleDigitSelect,
					87 - Assets.doubleDigitSelect.getWidth() / 2,
					516 - Assets.doubleDigitSelect.getHeight() / 2);
		else if (match.turnLength == 30)
			g.drawPixmap(Assets.doubleDigitSelect,
					238 - Assets.doubleDigitSelect.getWidth() / 2,
					516 - Assets.doubleDigitSelect.getHeight() / 2);
		else
			g.drawPixmap(Assets.doubleDigitSelect,
					392 - Assets.doubleDigitSelect.getWidth() / 2,
					516 - Assets.doubleDigitSelect.getHeight() / 2);

		// Draws the currently selected number of games
		if (match.numGames == 1)
			g.drawPixmap(Assets.singleDigitSelect,
					93 - Assets.singleDigitSelect.getWidth() / 2,
					669 - Assets.singleDigitSelect.getHeight() / 2);
		else if (match.numGames == 3)
			g.drawPixmap(Assets.singleDigitSelect,
					237 - Assets.singleDigitSelect.getWidth() / 2,
					669 - Assets.singleDigitSelect.getHeight() / 2);
		else
			g.drawPixmap(Assets.singleDigitSelect,
					383 - Assets.singleDigitSelect.getWidth() / 2,
					669 - Assets.singleDigitSelect.getHeight() / 2);

		// Draws the left and right buttons
		rightButton.draw(g);
		leftButton.draw(g);
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
		if (startingGame)
		{
		game.disposeImage(Assets.gameSetupBackground);
		game.disposeImage(Assets.mode2PlayerSel);
		game.disposeImage(Assets.singleDigitSelect);
		game.disposeImage(Assets.doubleDigitSelect);
		game.disposeImage(Assets.mixedSelect);

		game.disposeImage(Assets.rightButtonNor);
		game.disposeImage(Assets.rightButtonClck);

		game.disposeImage(Assets.leftButtonNor);
		game.disposeImage(Assets.leftButtonClck);
		
		game.disposeImage(Assets.mainMenuBackground);

		game.disposeImage(Assets.playGameButtonNor);
		game.disposeImage(Assets.playGameButtonClck);

		game.disposeImage(Assets.instructionsButtonNor);
		game.disposeImage(Assets.instructionsButtonClck);

		game.disposeImage(Assets.aboutButtonNor);
		game.disposeImage(Assets.aboutButtonClck);

		game.disposeImage(Assets.exitButtonNor);
		game.disposeImage(Assets.exitButtonClck);

		game.disposeImage(Assets.aboutBackButtonNor);
		game.disposeImage(Assets.aboutBackButtonClck);
		game.disposeImage(Assets.aboutBackground);
		}
	}
}
