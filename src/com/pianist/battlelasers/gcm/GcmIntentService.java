package com.pianist.battlelasers.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.pianist.battlelasers.activities.BattleLaserActivity;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Intent service that receives GCM intents
 * 
 * @author Peter Gokhshteyn
 *
 */
public class GcmIntentService extends IntentService {
	
	private static String TAG = "GcmIntentService";
	
    public static final int NOTIFICATION_ID = 1;
    NotificationCompat.Builder builder;
    BattleLaserActivity game;

    public GcmIntentService() {
        super(BattleLaserActivity.SENDER_ID);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    	// Get message data from the intent
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
        	// Filter messages based on message type
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            	Log.d(TAG, "Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
            	Log.d(TAG, "Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.d(TAG, "Received: " + extras.toString());
                parseAndReportMessage(extras);
            }
        }
    }
    
    /**
     * Called when a message is successfully received, it parses and reports the message to the main activity
     * 
     * @param data the message data
     */
    private void parseAndReportMessage(Bundle data) {
    	try {
    		
    		// Retrieve the message type
    		String messageType = data.getString("messageType");
    		if (messageType == null) {
    			return;
    		}
    		
    		// Based on the message type, retrieve the data associated with it and send a broadcast to the main activity
    		if (messageType.equals("matchFound")) {
    			String otherPlayerName = data.getString("otherPlayerName");
    			int playerNumber = Integer.parseInt(data.getString("playerNumber"));
    			int mapId = Integer.parseInt(data.getString("mapId"));
    			int otherPlayerRating = Integer.parseInt(data.getString("otherPlayerRating"));
    			Intent intent = new Intent(BattleLaserActivity.MATCH_FOUND);
    			intent.putExtra("otherPlayerName", otherPlayerName);
    			intent.putExtra("otherPlayerRating", otherPlayerRating);
    			intent.putExtra("playerNumber", playerNumber);
    			intent.putExtra("mapId", mapId);
    			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    		} else if (messageType.equals("move")) {
    			int startRow = Integer.parseInt(data.getString("startRow"));
    			int startCol = Integer.parseInt(data.getString("startCol"));
    			int endRow = Integer.parseInt(data.getString("endRow"));
    			int endCol = Integer.parseInt(data.getString("endCol"));
    			boolean turnRight = data.getString("turnRight").equals("true");
    			Intent intent = new Intent(BattleLaserActivity.MOVE);
    			intent.putExtra("startRow", startRow);
    			intent.putExtra("startCol", startCol);
    			intent.putExtra("endRow", endRow);
    			intent.putExtra("endCol", endCol);
    			intent.putExtra("turnRight", turnRight);
    			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    		} else if (messageType.equals("matchStart")) {
    			Intent intent = new Intent(BattleLaserActivity.MATCH_START);
    			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    		} else if (messageType.equals("matchEnd")) {
    			Intent intent = new Intent(BattleLaserActivity.MATCH_END);
    			LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    		}
    		
    	} catch (Exception e) {
    		Log.e("EXCEPTION", "exception: ", e);
    	}
    }
}