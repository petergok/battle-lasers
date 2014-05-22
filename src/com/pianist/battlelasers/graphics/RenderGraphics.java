package com.pianist.battlelasers.graphics;

import com.pianist.battlelasers.activities.BattleLaserActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * The RenderGraphics class deals with drawing what the game want to present to
 * the screen. It constantly presents what the game draws in the framebuffer and
 * keeps track of the time between frames.
 * 
 * @author Peter Gokhshteyn
 */
public class RenderGraphics extends SurfaceView implements Runnable {
	BattleLaserActivity game;
	SurfaceHolder surface;
	Bitmap frameBuffer;
	Thread graphicsThread = null;
	volatile boolean running = false;

	/**
	 * Creates a new RenderGraphics with the given game object and framebuffer
	 * that the game will draw to
	 * 
	 * @param game
	 *            The game that this RenderGraphics is being created for
	 * @param frameBuffer
	 *            The framebuffer that the game will be drawing to
	 */
	public RenderGraphics(Bitmap frameBuffer, BattleLaserActivity game) {
		super(game);
		this.game = game;
		this.frameBuffer = frameBuffer;
		this.surface = getHolder();
	}

	/**
	 * Resumes the running of the RenderGraphics
	 */
	public void resume() {
		running = true;
		graphicsThread = new Thread(this);
		graphicsThread.start();
	}

	/**
	 * Runs the RenderGraphics by counting the change in time and using it to
	 * update the screen and drawing the frame buffer to the canvas
	 */
	public void run() {
		Rect dstRect = new Rect();
		long startTime = System.nanoTime();
		while (running) {
			if (!surface.getSurface().isValid())
				continue;
			
			float deltaTime = (System.nanoTime() - startTime) / 1000000000.0f;
			startTime = System.nanoTime();
			
			game.getCurrentScreen().update(deltaTime);
			game.getCurrentScreen().present(deltaTime);
			
			Canvas canvas = surface.lockCanvas();
			canvas.getClipBounds(dstRect);
			canvas.drawBitmap(frameBuffer, null, dstRect, null);
			surface.unlockCanvasAndPost(canvas);
			
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{
			}
		}
	}

	/**
	 * Pauses the rendering graphics
	 */
	public void pause() {
		running = false;
		while (true) {
			try {
				graphicsThread.join();
				return;
			} catch (InterruptedException e) {
				// retry
			}
		}
	}
}
