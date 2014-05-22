package com.pianist.battlelasers.game_objects;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Point;

/**
 * The AI Class is used to simulate a player with different skill levels. When
 * asked to calculate a move, it creates a new thread that is used to calculate
 * that move and reports to the main thread when it is finished. It also doesn't
 * ever repeat the same grid configuration to make the game more fun.
 * 
 * @author Peter Gokhshteyn and Alex Szoke
 */
public class AI implements Runnable
{
	// Store the original grid and list of mirrors
	private AIMirror[][] originalGrid;

	private ArrayList<AIMirror> originalList;

	// A variable that tells the main thread when the AI is done calculating a
	// move
	private boolean isFinishedCalculating;

	// The move that the AI selected
	private Move selectedMove;

	// The thread in which the move is calculated
	private Thread calculationThread;

	// The start and end points of the last move
	// Are volatile because they are read and written from both threads
	private volatile Point illegalStart;

	private volatile Point illegalEnd;

	// Whether the AI should pretend to think for a long time or not
	private boolean thinkLong;

	// The lists of mirrors that describe previously used grids
	private ArrayList<ArrayList<AIMirror>> archivedGrids;

	// Whether the AI is difficult or easy
	private AIDifficulty difficulty;

	// Whether the AI could win after the last move
	private boolean computerCouldWin;

	public enum AIDifficulty {
		Easy, Medium, Hard, Impossible
	}

	/**
	 * Creates a new AI with the given grid, list of mirrors and difficulty. It
	 * copies over the contents of the grid and list into more efficiently
	 * stored mirror object.
	 * 
	 * @param grid
	 *            The initial grid that contains the mirrors
	 * @param mirrors
	 *            The initial list that contains the mirrors
	 * @param difficulty
	 *            The difficulty of the AI
	 */
	public AI(final Mirror[][] grid, final List<Mirror> mirrors,
			AIDifficulty difficulty)
	{
		// Copy over the mirror information
		originalGrid = new AIMirror[12][8];
		originalList = new ArrayList<AIMirror>();
		for (Mirror mirror : mirrors)
		{
			AIMirror nextMirror = new AIMirror(mirror.row, mirror.col,
					mirror.isHorizontal());
			originalList.add(nextMirror);
			originalGrid[nextMirror.row][nextMirror.col] = nextMirror;
		}

		// Initialise other variables to default
		isFinishedCalculating = false;
		selectedMove = new Move(null, null);
		thinkLong = false;
		calculationThread = null;
		illegalStart = null;
		illegalEnd = null;
		computerCouldWin = false;
		archivedGrids = new ArrayList<ArrayList<AIMirror>>();
		this.difficulty = difficulty;

		// Add the first grid to the archived grids
		ArrayList<AIMirror> saveGrid = new ArrayList<AIMirror>(
				originalList.size());
		for (AIMirror mirror : originalList)
			saveGrid.add(mirror.clone());
		archivedGrids.add(saveGrid);

	}

	/**
	 * Checks whether the AI is finished calculating the next move
	 * 
	 * @return whether the AI is finished calculating the next move
	 */
	public synchronized boolean isFinishedCalculating()
	{
		if (isFinishedCalculating)
		{
			isFinishedCalculating = false;
			return true;
		}
		return false;
	}

	/**
	 * Returns the move that the AI chose
	 * 
	 * @return the move that the AI chose
	 */
	public Move getMove()
	{
		return selectedMove;
	}

	/**
	 * Tell the AI that a move was made so it can update the board
	 * 
	 * @param move
	 *            The move that was made
	 */
	public void userMadeMove(Move move, boolean undoingMove)
	{
		// Update the grid
		makeMove(originalGrid, move);

		// Save the grid to the archived list if the move is not an undo
		if (!undoingMove)
		{
			ArrayList<AIMirror> saveGrid = new ArrayList<AIMirror>(
					originalList.size());
			for (AIMirror mirror : originalList)
				saveGrid.add(mirror.clone());
			archivedGrids.add(saveGrid);
		}
	}

	/**
	 * Checks if the AI could win after its last turn and stores it
	 */
	public void checkAIWin()
	{
		if (testGrid(originalGrid, false, false) >= 0
				|| testGrid(originalGrid, false, true) >= 0)
			computerCouldWin = true;
		else
			computerCouldWin = false;
	}

