package com.pianist.battlelasers.screens;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.pianist.battlelasers.Assets;
import com.pianist.battlelasers.activities.BattleLaserGame;
import com.pianist.battlelasers.game_objects.AI;
import com.pianist.battlelasers.game_objects.Button;
import com.pianist.battlelasers.game_objects.LaserGun;
import com.pianist.battlelasers.game_objects.Match;
import com.pianist.battlelasers.game_objects.Mirror;
import com.pianist.battlelasers.game_objects.Move;
import com.pianist.battlelasers.game_objects.Match.Layout;
import com.pianist.battlelasers.graphics.Graphics;
import com.pianist.battlelasers.graphics.Pixmap;
import com.pianist.battlelasers.graphics.Graphics.PixmapFormat;
import com.pianist.battlelasers.input_handlers.Input;
import com.pianist.battlelasers.input_handlers.Input.KeyEvent;
import com.pianist.battlelasers.input_handlers.Input.TouchEvent;
import com.pianist.battlelasers.tasks.MakeMoveTask;
import com.pianist.battlelasers.tasks.UnregisterPlayerTask;

import android.graphics.Color;
import android.graphics.Point;

/**
 * The GameScreen class is the main screen of the game, it handles the board
 * that the game takes place in as well as all of the user input for making
 * moves
 * 
 * @author Alex Szoke & Peter Gokhshteyn
 * @version Date: June 16, 2013
 */
public class GameScreen extends Screen
{
	// The grid and list of mirrors that describes the grid (stored in two ways
	// for efficiency)
	private ArrayList<Mirror> mirrors;

	private Mirror[][] grid;

	// The stack of moves that is used for undoing
	private LinkedList<Move> gameMoves;

	// Both player guns
	private LaserGun playerTwo;

	private LaserGun playerOne;

	// Whether to shoot the laser or not
	private boolean shootLaser;

	// Whether it is the first players turn
	private boolean playerOneTurn;

	// All the buttons that appear in the game screen
	private Button menuButton;

	private Button undoButton;

	private Button resumeButton;

	private Button closeButton;

	private Button restartButton;

	private Button newButton;

	private Button nextButton;

	private Button mainButton;

	// Time to store that are used for animation and turn length
	private float timeSinceStart;

	private float laserStartTime;

	private float winStartTime;

	private float laserDrawEnd;

	private float turnStart;

	// The speed of the laser animating (pixels per second)

	private final int ANIMATION_SPEED = 1000;

	// The number of bounces allowed off the back
	private int numBouncesAllowed;

	// The length of a turn in seconds
	private int turnLength;

	// The speed of the winning animation
	private final float WINNING_SPEED = 0.25f;

	// Whether to show the winner pop-up or not
	private boolean showWinner;

	// The start and end of the last move (used for preventing undoing)
	private Point lastMoveStart;

	private Point lastMoveEnd;

	// Whether to show the menu or not
	private boolean showMenu;

	// Lets the user release once without input feedback
	private boolean letRelease;

	// Freezes the timer bar
	private boolean freezeTime;

	// The match that describes the current game
	private Match match;

	// The computer player
	private AI computerPlayer;

	// The state of the game
	private GameState state;

	/**
	 * The GameState describes all the states that the game could be in
	 * 
	 * @author Peter Gokhshteyn and Alex Szoke
	 * @version June 10, 2013
	 */
	private enum GameState {
		Animate, TapToStart, Running, AnimatingLaser, WinningAnimation, TimeRanOut;
	}

	/**
	 * Creates the basic game screen and initializes all of the parameters for
	 * the match
	 * 
	 * @param game
	 *            The BattleLasersGame activity that is currently being played
	 * @param match
	 *            The parameters for the current match, including whether it is
	 *            one player or two player, how long a turn is and how many
	 *            mirrors are involved
	 */
	public GameScreen(BattleLaserGame game, Match match)
	{
		super(game, match);

		this.match = match;
		grid = new Mirror[12][8];
		mirrors = new ArrayList<Mirror>();
		gameMoves = new LinkedList<Move>();

		// Gets the current match's mirror layout and loads it into the grid and
		// list
		Layout nextLayout;
		if (!match.isOnline) {
			nextLayout = match.getNextLayout();
		} else {
			if (match.playerNumberForOnline == 2) {
				match.currentLayout.generatePositions(true);
			} else {
				match.currentLayout.generatePositions(false);
			}
			nextLayout = match.currentLayout;
		}
		Point nextPoint;
		while ((nextPoint = nextLayout.getNextPosition()) != null)
		{
			Mirror newMirror = new Mirror(nextPoint.x, nextPoint.y);
			mirrors.add(newMirror);
			grid[nextPoint.x][nextPoint.y] = newMirror;

			if (!nextLayout.isNextHorizontal())
				newMirror.rotate();
		}

		// If the match is a one player game, load the AI
		if (match.onePlayer)
			computerPlayer = new AI(grid, mirrors, match.computerDifficulty);

		// Create the two laser guns
		playerTwo = new LaserGun(0, 3, true);
		playerOne = new LaserGun(10, 3, false);

		// Create the menu and undo buttons at the top
		undoButton = new Button(117, 17, Assets.undoButtonNor,
				Assets.undoButtonClck);
		menuButton = new Button(257, 17, Assets.gameMenuButtonNor,
				Assets.gameMenuButtonClck);

		// Initialize other important variables to default
		if (match.isOnline && match.playerNumberForOnline == 2) {
			playerOneTurn = false;
		} else {
			playerOneTurn = true;
		}
		shootLaser = false;
		timeSinceStart = 0;
		laserStartTime = 0;
		winStartTime = 0;
		state = GameState.Animate;
		showWinner = false;
		laserDrawEnd = 0;

		turnLength = match.turnLength;
		numBouncesAllowed = 1;

		lastMoveStart = null;
		lastMoveEnd = null;

		showMenu = false;
		letRelease = false;
		freezeTime = false;
	}

