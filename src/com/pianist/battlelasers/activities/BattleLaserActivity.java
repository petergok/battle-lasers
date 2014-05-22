package com.pianist.battlelasers.activities;

import java.io.IOException;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.pianist.battlelasers.FileIO;
import com.pianist.battlelasers.R;
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
import android.os.Vibrator;
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
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * BattleLaserGame is the main game activity of the project. It handles the
 * creation of the screen as well as the transitioning between them. It also
 * deals stores all the objects that deal with input and output to the user and
 * files.
 * 
 * @author Alex Szoke & Peter Gokhshteyn
 */
public class BattleLaserActivity extends Activity
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
	
	// Server Address
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
	
	// Dialog variables
	private ProgressDialog mProgressDialog;
	private AlertDialog mAlertDialog;
	
	// The match object that contains game data
	private Match mMatch;
	
	// Whether the guide was completed yet
	private boolean mIsGuideCompleted;
	
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
	public static final String PREF_USER_NAME = "pref_user_name";
	
	// Store the screen height 
	private static int mScreenHeight;
	
	// Store the admob ad unit id and interstial ad
	private String MY_AD_UNIT_ID = "ca-app-pub-6108834597007793/9286444460";
	private InterstitialAd interstitial;
	
	// Whether the ad is current showing
	private boolean mShowingAd = false;
	
	// Vibration handler
	private Vibrator vibrator;
	
	/**
	 * A broadcast reciever that is used to recieve broadcasts from the GCM reciever
	 */
	private BroadcastReceiver bReceiver = new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        if(intent.getAction().equals(MATCH_FOUND)) {
	        	// If a match was found, hide dialogs and update user data
	        	mMatch.showDialogs = true;
	        	String otherPlayerName = intent.getStringExtra("otherPlayerName");
	        	int playerNumber = intent.getIntExtra("playerNumber", 0);
	        	int otherPlayerRating = intent.getIntExtra("otherPlayerRating", 1000);
	        	int mapId = intent.getIntExtra("mapId", 1);
	        	
	        	// Notify the screen that the a match was found
	        	if (screen instanceof MultiSetupScreen) {
	    			((MultiSetupScreen) screen).createdMatch(otherPlayerName, mapId, playerNumber, otherPlayerRating);
	    		}
	        } else if (intent.getAction().equals(MOVE)) {
	        	// If a move was made, retrieve the move data and verify that it is a valid move
	        	Point moveStart = new Point(intent.getIntExtra("startRow", -1), intent.getIntExtra("startCol", -1));
	        	Point moveEnd = new Point(intent.getIntExtra("endRow", -1), intent.getIntExtra("endCol", -1));
	        	boolean turnRight = intent.getBooleanExtra("turnRight", false);
	        	if (moveStart.y > 6 || moveStart.y < 1 || moveEnd.y > 6 || moveEnd.y < 1 || 
	        			moveStart.x > 10 || moveStart.x < 1 || moveEnd.x > 10 || moveEnd.x < 1) {
	        		moveStart = null;
	        		moveEnd = null;
	        	}
	        	
	        	// Notify the game screen that a move was made
	        	if (screen instanceof GameScreen) {
	        		((GameScreen) screen).onlineMoveMade(moveStart, moveEnd, turnRight);
	        	}
	        } else if (intent.getAction().equals(MATCH_END)) {
	        	// If the match ended, unregister the player and show an appropriate dialog
	        	new UnregisterPlayerTask(mMatch.onlineUserId).execute();
	        	if (!mMatch.matchStarted && !mMatch.endMatch && (screen instanceof MultiSetupScreen || screen instanceof GameScreen)) {
	        		showUserDeclinedDialog();
	        	} else if (mMatch.matchStarted) {
	        		showUserForfeitDialog(false);
	        	}
	        	
	        	// Update the user's rating to the shared preferences
	        	SharedPreferences.Editor editor = BattleLaserActivity.settings.edit();
	    	    editor.putInt(BattleLaserActivity.PREF_USER_ID, 0);
	    	    editor.commit();
	        } else if (intent.getAction().equals(MATCH_START)) {
	        	// Notify the game screen if the match has started
	        	if (screen instanceof GameScreen) {
	        		dismissDialogs();
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
		mScreenHeight = getResources().getDisplayMetrics().heightPixels;
		float scaleX = (float) frameBufferWidth
				/ getResources().getDisplayMetrics().widthPixels;
		float scaleY = (float) frameBufferHeight
				/ mScreenHeight;

		// Create all the objects that run the game
		renderView = new RenderGraphics(frameBuffer, this);
		graphics = new Graphics(getAssets(), frameBuffer);
		fileIO = new FileIO(this);
		input = new Input(this, renderView, scaleX, scaleY);
		
		// Create the vibration handler
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		// Retrieve user data from shared preferences
		mMatch = new Match();
		settings = getSharedPreferences(BATTLE_LASERS_PREFS, 0);
	    mMatch.onlineRating = settings.getInt(PREF_RATING, 1000);
	    mMatch.onlineUserId = settings.getInt(PREF_USER_ID, 0);
	    mMatch.userName = settings.getString(PREF_USER_NAME, "");
	    mIsGuideCompleted = settings.getBoolean(PREF_GUIDE_COMPLETED, false);
	    
	    // Create the screen and set the view to the render view that was created
		screen = new MainMenuScreen(this, true, mMatch);
		setContentView(renderView);
		
		// Create the interstitial.
	    interstitial = new InterstitialAd(this);
	    interstitial.setAdUnitId(MY_AD_UNIT_ID);

	    // Create ad request.
	    AdRequest adRequest = new AdRequest.Builder().build();

	    // Begin loading your interstitial.
	    interstitial.loadAd(adRequest);
		
		mContext = getApplicationContext();
		
		// Register the broadcast reciever
		LocalBroadcastManager bManager = LocalBroadcastManager.getInstance(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MATCH_START);
		intentFilter.addAction(MOVE);
		intentFilter.addAction(MATCH_FOUND);
		intentFilter.addAction(MATCH_END);
		bManager.registerReceiver(bReceiver, intentFilter);
	}
	
	/**
	 * Registers this current device for GCM
	 */
	public void registerGCM() {
	    if (checkPlayServices()) {
	    	gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(mContext);

            // If the registration id is empty, then this device hasn't registered yet and it needs to register,
            // otherwise, send the old registration id to the server
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
	 * Called when the guide was started so it's only shown once
	 */
	public void guideStarted() {
		SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean(PREF_GUIDE_COMPLETED, true);
	    editor.commit();
	    mIsGuideCompleted = true;
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
		// Get the registration id from shared preferences
	    final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    
	    // Check if app was updated. If so, it must clear the registration ID
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
	    return getSharedPreferences(BattleLaserActivity.class.getSimpleName(),
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

	                // Send the registration id to the server and store it in shared preferences
	                sendRegistrationIdToBackend();
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
	 * Sends the registration ID to the server
	 */
	private void sendRegistrationIdToBackend() {
		new SendRegistrationIdTask(this, regid, mMatch.onlineRating, mMatch.userName).execute();
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
		// When the game is stopped, unregister the player depending on what part of the online registration
		// process they are at. This helps keep the server in sync.
		if (mMatch != null && mMatch.onlineUserId != 0 && mMatch.matchStarted) {
			if (!mShowingAd) {
				loseOnlineGame();
				new UnregisterPlayerTask(mMatch.onlineUserId).execute();
			}
		} else if (mMatch != null && mMatch.isOnline) {
			if (!mShowingAd) {
				new UnregisterPlayerTask(mMatch.onlineUserId).execute();
			}
		}
		super.onStop();
	}
	
	/**
	 * Called when user loses an online game to update and store the rating
	 */
	public void loseOnlineGame() {
		mMatch.loseOnlineGame();
		SharedPreferences.Editor editor = settings.edit();
	    editor.putInt(PREF_RATING, mMatch.onlineRating);
	    editor.commit();
	}
	
	/**
	 * Called when user wins an online game to update and store the rating
	 */
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
	
	/**
	 * Called when the user is registered and notifies the proper screen of this event
	 * 
	 * @param userId the user id given from the registration
	 */
	public void registeredUser(int userId) {
		SharedPreferences.Editor editor = settings.edit();
	    editor.putInt(PREF_USER_ID, userId);
	    editor.commit();
		if (screen instanceof MultiSetupScreen) {
			((MultiSetupScreen) screen).registeredUser(userId);
		} else if (screen instanceof GameScreen) {
			((GameScreen) screen).registeredUser(userId);
		}
	}
	
	/**
	 * Shows the new match dialog
	 * 
	 * @param otherPlayerName the name of the other player
	 * @param otherPlayerRating the other player's rating
	 */
	public void showNewMatchDialog(final String otherPlayerName, final int otherPlayerRating) {
		if (!mMatch.showDialogs) {
			return;
		}
		
		final BattleLaserActivity game = this;
		dismissDialogs();
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				// Crate a new dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(game, AlertDialog.THEME_HOLO_DARK);
				
				// If the player name is too long, shorten it
				String displayName = otherPlayerName;
				if (displayName.length() >= 50) {
					displayName = displayName.substring(0, 50) + "...";
				}
				
				// Set the dialog properties
				mAlertDialog = builder.setTitle("Match Found")
					.setCancelable(false)
					.setMessage("Player Name: " + displayName + "\n\n" + "Rating: " + otherPlayerRating)
					.setPositiveButton("Accept", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// If the accept button is clicked, notify the server and screen
							new AcceptMatchTask(mMatch.onlineUserId).execute();
							if (screen instanceof MultiSetupScreen) {
								((MultiSetupScreen) screen).startMatch();
				    		}
						}
					}).setNegativeButton("Decline", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// If the decline button is clicked, notify the server and screen
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
	
	/**
	 * Shows a dialog when the other user declines a match
	 */
	public void showUserDeclinedDialog() {
		if (!mMatch.showDialogs) {
			return;
		}
		
		final BattleLaserActivity game = this;
		dismissDialogs();
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				// Create and set the properties of the dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(game, AlertDialog.THEME_HOLO_DARK);
				mAlertDialog = builder.setTitle("Match Declined")
					.setCancelable(false)
					.setMessage("Other player declined the match.")
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// Reregister the player when they dismiss the dialog
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
	
	/**
	 * Dialog shows when the other user forfeit
	 * 
	 * @param disconnected whether the forfeit occurred because of a disconnection
	 */
	public void showUserForfeitDialog(final boolean disconnected) {
		if (!mMatch.showDialogs) {
			return;
		}
		
		final BattleLaserActivity game = this;
		dismissDialogs();
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				// Create a dialog and set its properties based on the type of forfeit
				AlertDialog.Builder builder = new AlertDialog.Builder(game, AlertDialog.THEME_HOLO_DARK);
				mAlertDialog = builder.setTitle("Game Over")
					.setCancelable(false)
					.setMessage(disconnected ? "Other player disconnected, you win!" : "Other player forfeit, you win!")
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// Depending on the location of the user in the game flow when the other player disconnects,
							// notify the correct screen and either reregister or win the game
							if (screen instanceof GameScreen) {
								Screen nextScreen = new MultiSetupScreen(game, true,
										mMatch);
								game.setScreen(nextScreen);
								winOnlineGame();
							} else {
								registerGCM();
								game.showProgressDialog("Connecting to server...", true);
							}
						}
					}).create();
				mAlertDialog.show();
			}
		});
	}
	
	/**
	 * Dialog displayed to update the user's name
	 */
	public void updateUsername() {
		final BattleLaserActivity game = this;
		dismissDialogs();
		
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				// Inflate the edit text view and add it to the dialog
				LayoutInflater inflater = getLayoutInflater();
				final EditText input = (EditText)(inflater.inflate(R.layout.edit_text, null).findViewById(R.id.edit_text));
				AlertDialog.Builder builder = new AlertDialog.Builder(game, AlertDialog.THEME_HOLO_DARK);
				mAlertDialog = builder.setCancelable(false)
					.setTitle("Enter a Display Name:")
					.setPositiveButton("Finish", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// When the user finishes, update the shared preferences with the new name
							if (input.getText() != null && !TextUtils.isEmpty(input.getText().toString())) {
								mMatch.userName = input.getText().toString();
								SharedPreferences.Editor editor = settings.edit();
							    editor.putString(PREF_USER_NAME, mMatch.userName);
							    editor.commit();
							    registerGCM();
							}
						}
					}).create();
				mAlertDialog.setView(input);
				mAlertDialog.show();
			}
		});
	}
	
	/**
	 * Shows the dialog that confirms if a user wants to exit. This is shown when a user tries to exit mid-game
	 */
	public void showConfirmExitDialog() {
		final BattleLaserActivity game = this;
		dismissDialogs();
		
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				// Create the dialog and set its properties
				AlertDialog.Builder builder = new AlertDialog.Builder(game, AlertDialog.THEME_HOLO_DARK);
				mAlertDialog = builder.setTitle("Warning")
					.setCancelable(false)
					.setMessage("Are you sure you want to exit? You will forfeit the match.")
					.setPositiveButton("Yes", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							// If the user confirms, exit the game
							if (screen instanceof GameScreen) {
								((GameScreen) screen).exitGame();
							}
						}
					})
					.setNegativeButton("No", null).create();
				mAlertDialog.show();
			}
		});
	}
	
	/**
	 * Shows a progress dialog with the given text
	 * 
	 * @param text the text to display
	 * @param canceleable whether the dialog is canceleable
	 */
	public void showProgressDialog(final String text, final boolean canceleable) {
		final BattleLaserActivity game = this;
		dismissAlertDialog();
		
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				// If a dialog is already showing, update the old one, otherwise create a new one
				if (mProgressDialog == null || !mProgressDialog.isShowing()) {
					mProgressDialog = new ProgressDialog(game, ProgressDialog.THEME_HOLO_DARK);
					
					// Move the dialog over the start match button
					WindowManager.LayoutParams wmlp = mProgressDialog.getWindow().getAttributes();
					wmlp.y = mScreenHeight / 18;
					mProgressDialog.getWindow().setAttributes(wmlp);
					
					mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					mProgressDialog.setMessage(text);
					mProgressDialog.setCancelable(canceleable);
					mProgressDialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog)
						{
							// If the user canceled, unregister them from the server
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
							// If the user canceled, unregister them from the server
							new UnregisterPlayerTask(mMatch.onlineUserId).execute();
							mMatch.showDialogs = false;
						}
					});
					mProgressDialog.show();
				}
			}
		});
	}
	
	/**
	 * Dismisses the progress dialog
	 */
	public void dismissProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}
	
	/**
	 * Dismisses the alert dialog
	 */
	public void dismissAlertDialog() {
		if (mAlertDialog != null) {
			mAlertDialog.dismiss();
		}
	}
	
	/**
	 * Dismisses any dialogs that are open
	 */
	public void dismissDialogs() {
		dismissProgressDialog();
		dismissAlertDialog();
	}
	
	/**
	 * Checks the network connection and if it isn't available, notify the user that they need to connect to the internet
	 */
	public void checkNetworkConnection() {
		dismissDialogs();
		final BattleLaserActivity game = this;
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Check if the network is available and notify the user of the device state accordingly
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
	
	/**
	 * Checks if the network is available
	 * 
	 * @return if the network is available
	 */
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	/**
	 * Returns if a dialog is showing
	 * 
	 * @return if a dialog is showing
	 */
	public boolean isDialogShowing() {
		return (mAlertDialog != null && mAlertDialog.isShowing()) || mProgressDialog != null && mProgressDialog.isShowing();
	}
	
	/**
	 * Displays the interstitial ad and loads another one immediately for next time
	 */
	public void displayInterstitial() {
		final BattleLaserActivity activity = this;
		runOnUiThread(new Runnable() {

			@Override
			public void run()
			{
				if (interstitial.isLoaded()) {
					mShowingAd = true;
					
			    	interstitial.show();
			    	
			    	interstitial.setAdListener(new AdListener() {
				    	@Override
				    	public void onAdClosed() {
				    		mShowingAd = false;
				    	}
				    });
			    	
			    	// Create the interstitial.
				    interstitial = new InterstitialAd(activity);
				    interstitial.setAdUnitId(MY_AD_UNIT_ID);
					
					// Create ad request.
				    AdRequest adRequest = new AdRequest.Builder().build();

				    // Begin loading the interstitial.
				    interstitial.loadAd(adRequest);
			    } else {
			    	mShowingAd = false;
			    }
			}
		});
	}
	
	/**
	 * Vibrate the phone for a given length
	 * 
	 * @param length the length to vibrate the phone for
	 */
	public void vibrate(int length) {
		if (vibrator != null) {
			vibrator.vibrate(length);
		}
	}
	
	public void setGuideCompleted(boolean completed) {
		mIsGuideCompleted = completed;
	}
	
	public boolean isGuideCompleted() {
		return mIsGuideCompleted;
	}
	
	public boolean isShowingAd() {
		return mShowingAd;
	}
}