	/**
	 * Tells the AI to start calculating a new move along with the last move
	 * which it isn't allowed to undo
	 * 
	 * @param illegalStart
	 *            The start of the previous move
	 * @param illegalEnd
	 *            The end of the previous move
	 */
	public void startCalculatingMove(Point illegalStart, Point illegalEnd)
	{
		this.illegalStart = illegalStart;
		this.illegalEnd = illegalEnd;
		thinkLong = false;
		isFinishedCalculating = false;
		if (calculationThread != null)
			calculationThread.interrupt();
		calculationThread = new Thread(this);
		calculationThread.start();
	}

	/**
	 * Calculates the move taking at least 5 seconds for better gameplay
	 */
	public void run()
	{
		long startTime = System.currentTimeMillis();
		Move foundMove = findMove(originalGrid, originalList, 1);
		int thinkingLength = thinkLong ? 2000
				: (int) (Math.random() * 2000 + 1);
		if (System.currentTimeMillis() - startTime < thinkingLength)
		{
			try
			{
				Thread.sleep(thinkingLength
						- (System.currentTimeMillis() - startTime));
				// If the thread was interrupted, don't update the chosen move
				// so it isn't used
				if (!Thread.currentThread().isInterrupted())
				{
					synchronized (this)
					{
						selectedMove = foundMove;
						isFinishedCalculating = true;
						return;
					}
				}
			}
			catch (InterruptedException e)
			{
			}
		}

		// If the thread was interrupted, don't update the chosen move
		// so it isn't used
		if (!Thread.currentThread().isInterrupted())
		{
			synchronized (this)
			{
				selectedMove = foundMove;
				isFinishedCalculating = true;
				return;
			}
		}

	}

