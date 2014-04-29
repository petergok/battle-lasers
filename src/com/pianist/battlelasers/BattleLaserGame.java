package com.pianist.battlelasers;

import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * BattleLaserGame is the main game activity of the project. It handles the
 * creation of the screen as well as the transitioning between them. It also
 * deals stores all the objects that deal with input and output to the user and
 * files.
 * 
 * @author Alex Szoke & Peter Gokhshteyn
 * @version Date: June 16, 2013
 */
public class BattleLaserGame extends Activity
{
	private static String TAG = "BattleLaserGame";
	
	// Deals with drawing the framebuffer to the screen and updating all the
	// game screens
	RenderGraphics renderView;

	// Deals with all the graphics implementation in the game
	Graphics graphics;

	// Deals with all the user input
	Input input;

	// Deals with file input and output
	FileIO fileIO;

	// The current screen that is showing
	Screen screen;

	// The framebuffer width and height
	int frameBufferWidth;

	int frameBufferHeight;
	
	private int mRating;
	
	// Play services resolution request
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	public static final String BASE_URL = "http://mysterious-wave-3427.herokuapp.com";
	
	/**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    public static String SENDER_ID = "237835227996";
	
	// GCM service variables
	private GoogleCloudMessaging gcm;
	private String regid;
	
	// Context
	private Context mContext;
	
	// Preferences file
	public static final String BATTLE_LASERS_PREFS = "battle_lasers_prefs";
	
	public static final String MATCH_STARTED = "com.pianist.battlelasers.MATCH_STARTED";
	public static final String MOVE = "com.pinaist.battlelasers.MOVE";
	
	public static final String PREF_RATING = "pref_rating";
	
	private BroadcastReceiver bReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        if(intent.getAction().equals(MATCH_STARTED)) {
	        	String otherPlayerName = intent.getStringExtra("otherPlayerName");
	        	int playerNumber = intent.getIntExtra("playerNumber", 0);
	        	int mapId = intent.getIntExtra("mapId", 1);
	        	if (screen instanceof MultiSetupScreen) {
	    			((MultiSetupScreen) screen).createdMatch(otherPlayerName, mapId, playerNumber);
	    		}
	        } else if (intent.getAction().equals(MOVE)) {
	        	Point moveStart = new Point(intent.getIntExtra("startRow", -1), intent.getIntExtra("startCol", -1));
	        	Point moveEnd = new Point(intent.getIntExtra("endRow", -1), intent.getIntExtra("endCol", -1));
	        	if (moveStart.y > 6 || moveStart.y < 1 || moveEnd.y > 6 || moveEnd.y < 1 || 
	        			moveStart.x > 10 || moveStart.x < 1 || moveEnd.x > 10 || moveEnd.x < 1) {
	        		moveStart = null;
	        		moveEnd = null;
	        	}
	        	if (screen instanceof GameScreen) {
	        		((GameScreen) screen).onlineMoveMade(moveStart, moveEnd);
	        	}
	        }
	    }
	};

	/**
	 * The main method that gets called on creation of the activity. It
	 * initializes all the variables and starts the running of the program.
	 * 
	 * @param savedInstanceState
	 *            Passed to the super constructor to create the activity
	 */
	public void onCreate(Bundle savedInstanceState)
	{
		// Call the super method to create the activity
		super.onCreate(savedInstanceState);

		// Ask for no window title and fullscreen, as well as to keep the screen
		// from dimming
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Create the framebuffer that is draw to and its dimensions
		boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		frameBufferWidth = isLandscape ? 800 : 480;
		frameBufferHeight = isLandscape ? 480 : 800;

		Bitmap frameBuffer = Bitmap.createBitmap(frameBufferWidth,
				frameBufferHeight, Config.ARGB_8888);

		// Gets the scale factor between the screen and buffer to handle user
		// input
		float scaleX = (float) frameBufferWidth
				/ getWindowManager().getDefaultDisplay().getWidth();
		float scaleY = (float) frameBufferHeight
				/ getWindowManager().getDefaultDisplay().getHeight();

		// Create all the objects that run the game
		renderView = new RenderGraphics(frameBuffer, this);
		graphics = new Graphics(getAssets(), frameBuffer);
		fileIO = new FileIO(this);
		input = new Input(this, renderView, scaleX, scaleY);
		screen = new MainMenuScreen(this, true, new Match());
		setContentView(renderView);
		
		mContext = getApplicationContext();
		
		SharedPreferences settings = getSharedPreferences(BATTLE_LASERS_PREFS, 0);
	    mRating = settings.getInt(PREF_RATING, 1000);
		
		LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MATCH_STARTED);
		intentFilter.addAction(MOVE);
		bManager.registerReceiver(bReceiver, intentFilter);
	}
	
	public void registerGCM() {
		// Check device for Play Services APK.
	    if (checkPlayServices()) {
	        // If this check succeeds, proceed with normal processing.
	        // Otherwise, prompt user to get valid Play Services APK
	    	gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(mContext);

            if (regid.isEmpty()) {
                registerInBackground();
            } else {
            	sendRegistrationIdToBackend();
            }
	    	
	    } else {
	    	Log.i(TAG, "No valid Google Play Services APK found.");
	    }
	}
	
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return getSharedPreferences(BattleLaserGame.class.getSimpleName(),
	            Context.MODE_PRIVATE);
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
	    new AsyncTask<Void, Void, String>() {
	        @Override
	        protected String doInBackground(Void... params) {
	            String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(mContext);
	                }
	                regid = gcm.register(SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;

	                sendRegistrationIdToBackend();

	                // Persist the regID - no need to register again.
	                storeRegistrationId(mContext, regid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	            }
	            return msg;
	        }

	        @Override
	        protected void onPostExecute(String msg) {
	        }
	    }.execute(null, null, null);
	}
	
	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
	 * or CCS to send messages to your app. Not needed for this demo since the
	 * device sends upstream messages to a server that echoes back the message
	 * using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		new SendRegistrationIdTask(this, regid, mRating).execute();
	}
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    int appVersion = getAppVersion(context);
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}

	/**
	 * When the game is resumed, tell the graphics renderer to start drawing again
	 */
	public void onResume()
	{
		super.onResume();
		screen.resume();
		renderView.resume();
	}

	/**
	 * When the game is paused, tell the graphics to stop rendering, and if the game is closing tell the screen it is being closed
	 */
	public void onPause()
	{
		super.onPause();
		renderView.pause();
		screen.pause();
		if (isFinishing())
			screen.dispose();
	}

	/**
	 * Returns the objects that deals with user input
	 * 
	 * @return the input object
	 */
	public Input getInput()
	{
		return input;
	}

	/**
	 * Returns the object that deals with file input and output
	 * 
	 * @return the input output object
	 */ 
	public FileIO getFileIO()
	{
		return fileIO;
	}

	/**
	 * Returns the object that deals with graphics
	 * 
	 * @return the graphics object
	 */
	public Graphics getGraphics()
	{
		return graphics;
	}

	/**
	 * Switches to a new given screen
	 * 
	 * @param screen
	 * 		The screen to switch to
	 */
	public void setScreen(Screen screen)
	{
		if (screen == null)
			throw new IllegalArgumentException("Screen is null");
		
		// Pause and dispose the current screen
		this.screen.pause();
		this.screen.dispose();
		
		// Resume the new screen and update it
		screen.resume();
		screen.update(0);
		this.screen = screen;
	}

	/**
	 * Returns the current screen
	 * 
	 * @return the current screen
	 */
	public Screen getCurrentScreen()
	{
		return screen;
	}

	/**
	 * Disposes of a given image
	 * 
	 * @param image the image to dispose
	 */
	public void disposeImage(Pixmap image)
	{
		if (image != null)
		{
			image.dispose();
		}
	}
	
	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}
	
	public void registeredUser(int userId) {
		if (screen instanceof MultiSetupScreen) {
			((MultiSetupScreen) screen).registeredUser(userId);
		}
	}
	
	public void showProgressDialog() {

	}
	
	public void dismissProgressDialog() {
	}
	
	
}