	/**
	 * Update is called perpetually through the running of the activity. This
	 * constantly updates the the screen by checking for events
	 * 
	 * @param detalTime
	 *            The time since the last call of update
	 */
	public synchronized void update(float deltaTime)
	{
		// Update the time variables based on game state
		timeSinceStart += deltaTime;
		laserStartTime += deltaTime;
		if (!match.timerOn || freezeTime || state == GameState.TapToStart
				|| shootLaser || (showMenu && !match.isOnline))
		{
			turnStart += deltaTime;
		}

		// Check for key events (in particular the back key)
		List<KeyEvent> keyEvents = game.getInput().getKeyEvents();
		if (keyEvents.size() > 0
				&& keyEvents.get(0).type == KeyEvent.KEY_UP
				&& keyEvents.get(0).keyCode == android.view.KeyEvent.KEYCODE_BACK)
		{
			// If the back key was pressed, change the state of the game based
			// on the current state
			if (state == GameState.TapToStart) 
			{
				state = GameState.Running;
				match.matchStarted = true;
			}
			else if (showWinner)
				showWinner = false;
			else if (state == GameState.TimeRanOut && (!match.isOnline || playerOneTurn))
			{
				lastMoveStart = null;
				lastMoveEnd = null;
				changeTurns();
			}
			else if (state == GameState.Animate)
				return;
			else if (state == GameState.AnimatingLaser)
			{
				state = GameState.Running;
				laserDrawEnd = timeSinceStart;
			}

			showMenu = !showMenu;
			return;
		}
		// If the current state is animating, ignore other user input
		if (state == GameState.Animate)
			return;

		// Automatically switches turns 0.8 seconds after shooting
		if (state == GameState.Running)
		{
			if (shootLaser && timeSinceStart - laserDrawEnd > 0.8)
			{
				shootLaser = false;
				changeTurns();
				return;
			}
		}

		// If it is not the AI's turn, check if the player has held the gun long
		// enough to shoot and shoot if they did
		if ((!match.isOnline && !match.onePlayer) || playerOneTurn)
		{
			if (state == GameState.Running
					&& ((playerOneTurn && playerOne.shoot(timeSinceStart)) || (!playerOneTurn && playerTwo
							.shoot(timeSinceStart))))
			{
				state = GameState.AnimatingLaser;
				letRelease = true;
				shootLaser = true;
				laserStartTime = 0;
				lastMoveStart = null;
				lastMoveEnd = null;
				freezeTime = false;
				clearMirrors();
			}
		}

		// Get all the touch events that happened since the last call of update
		List<TouchEvent> touchEvents = game.getInput().getTouchEvents();

		// Get the size of the touch events list
		int size = touchEvents.size();

		// For each event
		for (int event = 0; event < size; event++)
		{
			// Get the event
			TouchEvent nextEvent = touchEvents.get(event);

			// If the state is to tap to start and the event was an up event
			// change the game state to running
			// Otherwise just return ignoring other input
			if (state == GameState.TapToStart)
			{
				if (nextEvent.type == TouchEvent.TOUCH_UP)
				{
					state = GameState.Running;
				}
				return;
			}

			// If the state is to run out of time and the event was an up event
			// change turns
			// Otherwise just return ignoring other input
			if (state == GameState.TimeRanOut)
			{
				if (nextEvent.type == TouchEvent.TOUCH_UP)
				{
					lastMoveStart = null;
					lastMoveEnd = null;
					changeTurns();
				}
				return;
			}

			/**
			 * If the menu is open right now, update the menu buttons
			 */
			if (showMenu)
			{
				mainButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				if (mainButton.wasReleased())
				{
					if (match.isOnline) 
					{
						new UnregisterPlayerTask(match.onlineUserId).execute();
					}
					Screen nextScreen = new MainMenuScreen(game, true, match);
					game.setScreen(nextScreen);
					disposeAnimationImages();
				}

				// Based on if the game is over and how games were played, show
				// the restart, close, next or new button and update it
				if (state != GameState.WinningAnimation)
				{
					resumeButton
							.click(nextEvent.x, nextEvent.y, nextEvent.type);
					restartButton.click(nextEvent.x, nextEvent.y,
							nextEvent.type);
					if (resumeButton.wasReleased())
					{
						showMenu = false;
					}
					else if (restartButton.wasReleased())
					{
						Screen newScreen = new GameScreen(game, match);
						game.setScreen(newScreen);
					}
				}
				else
				{
					closeButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
					if (closeButton.wasReleased())
					{
						showMenu = false;
					}

					if (match.playerOneScore == match.numGames / 2 + 1
							|| match.playerTwoScore == match.numGames / 2 + 1)
					{
						newButton.click(nextEvent.x, nextEvent.y,
								nextEvent.type);
						if (newButton.wasReleased())
						{
							if (match.isOnline) 
							{
								new UnregisterPlayerTask(match.onlineUserId).execute();
							}
								
							Screen nextScreen = new GameSetupScreen(game, true,
									match);
							game.setScreen(nextScreen);
						}
					}
					else
					{
						nextButton.click(nextEvent.x, nextEvent.y,
								nextEvent.type);
						if (nextButton.wasReleased())
						{
							match.nextGame();
							Screen nextScreen = new GameScreen(game, match);
							game.setScreen(nextScreen);
						}
					}
				}
				return;
			}
			// If the game is not animating the laser and it is not showing the
			// menu, update the top buttons
			else if (state != GameState.AnimatingLaser && !showWinner)
			{
				menuButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				undoButton.click(nextEvent.x, nextEvent.y, nextEvent.type);
				if (menuButton.wasReleased())
				{
					showMenu = true;
					return;
				}
				else if (undoButton.wasReleased()
						&& state != GameState.WinningAnimation)
				{
					undoMove();
					return;
				}
			}

			// If the next event was a down press and it is not the Computer's
			// turn
			if (nextEvent.type == TouchEvent.TOUCH_DOWN
					&& ((!match.isOnline && !match.onePlayer) || playerOneTurn))
			{
				Point selectedPoint = getGridCoordinates(nextEvent.x,
						nextEvent.y);

				// If the selection was player two's gun, update its state
				if (playerTwo.contains(selectedPoint) && nextEvent.y > 75)
				{
					if (!playerOneTurn)
					{
						playerTwo.startHold(timeSinceStart);
						freezeTime = true;
						clearMirrors();
					}
				}
				// If the selection was player one's gun, update its state
				else if (playerOne.contains(selectedPoint))
				{
					if (playerOneTurn)
					{
						playerOne.startHold(timeSinceStart);
						freezeTime = true;
						clearMirrors();
					}
				}
			}

			// If the next event was a release event
			if (nextEvent.type == TouchEvent.TOUCH_UP)
			{
				// Ignore this input once if necessary
				if (letRelease)
				{
					letRelease = false;
					return;
				}

				// If a player won a game and it is showing the winner pop-up,
				// close that pop-up
				if (state == GameState.WinningAnimation)
				{
					if (showWinner)
					{
						showWinner = false;
						showMenu = true;
					}
					return;
				}

				// If the game is animating the laser, finish the animation
				if (state == GameState.AnimatingLaser)
				{
					state = GameState.Running;
					laserDrawEnd = timeSinceStart;
					return;
				}
				// Otherwise, if it already finished the animation, switch turns
				else if (shootLaser)
				{
					shootLaser = false;
					changeTurns();
					return;
				}

				// If it is not the AI's turn
				if ((!match.isOnline && !match.onePlayer) || playerOneTurn)
				{
					Point selectedPoint = getGridCoordinates(nextEvent.x,
							nextEvent.y);

					// Check if the player turned their turret and update it
					// appropriately (this won't be called if the gun is held
					// because the game would be in a different state)
					if (playerTwo.contains(selectedPoint) && nextEvent.y > 75)
					{
						if (!playerOneTurn)
						{
							playerTwo.turn();
							freezeTime = false;
							clearMirrors();
						}
					}
					else if (playerOne.contains(selectedPoint))
					{
						if (playerOneTurn)
						{
							playerOne.turn();
							freezeTime = false;
							clearMirrors();
						}
					}
					else
					{
						// If the player didn't select their gun, find the
						// mirror at the selected point
						Mirror clickMirror = grid[selectedPoint.y][selectedPoint.x];
						Point reverseSelected = new Point(selectedPoint.y,
								selectedPoint.x);

						// Get the currently selected mirror
						Mirror selectedMirror = currentSelectedMirror();

						// If there is a mirror at that point
						if (clickMirror != null)
						{
							// Select or unselect the mirror
							clickMirror.changeState();

							// If a different mirror is selected, deselect the
							// old one
							if (selectedMirror != null
									&& !clickMirror.equals(selectedMirror))
								selectedMirror.deSelect();

							// If the mirror was double tapped, rotate it and
							// change turns
							if (clickMirror.doubleTap(timeSinceStart)
									&& (lastMoveStart == null || !lastMoveStart
											.equals(reverseSelected)))
							{
								clickMirror.rotate();
								lastMoveStart = new Point(clickMirror.row,
										clickMirror.col);
								lastMoveEnd = lastMoveStart;
								changeTurns();
							}
						}
						else
						{
							// If the selected point does not contain a mirror
							// and there is a mirror selected, check if it can
							// move to the new position
							if (selectedMirror != null
									&& selectedMirror.canMove(selectedPoint)
									&& (lastMoveEnd == null
											|| !lastMoveEnd.equals(new Point(
													selectedMirror.row,
													selectedMirror.col)) || !lastMoveStart
												.equals(reverseSelected)))
							{
								// Move the mirror and change turns if it can
								grid[selectedMirror.row][selectedMirror.col] = null;
								lastMoveStart = new Point(selectedMirror.row,
										selectedMirror.col);
								selectedMirror.move(selectedPoint);
								grid[selectedPoint.y][selectedPoint.x] = selectedMirror;
								lastMoveEnd = reverseSelected;
								changeTurns();
							}
							// Clear the mirrors otherwise
							else
								clearMirrors();
						}
					}
				}
			}
		}

		// If it is the AI's turn, check if it is done calculating a move and
		// then make that move if it did
		if (match.onePlayer && !playerOneTurn && !showMenu)
		{
			if (computerPlayer.isFinishedCalculating())
			{
				Move computerMove = computerPlayer.getMove();
				computerPlayer.userMadeMove(computerMove, false);
				makeMove(computerMove);
				computerPlayer.checkAIWin();
			}
		}

		// Update the game state based on how much time passed since the time
		// ran out
		if (timeSinceStart - turnStart >= turnLength + 0.2 && (!match.isOnline || playerOneTurn))
		{
			state = GameState.TimeRanOut;
		}
		if (timeSinceStart - turnStart >= turnLength + 2.5 && (!match.isOnline || playerOneTurn))
		{
			lastMoveStart = null;
			lastMoveEnd = null;
			changeTurns();
		}
	}

	/**
	 * Makes a move by updating the grid and changing turns. Used for AI
	 * 
	 * @param move
	 *            The move to make
	 */
	private void makeMove(Move move)
	{
		// Retrieve the starting and ending points of the move
		Point startPoint = move.getStartPoint();
		Point endPoint = move.getEndPoint();

		// If it is not a shoot move
		if (startPoint != null && endPoint != null)
		{
			// Turn or move accordingly
			if (startPoint.equals(endPoint))
			{
				Mirror movedMirror = grid[endPoint.x][endPoint.y];
				movedMirror.rotate();
			}
			else
			{
				Mirror movedMirror = grid[startPoint.x][startPoint.y];
				grid[endPoint.x][endPoint.y] = grid[startPoint.x][startPoint.y];
				grid[startPoint.x][startPoint.y] = null;
				movedMirror.move(new Point(endPoint.y, endPoint.x));
			}

			// Update the last move points and change turns
			lastMoveStart = startPoint;
			lastMoveEnd = endPoint;
			changeTurns();
		}
		// Otherwise, shoot the laser
		else
		{
			// Check if the AI specified to turn the gun
			if (move.turnRight())
			{
				if (playerTwo.getDirection() == 4)
					playerTwo.turn();
			}
			else if (playerTwo.getDirection() == 3)
				playerTwo.turn();

			state = GameState.AnimatingLaser;
			shootLaser = true;
			laserStartTime = 0;
			lastMoveStart = null;
			lastMoveEnd = null;
			letRelease = false;
			clearMirrors();
		}
	}