	/**
	 * Chooses a move based on the given grid and difficulty
	 * 
	 * @param grid
	 *            The grid that contains all the mirrors
	 * @param mirrors
	 *            The list of mirrors
	 * @param depth
	 *            The depth of recursion
	 * @return the move that was chosen
	 */
	private Move findMove(final AIMirror[][] grid,
			final ArrayList<AIMirror> mirrors, int depth)
	{
		// Copy the grid over to new variables in synchronized form to avoid
		// access issues
		AIMirror[][] gridCopy = new AIMirror[12][8];
		ArrayList<AIMirror> listCopy = new ArrayList<AIMirror>(mirrors.size());
		synchronized (this)
		{
			for (AIMirror mirror : mirrors)
			{
				AIMirror clone = mirror.clone();
				listCopy.add(clone);
				gridCopy[mirror.row][mirror.col] = clone;
			}
		}

		// If the recursion depth is one, check if the AI can win and return
		// that move based on the AI's difficulty level
		// In later depths this case is checked beforehand
		if (depth == 1
				&& (Math.random() >= 0.4 || difficulty != AIDifficulty.Easy)
				&& (Math.random() >= 0.15 || difficulty != AIDifficulty.Medium))
		{
			if (testGrid(gridCopy, false, false) >= 0)
			{
				return new Move(null, null);
			}
			else if (testGrid(gridCopy, false, true) >= 0)
			{
				return new Move(null, null).needToTurnRight();
			}
		}

		// Check if the human player can win
		boolean humanCanWin = (testGrid(gridCopy, true, false) >= 0 || testGrid(
				gridCopy, true, true) >= 0);

		// If the player made a move after the computer could win and can now
		// win the next turn, give them a luck bonus to reward them
		double bonus = (computerCouldWin && humanCanWin) ? -0.5 : 0.0;

		// If the player can win, reward them by making it seem as if the AI is
		// taking longer to think
		if (humanCanWin)
		{
			if (difficulty == AIDifficulty.Easy
					|| difficulty == AIDifficulty.Medium)
				thinkLong = true;
			else if (difficulty == AIDifficulty.Hard)
				thinkLong = Math.random() >= 0.5 + bonus;
		}

		// Generate a list of all possible moves
		ArrayList<Move> possibleMoves = new ArrayList<Move>();
		for (AIMirror mirror : listCopy)
		{
			if (grid[mirror.row - 1][mirror.col] == null
					&& mirror.canMove(new Point(mirror.row - 1, mirror.col),
							depth == 1))
				possibleMoves.add(new Move(new Point(mirror.row, mirror.col),
						new Point(mirror.row - 1, mirror.col)));
			if (grid[mirror.row][mirror.col - 1] == null
					&& mirror.canMove(new Point(mirror.row, mirror.col - 1),
							depth == 1))
				possibleMoves.add(new Move(new Point(mirror.row, mirror.col),
						new Point(mirror.row, mirror.col - 1)));
			if (grid[mirror.row][mirror.col + 1] == null
					&& mirror.canMove(new Point(mirror.row, mirror.col + 1),
							depth == 1))
				possibleMoves.add(new Move(new Point(mirror.row, mirror.col),
						new Point(mirror.row, mirror.col + 1)));
			if (grid[mirror.row + 1][mirror.col] == null
					&& mirror.canMove(new Point(mirror.row + 1, mirror.col),
							depth == 1))
				possibleMoves.add(new Move(new Point(mirror.row, mirror.col),
						new Point(mirror.row + 1, mirror.col)));
			if (mirror.canMove(new Point(mirror.row, mirror.col), depth == 1))
				possibleMoves.add(new Move(new Point(mirror.row, mirror.col),
						new Point(mirror.row, mirror.col)));
		}

		// Choose a random move in case the AI can't choose a good move or it is
		// in easy mode
		Move randomMove = possibleMoves
				.get((int) (Math.random() * possibleMoves.size()));

		// If the AI is in easy mode, always make a random move unless the
		// computer can win, and if the AI is in medium mode, it only makes a
		// random move 50% of the time.
		// However, this rule only applies if the human player cannot win the
		// next turn
		if (!humanCanWin)
		{
			if (difficulty == AIDifficulty.Easy
					|| (Math.random() >= 0.5 && difficulty == AIDifficulty.Medium))
				return randomMove;
		}

		// Filter out all the moves that lead to an opponent winning or that
		// lead to grids that have already been used
		// Only the impossible AI makes moves that make sure the opponent cannot
		// win because of an unlucky move (all other difficulties ignore the
		// next opponents turn unless they can win using the current game state)
		ArrayList<Move> filteredMoves = new ArrayList<Move>();
		for (Move move : possibleMoves)
		{
			makeMove(gridCopy, move);
			if (!usedGrid(listCopy)
					&& (difficulty != AIDifficulty.Impossible || !(testGrid(
							gridCopy, true, true) >= 0 || testGrid(gridCopy,
							true, false) >= 0)))
				filteredMoves.add(move);
			makeMove(gridCopy, move.reverse());
		}

		// If the human player can win, slightly alter the random move rate for
		// the difficulties
		if (humanCanWin)
		{
			if (difficulty == AIDifficulty.Easy && Math.random() >= 0.3 + bonus)
				return randomMove;
			if (difficulty == AIDifficulty.Medium
					&& Math.random() >= 0.7 + bonus)
				return randomMove;
		}

		// If there are no such possible moves, just make a random move
		if (filteredMoves.isEmpty())
			return randomMove;

		// Finds the best move as defined by the AI winning after moving
		// In case of tie breakers, it picks the move that shoots the laser the
		// farthest
		Move bestMove = null;
		for (Move move : filteredMoves)
		{
			makeMove(gridCopy, move);
			{
				move.setRating(Math.max(testGrid(gridCopy, false, false),
						testGrid(gridCopy, false, true)));
				if (move.getRating() >= 0
						&& (bestMove == null || move.getRating() >= bestMove
								.getRating()))
					bestMove = move;
			}
			makeMove(gridCopy, move.reverse());
		}

		// If a best move was found return it
		if (bestMove != null)
			return bestMove;

		// If a best move wasn't found and the AI is set to impossible, recurse
		// one move depth to look 2 moves ahead
		if (difficulty == AIDifficulty.Impossible && depth < 2)
		{
			for (Move move : filteredMoves)
			{
				makeMove(gridCopy, move);
				Move newMove = findMove(gridCopy, listCopy, depth + 1);
				if (newMove.getRating() > 0
						&& (bestMove == null || newMove.getRating() >= bestMove
								.getRating()))
					bestMove = move;
				makeMove(gridCopy, move.reverse());
			}
		}

		// If after recursing, there was no best move found, or the AI is set to
		// easy, return a random move that stops the opponent from winning
		if (bestMove == null)
			return filteredMoves.get((int) (Math.random() * filteredMoves
					.size()));
		else
			return bestMove;
	}

