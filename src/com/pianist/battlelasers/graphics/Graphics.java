package com.pianist.battlelasers.graphics;

import java.io.IOException;

import java.io.InputStream;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.Paint.Style;

/**
 * The Graphics class is the graphics content that draws all the images of the
 * game
 * 
 * @author Peter Gokhshteyn and "Apress Beginning Android Games"
 * @version Date: June 16, 2013
 */
public class Graphics
{
	AssetManager assets;

	Bitmap frameBuffer;

	Canvas canvas;

	Paint paint;

	Rect srcRect = new Rect();

	Rect dstRect = new Rect();

	public static enum PixmapFormat {
		ARGB8888, ARGB4444, RGB565
	}

	/**
	 * Creates the graphics content for the activity according to the given
	 * assets and framebuffer
	 * 
	 * @param assets
	 *            The activities assets
	 * @param frameBuffer
	 *            the frame set for the activity
	 */
	public Graphics(AssetManager assets, Bitmap frameBuffer)
	{
		this.assets = assets;
		this.frameBuffer = frameBuffer;
		this.canvas = new Canvas(frameBuffer);
		this.paint = new Paint();
	}

	/**
	 * Creates a pixmap image for the given files and format
	 * 
	 * @param fileName
	 *            The name of the image being created
	 * @param format
	 *            The format of the image being created
	 * @return the pixmap image that was created
	 */
	public Pixmap newPixmap(String fileName, PixmapFormat format)
	{
		Config config = null;

		// Handles the different formats
		if (format == PixmapFormat.RGB565)
			config = Config.RGB_565;
		else if (format == PixmapFormat.ARGB4444)
			config = Config.ARGB_4444;
		else
			config = Config.ARGB_8888;

		Options options = new Options();
		options.inPreferredConfig = config;

		InputStream in = null;
		Bitmap bitmap = null;
		try
		{
			in = assets.open(fileName);
			bitmap = BitmapFactory.decodeStream(in);
			if (bitmap == null)
				throw new RuntimeException("Couldn't load bitmap from asset '"
						+ fileName + "'");
		}
		catch (IOException e)
		{
			throw new RuntimeException("Couldn't load bitmap from asset '"
					+ fileName + "'");
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e)
				{
				}
			}
		}
		if (bitmap.getConfig() == Config.RGB_565)
			format = PixmapFormat.RGB565;
		else if (bitmap.getConfig() == Config.ARGB_4444)
			format = PixmapFormat.ARGB4444;
		else
			format = PixmapFormat.ARGB8888;
		return new Pixmap(bitmap, format);

	}

	/**
	 * Clears the canvas by drawing uniformally over it
	 * 
	 * @param color
	 *            The colour to clear the canvas with
	 */
	public void clear(int color)
	{
		canvas.drawRGB((color & 0xff0000) >> 16, (color & 0xff00) >> 8,
				(color & 0xff));
	}

	/**
	 * Draws a pixel at the given point with the given colour
	 * 
	 * @param x
	 *            The x coordinate to draw a pixel at
	 * @param y
	 *            The y coordinate to draw a pixel at
	 * @param color
	 *            The colour to draw the pixel in
	 */
	public void drawPixel(int x, int y, int color)
	{
		paint.setColor(color);
		canvas.drawPoint(x, y, paint);
	}

	/**
	 * Draws a line from the starting point, xy to the end point x2y2 in the
	 * given colour
	 * 
	 * @param x
	 *            The x coordinate of the starting point
	 * @param y
	 *            The y coordinate of the starting point
	 * @param x2
	 *            The x coordinate of the ending point
	 * @param y2
	 *            The y coordinate of the ending point
	 * @param color
	 *            The colour to draw the line in
	 */
	public void drawLine(int x, int y, int x2, int y2, int color)
	{
		paint.setColor(color);
		paint.setStrokeWidth(3);
		canvas.drawLine(x, y, x2, y2, paint);
	}

	/**
	 * Draws a rectangle at the given point xy with the given width and height
	 * and the given colour
	 * 
	 * @param x
	 *            The x coordinate to draw the rectangle
	 * @param y
	 *            The y coordinate to draw the rectangle
	 * @param width
	 *            The width of the rectangle
	 * @param height
	 *            The height of the rectangle
	 * @param color
	 *            The colour of the rectangle
	 * @param filled
	 *            Whether or not the rectangle should be filled in or not
	 */
	public void drawRect(int x, int y, int width, int height, int color,
			boolean filled)
	{
		paint.setColor(color);
		if (filled)
			paint.setStyle(Style.FILL);
		else
		{
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(3);
		}
		canvas.drawRect(x, y, x + width - 1, y + height - 1, paint);
	}

	/**
	 * Draws a pixmap image at the given coordinates
	 * 
	 * @param pixmap
	 *            The pixmap image to draw
	 * @param x
	 *            The x coordinate of the scaled image
	 * @param y
	 *            The y coordinate of the scaled image
	 * @param srcX
	 *            The oringal x coordinate
	 * @param srcY
	 *            The oringal y coordinate
	 * @param srcWidth
	 *            The original Width
	 * @param srcHeight
	 *            The original Height
	 */
	public void drawPixmap(Pixmap pixmap, int x, int y, int srcX, int srcY,
			int srcWidth, int srcHeight)
	{
		srcRect.left = srcX;
		srcRect.top = srcY;
		srcRect.right = srcX + srcWidth - 1;
		srcRect.bottom = srcY + srcHeight - 1;

		dstRect.left = x;
		dstRect.top = y;
		dstRect.right = x + srcWidth - 1;
		dstRect.bottom = y + srcHeight - 1;
		canvas.drawBitmap(((Pixmap) pixmap).bitmap, srcRect, dstRect, null);
	}

	/**
	 * Draws a circle based on the given coordinates, radius and colour
	 * 
	 * @param x
	 *            The x coordinate of the center of the circle
	 * @param y
	 *            The y coordinate of the center of the circle
	 * @param radius
	 *            The radius of the circle
	 * @param color
	 *            The to draw the circle in
	 * @param filled
	 *            Whether or not to fill the circle in or not
	 */
	public void drawCircle(int x, int y, int radius, int color, boolean filled)
	{
		paint.setColor(color);
		if (filled)
			paint.setStyle(Style.FILL);
		else
		{
			paint.setStyle(Style.STROKE);
			paint.setStrokeWidth(3);
		}
		canvas.drawCircle(x, y, radius, paint);
	}
	
	/**
	 * Draws this given text on the canvas
	 * 
	 * @param x
	 * 			 The x coordinate of the text
	 * @param y
	 * 			 The y coordinate of the text
	 * @param size
	 * 			 The size of the text
	 * @param text
	 * 		  	 The text to draw
	 */
	public void drawText(int x, int y, float size, String text) 
	{
		paint.setColor(Color.WHITE);
		paint.setStyle(Style.FILL);
		paint.setTextSize(size);
		canvas.drawText(text, x, y, paint);
	}

	/**
	 * Draws a pixmap image at the given coordinates
	 * 
	 * @param pixmap
	 *            The image to draw
	 * @param x
	 *            The x coordinate of the image
	 * @param y
	 *            The y coordinate of the image
	 */
	public void drawPixmap(Pixmap pixmap, int x, int y)
	{
		canvas.drawBitmap(((Pixmap) pixmap).bitmap, x, y, null);
	}

	/**
	 * Returns the width of the frameBuffer
	 * 
	 * @return The frame buffer's width
	 */
	public int getWidth()
	{
		return frameBuffer.getWidth();
	}

	/**
	 * Returns the height of the frameBuffer
	 * 
	 * @return The frame buffer's height
	 */
	public int getHeight()
	{
		return frameBuffer.getHeight();
	}
}
