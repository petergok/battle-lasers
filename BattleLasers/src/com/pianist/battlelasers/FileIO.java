package com.pianist.battlelasers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.preference.PreferenceManager;

/**
 * The FileIO class deals with writing and reading from external files (mainly
 * loading images)
 * 
 * @author Peter Gokhshteyn and "Apress Beginning Android Games"
 * @version May 14, 2013
 */
public class FileIO
{
	// The context from where to read the files in and write out
	Context context;

	// An asset manager to deal with assets
	AssetManager assets;

	// The path to the external storage
	String externalStoragePath;

	/**
	 * Create a new File input output object with the given context
	 * 
	 * @param context
	 *            the context from where to read and write files
	 */
	public FileIO(Context context)
	{
		this.context = context;
		this.assets = context.getAssets();
		this.externalStoragePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator;
	}

	/**
	 * Reads from an asset with the given filename
	 * 
	 * @param fileName
	 *            The name of the file to read
	 * @return an InputStream for the file
	 * @throws IOException
	 *             If there was an error finding the file
	 */
	public InputStream readAsset(String fileName) throws IOException
	{
		return assets.open(fileName);
	}

	/**
	 * Reads from a non-asset file with the given filename
	 * 
	 * @param fileName
	 *            The name of the file to read
	 * @return an InputStream for the file
	 * @throws IOException
	 *             If there was an error finding the file
	 */
	public InputStream readFile(String fileName) throws IOException
	{
		return new FileInputStream(externalStoragePath + fileName);
	}

	/**
	 * Writes to a given file with the given filename
	 * 
	 * @param fileName
	 *            The name of the file to write to
	 * @return an OutputStream to write to the file
	 * @throws IOException
	 *             If there was an error creating or finding the file
	 */
	public OutputStream writeFile(String fileName) throws IOException
	{
		return new FileOutputStream(externalStoragePath + fileName);
	}
}
