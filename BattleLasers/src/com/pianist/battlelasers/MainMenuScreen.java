package com.pianist.battlelasers;

import java.util.List;

import android.graphics.Color;
import android.graphics.Point;

import com.pianist.battlelasers.Graphics.PixmapFormat;
import com.pianist.battlelasers.Input.KeyEvent;
import com.pianist.battlelasers.Input.TouchEvent;

/**
 * The MainMenuScreen class keeps track of the first menu that opens when the
 * game starts up and the buttons on it
 * 
 * @author Alex Szoke & Peter Gokhshteyn
 * @version Date: June 16, 2013
 */
public class MainMenuScreen extends Screen
{
	boolean loaded;

	boolean loadImages;

	Button playGameButton;

	Button instructionsButton;

	Button aboutButton;

	Button exitButton;

	Button aboutBackButton;

	boolean showAbout;

	Match match;

	/**
	 * Creates the main menu with the current activity and the match details
	 * 
	 * @param game
	 *            The game activity that is currently being run
	 * @param loadImages
	 *            A boolean of whether to load the game screen images or not
	 * @param match
	 *            The match option for the past or future game
	 */
	public MainMenuScreen(BattleLaserGame game, boolean loadImages, Match match)
	{
		super(game);
		loaded = false;
		this.loadImages = loadImages;
		match.reset();
		this.match = match;
		showAbout = false;
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

		// If the graphic haven't loaded yet, load them
		if (!loaded)
		{
			loaded = true;

			if (loadImages)
			{
				Assets.gameMenuButtonNor = g.newPixmap(
						"GameMenuButtonNormal.png", PixmapFormat.ARGB4444);
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
				Assets.target = g
						.newPixmap("Target.png", PixmapFormat.ARGB4444);
				Assets.mirrorBorder = g.newPixmap("MirrorBorder.png",
						PixmapFormat.ARGB4444);
				Assets.gameTitleBar = g.newPixmap("GameTitleBar.png",
						PixmapFormat.ARGB4444);

				Assets.horAnimation = new Pixmap[5];
				Assets.verAnimation = new Pixmap[5];
				Assets.tileAnimation = new Pixmap[5];
				for (int index = 0; index < 5; index++)
				{
					Assets.horAnimation[index] = g.newPixmap(
							"HorizonalAnimation" + (index + 1) + ".png",
							PixmapFormat.ARGB4444);
					Assets.verAnimation[index] = g.newPixmap(
							"VerticalAnimation" + (index + 1) + ".png",
							PixmapFormat.ARGB4444);
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

				Assets.timerBar = g.newPixmap("TimerBar.png",
						PixmapFormat.ARGB4444);

				Assets.gunBLHighlight = g.newPixmap(
						"CannonBottomLeftHighlight.png", PixmapFormat.ARGB4444);
				Assets.gunBRHighlight = g
						.newPixmap("CannonBottomRightHighlight.png",
								PixmapFormat.ARGB4444);
				Assets.gunTLHighlight = g.newPixmap(
						"CannonTopLeftHighlight.png", PixmapFormat.ARGB4444);
				Assets.gunTRHighlight = g.newPixmap(
						"CannonTopRightHighlight.png", PixmapFormat.ARGB4444);

				Assets.mainMenuBackground = g.newPixmap(
						"MainMenuBackground.png", PixmapFormat.ARGB4444);

				Assets.playGameButtonNor = g.newPixmap(
						"PlayGameButtonNormal.png", PixmapFormat.ARGB4444);
				Assets.playGameButtonClck = g.newPixmap(
						"PlayGameButtonClicked.png", PixmapFormat.ARGB4444);

				Assets.instructionsButtonNor = g.newPixmap(
						"InstructionsButtonNormal.png", PixmapFormat.ARGB4444);
				Assets.instructionsButtonClck = g.newPixmap(
						"InstructionsButtonClicked.png", PixmapFormat.ARGB4444);
				
				Assets.instMenuButtonNor = g.newPixmap(
						"InstMenuButtonNormal.png", PixmapFormat.ARGB4444);
				Assets.instMenuButtonClck = g.newPixmap(
						"InstMenuButtonClicked.png", PixmapFormat.ARGB4444);

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

			playGameButton = new Button(140, 325, Assets.playGameButtonNor,
					Assets.playGameButtonClck);
			instructionsButton = new Button(140, 415,
					Assets.instructionsButtonNor, Assets.instructionsButtonClck);
			aboutButton = new Button(140, 505, Assets.aboutButtonNor,
					Assets.aboutButtonClck);
			exitButton = new Button(140, 595, Assets.exitButtonNor,
					Assets.exitButtonClck);
			aboutBackButton = new Button(342, 323, Assets.aboutBackButtonNor,
					Assets.aboutBackButtonClck);

		}
		// If the graphics have finished loading
		else
		{
			// Check if the back key was pressed and close the app if it was
			List<KeyEvent> keyEvents = game.getInput().getKeyEvents();
			if (keyEvents.size() > 0
					&& keyEvents.get(0).type == KeyEvent.KEY_UP
					&& keyEvents.get(0).keyCode == android.view.KeyEvent.KEYCODE_BACK)
			{
				if (showAbout)
					showAbout = false;
				else
					game.finish();
			}

			List<TouchEvent> touchEvents = game.getInput().getTouchEvents();
			int size = touchEvents.size();

			// For each touch event, update the buttons based on if the about
			// screen is being shown or not
			for (int event = 0; event < size; event++)
			{
				TouchEvent nextEvent = touchEvents.get(event);
				if (!showAbout)
				{
					playGameButton.click(nextEvent.x, nextEvent.y,
							nextEvent.type);
					instructionsButton.click(nextEvent.x, nextEvent.y,
							nextEvent.type);
					aboutButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
					exitButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				}
				else
				{
					aboutBackButton.click(nextEvent.x, nextEvent.y,
							nextEvent.type);
				}
			}

			// Change screens or state based on what button was pressed
			if (playGameButton.wasReleased())
			{
				Screen screen = new GameSetupScreen(game, false, match);
				game.setScreen(screen);
			}
			if (instructionsButton.wasReleased())
			{
				Screen screen = new InstructionsScreen(game, 1, match, true);
				game.setScreen(screen);
			}
			if (aboutButton.wasReleased())
			{
				showAbout = true;
			}
			if (exitButton.wasReleased())
			{
				game.finish();
			}
			if (aboutBackButton.wasReleased())
			{
				showAbout = false;
			}
		}
	}

	/**
	 * Draws the main menu screen
	 * 
	 * @param deltaTime
	 *            The time since the last frame update
	 */
	public void present(float deltaTime)
	{
		Graphics g = game.getGraphics();

		// Draw the main menu background
		g.drawPixmap(Assets.background, 0, 0);
		g.drawPixmap(Assets.mainMenuBackground, 0, 0);

		// Draw the about screen if it should be open
		if (!showAbout)
		{
			playGameButton.draw(g);
			instructionsButton.draw(g);
			aboutButton.draw(g);
			exitButton.draw(g);
		}
		else
		{
			g.drawPixmap(Assets.aboutBackground,
					240 - Assets.aboutBackground.getWidth() / 2,
					491 - Assets.aboutBackground.getHeight() / 2);
			aboutBackButton.draw(g);
		}

	}

	/**
	 * This is called whenever the screen is paused and switched to another
	 * screen on an android device, it has no special feature in BattleLasers
	 */
	public void pause()
	{
	}

	@Override
	/**
	 * This is called whenever the screen is resumed from another screen 
	 * on the android device, it has no special feature in BattleLasers
	 */
	public void resume()
	{
	}

	@Override
	/**
	 * Dispose is called whenever the screen is destroyed, to reduce the 
	 * memory taken up by the assets they are all disposed of
	 */
	public void dispose()
	{

	}

}
