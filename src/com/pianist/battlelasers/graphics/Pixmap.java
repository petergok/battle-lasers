package com.pianist.battlelasers.graphics;

import com.pianist.battlelasers.graphics.Graphics.PixmapFormat;

import android.graphics.Bitmap;

/**
 * Keeps track of an image in a Pixmap form. It creates an association between a
 * bitmap image and format for that image
 * 
 * @author Alex Szoke & Peter Gokhshteyn
 * @version Date: June 16, 2013
 */
public class Pixmap
{
	Bitmap bitmap;

	PixmapFormat format;

	/**
	 * Creates a Pixmap object that connects a bitmap image to a given format
	 * 
	 * @param bitmap
	 *            The given image to use
	 * @param format
	 *            The given format to use
	 */
	public Pixmap(Bitmap bitmap, PixmapFormat format)
	{
		this.bitmap = bitmap;
		this.format = format;
	}

	/**
	 * Returns the width of the image
	 * @return the image's width
	 */
	public int getWidth()
	{
		return bitmap.getWidth();
	}

	/**
	 * Returns the height of the image
	 * @return the image's height
	 */
	public int getHeight()
	{
		return bitmap.getHeight();
	}

	/**
	 * Returns the format of the image
	 * @return the image's format
	 */
	public PixmapFormat getFormat()
	{
		return format;
	}

	/**
	 * Disposes of the image
	 */
	public void dispose()
	{
		bitmap.recycle();
	}
}