	/**
	 * Draws the laser in the given Graphics context
	 * 
	 * @param g
	 *            The graphics to draw the laser to
	 */
	private void drawLaser(Graphics g)
	{
		// Calculate the number of full segments to draw and the leftover
		// segments for the animation
		int numOfSegments = (state == GameState.AnimatingLaser) ? (int) (ANIMATION_SPEED
				* laserStartTime / 62)
				: Integer.MAX_VALUE;
		int pixelsLeft = (int) (ANIMATION_SPEED * laserStartTime - (int) (ANIMATION_SPEED
				* laserStartTime / 62) * 62);

		// Find the start point and direction of the laser based on the guns
		// orientation and player turn
		// Note: directions are same as math quadrants
		Point laserStart;
		int laserDirection;

		if (!playerOneTurn)
		{
			laserDirection = playerTwo.getDirection();
			if (laserDirection == 3)
				laserStart = new Point(3, 1);
			else
				laserStart = new Point(4, 1);
		}
		else
		{
			laserDirection = playerOne.getDirection();
			if (laserDirection == 1)
				laserStart = new Point(4, 10);
			else
				laserStart = new Point(3, 10);
		}

		// Store which spaces have been visited in order to draw a smooth
		// cross-over
		boolean[][] occupied = new boolean[12][8];

		// Store the next point, the number of back bounces and
		Point nextPoint = new Point(laserStart);
		int numBackBounces = 0;
		int segmentsDone = 0;

		// Whether to draw a horizonal bounce or vertical bounce or neither
		boolean horBounce = false;
		boolean verBounce = false;

		// While there are still segments to draw in the animation
		while (segmentsDone <= numOfSegments)
		{
			// Get the drawing position
			Point drawPoint = getDrawCoordinates(nextPoint.y, nextPoint.x);

			// If the next point is bouncing off the side
			if (nextPoint.x == 0 || nextPoint.x == 7)
			{
				// If the next point is in the corner
				if (nextPoint.y == 0 || nextPoint.y == 11)
				{
					// If the animation is finished
					if (segmentsDone < numOfSegments)
					{
						// Draw the last segment as a line and update the game
						// state
						if (laserDirection % 2 == 1)
							g.drawPixmap(
									Assets.laserSegPos,
									drawPoint.x - Assets.laserSegPos.getWidth()
											/ 2,
									drawPoint.y
											- Assets.laserSegPos.getHeight()
											/ 2 - 1);
						else
							g.drawPixmap(
									Assets.laserSegNeg,
									drawPoint.x - Assets.laserSegPos.getWidth()
											/ 2,
									drawPoint.y
											- Assets.laserSegPos.getHeight()
											/ 2 - 1);
						if (state == GameState.AnimatingLaser)
							laserDrawEnd = timeSinceStart;
						state = GameState.Running;
						return;
					}
					// Otherwise, animate the laser based on the number of
					// pixels left and direction and update the game state
					else
					{
						if (laserDirection == 1)
						{
							g.drawPixmap(Assets.laserTipTR, drawPoint.x - 31
									+ pixelsLeft - Assets.laserTipTR.getWidth()
									/ 2, drawPoint.y + 31 - pixelsLeft
									- Assets.laserTipTR.getHeight() / 2);
							g.drawPixmap(
									Assets.laserSegPos,
									drawPoint.x - Assets.laserSegPos.getWidth()
											/ 2,
									drawPoint.y
											+ Assets.laserSegPos.getHeight()
											/ 2 - pixelsLeft - 11, 0,
									Assets.laserSegPos.getHeight() - pixelsLeft
											- 11, pixelsLeft + 11,
									pixelsLeft + 11);
						}
						else if (laserDirection == 2)
						{
							g.drawPixmap(Assets.laserTipTL, drawPoint.x + 31
									- pixelsLeft - Assets.laserTipTL.getWidth()
									/ 2, drawPoint.y + 31 - pixelsLeft
									- Assets.laserTipTL.getHeight() / 2);
							g.drawPixmap(Assets.laserSegNeg, drawPoint.x
									+ Assets.laserSegNeg.getWidth() / 2
									- pixelsLeft - 11, drawPoint.y
									+ Assets.laserSegPos.getHeight() / 2
									- pixelsLeft - 11,
									Assets.laserSegNeg.getWidth() - pixelsLeft
											- 11,
									Assets.laserSegNeg.getHeight() - pixelsLeft
											- 11, pixelsLeft + 11,
									pixelsLeft + 11);
						}
						else if (laserDirection == 3)
						{
							g.drawPixmap(Assets.laserTipBL, drawPoint.x + 31
									- pixelsLeft - Assets.laserTipBL.getWidth()
									/ 2, drawPoint.y - 31 + pixelsLeft
									- Assets.laserTipBL.getHeight() / 2);
							g.drawPixmap(Assets.laserSegPos, drawPoint.x
									+ Assets.laserSegPos.getWidth() / 2
									- pixelsLeft - 11, drawPoint.y
									- Assets.laserSegPos.getHeight() / 2,
									Assets.laserSegPos.getWidth() - pixelsLeft
											- 11, 0, pixelsLeft + 11,
									pixelsLeft + 11);
						}
						else
						{
							g.drawPixmap(Assets.laserTipBR, drawPoint.x - 31
									+ pixelsLeft - Assets.laserTipBR.getWidth()
									/ 2, drawPoint.y - 31 + pixelsLeft
									- Assets.laserTipBR.getHeight() / 2);
							g.drawPixmap(
									Assets.laserSegNeg,
									drawPoint.x - Assets.laserSegNeg.getWidth()
											/ 2,
									drawPoint.y
											- Assets.laserSegNeg.getHeight()
											/ 2, 0, 0, pixelsLeft + 11,
									pixelsLeft + 11);
						}
						state = GameState.AnimatingLaser;
						return;
					}
				}

				// If it didn't hit the corner, turn the laser appropriately
				if (laserDirection == 1)
					laserDirection = 2;
				else if (laserDirection == 2)
					laserDirection = 1;
				else if (laserDirection == 3)
					laserDirection = 4;
				else
					laserDirection = 3;

				// Specify to draw a vertical bounce
				verBounce = true;
			}
			// If the next point is off the back wall
			else if (nextPoint.y == 0 || nextPoint.y == 11)
			{
				// If it is the last bounce and the animation is finished, draw
				// the last segment as a line and change the game state
				if (numBackBounces == numBouncesAllowed
						&& segmentsDone < numOfSegments)
				{
					if (laserDirection % 2 == 1)
						g.drawPixmap(Assets.laserSegPos, drawPoint.x
								- Assets.laserSegPos.getWidth() / 2,
								drawPoint.y - Assets.laserSegPos.getHeight()
										/ 2 - 1);
					else
						g.drawPixmap(Assets.laserSegNeg, drawPoint.x
								- Assets.laserSegPos.getWidth() / 2,
								drawPoint.y - Assets.laserSegPos.getHeight()
										/ 2 - 1);
					if (state == GameState.AnimatingLaser)
						laserDrawEnd = timeSinceStart;
					state = GameState.Running;
					return;
				}
				// Otherwise, if it is the last bounce, but is still animating,
				// draw the animation and update the game state
				else if (numBackBounces == numBouncesAllowed
						&& segmentsDone == numOfSegments)
				{
					if (laserDirection == 1)
					{
						g.drawPixmap(Assets.laserTipTR,
								drawPoint.x - 31 + pixelsLeft
										- Assets.laserTipTR.getWidth() / 2,
								drawPoint.y + 31 - pixelsLeft
										- Assets.laserTipTR.getHeight() / 2);
						g.drawPixmap(Assets.laserSegPos, drawPoint.x
								- Assets.laserSegPos.getWidth() / 2,
								drawPoint.y + Assets.laserSegPos.getHeight()
										/ 2 - pixelsLeft - 11, 0,
								Assets.laserSegPos.getHeight() - pixelsLeft
										- 11, pixelsLeft + 11, pixelsLeft + 11);
					}
					else if (laserDirection == 2)
					{
						g.drawPixmap(Assets.laserTipTL,
								drawPoint.x + 31 - pixelsLeft
										- Assets.laserTipTL.getWidth() / 2,
								drawPoint.y + 31 - pixelsLeft
										- Assets.laserTipTL.getHeight() / 2);
						g.drawPixmap(
								Assets.laserSegNeg,
								drawPoint.x + Assets.laserSegNeg.getWidth() / 2
										- pixelsLeft - 11,
								drawPoint.y + Assets.laserSegPos.getHeight()
										/ 2 - pixelsLeft - 11,
								Assets.laserSegNeg.getWidth() - pixelsLeft - 11,
								Assets.laserSegNeg.getHeight() - pixelsLeft
										- 11, pixelsLeft + 11, pixelsLeft + 11);
					}
					else if (laserDirection == 3)
					{
						g.drawPixmap(Assets.laserTipBL,
								drawPoint.x + 31 - pixelsLeft
										- Assets.laserTipBL.getWidth() / 2,
								drawPoint.y - 31 + pixelsLeft
										- Assets.laserTipBL.getHeight() / 2);
						g.drawPixmap(
								Assets.laserSegPos,
								drawPoint.x + Assets.laserSegPos.getWidth() / 2
										- pixelsLeft - 11,
								drawPoint.y - Assets.laserSegPos.getHeight()
										/ 2,
								Assets.laserSegPos.getWidth() - pixelsLeft - 11,
								0, pixelsLeft + 11, pixelsLeft + 11);
					}
					else
					{
						g.drawPixmap(Assets.laserTipBR,
								drawPoint.x - 31 + pixelsLeft
										- Assets.laserTipBR.getWidth() / 2,
								drawPoint.y - 31 + pixelsLeft
										- Assets.laserTipBR.getHeight() / 2);
						g.drawPixmap(Assets.laserSegNeg, drawPoint.x
								- Assets.laserSegNeg.getWidth() / 2,
								drawPoint.y - Assets.laserSegNeg.getHeight()
										/ 2, 0, 0, pixelsLeft + 11,
								pixelsLeft + 11);
					}
					state = GameState.AnimatingLaser;
					return;
				}
				// If it isn't the last bounce, add this bounce to the number of
				// back bounces
				else
					numBackBounces++;

				// Update the direction of the laser
				if (laserDirection == 1)
					laserDirection = 4;
				else if (laserDirection == 4)
					laserDirection = 1;
				else if (laserDirection == 3)
					laserDirection = 2;
				else
					laserDirection = 3;

				// Specify to draw a horizontal bounce
				horBounce = true;
			}
			// If there are no bounces off the side
			else
			{
				// Retreave the mirror at the next position
				Mirror mirror = grid[nextPoint.y][nextPoint.x];

				// If there is a mirror there
				if (mirror != null)
				{
					// Turn the laser and update what to draw based on its
					// orientation
					if (mirror.isHorizontal())
					{
						if (laserDirection == 1)
							laserDirection = 4;
						else if (laserDirection == 4)
							laserDirection = 1;
						else if (laserDirection == 3)
							laserDirection = 2;
						else
							laserDirection = 3;

						horBounce = true;
					}
					else
					{
						if (laserDirection == 1)
							laserDirection = 2;
						else if (laserDirection == 2)
							laserDirection = 1;
						else if (laserDirection == 3)
							laserDirection = 4;
						else
							laserDirection = 3;

						verBounce = true;
					}
				}
				// If there is not mirror at the next draw point
				else
				{
					// If it was already occupied (cross-over)
					if (occupied[nextPoint.y][nextPoint.x])
					{
						// If it is animating the current segment
						if (segmentsDone == numOfSegments)
						{
							// Draw the animation based on the direction and
							// number of pixels left to draw
							if (pixelsLeft < 25)
							{
								if (laserDirection == 1)
								{
									g.drawPixmap(
											Assets.laserTipTR,
											drawPoint.x
													- 31
													+ pixelsLeft
													- Assets.laserTipTR
															.getWidth() / 2,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTR
															.getHeight() / 2);
									g.drawPixmap(
											Assets.laserSegPos,
											drawPoint.x
													- Assets.laserSegPos
															.getWidth() / 2,
											drawPoint.y
													+ Assets.laserSegPos
															.getHeight() / 2
													- pixelsLeft - 11, 0,
											Assets.laserSegPos.getHeight()
													- pixelsLeft - 11,
											pixelsLeft + 11, pixelsLeft + 11);
								}
								else if (laserDirection == 2)
								{
									g.drawPixmap(
											Assets.laserTipTL,
											drawPoint.x
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getWidth() / 2,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getHeight() / 2);
									g.drawPixmap(
											Assets.laserSegNeg,
											drawPoint.x
													+ Assets.laserSegNeg
															.getWidth() / 2
													- pixelsLeft - 11,
											drawPoint.y
													+ Assets.laserSegPos
															.getHeight() / 2
													- pixelsLeft - 11,
											Assets.laserSegNeg.getWidth()
													- pixelsLeft - 11,
											Assets.laserSegNeg.getHeight()
													- pixelsLeft - 11,
											pixelsLeft + 11, pixelsLeft + 11);
								}
								else if (laserDirection == 3)
								{
									g.drawPixmap(
											Assets.laserTipBL,
											drawPoint.x
													+ 31
													- pixelsLeft
													- Assets.laserTipBL
															.getWidth() / 2,
											drawPoint.y
													- 31
													+ pixelsLeft
													- Assets.laserTipBL
															.getHeight() / 2);
									g.drawPixmap(
											Assets.laserSegPos,
											drawPoint.x
													+ Assets.laserSegPos
															.getWidth() / 2
													- pixelsLeft - 11,
											drawPoint.y
													- Assets.laserSegPos
															.getHeight() / 2,
											Assets.laserSegPos.getWidth()
													- pixelsLeft - 11, 0,
											pixelsLeft + 11, pixelsLeft + 11);
								}
								else
								{
									g.drawPixmap(
											Assets.laserTipBR,
											drawPoint.x
													- 31
													+ pixelsLeft
													- Assets.laserTipBR
															.getWidth() / 2,
											drawPoint.y
													- 31
													+ pixelsLeft
													- Assets.laserTipBR
															.getHeight() / 2);
									g.drawPixmap(
											Assets.laserSegNeg,
											drawPoint.x
													- Assets.laserSegNeg
															.getWidth() / 2,
											drawPoint.y
													- Assets.laserSegNeg
															.getHeight() / 2,
											0, 0, pixelsLeft + 11,
											pixelsLeft + 11);
								}
							}
							else
							{
								horBounce = true;
							}

							if (pixelsLeft >= 25)
							{
								if (laserDirection < 3)
								{
									g.drawPixmap(
											Assets.laserBounTop,
											drawPoint.x
													- Assets.laserBounTop
															.getWidth() / 2,
											drawPoint.y);
								}
								else
								{
									g.drawPixmap(
											Assets.laserBounBottom,
											drawPoint.x
													- Assets.laserBounBottom
															.getWidth() / 2,
											drawPoint.y
													- Assets.laserBounBottom
															.getHeight());
								}
							}
						}
						// Otherwise, just draw the cross-over as two horizontal
						// bounces
						else
						{
							g.drawPixmap(Assets.laserBounTop, drawPoint.x
									- Assets.laserBounTop.getWidth() / 2,
									drawPoint.y);
							g.drawPixmap(
									Assets.laserBounBottom,
									drawPoint.x
											- Assets.laserBounBottom.getWidth()
											/ 2,
									drawPoint.y
											- Assets.laserBounBottom
													.getHeight());
						}
					}
					// If the position was not visited before
					else
					{
						// Store that this position was visited
						occupied[nextPoint.y][nextPoint.x] = true;

						// If it is animating this current segment
						if (segmentsDone == numOfSegments)
						{
							// Draw the line segment based on the animation
							if (laserDirection == 1)
							{
								g.drawPixmap(Assets.laserTipTR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipTR.getWidth()
												/ 2, drawPoint.y + 31
												- pixelsLeft
												- Assets.laserTipTR.getHeight()
												/ 2);
								g.drawPixmap(
										Assets.laserSegPos,
										drawPoint.x
												- Assets.laserSegPos.getWidth()
												/ 2,
										drawPoint.y
												+ Assets.laserSegPos
														.getHeight() / 2
												- pixelsLeft - 11, 0,
										Assets.laserSegPos.getHeight()
												- pixelsLeft - 11,
										pixelsLeft + 11, pixelsLeft + 11);
							}
							else if (laserDirection == 2)
							{
								g.drawPixmap(Assets.laserTipTL,
										drawPoint.x + 31 - pixelsLeft
												- Assets.laserTipTL.getWidth()
												/ 2, drawPoint.y + 31
												- pixelsLeft
												- Assets.laserTipTL.getHeight()
												/ 2);
								g.drawPixmap(Assets.laserSegNeg, drawPoint.x
										+ Assets.laserSegNeg.getWidth() / 2
										- pixelsLeft - 11, drawPoint.y
										+ Assets.laserSegPos.getHeight() / 2
										- pixelsLeft - 11,
										Assets.laserSegNeg.getWidth()
												- pixelsLeft - 11,
										Assets.laserSegNeg.getHeight()
												- pixelsLeft - 11,
										pixelsLeft + 11, pixelsLeft + 11);
							}
							else if (laserDirection == 3)
							{
								g.drawPixmap(Assets.laserTipBL,
										drawPoint.x + 31 - pixelsLeft
												- Assets.laserTipBL.getWidth()
												/ 2, drawPoint.y - 31
												+ pixelsLeft
												- Assets.laserTipBL.getHeight()
												/ 2);
								g.drawPixmap(Assets.laserSegPos, drawPoint.x
										+ Assets.laserSegPos.getWidth() / 2
										- pixelsLeft - 11, drawPoint.y
										- Assets.laserSegPos.getHeight() / 2,
										Assets.laserSegPos.getWidth()
												- pixelsLeft - 11, 0,
										pixelsLeft + 11, pixelsLeft + 11);
							}
							else
							{
								g.drawPixmap(Assets.laserTipBR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipBR.getWidth()
												/ 2, drawPoint.y - 31
												+ pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2);
								g.drawPixmap(
										Assets.laserSegNeg,
										drawPoint.x
												- Assets.laserSegNeg.getWidth()
												/ 2,
										drawPoint.y
												- Assets.laserSegNeg
														.getHeight() / 2, 0, 0,
										pixelsLeft + 11, pixelsLeft + 11);
							}
						}
						// Otherwise, just draw the line segment
						else
						{
							if (laserDirection % 2 == 1)
							{
								g.drawPixmap(
										Assets.laserSegPos,
										drawPoint.x
												- Assets.laserSegPos.getWidth()
												/ 2,
										drawPoint.y
												- Assets.laserSegPos
														.getHeight() / 2 - 1);
							}
							else
							{
								g.drawPixmap(
										Assets.laserSegNeg,
										drawPoint.x
												- Assets.laserSegNeg.getWidth()
												/ 2,
										drawPoint.y
												- Assets.laserSegNeg
														.getHeight() / 2 - 1);
							}
						}
					}
				}
			}

			// If the loop specified to draw a vertical bounce
			if (verBounce)
			{
				verBounce = false;

				// Draw a vertical bounce and animate it if necessary
				if (laserDirection == 2 || laserDirection == 3)
				{
					if (segmentsDone == numOfSegments)
					{
						if (laserDirection == 2)
						{
							if (pixelsLeft < 31)
							{
								if (31 - pixelsLeft < Assets.laserTipTR
										.getWidth() / 2)
									g.drawPixmap(
											Assets.laserTipTR,
											drawPoint.x
													- 31
													+ pixelsLeft
													- Assets.laserTipTR
															.getWidth() / 2,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTR
															.getHeight() / 2,
											0, 0, Assets.laserTipTR.getWidth()
													/ 2 + 31 - pixelsLeft,
											Assets.laserTipTR.getHeight());
								else
									g.drawPixmap(
											Assets.laserTipTR,
											drawPoint.x
													- 31
													+ pixelsLeft
													- Assets.laserTipTR
															.getWidth() / 2,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTR
															.getHeight() / 2);
							}
							else
							{
								if (pixelsLeft - 31 < Assets.laserTipTL
										.getWidth() / 2)
									g.drawPixmap(
											Assets.laserTipTL,
											drawPoint.x
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getWidth() / 2,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getHeight() / 2,
											0, 0, Assets.laserTipTR.getWidth()
													/ 2 + pixelsLeft - 31,
											Assets.laserTipTR.getHeight());
								else
									g.drawPixmap(
											Assets.laserTipTL,
											drawPoint.x
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getWidth() / 2,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getHeight() / 2);
							}
							g.drawPixmap(
									Assets.laserBounRight,
									drawPoint.x
											- Assets.laserBounRight.getWidth(),
									drawPoint.y
											+ Assets.laserBounRight.getHeight()
											/ 2 - pixelsLeft - 3, 0,
									Assets.laserBounRight.getHeight()
											- pixelsLeft - 3,
									Assets.laserBounRight.getWidth(),
									pixelsLeft + 13);
						}
						else if (pixelsLeft < 31)
						{
							if (31 - pixelsLeft < Assets.laserTipBR.getWidth() / 2)
								g.drawPixmap(Assets.laserTipBR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipBR.getWidth()
												/ 2, drawPoint.y - 31
												+ pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2, 0, 0,
										Assets.laserTipTR.getWidth() / 2 + 31
												- pixelsLeft,
										Assets.laserTipTR.getHeight());
							else
								g.drawPixmap(Assets.laserTipBR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipBR.getWidth()
												/ 2, drawPoint.y - 31
												+ pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2);
							g.drawPixmap(
									Assets.laserBounRight,
									drawPoint.x
											- Assets.laserBounRight.getWidth(),
									drawPoint.y
											- Assets.laserBounRight.getHeight()
											/ 2, 0, 0,
									Assets.laserBounRight.getWidth(),
									pixelsLeft + 3);
						}
						else
						{
							if (pixelsLeft - 31 < Assets.laserTipBL.getWidth() / 2)
								g.drawPixmap(Assets.laserTipBL,
										drawPoint.x + 31 - pixelsLeft
												- Assets.laserTipBL.getWidth()
												/ 2, drawPoint.y - 31
												+ pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2);
							else
								g.drawPixmap(Assets.laserTipBL,
										drawPoint.x + 31 - pixelsLeft
												- Assets.laserTipBL.getWidth()
												/ 2, drawPoint.y - 31
												+ pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2, 0, 0,
										Assets.laserTipTR.getWidth() / 2
												+ pixelsLeft - 31,
										Assets.laserTipTR.getHeight());
							g.drawPixmap(
									Assets.laserBounRight,
									drawPoint.x
											- Assets.laserBounRight.getWidth(),
									drawPoint.y
											- Assets.laserBounRight.getHeight()
											/ 2, 0, 0,
									Assets.laserBounRight.getWidth(),
									pixelsLeft + 3);
						}
					}
					else
					{
						g.drawPixmap(Assets.laserBounRight, drawPoint.x
								- Assets.laserBounRight.getWidth(), drawPoint.y
								- Assets.laserBounRight.getHeight() / 2);
					}
				}
				else
				{
					if (segmentsDone == numOfSegments)
					{
						if (laserDirection == 1)
						{
							if (pixelsLeft < 31)
							{
								if (31 - pixelsLeft < Assets.laserTipTL
										.getWidth() / 2)
									g.drawPixmap(
											Assets.laserTipTL,
											drawPoint.x,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getHeight() / 2,
											Assets.laserTipTL.getWidth() / 2
													+ pixelsLeft - 31, 0,
											Assets.laserTipTL.getWidth() / 2
													+ 31 - pixelsLeft,
											Assets.laserTipTL.getHeight());
								else
									g.drawPixmap(
											Assets.laserTipTL,
											drawPoint.x
													+ 31
													- pixelsLeft
													- Assets.laserTipTR
															.getWidth() / 2,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getHeight() / 2);
							}
							else
							{
								if (pixelsLeft - 31 < Assets.laserTipTL
										.getWidth() / 2)
									g.drawPixmap(
											Assets.laserTipTR,
											drawPoint.x,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getHeight() / 2,
											Assets.laserTipTL.getWidth() / 2
													- pixelsLeft + 31, 0,
											Assets.laserTipTL.getWidth() / 2
													- 31 + pixelsLeft,
											Assets.laserTipTL.getHeight());
								else
									g.drawPixmap(
											Assets.laserTipTR,
											drawPoint.x
													- 31
													+ pixelsLeft
													- Assets.laserTipTL
															.getWidth() / 2,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getHeight() / 2);
							}
							g.drawPixmap(
									Assets.laserBounLeft,
									drawPoint.x,
									drawPoint.y
											+ Assets.laserBounLeft.getHeight()
											/ 2 - pixelsLeft - 3, 0,
									Assets.laserBounLeft.getHeight()
											- pixelsLeft - 3,
									Assets.laserBounLeft.getWidth(),
									pixelsLeft + 13);
						}
						else if (pixelsLeft < 31)
						{
							if (31 - pixelsLeft < Assets.laserTipTL.getWidth() / 2)
								g.drawPixmap(Assets.laserTipBL, drawPoint.x,
										drawPoint.y - 31 + pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2,
										Assets.laserTipTL.getWidth() / 2
												+ pixelsLeft - 31, 0,
										Assets.laserTipTL.getWidth() / 2 + 31
												- pixelsLeft,
										Assets.laserTipTL.getHeight());
							else
								g.drawPixmap(Assets.laserTipBL,
										drawPoint.x + 31 - pixelsLeft
												- Assets.laserTipTR.getWidth()
												/ 2, drawPoint.y - 31
												+ pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2);
							g.drawPixmap(
									Assets.laserBounLeft,
									drawPoint.x,
									drawPoint.y
											- Assets.laserBounLeft.getHeight()
											/ 2, 0, 0,
									Assets.laserBounLeft.getWidth(),
									pixelsLeft + 3);
						}
						else
						{
							if (pixelsLeft - 31 < Assets.laserTipTL.getWidth() / 2)
								g.drawPixmap(Assets.laserTipBR, drawPoint.x,
										drawPoint.y - 31 + pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2,
										Assets.laserTipTL.getWidth() / 2
												- pixelsLeft + 31, 0,
										Assets.laserTipTL.getWidth() / 2 - 31
												+ pixelsLeft,
										Assets.laserTipTL.getHeight());
							else
								g.drawPixmap(Assets.laserTipBR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipTL.getWidth()
												/ 2, drawPoint.y - 31
												+ pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2);
							g.drawPixmap(
									Assets.laserBounLeft,
									drawPoint.x,
									drawPoint.y
											- Assets.laserBounLeft.getHeight()
											/ 2, 0, 0,
									Assets.laserBounLeft.getWidth(),
									pixelsLeft + 3);
						}
					}
					else
					{
						g.drawPixmap(Assets.laserBounLeft, drawPoint.x,
								drawPoint.y - Assets.laserBounLeft.getHeight()
										/ 2);
					}
				}
			}
			// Otherwise, if the loop specified to draw a horizontal bounce
			else if (horBounce)
			{
				horBounce = false;

				// Draw the bounce and animate it if necessary
				if (laserDirection < 3)
				{
					if (segmentsDone == numOfSegments)
					{
						if (laserDirection == 2)
						{
							if (pixelsLeft < 31)
							{
								if (31 - pixelsLeft < Assets.laserTipBL
										.getHeight())
									g.drawPixmap(
											Assets.laserTipBL,
											drawPoint.x
													+ 31
													- pixelsLeft
													- Assets.laserTipBL
															.getWidth() / 2,
											drawPoint.y
													- 31
													+ pixelsLeft
													- Assets.laserTipBL
															.getHeight() / 2,
											0, 0, Assets.laserTipBL.getWidth(),
											Assets.laserTipBL.getHeight() / 2
													+ 31 - pixelsLeft);
								else
									g.drawPixmap(
											Assets.laserTipBL,
											drawPoint.x
													+ 31
													- pixelsLeft
													- Assets.laserTipBL
															.getWidth() / 2,
											drawPoint.y
													- 31
													+ pixelsLeft
													- Assets.laserTipBL
															.getHeight() / 2);
							}
							else
							{
								if (pixelsLeft - 31 < Assets.laserTipBL
										.getHeight())
									g.drawPixmap(
											Assets.laserTipTL,
											drawPoint.x
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getWidth() / 2,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getHeight() / 2,
											0, 0, Assets.laserTipBL.getWidth(),
											Assets.laserTipBL.getHeight() / 2
													- 31 + pixelsLeft);
								else
									g.drawPixmap(
											Assets.laserTipTL,
											drawPoint.x
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getWidth() / 2,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getHeight() / 2);
							}
							g.drawPixmap(Assets.laserBounBottom, drawPoint.x
									+ Assets.laserBounBottom.getWidth() / 2
									- pixelsLeft - 3, drawPoint.y
									- Assets.laserBounBottom.getHeight(),
									Assets.laserBounBottom.getWidth()
											- pixelsLeft - 3, 0,
									pixelsLeft + 3,
									Assets.laserBounBottom.getHeight());
						}
						else if (pixelsLeft < 31)
						{
							if (31 - pixelsLeft < Assets.laserTipBL.getHeight())
								g.drawPixmap(Assets.laserTipBR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipBR.getWidth()
												/ 2, drawPoint.y - 31
												+ pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2, 0, 0,
										Assets.laserTipBL.getWidth(),
										Assets.laserTipBL.getHeight() / 2 + 31
												- pixelsLeft);
							else
								g.drawPixmap(Assets.laserTipBR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipBR.getWidth()
												/ 2, drawPoint.y - 31
												+ pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2);
							g.drawPixmap(
									Assets.laserBounBottom,
									drawPoint.x
											- Assets.laserBounBottom.getWidth()
											/ 2,
									drawPoint.y
											- Assets.laserBounBottom
													.getHeight(), 0, 0,
									pixelsLeft + 3,
									Assets.laserBounBottom.getHeight());
						}
						else
						{
							if (pixelsLeft - 31 < Assets.laserTipBL.getHeight())
								g.drawPixmap(Assets.laserTipTR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipBR.getWidth()
												/ 2, drawPoint.y + 31
												- pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2, 0, 0,
										Assets.laserTipBL.getWidth(),
										Assets.laserTipBL.getHeight() / 2 - 31
												+ pixelsLeft);
							else
								g.drawPixmap(Assets.laserTipTR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipBR.getWidth()
												/ 2, drawPoint.y + 31
												- pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2);
							g.drawPixmap(
									Assets.laserBounBottom,
									drawPoint.x
											- Assets.laserBounBottom.getWidth()
											/ 2,
									drawPoint.y
											- Assets.laserBounBottom
													.getHeight(), 0, 0,
									pixelsLeft + 3,
									Assets.laserBounBottom.getHeight());
						}
					}
					else
					{
						g.drawPixmap(
								Assets.laserBounBottom,
								drawPoint.x - Assets.laserBounBottom.getWidth()
										/ 2,
								drawPoint.y
										- Assets.laserBounBottom.getHeight());
					}
				}
				else
				{
					if (segmentsDone == numOfSegments)
					{
						if (laserDirection == 3)
						{
							if (pixelsLeft < 31)
							{
								if (31 - pixelsLeft < Assets.laserTipTL
										.getHeight())
									g.drawPixmap(Assets.laserTipTL, drawPoint.x
											+ 31 - pixelsLeft
											- Assets.laserTipTL.getWidth() / 2,
											drawPoint.y, 0,
											Assets.laserTipTL.getHeight() / 2
													+ pixelsLeft - 31,
											Assets.laserTipTL.getHeight(),
											Assets.laserTipTL.getWidth() / 2
													+ 31 - pixelsLeft);
								else
									g.drawPixmap(
											Assets.laserTipTL,
											drawPoint.x
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getWidth() / 2,
											drawPoint.y
													+ 31
													- pixelsLeft
													- Assets.laserTipTL
															.getHeight() / 2);
							}
							else
							{
								if (pixelsLeft - 31 < Assets.laserTipTL
										.getHeight())
									g.drawPixmap(Assets.laserTipBL, drawPoint.x
											+ 31 - pixelsLeft
											- Assets.laserTipBL.getWidth() / 2,
											drawPoint.y, 0,
											Assets.laserTipTL.getHeight() / 2
													- pixelsLeft + 31,
											Assets.laserTipTL.getHeight(),
											Assets.laserTipTL.getWidth() / 2
													- 31 + pixelsLeft);
								else
									g.drawPixmap(
											Assets.laserTipBL,
											drawPoint.x
													+ 31
													- pixelsLeft
													- Assets.laserTipBL
															.getWidth() / 2,
											drawPoint.y
													- 31
													+ pixelsLeft
													- Assets.laserTipBL
															.getHeight() / 2);
							}
							g.drawPixmap(Assets.laserBounTop, drawPoint.x
									+ Assets.laserBounTop.getWidth() / 2
									- pixelsLeft - 3, drawPoint.y,
									Assets.laserBounBottom.getWidth()
											- pixelsLeft - 3, 0,
									pixelsLeft + 3,
									Assets.laserBounBottom.getHeight());
						}
						else if (pixelsLeft < 31)
						{
							if (31 - pixelsLeft < Assets.laserTipTL.getHeight())
								g.drawPixmap(Assets.laserTipTR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipTR.getWidth()
												/ 2, drawPoint.y, 0,
										Assets.laserTipTL.getHeight() / 2
												+ pixelsLeft - 31,
										Assets.laserTipTL.getHeight(),
										Assets.laserTipTL.getWidth() / 2 + 31
												- pixelsLeft);
							else
								g.drawPixmap(Assets.laserTipTR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipTR.getWidth()
												/ 2, drawPoint.y + 31
												- pixelsLeft
												- Assets.laserTipTR.getHeight()
												/ 2);
							g.drawPixmap(Assets.laserBounTop, drawPoint.x
									- Assets.laserBounTop.getWidth() / 2,
									drawPoint.y, 0, 0, pixelsLeft + 3,
									Assets.laserBounTop.getHeight());
						}
						else
						{
							if (pixelsLeft - 31 < Assets.laserTipTL.getHeight())
								g.drawPixmap(Assets.laserTipBR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipBR.getWidth()
												/ 2, drawPoint.y, 0,
										Assets.laserTipTL.getHeight() / 2
												- pixelsLeft + 31,
										Assets.laserTipTL.getHeight(),
										Assets.laserTipTL.getWidth() / 2 - 31
												+ pixelsLeft);
							else
								g.drawPixmap(Assets.laserTipBR,
										drawPoint.x - 31 + pixelsLeft
												- Assets.laserTipBR.getWidth()
												/ 2, drawPoint.y - 31
												+ pixelsLeft
												- Assets.laserTipBR.getHeight()
												/ 2);
							g.drawPixmap(Assets.laserBounTop, drawPoint.x
									- Assets.laserBounTop.getWidth() / 2,
									drawPoint.y, 0, 0, pixelsLeft + 3,
									Assets.laserBounTop.getHeight());
						}
					}
					else
					{
						g.drawPixmap(Assets.laserBounTop, drawPoint.x
								- Assets.laserBounTop.getWidth() / 2,
								drawPoint.y);
					}
				}
			}

			// If the next point leads to hitting the target, update the time of
			// winning, increment the score and update the game state
			if (nextPoint.x == 3)
			{
				if (nextPoint.y == 5 && laserDirection == 4)
				{
					if (state != GameState.WinningAnimation)
					{
						winGame();
					}
					state = GameState.WinningAnimation;
					return;
				}
				else if (nextPoint.y == 6 && laserDirection == 1)
				{
					if (state != GameState.WinningAnimation)
					{
						winGame();
					}
					state = GameState.WinningAnimation;
					return;
				}
			}
			else if (nextPoint.x == 4)
			{
				if (nextPoint.y == 5 && laserDirection == 3)
				{
					if (state != GameState.WinningAnimation)
					{
						winGame();
					}
					state = GameState.WinningAnimation;
					return;
				}
				else if (nextPoint.y == 6 && laserDirection == 2)
				{
					if (state != GameState.WinningAnimation)
					{
						winGame();
					}
					state = GameState.WinningAnimation;
					return;
				}
			}

			// If the next point hits a gun, stop drawing the laser and update
			// the game state
			if (nextPoint.x == 3)
			{
				if (nextPoint.y == 0 && laserDirection == 4)
				{
					if (state == GameState.AnimatingLaser)
					{
						laserDrawEnd = timeSinceStart;
					}
					state = GameState.Running;
					return;
				}
				else if (nextPoint.y == 1 && laserDirection == 1)
				{
					if (state == GameState.AnimatingLaser)
					{
						laserDrawEnd = timeSinceStart;
					}
					state = GameState.Running;
					return;
				}
			}
			else if (nextPoint.x == 4)
			{
				if (nextPoint.y == 0 && laserDirection == 3)
				{
					if (state == GameState.AnimatingLaser)
					{
						laserDrawEnd = timeSinceStart;
					}
					state = GameState.Running;
					return;
				}
				else if (nextPoint.y == 1 && laserDirection == 2)
				{
					if (state == GameState.AnimatingLaser)
					{
						laserDrawEnd = timeSinceStart;
					}
					state = GameState.Running;
					return;
				}
			}

			if (nextPoint.x == 3)
			{
				if (nextPoint.y == 10 && laserDirection == 4)
				{
					if (state == GameState.AnimatingLaser)
					{
						laserDrawEnd = timeSinceStart;
					}
					state = GameState.Running;
					return;
				}
				else if (nextPoint.y == 11 && laserDirection == 1)
				{
					if (state == GameState.AnimatingLaser)
					{
						laserDrawEnd = timeSinceStart;
					}
					state = GameState.Running;
					return;
				}
			}
			else if (nextPoint.x == 4)
			{
				if (nextPoint.y == 10 && laserDirection == 3)
				{
					if (state == GameState.AnimatingLaser)
					{
						laserDrawEnd = timeSinceStart;
					}
					state = GameState.Running;
					return;
				}
				else if (nextPoint.y == 11 && laserDirection == 2)
				{
					if (state == GameState.AnimatingLaser)
					{
						laserDrawEnd = timeSinceStart;
					}
					state = GameState.Running;
					return;
				}
			}

			// Move the laser by updating the next point in the direction it is
			// shooting
			if (laserDirection == 1)
			{
				nextPoint.x++;
				nextPoint.y--;
			}
			else if (laserDirection == 2)
			{
				nextPoint.x--;
				nextPoint.y--;
			}
			else if (laserDirection == 3)
			{
				nextPoint.x--;
				nextPoint.y++;
			}
			else
			{
				nextPoint.x++;
				nextPoint.y++;
			}

			// Increment the number of segments drawn
			segmentsDone++;
		}

		// Update the game state
		state = GameState.AnimatingLaser;
	}