	/**
	 * Checks whether the given grid was used before
	 * 
	 * @param list
	 *            The list of mirrors that describe the current grid
	 * @return whether the current grid was used before
	 */
	private boolean usedGrid(ArrayList<AIMirror> list)
	{
		int numMirrors = archivedGrids.get(0).size();
		for (ArrayList<AIMirror> mirrors : archivedGrids)
		{
			boolean sameGrid = true;
			for (int index = 0; sameGrid && index < numMirrors; index++)
			{
				if (!mirrors.get(index).equals(list.get(index)))
					sameGrid = false;
			}
			if (sameGrid)
				return true;
		}
		return false;
	}

	/**
	 * Makes a move by updating the given grid and mirrors
	 * 
	 * @param grid
	 *            The grid that contains all the mirrors
	 * @param move
	 *            The move to make
	 */
	private void makeMove(AIMirror[][] grid, Move move)
	{
		// Retrieve the starting and ending points
		Point startPoint = move.getStartPoint();
		Point endPoint = move.getEndPoint();

		// If the move was a shot, return
		if (startPoint == null && endPoint == null)
			return;

		// If the move was a turn, turn the mirror, otherwise move it
		if (startPoint.equals(endPoint))
		{
			AIMirror movedMirror = grid[endPoint.x][endPoint.y];
			movedMirror.isHorizontal = !movedMirror.isHorizontal;
		}
		else
		{
			AIMirror movedMirror = grid[startPoint.x][startPoint.y];
			grid[endPoint.x][endPoint.y] = grid[startPoint.x][startPoint.y];
			grid[startPoint.x][startPoint.y] = null;
			movedMirror.col = endPoint.y;
			movedMirror.row = endPoint.x;
		}
	}

	/**
	 * Tests to see if the given grid is a winning configuration if shot from
	 * the given player in the given direction
	 * 
	 * @param grid
	 *            The grid that contains the mirrors
	 * @param playerOneTurn
	 *            Whether it is player one's turn
	 * @param turnedRight
	 *            Whether the player is shooting right (from their perspective)
	 * @return -1 if it is not a winning shot or a positive integer representing
	 *         the length of the winning shot
	 */
	private int testGrid(AIMirror[][] grid, boolean playerOneTurn,
			boolean turnedRight)
	{
		Point laserStart;
		int laserDirection;

		// Based on whose turn it is and if the player is shooting right, find
		// the starting position and direction of the laser
		if (!playerOneTurn)
		{
			if (turnedRight)
			{
				laserStart = new Point(3, 1);
				laserDirection = 3;
			}
			else
			{
				laserStart = new Point(4, 1);
				laserDirection = 4;
			}
		}
		else
		{
			if (turnedRight)
			{
				laserStart = new Point(4, 10);
				laserDirection = 1;
			}
			else
			{
				laserStart = new Point(3, 10);
				laserDirection = 2;
			}
		}

		// Store the next point that the laser is at and the total shot length
		// and number of back bounces
		Point nextPoint = new Point(laserStart);
		int numBackBounces = 0;
		int shotLength = 0;

		while (true)
		{
			// If the laser is bouncing off the side
			if (nextPoint.x == 0 || nextPoint.x == 7)
			{
				// If the laser is in the corner, return no win
				if (nextPoint.y == 0 || nextPoint.y == 11)
					return -1;

				// Otherwise, turn appropriately
				if (laserDirection == 1)
					laserDirection = 2;
				else if (laserDirection == 2)
					laserDirection = 1;
				else if (laserDirection == 3)
					laserDirection = 4;
				else
					laserDirection = 3;
			}
			// Otherwise, if the laser is just bouncing off the back
			else if (nextPoint.y == 0 || nextPoint.y == 11)
			{
				// Check if it has already bounced back once and return if it
				// did
				if (numBackBounces == 1)
					return -1;
				else
					numBackBounces++;

				// Turn appropriately
				if (laserDirection == 1)
					laserDirection = 4;
				else if (laserDirection == 4)
					laserDirection = 1;
				else if (laserDirection == 3)
					laserDirection = 2;
				else
					laserDirection = 3;
			}
			else
			{
				// If the laser is not bouncing off the sides, check if there is
				// a mirror at the next position and turn if needed
				AIMirror mirror = grid[nextPoint.y][nextPoint.x];
				if (mirror != null)
				{
					if (mirror.isHorizontal)
					{
						if (laserDirection == 1)
							laserDirection = 4;
						else if (laserDirection == 4)
							laserDirection = 1;
						else if (laserDirection == 3)
							laserDirection = 2;
						else
							laserDirection = 3;
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
					}
				}
			}

			// Check if the laser will hit the target the next turn, and if it
			// does, return the length of the laser shot
			if (nextPoint.x == 3)
			{
				if (nextPoint.y == 5 && laserDirection == 4)
				{
					return shotLength;
				}
				else if (nextPoint.y == 6 && laserDirection == 1)
				{
					return shotLength;
				}
			}
			else if (nextPoint.x == 4)
			{
				if (nextPoint.y == 5 && laserDirection == 3)
				{
					return shotLength;
				}
				else if (nextPoint.y == 6 && laserDirection == 2)
				{
					return shotLength;
				}
			}

			// Check if the laser will hit the top gun and return no win if it
			// does
			if (nextPoint.x == 3)
			{
				if (nextPoint.y == 0 && laserDirection == 4)
				{
					return -1;
				}
				else if (nextPoint.y == 1 && laserDirection == 1)
				{
					return -1;
				}
			}
			else if (nextPoint.x == 4)
			{
				if (nextPoint.y == 0 && laserDirection == 3)
				{
					return -1;
				}
				else if (nextPoint.y == 1 && laserDirection == 2)
				{
					return -1;
				}
			}

			// Check if the laser will hit the bottom gun and return no win if
			// it does
			if (nextPoint.x == 3)
			{
				if (nextPoint.y == 10 && laserDirection == 4)
				{
					return -1;
				}
				else if (nextPoint.y == 11 && laserDirection == 1)
				{
					return -1;
				}
			}
			else if (nextPoint.x == 4)
			{
				if (nextPoint.y == 10 && laserDirection == 3)
				{
					return -1;
				}
				else if (nextPoint.y == 11 && laserDirection == 2)
				{
					return -1;
				}
			}

			// Move the laser in the appropriate direction
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

			// Update the shot length
			shotLength++;
		}
	}

