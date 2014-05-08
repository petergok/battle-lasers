package com.pianist.battlelasers.activities;

import java.io.IOException;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.pianist.battlelasers.FileIO;
import com.pianist.battlelasers.game_objects.Match;
import com.pianist.battlelasers.graphics.Graphics;
import com.pianist.battlelasers.graphics.Pixmap;
import com.pianist.battlelasers.graphics.RenderGraphics;
import com.pianist.battlelasers.input_handlers.Input;
import com.pianist.battlelasers.screens.GameScreen;
import com.pianist.battlelasers.screens.MainMenuScreen;
import com.pianist.battlelasers.screens.MultiSetupScreen;
import com.pianist.battlelasers.screens.Screen;
import com.pianist.battlelasers.tasks.AcceptMatchTask;
import com.pianist.battlelasers.tasks.DeclineMatchTask;
import com.pianist.battlelasers.tasks.SendRegistrationIdTask;
import com.pianist.battlelasers.tasks.UnregisterPlayerTask;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
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
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

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
	
	private ProgressDialog mProgressDialog;
	
	private AlertDialog mAlertDialog;
	
	private Match mMatch;
	
	public boolean isGuideCompleted;
	
	// Preferences file
	public static SharedPreferences settings;
	public static final String BATTLE_LASERS_PREFS = "battle_lasers_prefs";
	
	public static final String MATCH_START = "com.pianist.battlelasers.MATCH_START";
	public static final String MATCH_FOUND = "com.pianist.battlelasers.MATCH_FOUND";
	public static final String MATCH_END = "com.pianist.battlelasers.MATCH_END";
	public static final String MOVE = "com.pinaist.battlelasers.MOVE";
	
	public static final String PREF_RATING = "pref_rating";
	public static final String PREF_USER_ID = "pref_user_id";
	public static final String PREF_GUIDE_COMPLETED = "pref_guide_completed";
	
	public static int screenHeight;
	
	private BroadcastReceiver bReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        if(intent.getAction().equals(MATCH_FOUND)) {
	        	String otherPlayerName = intent.getStringExtra("otherPlayerName");
	        	int playerNumber = intent.getIntExtra("playerNumber", 0);
	        	int otherPlayerRating = intent.getIntExtra("otherPlayerRating", 1000);
	        	int mapId = intent.getIntExtra("mapId", 1);
	        	if (screen instanceof MultiSetupScreen) {
	    			((MultiSetupScreen) screen).createdMatch(otherPlayerName, mapId, playerNumber, otherPlayerRating);
	    		}
	        } else if (intent.getAction().equals(MOVE)) {
	        	Point moveStart = new Point(intent.getIntExtra("startRow", -1), intent.getIntExtra("startCol", -1));
	        	Point moveEnd = new Point(intent.getIntExtra("endRow", -1), intent.getIntExtra("endCol", -1));
	        	boolean turnRight = intent.getBooleanExtra("turnRight", false);
	        	if (moveStart.y > 6 || moveStart.y < 1 || moveEnd.y > 6 || moveEnd.y < 1 || 
	        			moveStart.x > 10 || moveStart.x < 1 || moveEnd.x > 10 || moveEnd.x < 1) {
	        		moveStart = null;
	        		moveEnd = null;
	        	}
	        	if (screen instanceof GameScreen) {
	        		((GameScreen) screen).onlineMoveMade(moveStart, moveEnd, turnRight);
	        	}
	        } else if (intent.getAction().equals(MATCH_END)) {
	        	new UnregisterPlayerTask(mMatch.onlineUserId).execute();
	        	if (!mMatch.matchStarted && (screen instanceof MultiSetupScreen || screen instanceof GameScreen)) {
	        		showUserDeclinedDialog();
	        	} else if (mMatch.matchStarted) {
	        		showUserForfeitDialog();
	        	}
	        	SharedPreferences.Editor editor = BattleLaserGame.settings.edit();
	    	    editor.putInt(BattleLaserGame.PREF_USER_ID, 0);
	    	    editor.commit();
	        } else if (intent.getAction().equals(MATCH_START)) {
	        	if (screen instanceof GameScreen) {
	        		dismissProgressDialog();
	        		((GameScreen) screen).startOnlineMatch();
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
		screenHeight = getResources().getDisplayMetrics().heightPixels;
		float scaleX = (float) frameBufferWidth
				/ getResources().getDisplayMetrics().widthPixels;
		float scaleY = (float) frameBufferHeight
				/ screenHeight;

		// Create all the objects that run the game
		renderView = new RenderGraphics(frameBuffer, this);
		graphics = new Graphics(getAssets(), frameBuffer);
		fileIO = new FileIO(this);
		input = new Input(this, renderView, scaleX, scaleY);
		
		mMatch = new Match();
		settings = getSharedPreferences(BATTLE_LASERS_PREFS, 0);
	    mMatch.onlineRating = settings.getInt(PREF_RATING, 1000);
	    mMatch.onlineUserId = settings.getInt(PREF_USER_ID, 0);
	    isGuideCompleted = settings.getBoolean(PREF_GUIDE_COMPLETED, false);
		screen = new MainMenuScreen(this, true, mMatch);
		setContentView(renderView);
		
		mContext = getApplicationContext();
		
		LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MATCH_START);
		intentFilter.addAction(MOVE);
		intentFilter.addAction(MATCH_FOUND);
		intentFilter.addAction(MATCH_END);
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
	
	public void guideStarted() {
		SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean(PREF_GUIDE_COMPLETED, true);
	    editor.commit();
	    isGuideCompleted = true;
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
		new SendRegistrationIdTask(this, regid, mMatch.onlineRating).execute();
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
	@Override
	public void onPause()
	{
		super.onPause();
		renderView.pause();
		screen.pause();
		if (isFinishing())
			screen.dispose();
	}
	
	@Override
	public void onStop() {
		if (mMatch != null && mMatch.onlineUserId != 0 && mMatch.matchStarted) {
			loseOnlineGame();
			new UnregisterPlayerTask(mMatch.onlineUserId).execute();
		} else if (mMatch != null && mMatch.isOnline) {
			new UnregisterPlayerTask(mMatch.onlineUserId).execute();
		}
		mMatch.showDialogs = false;
		super.onStop();
	}
	
	public void loseOnlineGame() {
		mMatch.loseOnlineGame();
		SharedPreferences.Editor editor = settings.edit();
	    editor.putInt(PREF_RATING, mMatch.onlineRating);
	    editor.commit();
	}
	
	public void winOnlineGame() {
		mMatch.winOnlineGame();
		SharedPreferences.Editor editor = settings.edit();
	    editor.putInt(PREF_RATING, mMatch.onlineRating);
	    editor.commit();
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
		mMatch.showDialogs = true;
		SharedPreferences.Editor editor = settings.edit();
	    editor.putInt(PREF_USER_ID, userId);
	    editor.commit();
		if (screen instanceof MultiSetupScreen) {
			((MultiSetupScreen) screen).registeredUser(userId);
		} else if (screen instanceof GameScreen) {
			((GameScreen) screen).registeredUser(userId);
		}
	}
	
	public void showNewMatchDialog(final String otherPlayerName, final int otherPlayerRating) {
		if (!mMatch.showDialogs) {
			return;
		}
		
		final BattleLaserGame game = this;
		dismissDialogs();
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(game, AlertDialog.THEME_HOLO_DARK);
				String displayName = otherPlayerName;
				if (displayName.length() >= 30) {
					displayName = displayName.substring(0, 30) + "...";
				}
				mAlertDialog = builder.setTitle("Match found")
					.setCancelable(false)
					.setMessage("Player Name: " + displayName + "\n\n" + "Rating: " + otherPlayerRating)
					.setPositiveButton("Accept", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							new AcceptMatchTask(mMatch.onlineUserId).execute();
							if (screen instanceof MultiSetupScreen) {
								((MultiSetupScreen) screen).startMatch();
				    		}
						}
					}).setNegativeButton("Decline", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							if (screen instanceof MultiSetupScreen) {
								new DeclineMatchTask(mMatch.onlineUserId).execute();
				    		}
							mMatch.showDialogs = false;
						}
					}).create();
				mAlertDialog.show();
			}
		});
	}
	
	public void showUserDeclinedDialog() {
		if (!mMatch.showDialogs) {
			return;
		}
		
		final BattleLaserGame game = this;
		dismissDialogs();
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(game, AlertDialog.THEME_HOLO_DARK);
				mAlertDialog = builder.setTitle("Match found")
					.setCancelable(false)
					.setMessage("Other player declined the match.")
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							if (screen instanceof GameScreen) {
								Screen nextScreen = new MultiSetupScreen(game, true,
										mMatch);
								game.setScreen(nextScreen);
							}
							registerGCM();
							game.showProgressDialog("Connecting to server...", true);
						}
					}).create();
				mAlertDialog.show();
			}
		});
	}
	
	public void showUserForfeitDialog() {
		if (!mMatch.showDialogs) {
			return;
		}
		
		final BattleLaserGame game = this;
		dismissDialogs();
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(game, AlertDialog.THEME_HOLO_DARK);
				mAlertDialog = builder.setTitle("Match found")
					.setCancelable(false)
					.setMessage("Other player forfeit, you win!")
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							if (screen instanceof GameScreen) {
								Screen nextScreen = new MultiSetupScreen(game, true,
										mMatch);
								game.setScreen(nextScreen);
								winOnlineGame();
							}
							registerGCM();
							game.showProgressDialog("Connecting to server...", true);
						}
					}).create();
				mAlertDialog.show();
			}
		});
	}
	
	public void showProgressDialog(final String text, final boolean canceleable) {
		final BattleLaserGame game = this;
		dismissAlertDialog();
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				if (mProgressDialog == null || !mProgressDialog.isShowing()) {
					mProgressDialog = new ProgressDialog(game, ProgressDialog.THEME_HOLO_DARK);
					WindowManager.LayoutParams wmlp = mProgressDialog.getWindow().getAttributes();
					wmlp.y = screenHeight / 18;
					mProgressDialog.getWindow().setAttributes(wmlp);
					mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					mProgressDialog.setMessage(text);
					mProgressDialog.setCancelable(canceleable);
					mProgressDialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog)
						{
							new UnregisterPlayerTask(mMatch.onlineUserId).execute();
							mMatch.showDialogs = false;
						}
					});
					mProgressDialog.show();
				} else {
					mProgressDialog.setMessage(text);
					mProgressDialog.setCancelable(canceleable);
					mProgressDialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog)
						{
							new UnregisterPlayerTask(mMatch.onlineUserId).execute();
							mMatch.showDialogs = false;
						}
					});
					mProgressDialog.show();
				}
			}
		});
	}
	
	public void dismissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
	
	public void dismissAlertDialog() {
		if (mAlertDialog != null) {
			mAlertDialog.dismiss();
		}
	}
	
	public void dismissDialogs() {
		dismissProgressDialog();
		dismissAlertDialog();
	}
	
	public void checkNetworkConnection() {
		dismissDialogs();
		final BattleLaserGame game = this;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (isNetworkAvailable()) {
					Toast.makeText(game, "An error occured while connecting", Toast.LENGTH_SHORT).show();
				} else {
					AlertDialog.Builder builder = new AlertDialog.Builder(game, AlertDialog.THEME_HOLO_DARK);
					mAlertDialog = builder.setTitle("Network Error").setMessage("Please make sure that you are connected to the internet.")
						.setPositiveButton("OK", null).create();
					mAlertDialog.show();
				}
			}
		});
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