	/**
	 * Undoes the last move in the moves stack or does nothing if it is empty
	 */
	private void undoMove()
	{
		if (gameMoves.isEmpty())
			return;

		// Retrieve the last move and its starting and ending points
		Move lastMove = gameMoves.removeLast();
		Point startPoint = lastMove.getStartPoint();
		Point endPoint = lastMove.getEndPoint();

		// If the move was not shooting
		if (startPoint != null && endPoint != null)
		{
			// Turn or move the mirror backwards
			if (startPoint.equals(endPoint))
			{
				Mirror movedMirror = grid[endPoint.x][endPoint.y];
				movedMirror.rotate();
			}
			else
			{
				Mirror movedMirror = grid[endPoint.x][endPoint.y];
				grid[startPoint.x][startPoint.y] = grid[endPoint.x][endPoint.y];
				grid[endPoint.x][endPoint.y] = null;
				movedMirror.move(new Point(startPoint.y, startPoint.x));
			}
		}

		// Update the last move start and end point from the move before the
		// undone move
		if (gameMoves.isEmpty())
		{
			lastMoveStart = null;
			lastMoveEnd = null;
		}
		else
		{
			Move lastLastMove = gameMoves.getLast();
			lastMoveStart = lastLastMove.getStartPoint();
			lastMoveEnd = lastLastMove.getEndPoint();
		}

		// Reset the turn time
		turnStart = timeSinceStart;

		// Change turns
		playerOneTurn = !playerOneTurn;

		// Update the AI's grid and tell it to start calculating the next move
		// if it is its turn
		if (match.onePlayer)
			computerPlayer.userMadeMove(lastMove.reverse(), true);
		if (match.onePlayer && !playerOneTurn)
		{
			computerPlayer.startCalculatingMove(lastMoveStart, lastMoveEnd);
		}
	}
	