	/**
	 * The AIMirror class is used to efficiently store and access mirror data
	 * 
	 * @author Peter Gokhshteyn and Alex Szoke
	 * @version June 15, 2013
	 */
	private class AIMirror
	{
		// Store the row, column and whether the mirror is horizonal
		private int row;

		private int col;

		private boolean isHorizontal;

		/**
		 * Create a new mirror with the given data
		 * 
		 * @param row
		 *            The row the mirror is in
		 * @param col
		 *            The column the mirror is in
		 * @param isHorizonal
		 *            Whether the mirror is horizontal
		 */
		private AIMirror(int row, int col, boolean isHorizonal)
		{
			this.row = row;
			this.col = col;
			this.isHorizontal = isHorizonal;
		}

		/**
		 * Creates an exact copy of this mirror and returns it
		 * 
		 * @return an exact copy of this mirror
		 */
		public AIMirror clone()
		{
			return new AIMirror(row, col, isHorizontal);
		}

		/**
		 * Checks whether this mirror can move to the given point based on
		 * whether the player can undo moves or not
		 * 
		 * @param point
		 *            The point try to move this mirror to
		 * @param checkUndo
		 *            Whether to check undoing moves (doesn't check after
		 *            recursing)
		 * @return whether this mirror can move to the given position
		 */
		private boolean canMove(Point point, boolean checkUndo)
		{
			// If the move is to turn the mirror, check if turning would be
			// undoing a move and return the result
			if (point.equals(new Point(this.row, this.col)))
			{
				if (!checkUndo || !point.equals(illegalStart)
						|| !point.equals(illegalEnd))
					return true;
				else
					return false;
			}

			// Check if the move would undo the last move and return false if it
			// does
			if (checkUndo && new Point(this.row, this.col).equals(illegalEnd)
					&& point.equals(illegalStart))
				return false;

			// Check if the point is out of bounds and return false if it is
			if (point.y == 0 || point.y == 7 || point.x == 0 || point.x == 11)
				return false;
			if ((point.x < 2 || point.x > 9) && (point.y == 3 || point.y == 4))
				return false;

			// Return whether the point is right beside the mirror
			return ((point.y == col && point.x == row - 1)
					|| (point.y == col + 1 && point.x == row)
					|| (point.y == col && point.x == row + 1) || (point.y == col - 1 && point.x == row));
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
			AIMirror otherMirror = (AIMirror) object;
			return (this.row == otherMirror.row && this.col == otherMirror.col && this.isHorizontal == otherMirror.isHorizontal);
		}
	}
}
