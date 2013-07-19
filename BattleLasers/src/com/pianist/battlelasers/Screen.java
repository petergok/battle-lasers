package com.pianist.battlelasers;

/**
 * The Screen class defines a general Screen that would be presented in the game
 * 
 * @author Peter Gokhshteyn
 * @version May 26, 2013
 */
public abstract class Screen {
	
	// Store the game object to use for drawing, input management and other access to features
    protected final BattleLaserGame game;

    /**
     * Creates a default Screen by passing the BattleLaserGame object and storing it
     * 
     * @param game
     * 		The BattleLaserGame that created this screen
     */
    public Screen(BattleLaserGame game) {
        this.game = game;
    }

    /**
     * Updates the Screen by passing the amount of time since the last frame for smooth movement
     * 
     * @param deltaTime
     * 		The time since the last frame
     */
    public abstract void update(float deltaTime);

    /**
     * Presents the Screen by letting it draw itself to the framebuffer using the given time since the last frame
     * 
     * @param deltaTime
     * 		The time since the last frame
     */
    public abstract void present(float deltaTime);

    /**
     * Tells the Screen to pause to it can save the game state
     */
    public abstract void pause();

    /**
     * Resumes the screen using the saved game state
     */
    public abstract void resume();

    /**
     * Tells the Screen it is about to be disposed so it can save necessary game state
     */
    public abstract void dispose();
}