	private void winGame() {
		winStartTime = timeSinceStart;
		laserDrawEnd = timeSinceStart;
		showWinner = true;
		if (playerOneTurn)
			match.playerOneScore++;
		else
			match.playerTwoScore++;
		if (match.isOnline && playerOneTurn) {
			new MakeMoveTask(null, null, playerOne.getDirection() == 2, match.onlineUserId).execute();
		}
	}

	/**
	 * Changes turns and update the move stack and game state
	 */
	private void changeTurns()
	{
		// Add the last move to the move stack
		gameMoves.addLast(new Move(lastMoveStart, lastMoveEnd));

		// Update the game state and reset the turn timer
		state = GameState.Running;
		playerOneTurn = !playerOneTurn;
		turnStart = timeSinceStart;
		shootLaser = false;
		clearMirrors();

		// If it is the AI's turn, tell it to start calculating
		if (match.onePlayer && !playerOneTurn)
		{
			computerPlayer.userMadeMove(new Move(lastMoveStart, lastMoveEnd),
					false);
			computerPlayer.startCalculatingMove(lastMoveStart, lastMoveEnd);
		}
		else if (match.isOnline && !playerOneTurn)
		{
			new MakeMoveTask(lastMoveStart, lastMoveEnd, playerOne.getDirection() == 2, match.onlineUserId).execute();
		}
	}

	/**
	 * Clears all the mirrors on the screen by deselecting them and clearing
	 * their stored clicks
	 */
	private void clearMirrors()
	{
		for (Mirror mirror : mirrors)
		{
			mirror.clearClicks();
			mirror.deSelect();
		}
	}

	/**
	 * Returns the currently selected mirror
	 * 
	 * @return the currently selected mirror
	 */
	private Mirror currentSelectedMirror()
	{
		for (Mirror gameMirror : mirrors)
			if (gameMirror.isSelected())
				return gameMirror;

		return null;
	}

	/**
	 * Returns the given x and y coordinates as grid coordinates
	 * 
	 * @param x
	 *            The x position of the point
	 * @param y
	 *            The y position of the point
	 * @return the point in (column, row) form in grid coordinates
	 */
	private Point getGridCoordinates(int x, int y)
	{
		int col = Math.min(7, Math.max(0, (x + 5) / 62));
		int row = Math.min(11, Math.max(0, (y - 67) / 62));

		return new Point(col, row);
	}

	/**
	 * Returns the grid coordinates as an x and y coordinate on the screen
	 * (returns the centre of that grid spot)
	 * 
	 * @param row
	 *            The row the point is in
	 * @param col
	 *            The col the point is in
	 * @return a point in (x, y) form in pixel coordinates on the screen
	 */
	private Point getDrawCoordinates(int row, int col)
	{
		int x = 25 + col * 62;
		int y = 97 + row * 62;

		return new Point(x, y);
	}

	/**
	 * Recycles all the animation images that were used in the beginning of the
	 * game
	 */
	private void disposeAnimationImages()
	{
		for (int index = 0; index < 5; index++)
		{
			game.disposeImage(Assets.horAnimation[index]);
			game.disposeImage(Assets.verAnimation[index]);
			game.disposeImage(Assets.tileAnimation[index]);
		}
		Assets.horAnimation = null;
		Assets.verAnimation = null;
		Assets.tileAnimation = null;
	}

	/**
	 * Loads all the images used to play the game
	 * 
	 * @param g
	 *            The graphics from which to load the images
	 */
	private void loadPlayImages(Graphics g)
	{
		Assets.laserBounBottom = g.newPixmap("LaserBounceBottom.png",
				PixmapFormat.ARGB4444);
		Assets.laserBounLeft = g.newPixmap("LaserBounceLeft.png",
				PixmapFormat.ARGB4444);
		Assets.laserBounRight = g.newPixmap("LaserBounceRight.png",
				PixmapFormat.ARGB4444);
		Assets.laserBounTop = g.newPixmap("LaserBounceTop.png",
				PixmapFormat.ARGB4444);

		Assets.laserSegNeg = g.newPixmap("LaserDiagonalNegative.png",
				PixmapFormat.ARGB4444);
		Assets.laserSegPos = g.newPixmap("LaserDiagonalPositive.png",
				PixmapFormat.ARGB4444);

		Assets.laserTipBL = g
				.newPixmap("LaserTipBL.png", PixmapFormat.ARGB4444);
		Assets.laserTipBR = g
				.newPixmap("LaserTipBR.png", PixmapFormat.ARGB4444);
		Assets.laserTipTL = g
				.newPixmap("LaserTipTL.png", PixmapFormat.ARGB4444);
		Assets.laserTipTR = g
				.newPixmap("LaserTipTR.png", PixmapFormat.ARGB4444);

		Assets.mirrorSelect = g.newPixmap("MirrorSelect.png",
				PixmapFormat.ARGB4444);

		Assets.tapToStart = g
				.newPixmap("TapToStart.png", PixmapFormat.ARGB4444);
		Assets.player1Wins = g.newPixmap("Player1Wins.png",
				PixmapFormat.ARGB4444);
		Assets.player2Wins = g.newPixmap("Player2Wins.png",
				PixmapFormat.ARGB4444);
		Assets.computerWins = g.newPixmap("ComputerWins.png",
				PixmapFormat.ARGB4444);
		Assets.lastMove = g.newPixmap("LastMove.png", PixmapFormat.ARGB4444);
		Assets.timeRanOut = g.newPixmap("OutOfTime.png", PixmapFormat.ARGB4444);

		Assets.winningAnimation = new Pixmap[5];
		for (int animation = 0; animation < 5; animation++)
		{
			Assets.winningAnimation[animation] = g.newPixmap("WinningAnimation"
					+ (animation + 1) + ".png", PixmapFormat.ARGB4444);
		}

		Assets.gameMenuBackground = g.newPixmap("GameMenuBackground.png",
				PixmapFormat.ARGB4444);
		Assets.winCircle = g.newPixmap("WinCircle.png", PixmapFormat.ARGB4444);
		Assets.playCircle = g
				.newPixmap("PlayCircle.png", PixmapFormat.ARGB4444);

		Assets.mainButtonNor = g.newPixmap("QuitButtonNormal.png",
				PixmapFormat.ARGB4444);
		Assets.mainButtonClck = g.newPixmap("QuitButtonClicked.png",
				PixmapFormat.ARGB4444);
		Assets.resumeButtonNor = g.newPixmap("ResumeButtonNormal.png",
				PixmapFormat.ARGB4444);
		Assets.resumeButtonClck = g.newPixmap("ResumeButtonClicked.png",
				PixmapFormat.ARGB4444);
		Assets.closeButtonNor = g.newPixmap("CloseButtonNormal.png",
				PixmapFormat.ARGB4444);
		Assets.closeButtonClck = g.newPixmap("CloseButtonClicked.png",
				PixmapFormat.ARGB4444);
		Assets.restartButtonNor = g.newPixmap("RestartButtonNormal.png",
				PixmapFormat.ARGB4444);
		Assets.restartButtonClck = g.newPixmap("RestartButtonClicked.png",
				PixmapFormat.ARGB4444);
		Assets.menuNextButtonNor = g.newPixmap("MenuNextButtonNormal.png",
				PixmapFormat.ARGB4444);
		Assets.menuNextButtonClck = g.newPixmap("MenuNextButtonClicked.png",
				PixmapFormat.ARGB4444);
		Assets.menuNewButtonNor = g.newPixmap("MenuNewButtonNormal.png",
				PixmapFormat.ARGB4444);
		Assets.menuNewButtonClck = g.newPixmap("MenuNewButtonClicked.png",
				PixmapFormat.ARGB4444);

		Assets.player1Name = g.newPixmap("Player1Name.png",
				PixmapFormat.ARGB4444);
		Assets.player2Name = g.newPixmap("Player2Name.png",
				PixmapFormat.ARGB4444);
		Assets.computerName = g.newPixmap("ComputerName.png",
				PixmapFormat.ARGB4444);

		// Create all the buttons in the game
		resumeButton = new Button(170, 350, Assets.resumeButtonNor,
				Assets.resumeButtonClck);
		closeButton = new Button(170, 350, Assets.closeButtonNor,
				Assets.closeButtonClck);
		restartButton = new Button(170, 415, Assets.restartButtonNor,
				Assets.restartButtonClck);
		nextButton = new Button(170, 415, Assets.menuNextButtonNor,
				Assets.menuNextButtonClck);
		newButton = new Button(170, 415, Assets.menuNewButtonNor,
				Assets.menuNewButtonClck);
		mainButton = new Button(170, 480, Assets.mainButtonNor,
				Assets.mainButtonClck);
	}

	/**
	 * Draws the screen during the animation state of the game
	 * 
	 * @param deltaTime
	 *            The time since the last updating of the screen
	 */
	private void presentAnimation(float deltaTime)
	{
		Graphics g = game.getGraphics();

		g.drawPixmap(Assets.gameBackground, 0, 0);

		// Draw the shaded tiles
		if (timeSinceStart >= 1.05)
		{
			for (int row = 0; row < 12; row++)
			{
				for (int col = row % 2; col < 8; col += 2)
				{
					Point drawPos = getDrawCoordinates(row, col);
					g.drawPixmap(Assets.shadedTile, drawPos.x
							- Assets.shadedTile.getWidth() / 2, drawPos.y
							- Assets.shadedTile.getHeight() / 2);

				}
			}
		}

		// Draw the animation that creates the shaded tiles
		if (timeSinceStart >= 0.7 && timeSinceStart < 1.2)
		{
			for (int row = 0; row < 12; row++)
			{
				for (int col = row % 2; col < 8; col += 2)
				{
					Point drawPos = getDrawCoordinates(row, col);
					if (timeSinceStart - 0.7 < 0.25)
						g.drawPixmap(
								Assets.tileAnimation[(int) ((timeSinceStart - 0.7) * 20)],
								drawPos.x - Assets.shadedTile.getWidth() / 2,
								drawPos.y - Assets.shadedTile.getHeight() / 2);
					else
						g.drawPixmap(
								Assets.tileAnimation[9 - (int) ((timeSinceStart - 0.7) * 20)],
								drawPos.x - Assets.shadedTile.getWidth() / 2,
								drawPos.y - Assets.shadedTile.getHeight() / 2);
				}
			}
		}

		// Draw the horizontal lines on the grid
		if (timeSinceStart >= 0.25)
		{
			for (int row = 0; row < 11; row++)
			{
				g.drawPixmap(Assets.horLine,
						240 - Assets.horLine.getWidth() / 2, 97 + 62 * row);
			}
		}

		// Draw the vertical lines on the grid
		if (timeSinceStart >= 0.65)
		{
			for (int col = 0; col < 7; col++)
			{
				g.drawPixmap(Assets.verLine, 25 + 62 * col, 72);
			}
		}

		// Draw the animation that draws the horizontal lines
		if (timeSinceStart < 0.5)
		{
			for (int row = 0; row < 11; row++)
			{
				if (timeSinceStart < 0.25)
					g.drawPixmap(
							Assets.horAnimation[(int) (timeSinceStart * 20)],
							240 - Assets.horLine.getWidth() / 2, 97 + 62 * row);
				else
					g.drawPixmap(
							Assets.horAnimation[9 - (int) (timeSinceStart * 20)],
							240 - Assets.horLine.getWidth() / 2, 97 + 62 * row);
			}
		}

		// Draw the animation that draws the vertical lines
		if (timeSinceStart >= 0.4 && timeSinceStart < 0.9)
		{
			for (int col = 0; col < 7; col++)
			{
				if (timeSinceStart - 0.4 < 0.25)
					g.drawPixmap(
							Assets.verAnimation[(int) ((timeSinceStart - 0.4) * 20)],
							25 + 62 * col, 72);
				else
					g.drawPixmap(
							Assets.verAnimation[9 - (int) ((timeSinceStart - 0.4) * 20)],
							25 + 62 * col, 72);
			}
		}

		// Draw all the mirrors
		for (Mirror mirror : mirrors)
		{
			mirror.draw(g);
		}

		// Draw the gun, border, and title
		playerOne.drawHighlight(g, true);

		playerTwo.draw(g, false);
		playerOne.draw(g, true);

		g.drawPixmap(Assets.mirrorBorder, 0,
				800 - Assets.mirrorBorder.getHeight());
		g.drawPixmap(Assets.gameTitleBar, 0, 0);

		// Draw the buttons, target, and timer bar
		menuButton.draw(g);
		undoButton.draw(g);

		g.drawPixmap(Assets.target, 243 - Assets.target.getWidth() / 2,
				438 - Assets.target.getHeight() / 2);

		if (match.timerOn)
			g.drawPixmap(Assets.timerBar, 22, 778);

		// Change game states if the animation is finished
		if (timeSinceStart >= 1.3)
		{
			loadPlayImages(g);
			turnStart = timeSinceStart;
			state = GameState.TapToStart;
			if (match.isOnline) {
				game.showProgressDialog("Waiting for other player...", false);
			}
		}
	}

	/**
	 * Draws the game board
	 */
	private void presentGameScreen()
	{
		Graphics g = game.getGraphics();

		// Draw the background and shaded tiles
		g.drawPixmap(Assets.gameBackground, 0, 0);

		for (int row = 0; row < 12; row++)
		{
			for (int col = row % 2; col < 8; col += 2)
			{
				Point drawPos = getDrawCoordinates(row, col);
				g.drawPixmap(Assets.shadedTile,
						drawPos.x - Assets.shadedTile.getWidth() / 2, drawPos.y
								- Assets.shadedTile.getHeight() / 2);

			}
		}

		// Draw the grid lines
		for (int row = 0; row < 11; row++)
		{
			g.drawPixmap(Assets.horLine, 240 - Assets.horLine.getWidth() / 2,
					97 + 62 * row);
		}
		for (int col = 0; col < 7; col++)
		{
			g.drawPixmap(Assets.verLine, 25 + 62 * col, 72);
		}

		// Draw the player highlight and shoot the laser if necessary
		playerTwo.drawHighlight(g, !playerOneTurn);
		playerOne.drawHighlight(g, playerOneTurn);
		if (shootLaser)
		{
			drawLaser(g);
		}

		Point drawPoint;

		// Draw a slightly shaded tile to indicate what the last move was
		if (lastMoveStart != null)
		{
			drawPoint = getDrawCoordinates(lastMoveStart.x, lastMoveStart.y);
			g.drawPixmap(Assets.lastMove,
					drawPoint.x - Assets.lastMove.getWidth() / 2, drawPoint.y
							- Assets.lastMove.getHeight() / 2);
		}
		if (lastMoveEnd != null && !lastMoveStart.equals(lastMoveEnd))
		{
			drawPoint = getDrawCoordinates(lastMoveEnd.x, lastMoveEnd.y);
			g.drawPixmap(Assets.lastMove,
					drawPoint.x - Assets.lastMove.getWidth() / 2, drawPoint.y
							- Assets.lastMove.getHeight() / 2);
		}

		// For each mirror
		for (Mirror nextMirror : mirrors)
		{
			// Draw the mirror and the selection if it is selected
			nextMirror.draw(g);

			// If the mirror is selected
			if (nextMirror.isSelected())
			{
				// For each point around the mirror, check if the mirror can
				// move there and draw a highlight if it can
				Point checkPoint = new Point(nextMirror.row, nextMirror.col - 1);
				if (nextMirror.canMove(new Point(checkPoint.y, checkPoint.x))
						&& grid[checkPoint.x][checkPoint.y] == null
						&& (lastMoveEnd == null
								|| !lastMoveEnd.equals(new Point(
										nextMirror.row, nextMirror.col)) || !lastMoveStart
									.equals(checkPoint)))
				{
					drawPoint = getDrawCoordinates(checkPoint.x, checkPoint.y);
					g.drawPixmap(Assets.gridHighlight, drawPoint.x
							- Assets.gridHighlight.getWidth() / 2, drawPoint.y
							- Assets.gridHighlight.getHeight() / 2);
				}
				checkPoint = new Point(nextMirror.row - 1, nextMirror.col);
				if (nextMirror.canMove(new Point(checkPoint.y, checkPoint.x))
						&& grid[checkPoint.x][checkPoint.y] == null
						&& (lastMoveEnd == null
								|| !lastMoveEnd.equals(new Point(
										nextMirror.row, nextMirror.col)) || !lastMoveStart
									.equals(checkPoint)))
				{
					drawPoint = getDrawCoordinates(checkPoint.x, checkPoint.y);
					g.drawPixmap(Assets.gridHighlight, drawPoint.x
							- Assets.gridHighlight.getWidth() / 2, drawPoint.y
							- Assets.gridHighlight.getHeight() / 2);
				}
				checkPoint = new Point(nextMirror.row, nextMirror.col + 1);
				if (nextMirror.canMove(new Point(checkPoint.y, checkPoint.x))
						&& grid[checkPoint.x][checkPoint.y] == null
						&& (lastMoveEnd == null
								|| !lastMoveEnd.equals(new Point(
										nextMirror.row, nextMirror.col)) || !lastMoveStart
									.equals(checkPoint)))
				{
					drawPoint = getDrawCoordinates(checkPoint.x, checkPoint.y);
					g.drawPixmap(Assets.gridHighlight, drawPoint.x
							- Assets.gridHighlight.getWidth() / 2, drawPoint.y
							- Assets.gridHighlight.getHeight() / 2);
				}
				checkPoint = new Point(nextMirror.row + 1, nextMirror.col);
				if (nextMirror.canMove(new Point(checkPoint.y, checkPoint.x))
						&& grid[checkPoint.x][checkPoint.y] == null
						&& (lastMoveEnd == null
								|| !lastMoveEnd.equals(new Point(
										nextMirror.row, nextMirror.col)) || !lastMoveStart
									.equals(checkPoint)))
				{
					drawPoint = getDrawCoordinates(checkPoint.x, checkPoint.y);
					g.drawPixmap(Assets.gridHighlight, drawPoint.x
							- Assets.gridHighlight.getWidth() / 2, drawPoint.y
							- Assets.gridHighlight.getHeight() / 2);
				}
			}
		}

		// Draw the target and the guns
		g.drawPixmap(Assets.target, 243 - Assets.target.getWidth() / 2,
				438 - Assets.target.getHeight() / 2);
		playerTwo.draw(g, !playerOneTurn);
		playerOne.draw(g, playerOneTurn);

		// Draw the border and title bar
		g.drawPixmap(Assets.mirrorBorder, 0,
				800 - Assets.mirrorBorder.getHeight());
		g.drawPixmap(Assets.gameTitleBar, 0, 0);

		// Draw the buttons
		menuButton.draw(g);
		undoButton.draw(g);

		// Draw the timer bar based on whose turn it is
		if (match.timerOn)
		{
			if (!playerOneTurn)
			{
				g.drawPixmap(Assets.timerBar, 22, 75, 448 - (int) Math.max(18,
						(turnLength - (timeSinceStart - turnStart)) * 427
								/ turnLength + 18), 0, (int) Math.max(18,
						(turnLength - (timeSinceStart - turnStart)) * 427
								/ turnLength + 18), Assets.timerBar.getHeight());
			}
			else
			{
				g.drawPixmap(Assets.timerBar, 22, 778, 448 - (int) Math.max(18,
						(turnLength - (timeSinceStart - turnStart)) * 427
								/ turnLength + 18), 0, (int) Math.max(18,
						(turnLength - (timeSinceStart - turnStart)) * 427
								/ turnLength + 18), Assets.timerBar.getHeight());
			}
		}
	}

	/**
	 * Draws the extra graphics when the player wins the game
	 */
	private void presentWinningScreen()
	{
		Graphics g = game.getGraphics();

		// Draw the growing highlight based on the animation speed
		g.drawPixmap(Assets.winningAnimation[(int) (Math.min(0.999f,
				(timeSinceStart - winStartTime) / WINNING_SPEED) * 5)],
				243 - Assets.winningAnimation[(int) (Math.min(0.999f,
						(timeSinceStart - winStartTime) / WINNING_SPEED) * 5)]
						.getWidth() / 2,
				438 - Assets.winningAnimation[(int) (Math.min(0.999f,
						(timeSinceStart - winStartTime) / WINNING_SPEED) * 5)]
						.getHeight() / 2);

		// Draws the target
		g.drawPixmap(Assets.target, 243 - Assets.target.getWidth() / 2,
				438 - Assets.target.getHeight() / 2);

		// Draw the winner pop-up if necessary and baed on whose turn it is
		if (playerOneTurn && showWinner)
		{
			g.drawPixmap(Assets.player1Wins,
					240 - Assets.player1Wins.getWidth() / 2, 285);
		}
		else if (showWinner)
		{
			if (match.onePlayer)
				g.drawPixmap(Assets.computerWins,
						240 - Assets.computerWins.getWidth() / 2, 285);
			else
				g.drawPixmap(Assets.player2Wins,
						240 - Assets.player2Wins.getWidth() / 2, 285);
		}
	}

	/**
	 * Draws the game menu screen
	 */
	private void presentMenu()
	{
		Graphics g = game.getGraphics();

		// Draw the background and player names at the top
		g.drawPixmap(Assets.gameMenuBackground,
				240 - Assets.gameMenuBackground.getWidth() / 2,
				400 - Assets.gameMenuBackground.getHeight() / 2);
		g.drawPixmap(Assets.player1Name,
				162 - Assets.player1Name.getWidth() / 2,
				278 - Assets.player1Name.getHeight() / 2);
		if (match.onePlayer)
			g.drawPixmap(Assets.computerName,
					314 - Assets.computerName.getWidth() / 2,
					278 - Assets.computerName.getHeight() / 2);
		else
			g.drawPixmap(Assets.player2Name,
					319 - Assets.player2Name.getWidth() / 2,
					278 - Assets.player2Name.getHeight() / 2);

		// Draws the black circles based on how many games there are
		for (int circle = 0; circle < match.numGames / 2 + 1; circle++)
		{
			g.drawPixmap(Assets.playCircle, 137 + circle * 28
					- Assets.playCircle.getWidth() / 2,
					310 - Assets.playCircle.getHeight() / 2);
		}
		for (int circle = 0; circle < match.numGames / 2 + 1; circle++)
		{
			g.drawPixmap(Assets.playCircle, 290 + (2 - circle) * 28
					- Assets.playCircle.getWidth() / 2,
					310 - Assets.playCircle.getHeight() / 2);
		}

		// Draws the filled in circles for whichever games the players won on
		// top of the other circles
		for (int circle = 0; circle < match.playerOneScore; circle++)
		{
			g.drawPixmap(Assets.winCircle, 137 + circle * 28
					- Assets.playCircle.getWidth() / 2,
					310 - Assets.playCircle.getHeight() / 2);
		}
		for (int circle = 0; circle < match.playerTwoScore; circle++)
		{
			g.drawPixmap(Assets.winCircle, 290 + (2 - circle) * 28
					- Assets.playCircle.getWidth() / 2,
					310 - Assets.playCircle.getHeight() / 2);
		}

		// Draw the buttons based on the game state (normal: resume, restart,
		// main) (winning: close, next/new, main)
		if (state == GameState.WinningAnimation)
		{
			closeButton.draw(g);
			if (match.playerOneScore == match.numGames / 2 + 1
					|| match.playerTwoScore == match.numGames / 2 + 1)
				newButton.draw(g);
			else
				nextButton.draw(g);
		}
		else
		{
			resumeButton.draw(g);
			restartButton.draw(g);
		}
		mainButton.draw(g);
	}

	/**
	 * Draws the game screen based on how much time passed since the last frame
	 * was drawn
	 * 
	 * @param deltaTime
	 *            The amount of time since the last frame was drawn
	 */
	public void present(float deltaTime)
	{
		// Draw the game screen based on what the game state is
		if (state == GameState.Animate)
		{
			presentAnimation(deltaTime);
		}
		else if (state == GameState.TapToStart)
		{
			presentGameScreen();
			Graphics g = game.getGraphics();
			if (!match.isOnline) {
				g.drawPixmap(Assets.tapToStart,
						240 - Assets.tapToStart.getWidth() / 2, 285);
			}
		}
		else if (state == GameState.TimeRanOut)
		{
			presentGameScreen();
			Graphics g = game.getGraphics();
			g.drawPixmap(Assets.timeRanOut,
					240 - Assets.timeRanOut.getWidth() / 2, 285);
		}
		else if (state == GameState.Running
				|| state == GameState.AnimatingLaser)
		{
			presentGameScreen();
		}
		else if (state == GameState.WinningAnimation)
		{
			presentGameScreen();
			presentWinningScreen();
		}

		// Draw the menu if it is open
		if (showMenu)
		{
			presentMenu();
		}
	}
	
	public synchronized void onlineMoveMade(Point start, Point end, boolean turnRight) {
		Move move = new Move(start, end);
		if (turnRight) {
			move.needToTurnRight();
		}
		makeMove(move);
	}
	
	public Match getMatch() {
		return match;
	}
	
	public synchronized void registeredUser(int userId) {
		if (match != null) {
			match.onlineUserId = userId;
		}
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
		game.disposeImage(Assets.laserBounBottom);
		game.disposeImage(Assets.laserBounLeft);
		game.disposeImage(Assets.laserBounRight);
		game.disposeImage(Assets.laserBounTop);

		game.disposeImage(Assets.laserSegNeg);
		game.disposeImage(Assets.laserSegPos);

		game.disposeImage(Assets.laserTipBL);
		game.disposeImage(Assets.laserTipBR);
		game.disposeImage(Assets.laserTipTL);
		game.disposeImage(Assets.laserTipTR);

		game.disposeImage(Assets.mirrorSelect);

		game.disposeImage(Assets.tapToStart);
		game.disposeImage(Assets.player1Wins);
		game.disposeImage(Assets.player2Wins);
		game.disposeImage(Assets.computerWins);
		game.disposeImage(Assets.lastMove);

		for (int animation = 1; animation <= 5; animation++)
		{
			game.disposeImage(Assets.winningAnimation[animation - 1]);
		}
		Assets.winningAnimation = null;
	}
	
	public void startOnlineMatch() {
		state = GameState.Running;
		match.matchStarted = true;
	}
}
